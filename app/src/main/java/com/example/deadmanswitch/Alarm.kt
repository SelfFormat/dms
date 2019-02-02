package com.example.deadmanswitch

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.util.Log

internal class Alarm {

    enum class State {
        ON, OFF
    }

    fun startAlarm(context: Context, time: Long?, state: Alarm.State) {
        val alarmIntent = Intent(context, AlarmReceiver::class.java)
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val pendingIntent = PendingIntent.getBroadcast(context, 0, alarmIntent, PendingIntent.FLAG_CANCEL_CURRENT)
        Log.i("Alarm manager is now: ", state.name)
        when (state) {
            State.ON -> {
                alarmManager.setExact(AlarmManager.RTC_WAKEUP, time!!, pendingIntent)
            }
            State.OFF -> {
                alarmManager.cancel(pendingIntent)
            }
        }
    }
}