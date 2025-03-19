package com.example.fittr_app.ui.auth

import android.view.View
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.Intents.intended
import androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent
import androidx.test.espresso.intent.matcher.IntentMatchers.hasExtra
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import com.example.fittr_app.DashboardActivity
import com.example.fittr_app.R
import com.example.fittr_app.ToastMatcher
import com.example.fittr_app.ui.registration.RegistrationActivity
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@LargeTest
@RunWith(AndroidJUnit4::class)
class AuthActivityTest  {

    @Rule
    @JvmField
    var activityRule = ActivityScenarioRule(AuthActivity::class.java)

    private lateinit var decorView : View

    @Before
    fun setUp() {
        // Initialize Espresso Intents before running tests
        Intents.init()
        activityRule.scenario.onActivity{activity->
            decorView = activity.window.decorView
        }
    }

    @Test
    fun testFieldsExist() {
        // Check if all fields are displayed
        onView(withId(R.id.auth_id_field)).check(matches(isDisplayed()))
        onView(withId(R.id.auth_password_field)).check(matches(isDisplayed()))
        onView(withId(R.id.auth_login_button)).check(matches(isDisplayed()))
        onView(withId(R.id.auth_registration_button)).check(matches(isDisplayed()))
    }


    @Test
    fun testRegistrationButtonClick() {
        onView(withId(R.id.auth_registration_button
        )).perform(click())
        intended(hasComponent(RegistrationActivity::class.java.name))
    }

    @Test
    fun testLoginButtonClick_EmptyCredentials() {
        onView(withId(R.id.auth_login_button)).perform(click())
        Thread.sleep(500)
        onView(withText("Login Failed. Check your credentials."))
            .inRoot(ToastMatcher().apply { matches(isDisplayed()) })
    }

    @Test
    fun testLoginButtonClick_InvalidCredentials() {
        onView(withId(R.id.auth_id_field)).perform(typeText("invalid@email.com"), closeSoftKeyboard())
        onView(withId(R.id.auth_password_field)).perform(typeText("wrongpassword"), closeSoftKeyboard())
        onView(withId(R.id.auth_login_button)).perform(click())
        // Check for error Toast
        Thread.sleep(500)
        onView(withText("Login Unsuccessful. Try again later."))
            .inRoot(ToastMatcher().apply { matches(isDisplayed()) })
    }

    @Test
    fun testLoginButtonClick_ValidCredentials() {
        // Simulate entering valid credentials
        onView(withId(R.id.auth_id_field)).perform(typeText("testerone@gmail.com"), closeSoftKeyboard())
        onView(withId(R.id.auth_password_field)).perform(typeText("testpassword"), closeSoftKeyboard())
        onView(withId(R.id.auth_login_button)).perform(click())
        Thread.sleep(500) // takes time for the backend to respond
        // Check for successful Toast
        onView(withText("Login Complete"))
            .inRoot(ToastMatcher().apply { matches(isDisplayed()) })
        Thread.sleep(200)
        // Verify that DashboardActivity is launched
        intended(hasComponent(DashboardActivity::class.java.name))
        intended(hasExtra("user_id", 1)) //
    }

    @After
    fun tearDown() {
        // Release Espresso Intents after tests finish
        Intents.release()
    }
}
