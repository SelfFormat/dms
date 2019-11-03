package com.selfformat.deadmanswitch.base

import android.content.SharedPreferences
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.edit
import com.selfformat.deadmanswitch.data.ALARM_STATUS_KEY
import com.selfformat.deadmanswitch.data.EMERGENCY_ENABLED_KEY
import com.selfformat.deadmanswitch.data.LIGHT_THEME_KEY
import org.jetbrains.anko.defaultSharedPreferences

open class CustomActivity : AppCompatActivity() {

    lateinit var sharedPref: SharedPreferences
    var lightTheme = true
    var alarmOn = false
    var premium = false
    var emergencyEnabled = false

    override fun onResume() {
        super.onResume()
        updateImportantBooleansFromPrefs()
        setUpStatusBarAppearance()
        sharedPref.registerOnSharedPreferenceChangeListener { sharedPreferencess, key ->
            if (key == ALARM_STATUS_KEY) {
                Log.i("PL", "ININ: ")
                if (sharedPreferencess.getBoolean(ALARM_STATUS_KEY, false)) {
                    Log.i("PL", ":true key ")
                } else {
                    Log.i("PL", "false key: ")
                }
            }
        }
    }

    override fun onPause() {
        super.onPause()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sharedPref = defaultSharedPreferences
        updateImportantBooleansFromPrefs()
    }

    private fun updateImportantBooleansFromPrefs() {
        lightTheme = sharedPref.getBoolean(LIGHT_THEME_KEY, true)
        alarmOn = sharedPref.getBoolean(ALARM_STATUS_KEY, false)
        premium = true // PREMIUM FOR TESTING ONLY
        emergencyEnabled = sharedPref.getBoolean(EMERGENCY_ENABLED_KEY, false)
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