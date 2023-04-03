package com.github.ljts42.hw7_im.data

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [MessageModel::class], version = 1)
abstract class ChatDatabase : RoomDatabase() {
    abstract fun chatDao(): ChatDao
}