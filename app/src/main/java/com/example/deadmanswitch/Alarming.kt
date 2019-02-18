package com.example.deadmanswitch

import android.content.Context
import android.content.pm.ActivityInfo
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.MediaPlayer
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import android.view.View
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_alarming.*
import java.util.Random

class Alarming : AppCompatActivity(), SensorEventListener {

    private lateinit var mp: MediaPlayer
    private var randomTime = Random()
    private var alarm = Alarm()
    private var mSensorManager: SensorManager? = null
    private var mProximity: Sensor? = null
    private var valueMin = 5
    private var valueMax = 10
    private lateinit var audioManager: AudioManager
    private val USER_AUDIO_VOLUME: Int = 5
    private val EMERGENCY_TIME = 300000

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_alarming)
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        Log.i("Alarming activity", " here")

        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            window.addFlags(
                WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD or
                        WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                        WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON  or
                        WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
            )
        }

        mSensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        mProximity = mSensorManager!!.getDefaultSensor(Sensor.TYPE_PROXIMITY)

        audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager
        audioManager.setStreamVolume(AudioManager.STREAM_ALARM, USER_AUDIO_VOLUME, 0)

        mp = MediaPlayer()
        mp.run {
            isLooping = true
            setAudioAttributes(AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_ALARM)
                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC).build())
            setDataSource(
                applicationContext,
                Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.old)
            )
            prepare()
            start()
        }

        val time = System.currentTimeMillis() + EMERGENCY_TIME //change it to time from preferences

        alarm.prepareForSms(this, time, Alarm.State.ON)


        val off = findViewById<View>(R.id.clickableLayOff)
        off.setOnClickListener {
            textOff.text = "jakisstring"
            releaseMediaPlayer()
            alarm.prepareForSms(this, null, Alarm.State.OFF)
            onBackPressed()
        }

        val mute = findViewById<View>(R.id.circle_repeat)
        mute.setOnClickListener {
            textRepeat.text = "jakisstring"
            releaseMediaPlayer()
            mp = MediaPlayer.create(this, R.raw.old)
            runAlarmAgain()
        }
    }

     override fun onResume() {
         super.onResume()
         mSensorManager!!.registerListener(this, mProximity, SensorManager.SENSOR_DELAY_NORMAL)
    }

    override fun onPause() {
        super.onPause()
        mSensorManager!!.unregisterListener(this)
    }

    override fun onSensorChanged(event: SensorEvent) {
        val distance = event?.values?.get(0)?.toInt()
        if (distance == 0) {
            runAlarmAgain()
        }
    }

    override fun dispatchKeyEvent(event: KeyEvent): Boolean {
        val action = event.action
        val keyCode = event.keyCode
        return when (keyCode) {
            KeyEvent.KEYCODE_VOLUME_UP -> {
                if (action == KeyEvent.ACTION_DOWN) {
                    audioManager.adjustStreamVolume(AudioManager.STREAM_ALARM, AudioManager.ADJUST_RAISE, AudioManager.FLAG_SHOW_UI)
                }
                true
            }
            KeyEvent.KEYCODE_VOLUME_DOWN -> {
                if (action == KeyEvent.ACTION_DOWN) {
                    audioManager.adjustStreamVolume(AudioManager.STREAM_ALARM, AudioManager.ADJUST_LOWER, AudioManager.FLAG_SHOW_UI)
                }
                true
            }
            else -> super.dispatchKeyEvent(event)
        }
    }

    private fun runAlarmAgain() {
        alarm.prepareForSms(this, null, Alarm.State.OFF)
        releaseMediaPlayer()
        val time = System.currentTimeMillis() + randomTime()
        alarm.prepareForSms(this, null, Alarm.State.OFF)
        alarm.startAlarm(this, time, Alarm.State.ON)
        onBackPressed()
    }

    private fun releaseMediaPlayer() {
        mp.run {
            stop()
            release()
        }
    }

    override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {}

    private fun randomTime(): Int {
        val initialTime = valueMin * 1000
        val generated = randomTime.nextInt(valueMax + 1 - valueMin) * 1000
        Log.i("init: $initialTime", ", gen: $generated")
        return generated + initialTime
    }
}