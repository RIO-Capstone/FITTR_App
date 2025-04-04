package com.example.fittr_app

import android.content.Intent
import android.view.View
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.rules.ActivityScenarioRule
import com.example.fittr_app.connections.ApiClient
import com.example.fittr_app.connections.ApiClientProvider
import com.example.fittr_app.types.AIReply
import com.example.fittr_app.types.Feedback
import com.example.fittr_app.types.GetUserBackendResponse
import com.example.fittr_app.types.GetUsersBackendResponse
import com.example.fittr_app.types.User
import com.example.fittr_app.types.UserSimple
import io.mockk.clearAllMocks
import io.mockk.coEvery
import io.mockk.mockk
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class AIDashboardActivityUITest {
    @Rule
    @JvmField
    val activityRule = ActivityScenarioRule(AIDashboardActivity::class.java)
    private lateinit var decorView: View

    private val mockApiClient = mockk<ApiClient>()

    private val validIntent = Intent(ApplicationProvider.getApplicationContext(), AIDashboardActivity::class.java).apply {
        putExtra("summary_analysis", "Great job on your workout!")
        putExtra("form_score", 85)
        putExtra("stability_score", 70)
        putExtra("range_of_motion_score", 90)
        putExtra("future_advice", "Focus on maintaining proper form during squats.")
    }

    @Before
    fun setup(){
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

    }

    @Test
    fun testDisplayedData_matchesIntentExtras() {
        val scenario = ActivityScenario.launch<AIDashboardActivity>(validIntent)
        // Verify that the text views display the correct values from intent extras
        onView(withId(R.id.adviceTextView))
            .check(matches(withText("Great job on your workout!")))

        onView(withId(R.id.formScoreTextView))
            .check(matches(withText("85")))

        onView(withId(R.id.stabilityScoreTextView))
            .check(matches(withText("70")))

        onView(withId(R.id.rangeOfMotionScoreTextView))
            .check(matches(withText("90")))

        onView(withId(R.id.futureAdviceTextView))
            .check(matches(withText("Focus on maintaining proper form during squats.")))

        scenario.close()
    }

    @Test
    fun testBackButton_finishesActivity() {
        // Click the back button
        onView(withId(R.id.btnBack)).perform(click())

        // Verify that the activity is finishing
        activityRule.scenario.onActivity { activity ->
            assert(activity.isFinishing)
        }
    }

    @After
    fun tearDown(){
        Intents.release()
        clearAllMocks()
        activityRule.scenario.close()
    }
}