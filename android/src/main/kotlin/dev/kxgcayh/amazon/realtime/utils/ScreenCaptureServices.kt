/*
 * Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
 * SPDX-License-Identifier: Apache-2.0
 */

package dev.kxgcayh.amazon.realtime.utils

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Binder
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import dev.kxgcayh.amazon.realtime.R

class ScreenCaptureService : Service() {
    private lateinit var notificationManager: NotificationManager

    private val CHANNEL_ID = "ScreenCaptureServiceChannelID"
    private val CHANNEL_NAME = "Screen Share"
    private val SERVICE_ID = 1

    private val binder = ScreenCaptureBinder()

    inner class ScreenCaptureBinder : Binder() {
        fun getService(): ScreenCaptureService = this@ScreenCaptureService
    }

    override fun onCreate() {
        super.onCreate()

        notificationManager =
            applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_DEFAULT
            )
            notificationManager.createNotificationChannel(channel)
        }

        startForeground(
            SERVICE_ID,
            NotificationCompat.Builder(this, CHANNEL_ID)
                // .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle("Screen Sharing")
                .setContentText("Your screen is being shared.")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .build()
        )
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = binder
}
