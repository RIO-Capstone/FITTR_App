package com.example.fittr_app

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.view.View
import androidx.core.content.ContextCompat
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.closeSoftKeyboard
import androidx.test.espresso.action.ViewActions.scrollTo
import androidx.test.espresso.action.ViewActions.typeText
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent
import androidx.test.espresso.intent.matcher.IntentMatchers.hasExtra
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withHint
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.rules.ActivityScenarioRule
import com.example.fittr_app.connections.ApiClient
import com.example.fittr_app.connections.ApiClientProvider
import com.example.fittr_app.connections.BluetoothHelper
import com.example.fittr_app.media_pipe.PoseLandmarkerHelper.Companion.MAX_RESISTANCE_VALUE
import com.example.fittr_app.media_pipe.PoseLandmarkerHelper.Companion.MIN_RESISTANCE_VALUE
import com.example.fittr_app.types.AIExercisePlan
import com.example.fittr_app.types.AISessionReply
import com.example.fittr_app.types.Exercise
import com.example.fittr_app.types.GetUserBackendResponse
import com.example.fittr_app.types.GetUsersBackendResponse
import com.example.fittr_app.types.ProductData
import com.example.fittr_app.types.User
import com.example.fittr_app.types.UserSimple
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import androidx.test.espresso.intent.Intents.intended
import androidx.test.espresso.matcher.ViewMatchers.hasChildCount
import com.example.fittr_app.media_pipe.NoCameraActivity
import io.mockk.clearAllMocks
import io.mockk.mockkStatic
import org.hamcrest.CoreMatchers.allOf

class DashboardActivityUITest {
    @Rule
    @JvmField
    val activityRule = ActivityScenarioRule(DashboardActivity::class.java)
    private lateinit var decorView: View

    private val mockApiClient = mockk<ApiClient>()

    private val mockUser = User(user_id = 1, product_id = 1, first_name = "Tester", last_name = "One", weight = 70, height = 180, email = "testerone@gmail.com")

    private val mockProduct = ProductData(service_uuid = "",
        left_resistance_uuid = "",
        right_resistance_uuid = "",
        heartbeat_uuid = "",
        exercise_initialize_uuid = "",
        stop_uuid = "",
        error = "",
        message = "")

    @Before
    fun setUp(){
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

        coEvery {
            mockApiClient.getUser(any(), any())
        } returns Result.success(userResponse)

        coEvery {
            mockApiClient.getUsers(any(), any())
        } returns Result.success(usersResponse)

        coEvery { mockApiClient.getUserExerciseSessionFeedback(any()) } returns Result.success(
            AISessionReply(feedback_message = "Test feedback", error = "")
        )

        coEvery { mockApiClient.getUserAIExercisePlan(any()) } returns Result.success(
            AIExercisePlan(error = "", feedback_message = mapOf(Exercise.SQUATS to 10, Exercise.RIGHT_BICEP_CURLS to 20, Exercise.LEFT_BICEP_CURLS to 30))
        )

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
            callback.onValueRead(args[0] as String) // Return the value that was sent
        }

        val intent = Intent(ApplicationProvider.getApplicationContext(), DashboardActivity::class.java)
        intent.putExtra("user_id", 1) // Set user_id
        ApiClientProvider.apiClient = mockApiClient

        activityRule.scenario.onActivity { activity ->
            decorView = activity.window.decorView
        }
    }

    @After
    fun tearDown(){
        clearAllMocks()
        Intents.release()
    }

    @Test
    fun checkComponentVisibilities(){
        onView(withId(R.id.dashboard_back_btn)).check(matches(isDisplayed()))
        onView(withId(R.id.dashboard_squats_frame)).check(matches(isDisplayed()))
        onView(withId(R.id.squat_exercise_weight)).check(matches(isDisplayed()))
        onView(withId(R.id.dashboard_ai_button)).perform(scrollTo()).check(matches(isDisplayed()))
    }

    @Test
    fun testDashboardBackButton(){
        activityRule.scenario.onActivity { activity ->
            activity.user = mockUser
            activity.productData = mockProduct
            activity.testMode = true
        }
        onView(withId(R.id.dashboard_back_btn)).check(matches(isDisplayed())).perform(click())
        onView(withId(R.id.viewProfiles)).check(matches(hasChildCount(3)))
    }

    @Test
    fun testNavigateToMain_whenBluetoothConnectedAndRepsSet_shouldStartActivity() {
        // Set up the environment needed for navigateToMain to work
        activityRule.scenario.onActivity { activity ->
            activity.user = mockUser
            activity.productData = mockProduct
            activity.isBluetoothConnected = true
            activity.exerciseReps[Exercise.SQUATS] = 10

        }
        onView(withId(R.id.dashboard_squats_frame)).perform(click())
        // Verify that MainActivity was started with correct intent data
        intended(hasComponent(MainActivity::class.java.name))
        intended(hasExtra("selectedExercise", Exercise.SQUATS))
        intended(hasExtra("total_session_reps", 10))
    }

    @Test
    fun testNavigateToMain_whenBluetoothNotConnected_shouldShowToast() {
        activityRule.scenario.onActivity { activity ->
            activity.user = mockUser
            activity.productData = mockProduct
            // Ensure bluetooth is not connected
            activity.isBluetoothConnected = false
            activity.exerciseReps[Exercise.SQUATS] = 10
        }
        onView(withId(R.id.dashboard_squats_frame)).perform(click())
        // Check that toast with correct message is shown
        onView(withText("Establish Bluetooth connection first"))
            .inRoot(ToastMatcher().apply { matches(isDisplayed()) })
    }

    @Test
    fun testNavigateToMain_whenRepsNotSet_shouldShowToast() {
        activityRule.scenario.onActivity { activity ->
            activity.user = mockUser
            activity.productData = mockProduct
            activity.isBluetoothConnected = true
            activity.exerciseReps[Exercise.SQUATS] = 0
        }
        onView(withId(R.id.dashboard_squats_frame)).perform(click())
        // Check that toast with correct message is shown
        onView(withText("Select more than 0 reps to start the exercise"))
            .inRoot(ToastMatcher().apply { matches(isDisplayed()) })
    }

    @Test
    fun testGetAIExercisePlan_shouldUpdateExerciseReps() {
        // Set the testing parameters
        activityRule.scenario.onActivity { activity ->
            activity.user = mockUser
            activity.productData = mockProduct
            activity.testMode = true
        }
        onView(withId(R.id.dashboard_ai_button)).perform(scrollTo(), click())
        onView(withId(R.id.squat_exercise_rep)).check(matches(withText("10")))
        onView(withId(R.id.right_bicep_curl_exercise_rep)).check(matches(withText("20")))
        onView(withId(R.id.left_bicep_curl_exercise_rep)).check(matches(withText("30")))
    }

    @Test
    fun testGetAIExercisePlan_whenApiCallFails_shouldHandleError() {
        // Mock API failure
        coEvery {
            mockApiClient.getUserAIExercisePlan(any())
        } returns Result.failure(Exception("API Error"))

        // Execute the function
        activityRule.scenario.onActivity { activity ->
            activity.user = mockUser
            activity.productData = mockProduct
            activity.testMode = true
        }

        onView(withId(R.id.dashboard_ai_button)).perform(scrollTo(),click())
        // No UI should be updated
        onView(withId(R.id.squat_exercise_rep)).check(matches(withHint("Set Reps")))
    }

    @Test
    fun testCheckBluetoothConnection_whenPermissionsGranted_andDeviceConnected_shouldReturnTrue() {
        // Mock the BluetoothManager and BluetoothAdapter
        val mockBluetoothManager = mockk<BluetoothManager>()
        val mockAdapter = mockk<BluetoothAdapter>()
        val mockDevice = mockk<BluetoothDevice>()

        // Mock the permission check
        val mockContext = mockk<Context>()

        activityRule.scenario.onActivity { activity ->
            every {
                BluetoothHelper.initialize(any(), any(), any())
            } returns Unit
            // Mock the permissions
            mockkStatic(ContextCompat::class)
            activity.checkSelfPermissionForTesting = { permission ->
                PackageManager.PERMISSION_GRANTED // always grant permission
            }

            // Mock the BluetoothManager and adapter
            mockkStatic("android.content.Context")
            activity.bluetoothManagerForTesting = mockBluetoothManager

            every { mockBluetoothManager.adapter } returns mockAdapter
            every { mockAdapter.isEnabled } returns true

            val bondedDevices = setOf(mockDevice)
            every { mockAdapter.bondedDevices } returns bondedDevices
            every { mockDevice.bondState } returns BluetoothDevice.BOND_BONDED
            val result = activity.checkBluetoothConnection()
            Assert.assertTrue(result)
        }
    }

    @Test
    fun testCheckBluetoothConnection_whenPermissionsNotGranted_shouldReturnFalse() {
        activityRule.scenario.onActivity { activity ->
            // Mock the permissions not granted
            mockkStatic(ContextCompat::class)
            every {
                ContextCompat.checkSelfPermission(any(), android.Manifest.permission.BLUETOOTH_CONNECT)
            } returns PackageManager.PERMISSION_DENIED

            // Call the function
            val result = activity.checkBluetoothConnection()
            Assert.assertFalse(result)
        }
        // Verify toast is shown
        onView(withText("Bluetooth permission not granted"))
            .inRoot(ToastMatcher().apply { matches(isDisplayed()) })
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
        squatRepTextView.check(matches(withHint("Set Reps")))
        squatRepTextView.perform(click(), typeText("10"), closeSoftKeyboard())
        squatRepTextView.check(matches(withText("10")))
    }

    @Test
    fun testInvalidSquatRepCount() {
        val squatRepTextView = onView(withId(R.id.squat_exercise_rep))
        squatRepTextView.check(matches(withHint("Set Reps")))
        squatRepTextView.perform(click(), typeText("1.0"), closeSoftKeyboard())
        squatRepTextView.check(matches(withText("10")))
        onView(withText("Only whole numbers are allowed.")).inRoot(ToastMatcher().apply { matches(isDisplayed()) })
    }

    @Test
    fun testZeroSquatRepCount() {
        val squatRepTextView = onView(withId(R.id.squat_exercise_rep))
        squatRepTextView.check(matches(withHint("Set Reps")))
        squatRepTextView.perform(click(), typeText("0"), closeSoftKeyboard())
        Thread.sleep(500)
        onView(withText("Value must be greater than 0."))
            .inRoot(ToastMatcher().apply { matches(isDisplayed()) })
    }

    @Test
    fun changeSquatWeightCountToAboveTheMaximumValue(){
        val squatWeightTextView = onView(withId(R.id.squat_exercise_weight))
        squatWeightTextView.check(matches(withHint("Weight")))
        squatWeightTextView.perform(click(), typeText((MAX_RESISTANCE_VALUE+1.0f).toInt().toString()), closeSoftKeyboard())
        Thread.sleep(500)
        onView(withText("Please select from valid values between $MIN_RESISTANCE_VALUE and $MAX_RESISTANCE_VALUE"))
            .inRoot(ToastMatcher().apply { matches(isDisplayed()) })
    }

    @Test
    fun changeSquatWeightCountToBelowTheMinimumValue() {
        val squatWeightTextView = onView(withId(R.id.squat_exercise_weight))
        squatWeightTextView.check(matches(withHint("Weight")))
        squatWeightTextView.perform(click(), typeText((MIN_RESISTANCE_VALUE-1.0f).toInt().toString()), closeSoftKeyboard())
        Thread.sleep(500)
        onView(withText("Please select from valid values between $MIN_RESISTANCE_VALUE and $MAX_RESISTANCE_VALUE"))
            .inRoot(ToastMatcher().apply { matches(isDisplayed()) })
    }

    @Test
    fun validChangeSquatWeightCount() {
        val squatWeightTextView = onView(withId(R.id.squat_exercise_weight))
        squatWeightTextView.check(matches(withHint("Weight")))
        squatWeightTextView.perform(click(), typeText(MAX_RESISTANCE_VALUE.toInt().toString()), closeSoftKeyboard())
        Thread.sleep(500)
        squatWeightTextView.check(matches(withText(MAX_RESISTANCE_VALUE.toInt().toString())))
    }

    @Test
    fun navigateToAIDashboard(){
        activityRule.scenario.onActivity { activity ->
            activity.testMode = true
            activity.user = mockUser
            activity.productData = mockProduct
            activity.summary_analysis = "Test summary"
            activity.future_advice = "Test advice"
            activity.form_score = 20
            activity.stability_score = 30
            activity.range_of_motion_score = 10
        }
        onView(withId(R.id.ai_layout)).check(matches(isDisplayed())).perform(click())
        intended(allOf(
            hasComponent(AIDashboardActivity::class.java.name),
            hasExtra("summary_analysis", "Test summary"),
            hasExtra("range_of_motion_score", 10)
        ))
    }

    @Test
    fun navigateToNoCameraActivity(){
        activityRule.scenario.onActivity { activity ->
            activity.testMode = true
            activity.user = mockUser
            activity.productData = mockProduct
            activity.isBluetoothConnected = true
        }
        onView(withId(R.id.dashboard_free_flow_exercise))
            .perform(scrollTo()).check(matches(isDisplayed())).perform(click())
        intended(allOf(
            hasComponent(NoCameraActivity::class.java.name),
            hasExtra("user_id",1)
        ))
    }

}