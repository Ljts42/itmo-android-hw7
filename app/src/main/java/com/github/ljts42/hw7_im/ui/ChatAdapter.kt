package com.github.ljts42.hw7_im.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.github.ljts42.hw7_im.R
import com.github.ljts42.hw7_im.network.Message
import com.github.ljts42.hw7_im.utils.Constants
import com.squareup.picasso.Picasso
import java.text.SimpleDateFormat
import java.util.*

class ChatAdapter(
    private var messages: MutableList<Message>, private val onClickImage: (Message) -> Unit
) : RecyclerView.Adapter<ChatAdapter.MessageViewHolder>() {

    class MessageViewHolder(root: View) : RecyclerView.ViewHolder(root) {
        private val usernameView = root.findViewById<TextView>(R.id.channel_name_view)
        private val msgIdView = root.findViewById<TextView>(R.id.msg_id_view)
        private val timeView = root.findViewById<TextView>(R.id.time_view)
        private val textView = root.findViewById<TextView>(R.id.text_view)
        val imageView: ImageView = root.findViewById(R.id.image_view)

        fun bind(message: Message) {
            if (message.data?.Text != null) {
                textView.visibility = View.VISIBLE
                imageView.visibility = View.GONE

                textView.text = message.data.Text.text
            } else if (message.data?.Image != null) {
                textView.visibility = View.GONE
                imageView.visibility = View.VISIBLE

                Picasso.get().load("${Constants.BASE_URL}/thumb/${message.data.Image.link}")
                    .noFade().placeholder(R.drawable.ic_broken_image).into(imageView)
            }
            usernameView.text = message.from
            msgIdView.text = message.id.toString()
            message.time?.let {
                timeView.text = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(it))
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        return if (messages[position].data?.Text != null) 0 else 1
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessageViewHolder {
        val holder = MessageViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.message_item, parent, false)
        )
        if (viewType == 1) {
            holder.imageView.setOnClickListener {
                onClickImage(messages[holder.adapterPosition])
            }
        }
        return holder
    }

    override fun getItemCount(): Int = messages.size

    override fun onBindViewHolder(holder: MessageViewHolder, position: Int) =
        holder.bind(messages[position])
}