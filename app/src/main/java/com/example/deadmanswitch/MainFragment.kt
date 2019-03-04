package com.example.deadmanswitch

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Color
import android.graphics.PorterDuff
import android.media.AudioManager
import android.media.RingtoneManager
import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.transaction
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.card_addwidget.*
import kotlinx.android.synthetic.main.card_emergency.*
import kotlinx.android.synthetic.main.card_tone_picker.*
import kotlinx.android.synthetic.main.fragment_main.*
import org.jetbrains.anko.startActivity
import java.util.*

class MainFragment : Fragment() {

    private var sharedPref: SharedPreferences? = null
    private val valueMin = 5
    private val valueMax = 10
    private var alarm = Alarm()
    private val randomTime = Random()
    private lateinit var audioManager: AudioManager
    private lateinit var ringTonePairs: MutableList<Pair<String, String>>
    private lateinit var tonesAdapter: ArrayAdapter<String>
    private var defaultRingtoneUri: Uri? = null
    private val CUSTOM_RINGOTONE_PICKER_REQUEST_CODE = 2
    private val SYSTEM_RINGTONE_PICKER_REQUEST_CODE = 5
    private var widgetCardVisibility = View.VISIBLE

    companion object {
        fun newInstance(): MainFragment {
            return MainFragment()
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_main, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        premiumBarMain.setOnClickListener { activity?.startActivity<BuyPremiumActivity>() }
        editEmergency.setOnClickListener { newFrag(EmergencySmsFragment.newInstance()) }
        chooseToneButton.setOnClickListener { openSystemRingtonePicker() }
        currentAlarmName.setOnClickListener { openSystemRingtonePicker() }
        customRingtone.run {
            visibility = View.VISIBLE
            setOnClickListener {
                openCustomSoundPicker()
            }
        }

        alarmVolumeSeekBar.max = getAlarmMaxVolume()
        alarmVolumeSeekBar.progress = getCurrentAlarmVolume()

        fab.setOnClickListener {
            val random = randomTime()
            val time = System.currentTimeMillis() + random
            val sum = "Alarm" + random / 1000 + " seconds"
            showSnackBar(sum)
            alarm.broadcast(activity!!.applicationContext, time, Alarm.State.ON)
            fab.setText(R.string.turn_off)
        }
        changeSeekBarColor(getString(R.string.seek_bar_color))
        currentAlarmName.text = (activity as MainActivity).getRingtoneName()

        cardWidget.visibility = widgetCardVisibility
        dismissWidgetHintButton.setOnClickListener {
            widgetCardVisibility = View.GONE
            cardWidget.visibility = widgetCardVisibility
        }
    }

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

    fun newFrag(fragment: Fragment) {
        activity!!.supportFragmentManager.transaction(allowStateLoss = true) {
            setCustomAnimations(android.R.anim.slide_in_left, android.R.anim.slide_out_right, android.R.anim.slide_in_left, android.R.anim.slide_out_right)
            replace(R.id.mainFrame, fragment)
            addToBackStack(fragment.toString())
        }
    }

    private fun randomTime(): Int {
        val initialTime = valueMin * 1000
        val generated = randomTime.nextInt(valueMax + 1 - valueMin) * 1000
        Log.i("init: $initialTime", ", gen: $generated")
        return generated + initialTime
    }

    private fun showSnackBar(text: String) {
        Snackbar.make(view!!.findViewById(com.example.deadmanswitch.R.id.scrollableMainLayout), text, Snackbar.LENGTH_LONG).show()
    }

    private fun getCurrentAlarmVolume() : Int {
        audioManager = context?.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        return  audioManager.getStreamVolume(AudioManager.STREAM_ALARM) - 1 // -1 because of return shift value
    }

    private fun getAlarmMaxVolume() : Int {
        audioManager = context?.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        return audioManager.getStreamMaxVolume(AudioManager.STREAM_ALARM) - 1 // -1 because of return shift value
    }

    override fun onResume() {
        super.onResume()
        getCurrentAlarmVolume()
        alarmVolumeSeekBar.progress = getCurrentAlarmVolume()
        val summary = "${(activity as MainActivity).getEmergencyContact().name ?: getString(R.string.sample_contact_name)} (${(activity as MainActivity).getEmergencyContact().number ?: getString(R.string.sample_contact_name)})"
        val textSummary = (activity as MainActivity).getEmergencyContact().message ?: getString(R.string.message)
        contactSummary.text = summary
        messageSummary.text = textSummary
        defaultRingtoneUri = (activity as MainActivity).getRingtoneUri()
        activity?.toolbarTitle?.text = resources.getString(R.string.app_name)

    }

    private fun openCustomSoundPicker() {
        val soundPickerIntent = Intent()
        soundPickerIntent.action = Intent.ACTION_GET_CONTENT
        soundPickerIntent.type = "audio/*"
        startActivityForResult(
            Intent.createChooser(soundPickerIntent, "Choose Sound File"), CUSTOM_RINGOTONE_PICKER_REQUEST_CODE
        )
    }

    private fun saveRingtone(ringtoneResourceID: String, name: String) {
        sharedPref = context?.getSharedPreferences(
            "myPrefs",
            Context.MODE_PRIVATE
        )
        val editor= sharedPref!!.edit()
        editor.putString("ringtoneName", name)
        editor.putString("ringtone", ringtoneResourceID)
        editor.commit()
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
                    updateCurrentAlarmName((activity as MainActivity).getRingtoneName()!!)
                }
            }
            CUSTOM_RINGOTONE_PICKER_REQUEST_CODE -> {
                uri = data?.data
                if (uri != null) {
                    val fileName = getFileNameFromIntent(uri)
                    saveRingtone(uri.toString(), fileName)
                    updateCurrentAlarmName(fileName)
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

    private fun updateCurrentAlarmName(name: String) {
        currentAlarmName.text = name
    }
}
