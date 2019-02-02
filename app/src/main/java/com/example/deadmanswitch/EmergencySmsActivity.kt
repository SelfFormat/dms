package com.example.deadmanswitch

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import androidx.core.content.edit
import androidx.fragment.app.transaction
import kotlinx.android.synthetic.main.activity_emergency_sms.*
import kotlinx.android.synthetic.main.fragment_emergency_sms.*

class EmergencySmsActivity : CustomStatusBarActivity() {

    private lateinit var sharedPref : SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_emergency_sms)
        setUpStatusBarAppearance()
        emergencyToolbarTitle.text = resources.getString(R.string.set_emergency_contact)
        sharedPref = getPreferences(Context.MODE_PRIVATE)
        toolbarEmergency.setNavigationOnClickListener { onBackPressed() }
        supportFragmentManager.transaction(allowStateLoss = true) {
            replace(R.id.emergencyFrame, EmergencySmsFragment.newInstance(), "MAIN")
        }
        saveContactButton.setOnClickListener {
            saveContact()
        }
    }

    //TODO: edittext fields validation

    private fun saveContact() {
        sharedPref.edit {
            putString("contactName", contactName.editText?.text.toString())
            putString("contactNumber", contactNumber.editText?.text.toString())
            putString("emergencyMessage", emergencyMessage.editText?.text.toString())
        }
    }

}