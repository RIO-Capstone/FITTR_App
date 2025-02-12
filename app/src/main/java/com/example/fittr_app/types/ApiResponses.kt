package com.example.fittr_app.types

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
    val left_resistance_uuid:String,
    val right_resistance_uuid:String,
    val exercise_initialize_uuid: String,
    val stop_uuid:String,
    val error:String?,
    val message:String?
)
data class AIReply(
    val message:String,
    val error: String?
)
