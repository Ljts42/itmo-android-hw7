package com.github.ljts42.hw7_im.network

import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.*

interface ServerApi {
    @POST("/messages")
    suspend fun sendMessage(@Body message: Message): Response<Int>

    @Multipart
    @POST("/messages")
    suspend fun sendImage(
        @Part("msg") message: RequestBody, @Part image: MultipartBody.Part
    ): Response<Int>

    @GET("/channels")
    suspend fun getChannels(): List<String>

    @GET("/channel/{to}")
    suspend fun getMessages(
        @Path("to") channel: String, @Query("lastKnownId") start: Int, @Query("limit") count: Int
    ): List<Message>
}