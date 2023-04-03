package com.github.ljts42.hw7_im.network

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class Data(
    val Text: TextData? = null, val Image: ImageData? = null
)