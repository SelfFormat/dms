package com.selfformat.deadmanswitch.components

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.util.Log

internal class Alarm {

    companion object {
        fun prepareForAlarm(context: Context, time: Long?) {
            broadcast(context, time, State.ON, AlarmReceiver::class.java)
        }

        fun cancelPendingAlarm(context: Context) {
            broadcast(context, null, State.OFF, AlarmReceiver::class.java)
        }

        private fun broadcast(context: Context, time: Long?, state: State, className: Class<*>) {
            val alarmIntent = Intent(context, className)
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

    enum class State {
        ON, OFF
    }
}