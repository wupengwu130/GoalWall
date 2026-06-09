package com.goalwall.worker

import android.content.Context
import com.goalwall.data.UserPreferences
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ReminderWorkScheduler
    @Inject
    constructor(
        @ApplicationContext private val context: Context,
        private val userPreferences: UserPreferences,
    ) {
        suspend fun reschedule() {
            val hour = userPreferences.reminderHour.first()
            val minute = userPreferences.reminderMinute.first()
            context.scheduleDailyReminder(hour = hour, minute = minute)
        }
    }
