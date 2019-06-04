package com.selfformat.deadmanswitch.base

import android.content.Context
import android.content.SharedPreferences
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.edit
import com.selfformat.deadmanswitch.data.ALARM_STATUS_KEY
import com.selfformat.deadmanswitch.data.LIGHT_THEME_KEY
import com.selfformat.deadmanswitch.data.PREFERENCES_KEY

open class CustomActivity : AppCompatActivity() {

    lateinit var sharedPref: SharedPreferences
    var lightTheme = true
    var alarmOn = false

    override fun onResume() {
        super.onResume()
        lightTheme = sharedPref.getBoolean(LIGHT_THEME_KEY, true)
        alarmOn = sharedPref.getBoolean(ALARM_STATUS_KEY, false)
        setUpStatusBarAppearance()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sharedPref = getSharedPreferences(
            PREFERENCES_KEY,
            Context.MODE_PRIVATE
        )
        lightTheme = sharedPref.getBoolean(LIGHT_THEME_KEY, true)
    }

    fun saveAlarmState(alarmIsRunning: Boolean) {
        sharedPref.edit(true) {
            putBoolean(ALARM_STATUS_KEY, alarmIsRunning)
        }
    }

    private fun setUpStatusBarAppearance() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && lightTheme) {
            window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
            window.statusBarColor = Color.WHITE
        }
    }
}