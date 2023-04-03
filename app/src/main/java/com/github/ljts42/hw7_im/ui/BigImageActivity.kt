package com.github.ljts42.hw7_im.ui

import android.os.Bundle
import android.view.MenuItem
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import com.github.ljts42.hw7_im.R
import com.github.ljts42.hw7_im.utils.Constants
import com.squareup.picasso.Picasso

class BigImageActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_big_image)

        val imageView: ImageView = findViewById(R.id.image_big)
        val link = intent.getStringExtra("imageUrl")
        Picasso.get().load("${Constants.BASE_URL}/img/$link").noFade()
            .placeholder(R.drawable.ic_broken_image).into(imageView)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                finish()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }
}