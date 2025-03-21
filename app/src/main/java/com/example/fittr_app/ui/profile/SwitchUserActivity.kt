package com.example.fittr_app.ui.profile

import android.content.Intent
import android.os.Bundle
import android.os.PersistableBundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.fittr_app.DashboardActivity
import com.example.fittr_app.R
import com.example.fittr_app.connections.ApiClient
import com.example.fittr_app.connections.ApiClientProvider
import com.example.fittr_app.connections.ApiPaths
import com.example.fittr_app.ui.auth.AuthActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class SwitchUserActivity: AppCompatActivity() {

    private val apiClient = ApiClientProvider.apiClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_switch_user)

        val recyclerView: RecyclerView = findViewById(R.id.viewProfiles)

        val productId = intent.getIntExtra("product_id",1)

        // Launch the coroutine within the lifecycle scope
        lifecycleScope.launch {
            val userList = fetchUserProfiles(productId).toMutableList()
            userList.add(UserProfile("Add User", R.drawable.ic_add_user, isAddUserButton = true))
            // Now set the adapter after the data is fetched
            recyclerView.layoutManager = GridLayoutManager(this@SwitchUserActivity, 2)
            recyclerView.adapter = UserProfileAdapter(userList) { selectedUser ->
                val intent = Intent(this@SwitchUserActivity, DashboardActivity::class.java).apply {
                    putExtra("User_Name", selectedUser.name)
                    putExtra("user_id",selectedUser.user_id)
                }
                startActivity(intent)
                finish()
            }
        }
    }

    private suspend fun fetchUserProfiles(productId: Int): List<UserProfile> {
        // Make the API call using the productId
        val result = apiClient.getUsers(ApiPaths.GetUsers(productId), data = null)

        return if (result.isSuccess) {
            // If successful, extract the response (GetUsersBackendResponse)
            val response = result.getOrNull()

            // If the response is not null, map it to a list of UserProfiles
            response?.users?.map { backendUser ->
                // Map backend data to UserProfile
                UserProfile(name = backendUser.full_name, user_id = backendUser.id) // Adjust this if necessary
            } ?: emptyList()
        } else {
            // On failure, show a Toast and return an empty list
            runOnUiThread {
                Toast.makeText(
                    this@SwitchUserActivity,
                    "Failed to load users",
                    Toast.LENGTH_LONG
                ).show()
            }
            emptyList()
        }
    }



}