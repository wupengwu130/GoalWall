// Package: com.goalwall
// Layer: Android Test
// Responsibility: Verifies the androidTest source set can start a Hilt test.
// Dependencies: Hilt Android Testing, JUnit
package com.goalwall

import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Rule
import org.junit.Test

@HiltAndroidTest
class HiltAndroidTestSmokeTest {
    @get:Rule
    val hiltRule = HiltAndroidRule(this)

    @Test
    fun hiltAndroidTestSourceSetStarts() {
        hiltRule.inject()
    }
}
