package com.example.fittr_app.connections

import com.example.fittr_app.types.AIExercisePlan
import com.example.fittr_app.types.AIReply
import com.example.fittr_app.types.AISessionReply
import com.example.fittr_app.types.Exercise
import com.example.fittr_app.types.Feedback
import com.example.fittr_app.types.GetUserBackendResponse
import com.example.fittr_app.types.GetUsersBackendResponse
import com.example.fittr_app.types.ProductData
import com.example.fittr_app.types.RegisterUserBackendResponse
import com.example.fittr_app.types.User
import com.example.fittr_app.types.UserSimple
import com.squareup.moshi.Moshi
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
import org.mockito.junit.MockitoJUnitRunner

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
        val result = apiClient.getUser(ApiPaths.GetUser(userId = 1), null)
        assertTrue(result.isSuccess)
        assertEquals(expectedResponse, result.getOrNull())
    }

    @Test
    fun `loginUser should return error when API responds with 401`() = runBlocking {
        // Given
        mockWebServer.enqueue(MockResponse().setResponseCode(401).setBody("{\"error\": \"Unauthorized\"}"))
        val result = apiClient.loginUser(ApiPaths.LoginUser, mapOf("username" to "test", "password" to "wrong"))
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
        assertEquals("Empty response body", result.exceptionOrNull()?.message)
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
    fun `getProductData returns a valid response`() = runBlocking {
        val testProduct = ProductData(
            service_uuid = "",
            left_resistance_uuid = "",
            right_resistance_uuid = "",
            stop_uuid = "",
            exercise_initialize_uuid = "",
            message = "",
            error = "",
            heartbeat_uuid = "")
        val validResponse = moshi.adapter(ProductData::class.java).toJson(testProduct)
        mockWebServer.enqueue(MockResponse().setResponseCode(200).setBody(validResponse))
        val result = apiClient.getProductData(ApiPaths.GetProduct(1),null)
        assertTrue(result.isSuccess)
        assertEquals(testProduct, result.getOrNull())
    }

    @Test
    fun `getAIReply returns a valid response`() = runBlocking{
        val testOutput = AIReply(user_id = 1, feedback_message = Feedback(
            summary_analysis = "Test Analysis",
            summary_advice = "Test Advice",
            future_advice = "Keep improving",
            range_of_motion_score = 80,
            form_score = 90,
            stability_score = 25
        ), error = "")
        val validResponse = moshi.adapter(AIReply::class.java).toJson(testOutput)
        mockWebServer.enqueue(MockResponse().setResponseCode(200).setBody(validResponse))
        val result = apiClient.getUserAIReply(1)
        assertTrue(result.isSuccess)
        assertEquals(result.getOrNull(),testOutput)
    }

    @Test
    fun `registerUser is successful`()= runBlocking{
        val testUser = mapOf(
            "first_name" to "John",
            "last_name" to "Doe",
            "email" to "john.doe@example.com",
            "password" to "password",
            "weight" to 75,
            "height" to 180,
            "phone_number" to "12345678",
            "gender" to "Male",
            "date_of_birth" to "12/12/2012",
            "product_id" to 1,
            "fitness_goal" to "Strength Seeker",
        )
        val validResponse = moshi.adapter(RegisterUserBackendResponse::class.java).toJson(
            RegisterUserBackendResponse(user_id = 1, message = "Success", error = "")
        )
        mockWebServer.enqueue(MockResponse().setResponseCode(200).setBody(validResponse))
        val result = apiClient.registerUser(ApiPaths.RegisterUser,testUser)
        assertTrue(result.isSuccess)
        assertEquals(result.getOrNull()?.user_id,1)
        assertEquals(result.getOrNull()?.message,"Success")
    }

    @Test
    fun `getUserExerciseSessionFeedback should return successfully`() = runBlocking {
        val testFeedback = AISessionReply(feedback_message = "TestFeedback", error = "")
        val validResponse = moshi.adapter(AISessionReply::class.java).toJson(testFeedback)
        mockWebServer.enqueue(MockResponse().setResponseCode(200).setBody(validResponse))
        val result = apiClient.getUserExerciseSessionFeedback(data = mapOf(
            "exercise_type" to "SQUATS",
            "rep_count" to 10,
            "created_at" to "",
            "duration" to "4.5",
            "errors" to 3,
            "user_id" to 1,
        ))
        assertTrue(result.isSuccess)
        assertEquals(result.getOrNull(),testFeedback)
    }

    @Test
    fun `getUserAIExercisePlan should return successfully`() = runBlocking {
        val testFeedback = AIExercisePlan(feedback_message = mapOf(Exercise.SQUATS to 2), error = "")
        val validResponse = moshi.adapter(AIExercisePlan::class.java).toJson(testFeedback)
        mockWebServer.enqueue(MockResponse().setResponseCode(200).setBody(validResponse))
        val result = apiClient.getUserAIExercisePlan(1)
        assertTrue(result.isSuccess)
        assertEquals(result.getOrNull(),testFeedback)
    }

    @Test
    fun `getUsers should return successfully`() = runBlocking {
        val testUsersLists = GetUsersBackendResponse(
            users = listOf(UserSimple(id = 1, full_name = "John Doe"),
                UserSimple(id = 2, full_name = "Jane Doe")
            )
        )
        val validResponse = moshi.adapter(GetUsersBackendResponse::class.java).toJson(testUsersLists)
        mockWebServer.enqueue(MockResponse().setResponseCode(200).setBody(validResponse))
        val result = apiClient.getUsers(ApiPaths.GetUsers(1),null)
        assertTrue(result.isSuccess)
        assertEquals(result.getOrNull(),testUsersLists)
    }

    @Test
    fun `makeApiRequest handles malformed JSON response`() = runBlocking {
        // Given
        mockWebServer.enqueue(MockResponse().setResponseCode(200).setBody("{ malformed json }"))

        // When
        val result = apiClient.getUser(ApiPaths.GetUser(userId = 1), null)

        // Then
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is IOException)
        assertTrue(result.exceptionOrNull()?.message?.contains("Failed to parse") == true ||
                result.exceptionOrNull()?.message?.contains("malformed") == true)
    }


    @Test
    fun `makeApiRequest handles timeout correctly`() = runBlocking {
        mockWebServer.enqueue(MockResponse()
            .setSocketPolicy(SocketPolicy.NO_RESPONSE))

        val result = apiClient.getProductData(ApiPaths.GetProduct(productId = 1), null)

        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is IOException)
    }

    @Test
    fun `makeApiRequest handles internal server error code`() = runBlocking {
        mockWebServer.enqueue(MockResponse().setResponseCode(500).setBody("{\"error\": \"Internal Server Error\"}"))

        val result = apiClient.getUser(ApiPaths.GetUser(userId = 1), null)

        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull()?.message?.contains("500") == true)
    }

    @Test
    fun `apiClient uses proper HTTP methods for different requests`() = runBlocking {
        // Test POST method with registerUser
        mockWebServer.enqueue(MockResponse().setResponseCode(200)
            .setBody(moshi.adapter(RegisterUserBackendResponse::class.java)
                .toJson(RegisterUserBackendResponse(user_id = 1, message = "Success", error = ""))))

        apiClient.registerUser(ApiPaths.RegisterUser, mapOf("name" to "test"))
        var request = mockWebServer.takeRequest()
        assertEquals("POST", request.method)

        // Test GET method with getUser
        mockWebServer.enqueue(MockResponse().setResponseCode(200)
            .setBody(moshi.adapter(GetUserBackendResponse::class.java)
                .toJson(GetUserBackendResponse(User(1, "test", "user", 70, 180, "test@example.com", 1)))))

        apiClient.getUser(ApiPaths.GetUser(1), null)
        request = mockWebServer.takeRequest()
        assertEquals("GET", request.method)
    }

    @Test
    fun `apiClient handles different HTTP status codes`() = runBlocking {
        mockWebServer.enqueue(MockResponse().setResponseCode(400).setBody("{\"error\": \"Bad Request\"}"))
        val result = apiClient.loginUser(ApiPaths.LoginUser, mapOf("username" to ""))
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull()?.message?.contains("400") == true)

        mockWebServer.enqueue(MockResponse().setResponseCode(404).setBody("{\"error\": \"Not Found\"}"))
        val result2 = apiClient.getUser(ApiPaths.GetUser(userId = 1), null)
        assertTrue(result2.isFailure)
        assertTrue(result2.exceptionOrNull()?.message?.contains("404") == true)
    }

    @Test
    fun `apiClient handles connection failure`() = runBlocking {
        mockWebServer.shutdown() // Shut down the server to cause a connection failure

        val result = apiClient.getUser(ApiPaths.GetUser(1), null)

        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is IOException)

        // Restart the server for subsequent tests
        mockWebServer = MockWebServer()
        mockWebServer.start()
        apiClient.BASE_URL = mockWebServer.url("/").toString()
    }

    @Test
    fun `apiClient handles empty but valid JSON responses`() = runBlocking {
        // Empty array
        mockWebServer.enqueue(MockResponse().setResponseCode(200).setBody("[]"))

        val result = apiClient.getUsers(ApiPaths.GetUsers(1), null)

        assertTrue(result.isFailure)

        // Empty object
        mockWebServer.enqueue(MockResponse().setResponseCode(200).setBody("{}"))

        val productResult = apiClient.getProductData(ApiPaths.GetProduct(1), null)

        assertTrue(productResult.isFailure) // Empty object can't be parsed to the expected output
    }

    @Test
    fun `apiClient properly transmits request bodies`() = runBlocking {
        // Prepare response
        mockWebServer.enqueue(MockResponse().setResponseCode(200)
            .setBody(moshi.adapter(RegisterUserBackendResponse::class.java)
                .toJson(RegisterUserBackendResponse(user_id = 1, message = "Success", error = ""))))

        val userData = mapOf(
            "first_name" to "John",
            "last_name" to "Doe",
            "email" to "john@example.com",
            "password" to "secret",
            "weight" to 75,
            "height" to 180
        )

        apiClient.registerUser(ApiPaths.RegisterUser, userData)
        val request = mockWebServer.takeRequest()
        val requestBody = request.body.readUtf8()

        // Check that all fields are present in the JSON
        userData.forEach { (key, value) ->
            assertTrue(requestBody.contains("\"$key\""))
            when (value) {
                is String -> assertTrue(requestBody.contains("\"$value\""))
                is Number -> assertTrue(requestBody.contains("$value"))
                else -> assertTrue(requestBody.contains(value.toString()))
            }
        }
    }

    @Test
    fun `put request should return success on valid response`() = runBlocking {
        // Given
        val updateData = mapOf("field" to "new value")
        val expectedResponse = mapOf("status" to "success")
        val responseBody = moshi.adapter(Map::class.java).toJson(expectedResponse)

        mockWebServer.enqueue(MockResponse().setBody(responseBody).setResponseCode(200))
        val result: Result<Map<String, String>> = apiClient.makeApiRequest(
            ApiPaths.TestPut(userId = 1),
            updateData
        )
        assertTrue(result.isSuccess)
        assertEquals(expectedResponse, result.getOrNull())
    }

    @Test
    fun `delete request should return success on valid response`() = runBlocking {
        mockWebServer.enqueue(MockResponse().setResponseCode(200))
        val result: Result<Unit> = apiClient.makeApiRequest(ApiPaths.TestDelete(userId = 1))
        assertTrue(result.isSuccess)
    }

    @Test
    fun `should return failure on empty response body`() = runBlocking {
        // Enqueue a null response
        mockWebServer.enqueue(MockResponse().setResponseCode(200))
        val result: Result<Map<String, String>> = apiClient.makeApiRequest(
            ApiPaths.GetUser(userId = 1),
            null
        )
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is IOException)
        assertEquals("Empty response body", result.exceptionOrNull()?.message)
    }

    @Test
    fun `should return failure on unexpected response code`() = runBlocking {
        // Given
        mockWebServer.enqueue(MockResponse().setResponseCode(404))

        // When
        val result: Result<Map<String, String>> = apiClient.makeApiRequest(
            ApiPaths.GetUser(userId = 1),
            null
        )

        // Then
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is IOException)
        assertEquals("Unexpected response code: 404", result.exceptionOrNull()?.message)
    }

}