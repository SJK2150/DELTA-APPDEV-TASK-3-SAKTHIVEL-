package com.example.meritmatch4

import com.google.gson.annotations.SerializedName

data class Task(
    val id: Int,
    val title: String,
    val description: String,
    val karma_points: Int,
    val user_id: Int,
    val reserved: Boolean,
    val completed: Boolean
)
