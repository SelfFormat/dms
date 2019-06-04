package com.selfformat.deadmanswitch

import android.media.RingtoneManager
import android.net.Uri
import android.os.Bundle
import android.view.KeyEvent
import androidx.core.content.edit
import androidx.fragment.app.transaction
import com.selfformat.deadmanswitch.base.CustomActivity
import com.selfformat.deadmanswitch.data.*

class MainActivity : CustomActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        darkThemeSwitch()
        setContentView(R.layout.activity_main)
        supportFragmentManager.transaction(allowStateLoss = true) {
            replace(R.id.mainFrame, MainFragment.newInstance(), MAIN_FRAGMENT)
        }
    }

    //region theme change

    fun changeTheme() {
        sharedPref.edit(true) {
            putBoolean(LIGHT_THEME_KEY, !lightTheme)
        }
        recreate()
    }

    private fun darkThemeSwitch() {
        lightTheme = sharedPref.getBoolean(LIGHT_THEME_KEY, true)
        setTheme(if (lightTheme) R.style.AppThemeLight else R.style.AppThemeDark)
    }

    //endregion

    //region sharedPrefs

    fun getRingtoneName() : String? {
        return sharedPref.getString(RINGTONE_NAME_KEY, RingtoneManager.getRingtone(this, RingtoneManager.getActualDefaultRingtoneUri(this, RingtoneManager.TYPE_RINGTONE)).getTitle(this))
    }

    fun saveRingtoneTime(key: String, time: Int) {
        sharedPref.edit {
            putInt(key, time)
        }
    }

    fun isWidgetCardVisible() : Boolean {
        return sharedPref.getBoolean(IS_WIDGET_CARD_VISIBLE_KEY, true)
    }

    fun getRingtoneUri() : Uri? {
        val uri = sharedPref.getString(RINGTONE_KEY, null) ?: return RingtoneManager.getActualDefaultRingtoneUri(this, RingtoneManager.TYPE_RINGTONE)
        return Uri.parse(uri)
    }

    fun getEmergencyContact(): EmergencyContact {
        return EmergencyContact(
            sharedPref.getString(CONTACT_NUMBER_KEY, getString(R.string.example_phone_number)),
            sharedPref.getString(EMERGENCY_MESSAGE_KEY, getString(R.string.default_emergency_message)),
            sharedPref.getString(CONTACT_NAME_KEY, getString(R.string.sample_contact_name))
        )
    }

    //endregion


    //region updating slider after alarm volume change

    override fun dispatchKeyEvent(event: KeyEvent): Boolean {
        super.dispatchKeyEvent(event)
        val fragment = supportFragmentManager.findFragmentByTag(MAIN_FRAGMENT) as MainFragment
        return when (event.keyCode) {
            KeyEvent.KEYCODE_VOLUME_UP -> {
                if (event.action == KeyEvent.ACTION_DOWN) {
                    fragment.updateVolumeSlider()
                }
                true
            }
            KeyEvent.KEYCODE_VOLUME_DOWN -> {
                if (event.action == KeyEvent.ACTION_DOWN) {
                    fragment.updateVolumeSlider()
                }
                true
            }
            else -> super.dispatchKeyEvent(event)
        }
    }

    //endregion
}