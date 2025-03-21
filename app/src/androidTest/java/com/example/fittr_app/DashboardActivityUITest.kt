package com.example.fittr_app

import android.content.Intent
import android.view.View
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.closeSoftKeyboard
import androidx.test.espresso.action.ViewActions.typeText
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withHint
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.rules.ActivityScenarioRule
import com.example.fittr_app.types.User
import org.hamcrest.CoreMatchers.not
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class DashboardActivityUITest {
    @Rule
    @JvmField
    val activityRule = ActivityScenarioRule(DashboardActivity::class.java)
    private lateinit var decorView: View

    @Before
    fun setUp(){
        Intents.init()
        val intent = Intent(ApplicationProvider.getApplicationContext(), DashboardActivity::class.java)
        intent.putExtra("user_id", 1) // Set user_id

        activityRule.scenario.onActivity { activity ->
            decorView = activity.window.decorView
            activity.user = User(user_id = 1, product_id = 1, first_name = "Tester", last_name = "One", weight = 70, height = 180, email = "testerone@gmail.com")
        }
    }

    @After
    fun tearDown(){
        Intents.release()
    }

    @Test
    fun checkComponentVisibilities(){
        onView(withId(R.id.dashboard_back_btn)).check(matches(isDisplayed()))
        onView(withId(R.id.dashboard_ai_button)).check(matches(isDisplayed()))
    }

    @Test
    fun testDashboardBackButton(){
        val backButton = onView(withId(R.id.dashboard_back_btn))
        backButton.perform(click())
        // should navigate to the auth page
        onView(withId(R.id.auth_login_button)).check(matches(isDisplayed()))
    }

    @Test
    fun testDashboardAIButton(){
        val squatAiRepTextView = onView(withId(R.id.squat_exercise_rep))
        val rightBicepCurlAiRepTextView = onView(withId(R.id.right_bicep_curl_exercise_rep))
        val leftBicepCurlAiRepTextView = onView(withId(R.id.left_bicep_curl_exercise_rep))
        squatAiRepTextView.check(matches(withHint("0")))
        rightBicepCurlAiRepTextView.check(matches(withHint("0")))
        leftBicepCurlAiRepTextView.check(matches(withHint("0")))
        val aiButton = onView(withId(R.id.dashboard_ai_button))
        // give time for the page to load the user
        aiButton.perform(click())
        // wait for the API response
        Thread.sleep(5000)
        squatAiRepTextView.check(matches(not(withText("0"))))
        rightBicepCurlAiRepTextView.check(matches(not(withText("0"))))
        leftBicepCurlAiRepTextView.check(matches(not(withText("0"))))
    }

    @Test
    fun unableToNavigateToMainWithoutBluetoothConnection(){
        val squatStartButton = onView(withId(R.id.dashboard_exercise_squats))
        squatStartButton.perform(click())
        // should show Toast error
        Thread.sleep(500)
        onView(withText("Establish Bluetooth connection first")).inRoot(ToastMatcher().apply { matches(isDisplayed()) })
    }

    @Test
    fun testClickBluetoothButtonWithoutPermission(){
        val bluetoothButton = onView(withId(R.id.dashboard_bluetooth_status_button))
        bluetoothButton.perform(click())
        // should check for bluetooth devices
        Thread.sleep(500)
        onView(withText("Bluetooth permission not granted")).inRoot(ToastMatcher().apply { matches(isDisplayed()) })
    }

    @Test
    fun changeSquatRepCount(){
        val squatRepTextView = onView(withId(R.id.squat_exercise_rep))
        squatRepTextView.check(matches(withHint("0")))
        squatRepTextView.perform(click(), typeText("10"), closeSoftKeyboard())
        squatRepTextView.check(matches(withText("10")))
    }

    @Test
    fun testInvalidSquatRepCount() {
        val squatRepTextView = onView(withId(R.id.squat_exercise_rep))
        squatRepTextView.check(matches(withHint("0")))
        squatRepTextView.perform(click(), typeText("1.0"), closeSoftKeyboard())
        squatRepTextView.check(matches(withText("10")))
        onView(withText("Only whole numbers are allowed.")).inRoot(ToastMatcher().apply { matches(isDisplayed()) })
    }

    @Test
    fun testZeroSquatRepCount() {
        val squatRepTextView = onView(withId(R.id.squat_exercise_rep))
        squatRepTextView.check(matches(withHint("0")))
        squatRepTextView.perform(click(), typeText("0"), closeSoftKeyboard())
        Thread.sleep(500)
        onView(withText("Value must be greater than 0."))
            .inRoot(ToastMatcher().apply { matches(isDisplayed()) })
    }

}