package com.github.ljts42.hw7_im.ui

import android.content.res.Configuration
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.github.ljts42.hw7_im.R
import com.github.ljts42.hw7_im.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var chatViewModel: ChatViewModel
    private var channelsFragment: ChannelsFragment? = null
    private var chatFragment: ChatFragment? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        chatViewModel = ViewModelProvider(this)[ChatViewModel::class.java]
        initFragments()
    }

    private fun initFragments() {
        channelsFragment = ChannelsFragment()
        when (resources.configuration.orientation) {
            Configuration.ORIENTATION_PORTRAIT -> {
                supportFragmentManager.beginTransaction().replace(
                    R.id.fragment_container, channelsFragment!!
                ).commit()
            }
            Configuration.ORIENTATION_LANDSCAPE -> {
                chatFragment = ChatFragment()
                supportFragmentManager.beginTransaction().replace(
                    R.id.channels_container, channelsFragment!!
                ).commit()
                supportFragmentManager.beginTransaction()
                    .replace(R.id.chat_container, chatFragment!!).commit()
            }
            else -> {}
        }
    }

    fun changeChat(name: String) {
        val flag = (name != chatViewModel.channel && chatFragment != null)
        chatViewModel.channel = name
        chatViewModel.messages.clear()

        if (resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT) {
            if (chatFragment == null) {
                chatFragment = ChatFragment()
            }
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, chatFragment!!).addToBackStack(null).commit()
            supportFragmentManager.executePendingTransactions()
        }
        if (flag) {
            chatFragment!!.updateMessages()
        }
    }
}