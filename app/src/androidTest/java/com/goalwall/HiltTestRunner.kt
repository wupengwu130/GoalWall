// Package: com.goalwall
// Layer: Android Test
// Responsibility: Uses HiltTestApplication for instrumentation tests.
// Dependencies: AndroidJUnitRunner, HiltTestApplication
package com.goalwall

import android.app.Application
import android.content.Context
import androidx.test.runner.AndroidJUnitRunner
import dagger.hilt.android.testing.HiltTestApplication

class HiltTestRunner : AndroidJUnitRunner() {
    override fun newApplication(
        cl: ClassLoader,
        className: String,
        context: Context,
    ): Application =
        super.newApplication(
            cl,
            HiltTestApplication::class.java.name,
            context,
        )
}
