package com.selfformat.deadmanswitch.base

import android.content.SharedPreferences
import android.os.Bundle
import androidx.core.content.edit
import androidx.fragment.app.Fragment
import com.selfformat.deadmanswitch.data.ALARM_STATUS_KEY
import com.selfformat.deadmanswitch.data.LIGHT_THEME_KEY
import com.selfformat.deadmanswitch.data.PREMIUM_FEATURES_KEY
import org.jetbrains.anko.defaultSharedPreferences
import kotlin.properties.Delegates

open class CustomFragment : Fragment() {

    var sharedPref: SharedPreferences? = null
    var lightTheme = true
    var alarmOn : Boolean by Delegates.observable(false) { _, _, _ ->
        updateFAB()
    }
    var premium = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sharedPref = context?.defaultSharedPreferences
        updateImportantBooleansFromPrefs()
    }

    override fun onResume() {
        super.onResume()
        updateImportantBooleansFromPrefs()
    }

    private fun updateImportantBooleansFromPrefs() {
        lightTheme = sharedPref?.getBoolean(LIGHT_THEME_KEY, true) ?: true
        alarmOn = sharedPref?.getBoolean(ALARM_STATUS_KEY, false) ?: false
        premium = sharedPref?.getBoolean(PREMIUM_FEATURES_KEY, false) ?: false
    }

    fun saveAlarmState(isAlarmRunning: Boolean) {
        alarmOn = isAlarmRunning
        sharedPref!!.edit(true) {
            putBoolean(ALARM_STATUS_KEY, isAlarmRunning)
        }
    }

    open fun updateFAB() {

    }
}