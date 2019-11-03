//Don't move this from this package - it's checked in service to ensure it is running or dead
package com.selfformat.deadmanswitch.alarming

import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import android.view.WindowManager
import android.view.animation.AnimationUtils
import androidx.core.content.edit
import com.selfformat.deadmanswitch.R
import com.selfformat.deadmanswitch.base.CustomActivity
import com.selfformat.deadmanswitch.components.Alarm
import com.selfformat.deadmanswitch.components.SmsService
import com.selfformat.deadmanswitch.data.*
import kotlinx.android.synthetic.main.activity_alarming.*
import kotlin.concurrent.thread

class AlarmingActivity : CustomActivity(), SensorEventListener {

    private lateinit var mediaPlayer: MediaPlayer
    private lateinit var audioManager: AudioManager
    private var sensorManager: SensorManager? = null
    private var proximitySensor: Sensor? = null
    private lateinit var uri: Uri
    private lateinit var thread: Thread

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initActivityLayoutAndSettings()
        initManagers()
        initMediaPlayer()
        runSendEmergencyMessageThread()
        turnAlarmOffButton.setOnClickListener {
            textOff.text = getString(R.string.closing)
            turnOffAlarm()
            onBackPressed()
        }
        repeatAlarmButton.setOnClickListener { scheduleNextAlarm() }
        repeatAlarmButton.startAnimation(AnimationUtils.loadAnimation(this, R.anim.large_circle_anim))
    }

    override fun onDestroy() {
        mediaPlayer.release()
        if (::thread.isInitialized) {
            thread.interrupt()
        }
        super.onDestroy()
    }

    override fun onResume() {
        super.onResume()
        sensorManager?.registerListener(this, proximitySensor, SensorManager.SENSOR_DELAY_NORMAL)
    }

    override fun onPause() {
        super.onPause()
        sensorManager?.unregisterListener(this)
    }

    //region initializer

    private fun initManagers() {
        audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager
        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        proximitySensor = sensorManager!!.getDefaultSensor(Sensor.TYPE_PROXIMITY)
    }

    private fun initMediaPlayer() {
        uri = getRingtoneUri()

        getRingtoneUri()
        mediaPlayer = MediaPlayer()
        mediaPlayer.run {
            isLooping = true
            setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_ALARM)
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC).build()
            )
            setDataSource(
                applicationContext, uri
            )
            prepare()
            start()
        }
    }

    private fun initActivityLayoutAndSettings() {
        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        if (lightTheme) {
            setContentView(R.layout.activity_alarming)
        } else {
            setContentView(R.layout.activity_alarming_dark)
        }

        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            window.addFlags(
                WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD or
                        WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                        WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON or
                        WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
            )
        }
    }

    //endregion

    //region alarm lifecycle

    private fun turnOffAlarm() {
        Alarm.cancelPendingAlarm(this)
        releaseMediaPlayer()
        saveAlarmState(false)
    }

    private fun runSendEmergencyMessageThread() {
        if (premium && emergencyEnabled && isSmsPermissionGranted(this)) {
            thread = thread(start = true) {
                val timeToSms = getTimeUntilEmergencySms()
                if (timeToSms != null) {
                    try {
                        Thread.sleep(timeToSms)
                        startService(Intent(this, SmsService::class.java))
                        turnOffAlarm()
                        runOnUiThread {
                            textOff.text = getString(R.string.closing)
                            onBackPressed()
                        }
                    } catch (e: InterruptedException) {
                        Log.i("tag", "InterruptedException")
                    }
                } else {
                    Log.w("tag", "timeToSms was null -> probably not specified")
                }
                //If user stop alarm or re-schedule alarm, or kills activity thread will also die, so it won't send emergency sms
            }
        }
    }

    private fun scheduleNextAlarm() {
        saveAlarmState(true)
        val timeToNextAlarm = randomTime(getRingtoneMinTime(sharedPref), getRingtoneMaxTime(sharedPref))
        sharedPref.edit(true) {
            putString(TIME_TO_NEXT_ALARM_KEY, timeToNextAlarm.toString())
        }
        releaseMediaPlayer()
        Alarm.prepareForAlarm(this, System.currentTimeMillis() + timeToNextAlarm)
        onBackPressed()
    }

    //endregion

    //region utils

    private fun releaseMediaPlayer() {
        mediaPlayer.run {
            stop()
            release()
        }
    }

    private fun getRingtoneUri() : Uri {
        val uri = sharedPref.getString(RINGTONE_KEY, null) ?: return RingtoneManager.getActualDefaultRingtoneUri(this, RingtoneManager.TYPE_RINGTONE)
        return Uri.parse(uri)
    }

    private fun getTimeUntilEmergencySms(): Long? {
        val time = sharedPref.getString(
            TIMEOUT_UNTIL_EMERGENCY_MESSAGE_KEY, null)?.toLong()
        return if (time != null) {
            time * 1000
        } else time
    }

    //endregion

    //region proximity sensor

    override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {}

    override fun onSensorChanged(event: SensorEvent) {
        val distance = event.values?.get(0)?.toInt()
        if (distance == 0) {
            //USER SWIPE IN FRONT OF PROXIMITY SENSOR -> SCHEDULE NEXT ALARM
            scheduleNextAlarm()
        }
    }

    //endregion

    //region showing UI of alarm volume change

    override fun dispatchKeyEvent(event: KeyEvent): Boolean {
        return when (event.keyCode) {
            KeyEvent.KEYCODE_VOLUME_UP -> {
                if (event.action == KeyEvent.ACTION_DOWN) {
                    audioManager.adjustStreamVolume(AudioManager.STREAM_ALARM, AudioManager.ADJUST_RAISE, AudioManager.FLAG_SHOW_UI)
                }
                true
            }
            KeyEvent.KEYCODE_VOLUME_DOWN -> {
                if (event.action == KeyEvent.ACTION_DOWN) {
                    audioManager.adjustStreamVolume(AudioManager.STREAM_ALARM, AudioManager.ADJUST_LOWER, AudioManager.FLAG_SHOW_UI)
                }
                true
            }
            else -> super.dispatchKeyEvent(event)
        }
    }

    //endregion
}