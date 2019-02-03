package com.example.deadmanswitch

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.util.Log
import androidx.core.content.edit
import com.example.deadmanswitch.MainActivity.Companion.EMERGENCY_SMS

class EmergencySmsReceiver : BroadcastReceiver() {

    private lateinit var sharedPref: SharedPreferences

    override fun onReceive(context: Context, intent: Intent) {
        Log.i("EmergencySmsReceiver", "onReceive: Broadcast ")
        val myIntent = Intent(context, MainActivity::class.java)
        myIntent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
        val sharedPref = context.getSharedPreferences(
            "myPrefs",
            Context.MODE_PRIVATE
        )
        sharedPref.edit {
            putBoolean(EMERGENCY_SMS, true)
        }
        context.startActivity(myIntent)
    }
}