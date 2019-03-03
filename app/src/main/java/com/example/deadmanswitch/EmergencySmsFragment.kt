package com.example.deadmanswitch

import android.Manifest
import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.edit
import androidx.fragment.app.Fragment
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.fragment_emergency_sms.*
import androidx.core.app.ActivityCompat
import android.app.Activity
import android.content.pm.PackageManager

class EmergencySmsFragment : Fragment() {

    private var sharedPref: SharedPreferences? = null
    private val MY_PERMISSIONS_REQUEST_SEND_SMS = 1

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
        sharedPref = view.context.getSharedPreferences(
            "myPrefs",
            Context.MODE_PRIVATE
        )
        activity?.toolbarTitle?.text = resources.getString(R.string.set_emergency_contact)
        setEmergencyContatct()
        saveContactButton.setOnClickListener {
            saveContact()
            checkForSmsPermission()
            (activity as MainActivity).onBackPressed()
        }
    }

    private fun checkForSmsPermission() {
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
                MY_PERMISSIONS_REQUEST_SEND_SMS
            )
        } else {

        }
    }

    fun setEmergencyContatct(){
        contactName.setText(sharedPref?.getString("contactName", null))
        contactNumber.setText(sharedPref?.getString("contactNumber", null))
        emergencyMessage.setText(sharedPref?.getString("emergencyMessage", null))
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            MY_PERMISSIONS_REQUEST_SEND_SMS -> {
                if (permissions[0] == Manifest.permission.SEND_SMS && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // Permission was granted. Enable sms button.

                    (activity as MainActivity).smsSendMessage()

                } else {
                    // Permission denied.
                    Log.d("TAG", "No permission")
                    Toast.makeText(
                        context, "No perission",
                        Toast.LENGTH_LONG
                    ).show()
                    // Disable the sms button.
                }
            }
        }
    }

    //TODO: edittext fields validation

    private fun saveContact() {
        Toast.makeText(context, contactName.text.toString(), Toast.LENGTH_SHORT).show()
        sharedPref?.edit {
            putString("contactName", contactName.text.toString())
            putString("contactNumber", contactNumber.text.toString())
            putString("emergencyMessage", emergencyMessage.text.toString())
        }
    }
}