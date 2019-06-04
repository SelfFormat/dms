package com.selfformat.deadmanswitch.components

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.selfformat.deadmanswitch.AlarmingActivity

class AlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val myIntent = Intent(context, AlarmingActivity::class.java)
        myIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        context.startActivity(myIntent)
    }
}