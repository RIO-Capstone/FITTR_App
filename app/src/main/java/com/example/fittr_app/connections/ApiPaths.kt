package com.example.fittr_app.connections


sealed class ApiPaths(val path: String, val method: String) {
    object RegisterUser : ApiPaths("user/register", "POST")
    object LoginUser : ApiPaths("user/login", "POST")
    object ExerciseSessionFeedback : ApiPaths("user/latest_exercise_session_feedback","POST")
    data class GetUser(val userId: Int) : ApiPaths("user/$userId", "GET")
    data class GetProduct(val productId:Int) : ApiPaths("product/$productId", "GET")
    data class GetUserAIReply(val userId: Int) : ApiPaths("user/$userId/ai_feedback", "GET")
    data class GetUserExercisePlan(val userId: Int) : ApiPaths("user/$userId/ai_rep_generation", "GET")
}