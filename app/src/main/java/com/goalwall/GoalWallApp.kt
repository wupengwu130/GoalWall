package com.goalwall

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import com.goalwall.notification.createNotificationChannels
import com.goalwall.worker.enqueueWidgetSync
import com.goalwall.worker.scheduleDailyReminder
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class GoalWallApp : Application(), Configuration.Provider {
    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    override fun onCreate() {
        super.onCreate()
        createNotificationChannels()
        applicationContext.enqueueWidgetSync()
        applicationContext.scheduleDailyReminder()
    }

    override val workManagerConfiguration: Configuration
        get() =
            Configuration.Builder()
                .setWorkerFactory(workerFactory)
                .build()
}
