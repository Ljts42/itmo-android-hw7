package com.github.ljts42.hw7_im.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.github.ljts42.hw7_im.utils.Constants

@Entity(tableName = Constants.DB_NAME)
data class MessageModel(
    @PrimaryKey val id: Int,
    @ColumnInfo(name = "user") val from: String,
    @ColumnInfo(name = "channel") val to: String,
    @ColumnInfo(name = "type") val type: DataType,
    @ColumnInfo(name = "data") val data: String,
    @ColumnInfo(name = "time") val time: Long
)