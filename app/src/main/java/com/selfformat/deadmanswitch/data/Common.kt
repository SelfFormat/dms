package com.selfformat.deadmanswitch.data

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.util.Log
import com.selfformat.deadmanswitch.MainActivity
import java.util.*

const val PREFERENCES_KEY = "prefs_key"
const val LIGHT_THEME_KEY = "light_theme"
const val CONTACT_NUMBER_KEY = "contact_number"
const val EMERGENCY_MESSAGE_KEY = "emergency_message"
const val CONTACT_NAME_KEY = "contact_name"
const val RINGTONE_KEY = "ringtone"
const val RINGTONE_NAME_KEY = "ringtone_name"
const val TIMEOUT_UNTIL_EMERGENCY_MESSAGE_KEY = "timeout"
const val ALARM_STATUS_KEY = "alarm_status_key"
const val TIME_TO_NEXT_ALARM_KEY = "time_to_next_alarm"
const val IS_WIDGET_CARD_VISIBLE_KEY = "is_widget_card_visible"

const val CHANNEL_ID = "com.selfformat.deadmanswitch"
const val MAIN_FRAGMENT = "MAIN_FRAGMENT"
const val EMPTY = "empty"

const val MIN_DEFAULT_TIME = 5
const val MAX_DEFAULT_TIME = 10
const val CUSTOM_RINGTONE_PICKER_REQUEST_CODE = 2
const val SYSTEM_RINGTONE_PICKER_REQUEST_CODE = 5
const val PERMISSIONS_REQUEST_SEND_SMS = 1
const val DEFAULT_EMERGENCY_TIME = 8

fun goToMainScreen(context: Context) {
    val intent = Intent(context, MainActivity::class.java)
    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
    context.startActivity(intent)
}

fun getRingtoneMinTime(sharedPreferences: SharedPreferences?) : Int {
    return sharedPreferences?.getInt("ringtoneMinTime", MIN_DEFAULT_TIME) ?: MIN_DEFAULT_TIME
}

fun getRingtoneMaxTime(sharedPreferences: SharedPreferences?) : Int {
    return sharedPreferences?.getInt("ringtoneMaxTime", MAX_DEFAULT_TIME) ?: MAX_DEFAULT_TIME
}

fun randomTime(minTime: Int, maxTime: Int): Int {
    val initialTime = minTime * 1000
    val generated = Random().nextInt(maxTime + 1 - minTime) * 1000
    Log.i("init: $initialTime", ", gen: $generated")
    return generated + initialTime
}