package com.example.meritmatch4

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.meritmatch4.network.NetworkUtils
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class HomeActivity : AppCompatActivity() {

    private val apiService: ApiService by lazy {
        NetworkUtils.retrofitInstance.create(ApiService::class.java)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        val addTaskButton: Button = findViewById(R.id.add_task_button)
        addTaskButton.setOnClickListener {
            showAddTaskDialog()
        }

        val token = intent.getStringExtra("TOKEN")
        if (token != null) {
            fetchTasks(token)
        } else {
            Toast.makeText(this, "No token found. Please log in first.", Toast.LENGTH_LONG).show()
        }
    }

    private fun showAddTaskDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_add_task, null)
        val dialogBuilder = AlertDialog.Builder(this)
            .setTitle("Add Task")
            .setView(dialogView)
            .setPositiveButton("Add") { dialog, _ ->
                val title = dialogView.findViewById<EditText>(R.id.edit_text_title).text.toString()
                val description = dialogView.findViewById<EditText>(R.id.edit_text_description).text.toString()
                val karmaPoints = dialogView.findViewById<EditText>(R.id.edit_text_karma_points).text.toString().toIntOrNull() ?: 0
                val userId = dialogView.findViewById<EditText>(R.id.edit_text_user_id).text.toString().toIntOrNull() ?: 1

                val token = intent.getStringExtra("TOKEN")
                if (token != null) {
                    addTask(token, title, description, karmaPoints, userId)
                } else {
                    Toast.makeText(this, "No token found. Please log in first.", Toast.LENGTH_LONG).show()
                }
                dialog.dismiss()
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }
            .create()

        dialogBuilder.show()
    }

    private fun fetchTasks(token: String) {
        val call = apiService.getTasks("Bearer $token")

        call.enqueue(object : Callback<List<Task>> {
            override fun onResponse(call: Call<List<Task>>, response: Response<List<Task>>) {
                Log.d("HomeActivity", "Fetch Tasks - Response Code: ${response.code()}")
                if (response.isSuccessful) {
                    val tasks = response.body()
                    tasks?.forEach { task ->
                        Log.d("HomeActivity", "Task: $task")
                    }
                } else {
                    Log.d("HomeActivity", "Fetch Tasks - Error Body: ${response.errorBody()?.string()}")
                    Toast.makeText(this@HomeActivity, "Failed to fetch tasks", Toast.LENGTH_LONG).show()
                }
            }

            override fun onFailure(call: Call<List<Task>>, t: Throwable) {
                Log.e("HomeActivity", "Fetch Tasks - Network Error: ${t.message}")
                Toast.makeText(this@HomeActivity, "Network Error: ${t.message}", Toast.LENGTH_LONG).show()
            }
        })
    }

    private fun addTask(token: String, title: String, description: String, karmaPoints: Int, userId: Int) {
        val task = Task(
            id = 0,                // Assuming ID is auto-generated by the server
            title = title,
            description = description,
            karma_points = karmaPoints,
            user_id = userId,
            reserved = false,     // Default values for new task
            completed = false
        )

        val authHeader = "Bearer $token"

        apiService.addTask(authHeader, task).enqueue(object : Callback<TaskResponse> {
            override fun onResponse(call: Call<TaskResponse>, response: Response<TaskResponse>) {
                Log.d("HomeActivity", "Response Code: ${response.code()}")
                Log.d("HomeActivity", "Response Body: ${response.body()}")
                Log.d("HomeActivity", "Error Body: ${response.errorBody()?.string()}")

                if (response.isSuccessful) {
                    val newTask = response.body()
                    Toast.makeText(this@HomeActivity, "Task added: ${newTask?.title}", Toast.LENGTH_LONG).show()

                    // Navigate to UserTaskActivity to see the updated list
                    val intent = Intent(this@HomeActivity, UserTaskActivity::class.java)
                    intent.putExtra("TOKEN", token)
                    startActivity(intent)
                    finish() // Optionally finish the current activity
                } else {
                    Toast.makeText(this@HomeActivity, "Failed to add task: ${response.errorBody()?.string()}", Toast.LENGTH_LONG).show()
                }
            }

            override fun onFailure(call: Call<TaskResponse>, t: Throwable) {
                Log.e("HomeActivity", "Error: ${t.message}")
                Toast.makeText(this@HomeActivity, "Network Error: ${t.message}", Toast.LENGTH_LONG).show()
            }
        })
    }
}
