package com.example.fittr_app

import android.content.Intent
import android.os.Looper
import android.view.View
import androidx.test.core.app.ActivityScenario
import com.example.fittr_app.ui.auth.AuthActivity
import com.example.fittr_app.ui.registration.RegistrationActivity
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertNotNull
import junit.framework.TestCase.assertTrue
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config
import org.robolectric.shadows.ShadowLooper
import org.robolectric.shadows.ShadowToast
import java.util.concurrent.TimeUnit

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [30])
class AuthActivityUnitTest {
    private lateinit var scenario: ActivityScenario<AuthActivity>
    private lateinit var decorView: View

    @Before
    fun setUp() {
        scenario = ActivityScenario.launch(AuthActivity::class.java)
        scenario.onActivity { activity ->
            decorView = activity.window.decorView
        }
    }

    @After
    fun tearDown() {
        scenario.close()
    }

    @Test
    fun testUsernameFieldExists() {
        scenario.onActivity { activity ->
            val field = activity.findViewById<View>(R.id.auth_id_field)
            assertNotNull(field)
            assertTrue(field.isShown)
        }
    }

    @Test
    fun testRegistrationButtonClick() {
        scenario.onActivity { activity ->
            val button = activity.findViewById<View>(R.id.auth_registration_button)
            assertNotNull(button)
            button.performClick()

            // Get the ShadowActivity for intent verification
            val shadowActivity = shadowOf(activity)
            val actualIntent = shadowActivity.nextStartedActivity
            assertNotNull(actualIntent)

            // Verify that the intent's component matches RegistrationActivity
            val expectedIntent = Intent(activity, RegistrationActivity::class.java)
            assertEquals(expectedIntent.component, actualIntent.component)
        }
    }


    @Test
    fun testLoginButtonClick_ValidCredentials() {
        scenario.onActivity { activity ->
            val usernameField = activity.findViewById<View>(R.id.auth_id_field)
            val passwordField = activity.findViewById<View>(R.id.auth_password_field)
            val loginButton = activity.findViewById<View>(R.id.auth_login_button)
            // check that all fields exist on the page
            assertNotNull(usernameField)
            assertNotNull(passwordField)
            assertNotNull(loginButton)

            usernameField.requestFocus()
            passwordField.requestFocus()

            loginButton.performClick()
            // after clicking check that the dashboard title exists on the screen
            val dashboardTitle = activity.findViewById<View>(R.id.activity_dashboard_title)
            assertNotNull(dashboardTitle)
            assertTrue(dashboardTitle.isShown)
        }
    }

    @Test
    fun testLoginButtonClick_InvalidCredentials() {
        scenario.onActivity { activity ->
            val loginButton = activity.findViewById<View>(R.id.auth_login_button)
            assertNotNull(loginButton)
            loginButton.performClick()
            ShadowLooper.idleMainLooper()
            println(ShadowToast.getTextOfLatestToast())
            assertTrue(ShadowToast.getTextOfLatestToast(),equals("Login Failed. Check your credentials."));
        }
    }
}