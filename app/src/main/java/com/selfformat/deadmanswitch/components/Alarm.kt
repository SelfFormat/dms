package com.selfformat.deadmanswitch.components

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.content.edit
import com.selfformat.deadmanswitch.data.ALARM_STATUS_KEY
import org.jetbrains.anko.defaultSharedPreferences

internal class Alarm {

    companion object {
        fun prepareForAlarm(context: Context, time: Long?) {
            saveAlarmState(true, context)
            broadcast(context, time, AlarmState.ON, AlarmReceiver::class.java)
        }

        fun cancelPendingAlarm(context: Context) {
            saveAlarmState(false, context)
            broadcast(context, null, AlarmState.OFF, AlarmReceiver::class.java)
        }

        private fun broadcast(context: Context, time: Long?, alarmState: AlarmState, className: Class<*>) {
            val alarmIntent = Intent(context, className)
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            val pendingIntent = PendingIntent.getBroadcast(context, 0, alarmIntent, PendingIntent.FLAG_CANCEL_CURRENT)
            Log.i("Alarm manager is now: ", alarmState.name)
            when (alarmState) {
                AlarmState.ON -> {
                    alarmManager.setExact(AlarmManager.RTC_WAKEUP, time!!, pendingIntent)
                }
                AlarmState.OFF -> {
                    alarmManager.cancel(pendingIntent)
                }
            }
        }

        private fun saveAlarmState(alarmIsRunning: Boolean, context: Context) {
            val sharedPref = context.defaultSharedPreferences
            sharedPref.edit(true) {
                putBoolean(ALARM_STATUS_KEY, alarmIsRunning)
            }
        }
    }

    enum class AlarmState {
        ON, OFF
    }
}