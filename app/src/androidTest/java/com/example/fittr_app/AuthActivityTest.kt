package com.example.fittr_app.ui.auth

import android.view.View
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
import androidx.test.filters.LargeTest
import com.example.fittr_app.DashboardActivity
import com.example.fittr_app.R
import com.example.fittr_app.ui.registration.RegistrationActivity
import org.hamcrest.Matchers.allOf
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.hamcrest.Matchers.not;

@LargeTest
@RunWith(AndroidJUnit4::class)
class AuthActivityTest  {

    @Rule
    @JvmField
    var activityRule = ActivityScenarioRule(AuthActivity::class.java)
//
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
    fun testUsernameFieldExists(){
        val field = onView(
            allOf(
                withId(R.id.auth_id_field),
                isDisplayed()
            )
        )
        field.check(matches(isDisplayed()))
    }

    @Test
    fun testRegistrationButtonClick() {
        onView(withId(R.id.auth_registration_button
        )).perform(click())
        intended(hasComponent(RegistrationActivity::class.java.name))
    }
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
    @Test
    fun testLoginButtonClick_ValidCredentials() {
        // Simulate entering valid credentials
        onView(withId(R.id.auth_id_field)).perform(typeText("testerone@gmail.com"), closeSoftKeyboard())
        onView(withId(R.id.auth_password_field)).perform(typeText("testpassword"), closeSoftKeyboard())
        onView(withId(R.id.auth_login_button)).perform(click())
        Thread.sleep(1000) // takes time for the backend to respond
        // Verify that DashboardActivity is launched
        intended(hasComponent(DashboardActivity::class.java.name))

        // Make sure a valid user_id extra is passed
        intended(hasExtra("user_id", 1)) //
    }

//    @Test
//    fun testLoginButtonClick_EmptyEmail() {
//        onView(withId(R.id.auth_password_field)).perform(typeText("password"), closeSoftKeyboard())
//        onView(withId(R.id.auth_login_button)).perform(click())
//        Thread.sleep(10000) // Ensure the Toast appears before checking
//
//        onView(isRoot())
//            .inRoot(RootMatchers.isSystemAlertWindow())
//            .check(matches(isDisplayed()))
//    }

//    @Test
//    fun testLoginButtonClick_EmptyPassword() {
//        onView(withId(R.id.auth_id_field)).perform(typeText("email@example.com"), closeSoftKeyboard())
//        onView(withId(R.id.auth_login_button)).perform(click())
//        Thread.sleep(500)
//        // Verify error message via Toast
//        onView(withText("Login Failed. Check your credentials."))
//            .inRoot(ToastMatcher())
//            .check(matches(isDisplayed()))
//    }

    @After
    fun tearDown() {
        // Release Espresso Intents after tests finish
        Intents.release()
    }
}
