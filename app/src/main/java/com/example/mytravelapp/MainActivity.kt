package com.example.mytravelapp

import android.app.DatePickerDialog
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.RatingBar
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.ViewModelProvider
import com.example.mytravelapp.databinding.ActivityMainBinding
import com.example.mytravelapp.models.Place
import com.example.mytravelapp.utils.Status
import com.example.mytravelapp.utils.clearEditText
import com.example.mytravelapp.utils.shortToastShow
import com.example.mytravelapp.utils.setUpDialog
import com.example.mytravelapp.utils.validateCoherenceDate
import com.example.mytravelapp.utils.validateEditText
import com.example.mytravelapp.utils.validateRatingBar
import com.example.mytravelapp.viewmodels.PlaceViewModel
import com.example.travelapplicationkotlin.adapters.PlaceRVViewBindingAdapter
import com.github.drjacky.imagepicker.ImagePicker
import com.github.drjacky.imagepicker.constant.ImageProvider
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.sql.Types.NULL
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.UUID

class MainActivity : AppCompatActivity() {

    lateinit var addStartDateBtn : Button
    lateinit var addEndDateBtn : Button
    private var isStartDateSelected = false
    private var isEndDateSelected = false


    lateinit var addPhoto : ImageView
    lateinit var addPhotoBtn : Button
    private var selectedImageUri: Uri? = null


    lateinit var updateStartDateBtn : Button
    lateinit var updateEndDateBtn : Button
    lateinit var updatePhoto : ImageView
    lateinit var updatePhotoBtn : Button
    private var updatedImageUri: Uri? = null

    lateinit var viewStartDateBtn : Button
    lateinit var viewEndDateBtn : Button
    lateinit var viewPhoto : ImageView
    private var viewImageUri: Uri? = null

    val launcher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){
        if(it.resultCode == RESULT_OK){
            val uri = it.data?.data
            uri?.let { u ->
                addPhoto.setImageURI(u)
                selectedImageUri = u
            }
        }
    }

    val launcherUP = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){
        if(it.resultCode == RESULT_OK){
            val uri = it.data?.data
            uri?.let { u ->
                updatePhoto.setImageURI(u)
                updatedImageUri = u
            }
        }
    }

    private val mainBinding: ActivityMainBinding by lazy {
        ActivityMainBinding.inflate(layoutInflater)
    }

    private val addPlaceDialog : Dialog by lazy {
        Dialog(this).apply {
            setUpDialog(R.layout.add_place)
        }
    }

    private val updatePlaceDialog : Dialog by lazy {
        Dialog(this).apply {
            setUpDialog(R.layout.update_place)
        }
    }

    private val loadingDialog : Dialog by lazy {
        Dialog(this).apply {
            setUpDialog(R.layout.loading_dialog)
        }
    }

    private val viewPlaceDialog : Dialog by lazy {
        Dialog(this).apply {
            setUpDialog(R.layout.view_info_place)
        }
    }

    private val placeViewModel : PlaceViewModel by lazy {
        ViewModelProvider(this)[PlaceViewModel::class.java]
    }

    lateinit var viewWorldMapBtn : Button
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(mainBinding.root)

        viewWorldMapBtn = mainBinding.viewMaps
        viewWorldMapBtn.setOnClickListener {
            val intent = Intent(this, MapActivity::class.java)
            startActivity(intent)
        }

        //Add place
        //close add place window
        val addCloseImg = addPlaceDialog.findViewById<ImageView>(R.id.closeImg)
        addCloseImg.setOnClickListener { addPlaceDialog.dismiss() }

        //placeName
        val addETTitle = addPlaceDialog.findViewById<TextInputEditText>(R.id.PlaceName)
        val addETTitleL = addPlaceDialog.findViewById<TextInputLayout>(R.id.placeNameL)

        addETTitle.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
            override fun afterTextChanged(s: Editable) {
                validateEditText(addETTitle, addETTitleL)
            }
        })

        //Dates
        addStartDateBtn = addPlaceDialog.findViewById(R.id.startDateButton)
        addStartDateBtn.setOnClickListener {
            showDatePickerStart()
        }

        addEndDateBtn = addPlaceDialog.findViewById(R.id.endDateButton)
        addEndDateBtn.setOnClickListener {
            showDatePickerEnd()

        }

        //Description
        val addETDesc = addPlaceDialog.findViewById<TextInputEditText>(R.id.Description)
        val addETDescL = addPlaceDialog.findViewById<TextInputLayout>(R.id.descL)

        addETDesc.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
            override fun afterTextChanged(s: Editable) {
                validateEditText(addETDesc, addETDescL)
            }
        })

        //Add Photo
        addPhoto = addPlaceDialog.findViewById(R.id.addPhotos)
        addPhotoBtn = addPlaceDialog.findViewById(R.id.addPhotoBtn)
        addPhotoBtn.setOnClickListener {
            ImagePicker.with(this)
                .provider((ImageProvider.BOTH))
                .createIntentFromDialog { i ->
                    launcher.launch(i) }
        }

        //Rate
        val rateBar = addPlaceDialog.findViewById<RatingBar>(R.id.ratingBar)

        val initialIconResource = R.drawable.fotos
        mainBinding.addNewPlace.setOnClickListener {
            clearEditText(addETTitle, addETTitleL)
            clearEditText(addETDesc, addETDescL)
            addStartDateBtn.setText("Start Date")
            addEndDateBtn.setText("End Date")
            selectedStartDate = Date()
            selectedEndDate = Date()
            addPhoto.setImageResource(initialIconResource)
            rateBar.rating = 0.0f

            addPlaceDialog.show()
        }

        val savePlaceBtn = addPlaceDialog.findViewById<Button>(R.id.addPlace)
        savePlaceBtn.setOnClickListener {
            val rate = rateBar.rating.toInt()

            if (validateEditText(addETTitle, addETTitleL) &&
                validateEditText(addETDesc, addETDescL) &&
                isStartDateSelected && isEndDateSelected &&
                validateCoherenceDate(this, selectedStartDate, selectedEndDate) &&
                validateRatingBar(this, rateBar)){

                val newPlace: Place?
                if (selectedImageUri != null) {
                    val imageBytes: ByteArray? = uriToByteArray(this, selectedImageUri!!)
                    addPlaceDialog.dismiss()
                    newPlace = Place(
                        UUID.randomUUID().toString(),
                        addETTitle.text.toString().trim(),
                        selectedStartDate,
                        selectedEndDate,
                        addETDesc.text.toString().trim(),
                        imageBytes,
                        rate
                    )
                } else {
                    addPlaceDialog.dismiss()
                    newPlace = Place(
                        UUID.randomUUID().toString(),
                        addETTitle.text.toString().trim(),
                        selectedStartDate,
                        selectedEndDate,
                        addETDesc.text.toString().trim(),
                        null,
                        rate
                    )
                }
                placeViewModel.insertPlace(newPlace).observe(this) {
                    when (it.status) {
                        Status.LOADING -> {
                            loadingDialog.show()
                        }

                        Status.SUCCESS -> {
                            loadingDialog.dismiss()
                            if (it.data?.toInt() != -1) {
                                shortToastShow("Place added successfully")
                            }
                        }

                        Status.ERROR -> {
                            loadingDialog.dismiss()
                            it.message?.let { it1 -> shortToastShow(it1) }
                        }
                    }
                }
            }
            else {
                Toast.makeText(this, "Please select start and end date for your place!", Toast.LENGTH_SHORT).show()
            }
        }

        //View Place
        val viewCloseImg = viewPlaceDialog.findViewById<ImageView>(R.id.closeImg)
        viewCloseImg.setOnClickListener { viewPlaceDialog.dismiss() }

        //Place Name
        val viewPlaceName = viewPlaceDialog.findViewById<TextInputEditText>(R.id.PlaceName)

        //Dates
        viewStartDateBtn = viewPlaceDialog.findViewById(R.id.startDateButton)
        viewEndDateBtn = viewPlaceDialog.findViewById(R.id.endDateButton)

        //Description
        val viewDesc = viewPlaceDialog.findViewById<TextInputEditText>(R.id.Description)

        //Photo
        viewPhoto = viewPlaceDialog.findViewById(R.id.viewPhotos)

        //Rate
        val viewRateBar = viewPlaceDialog.findViewById<RatingBar>(R.id.ratingBar)





        //Update Place
        //close update window
        val updateCloseImg = updatePlaceDialog.findViewById<ImageView>(R.id.closeImg)
        updateCloseImg.setOnClickListener { updatePlaceDialog.dismiss() }

        //placeName
        val updateETTitle = updatePlaceDialog.findViewById<TextInputEditText>(R.id.PlaceName)
        val updateETTitleL = updatePlaceDialog.findViewById<TextInputLayout>(R.id.placeNameL)

        updateETTitle.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
            override fun afterTextChanged(s: Editable) {
                validateEditText(updateETTitle, updateETTitleL)
            }
        })

        //Dates
        updateStartDateBtn = updatePlaceDialog.findViewById(R.id.startDateButton)
        updateEndDateBtn = updatePlaceDialog.findViewById(R.id.endDateButton)

        //Description
        val updateETDesc = updatePlaceDialog.findViewById<TextInputEditText>(R.id.Description)
        val updateETDescL = updatePlaceDialog.findViewById<TextInputLayout>(R.id.descL)

        updateETDesc.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
            override fun afterTextChanged(s: Editable) {
                validateEditText(updateETDesc, updateETDescL)
            }
        })

        //Update Photo
        updatePhoto = updatePlaceDialog.findViewById(R.id.updatePhotos)
        updatePhotoBtn = updatePlaceDialog.findViewById(R.id.updatePhotoBtn)
        updatePhotoBtn.setOnClickListener {
            ImagePicker.with(this)
                .provider((ImageProvider.BOTH))
                .createIntentFromDialog { i ->
                    launcherUP.launch(i) }
        }

        val removePhoto = updatePlaceDialog.findViewById<ImageView>(R.id.deletePhoto)
        removePhoto.setOnClickListener {
            updatePhoto.setImageResource(initialIconResource)
            updatedImageUri = null
            removePhoto.visibility = View.GONE
        }

        //Rate
        val updateRateBar = updatePlaceDialog.findViewById<RatingBar>(R.id.ratingBar)



        val updatePlaceBtn = updatePlaceDialog.findViewById<Button>(R.id.updatePlace)

        val placeRecyclerViewAdapter = PlaceRVViewBindingAdapter{ type, position, place ->
            if(type == "Delete") {
                placeViewModel
                    .deletePlaceUsingId(place.id)
                    .observe(this) {
                        when (it.status) {
                            Status.LOADING -> {
                                loadingDialog.show()
                            }

                            Status.SUCCESS -> {
                                loadingDialog.dismiss()
                                if (it.data != -1) {
                                    shortToastShow("Place deleted successfully")
                                }
                            }

                            Status.ERROR -> {
                                loadingDialog.dismiss()
                                it.message?.let { it1 -> shortToastShow(it1) }
                            }
                        }
                    }
            }
            else if(type == "View"){

                viewPlaceName.setText(place.name)
                viewStartDateBtn.setText(SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(place.startDate))
                viewEndDateBtn.setText(SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(place.endDate))
                viewDesc.setText(place.description)
                if (place.photo != null) {
                    viewImageUri = byteArrayToUri(this, place.photo)
                    viewPhoto.setImageURI(viewImageUri)
                } else {
                    viewImageUri = null
                    viewPhoto.setImageResource(initialIconResource)
                }
                viewRateBar.rating = place?.rate?.toFloat() ?: 0.0f

                viewPlaceDialog.show()

            }

            else if(type == "Update"){
                if(place.photo != null) {
                    updatedImageUri = byteArrayToUri(this, place.photo)
                    updatePhoto.setImageURI(updatedImageUri)
                    removePhoto.visibility = View.VISIBLE
                }
                else {
                    updatePhoto.setImageResource(initialIconResource)
                }

                updateETTitle.setText(place.name)
                updateETDesc.setText(place.description)
                updateStartDateBtn.setText(SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(place.startDate))
                updateEndDateBtn.setText(SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(place.endDate))
                updateRateBar.rating = place?.rate?.toFloat() ?: 0.0f

                updateStartDateBtn.setOnClickListener {
                    showDatePickerStart()
                }
                updateEndDateBtn.setOnClickListener {
                    showDatePickerEnd()
                }


                updatePlaceBtn.setOnClickListener {

                    val imageBytesUP: ByteArray? =
                        updatedImageUri?.let { it1 -> uriToByteArray(this, it1) }
                    val updateRate = updateRateBar.rating.toInt()

                    if ( validateEditText(updateETTitle, updateETTitleL)
                        &&  validateEditText(updateETDesc, updateETDescL)
                        && validateCoherenceDate(this, selectedStartDate, selectedEndDate)){

                        updatePlaceDialog.dismiss()
                        loadingDialog.show()

                        placeViewModel
                            .updateParticularFieldOfPlace(
                                place.id,
                                updateETTitle.text.toString().trim(),
                                selectedStartDate,
                                selectedEndDate,
                                updateETDesc.text.toString().trim(),
                                imageBytesUP,
                                updateRate
                            )
                            .observe(this) {
                                when (it.status) {
                                    Status.LOADING -> {
                                        loadingDialog.show()
                                    }

                                    Status.SUCCESS -> {
                                        loadingDialog.dismiss()
                                        if (it.data != -1) {
                                            shortToastShow("Place updated successfully")
                                        }
                                    }

                                    Status.ERROR -> {
                                        loadingDialog.dismiss()
                                        it.message?.let { it1 -> shortToastShow(it1) }
                                    }
                                }
                            }
                    }
                    else{
                        shortToastShow("Please make sure the end date is after the start date!")
                    }
                }
                updatePlaceDialog.show()
            }
        }

        mainBinding.placeRV.adapter = placeRecyclerViewAdapter
        callGetPlaceList(placeRecyclerViewAdapter)
    }

    private fun callGetPlaceList(placeRecyclerViewAdapter : PlaceRVViewBindingAdapter){
        loadingDialog.show()
        CoroutineScope(Dispatchers.Main).launch {
            placeViewModel.getPlaceList().collect{
                when(it.status){
                    Status.LOADING -> {
                        loadingDialog.show()
                    }
                    Status.SUCCESS -> {
                        it.data?.collect{placeList ->
                            loadingDialog.dismiss()
                            placeRecyclerViewAdapter.addAllPlaces(placeList)
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

    val calendar = Calendar.getInstance()

    private var selectedStartDate : Date = Date()
    private fun showDatePickerStart(){
        val datePickerDialog = DatePickerDialog(this, {DatePicker, year : Int, monthOfYear : Int, dayOfMonth : Int ->
            val calendar = Calendar.getInstance()
            calendar.set(year, monthOfYear, dayOfMonth)

            selectedStartDate = calendar.time
            addStartDateBtn.setText(SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(selectedStartDate))

            isStartDateSelected = true

        },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )
        datePickerDialog.show()
    }

    private var selectedEndDate : Date = Date()
    private fun showDatePickerEnd(){
        val datePickerDialog = DatePickerDialog(this, {DatePicker, year : Int, monthOfYear : Int, dayOfMonth : Int ->
            val calendar = Calendar.getInstance()
            calendar.set(year, monthOfYear, dayOfMonth)

            selectedEndDate = calendar.time
            addEndDateBtn.setText(SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(selectedEndDate))

            isEndDateSelected = true

        },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )
        datePickerDialog.show()
    }


    private fun uriToByteArray(context: Context, uri: Uri): ByteArray? {
        try {
            val inputStream: InputStream? = context.contentResolver.openInputStream(uri)
            val byteArrayOutputStream = ByteArrayOutputStream()
            val buffer = ByteArray(1024)
            var bytesRead: Int
            while (inputStream?.read(buffer).also { bytesRead = it!! } != -1) {
                byteArrayOutputStream.write(buffer, 0, bytesRead)
            }
            return byteArrayOutputStream.toByteArray()
        } catch (e: IOException) {
            e.printStackTrace()
            return null
        }
    }

    fun byteArrayToUri(context: Context, byteArray: ByteArray?): Uri? {
        if (byteArray != null) {
            try {
                val file = File.createTempFile("temp_image", null, context.cacheDir)

                val fos = FileOutputStream(file)
                fos.write(byteArray)
                fos.close()

                return if (file.exists()) Uri.fromFile(file) else null
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
        return null
    }
}