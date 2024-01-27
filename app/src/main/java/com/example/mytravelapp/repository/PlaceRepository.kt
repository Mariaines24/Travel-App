package com.example.mytravelapp.repository

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.mytravelapp.models.PointOfInterest
import com.example.mytravelapp.database.PlaceDataBase
import com.example.mytravelapp.models.Place
import com.example.mytravelapp.utils.Resource
import com.example.mytravelapp.utils.Resource.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import java.lang.Exception
import java.util.Date

class PlaceRepository (application: Application) {

    private val placeDao = PlaceDataBase.getInstance(application).placeDao

    fun getPlaceList() = flow {
        emit(Loading())
        try {
            val result = placeDao.getPlaceList()
            emit(Success(result))
        }
        catch (e: Exception){
            emit(Error(e.message.toString()))
        }
    }

    fun getPoisList() = flow {
        emit(Loading())
        try {
            val result = placeDao.getPoisList()
            emit(Success(result))
        }
        catch (e: Exception){
            emit(Error(e.message.toString()))
        }
    }

    fun poisOnDateRange(startDate: String, endDate: String) = flow {
        emit(Loading())
        try {
            val result = placeDao.poisOnDateRange(startDate, endDate)
            emit(Success(result))
        }
        catch (e: Exception){
            emit(Error(e.message.toString()))
        }
    }

    fun poisUpcomingEvents(currentDate: String) = flow {
        emit(Loading())
        try {
            val result = placeDao.getUpcomingEvents(currentDate)
            emit(Success(result))
        }
        catch (e: Exception){
            emit(Error(e.message.toString()))
        }
    }

    fun insertPlace(place : Place)= MutableLiveData<Resource<Long>>().apply {
        postValue(Loading())
        try {
            CoroutineScope(Dispatchers.IO).launch {
                val result = placeDao.insertPlace(place)
                postValue(Success(result))
            }
        }
        catch (e : Exception){
            postValue(Error(e.message.toString()))
        }
    }

    fun insertPOIs(pois : PointOfInterest)= MutableLiveData<Resource<Long>>().apply {
        postValue(Loading())
        try {
            CoroutineScope(Dispatchers.IO).launch {
                val result = placeDao.insertPOIs(pois)
                postValue(Success(result))
            }
        }
        catch (e : Exception){
            postValue(Error(e.message.toString()))
        }
    }

    fun deletePlaceUsingId(placeId : String)= MutableLiveData<Resource<Int>>().apply {
        postValue(Loading())
        try {
            CoroutineScope(Dispatchers.IO).launch {
                val result = placeDao.deletePlaceUsingId(placeId)
                postValue(Success(result))
            }
        }
        catch (e : Exception){
            postValue(Error(e.message.toString()))
        }
    }

    fun deletePOIsUsingId(poisID : String)= MutableLiveData<Resource<Int>>().apply {
        postValue(Loading())
        try {
            CoroutineScope(Dispatchers.IO).launch {
                val result = placeDao.deletePOIsUsingId(poisID)
                postValue(Success(result))
            }
        }
        catch (e : Exception){
            postValue(Error(e.message.toString()))
        }
    }

    fun getAllPOIs() : LiveData<List<PointOfInterest>> {
        return placeDao.getAllPOIs()
    }

    fun updateParticularFieldOfPlace(id: String, name: String, startDate : Date, endDate : Date, description: String, photo : ByteArray?, rate : Int) = MutableLiveData<Resource<Int>>().apply {
        postValue(Loading())
        try {
            CoroutineScope(Dispatchers.IO).launch {
                val result = placeDao.updateParticularFieldOfPlace(id, name, startDate, endDate, description, photo, rate)
                postValue(Success(result))
            }
        }
        catch (e : Exception){
            postValue(Error(e.message.toString()))
        }
    }
}