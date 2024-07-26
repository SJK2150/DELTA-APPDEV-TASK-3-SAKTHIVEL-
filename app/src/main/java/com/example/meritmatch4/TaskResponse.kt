package com.example.meritmatch4

data class TaskResponse(
    val id: Int,
    val title: String,
    val description: String,
    val karmaPoints: Int,
    val userId: Int,
    val reserved: Boolean,
    val completed: Boolean
)
