package com.selfformat.deadmanswitch.components

import android.app.IntentService
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.telephony.SmsManager
import android.util.Log
import com.selfformat.deadmanswitch.R
import com.selfformat.deadmanswitch.data.*

class SmsService : IntentService("SmsService") {

    lateinit var sharedPref: SharedPreferences

    override fun onHandleIntent(p0: Intent?) {
        sharedPref = getSharedPreferences(
            PREFERENCES_KEY,
            Context.MODE_PRIVATE
        )
        //TODO: add check if AlarmingActivity is dead. If yes then don't send sms (it means that user kills activity)
        //TODO: add check if user has paid version
        //TODO: add check if user enabled sms feature
        //TODO: add check for SMS permission
        sendSmsMessage()
        goToMainScreen(this)
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
        Log.i("SENDING", "SMS")
        smsManager.sendTextMessage(
            contact.number, serviceCenterAddress, contact.message,
            null, null
        )
    }
}