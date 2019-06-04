package com.selfformat.deadmanswitch

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
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
import android.view.KeyEvent
import android.view.WindowManager
import android.view.animation.AnimationUtils
import androidx.core.app.NotificationCompat
import androidx.core.content.edit
import com.selfformat.deadmanswitch.base.CustomActivity
import com.selfformat.deadmanswitch.components.Alarm
import com.selfformat.deadmanswitch.components.SmsService
import com.selfformat.deadmanswitch.data.*
import kotlinx.android.synthetic.main.activity_alarming.*
import kotlin.concurrent.thread

class AlarmingActivity : CustomActivity(), SensorEventListener {

    private lateinit var mp: MediaPlayer
    private var sensorManager: SensorManager? = null
    private var proximitySensor: Sensor? = null
    private lateinit var audioManager: AudioManager
    private var notificationManager: NotificationManager? = null
    private lateinit var uri: Uri

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN)
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
                        WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON  or
                        WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
            )
        }

        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        proximitySensor = sensorManager!!.getDefaultSensor(Sensor.TYPE_PROXIMITY)
        notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        setUpNotification(getString(R.string.cancle_alarm_notification_title))
        audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager
        uri = getRingtoneUri()

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

        runSendEmergencyMessageThread()

        turnAlarmOffButton.setOnClickListener {
            textOff.text = getString(R.string.closing)
            Alarm.cancelAlarm(this)
            releaseMediaPlayer()
            saveAlarmState(false)
            onBackPressed()
        }

        repeatAlarmButton.startAnimation(AnimationUtils.loadAnimation(this, R.anim.large_circle_anim))
        repeatAlarmButton.setOnClickListener {
            textRepeat.text = getString(R.string.closing)
            releaseMediaPlayer()
            mp = MediaPlayer.create(this, uri)
            scheduleNextAlarm()
        }
    }

    private fun runSendEmergencyMessageThread() {
        thread {
            //If user stop alarm or re-schedule alarm, or kills activity thread will also die, so it won't send emergency sms
            val timeToSms = sharedPref.getString(
                TIMEOUT_UNTIL_EMERGENCY_MESSAGE_KEY,
                DEFAULT_EMERGENCY_TIME.toString()
            ).toLong() * 1000
            Thread.sleep(timeToSms)
            val smsIntent = Intent(this, SmsService::class.java)
            startService(smsIntent)
        }
    }

    private fun getRingtoneUri() : Uri {
        val uri = sharedPref.getString(RINGTONE_KEY, null) ?: return RingtoneManager.getActualDefaultRingtoneUri(this, RingtoneManager.TYPE_RINGTONE)
        return Uri.parse(uri)
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

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        textOff.text = getString(R.string.closing)
        releaseMediaPlayer()
        onBackPressed()
    }

    private fun releaseMediaPlayer() {
        mp.run {
            stop()
            release()
        }
    }

    override fun onDestroy() {
        mp.release()
        super.onDestroy()
    }

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

    //region proximity sensor

    override fun onResume() {
         super.onResume()
         sensorManager!!.registerListener(this, proximitySensor, SensorManager.SENSOR_DELAY_NORMAL)
    }

    override fun onPause() {
        super.onPause()
        sensorManager!!.unregisterListener(this)
    }

    override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {}

    override fun onSensorChanged(event: SensorEvent) {
        val distance = event.values?.get(0)?.toInt()
        if (distance == 0) {
            scheduleNextAlarm()
        }
    }

    //endregion

    //region setUpNotification

    private fun setUpNotification(title: String) {
        val resultPendingIntent = PendingIntent.getActivity(
            this,
            0,
            Intent(this, AlarmingActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT
        )

        val mNotificationId = 1

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createNotificationChannel(
                CHANNEL_ID,
                getString(R.string.app_name),
                getString(R.string.channel_description))
            val mBuilder = NotificationCompat.Builder(this, CHANNEL_ID)
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