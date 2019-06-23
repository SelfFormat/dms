package com.selfformat.deadmanswitch.components

import android.app.ActivityManager
import android.app.IntentService
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.telephony.SmsManager
import android.util.Log
import com.selfformat.deadmanswitch.R
import com.selfformat.deadmanswitch.data.*
import org.jetbrains.anko.defaultSharedPreferences

class SmsService : IntentService("SmsService") {

    private lateinit var sharedPref: SharedPreferences
    var premium = false
    var emergencyEnabled = false

    companion object {
        private const val TAG = "SmsService"
    }

    override fun onHandleIntent(p0: Intent?) {
        sharedPref = defaultSharedPreferences
        premium = sharedPref.getBoolean(PREMIUM_FEATURES_KEY, false)
        emergencyEnabled = sharedPref.getBoolean(EMERGENCY_ENABLED_KEY, false)
        if (premium && emergencyEnabled && isSmsPermissionGranted(this)
            && isAppStillRunning(this)) {
            sendSmsMessage()
        }
    }

    private fun getEmergencyContact(): EmergencyContact {
        return EmergencyContact(
            sharedPref.getString(CONTACT_NUMBER_KEY, getString(R.string.example_phone_number)),
            sharedPref.getString(EMERGENCY_MESSAGE_KEY, getString(R.string.default_emergency_message)),
            sharedPref.getString(CONTACT_NAME_KEY, getString(R.string.sample_contact_name))
        )
    }

    private fun sendSmsMessage() {
        val contact = getEmergencyContact()
        val serviceCenterAddress: String? = null
        val smsManager = SmsManager.getDefault()
        Log.i(TAG, "SENDING SMS")
        smsManager.sendTextMessage(
            contact.number, serviceCenterAddress, contact.message,
            null, null
        )
    }

    private fun isAppStillRunning(context: Context): Boolean {
        val am = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val runningProcesses = am.runningAppProcesses
        for (processInfo in runningProcesses) {
            if (processInfo.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND) {
                for (activeProcess in processInfo.pkgList) {
                    if (activeProcess == context.packageName) {
                        return true
                    }
                }
            }
        }
        return false
    }
}