package com.example.mytravelapp.models

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.android.gms.maps.model.LatLng

@Entity
data class PointOfInterest(
    @PrimaryKey(autoGenerate = false)
    @ColumnInfo(name = "id")
    val id : String,

    @ColumnInfo(name = "title")
    val title: String,

    @ColumnInfo(name = "description")
    val description: String,

    @ColumnInfo(name = "startDate")
    val startDate: String,

    @ColumnInfo(name = "endDate")
    val endDate: String,

    @ColumnInfo(name = "latLng")
    val latLng: LatLng
)
