package com.example.mytravelapp

import android.app.DatePickerDialog
import android.app.Dialog
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ImageButton
import android.widget.PopupMenu
import androidx.lifecycle.ViewModelProvider
import com.example.mytravelapp.adapters.POIsRVViewBindingAdapter
import com.example.mytravelapp.databinding.ActivityMainBinding
import com.example.mytravelapp.databinding.ActivityMyPoisBinding
import com.example.mytravelapp.utils.Status
import com.example.mytravelapp.utils.setUpDialog
import com.example.mytravelapp.utils.shortToastShow
import com.example.mytravelapp.viewmodels.PlaceViewModel
import com.example.travelapplicationkotlin.adapters.PlaceRVViewBindingAdapter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class MyPOIsActivity : AppCompatActivity() {

    private val mainBinding: ActivityMyPoisBinding by lazy {
        ActivityMyPoisBinding.inflate(layoutInflater)
    }

    private val loadingDialog : Dialog by lazy {
        Dialog(this).apply {
            setUpDialog(R.layout.loading_dialog)
        }
    }

    private val placeViewModel : PlaceViewModel by lazy {
        ViewModelProvider(this)[PlaceViewModel::class.java]
    }

    private val poisRecyclerViewAdapter: POIsRVViewBindingAdapter by lazy {
        POIsRVViewBindingAdapter()
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(mainBinding.root)

        mainBinding.poisRV.adapter = poisRecyclerViewAdapter
        callGetPoisList()

        val filterOptionsMenu: ImageButton = findViewById(R.id.orderBy)
        val popupMenu = PopupMenu(this, filterOptionsMenu)
        popupMenu.menuInflater.inflate(R.menu.filter_options, popupMenu.menu)
        popupMenu.setOnMenuItemClickListener { menuIntem ->
            orderBy(menuIntem.itemId)
            true
        }

        filterOptionsMenu.setOnClickListener {
            popupMenu.show()
        }
    }

    private fun orderBy(itemId: Int) {
        when(itemId){

            R.id.nearEvent -> {
                getUpcomingEvents()
            }
            R.id.dateRange -> {
                showDatePickerDialog()
            }

            R.id.reset -> {
                callGetPoisList()
            }

        }

    }

    private fun callGetPoisList(){
        loadingDialog.show()
        CoroutineScope(Dispatchers.Main).launch {
            placeViewModel.getPoisList().collect{
                when(it.status){
                    Status.LOADING -> {
                        loadingDialog.show()
                    }
                    Status.SUCCESS -> {
                        it.data?.collect{ poisList ->
                            loadingDialog.dismiss()
                            poisRecyclerViewAdapter.addAllPOIs(poisList)
                        }
                    }
                    Status.ERROR -> {
                        loadingDialog.dismiss()
                        it.message?.let { it1 -> shortToastShow(it1) }
                    }
                }
            }
        }
    }

    private fun getUpcomingEvents(){
        val currentDate = getCurrentDateString()
        loadingDialog.show()
        CoroutineScope(Dispatchers.Main).launch {
            placeViewModel.getPOIUpcomingEvents(currentDate).collect{
                when(it.status){
                    Status.LOADING -> {
                        loadingDialog.show()
                    }
                    Status.SUCCESS -> {
                        it.data?.collect{ poisList ->
                            loadingDialog.dismiss()
                            poisRecyclerViewAdapter.addAllPOIs(poisList)
                        }
                    }
                    Status.ERROR -> {
                        loadingDialog.dismiss()
                        it.message?.let { it1 -> shortToastShow(it1) }
                    }
                }
            }
        }
    }

    private fun showDatePickerDialog() {
        val currentDate = Calendar.getInstance()
        val startYear = currentDate.get(Calendar.YEAR)
        val startMonth = currentDate.get(Calendar.MONTH)
        val startDay = currentDate.get(Calendar.DAY_OF_MONTH)

        val datePickerDialog = DatePickerDialog(
            this,
            { _, year, month, day ->
                val endDatePickerDialog = DatePickerDialog(
                    this,
                    { _, endYear, endMonth, endDay ->
                        val startDate = formatDate(year, month, day)
                        val endDate = formatDate(endYear, endMonth, endDay)

                        loadingDialog.show()
                        CoroutineScope(Dispatchers.Main).launch {
                            placeViewModel.getPOIsOnDateRange(startDate, endDate).collect{
                                when(it.status){
                                    Status.LOADING -> {
                                        loadingDialog.show()
                                    }
                                    Status.SUCCESS -> {
                                        it.data?.collect{ poisList ->
                                            loadingDialog.dismiss()
                                            poisRecyclerViewAdapter.addAllPOIs(poisList)
                                        }
                                    }
                                    Status.ERROR -> {
                                        loadingDialog.dismiss()
                                        it.message?.let { it1 -> shortToastShow(it1) }
                                    }
                                }
                            }
                        }
                    },
                    startYear,
                    startMonth,
                    startDay
                )

                endDatePickerDialog.show()
            },
            startYear,
            startMonth,
            startDay
        )

        datePickerDialog.show()
    }

    private fun formatDate(year: Int, month: Int, day: Int): String {
        val calendar = Calendar.getInstance()
        calendar.set(year, month, day)
        val dateFormat = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
        return dateFormat.format(calendar.time)
    }

    fun getCurrentDateString(): String {
        val calendar = Calendar.getInstance()
        val dateFormat = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
        return dateFormat.format(calendar.time)
    }

}