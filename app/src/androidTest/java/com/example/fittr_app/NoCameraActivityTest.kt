package com.example.fittr_app

import android.content.Intent
import android.widget.TextView
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.Intents.intended
import androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent
import androidx.test.espresso.intent.matcher.IntentMatchers.hasExtra
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.fittr_app.connections.ApiClient
import com.example.fittr_app.connections.ApiClientProvider
import com.example.fittr_app.connections.BluetoothHelper
import com.example.fittr_app.media_pipe.NoCameraActivity
import com.example.fittr_app.media_pipe.PoseLandmarkerHelper.Companion.MIN_RESISTANCE_VALUE
import com.example.fittr_app.types.AIReply
import com.example.fittr_app.types.Feedback
import com.example.fittr_app.types.GetUserBackendResponse
import com.example.fittr_app.types.User
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.unmockkObject
import io.mockk.verify
import org.hamcrest.CoreMatchers.allOf
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class NoCameraActivityTest {

    private var mockApiClient : ApiClient = mockk()

    private val validIntent = Intent(ApplicationProvider.getApplicationContext(), NoCameraActivity::class.java).apply {
        putExtra("user_id", 1)
        putExtra("leftResistanceUUID", "leftUUID")
        putExtra("rightResistanceUUID", "rightUUID")
    }

    @Before
    fun setup() {
        Intents.init()
        val userResponse = GetUserBackendResponse(
            User(1, "John", "Doe", 80, 180, "johndoe@gmail.com", 1)
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
            mockApiClient.getUserAIReply(any())
        } returns Result.success(mockAIFeedback)

        mockkObject(BluetoothHelper)

        // Simulate successful write operations
        every {
            BluetoothHelper.queueWriteOperation(
                any(),
                any(),
                any()
            )
        } answers {
            // Call the callback's onValueRead method to simulate successful operation
            val callback = arg<BluetoothReadCallback>(2)
            callback.onValueRead(args[0] as String) // Return the resistance value that was sent
        }

        ApiClientProvider.apiClient = mockApiClient

    }

    @Test
    fun testActivityLaunch() {
        // Launch activity with this intent
        val scenario = ActivityScenario.launch<NoCameraActivity>(validIntent)
        // Now test UI components
        onView(withId(R.id.no_camera_resistance_value)).check(matches(isDisplayed()))
        scenario.close()
    }

    @Test
    fun testResistanceValueUpdatedOnMinusClick() {
        val scenario = ActivityScenario.launch<NoCameraActivity>(validIntent)
        // Set the resistance value through the activity
        scenario.onActivity { activity ->
            activity.findViewById<TextView>(R.id.no_camera_resistance_value).text = (MIN_RESISTANCE_VALUE+1.0f).toString()
        }
        onView(withId(R.id.no_camera_resistance_minus)).perform(click())
        // Ensure the resistance value decreases
        onView(withId(R.id.no_camera_resistance_value)).check(matches(withText((MIN_RESISTANCE_VALUE).toString())))

        scenario.close()
    }

    @Test
    fun testResistanceDoesNotGoBelowMinimum() {
        val scenario = ActivityScenario.launch<NoCameraActivity>(validIntent)
        onView(withId(R.id.no_camera_resistance_value)).check(matches(withText(MIN_RESISTANCE_VALUE.toString())))
        onView(withId(R.id.no_camera_resistance_minus)).perform(click())
        // Ensure the resistance value does not go below the minimum
        onView(withId(R.id.no_camera_resistance_value)).check(matches(withText(MIN_RESISTANCE_VALUE.toString())))
        Thread.sleep(500)
        // Verify that a Toast is shown
        onView(withText("Cannot set resistance value lower than the minimum"))
            .inRoot(ToastMatcher().apply { matches(isDisplayed()) })
        scenario.close()
    }

    @Test
    fun testStopButtonNavigatesToDashboardActivity() {
        val scenario = ActivityScenario.launch<NoCameraActivity>(validIntent)

        // Simulate clicking the stop button
        onView(withId(R.id.no_camera_exercise_stop_button)).perform(click())

        // Verify that the DashboardActivity is launched
        intended(allOf(
            hasComponent(DashboardActivity::class.java.name),
            hasExtra("user_id", 1)
        ))
        scenario.close()
    }

    @Test
    fun testBluetoothOperationsCalled() {
        val rightUUID = "rightUUID"
        val leftUUID = "leftUUID"
        val newResistance = 10.0f

        // Simulate resistance update
        val scenario = ActivityScenario.launch<NoCameraActivity>(validIntent)
        scenario.onActivity { activity ->
            activity.handleResistanceUpdate(newResistance, rightUUID, leftUUID)
        }

        // Verify BluetoothHelper was called with correct parameters
        verify {
            BluetoothHelper.queueWriteOperation(newResistance.toString(), rightUUID, any())
            BluetoothHelper.queueWriteOperation(newResistance.toString(), leftUUID, any())
        }
    }

    @After
    fun tearDown() {
        unmockkObject(BluetoothHelper)
        Intents.release()
    }
}
