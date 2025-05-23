package com.example.fittr_app.connections

import android.util.Log
import com.example.fittr_app.types.AIExercisePlan
import java.util.concurrent.TimeUnit

import com.example.fittr_app.types.AIReply
import com.example.fittr_app.types.AISessionReply
import com.example.fittr_app.types.Feedback
import com.example.fittr_app.types.GetUserBackendResponse
import com.example.fittr_app.types.GetUsersBackendResponse
import com.example.fittr_app.types.LoginUserBackendResponse
import com.example.fittr_app.types.ProductData
import com.example.fittr_app.types.RegisterUserBackendResponse
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException

object ApiClient {

    private val client = OkHttpClient.Builder()
        .connectTimeout(120, TimeUnit.SECONDS)  // Increase connection timeout
        .readTimeout(120, TimeUnit.SECONDS)     // Increase read timeout
        .writeTimeout(120, TimeUnit.SECONDS)    // Increase write timeout
        .build()

    private val moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()

    var BASE_URL = "http://172.20.10.9:8000/"
    // Get user
    suspend fun getUser(endpoint: ApiPaths,data:Any?): Result<GetUserBackendResponse>{
        return makeApiRequest<GetUserBackendResponse>(endpoint,data)
    }

    // Login User
    suspend fun loginUser(endpoint: ApiPaths, data: Any): Result<LoginUserBackendResponse> {
        return makeApiRequest<LoginUserBackendResponse>(endpoint, data)
    }

    // Register User
    suspend fun registerUser(endpoint: ApiPaths, data: Any): Result<RegisterUserBackendResponse> {
        return makeApiRequest<RegisterUserBackendResponse>(endpoint, data)
    }

    // Get product data
    suspend fun getProductData(endpoint: ApiPaths, data: Any?): Result<ProductData> {
        return makeApiRequest<ProductData>(endpoint,data)
    }

    suspend fun getUserAIReply(userId: Int): Result<AIReply> {
        return makeApiRequest<AIReply>(ApiPaths.GetUserAIReply(userId))
    }

    suspend fun getUserExerciseSessionFeedback(data: Any): Result<AISessionReply>{
        return makeApiRequest<AISessionReply>(ApiPaths.ExerciseSessionFeedback,data)
    }

    suspend fun getUserAIExercisePlan(userId: Int) : Result<AIExercisePlan>{
        return makeApiRequest<AIExercisePlan>(ApiPaths.GetUserExercisePlan(userId))
    }

    // Get all Users using a product
    suspend fun getUsers(endpoint: ApiPaths.GetUsers, data: Any?): Result<GetUsersBackendResponse> {
        return makeApiRequest<GetUsersBackendResponse>(endpoint, data)
    }

    private suspend inline fun <reified T> makeApiRequest(
        endpoint: ApiPaths,
        data: Any? = null
    ): Result<T> {
        return withContext(Dispatchers.IO) {
            try {
                val requestBuilder = Request.Builder()
                    .url("$BASE_URL${endpoint.path}")

                // Determine HTTP method based on endpoint.method
                when (endpoint.method) {
                    "POST" -> {
                        val jsonData = data?.let { moshi.adapter(Any::class.java).toJson(it) }
                        val requestBody = jsonData?.toRequestBody("application/json".toMediaType())
                        requestBuilder.post(requestBody ?: throw IllegalArgumentException("POST request requires a body"))
                    }
                    "GET" -> requestBuilder.get()
                    "PUT" -> {
                        val jsonData = data?.let { moshi.adapter(Any::class.java).toJson(it) }
                        val requestBody = jsonData?.toRequestBody("application/json".toMediaType())
                        requestBuilder.put(requestBody ?: throw IllegalArgumentException("PUT request requires a body"))
                    }
                    "DELETE" -> requestBuilder.delete()
                    else -> throw IllegalArgumentException("Unsupported HTTP method: ${endpoint.method}")
                }

                val request = requestBuilder.build()

                // Execute the HTTP request
                val response = client.newCall(request).execute()
                Log.d("ApiClient", "Response code: ${response.code}, feedback_message: ${response.message}")

                if (response.isSuccessful) {
                    response.body?.let {
                        val responseBody = it.string()
                        Log.d("ApiClient", "Response body: $responseBody")

                        val jsonAdapter = if (T::class.java == List::class.java) {
                            val listType = Types.newParameterizedType(List::class.java, T::class.java)
                            moshi.adapter<T>(listType)
                        } else {
                            moshi.adapter(T::class.java)
                        }
                        val apiResponse = jsonAdapter.fromJson(responseBody)

                        if (apiResponse != null) {
                            return@withContext Result.success(apiResponse)
                        } else {
                            return@withContext Result.failure(IOException("Failed to parse response"))
                        }
                    } ?: return@withContext Result.failure(IOException("Empty response body"))
                } else {
                    return@withContext Result.failure(IOException("Unexpected response code: ${response.code}"))
                }
            } catch (e: Exception) {
                Log.e("ApiClient", "Error during API request")
                return@withContext Result.failure(e)
            }
        }
    }
}

object ApiClientProvider {
    var apiClient: ApiClient = ApiClient
}
