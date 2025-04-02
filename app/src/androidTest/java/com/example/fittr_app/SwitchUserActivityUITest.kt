package com.example.fittr_app

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.UiController
import androidx.test.espresso.ViewAction
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.RecyclerViewActions
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent
import androidx.test.espresso.intent.matcher.IntentMatchers.hasExtra
import androidx.test.espresso.matcher.ViewMatchers.hasChildCount
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.isRoot
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import com.example.fittr_app.connections.ApiClient
import com.example.fittr_app.connections.ApiClientProvider
import com.example.fittr_app.types.AIReply
import com.example.fittr_app.types.Feedback
import com.example.fittr_app.types.GetUserBackendResponse
import com.example.fittr_app.types.GetUsersBackendResponse
import com.example.fittr_app.types.User
import com.example.fittr_app.types.UserSimple
import com.example.fittr_app.ui.profile.SwitchUserActivity
import io.mockk.coEvery
import io.mockk.mockk
import org.hamcrest.CoreMatchers.allOf
import org.junit.After
import org.junit.Before
import org.junit.Test

class SwitchUserActivityUITest {

    private lateinit var decorView: View

    private var mockApiClient : ApiClient = mockk()

    @Before
    fun setup() {
        Intents.init()

        val userResponse = GetUserBackendResponse(
            User(1, "John", "Doe", 80, 180, "johndoe@gmail.com", 1)
        )

        val usersResponse = GetUsersBackendResponse(
            users = listOf(
                UserSimple(1, "John Doe"),
                UserSimple(2, "Jane Smith")
            )
        )

        val mockAIFeedback = AIReply(user_id = 1, feedback_message = Feedback(
            summary_advice = "",
            summary_analysis = "",
            range_of_motion_score = 10,
            future_advice = "",
            form_score = 20,
            stability_score = 30
        ))

        coEvery {
            mockApiClient.getUser(any(), any())
        } returns Result.success(userResponse)

        coEvery {
            mockApiClient.getUsers(any(), any())
        } returns Result.success(usersResponse)

        coEvery {
            mockApiClient.getUserAIReply(any())
        } returns Result.success(mockAIFeedback)

        ApiClientProvider.apiClient = mockApiClient

        // Launch the activity explicitly
        val scenario = ActivityScenario.launch(SwitchUserActivity::class.java)
        scenario.onActivity { activity ->
            decorView = activity.window.decorView
        }
    }

    @Test
    fun testRecyclerViewPopulatedWithUsers() {
        onView(withId(R.id.viewProfiles))
            .check(matches(hasChildCount(3))) // 3 items exactly (users + add user)
    }

    @Test
    fun testNavigationToDashboard() {
        // Perform click on first item in RecyclerView
        onView(withId(R.id.viewProfiles))
            .perform(
                RecyclerViewActions.actionOnItemAtPosition<RecyclerView.ViewHolder>(
                    0,
                    click()
                )
            )
        Thread.sleep(1000)
        // Verify intent
        Intents.intended(allOf(
            hasComponent(DashboardActivity::class.java.name),
            hasExtra("User_Name", "John Doe"),
            hasExtra("user_id", 1)
        ))
    }

    @Test
    fun testErrorHandlingWhenNoUsersLoaded() {
        // Enqueue a server error
        coEvery {
            mockApiClient.getUsers(any(), any())
        } returns Result.failure(Exception("Failed to load users"))
        Thread.sleep(500)
        onView(withText("Failed to load users"))
            .inRoot(ToastMatcher().apply { matches(isDisplayed()) })
    }

    @After
    fun tearDown(){
        Intents.release()
    }

}