package com.example.deadmanswitch

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.ActivityInfo
import android.graphics.Color
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
import android.view.View
import android.view.WindowManager
import android.view.animation.AnimationUtils
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import kotlinx.android.synthetic.main.activity_alarming.*
import java.util.*

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
    private var notificationManager: NotificationManager? = null
    private lateinit var sharedPref: SharedPreferences
    private var urik: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_alarming)
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        Log.i("Alarming activity", " here")
        sharedPref = getSharedPreferences(
            "myPrefs",
            Context.MODE_PRIVATE
        )

        urik = sharedPref.getString("ringtone", null)

        val uri: Uri = getRingtoneUri()

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

        notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notification("Cancel Dead Man's Switch alarm")

        audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager
        audioManager.setStreamVolume(AudioManager.STREAM_ALARM, USER_AUDIO_VOLUME, 0)

        mp = MediaPlayer()
        mp.run {
            isLooping = true
            setAudioAttributes(AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_ALARM)
                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC).build())
            setDataSource(
                applicationContext, uri
            )
            prepare()
            start()
        }

        val time = System.currentTimeMillis() + EMERGENCY_TIME //change it to time from preferences

        alarm.prepareForSms(this, time, Alarm.State.ON)

        val off = findViewById<View>(R.id.clickableLayOff)
        off.setOnClickListener {
            textOff.text = getString(R.string.closing)
            releaseMediaPlayer()
            alarm.prepareForSms(this, null, Alarm.State.OFF)
            onBackPressed()
        }

        val mute = findViewById<View>(R.id.circle_repeat)
        animateMuteCircleButton(this)
        mute.setOnClickListener {
            textRepeat.text = getString(R.string.closing)
            releaseMediaPlayer()
            mp = MediaPlayer.create(this, uri)
            runAlarmAgain()
        }
    }

    private fun animateMuteCircleButton(context: Context) {
        circle_repeat.startAnimation(AnimationUtils.loadAnimation(context, R.anim.large_circle_anim))
    }

    private fun getRingtoneUri() : Uri {
        val uri = sharedPref.getString("ringtone", null) ?: return RingtoneManager.getActualDefaultRingtoneUri(this, RingtoneManager.TYPE_RINGTONE)
        return Uri.parse(uri)
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
        val distance = event.values?.get(0)?.toInt()
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
        alarm.broadcast(this, time, Alarm.State.ON)
        onBackPressed()
    }

    private fun releaseMediaPlayer() {
        mp.run {
            stop()
            release()
        }
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        textOff.text = getString(R.string.closing)
        releaseMediaPlayer()
        alarm.prepareForSms(this, null, Alarm.State.OFF)
        onBackPressed()
    }

    override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {}

    private fun randomTime(): Int {
        val initialTime = valueMin * 1000
        val generated = randomTime.nextInt(valueMax + 1 - valueMin) * 1000
        Log.i("init: $initialTime", ", gen: $generated")
        return generated + initialTime
    }

    //region notification

    private fun notification(title: String) {

        val resultPendingIntent = PendingIntent.getActivity(
            this,
            0,
            Intent(this, Alarming::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT
        )

        val mNotificationId = 1

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createNotificationChannel(
                "com.example.deadmanswitch",
                "Dead Man Switch",
                "DMS Channel")
            val mBuilder = NotificationCompat.Builder(this, "com.example.deadmanswitch")
                .setContentTitle(title)
                .setAutoCancel(true)
                .setContentIntent(resultPendingIntent)
                .setSmallIcon(R.drawable.ic_moon)
            notificationManager?.notify(1, mBuilder.build())

        } else {
            val mBuilder = Notification.Builder(this).apply {
                setContentTitle(title)
                setAutoCancel(true)
                setSmallIcon(R.drawable.ic_moon)
                setContentIntent(resultPendingIntent)
            }
            notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager?
            notificationManager?.notify(mNotificationId, mBuilder.build())
        }
    }

    private fun createNotificationChannel(id: String, name: String,
                                          description: String) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            return
        } else {
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                NotificationChannel(id, name, importance)
            } else {
                TODO("VERSION.SDK_INT < O")
            }
            channel.description = description
            channel.enableLights(true)
            channel.lightColor = Color.RED
            channel.enableVibration(true)
            channel.vibrationPattern =
                longArrayOf(100, 200, 300, 400, 500, 400, 300, 200, 400)
            notificationManager?.createNotificationChannel(channel)
        }
    }

    //endregion
}