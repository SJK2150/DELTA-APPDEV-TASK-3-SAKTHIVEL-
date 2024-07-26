package com.example.meritmatch4

import com.example.meritmatch4.Task
import com.example.meritmatch4.TaskResponse
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path

interface ApiService {
    @POST("login")
    fun loginUser(@Body credentials: LoginCredentials): Call<LoginResponse>

    @POST("register")
    fun registerUser(@Body user: User): Call<UserResponse>

    @POST("tasks")  // Ensure this matches the server endpoint
    fun addTask(
        @Header("Authorization") authHeader: String,
        @Body task: Task
    ): Call<TaskResponse>

    @GET("tasks")
    fun getTasks(@Header("Authorization") authHeader: String): Call<List<Task>>

    @PATCH("tasks/{id}")
    fun updateTask(
        @Header("Authorization") token: String,
        @Path("id") id: Int,
        @Body task: Task
    ): Call<TaskResponse>

    @PATCH("tasks/{id}/reserve")
    fun reserveTask(
        @Header("Authorization") token: String,
        @Path("id") id: Int,
        @Body userId: Int
    ): Call<Void>

    @PATCH("tasks/{id}/complete")
    fun completeTask(
        @Header("Authorization") token: String,
        @Path("id") id: Int
    ): Call<Void>

    @PATCH("tasks/{id}/approve")
    fun approveTask(
        @Header("Authorization") token: String,
        @Path("id") id: Int
    ): Call<Void>

    @PATCH("tasks/{id}/settle")
    fun settleTask(
        @Header("Authorization") token: String,
        @Path("id") id: Int
    ): Call<Void>
}
