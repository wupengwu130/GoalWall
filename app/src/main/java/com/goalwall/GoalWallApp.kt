package com.goalwall

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import com.goalwall.data.UserPreferences
import com.goalwall.notification.createNotificationChannels
import com.goalwall.util.LocaleHelper
import com.goalwall.worker.ReminderWorkScheduler
import com.goalwall.worker.enqueueWidgetSync
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import javax.inject.Inject

@HiltAndroidApp
class GoalWallApp : Application(), Configuration.Provider {
    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    @Inject
    lateinit var userPreferences: UserPreferences

    @Inject
    lateinit var reminderWorkScheduler: ReminderWorkScheduler

    override fun onCreate() {
        super.onCreate()
        runBlocking {
            LocaleHelper.applyLanguage(userPreferences.language.first())
        }
        createNotificationChannels()
        applicationContext.enqueueWidgetSync()
        runBlocking {
            reminderWorkScheduler.reschedule()
        }
    }

    override val workManagerConfiguration: Configuration
        get() =
            Configuration.Builder()
                .setWorkerFactory(workerFactory)
                .build()
}
