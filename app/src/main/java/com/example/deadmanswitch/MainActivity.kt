package com.example.deadmanswitch

import android.content.Context
import android.content.SharedPreferences
import android.media.RingtoneManager
import android.net.Uri
import android.os.Bundle
import android.telephony.SmsManager
import android.util.Log
import android.widget.Toast
import androidx.core.content.edit
import androidx.fragment.app.transaction
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : CustomStatusBarActivity() {
    private lateinit var sharedPref: SharedPreferences
    private var lightTheme = true

    override fun onResume() {
        super.onResume()
        val emergencySMS = sharedPref.getBoolean(EMERGENCY_SMS, false)
        if (emergencySMS) {
            Log.i("SENDING", "SMS")
            smsSendMessage()
            sharedPref.edit {
                putBoolean(EMERGENCY_SMS, false)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        sharedPref = getSharedPreferences(
            "myPrefs",
            Context.MODE_PRIVATE
        )

        lightTheme = sharedPref.getBoolean("lightTheme", true)

        setTheme(if (lightTheme) R.style.AppThemeLight else R.style.AppThemeDark)

        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)
        setUpStatusBarAppearance()
        toolbarTitle.text = resources.getString(R.string.app_name)


        toolbarTitle.setOnClickListener {
            sharedPref.edit {
                putBoolean("lightTheme", !lightTheme)
            }
            recreate()
        }

        supportFragmentManager.transaction(allowStateLoss = true) {
            replace(R.id.mainFrame, MainFragment.newInstance(), "MAIN")
        }
    }

    //region sending emergency sms

    fun getEmergencyContact(): EmergencyContact {
        return EmergencyContact(
            sharedPref.getString("contactNumber", null),
            sharedPref.getString("emergencyMessage", null),
            sharedPref.getString("contactName", null)
        )
    }

    fun smsSendMessage() {
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
        return sharedPref.getString("ringtoneName", RingtoneManager.getRingtone(this, RingtoneManager.getActualDefaultRingtoneUri(this, RingtoneManager.TYPE_RINGTONE)).getTitle(this))
    }

    fun isWidgetCardVisible() : Boolean {
        return sharedPref.getBoolean("isWidgetCardVisible", true)
    }

    fun getRingtoneUri() : Uri? {
        val uri = sharedPref.getString("ringtone", null) ?: return RingtoneManager.getActualDefaultRingtoneUri(this, RingtoneManager.TYPE_RINGTONE)
        return Uri.parse(uri)
    }

    //endregion

    companion object {
        public const val EMERGENCY_SMS = "EMERGENCY_SMS"
    }
}