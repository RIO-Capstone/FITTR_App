package com.example.fittr_app.ui.profile

import android.content.Intent
import android.os.Bundle
import android.os.PersistableBundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.fittr_app.DashboardActivity
import com.example.fittr_app.R

class SwitchUserActivity: AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_switch_user)

        val recyclerView: RecyclerView = findViewById(R.id.viewProfiles)

        val userList = listOf(
            UserProfile("User 1"),
            UserProfile("User 2"),
            UserProfile("User 3"),
            UserProfile("User 4"),
            UserProfile("User 5"),
            UserProfile("User 6")
        )

        // Set up RecyclerView with GridLayoutManager (2 columns)
        recyclerView.layoutManager = GridLayoutManager(this, 2)
        recyclerView.adapter = UserProfileAdapter(userList) { selectedUser ->
            val intent = Intent(this, DashboardActivity::class.java).apply {
                putExtra("User_Name", selectedUser.name)
            }
            startActivity(intent)
            finish()
        }
    }
}