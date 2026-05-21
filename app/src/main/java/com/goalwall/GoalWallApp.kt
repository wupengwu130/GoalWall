package com.goalwall

import android.app.Application
import com.goalwall.notification.createNotificationChannels
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class GoalWallApp : Application() {

    override fun onCreate() {
        super.onCreate()
        createNotificationChannels()
    }
}
