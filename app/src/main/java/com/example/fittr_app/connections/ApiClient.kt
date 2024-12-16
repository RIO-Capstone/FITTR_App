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
    private val IP_ADDRESS = "<GET FROM BACKEND>";

    companion object {
        private const val BASE_URL = "http://<GET FROM BACKEND>:8000/"
    }

    // This method sends data to the backend server (POST request)
    suspend fun registerUser(endpoint: ApiPaths, data: Any): Result<RegisterUserBackendResponse> {
        // non-blocking function
        return withContext(Dispatchers.IO) {
            try {
                val jsonData = moshi.adapter(Any::class.java).toJson(data)
                val requestBody = jsonData.toRequestBody("application/json".toMediaType())

                val request = Request.Builder()
                    .url("$BASE_URL${endpoint.path}")
                    .post(requestBody) // POST request
                    .build()
                // sending request here
                val response = client.newCall(request).execute()
                Log.d("ApiClient", "Response code: ${response.code}, message: ${response.message}")

                if (response.isSuccessful) {
                    response.body?.let {
                        val responseBody = it.string()
                        Log.d("ApiClient", "Response body: $responseBody")

                        val jsonAdapter = moshi.adapter(RegisterUserBackendResponse::class.java)
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

    data class RegisterUserBackendResponse(
        val message: String,
        val user_id: Int
    )
    data class GetUserBackendResponse(
        val status: Int,
        val user: User
    )
    data class User(
        val id: Int?,
        val firstName: String,
        val lastName: String,
        val email: String,
        val weight: Int,
        val height: Int,
        val phoneNumber: String?,
        val gender: String?,
        val dateOfBirth: String?, // formatted string "dd-MM-yyyy"
        val productId: Int?,
        val password:String?
    )

}