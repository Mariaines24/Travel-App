package com.example.mytravelapp.utils

import android.app.Dialog
import android.content.Context
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.RatingBar
import android.widget.Toast
import com.google.android.material.textfield.TextInputLayout
import java.util.Calendar
import java.util.Date


enum class Status{
    SUCCESS,
    ERROR,
    LOADING
}

fun Context.shortToastShow(msg : String){
    Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
}

fun Dialog.setUpDialog(layoutResId: Int){
    setContentView(layoutResId)
    window!!.setLayout(
        LinearLayout.LayoutParams.MATCH_PARENT,
        LinearLayout.LayoutParams.WRAP_CONTENT
    )
    setCancelable(false)
}

fun validateEditText(editText: EditText, textTextInputLayout: TextInputLayout): Boolean {
    return when {
        editText.text.toString().trim().isEmpty() -> {
            textTextInputLayout.error = "Required"
            false
        }
        else -> {
            textTextInputLayout.error = null
            true
        }
    }
}

fun validateCoherenceDate(context: Context, startDate: Date, endDate: Date): Boolean {
    return if (isEndDateAfterStartDate(startDate, endDate)) {
        true
    } else {
        Toast.makeText(context, "Please make sure the end date is after the start date!", Toast.LENGTH_LONG).show()
        false
    }
}

private fun isEndDateAfterStartDate(date1: Date, date2: Date): Boolean {
    val cal1 = Calendar.getInstance().apply { time = date1 }
    val cal2 = Calendar.getInstance().apply { time = date2 }

    val year1 = cal1.get(Calendar.YEAR)
    val month1 = cal1.get(Calendar.MONTH)
    val day1 = cal1.get(Calendar.DAY_OF_MONTH)

    val year2 = cal2.get(Calendar.YEAR)
    val month2 = cal2.get(Calendar.MONTH)
    val day2 = cal2.get(Calendar.DAY_OF_MONTH)

    return year1 < year2 || (year1 == year2 && (month1 < month2 || (month1 == month2 && day1 <= day2)))
}


fun validateRatingBar(context: Context, ratingBar: RatingBar): Boolean {
    return if (ratingBar.rating > 0) {
        true
    } else {
        Toast.makeText(context, "Please provide a rating!", Toast.LENGTH_LONG).show()
        false
    }
}


fun clearEditText(editText: EditText, textTextInputLayout: TextInputLayout) {
    editText.text = null
    textTextInputLayout.error = null
}

