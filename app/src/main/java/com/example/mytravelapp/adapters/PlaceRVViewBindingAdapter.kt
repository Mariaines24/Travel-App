package com.example.travelapplicationkotlin.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.mytravelapp.databinding.ViewAllPlacesBinding
import com.example.mytravelapp.models.Place
import java.text.SimpleDateFormat
import java.util.Locale

class PlaceRVViewBindingAdapter(
    private val deleteUpdateViewCallback : (type: String, position: Int, place: Place) -> Unit
) :
    RecyclerView.Adapter<PlaceRVViewBindingAdapter.ViewHolder>() {

    private val placeList  = arrayListOf<Place>()

    class ViewHolder(val viewAllPlacesBinding : ViewAllPlacesBinding)
        : RecyclerView.ViewHolder(viewAllPlacesBinding.root)

    fun addAllPlaces(newPlaceList : List<Place>){
        placeList.clear()
        placeList.addAll(newPlaceList)
        notifyDataSetChanged()
    }
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        return ViewHolder(
            ViewAllPlacesBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val place = placeList[position]
        holder.viewAllPlacesBinding.titleTxt.text = place.name
        holder.viewAllPlacesBinding.descrTxt.text = place.description

        val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

        holder.viewAllPlacesBinding.dateStartTxt.text = dateFormat.format(place.startDate)
        holder.viewAllPlacesBinding.dateEndTxt.text = dateFormat.format(place.endDate)

        holder.viewAllPlacesBinding.deleteImg.setOnClickListener {
            if(holder.adapterPosition != -1) {
                deleteUpdateViewCallback("Delete", holder.adapterPosition, place)
            }
        }

        holder.viewAllPlacesBinding.editImg.setOnClickListener {
            if(holder.adapterPosition != -1) {
                deleteUpdateViewCallback("Update", holder.adapterPosition, place)
            }
        }

        holder.viewAllPlacesBinding.viewPlacesImg.setOnClickListener {
            if(holder.adapterPosition != -1) {
                deleteUpdateViewCallback("View", holder.adapterPosition, place)
            }
        }
    }

    override fun getItemCount(): Int {
        return placeList.size
    }
}