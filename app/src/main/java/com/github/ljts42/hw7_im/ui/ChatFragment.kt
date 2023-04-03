package com.github.ljts42.hw7_im.ui

import android.content.Intent
import android.database.sqlite.SQLiteException
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.room.Room
import com.github.ljts42.hw7_im.data.ChatDao
import com.github.ljts42.hw7_im.data.ChatDatabase
import com.github.ljts42.hw7_im.data.DataType
import com.github.ljts42.hw7_im.data.MessageModel
import com.github.ljts42.hw7_im.databinding.FragmentChatBinding
import com.github.ljts42.hw7_im.network.*
import com.github.ljts42.hw7_im.utils.Constants
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.io.ByteArrayOutputStream
import java.io.FileNotFoundException
import java.io.IOException

class ChatFragment : Fragment() {
    private lateinit var binding: FragmentChatBinding
    private val chatViewModel: ChatViewModel by activityViewModels()
    private lateinit var chatAdapter: ChatAdapter

    private lateinit var moshi: Moshi
    private lateinit var serverApi: ServerApi

    private lateinit var messageDatabase: ChatDatabase
    private lateinit var messageDao: ChatDao

    private var isLoading = false

    private val choosePhoto =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val inputStream =
                        uri?.let { requireContext().contentResolver.openInputStream(it) }
                    val bitmap = BitmapFactory.decodeStream(inputStream)

                    if (bitmap != null) {
                        val messageBody = moshi.adapter(Message::class.java).toJson(
                            Message(
                                from = Constants.USERNAME, to = chatViewModel.channel
                            )
                        ).toRequestBody("application/json".toMediaTypeOrNull())

                        val byteArrayOutputStream = ByteArrayOutputStream()
                        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream)
                        val imageBytes = byteArrayOutputStream.toByteArray()
                        val imageBody = imageBytes.toRequestBody("image/jpeg".toMediaTypeOrNull())

                        serverApi.sendImage(
                            messageBody, MultipartBody.Part.createFormData(
                                "picture", "${System.currentTimeMillis()}.jpg", imageBody
                            )
                        )
                    }
                } catch (e: IOException) {
                    Log.e("SendImage", "Failed to send image: ${e.message}")
                } catch (e: FileNotFoundException) {
                    Log.e("SendImage", "Failed to send image: ${e.message}")
                }
            }
        }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = FragmentChatBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initDatabase()
        initRetrofit()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initRecyclerView()
        initMessages()
        binding.apply {
            btnSendMsg.setOnClickListener {
                sendMessage()
            }
            btnAttachImg.setOnClickListener {
                choosePhoto.launch("image/*")
            }
        }
    }

    private fun initDatabase() {
        messageDatabase = Room.databaseBuilder(
            requireContext(), ChatDatabase::class.java, "${Constants.DB_NAME}.db"
        ).build()
        messageDao = messageDatabase.chatDao()
    }

    private fun initRetrofit() {
        moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()

        val retrofit = Retrofit.Builder().baseUrl(Constants.BASE_URL)
            .addConverterFactory(MoshiConverterFactory.create(moshi)).build()

        serverApi = retrofit.create(ServerApi::class.java)
    }

    private fun initRecyclerView() {
        binding.chatRecyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            chatAdapter = ChatAdapter(chatViewModel.messages, onClickImage = {
                if (it.data?.Image != null) {
                    val intent = Intent(context, BigImageActivity::class.java)
                    intent.putExtra("imageUrl", it.data.Image.link)
                    intent.flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
                    startActivity(intent)
                }
            })
            adapter = chatAdapter
        }

        binding.chatRecyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                val layoutManager = binding.chatRecyclerView.layoutManager as LinearLayoutManager
                if (layoutManager.findLastVisibleItemPosition() == chatViewModel.messages.size - 1) {
                    getMessages()
                }
            }

            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                val layoutManager = binding.chatRecyclerView.layoutManager as LinearLayoutManager
                if (newState == RecyclerView.SCROLL_STATE_IDLE && layoutManager.findLastVisibleItemPosition() == chatViewModel.messages.size - 1) {
                    getMessages()
                }
            }
        })
    }

    private fun initMessages() {
        if (chatViewModel.messages.isEmpty()) {
            CoroutineScope(Dispatchers.IO).launch {
                val newMessages = try {
                    messageDao.getMessages(chatViewModel.channel)
                } catch (e: SQLiteException) {
                    Log.e("initRecycleView", "Error getting message from database: ${e.message}")
                    listOf()
                }
                withContext(Dispatchers.Main) {
                    val start = chatViewModel.messages.size
                    chatViewModel.messages.addAll(newMessages.map {
                        Message(
                            it.id,
                            it.from,
                            it.to,
                            if (it.type == DataType.TextData) Data(TextData(it.data), null)
                            else Data(null, ImageData(it.data)),
                            it.time
                        )
                    })
                    chatAdapter.notifyItemRangeInserted(
                        start, newMessages.size
                    )
                    if (chatViewModel.messages.isEmpty()) {
                        getMessages()
                    }
                }
            }
        }
    }

    private fun getMessages(count: Int = 20) {
        if (isLoading) return
        isLoading = true

        CoroutineScope(Dispatchers.IO).launch {
            val newMessages = try {
                serverApi.getMessages(chatViewModel.channel, chatViewModel.lastId(), count)
            } catch (e: IOException) {
                Log.e("getMessages", "Error getting messages from server: ${e.message}")
                listOf()
            }
            try {
                newMessages.forEach {
                    messageDao.addMessage(
                        MessageModel(
                            it.id!!,
                            it.from,
                            it.to!!,
                            if (it.data?.Text != null) DataType.TextData else DataType.ImageData,
                            it.data!!.Text?.text ?: it.data.Image!!.link,
                            it.time!!
                        )
                    )
                }
            } catch (e: SQLiteException) {
                Log.e("getMessages", "Error adding message to database: ${e.message}")
            }
            withContext(Dispatchers.Main) {
                if (newMessages.isNotEmpty()) {
                    val start = chatViewModel.messages.size
                    chatViewModel.messages.addAll(newMessages)
                    chatAdapter.notifyItemRangeInserted(
                        start, newMessages.size
                    )
                }
                isLoading = false
            }
        }
    }

    private fun sendMessage() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                serverApi.sendMessage(
                    Message(
                        from = Constants.USERNAME,
                        to = chatViewModel.channel,
                        data = Data(TextData(binding.inputField.text.toString()), null)
                    )
                )
            } catch (e: Exception) {
                Log.e("SendMessage", "Failed to send message: ${e.message}")
            }
        }
        binding.inputField.setText("")
    }

    fun updateMessages() {
        chatAdapter.notifyDataSetChanged()
        initMessages()
    }
}