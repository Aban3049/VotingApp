package com.example.votingapp

import android.content.Context
import android.text.format.DateFormat
import android.widget.Toast
import java.util.Calendar
import java.util.Locale

object Utills {

    fun formatTimestamp(timestamp: Long): String {
        val calendar = Calendar.getInstance(Locale.ENGLISH)
        calendar.timeInMillis = timestamp

        return DateFormat.format("dd/MM/yyyy", calendar).toString()

    }

    fun toast(context:Context,message:String){
        return Toast.makeText(context,message,Toast.LENGTH_LONG).show()
    }
}