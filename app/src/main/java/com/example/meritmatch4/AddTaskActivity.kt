package com.example.meritmatch4

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class AddTaskActivity : AppCompatActivity() {

    private lateinit var sharedPreferences: SharedPreferences
    private val PREFS_NAME = "MyPrefs"
    private val USER_ID_KEY = "user_id"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_task)

        sharedPreferences = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

        val titleEditText = findViewById<EditText>(R.id.title)
        val descriptionEditText = findViewById<EditText>(R.id.description)
        val karmaPointsEditText = findViewById<EditText>(R.id.karma_points)
        val submitButton = findViewById<Button>(R.id.submit_button)

        submitButton.setOnClickListener {
            val title = titleEditText.text.toString()
            val description = descriptionEditText.text.toString()
            val karmaPoints = karmaPointsEditText.text.toString().toIntOrNull() ?: 0
            val userId = getNextUserId()
            val token = intent.getStringExtra("TOKEN") ?: "your_token_here"

            val task = Task(
                id = 0,
                title = title,
                description = description,
                karma_points = karmaPoints,
                user_id = userId,
                reserved = false,
                completed = false
            )

            RetrofitClient.instance.addTask("Bearer $token", task).enqueue(object : Callback<TaskResponse> {
                override fun onResponse(call: Call<TaskResponse>, response: Response<TaskResponse>) {
                    if (response.isSuccessful) {
                        Toast.makeText(this@AddTaskActivity, "Task added successfully!", Toast.LENGTH_SHORT).show()
                        // Return to UserTaskActivity and pass the token
                        val intent = Intent(this@AddTaskActivity, UserTaskActivity::class.java)
                        intent.putExtra("TOKEN", token)
                        startActivity(intent)
                        finish()
                    } else {
                        Toast.makeText(this@AddTaskActivity, "Failed to add task: ${response.errorBody()?.string()}", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<TaskResponse>, t: Throwable) {
                    Toast.makeText(this@AddTaskActivity, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
                }
            })
        }
    }

    private fun getNextUserId(): Int {
        val currentId = sharedPreferences.getInt(USER_ID_KEY, 99)
        val newId = currentId + 1
        sharedPreferences.edit().putInt(USER_ID_KEY, newId).apply()
        return newId
    }
}
