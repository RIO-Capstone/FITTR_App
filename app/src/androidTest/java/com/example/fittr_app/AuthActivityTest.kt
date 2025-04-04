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
import com.example.fittr_app.connections.ApiClient
import com.example.fittr_app.connections.ApiClientProvider
import com.example.fittr_app.types.GetUsersBackendResponse
import com.example.fittr_app.types.LoginUserBackendResponse
import com.example.fittr_app.types.User
import com.example.fittr_app.types.UserSimple
import com.example.fittr_app.ui.profile.SwitchUserActivity
import com.example.fittr_app.ui.registration.RegistrationActivity
import io.mockk.coEvery
import io.mockk.mockk
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

    private var mockApiClient = mockk<ApiClient>()

    @Before
    fun setUp() {
        // Initialize Espresso Intents before running tests
        Intents.init()
        coEvery { mockApiClient.loginUser(any(),any()) } returns Result.success(
            LoginUserBackendResponse(user =
                User(user_id = 1,
                    first_name = "John",
                    last_name = "Doe",
                    weight = 80,
                    height = 180,
                    email = "johndoe@gmail.com",
                    product_id = 1), message = "")
        )
        coEvery { mockApiClient.getUsers(any(),any())} returns Result.success(
            GetUsersBackendResponse(listOf(UserSimple(id = 1, full_name = "John Doe")))
        )
        ApiClientProvider.apiClient = mockApiClient
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
    }

    @Test
    fun testLoginButtonClick_EmptyCredentials() {
        coEvery { mockApiClient.loginUser(any(),any()) } returns Result.failure(Exception("Test Error"))
        onView(withId(R.id.auth_login_button)).perform(click())
        Thread.sleep(500)
        onView(withText("Login Failed. Check your credentials."))
            .inRoot(ToastMatcher().apply { matches(isDisplayed()) })
    }

    @Test
    fun testLoginButtonClick_InvalidCredentials() {
        coEvery { mockApiClient.loginUser(any(),any()) } returns Result.failure(Exception("Test Error"))
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
        // Verify that SwitchUserActivity is launched
        intended(hasComponent(SwitchUserActivity::class.java.name))
        intended(hasExtra("product_id", 1))
    }

    @After
    fun tearDown() {
        // Release Espresso Intents after tests finish
        Intents.release()
    }
}
