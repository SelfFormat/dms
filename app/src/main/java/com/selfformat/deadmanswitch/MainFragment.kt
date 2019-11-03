package com.selfformat.deadmanswitch

import android.Manifest
import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.graphics.Color
import android.media.AudioManager
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.OpenableColumns
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.content.edit
import androidx.core.text.isDigitsOnly
import androidx.fragment.app.Fragment
import androidx.fragment.app.transaction
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.MobileAds
import com.google.android.material.snackbar.Snackbar
import com.selfformat.deadmanswitch.base.CustomFragment
import com.selfformat.deadmanswitch.components.Alarm
import com.selfformat.deadmanswitch.components.NotificationCancelAlarm
import com.selfformat.deadmanswitch.data.*
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.ad_card.*
import kotlinx.android.synthetic.main.card_addwidget.*
import kotlinx.android.synthetic.main.card_time_picker.*
import kotlinx.android.synthetic.main.card_tone_picker.*
import kotlinx.android.synthetic.main.card_turn_on_light_mode.*
import kotlinx.android.synthetic.main.fragment_main.*
import org.jetbrains.anko.startActivity

class MainFragment : CustomFragment() {

    private var minTime = MIN_DEFAULT_TIME
    private var maxTime = MAX_DEFAULT_TIME
    private lateinit var audioManager: AudioManager
    private var defaultRingtoneUri: Uri? = null
    private var widgetCardVisibility = View.VISIBLE
    private lateinit var notification: NotificationCancelAlarm

    companion object {
        fun newInstance(): MainFragment {
            return MainFragment()
        }
        private const val TAG = "MainFragment"
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
//        val listener: SharedPreferences.OnSharedPreferenceChangeListener =
            SharedPreferences.OnSharedPreferenceChangeListener { prefs, key ->
                Log.i(TAG, "IN: ")
                if (key == ALARM_STATUS_KEY) {
                    Log.i(TAG, "ININ: ")
                    if (prefs.getBoolean(ALARM_STATUS_KEY, false)) {
                        Log.i(TAG, ":true key ")
                        fab?.text = "ON"
                    } else {
                        Log.i(TAG, "false key: ")
                        fab?.text = "OFF"
                    }
                }
            }
//        prefs = PreferenceManager.getDefaultSharedPreferences(context)
//        sharedPref!!.registerOnSharedPreferenceChangeListener(listener)
        notification = NotificationCancelAlarm(context!!)
        initVisibilityModeSwitchCard()
        initTimeRangeCard()
        initAlarmSoundCard()
        initRunSwitchFAB(view)
        initWidgetCard()
        initBuyPremiumBar()
        initAdCard()
    }

    private fun initAdCard() {
        MobileAds.initialize(context, BuildConfig.APP_KEY)
        adView.loadAd(AdRequest.Builder().build())
    }

    override fun onDestroyView() {
        notification.cancelNotifications()
        super.onDestroyView()
    }

    override fun onResume() {
        super.onResume()
        updateUI()
    }

    //region darkMode Card

    private fun initVisibilityModeSwitchCard() {
        visibilityModeSwitch.isChecked = !lightTheme
        visibilityModeSwitch.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                (activity as MainActivity).changeTheme()
            } else {
                (activity as MainActivity).changeTheme()
            }
        }
    }

    //endregion

    //region RunSwitch FAB

    private fun initRunSwitchFAB(view: View) {
        fab.run {
            setOnClickListener {
                //TODO: detect if user kills app and alarm state wasn't updated (maybe use startActivityForResult)
                if (alarmOn) {
                    Alarm.cancelPendingAlarm(activity!!.applicationContext)
                    saveAlarmState(false)
                    fab.text = getString(R.string.run_switch)
                    notification.cancelNotifications()
                    Snackbar.make(
                        view!!.findViewById(R.id.coordinatorMainFragment),
                        getString(R.string.alarm_canceled_snackbar_message),
                        Snackbar.LENGTH_SHORT
                    ).show()
                } else {
                    if (getCurrentAlarmVolume() <= 0) {
                        Toast.makeText(context, getCurrentAlarmVolume().toString(), Toast.LENGTH_SHORT).show()
                        showMutedAlarmSnackBarWarningSnackBar()
                    } else {
                        saveAlarmState(true)
                        notification.setUpNotification(getString(R.string.cancel_alarm_notification_title))
                        val random = randomTime(minTime, maxTime)
                        val time = System.currentTimeMillis() + random
                        Snackbar.make(
                            view!!.findViewById(R.id.coordinatorMainFragment),
                            snackBarMessage(random),
                            Snackbar.LENGTH_SHORT
                        ).show()
                        Alarm.prepareForAlarm(activity!!.applicationContext, time)
                        fab.text = getString(R.string.turn_off)
                    }
                }
            }
        }
    }

    //endregion

    //region BuyPremium Bar

    private fun initBuyPremiumBar() {
        if (premium) premiumBarMain?.visibility = View.GONE
        else {
            premiumBarMain?.setOnClickListener { activity?.startActivity<BuyPremiumActivity>() }
        }
    }

    //endregion

    //region hideWidgetForever Card

    private fun hideWidgetCardForever() {
        val builder: AlertDialog.Builder = when {
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP -> AlertDialog.Builder(context!!, android.R.style.Theme_Material_Dialog_Alert)
            else -> AlertDialog.Builder(context!!)
        }
        builder.setTitle(getString(R.string.hide_widget_popup_title))
            .setMessage(
                getString(R.string.hide_widget_popup_description)
            )
            .setPositiveButton(android.R.string.yes) { _, _ ->
                widgetCardVisibility = View.GONE
                cardWidget.visibility = widgetCardVisibility
                sharedPref?.edit(true) {
                    putBoolean(IS_WIDGET_CARD_VISIBLE_KEY, false)
                }
            }
            .setNegativeButton(android.R.string.cancel) { _, _ ->
                //Do nothing
            }
            .setIcon(android.R.drawable.ic_dialog_info)
            .show()
    }

    private fun initWidgetCard() {
        widgetCardVisibility = when {
            (activity as MainActivity).isWidgetCardVisible() -> View.VISIBLE
            else -> View.GONE
        }
        cardWidget.visibility = widgetCardVisibility

        dismissWidgetHintButton.setOnClickListener {
            hideWidgetCardForever()
        }
    }

    //endregion

    //region timeRange Card

    private fun initTimeRangeCard() {
        minTime = getRingtoneMinTime(sharedPref)
        maxTime = getRingtoneMaxTime(sharedPref)

        minTimeText.run {
            setText(minTime.toString())
            addTextChangedListener(object: TextWatcher {
                override fun afterTextChanged(p0: Editable?) {
                }

                override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                }

                override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                    when {
                        minTimeText.text.isNullOrBlank() -> minTimeText.error = "Min value cannot be empty"
                        !minTimeText.text.toString().isDigitsOnly() -> minTimeText.error = "Min value must be positive number"
                        minTimeText.text.toString().toInt() > maxTimeText.text.toString().toInt() -> {
                            minTimeText.error = "Min value cannot be higher then max"
                        }
                    }
                }
            })
            onFocusChangeListener = View.OnFocusChangeListener { _, hasFocus ->
                if (!hasFocus) {
                    if (minTimeText.text.isNullOrBlank()) {
                        minTimeText.text = maxTimeText.text
                    } else {
                        when {
                            !minTimeText.text.toString().isDigitsOnly() -> minTimeText.text = maxTimeText.text
                            minTimeText.text.toString().toInt() > maxTimeText.text.toString().toInt() -> minTimeText.text = maxTimeText.text
                            else -> {
                                minTimeText.error = null
                                minTime = minTimeText.text.toString().toInt()
                                (activity as MainActivity).saveRingtoneTime("ringtoneMinTime", minTime)
                            }
                        }
                    }
                }
            }
        }

        maxTimeText.run {
            setText(maxTime.toString())
            addTextChangedListener(object: TextWatcher {
                override fun afterTextChanged(p0: Editable?) {
                }

                override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                }

                override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                    when {
                        maxTimeText.text.isNullOrBlank() -> maxTimeText.error = "Max value cannot be empty"
                        !maxTimeText.text.toString().isDigitsOnly() -> maxTimeText.error = "Max value must be positive number"
                        maxTimeText.text.toString().toInt() < minTimeText.text.toString().toInt() -> {
                            maxTimeText.error = "Max value cannot be lower then min"
                        }
                    }
                }
            })
            onFocusChangeListener = View.OnFocusChangeListener { _, hasFocus ->
                if (!hasFocus) {
                    if (maxTimeText.text.isNullOrBlank()) {
                        maxTimeText.text = minTimeText.text
                    } else {
                        when {
                            !maxTimeText.text.toString().isDigitsOnly() -> maxTimeText.text = minTimeText.text
                            maxTimeText.text.toString().toInt() < minTimeText.text.toString().toInt() -> maxTimeText.text = minTimeText.text
                            else -> {
                                maxTimeText.error = null
                                maxTime = maxTimeText.text.toString().toInt()
                                (activity as MainActivity).saveRingtoneTime("ringtoneMaxTime", maxTime)
                            }
                        }
                    }
                }
            }
        }
    }

    //endregion

    //region alarm sound Card

    //TODO AFTER RELEASE: add vibration on/off option

    private fun initAlarmSoundCard() {
        chooseToneButton.setOnClickListener { openSystemRingtonePicker() }
        currentAlarmName.setOnClickListener { openSystemRingtonePicker() }
        customRingtone.run {
            visibility = View.VISIBLE
            setOnClickListener {
                openCustomSoundPicker()
            }
        }
        alarmVolumeSeekBar.run {
            max = getAlarmMaxVolume()
            progress = getCurrentAlarmVolume()
            if (progress <= 0) {
                volumeIcon.setImageResource(R.drawable.ic_volume_off_black_24dp)
            }
            setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(p0: SeekBar?, volume: Int, p2: Boolean) {
                    saveAlarmVolume(volume)
                    if (volume == 0) {
                        showMutedAlarmSnackBarWarningSnackBar()
                        volumeIcon.setImageResource(R.drawable.ic_volume_off_black_24dp)
                    } else {
                        volumeIcon.setImageResource(R.drawable.ic_volume_up_black_24dp)
                    }
                }

                override fun onStartTrackingTouch(p0: SeekBar?) {
                }

                override fun onStopTrackingTouch(p0: SeekBar?) {
                }
            })
        }
        currentAlarmName.text = (activity as MainActivity).getRingtoneName()
    }

    private fun openSystemRingtonePicker() {
        val intentUpload = Intent(RingtoneManager.ACTION_RINGTONE_PICKER)
        intentUpload.putExtra(RingtoneManager.EXTRA_RINGTONE_TITLE, "SYSTEM SOUNDS LIST")
        intentUpload.putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_DEFAULT, true)
        intentUpload.putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_SILENT, false)
        intentUpload.putExtra(RingtoneManager.EXTRA_RINGTONE_EXISTING_URI, defaultRingtoneUri)
        intentUpload.putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE, RingtoneManager.TYPE_ALL)
        try {
            startActivityForResult(intentUpload, SYSTEM_RINGTONE_PICKER_REQUEST_CODE)
        } catch (e: ActivityNotFoundException) {
            Snackbar.make(
                view!!.findViewById(R.id.coordinatorMainFragment),
                getString(R.string.no_ringtone_picker),
                Snackbar.LENGTH_SHORT
            ).show()
        }
    }

    private fun getCurrentAlarmVolume() : Int {
        audioManager = context?.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        return audioManager.getStreamVolume(AudioManager.STREAM_ALARM)
    }

    private fun getAlarmMaxVolume() : Int {
        audioManager = context?.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        return audioManager.getStreamMaxVolume(AudioManager.STREAM_ALARM)
    }

    private fun saveAlarmVolume(volume: Int) {
        audioManager.setStreamVolume(AudioManager.STREAM_ALARM, volume, 0)
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

    //endregion

    //region sms permissions

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            PERMISSIONS_REQUEST_SEND_SMS -> {
                if (grantResults.isNotEmpty() && permissions[0] == Manifest.permission.SEND_SMS && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // Permission was granted. Enable emergency contact.
                    Log.d("TAG", "Permission granted :)")
                } else {
                    // Permission denied.®
                    Log.d("TAG", "No permission :(")
                    Toast.makeText(
                        context, getString(R.string.no_permission),
                        Toast.LENGTH_LONG
                    ).show()
                    // Disable emergency contact.
                }
            }
        }
    }

    //endregion

    //region utils

//    private fun listenForAlarmStatusChanges() {
//        PreferenceManager.getDefaultSharedPreferences(context)
//            .registerOnSharedPreferenceChangeListener { pref, key ->
//
//            }
//    }

    private fun updateUI() {
        sharedPref?.run {
            if (getString(TIME_TO_NEXT_ALARM_KEY, EMPTY) != EMPTY) {
                val timeToNextAlarm = getString(TIME_TO_NEXT_ALARM_KEY, EMPTY)!!.toInt()
                Snackbar.make(
                    view!!.findViewById(R.id.coordinatorMainFragment),
                    snackBarMessage(timeToNextAlarm),
                    Snackbar.LENGTH_SHORT
                ).show()
                edit(true) {
                    putString(TIME_TO_NEXT_ALARM_KEY, EMPTY)
                }
            } else {
                notification.cancelNotifications()
            }
        }
        Log.i(TAG, "FAB text changed ")
        fab.text = if (alarmOn) getString(R.string.turn_off) else getString(R.string.run_switch)
        alarmVolumeSeekBar.progress = getCurrentAlarmVolume()
        defaultRingtoneUri = (activity as MainActivity).getRingtoneUri()
        setToolbarBasedOnTheme()
    }

    private fun snackBarMessage(timeToNextAlarm: Int) = "New alarm in ${timeToNextAlarm / 1000} seconds"

    private fun showMutedAlarmSnackBarWarningSnackBar() {
        Snackbar.make(view!!.findViewById(R.id.coordinatorMainFragment),
            "Running alarm isn't available when sound is muted", Snackbar.LENGTH_LONG).show()
    }

    private fun replaceFragmentWithTransition(fragment: Fragment) {
        activity!!.supportFragmentManager.transaction(allowStateLoss = true) {
            setCustomAnimations(android.R.anim.slide_in_left, android.R.anim.slide_out_right, android.R.anim.slide_in_left, android.R.anim.slide_out_right)
            replace(R.id.mainFrame, fragment)
            addToBackStack(fragment.toString())
        }
    }

    private fun setToolbarBasedOnTheme() {
        activity?.toolbarTitle?.setBackgroundColor(if (lightTheme) Color.WHITE else Color.BLACK)
        activity?.mainToolbar?.setBackgroundColor(if (lightTheme) Color.WHITE else Color.BLACK)
        activity?.toolbarTitle?.setTextColor(if (lightTheme) Color.BLACK else Color.WHITE)
        activity?.toolbarTitle?.text = resources.getString(R.string.app_name)
    }


    private fun getFileNameFromIntent(uri: Uri): String {
        val returnCursor = context?.contentResolver?.query(uri, null, null, null, null)!!
        val nameIndex = returnCursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
        returnCursor.moveToFirst()
        val name = returnCursor.getString(nameIndex)
        returnCursor.close()
        return name
    }

    override fun updateFAB() {
        Log.i(TAG, "updating FAB ")
        fab?.setBackgroundColor(Color.GREEN)
        if (fab != null ) {
            fab.text = "SNHKFASNf"
        }
        fab?.text = if (alarmOn) "ON__" else "OFF__"
    }

    //endregion
}
