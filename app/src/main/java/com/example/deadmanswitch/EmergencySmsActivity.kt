package com.example.deadmanswitch

import android.os.Bundle
import androidx.fragment.app.transaction
import kotlinx.android.synthetic.main.activity_emergency_sms.*

class EmergencySmsActivity : CustomStatusBarActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_emergency_sms)
        setUpStatusBarAppearance()
        emergencyToolbarTitle.text = resources.getString(R.string.set_emergency_contact)
        toolbarEmergency.setNavigationOnClickListener { onBackPressed() }
        supportFragmentManager.transaction(allowStateLoss = true) {
            replace(R.id.emergencyFrame, EmergencySmsFragment.newInstance(), "MAIN")
        }
    }
}