package com.example.deadmanswitch.base

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import androidx.fragment.app.Fragment
import com.example.deadmanswitch.data.LIGHT_THEME_KEY
import com.example.deadmanswitch.data.PREFERENCES_KEY

open class CustomFragment : Fragment() {

    var sharedPref: SharedPreferences? = null
    var lightTheme = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sharedPref = context?.getSharedPreferences(
            PREFERENCES_KEY,
            Context.MODE_PRIVATE
        )
    }

    override fun onResume() {
        super.onResume()
        lightTheme = sharedPref?.getBoolean(LIGHT_THEME_KEY, true) ?: true
    }
}