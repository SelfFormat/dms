package com.example.deadmanswitch

import android.content.BroadcastReceiver
import android.content.ContentValues.TAG
import android.content.Context
import android.content.Intent
import android.util.Log

class AlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        Log.i(TAG, "onReceive: Broadcast ")
        val myIntent = Intent(context, Alarming::class.java)
        myIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        context.startActivity(myIntent)
    }
}