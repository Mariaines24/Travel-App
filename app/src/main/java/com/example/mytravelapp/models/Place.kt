package com.example.mytravelapp.models

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

@Entity
data class Place(
    @PrimaryKey(autoGenerate = false)
    @ColumnInfo(name = "id")
    val id : String,

    @ColumnInfo(name = "placeName")
    val name : String,

    @ColumnInfo(name = "startDate")
    val startDate : Date,

    @ColumnInfo(name = "endDate")
    val endDate : Date,

    @ColumnInfo(name = "description")
    val description : String,

    @ColumnInfo(name = "photo")
    val photo : ByteArray? = null,

    @ColumnInfo(name = "rate")
    val rate : Int
)

