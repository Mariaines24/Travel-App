package com.example.mytravelapp.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.example.mytravelapp.models.PointOfInterest
import com.example.mytravelapp.models.Place
import kotlinx.coroutines.flow.Flow
import java.util.Date

@Dao
interface PlaceDao {

    @Query("SELECT * FROM Place ORDER BY `startDate` DESC")
    fun getPlaceList() : Flow<List<Place>>

    @Query("SELECT * FROM PointOfInterest ORDER BY `startDate` ASC")
    fun getPoisList() : Flow<List<PointOfInterest>>

    @Query("SELECT * FROM PointOfInterest WHERE startDate >= :startDate AND endDate <= :endDate")
    fun poisOnDateRange(startDate : String, endDate : String) : Flow<List<PointOfInterest>>

    @Query("SELECT * FROM PointOfInterest WHERE `startDate` > :currentDate ORDER BY `startDate` ASC")
    fun getUpcomingEvents(currentDate: String): Flow<List<PointOfInterest>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlace(place : Place): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPOIs(pois : PointOfInterest): Long

    @Query("DELETE FROM Place WHERE id == :id")
    suspend fun deletePlaceUsingId(id: String) : Int

    @Query("DELETE FROM PointOfInterest WHERE id == :id")
    suspend fun deletePOIsUsingId(id: String) : Int

    @Query("SELECT *  FROM PointOfInterest")
    fun getAllPOIs(): LiveData<List<PointOfInterest>>

    @Query("UPDATE Place SET `placeName` = :name, `startDate` = :startDate, endDate = :endDate, description = :description, photo = :photo, rate = :rate WHERE id = :id")
    suspend fun updateParticularFieldOfPlace(id: String, name: String, startDate : Date, endDate : Date, description: String, photo : ByteArray?, rate: Int): Int
}