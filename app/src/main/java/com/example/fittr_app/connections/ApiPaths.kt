package com.example.fittr_app.connections


sealed class ApiPaths(val path: String) {
    object RegisterUser : ApiPaths("user/register")
    object LoginUser : ApiPaths("user/login")
    data class GetUser(val userId: Int) : ApiPaths("user/$userId")
}
