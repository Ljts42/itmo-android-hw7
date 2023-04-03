package com.github.ljts42.hw7_im.ui

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.github.ljts42.hw7_im.network.Message

class ChatViewModel : ViewModel() {
    var messages: MutableList<Message> = mutableListOf()
    var channels = MutableLiveData<List<String>>()
    var channel = "1@channel"

    fun lastId(): Int {
        return if (messages.isEmpty()) {
            if (channel == "1@channel") 5700 else 0
        } else messages.last().id!!
    }
}