package com.example.salesforce.core.ui

import org.junit.Assert.assertEquals
import org.junit.Test

class AppScaleTest {
    @Test
    fun scalesDownSmallScreensProportionally() {
        assertEquals(0.82f, screenScaleFactor(320), 0.001f)
        assertEquals(360f / 390f, screenScaleFactor(360), 0.001f)
    }

    @Test
    fun doesNotScaleUpLargerScreens() {
        assertEquals(1f, screenScaleFactor(390), 0.001f)
        assertEquals(1f, screenScaleFactor(600), 0.001f)
    }

    @Test
    fun invalidScreenWidthUsesDefaultScale() {
        assertEquals(1f, screenScaleFactor(0), 0.001f)
    }
}
