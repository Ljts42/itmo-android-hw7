package com.github.ljts42.hw7_im.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.github.ljts42.hw7_im.R

class ChannelsAdapter(
    private var channels: List<String>, private val onClickChannel: (String) -> Unit
) : RecyclerView.Adapter<ChannelsAdapter.ChannelViewHolder>() {
    class ChannelViewHolder(root: View) : RecyclerView.ViewHolder(root) {
        private val channelNameView = root.findViewById<TextView>(R.id.channel_name_view)

        fun bind(
            name: String, onClickChannel: (String) -> Unit
        ) {
            channelNameView.text = name
            itemView.setOnClickListener {
                onClickChannel(name)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChannelViewHolder {
        return ChannelViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.channel_item, parent, false)
        )
    }

    override fun getItemCount(): Int = channels.size

    override fun onBindViewHolder(holder: ChannelViewHolder, position: Int) =
        holder.bind(channels[position], onClickChannel)

    fun updateData(newChannels: List<String>) {
        channels = newChannels
        notifyDataSetChanged()
    }
}