package com.example.mytravelapp.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.mytravelapp.models.PointOfInterest
import com.example.mytravelapp.models.Place
import com.example.mytravelapp.repository.PlaceRepository
import com.example.mytravelapp.utils.Resource
import java.util.Date

class PlaceViewModel(application: Application) : AndroidViewModel(application){

    private val placeRepository = PlaceRepository(application)

    fun getPlaceList() = placeRepository.getPlaceList()

    fun getPoisList() = placeRepository.getPoisList()

    fun insertPlace(place : Place): MutableLiveData<Resource<Long>> {
        return placeRepository.insertPlace(place)
    }

    fun getPOIsOnDateRange(startDate: String, endDate: String) = placeRepository.poisOnDateRange(startDate, endDate)

    fun getPOIUpcomingEvents(currentDate: String) = placeRepository.poisUpcomingEvents(currentDate)

    fun insertPOIs(pois : PointOfInterest): MutableLiveData<Resource<Long>> {
        return placeRepository.insertPOIs(pois)
    }

    fun deletePlaceUsingId(placeId : String): MutableLiveData<Resource<Int>> {
        return placeRepository.deletePlaceUsingId(placeId)
    }

    fun deletePOIsUsingId(poisID : String): MutableLiveData<Resource<Int>> {
        return placeRepository.deletePOIsUsingId(poisID)
    }

    fun getAllPOIs(): LiveData<List<PointOfInterest>> {
        return placeRepository.getAllPOIs()
    }

    fun updateParticularFieldOfPlace(id: String, name: String, startDate : Date, endDate : Date, description: String, photo : ByteArray?, rate : Int): MutableLiveData<Resource<Int>> {
        return placeRepository.updateParticularFieldOfPlace(id, name, startDate, endDate, description, photo, rate)
    }
}