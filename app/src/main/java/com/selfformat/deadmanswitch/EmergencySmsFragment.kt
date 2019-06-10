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
        //TODO: Add timeoutInput to summary in widget

    }

    private fun setFieldValidation() {
        timeoutInput.run {
            addTextChangedListener(object : TextWatcher {
                override fun afterTextChanged(p0: Editable?) {
                }

                override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                }

                override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                    when {
                        timeoutInput.text.isNullOrBlank() -> timeoutInput.error = "Timeout cannot be empty"
                        !timeoutInput.text.toString().isDigitsOnly() -> timeoutInput.error = "Timeout must be positive number"
                    }
                }
            })
            setOnFocusChangeListener { _, hasFocus ->
                if (!hasFocus) {
                    if (timeoutInput.text.isNullOrBlank() || !timeoutInput.text.toString().isDigitsOnly()) timeoutInput.setText("30", TextView.BufferType.EDITABLE)
                    else timeoutInput.error = null
                }
            }
        }
        contactNameInput.run {
            addTextChangedListener(object : TextWatcher {
                override fun afterTextChanged(p0: Editable?) {
                }

                override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                }

                override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                    if (contactNameInput.text.isNullOrBlank()) contactNameInput.error = "Contact name cannot be empty"
                }
            })
            setOnFocusChangeListener { _, hasFocus ->
                if (!hasFocus) {
                    if (contactNameInput.text.isNullOrBlank()) contactNameInput.setText("default name", TextView.BufferType.EDITABLE)
                    else contactNameInput.error = null
                }
            }
        }
        contactNumberInput.run {
            addTextChangedListener(object : TextWatcher {
                override fun afterTextChanged(p0: Editable?) {
                }

                override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                }

                override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                    when {
                        contactNumberInput.text.isNullOrBlank() -> contactNumberInput.error = "Contact number cannot be empty"
                        !(contactNumberInput.text.toString().trim()).isDigitsOnly() -> contactNumberInput.error = "Number has to be digits only"
                    }
                }
            })
            setOnFocusChangeListener { _, hasFocus ->
                if (!hasFocus) {
                    when {
                        contactNumberInput.text.isNullOrBlank() -> contactNumberInput.setText("default number", TextView.BufferType.EDITABLE)
                        !(contactNumberInput.text.toString().trim()).isDigitsOnly() -> contactNumberInput.setText("123 456 789", TextView.BufferType.EDITABLE)
                        else -> contactNumberInput.error = null
                    }
                }
            }
        }
        emergencyMessageInput.run {
            addTextChangedListener(object : TextWatcher {
                override fun afterTextChanged(p0: Editable?) {
                }

                override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                }

                override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                    if (emergencyMessageInput.text.isNullOrBlank()) emergencyMessageInput.error = "Message cannot be empty"
                }
            })
            setOnFocusChangeListener { _, hasFocus ->
                if (!hasFocus) {
                    if (emergencyMessageInput.text.isNullOrBlank()) emergencyMessageInput.setText("default message", TextView.BufferType.EDITABLE)
                    else emergencyMessageInput.error = null
                }
            }
        }
    }

    private fun setEmergencyContact(){
        timeoutInput.setText(sharedPref!!.getString(TIMEOUT_UNTIL_EMERGENCY_MESSAGE_KEY, DEFAULT_EMERGENCY_TIME.toString()))
        contactNameInput.setText(sharedPref!!.getString(CONTACT_NAME_KEY, getString(R.string.sample_contact_name)))
        contactNumberInput.setText(sharedPref!!.getString(CONTACT_NUMBER_KEY, getString(R.string.example_phone_number)))
        emergencyMessageInput.setText(sharedPref!!.getString(EMERGENCY_MESSAGE_KEY, getString(R.string.default_emergency_message)))
    }

    private fun saveContact() {
        Log.i("EmergencySmsFragment", "Contact saved")
        sharedPref?.edit {
            putString(TIMEOUT_UNTIL_EMERGENCY_MESSAGE_KEY, timeoutInput.text.toString())
            putString(CONTACT_NAME_KEY, contactNameInput.text.toString())
            putString(CONTACT_NUMBER_KEY, contactNumberInput.text.toString().trim()) //TODO: add number validation regexp (convert it to number only)
            putString(EMERGENCY_MESSAGE_KEY, emergencyMessageInput.text.toString())
        }
    }

    private fun fieldsHasErrorMessages(): Boolean {
        if (emergencyMessageInput.error != null ||
                contactNameInput.error != null ||
                contactNumberInput.error != null ||
                timeoutInput.error != null) {
            return true
        }
        return false
    }
}