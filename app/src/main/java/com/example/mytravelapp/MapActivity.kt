package com.example.mytravelapp

import android.Manifest
import android.app.DatePickerDialog
import android.app.Dialog
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.PopupMenu
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.lifecycle.ViewModelProvider
import com.example.mytravelapp.models.PointOfInterest
import com.example.mytravelapp.utils.setUpDialog
import com.example.mytravelapp.utils.shortToastShow
import com.example.mytravelapp.utils.validateCoherenceDate
import com.example.mytravelapp.utils.validateEditText
import com.example.mytravelapp.viewmodels.PlaceViewModel
import com.google.android.gms.common.api.Status
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.widget.AutocompleteSupportFragment
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class MapActivity : AppCompatActivity(), OnMapReadyCallback {

    private var myGoogleMap: GoogleMap? = null
    private lateinit var autocompleteFragment: AutocompleteSupportFragment

    private val dynamicMarkers = mutableListOf<Marker>()

    private lateinit var selectedMarker: Marker
    private lateinit var pos: LatLng
    private lateinit var namePOIs: TextInputEditText
    private lateinit var descPOIs: TextInputEditText
    private lateinit var startDateButton: Button
    private lateinit var endDateButton: Button
    private lateinit var zoomIn: ImageButton
    private lateinit var zoomOut: ImageButton
    private lateinit var viewAllPoisBtn: ImageButton

    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient


    private val addPlaceOfInterestDialog: Dialog by lazy {
        Dialog(this).apply {
            setUpDialog(R.layout.pois_dialog)
        }
    }

    private val viewPOIsDialog: Dialog by lazy {
        Dialog(this).apply {
            setUpDialog(R.layout.view_poi)
        }
    }

    private val loadingDialog: Dialog by lazy {
        Dialog(this).apply {
            setUpDialog(R.layout.loading_dialog)
        }
    }

    private val placeViewModel: PlaceViewModel by lazy {
        ViewModelProvider(this)[PlaceViewModel::class.java]
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.map)

        viewAllPoisBtn = findViewById(R.id.myPOIs)
        viewAllPoisBtn.setOnClickListener {
            val intent = Intent(this, MyPOIsActivity::class.java)
            startActivity(intent)
        }

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)

        zoomIn = findViewById(R.id.zoomIn)
        zoomIn.setOnClickListener {
            zoomIn()
        }

        zoomOut = findViewById(R.id.zoomOut)
        zoomOut.setOnClickListener {
            zoomOut()
        }

        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.mapFragment) as SupportMapFragment
        mapFragment.getMapAsync(this)

        //Map types
        val mapOptionsMenu: ImageButton = findViewById(R.id.mapOptionsMenu)
        val popupMenu = PopupMenu(this, mapOptionsMenu)
        popupMenu.menuInflater.inflate(R.menu.map_options, popupMenu.menu)
        popupMenu.setOnMenuItemClickListener { menuIntem ->
            changeMap(menuIntem.itemId)
            true
        }

        mapOptionsMenu.setOnClickListener {
            popupMenu.show()
        }

        //Search Places
        Places.initialize(applicationContext, getString(R.string.google_map_api_key))
        autocompleteFragment =
            supportFragmentManager.findFragmentById(R.id.autocomplete_fragment) as AutocompleteSupportFragment
        autocompleteFragment.setPlaceFields(
            listOf(
                Place.Field.ID,
                Place.Field.ADDRESS,
                Place.Field.LAT_LNG
            )
        )
        autocompleteFragment.setOnPlaceSelectedListener(object : PlaceSelectionListener {
            override fun onError(p0: Status) {
                Toast.makeText(this@MapActivity, "Some error while searching", Toast.LENGTH_SHORT)
                    .show()
            }

            override fun onPlaceSelected(place: Place) {
                val id = place.id
                val add = place.address
                val latLng = place.latLng!!

                val marker = addPOIs(latLng)
                marker.title = add
                marker.snippet = id

                zoomOnMap(latLng)
            }
        })
    }

    //Change map type
    private fun changeMap(itemId: Int) {
        when (itemId) {
            R.id.normalMap -> myGoogleMap?.mapType = GoogleMap.MAP_TYPE_NORMAL
            R.id.hybridMap -> myGoogleMap?.mapType = GoogleMap.MAP_TYPE_HYBRID
            R.id.satelliteMap -> myGoogleMap?.mapType = GoogleMap.MAP_TYPE_SATELLITE
            R.id.terrainMap -> myGoogleMap?.mapType = GoogleMap.MAP_TYPE_TERRAIN
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        myGoogleMap = googleMap
        getLocation()
        placeViewModel.getAllPOIs().observe(this) { pois ->
            pois.forEach { poi ->
                val marker = addPOIs(poi.latLng)
                marker.title = poi.title
                marker.snippet =
                    "Description: ${poi.description}\n${poi.startDate} - ${poi.endDate}\nLatitude: ${poi.latLng.latitude}\nLongitude: ${poi.latLng.longitude}"
            }
        }

        //"Add" Simple Marker
        myGoogleMap?.setOnMapClickListener {
            clearDynamicMarkers()

            val dynamicMarker = addPOIs(it)
            dynamicMarkers.add(dynamicMarker)
            zoomOnMarker(LatLng(it.latitude, it.longitude))
        }

        //Add Point Of Interest
        myGoogleMap?.setOnMapLongClickListener { position ->
            clearDynamicMarkers()
            selectedMarker = addPOIs(position)
            addPlaceOfInterestDialog.show()

            pos = position
            //close add POIs window
            val addCloseImg = addPlaceOfInterestDialog.findViewById<ImageView>(R.id.closeImg)
            addCloseImg.setOnClickListener {
                addPlaceOfInterestDialog.dismiss()
                selectedMarker.remove()
            }

            namePOIs = addPlaceOfInterestDialog.findViewById(R.id.PlaceName)
            val namePOIsL = addPlaceOfInterestDialog.findViewById<TextInputLayout>(R.id.placeNameL)

            namePOIs.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
                override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
                override fun afterTextChanged(s: Editable) {
                    validateEditText(namePOIs, namePOIsL)
                }
            })

            descPOIs = addPlaceOfInterestDialog.findViewById(R.id.Description)
            val descPOIsL = addPlaceOfInterestDialog.findViewById<TextInputLayout>(R.id.descL)

            descPOIs.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
                override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
                override fun afterTextChanged(s: Editable) {
                    validateEditText(descPOIs, descPOIsL)
                }
            })

            startDateButton = addPlaceOfInterestDialog.findViewById(R.id.startDateButton)
            endDateButton = addPlaceOfInterestDialog.findViewById(R.id.endDateButton)

            startDateButton.setOnClickListener {
                showDatePicker(startDateButton)
            }

            endDateButton.setOnClickListener {
                showDatePicker(endDateButton)
            }

            val saveButton = addPlaceOfInterestDialog.findViewById<Button>(R.id.savePois)
            saveButton.setOnClickListener {
                savePlaceInfo(selectedMarker)
                addPlaceOfInterestDialog.dismiss()
            }
        }

        //View Info Of Point Of Interest
        myGoogleMap?.setOnMarkerClickListener {marker ->
            val addCloseImg = viewPOIsDialog.findViewById<ImageView>(R.id.closeImg)
            addCloseImg.setOnClickListener { viewPOIsDialog.dismiss() }

            val deletePOIs = viewPOIsDialog.findViewById<Button>(R.id.deletePois)
            deletePOIs.setOnClickListener {

                    placeViewModel.deletePOIsUsingId(marker.id).observe(this) { result ->
                        when (result.status) {
                            com.example.mytravelapp.utils.Status.LOADING -> {
                                loadingDialog.show()
                            }

                            com.example.mytravelapp.utils.Status.SUCCESS -> {
                                loadingDialog.dismiss()
                                if (result.data != -1) {
                                    shortToastShow("Point of Interest deleted successfully")
                                    marker.remove()
                                    viewPOIsDialog.dismiss()
                                }
                            }

                            com.example.mytravelapp.utils.Status.ERROR -> {
                                loadingDialog.dismiss()
                                result.message?.let { message -> shortToastShow(message) }
                            }
                        }
                    }
                true
            }


            val titlePOI = marker.title
            val snippet = marker.snippet

            val parts = snippet?.split("\n")
            val description = parts?.get(0)?.substringAfter("Description: ")
            val datePart = parts?.get(1)?.substringAfter("Date: ")
            val dateParts = datePart?.split(" - ")

            val startDateStr = dateParts?.get(0)?.trim()
            val endDateStr = dateParts?.get(1)?.trim()


            val title = viewPOIsDialog.findViewById<EditText>(R.id.PlaceName)
            val startDate = viewPOIsDialog.findViewById<Button>(R.id.startDateButton)
            val endDate = viewPOIsDialog.findViewById<Button>(R.id.endDateButton)
            val desc = viewPOIsDialog.findViewById<EditText>(R.id.Description)

            title.setText(titlePOI)
            startDate.text = "$startDateStr"
            endDate.text = "$endDateStr"
            desc.setText(description)

            viewPOIsDialog.show()
            true
        }
    }

    //Add marker
    private fun addPOIs(position: LatLng): Marker {
        val marker = myGoogleMap?.addMarker(
            MarkerOptions()
                .position(position)
                .title("Marker")
        )
        return marker!!
    }

    private fun clearDynamicMarkers() {
        for (marker in dynamicMarkers) {
            marker.remove()
        }
        dynamicMarkers.clear()
    }

    //Current Location Marker
    private fun addCurrentLocation(icon: Int, position: LatLng) {
        myGoogleMap?.addMarker(
            MarkerOptions()
                .position(position)
                .title("Current Location")
                .icon(BitmapDescriptorFactory.fromResource(icon))
        )
    }

    //Zoom to a place after search
    private fun zoomOnMap(latLong: LatLng) {
        val newLatLngZoom = CameraUpdateFactory.newLatLngZoom(latLong, 20f)
        myGoogleMap?.animateCamera(newLatLngZoom)
    }

    //Zoom after clicking on map
    private fun zoomOnMarker(latLong: LatLng) {
        val newLatLngZoom = CameraUpdateFactory.newLatLngZoom(latLong, 8f)
        myGoogleMap?.animateCamera(newLatLngZoom)
    }

    private fun savePlaceInfo(marker : Marker) {

        val name = namePOIs.text.toString()
        val description = descPOIs.text.toString()
        val startDateStr = startDateButton.text.toString()
        val endDateStr = endDateButton.text.toString()
        val lat = pos.latitude
        val long = pos.longitude

        if (datesStrToDate(startDateStr, endDateStr)) {
            selectedMarker.title = name
            selectedMarker.snippet =
                "Description: $description\n$startDateStr  -  $endDateStr\nLatitude: $lat\nLongitude: $long"

            val newPOIs = PointOfInterest(
                marker.id,
                name,
                description,
                startDateStr,
                endDateStr,
                selectedMarker.position
            )

            placeViewModel.insertPOIs(newPOIs).observe(this) {
                when (it.status) {
                    com.example.mytravelapp.utils.Status.LOADING -> {
                        loadingDialog.show()
                    }

                    com.example.mytravelapp.utils.Status.SUCCESS -> {
                        loadingDialog.dismiss()
                        if (it.data?.toInt() != -1) {
                            shortToastShow("Marker added successfully")
                        }
                    }

                    com.example.mytravelapp.utils.Status.ERROR -> {
                        loadingDialog.dismiss()
                        it.message?.let { it1 -> shortToastShow(it1) }
                    }
                }
            }
            clearFields()
        } else {
            selectedMarker.remove()
            clearFields()
        }
    }

        private fun getLocation(){
            if(ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED){
                    ActivityCompat.requestPermissions(this,
                        arrayOf(Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION), 100)
                    return
            }

            fusedLocationProviderClient.lastLocation.addOnSuccessListener { location ->
                if (location != null) {
                    addCurrentLocation(R.drawable.location, LatLng(location.latitude, location.longitude))
                    zoomOnMap(LatLng(location.latitude, location.longitude))
                } else {
                    Log.e("MapActivity", "Last known location is null")
                }
            }.addOnFailureListener { e ->
                Log.e("MapActivity", "Error getting last known location: $e")
            }

        }

    //Dates
    private fun showDatePicker(button: Button) {
        val datePickerDialog = DatePickerDialog(
            this,
            { _, year, month, day ->
                val formattedDate = String.format("%02d-%02d-%d", day, month + 1, year)
                button.text = formattedDate
            },
            Calendar.getInstance().get(Calendar.YEAR),
            Calendar.getInstance().get(Calendar.MONTH),
            Calendar.getInstance().get(Calendar.DAY_OF_MONTH)
        )
        datePickerDialog.show()
    }

    private fun datesStrToDate(startDate: String, endDate: String): Boolean {
        val dateFormat = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())

        try {
            val start = dateFormat.parse(startDate)
            val end = dateFormat.parse(endDate)

            return validateCoherenceDate(this, start, end)

        } catch (e: Exception) {
            e.printStackTrace()
            return false
        }
    }

    private fun clearFields() {
        namePOIs.text = null
        descPOIs.text = null
        startDateButton.text = "Start Date"
        endDateButton.text = "End Date"
    }

    private fun zoomIn() {
        myGoogleMap?.animateCamera(CameraUpdateFactory.zoomIn())
    }

    private fun zoomOut() {
        myGoogleMap?.animateCamera(CameraUpdateFactory.zoomOut())
    }

}

