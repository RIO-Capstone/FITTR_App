package com.example.fittr_app

import android.content.Intent
import android.os.Build
import android.view.View
import android.widget.ImageButton
import android.widget.TextView
import androidx.test.core.app.ApplicationProvider
import com.example.fittr_app.connections.ApiClient
import com.example.fittr_app.connections.ApiClientProvider
import com.example.fittr_app.connections.ApiPaths
import com.example.fittr_app.databinding.ActivityDashboardBinding
import com.example.fittr_app.types.AIExercisePlan
import com.example.fittr_app.types.AIReply
import com.example.fittr_app.types.Feedback
import com.example.fittr_app.types.GetUserBackendResponse
import com.example.fittr_app.types.User
import com.example.fittr_app.ui.auth.AuthActivity
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertNotNull
import junit.framework.TestCase.assertNull
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import org.mockito.MockitoAnnotations
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config
import org.robolectric.shadows.ShadowToast
import java.lang.reflect.Field

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [30])
class DashboardActivityUnitTest {

    private lateinit var activity: DashboardActivity
    private lateinit var binding: ActivityDashboardBinding
    @Mock
    private val mockApiClient = mock<ApiClient>()

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        val intent = Intent(ApplicationProvider.getApplicationContext(), DashboardActivity::class.java)
        intent.putExtra("user_id", 1) // Provide a dummy user ID for testing
        ApiClientProvider.apiClient = mockApiClient // using Dependency Injection
        // initialize user object in DashboardActivity
        runBlocking {
            `when`(mockApiClient.getUser(endpoint = ApiPaths.GetUser(userId = 1),data = null)).thenReturn(
                Result.success(
                    GetUserBackendResponse(
                        user = User(
                            user_id = 1,
                            product_id = 1,
                            first_name = "Tester",
                            last_name = "One",
                            weight = 70,
                            height = 180,
                            email = "testerone@gmail.com"
                        )
                    )
                )
            )
            `when`(mockApiClient.getUserAIReply(1)).thenReturn(Result.success(
                AIReply(
                    user_id = 1,
                    feedback_message = Feedback(
                        summary_analysis = "Test Analysis",
                        summary_advice = "Test Advice",
                        future_advice = "Keep improving",
                        range_of_motion_score = 80,
                        form_score = 90,
                        stability_score = 85
                    )
                )
            ))
        }
        activity = Robolectric.buildActivity(DashboardActivity::class.java, intent).create().start().resume().get()
        binding = ActivityDashboardBinding.inflate(activity.layoutInflater)
    }


    @Test
    fun `Check whether the title is displayed`() {
        val titleTextView = activity.findViewById<TextView>(R.id.dashboard_user_name_text)
        assertNotNull(titleTextView)
    }

    @Test
    fun `Check back button navigation`() {
        val backButton = activity.findViewById<View>(R.id.dashboard_back_btn)
        backButton.performClick()
        val expectedIntent = Intent(activity, AuthActivity::class.java)
        val actualIntent = shadowOf(activity).nextStartedActivity
        assertNotNull(actualIntent)
        assertEquals(expectedIntent.component, actualIntent.component)
    }

    @Test
    fun `Check AI layout navigation - Valid Response`() {
        val aiLayout = activity.findViewById<View>(R.id.ai_layout)
        assertNotNull(aiLayout)

        aiLayout.performClick()

        val actualIntent = shadowOf(activity).nextStartedActivity
        assertNotNull(actualIntent)
        assertEquals(AIDashboardActivity::class.java.name, actualIntent.component?.className)
    }

    @Test
    fun `Check bluetooth button click`() {
        val bluetoothButton = activity.findViewById<ImageButton>(R.id.dashboard_bluetooth_status_button)
        bluetoothButton.performClick()
    }

    @Test
    fun `Check AI exercise plan button click and loading`() {
        val aiExercisePlanButton = activity.findViewById<View>(R.id.dashboard_ai_button)
        val loadingComponent = activity.findViewById<View>(R.id.dashboard_loading_progress)
        assertEquals(View.VISIBLE, aiExercisePlanButton.visibility)
        assertEquals(View.GONE, loadingComponent.visibility)
        aiExercisePlanButton.performClick()
        assertEquals(View.VISIBLE, loadingComponent.visibility)
    }

    @Test
    fun `Check error toast on bluetooth error`() {
        activity.onError("Test Error")
        assertEquals("Unstable bluetooth connection", ShadowToast.getTextOfLatestToast())
    }

    @Test
    fun `Check bluetooth connection change`() {
        val bluetoothButton = activity.findViewById<ImageButton>(R.id.dashboard_bluetooth_status_button)
        activity.onBluetoothConnectionChange(true)
        assertEquals(R.drawable.bluetooth_green, shadowOf(bluetoothButton.drawable).createdFromResId)
        activity.onBluetoothConnectionChange(false)
        assertEquals(R.drawable.bluetooth_red, shadowOf(bluetoothButton.drawable).createdFromResId)
    }
}