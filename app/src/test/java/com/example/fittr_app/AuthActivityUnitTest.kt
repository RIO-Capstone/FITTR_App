package com.example.fittr_app

import android.content.Intent
import android.view.View
import android.widget.EditText
import androidx.core.view.isVisible
import androidx.test.core.app.ApplicationProvider
import com.example.fittr_app.connections.ApiClient
import com.example.fittr_app.connections.ApiClientProvider
import com.example.fittr_app.connections.ApiPaths
import com.example.fittr_app.databinding.ActivityAuthBinding
import com.example.fittr_app.types.LoginUserBackendResponse
import com.example.fittr_app.types.User
import com.example.fittr_app.ui.auth.AuthActivity
import com.example.fittr_app.ui.registration.RegistrationActivity
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertNotNull
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.mockito.MockitoAnnotations
import org.mockito.junit.MockitoJUnitRunner
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config
import org.robolectric.shadow.api.Shadow
import org.robolectric.shadows.ShadowLooper
import org.robolectric.shadows.ShadowToast
import java.util.concurrent.TimeUnit

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [30])
class AuthActivityUnitTest {
    private lateinit var activity: AuthActivity
    private lateinit var binding: ActivityAuthBinding
    @Mock
    private val mockApiClient = mock<ApiClient>()

    @Before
    fun setUp() {
        MockitoAnnotations.openMocks(this)
        val intent = Intent(ApplicationProvider.getApplicationContext(), AuthActivity::class.java)
        ApiClientProvider.apiClient = mockApiClient

        activity = Robolectric.buildActivity(AuthActivity::class.java, intent).create().start().resume().get()
        binding = ActivityAuthBinding.inflate(activity.layoutInflater)
    }

    @After
    fun tearDown() {
        activity.finish()
    }

    @Test
    fun testUsernameFieldExists() {
        val field = activity.findViewById<EditText>(R.id.auth_id_field)
        assertNotNull(field)
        field.setText("testerone@gmail.com")
        assertTrue(field.isVisible)
    }

    @Test
    fun testRegistrationButtonClick() {

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


    @Test
    fun testLoginButtonClick_ValidCredentials() {
        runBlocking {
            `when`(mockApiClient.loginUser(ApiPaths.LoginUser, mapOf("email" to "testerone@gmail.com", "password" to "123")))
                .thenReturn(Result.success(
                    LoginUserBackendResponse(user = User(
                    user_id = 1,
                    product_id = 1,
                    first_name = "Tester",
                    last_name = "One",
                    weight = 70,
                    height = 180,
                    email = "testerone@gmail.com"
                ), message = "login successful")
                ))
        }
        val usernameField = activity.findViewById<EditText>(R.id.auth_id_field)
        val passwordField = activity.findViewById<EditText>(R.id.auth_password_field)
        val loginButton = activity.findViewById<View>(R.id.auth_login_button)
        // check that all fields exist on the page
        assertNotNull(usernameField)
        assertNotNull(passwordField)
        assertNotNull(loginButton)
        usernameField.setText("testerone@gmail.com")
        passwordField.setText("123")

        loginButton.performClick()
        ShadowLooper.idleMainLooper()
        runBlocking {
            verify(mockApiClient).loginUser(ApiPaths.LoginUser, mapOf("email" to "testerone@gmail.com", "password" to "123"))
        }
        // after clicking check that the dashboard title exists on the screen
        val expectedIntent = Intent(activity, DashboardActivity::class.java)
        expectedIntent.putExtra("user_id",1)
        val actualIntent = shadowOf(activity).nextStartedActivity
        assertNotNull(actualIntent)
        assertEquals(expectedIntent.component, actualIntent.component)
    }

    @Test
    fun testLoginButtonClick_InvalidCredentials() {
        runBlocking {
            `when`(mockApiClient.loginUser(ApiPaths.LoginUser, mapOf("email" to "testerone@gmail.com", "password" to "123")))
                .thenReturn(Result.failure(Exception("Login Failed. Check your credentials.")))
        }
        val loginButton = activity.findViewById<View>(R.id.auth_login_button)
        assertNotNull(loginButton)
        loginButton.performClick()
        ShadowLooper.idleMainLooper()
        println(ShadowToast.getTextOfLatestToast())
        assertEquals("Login Failed. Check your credentials.", ShadowToast.getTextOfLatestToast())
    }
}