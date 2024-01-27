package com.example.mytravelapp.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.mytravelapp.models.PointOfInterest
import com.example.mytravelapp.converters.TypeConverter
import com.example.mytravelapp.dao.PlaceDao
import com.example.mytravelapp.models.Place

@Database(
    entities = [Place::class, PointOfInterest::class],
    version = 3,
    exportSchema = false)

@TypeConverters(TypeConverter::class)
abstract class PlaceDataBase : RoomDatabase() {

    abstract val placeDao : PlaceDao

    companion object{
        @Volatile
        private var INSTANCE : PlaceDataBase? = null

        fun getInstance(context: Context) : PlaceDataBase{
            synchronized(this){
                return INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    PlaceDataBase::class.java,
                    "place_db"
                )
                    .fallbackToDestructiveMigration()
                    .build().also {
                        INSTANCE = it
                    }
            }
        }
    }
}