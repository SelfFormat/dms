package com.example.deadmanswitch

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import androidx.fragment.app.transaction
import kotlinx.android.synthetic.main.activity_main.*
import android.telephony.SmsManager
import android.util.Log
import android.widget.Toast
import androidx.core.content.edit

class MainActivity : CustomStatusBarActivity() {
    private var notificationManager: NotificationManager? = null
    private lateinit var sharedPref: SharedPreferences

    override fun onResume() {
        super.onResume()
        val emergencySMS = sharedPref.getBoolean(EMERGENCY_SMS, false)
        if (emergencySMS) {
            Log.i("SENDING", "SMS")
            smsSendMessage()
            sharedPref.edit {
                putBoolean(EMERGENCY_SMS, false)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)
        setUpStatusBarAppearance()
        toolbarTitle.text = resources.getString(R.string.app_name)
        sharedPref = getSharedPreferences(
            "myPrefs",
            Context.MODE_PRIVATE
        )

        supportFragmentManager.transaction(allowStateLoss = true) {
            replace(R.id.mainFrame, MainFragment.newInstance(), "MAIN")
        }
        notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notification("Title", "description")
    }

    //region sending emergency sms

    fun getEmergencyContact(): EmergencyContact {
        return EmergencyContact(
            sharedPref.getString("contactNumber", null),
            sharedPref.getString("emergencyMessage", null),
            sharedPref.getString("contactName", null)
        )
    }

    fun smsSendMessage() {
        val contact = getEmergencyContact()
        Toast.makeText(this, "Sending emergency sms to: ${contact.number}", Toast.LENGTH_SHORT).show()
        val serviceCenterAddress: String? = null
        val smsManager = SmsManager.getDefault()
        smsManager.sendTextMessage(
            contact.number, serviceCenterAddress, contact.message,
            null, null
        )
    }

    //endregion

    //region notification

    private fun notification(title: String, body: String) {

        val resultIntent = Intent(this, MainActivity::class.java)

        resultIntent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP)
        resultIntent.action = Intent.ACTION_MAIN
        resultIntent.addCategory(Intent.CATEGORY_LAUNCHER)
        resultIntent.putExtra("off", "TURN OFF")

        val resultPendingIntent = PendingIntent.getActivity(
            this,
            0,
            resultIntent,
            PendingIntent.FLAG_UPDATE_CURRENT
        )
        val mNotificationId = 1

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createNotificationChannel(
                "com.example.deadmanswitch",
                "Dead Man Switch",
                "DMS Channel")
            val mBuilder = Notification.Builder(this, "com.example.deadmanswitch")
                .setContentTitle(title)
                .setContentText(body)
                .setAutoCancel(true)
                .setContentIntent(resultPendingIntent)
                .setSmallIcon(R.drawable.ic_moon)
            notificationManager?.notify(1, mBuilder.build())

        } else {
            val mBuilder = Notification.Builder(this).apply {
                setContentTitle(title)
                setContentText(body)
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

    companion object {
        public const val EMERGENCY_SMS = "EMERGENCY_SMS"
    }
}