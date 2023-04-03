package com.github.ljts42.hw7_im.ui

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.github.ljts42.hw7_im.databinding.FragmentChannelsBinding
import com.github.ljts42.hw7_im.network.ServerApi
import com.github.ljts42.hw7_im.utils.Constants
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.io.File
import java.io.FileNotFoundException
import java.io.IOException

class ChannelsFragment : Fragment() {
    private lateinit var binding: FragmentChannelsBinding
    private val chatViewModel: ChatViewModel by activityViewModels()

    private lateinit var serverApi: ServerApi
    private lateinit var jsonAdapter: JsonAdapter<List<String>>

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = FragmentChannelsBinding.inflate(inflater, container, false)
        chatViewModel.channels.observe(viewLifecycleOwner) { channels ->
            (binding.channelsRecyclerView.adapter as ChannelsAdapter).updateData(channels)
        }
        return binding.root
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initRetrofit()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initRecyclerView()
        initChannels()
    }

    private fun initRetrofit() {
        val moshi = Moshi.Builder().build()
        val retrofit = Retrofit.Builder().baseUrl(Constants.BASE_URL)
            .addConverterFactory(MoshiConverterFactory.create(moshi)).build()
        serverApi = retrofit.create(ServerApi::class.java)

        val type = Types.newParameterizedType(List::class.java, String::class.java)
        jsonAdapter = moshi.adapter(type)
    }

    private fun initRecyclerView() {
        binding.channelsRecyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter =
                ChannelsAdapter(chatViewModel.channels.value ?: emptyList(), onClickChannel = {
                    (activity as MainActivity).changeChat(it)
                })
        }
        binding.channelsRecyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                val layoutManager =
                    binding.channelsRecyclerView.layoutManager as LinearLayoutManager
                if (layoutManager.findLastVisibleItemPosition() == chatViewModel.messages.size - 1) {
                    if (chatViewModel.channels.value?.isEmpty() == true) {
                        initChannels()
                    }
                }
            }

            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                val layoutManager =
                    binding.channelsRecyclerView.layoutManager as LinearLayoutManager
                if (newState == RecyclerView.SCROLL_STATE_IDLE && layoutManager.findLastVisibleItemPosition() == chatViewModel.messages.size - 1) {
                    if (chatViewModel.channels.value?.isEmpty() == true) {
                        initChannels()
                    }
                }
            }
        })
    }

    private fun initChannels() {
        if (chatViewModel.channels.value?.isEmpty() != false) {
            CoroutineScope(Dispatchers.IO).launch {
                var list = readChannels()
                if (list.isEmpty()) {
                    list = loadChannels()
                    if (list.isNotEmpty()) {
                        writeChannels(list)
                    }
                }
                withContext(Dispatchers.Main) {
                    chatViewModel.channels.postValue(list)
                }
            }
        }
    }

    private fun readChannels(): List<String> {
        try {
            val file = File(context?.filesDir, Constants.FILENAME)
            val json = file.inputStream().bufferedReader().use { it.readText() }
            Log.d("readChannels", "success")
            return jsonAdapter.fromJson(json) ?: listOf()
        } catch (e: IOException) {
            Log.e("readChannels", "Failed to read file with channels: ${e.message}")
        } catch (e: FileNotFoundException) {
            Log.e("readChannels", "Failed to read file with channels: ${e.message}")
        }
        return listOf()
    }

    private suspend fun loadChannels(): List<String> {
        return try {
            Log.d("loadChannels", "success")
            serverApi.getChannels()
        } catch (e: IOException) {
            Log.e("loadChannels", "Failed to load channels from server: ${e.message}")
            listOf()
        }
    }

    private fun writeChannels(list: List<String>) {
        try {
            val file = File(context?.filesDir, Constants.FILENAME)
            val json = jsonAdapter.toJson(list)
            file.outputStream().bufferedWriter().use { it.write(json) }
            Log.d("writeChannels", "success")
        } catch (e: IOException) {
            Log.e("writeChannels", "Failed to write channels to file: ${e.message}")
        } catch (e: FileNotFoundException) {
            Log.e("writeChannels", "Failed to write channels to file: ${e.message}")
        }
    }
}
