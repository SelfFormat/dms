package com.example.deadmanswitch.base

import android.content.Context
import android.content.SharedPreferences
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.example.deadmanswitch.data.LIGHT_THEME_KEY
import com.example.deadmanswitch.data.PREFERENCES_KEY

open class CustomActivity : AppCompatActivity() {

    lateinit var sharedPref: SharedPreferences
    var lightTheme = true

    override fun onResume() {
        super.onResume()
        lightTheme = sharedPref.getBoolean(LIGHT_THEME_KEY, true)
        setUpStatusBarAppearance()
    }

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sharedPref = getSharedPreferences(
            PREFERENCES_KEY,
            Context.MODE_PRIVATE
        )
    }

    private fun setUpStatusBarAppearance() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && lightTheme) {
            window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
            window.statusBarColor = Color.WHITE
        }
    }
}