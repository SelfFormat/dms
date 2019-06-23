package com.selfformat.deadmanswitch.components

import android.app.IntentService
import android.content.Intent

class CancelAlarmService : IntentService("CancelAlarmService") {
    override fun onHandleIntent(p0: Intent?) {
        Alarm.cancelPendingAlarm(applicationContext)
    }
}