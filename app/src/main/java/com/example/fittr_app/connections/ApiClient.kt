package com.example.fittr_app.connections

import android.util.Log
import com.squareup.moshi.Moshi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException

class ApiClient {
    private val client = OkHttpClient()

    private val moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
    private val jsonAdapter = moshi.adapter(RegisterUserBackendResponse::class.java)

    companion object {
        private const val BASE_URL = "http://GET FROM BACKEND:8000/"
    }
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
    // Get user history
    suspend fun getUserHistory(endpoint: ApiPaths, data: Any?): Result<UserHistoryBackendResponse> {
        return makeApiRequest<UserHistoryBackendResponse>(endpoint, data)
    }
    // Get product data
    suspend fun getProductData(endpoint: ApiPaths, data: Any?): Result<ProductData> {
        return makeApiRequest<ProductData>(endpoint,data)
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
                Log.d("ApiClient", "Response code: ${response.code}, message: ${response.message}")

                if (response.isSuccessful) {
                    response.body?.let {
                        val responseBody = it.string()
                        Log.d("ApiClient", "Response body: $responseBody")

                        val jsonAdapter = moshi.adapter(T::class.java)
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
                Log.e("ApiClient", "Error during API request", e)
                return@withContext Result.failure(e)
            }
        }
    }

    data class LoginUserBackendResponse(
        val user: User,
        val message: String
    )

    data class RegisterUserBackendResponse(
        val message: String,
        val user_id: Int,
        val error:String?
    )
    data class GetUserBackendResponse(
        val user: User
    )
    data class User(
        val user_id: Int,
        val first_name: String,
        val last_name: String,
        val weight: Int,
        val height: Int,
        val email: String,
        val product_id: Int
    )
    data class UserHistoryBackendResponse(
        val session_data: List<SessionData>,
        val streak: Int
    )
    data class SessionData(
        val duration: Int,
        val date: String
    )
    data class ProductData(
        val service_uuid:String,
        val resistance_uuid:String,
        val stop_uuid:String,
        val error:String?,
        val message:String?
    )

}