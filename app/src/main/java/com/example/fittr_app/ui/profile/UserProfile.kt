package com.example.fittr_app.ui.profile

import com.example.fittr_app.R

data class UserProfile(
    val name: String,
    val user_id:Int,
    val imageResourceId: Int = R.drawable.user,
    val isAddUserButton: Boolean = false
)
