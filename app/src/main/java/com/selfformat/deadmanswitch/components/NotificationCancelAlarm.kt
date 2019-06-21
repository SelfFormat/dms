package com.selfformat.deadmanswitch.components

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Build
import androidx.core.app.NotificationCompat
import com.selfformat.deadmanswitch.R
import com.selfformat.deadmanswitch.alarming.AlarmingActivity
import com.selfformat.deadmanswitch.data.CHANNEL_ID

class NotificationCancelAlarm {

    lateinit var notificationManager: NotificationManager

    fun setUpNotification(context: Context, title: String) {
        notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val resultPendingIntent = PendingIntent.getActivity(
            context,
            0,
            Intent(context, AlarmingActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT
        )

        val mNotificationId = 1

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createNotificationChannel(
                CHANNEL_ID,
                context.getString(R.string.app_name),
                context.getString(R.string.channel_description), notificationManager)
            val mBuilder = NotificationCompat.Builder(context, CHANNEL_ID)
                .setContentTitle(title)
                .setAutoCancel(true)
                .setContentIntent(resultPendingIntent)
                .setDeleteIntent(resultPendingIntent)
                .setSmallIcon(R.drawable.ic_moon)
            notificationManager.notify(1, mBuilder.build())

        } else {
            val mBuilder = Notification.Builder(context).apply {
                setContentTitle(title)
                setAutoCancel(true)
                setSmallIcon(R.drawable.ic_moon)
                setContentIntent(resultPendingIntent)
            }
            notificationManager.notify(mNotificationId, mBuilder.build())
        }
    }

    fun cancelNotifications() {
        notificationManager.cancelAll()
    }

    private fun createNotificationChannel(id: String, name: String,
                                          description: String, notificationManager: NotificationManager) {
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
            notificationManager.createNotificationChannel(channel)
        }
    }
}