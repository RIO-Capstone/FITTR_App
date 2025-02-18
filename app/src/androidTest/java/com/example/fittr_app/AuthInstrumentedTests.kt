package com.example.fittr_app.ui.auth

import android.content.Intent
import android.view.View
import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.Intents.intended
import androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent
import androidx.test.espresso.intent.matcher.IntentMatchers.hasExtra
import androidx.test.espresso.matcher.RootMatchers
import androidx.test.espresso.matcher.RootMatchers.withDecorView
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.rule.ActivityTestRule
import com.example.fittr_app.DashboardActivity
import com.example.fittr_app.R
import com.example.fittr_app.ToastMatcher
import com.example.fittr_app.ui.registration.RegistrationActivity
import com.google.android.datatransport.cct.internal.NetworkConnectionInfo.MobileSubtype
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.hamcrest.Matchers.not;

@RunWith(AndroidJUnit4::class)
class AuthInstrumentedTests  {

    @get:Rule
    val activityRule = ActivityTestRule(AuthActivity::class.java)
//
//    private lateinit var decorView : View

    @Before
    fun setUp() {
        // Initialize Espresso Intents before running tests
        Intents.init()
//        activityRule.activity.runOnUiThread{
//            decorView = activityRule.activity.window.decorView
//        }
    }

//    @Test
//    fun testRegistrationButtonClick() {
//        onView(withId(R.id.auth_registration_button
//        )).perform(click())
//        intended(hasComponent(RegistrationActivity::class.java.name))
//    }
//
//    @Test
//    fun testLoginButtonClick_EmptyCredentials() {
//        onView(withId(R.id.auth_login_button)).perform(click())
//
//        // Check for Toast message (since there's no errorTextView in AuthActivity)
//        onView(withText("Login Failed. Check your credentials."))
//            .inRoot(ToastMatcher())
//            .check(matches(isDisplayed()))
//    }
//
//    @Test
//    fun testLoginButtonClick_InvalidCredentials() {
//        onView(withId(R.id.auth_id_field)).perform(typeText("invalid@email.com"), closeSoftKeyboard())
//        onView(withId(R.id.auth_password_field)).perform(typeText("wrongpassword"), closeSoftKeyboard())
//        onView(withId(R.id.auth_login_button)).perform(click())
//
//        // Check for error Toast
//        onView(withText("Login Unsuccessful. Try again later."))
//            .inRoot(ToastMatcher())
//            .check(matches(isDisplayed()))
//    }
//
//    @Test
//    fun testLoginButtonClick_ValidCredentials() {
//        // Simulate entering valid credentials
//        onView(withId(R.id.auth_id_field)).perform(typeText("test@example.com"), closeSoftKeyboard())
//        onView(withId(R.id.auth_password_field)).perform(typeText("testpassword"), closeSoftKeyboard())
//        onView(withId(R.id.auth_login_button)).perform(click())
//
//        // Verify that DashboardActivity is launched
//        intended(hasComponent(DashboardActivity::class.java.name))
//
//        // Check if the correct user_id extra is passed (replace with mock server expected value)
//        intended(hasExtra("user_id", 123)) // Replace 123 with your expected test value
//    }

    @Test
    fun testLoginButtonClick_EmptyEmail() {
        onView(withId(R.id.auth_password_field)).perform(typeText("password"), closeSoftKeyboard())
        onView(withId(R.id.auth_login_button)).perform(click())
        Thread.sleep(1000)
        // Verify error message via Toast
        onView(withText("Login Failed. Check your credentials."))
            .inRoot(ToastMatcher())
            .check(matches(isDisplayed()));
    }

    @Test
    fun testLoginButtonClick_EmptyPassword() {
        onView(withId(R.id.auth_id_field)).perform(typeText("email@example.com"), closeSoftKeyboard())
        onView(withId(R.id.auth_login_button)).perform(click())
        Thread.sleep(500)
        // Verify error message via Toast
        onView(withText("Login Failed. Check your credentials."))
            .inRoot(ToastMatcher())
            .check(matches(isDisplayed()))
    }

    @After
    fun tearDown() {
        // Release Espresso Intents after tests finish
        Intents.release()
    }
}
