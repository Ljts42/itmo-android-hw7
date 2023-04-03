package com.github.ljts42.hw7_im.network

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class ImageData(val link: String)