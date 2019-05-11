package com.example.deadmanswitch

import android.graphics.Color
import android.media.RingtoneManager
import android.net.Uri
import android.os.Bundle
import android.telephony.SmsManager
import android.util.Log
import android.widget.Toast
import androidx.core.content.edit
import androidx.fragment.app.transaction
import com.example.deadmanswitch.base.CustomActivity
import com.example.deadmanswitch.data.*
import kotlinx.android.synthetic.main.card_darkmode.*

class MainActivity : CustomActivity() {

    override fun onResume() {
        super.onResume()
        val shouldSendEmergencySMS= sharedPref.getBoolean(EMERGENCY_SMS_KEY, false)
        if (shouldSendEmergencySMS) {
            Log.i("SENDING", "SMS")
            sendSmsMessage()
            sharedPref.edit {
                putBoolean(EMERGENCY_SMS_KEY, false)
            }
        }

        when {
            lightTheme -> enableSwitchToDarkMode()
            else -> enableSwitchToLightMode()
        }

        visibilityModeSwitch.isChecked = !lightTheme
        visibilityModeSwitch.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                changeTheme()
            } else {
                changeTheme()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        darkModeSwitch()
        setContentView(R.layout.activity_main)
        supportFragmentManager.transaction(allowStateLoss = true) {
            replace(R.id.mainFrame, MainFragment.newInstance(), "MAIN")
        }
    }

    //region mode change

    private fun enableSwitchToDarkMode() {
        visibilityModeLabel.text = getString(R.string.night_mode)
        visibilityModeLabel.setTextColor(Color.WHITE)
        visibiltyModeLayout.background = getDrawable(R.drawable.gradient_dark)
    }

    private fun enableSwitchToLightMode() {
        visibilityModeLabel.text = getString(R.string.day_mode)
        visibilityModeLabel.setTextColor(Color.BLACK)
        visibiltyModeLayout.setBackgroundColor(Color.WHITE)
    }

    private fun changeTheme() {
        sharedPref.edit {
            putBoolean(LIGHT_THEME_KEY, !lightTheme)
        }
        recreate()
    }

    private fun darkModeSwitch() {
        lightTheme = sharedPref.getBoolean(LIGHT_THEME_KEY, true)
        setTheme(if (lightTheme) R.style.AppThemeLight else R.style.AppThemeDark)
    }

    //endregion

    //region sending emergency sms

    fun getEmergencyContact(): EmergencyContact {
        return EmergencyContact(
            sharedPref.getString(CONTACT_NUMBER_KEY, getString(R.string.example_phone_number)),
            sharedPref.getString(EMERGENCY_MESSAGE_KEY, getString(R.string.default_emergency_message)),
            sharedPref.getString(CONTACT_NAME_KEY, getString(R.string.sample_contact_name))
        )
    }

    fun sendSmsMessage() {
        val contact = getEmergencyContact()
        Toast.makeText(this, "Sending emergency sms to: ${contact.number}", Toast.LENGTH_SHORT).show()
        val serviceCenterAddress: String? = null
        val smsManager = SmsManager.getDefault()
        smsManager.sendTextMessage(
            contact.number, serviceCenterAddress, contact.message,
            null, null
        )
    }

    fun getRingtoneName() : String? {
        return sharedPref.getString(RINGTONE_NAME_KEY, RingtoneManager.getRingtone(this, RingtoneManager.getActualDefaultRingtoneUri(this, RingtoneManager.TYPE_RINGTONE)).getTitle(this))
    }

    fun getRingtoneMinTime() : Int {
        return sharedPref.getInt("ringtoneMinTime", MIN_DEFAULT_TIME)
    }

    fun saveRingtoneTime(key: String, time: Int) {
        sharedPref.edit {
            putInt(key, time)
        }
    }

    fun getRingtoneMaxTime() : Int {
        return sharedPref.getInt("ringtoneMaxTime", MAX_DEFAULT_TIME)
    }

    fun isWidgetCardVisible() : Boolean {
        return sharedPref.getBoolean("isWidgetCardVisible", true)
    }

    fun getRingtoneUri() : Uri? {
        val uri = sharedPref.getString(RINGTONE_KEY, null) ?: return RingtoneManager.getActualDefaultRingtoneUri(this, RingtoneManager.TYPE_RINGTONE)
        return Uri.parse(uri)
    }

    //endregion
}