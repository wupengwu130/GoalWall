// Package: com.goalwall.di
// Layer: DI
// Responsibility: Worker tags and unique work name constants (no scheduling logic)

package com.goalwall.di

object WorkerModule {

    object Tags {
        const val REMINDER = "reminder"
        const val DAILY_RESET = "daily_reset"
        const val WIDGET_REFRESH = "widget_refresh"
    }

    object UniqueWorkNames {
        const val REMINDER = "unique_reminder"
        const val DAILY_RESET = "unique_daily_reset"
        const val WIDGET_REFRESH = "unique_widget_refresh"
    }
}
