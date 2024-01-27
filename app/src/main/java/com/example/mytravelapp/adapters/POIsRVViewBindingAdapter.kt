package com.example.mytravelapp.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.mytravelapp.models.PointOfInterest
import com.example.mytravelapp.R

class POIsRVViewBindingAdapter :
RecyclerView.Adapter<POIsRVViewBindingAdapter.ViewHolder>() {

    private val poisList  = arrayListOf<PointOfInterest>()

    class ViewHolder(viewAllPOIs : View): RecyclerView.ViewHolder(viewAllPOIs){
            val titlePOIs : TextView = itemView.findViewById(R.id.titleTxt)
            val descPOIs : TextView = itemView.findViewById(R.id.descrTxt)
            val startDatePOIs : TextView = itemView.findViewById(R.id.dateStartTxt)
            val endDatePOIs : TextView = itemView.findViewById(R.id.dateEndTxt)
        }


    fun addAllPOIs(newPOIsList : List<PointOfInterest>){
        poisList.clear()
        poisList.addAll(newPOIsList)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        return ViewHolder(
           LayoutInflater.from(parent.context)
               .inflate(R.layout.view_all_pois, parent, false)
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val pois = poisList[position]

        holder.titlePOIs.text = pois.title
        holder.descPOIs.text = pois.description
        holder.startDatePOIs.text = pois.startDate
        holder.endDatePOIs.text = pois.endDate
    }

    override fun getItemCount(): Int {
        return poisList.size
    }
}