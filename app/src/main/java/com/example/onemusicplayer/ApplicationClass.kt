package com.example.onemusicplayer

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build

/*
   Role of ApplicationClass in application is it run after application
   is installed in device whether user open the application or not
*/


class ApplicationClass : Application() {

    companion object {
        const val CHANNEL_ID = "channel1"
        const val PLAY = "play"
        const val NEXT = "next"
        const val PREVIOUS = "previous"
        const val EXIT = "exit"
    }

    override fun onCreate() {
        super.onCreate()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            val notificationChannel = NotificationChannel(
                CHANNEL_ID,
                "Now Playing Song",
                NotificationManager.IMPORTANCE_HIGH
            )

            notificationChannel.description = "This is important channel for showing songs!!"
            val notificationManager : NotificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(notificationChannel)

        }
    }
}