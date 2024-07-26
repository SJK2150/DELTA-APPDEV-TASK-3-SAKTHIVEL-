package com.example.meritmatch4

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.meritmatch4.ApiService
import com.example.meritmatch4.network.NetworkUtils
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class UpdateTaskActivity : AppCompatActivity() {

    private val apiService: ApiService by lazy {
        NetworkUtils.retrofitInstance.create(ApiService::class.java)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_update_task)

        val titleEditText = findViewById<EditText>(R.id.title)
        val descriptionEditText = findViewById<EditText>(R.id.description)
        val karmaPointsEditText = findViewById<EditText>(R.id.karma_points)
        val submitButton = findViewById<Button>(R.id.submit_button)


        val taskId = intent.getIntExtra("TASK_ID", 0)
        val userId = intent.getIntExtra("USER_ID", 1)
        val token = intent.getStringExtra("TOKEN") ?: "your_token_here"

        submitButton.setOnClickListener {
            val title = titleEditText.text.toString()
            val description = descriptionEditText.text.toString()
            val karmaPoints = karmaPointsEditText.text.toString().toIntOrNull() ?: 0


            val task = Task(
                id = taskId,
                title = title,
                description = description,
                karma_points = karmaPoints,
                user_id = userId,
                reserved = false,
                completed = false
            )


            apiService.updateTask("Bearer $token", taskId, task).enqueue(object : Callback<TaskResponse> {
                override fun onResponse(call: Call<TaskResponse>, response: Response<TaskResponse>) {
                    if (response.isSuccessful) {
                        Toast.makeText(this@UpdateTaskActivity, "Task updated successfully!", Toast.LENGTH_SHORT).show()
                        finish()
                    } else {
                        Toast.makeText(this@UpdateTaskActivity, "Failed to update task: ${response.errorBody()?.string()}", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<TaskResponse>, t: Throwable) {
                    Toast.makeText(this@UpdateTaskActivity, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
                }
            })
        }
    }
}
