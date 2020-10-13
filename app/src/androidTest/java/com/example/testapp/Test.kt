package com.example.testapp


import android.content.Intent
import android.os.SystemClock
import android.view.View
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.UiController
import androidx.test.espresso.ViewAction
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import org.junit.Test
import org.junit.Rule

import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.UiSelector

class Test {
    private lateinit var device: UiDevice

    @get:Rule
    var activityRule: ActivityScenarioRule<MapsActivity>
            = ActivityScenarioRule(MapsActivity::class.java)

    @Test
    fun testDescriptionOpen() {
        device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
        onView(withId(R.id.map)).perform(click())
        SystemClock.sleep(500);
        onView(withId(R.id.descriptionPad)).check(matches(isDisplayed()))
    }

    @Test
    fun testNameInput() {
        device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
        onView(withId(R.id.map)).perform(click())
        SystemClock.sleep(500);
        onView(withId(R.id.name)).perform(typeText("TEST_NAME"), closeSoftKeyboard())
        SystemClock.sleep(500);
        onView(withId(R.id.name)).check(matches(withText("TEST_NAME")))
    }

    @Test
    fun testDescriptionInput() {
        device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
        onView(withId(R.id.map)).perform(click())
        SystemClock.sleep(500);
        onView(withId(R.id.description)).perform(typeText("TEST_DESCRIPTION"), closeSoftKeyboard())
        SystemClock.sleep(500);
        onView(withId(R.id.description)).check(matches(withText("TEST_DESCRIPTION")))
    }

    @Test
    fun testCreateNewPointAndDelete() {
        device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
        onView(withId(R.id.map)).perform(click())
        SystemClock.sleep(500);
        onView(withId(R.id.name)).perform(typeText("TEST_NAME"), closeSoftKeyboard())
        SystemClock.sleep(500);
        onView(withId(R.id.description)).perform(typeText("TEST_DESCRIPTION"), closeSoftKeyboard())
        SystemClock.sleep(100);
        onView(withId(R.id.add)).perform(click())
        SystemClock.sleep(1000);
        device.findObject(UiSelector().descriptionContains("Marker")).click()
        onView(withId(R.id.delete)).perform(click())
        SystemClock.sleep(500);

    }





}
