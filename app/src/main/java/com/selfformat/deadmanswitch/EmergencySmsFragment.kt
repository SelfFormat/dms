package com.selfformat.deadmanswitch

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.edit
import com.selfformat.deadmanswitch.base.CustomFragment
import com.selfformat.deadmanswitch.data.*
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.fragment_emergency_sms.*

class EmergencySmsFragment : CustomFragment() {

    companion object {
        fun newInstance(): EmergencySmsFragment {
            return EmergencySmsFragment()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_emergency_sms, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        activity?.toolbarTitle?.text = resources.getString(R.string.set_emergency_contact)
        askUserForSmsPermission()
        setEmergencyContact()
        saveContactButton.setOnClickListener {
            saveContact()
            (activity as MainActivity).onBackPressed()
        }
    }

    private fun askUserForSmsPermission() {
        if (context?.let {
                ActivityCompat.checkSelfPermission(
                    it,
                    Manifest.permission.SEND_SMS
                )
            } != PackageManager.PERMISSION_GRANTED
        ) {
            Log.d("TAG", "Permission not granted")
            ActivityCompat.requestPermissions(
                context as Activity,
                arrayOf(Manifest.permission.SEND_SMS),
                PERMISSIONS_REQUEST_SEND_SMS
            )
        } else {
        }
    }

    private fun setEmergencyContact(){
        timeout.setText(sharedPref!!.getString(TIMEOUT_UNTIL_EMERGENCY_MESSAGE_KEY, DEFAULT_EMERGENCY_TIME.toString()))
        contactName.setText(sharedPref!!.getString(CONTACT_NAME_KEY, getString(R.string.sample_contact_name)))
        contactNumber.setText(sharedPref!!.getString(CONTACT_NUMBER_KEY, getString(R.string.example_phone_number)))
        emergencyMessage.setText(sharedPref!!.getString(EMERGENCY_MESSAGE_KEY, getString(R.string.default_emergency_message)))
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            PERMISSIONS_REQUEST_SEND_SMS -> {
                if (permissions[0] == Manifest.permission.SEND_SMS && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // Permission was granted. Enable sms button.
                } else {
                    // Permission denied.
                    Log.d("TAG", "No permission")
                    Toast.makeText(
                        context, "No permission",
                        Toast.LENGTH_LONG
                    ).show()
                    // Disable the sms button.
                }
            }
        }
    }

    //TODO: edittext fields validation

    private fun saveContact() {
        Log.i("EmergencySmsFragment", "Contact saved")
        sharedPref?.edit {
            putString(TIMEOUT_UNTIL_EMERGENCY_MESSAGE_KEY, timeout.text.toString())
            putString(CONTACT_NAME_KEY, contactName.text.toString())
            putString(CONTACT_NUMBER_KEY, contactNumber.text.toString())
            putString(EMERGENCY_MESSAGE_KEY, emergencyMessage.text.toString())
        }
    }
}