package com.example.deadmanswitch

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.PorterDuff
import android.media.AudioManager
import android.media.RingtoneManager
import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.edit
import androidx.fragment.app.Fragment
import androidx.fragment.app.transaction
import com.example.deadmanswitch.base.CustomFragment
import com.example.deadmanswitch.components.Alarm
import com.example.deadmanswitch.data.*
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.card_addwidget.*
import kotlinx.android.synthetic.main.card_emergency.*
import kotlinx.android.synthetic.main.card_time_picker.*
import kotlinx.android.synthetic.main.card_tone_picker.*
import kotlinx.android.synthetic.main.card_turn_on_dark_mode.*
import kotlinx.android.synthetic.main.fragment_main.*
import org.jetbrains.anko.startActivity

class MainFragment : CustomFragment() {

    private var minTime = MIN_DEFAULT_TIME
    private var maxTime = MAX_DEFAULT_TIME
    private lateinit var audioManager: AudioManager
    private var defaultRingtoneUri: Uri? = null
    private var widgetCardVisibility = View.VISIBLE

    companion object {
        fun newInstance(): MainFragment {
            return MainFragment()
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return if (lightTheme) {
            inflater.inflate(R.layout.fragment_main, container, false)
        } else {
            inflater.inflate(R.layout.fragment_main_dark, container, false)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        visibilityModeSwitch.isChecked = !lightTheme
        visibilityModeSwitch.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                (activity as MainActivity).changeTheme()
            } else {
                (activity as MainActivity).changeTheme()
            }
        }

        if (lightTheme) premiumBarMain.setOnClickListener { activity?.startActivity<BuyPremiumActivity>() }
        editEmergency.setOnClickListener { newFrag(EmergencySmsFragment.newInstance()) }
        chooseToneButton.setOnClickListener { openSystemRingtonePicker() }
        currentAlarmName.setOnClickListener { openSystemRingtonePicker() }
        customRingtone.run {
            visibility = View.VISIBLE
            setOnClickListener {
                openCustomSoundPicker()
            }
        }

        val emergencyContact = (activity as MainActivity).getEmergencyContact()
        val contactSummaryText = "${emergencyContact.name} (${emergencyContact.number})"
        contactSummary.text = contactSummaryText
        messageSummary.text = emergencyContact.message

        setTimeRange()

        alarmVolumeSeekBar.max = getAlarmMaxVolume()
        alarmVolumeSeekBar.progress = getCurrentAlarmVolume()

        changeSeekBarColor(getString(R.string.seek_bar_color))
        currentAlarmName.text = (activity as MainActivity).getRingtoneName()


        fab.run {
            setOnClickListener {
                if (alarmOn) {
                    Alarm.cancelAlarm(activity!!.applicationContext)
                    saveAlarmState(false)
                    fab.text = getString(R.string.run_switch)
                    Snackbar.make(view!!.findViewById(R.id.coordinatorMainFragment), getString(R.string.alarm_canceled_snackbar_message), Snackbar.LENGTH_SHORT).show()
                } else {
                    saveAlarmState(true)
                    val random = randomTime(minTime, maxTime)
                    val time = System.currentTimeMillis() + random
                    Snackbar.make(view!!.findViewById(R.id.coordinatorMainFragment), snackBarMessage(random), Snackbar.LENGTH_SHORT).show()
                    Alarm.prepareForAlarm(activity!!.applicationContext, time, Alarm.State.ON)
                    fab.text = getString(R.string.turn_off)
                }
            }
        }

        widgetCardVisibility = when {
            (activity as MainActivity).isWidgetCardVisible() -> View.VISIBLE
            else -> View.GONE
        }
        cardWidget.visibility = widgetCardVisibility

        dismissWidgetHintButton.setOnClickListener {
            hideWidgetCardForever()
        }
    }

    private fun newFrag(fragment: Fragment) {
        activity!!.supportFragmentManager.transaction(allowStateLoss = true) {
            setCustomAnimations(android.R.anim.slide_in_left, android.R.anim.slide_out_right, android.R.anim.slide_in_left, android.R.anim.slide_out_right)
            replace(R.id.mainFrame, fragment)
            addToBackStack(fragment.toString())
        }
    }

    override fun onResume() {
        super.onResume()
        sharedPref?.let {
            if(it.getString(TIME_TO_NEXT_ALARM_KEY, EMPTY) != "empty") {
                val timeToNextAlarm = it.getString(TIME_TO_NEXT_ALARM_KEY, "empty").toInt()
                Snackbar.make(view!!.findViewById(R.id.coordinatorMainFragment), snackBarMessage(timeToNextAlarm), Snackbar.LENGTH_SHORT).show()
                it.edit(true) {
                    putString(TIME_TO_NEXT_ALARM_KEY, "empty")
                }
            }
        }
        getCurrentAlarmVolume()
        fab.text = if (alarmOn) getString(R.string.turn_off) else getString(R.string.run_switch)
        alarmVolumeSeekBar.progress = getCurrentAlarmVolume()
        val summary = "${(activity as MainActivity).getEmergencyContact().name ?: getString(R.string.sample_contact_name)} (${(activity as MainActivity).getEmergencyContact().number ?: getString(R.string.sample_contact_name)})"
        val textSummary = (activity as MainActivity).getEmergencyContact().message ?: getString(R.string.message)
        contactSummary.text = summary
        messageSummary.text = textSummary
        defaultRingtoneUri = (activity as MainActivity).getRingtoneUri()

        activity?.toolbarTitle?.setBackgroundColor(if (lightTheme) Color.WHITE else Color.BLACK)
        activity?.mainToolbar?.setBackgroundColor(if (lightTheme) Color.WHITE else Color.BLACK)
        activity?.toolbarTitle?.setTextColor(if (lightTheme) Color.BLACK else Color.WHITE)
        activity?.toolbarTitle?.text = resources.getString(R.string.app_name)
    }

    private fun snackBarMessage(timeToNextAlarm: Int) = "New alarm in " + timeToNextAlarm / 1000 + " seconds"

    private fun hideWidgetCardForever() {
        //TODO: add popup "are you sure?"
        widgetCardVisibility = View.GONE
        cardWidget.visibility = widgetCardVisibility
        sharedPref?.edit(true) {
            putBoolean("isWidgetCardVisible", false)
        }
    }

    private fun setTimeRange() {
        minTime = (activity as MainActivity).getRingtoneMinTime()
        maxTime = (activity as MainActivity).getRingtoneMaxTime()

        minTimeText.run {
            setText(minTime.toString())
            setOnFocusChangeListener { _, hasFocus ->
                if (!hasFocus) {
                    minTime = minTimeText.text.toString().toInt()
                    (activity as MainActivity).saveRingtoneTime("ringtoneMinTime", minTime)
                }
            }
        }

        maxTimeText.run {
            setText(maxTime.toString())
            setOnFocusChangeListener { _, hasFocus ->
                if (!hasFocus) {
                    //TODO: crashes if leaved empty
                    maxTime = maxTimeText.text.toString().toInt()
                    (activity as MainActivity).saveRingtoneTime("ringtoneMaxTime", maxTime)
                }
            }
        }
    }

    //region soundPicker

    private fun openSystemRingtonePicker() {
        val intentUpload = Intent(RingtoneManager.ACTION_RINGTONE_PICKER)
        intentUpload.putExtra(RingtoneManager.EXTRA_RINGTONE_TITLE, "SYSTEM SOUNDS LIST")
        intentUpload.putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_DEFAULT, true)
        intentUpload.putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_SILENT, false)
        intentUpload.putExtra(RingtoneManager.EXTRA_RINGTONE_EXISTING_URI, defaultRingtoneUri)
        intentUpload.putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE, RingtoneManager.TYPE_ALL)
        startActivityForResult(intentUpload, SYSTEM_RINGTONE_PICKER_REQUEST_CODE)
    }

    private fun changeSeekBarColor(hexColor: String) {
        alarmVolumeSeekBar.progressDrawable.setColorFilter(Color.parseColor(hexColor), PorterDuff.Mode.SRC_IN)
        alarmVolumeSeekBar.thumb.setColorFilter(Color.parseColor(hexColor), PorterDuff.Mode.SRC_IN)
    }

    private fun getCurrentAlarmVolume() : Int {
        audioManager = context?.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        return  audioManager.getStreamVolume(AudioManager.STREAM_ALARM) - 1 // -1 because of return shift value
    }

    private fun getAlarmMaxVolume() : Int {
        audioManager = context?.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        return audioManager.getStreamMaxVolume(AudioManager.STREAM_ALARM) - 1 // -1 because of return shift value
    }

    private fun openCustomSoundPicker() {
        val soundPickerIntent = Intent()
        soundPickerIntent.action = Intent.ACTION_GET_CONTENT
        soundPickerIntent.type = "audio/*"
        startActivityForResult(
            Intent.createChooser(soundPickerIntent, "Choose Sound File"), CUSTOM_RINGTONE_PICKER_REQUEST_CODE
        )
    }

    private fun saveRingtone(ringtoneResourceID: String, name: String) {
        sharedPref?.edit(true) {
            putString(RINGTONE_NAME_KEY, name)
            putString(RINGTONE_KEY, ringtoneResourceID)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode != Activity.RESULT_OK) return
        var uri: Uri? = null
        when (requestCode) {
            SYSTEM_RINGTONE_PICKER_REQUEST_CODE -> {
                uri = data?.getParcelableExtra(RingtoneManager.EXTRA_RINGTONE_PICKED_URI)
                if (uri != null) {
                    saveRingtone(uri.toString(),  RingtoneManager.getRingtone(context, uri).getTitle(context))
                    currentAlarmName.text = (activity as MainActivity).getRingtoneName()!!
                }
            }
            CUSTOM_RINGTONE_PICKER_REQUEST_CODE -> {
                uri = data?.data
                if (uri != null) {
                    val fileName = getFileNameFromIntent(uri)
                    saveRingtone(uri.toString(), fileName)
                    currentAlarmName.text = fileName
                }
            }
        }
        Toast.makeText(context, "$uri ${(activity as MainActivity).getRingtoneName()}", Toast.LENGTH_SHORT).show()
    }

    private fun getFileNameFromIntent(uri:Uri):String {
        val returnCursor = context?.contentResolver?.query(uri, null, null, null, null)!!
        val nameIndex = returnCursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
        returnCursor.moveToFirst()
        val name = returnCursor.getString(nameIndex)
        returnCursor.close()
        return name
    }

    //endregion
}
