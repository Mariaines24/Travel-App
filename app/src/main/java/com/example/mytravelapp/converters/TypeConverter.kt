package com.example.mytravelapp.converters

import androidx.room.TypeConverter
import com.google.android.gms.maps.model.LatLng
import java.util.Date

class TypeConverter {
    @TypeConverter
    fun fromTimestamp(value:Long): Date {
        return Date(value)
    }

    @TypeConverter
    fun dateToTimestamp(date: Date): Long {
        return date.time
    }

    @TypeConverter
    fun fromLatLng(latLng: LatLng): String {
        return latLng.latitude.toString() + "," + latLng.longitude.toString()
    }

    @TypeConverter
    fun toLatLng(latLngString: String): LatLng {
        val parts = latLngString.split(",").toTypedArray()
        val latitude = parts[0].toDouble()
        val longitude = parts[1].toDouble()
        return LatLng(latitude, longitude)
    }
}