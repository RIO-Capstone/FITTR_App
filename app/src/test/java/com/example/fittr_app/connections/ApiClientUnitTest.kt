package com.example.fittr_app.connections

import com.example.fittr_app.types.GetUserBackendResponse
import com.example.fittr_app.types.ProductData
import com.example.fittr_app.types.User
import com.squareup.moshi.Moshi
import com.squareup.moshi.adapter
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import kotlinx.coroutines.runBlocking
import okhttp3.mockwebserver.SocketPolicy
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import java.io.IOException
import org.junit.runner.RunWith
import org.mockito.MockitoAnnotations
import org.mockito.junit.MockitoJUnitRunner
import org.robolectric.annotation.Config

@RunWith(MockitoJUnitRunner::class)
class ApiClientUnitTest {

    private lateinit var mockWebServer: MockWebServer
    private var apiClient = ApiClient
    private val moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()

    @Before
    fun setUp() {
        mockWebServer = MockWebServer()
        mockWebServer.start()
        apiClient.BASE_URL = mockWebServer.url("/").toString()
    }

    @After
    fun tearDown() {
        mockWebServer.shutdown()
    }

    @Test
    fun `getUser should return valid user data on success`() = runBlocking {
        // Given
        val user = User(
            1, "tester", "one",
            weight = 10,
            height = 10,
            email = "testerone@gmail.com",
            product_id = 1
        )
        val expectedResponse = GetUserBackendResponse(user)
        val responseBody = moshi.adapter(GetUserBackendResponse::class.java).toJson(expectedResponse)

        mockWebServer.enqueue(MockResponse().setBody(responseBody).setResponseCode(200))

        // When
        val result = apiClient.getUser(ApiPaths.GetUser(userId = 1), null)

        // Then
        assertTrue(result.isSuccess)
        assertEquals(expectedResponse, result.getOrNull())
    }

    @Test
    fun `loginUser should return error when API responds with 401`() = runBlocking {
        // Given
        mockWebServer.enqueue(MockResponse().setResponseCode(401).setBody("{\"error\": \"Unauthorized\"}"))

        // When
        val result = apiClient.loginUser(ApiPaths.LoginUser, mapOf("username" to "test", "password" to "wrong"))

        // Then
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull()?.message?.contains("Unexpected response code: 401") == true)
    }

    @Test
    fun `getProductData should return empty response error`() = runBlocking {
        // Given
        mockWebServer.enqueue(MockResponse().setResponseCode(200).setBody(""))

        // When
        val result = apiClient.getProductData(ApiPaths.GetProduct(productId = 1), null)

        // Then
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is IOException)
        assertEquals("End of input", result.exceptionOrNull()?.message)
    }

    @Test
    fun `network failure should throw IOException`() = runBlocking {
        // Given
        mockWebServer.enqueue(MockResponse().setSocketPolicy(SocketPolicy.DISCONNECT_DURING_RESPONSE_BODY))

        // When
        val result = apiClient.getUser(ApiPaths.GetUser(userId = 1), null)

        // Then
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is IOException)
    }

    @Test
    fun `get product returns a valid response`() = runBlocking {
        val testProduct = ProductData(
            service_uuid = "",
            left_resistance_uuid = "",
            right_resistance_uuid = "",
            stop_uuid = "",
            exercise_initialize_uuid = "",
            message = "",
            error = "")
        val validResponse = moshi.adapter(ProductData::class.java).toJson(testProduct)
        mockWebServer.enqueue(MockResponse().setResponseCode(200).setBody(validResponse))
        val result = apiClient.getProductData(ApiPaths.GetProduct(1),null)
        assertTrue(result.isSuccess)
        assertEquals(testProduct, result.getOrNull())
    }
}