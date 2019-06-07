package com.selfformat.deadmanswitch

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.edit
import androidx.core.text.isDigitsOnly
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
        setEmergencyContact()
        setFieldValidation()
        saveContactButton.setOnClickListener {
            if (fieldsHasErrorMessages()) {
                Toast.makeText(context, "Fix errors before saving", Toast.LENGTH_SHORT).show()
            } else {
                saveContact()
                (activity as MainActivity).onBackPressed()
            }
        }

        //TODO: add on/off to emergency sms feature with "are you sure" popup
        //TODO: Add timeout to summary in widget

    }

    private fun setFieldValidation() {
        timeout.run {
            addTextChangedListener(object : TextWatcher {
                override fun afterTextChanged(p0: Editable?) {
                }

                override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                }

                override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                    when {
                        timeout.text.isNullOrBlank() -> timeout.error = "Timeout cannot be empty"
                        !timeout.text.toString().isDigitsOnly() -> timeout.error = "Timeout must be positive number"
                    }
                }
            })
            setOnFocusChangeListener { _, hasFocus ->
                if (!hasFocus) {
                    if (timeout.text.isNullOrBlank() || !timeout.text.toString().isDigitsOnly()) timeout.setText("30", TextView.BufferType.EDITABLE)
                    else timeout.error = null
                }
            }
        }
        contactName.run {
            addTextChangedListener(object : TextWatcher {
                override fun afterTextChanged(p0: Editable?) {
                }

                override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                }

                override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                    if (contactName.text.isNullOrBlank()) contactName.error = "Contact name cannot be empty"
                }
            })
            setOnFocusChangeListener { _, hasFocus ->
                if (!hasFocus) {
                    if (contactName.text.isNullOrBlank()) contactName.setText("default name", TextView.BufferType.EDITABLE)
                    else contactName.error = null
                }
            }
        }
        contactNumber.run {
            addTextChangedListener(object : TextWatcher {
                override fun afterTextChanged(p0: Editable?) {
                }

                override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                }

                override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                    if (contactNumber.text.isNullOrBlank()) contactNumber.error = "Contact number cannot be empty"
                }
            })
            setOnFocusChangeListener { _, hasFocus ->
                if (!hasFocus) {
                    if (contactNumber.text.isNullOrBlank()) contactNumber.setText("default number", TextView.BufferType.EDITABLE)
                    else contactNumber.error = null
                }
            }
        }
        emergencyMessage.run {
            addTextChangedListener(object : TextWatcher {
                override fun afterTextChanged(p0: Editable?) {
                }

                override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                }

                override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                    if (emergencyMessage.text.isNullOrBlank()) emergencyMessage.error = "Message cannot be empty"
                }
            })
            setOnFocusChangeListener { _, hasFocus ->
                if (!hasFocus) {
                    if (emergencyMessage.text.isNullOrBlank()) emergencyMessage.setText("default message", TextView.BufferType.EDITABLE)
                    else emergencyMessage.error = null
                }
            }
        }
    }

    private fun setEmergencyContact(){
        timeout.setText(sharedPref!!.getString(TIMEOUT_UNTIL_EMERGENCY_MESSAGE_KEY, DEFAULT_EMERGENCY_TIME.toString()))
        contactName.setText(sharedPref!!.getString(CONTACT_NAME_KEY, getString(R.string.sample_contact_name)))
        contactNumber.setText(sharedPref!!.getString(CONTACT_NUMBER_KEY, getString(R.string.example_phone_number)))
        emergencyMessage.setText(sharedPref!!.getString(EMERGENCY_MESSAGE_KEY, getString(R.string.default_emergency_message)))
    }

    private fun saveContact() {
        Log.i("EmergencySmsFragment", "Contact saved")
        sharedPref?.edit {
            putString(TIMEOUT_UNTIL_EMERGENCY_MESSAGE_KEY, timeout.text.toString())
            putString(CONTACT_NAME_KEY, contactName.text.toString())
            putString(CONTACT_NUMBER_KEY, contactNumber.text.toString())
            putString(EMERGENCY_MESSAGE_KEY, emergencyMessage.text.toString())
        }
    }

    private fun fieldsHasErrorMessages(): Boolean {
        if (emergencyMessage.error != null ||
                contactName.error != null ||
                contactNumber.error != null ||
                timeout.error != null) {
            return true
        }
        return false
    }
}