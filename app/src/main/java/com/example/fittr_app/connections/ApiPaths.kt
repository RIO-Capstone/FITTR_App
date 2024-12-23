package com.example.fittr_app.connections


sealed class ApiPaths(val path: String, val method: String) {
    object RegisterUser : ApiPaths("user/register", "POST")
    object LoginUser : ApiPaths("user/login", "POST")
    data class GetUser(val userId: Int) : ApiPaths("user/$userId", "GET")
}