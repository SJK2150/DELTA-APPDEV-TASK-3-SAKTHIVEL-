package com.example.meritmatch4

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.meritmatch4.network.NetworkUtils
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class UserTaskActivity : AppCompatActivity() {

    private lateinit var taskAdapter: TaskAdapter
    private lateinit var recyclerViewTasks: RecyclerView
    private lateinit var actionButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_task)

        // Initialize UI components
        val addTaskButton: Button = findViewById(R.id.add_task_button)
        recyclerViewTasks = findViewById(R.id.recycler_view_tasks)
        actionButton = findViewById(R.id.action_button)

        taskAdapter = TaskAdapter(mutableListOf(), { task -> reserveTask(task) }, { taskId -> completeTask(taskId) })

        recyclerViewTasks.layoutManager = LinearLayoutManager(this)
        recyclerViewTasks.adapter = taskAdapter

        val token = intent.getStringExtra("TOKEN")
        if (token != null) {
            fetchTasks(token)
        } else {
            Toast.makeText(this, "No token found. Please log in first.", Toast.LENGTH_LONG).show()
        }

        addTaskButton.setOnClickListener {
            val intent = Intent(this, AddTaskActivity::class.java)
            intent.putExtra("TOKEN", token)
            startActivity(intent)
        }

        actionButton.setOnClickListener {
            showReservationRequests()
        }
    }

    private fun fetchTasks(token: String) {
        val apiService: ApiService = NetworkUtils.retrofitInstance.create(ApiService::class.java)
        val call = apiService.getTasks("Bearer $token")

        call.enqueue(object : Callback<List<Task>> {
            override fun onResponse(call: Call<List<Task>>, response: Response<List<Task>>) {
                if (response.isSuccessful) {
                    val tasks = response.body()
                    tasks?.let {
                        taskAdapter.updateTasks(it)
                    }
                } else {
                    Toast.makeText(this@UserTaskActivity, "Failed to fetch tasks", Toast.LENGTH_LONG).show()
                }
            }

            override fun onFailure(call: Call<List<Task>>, t: Throwable) {
                Toast.makeText(this@UserTaskActivity, "Network Error: ${t.message}", Toast.LENGTH_LONG).show()
            }
        })
    }

    private fun reserveTask(task: Task) {
        val token = intent.getStringExtra("TOKEN")
        val userId = 1  // Replace with the actual user ID
        if (token != null) {
            val apiService: ApiService = NetworkUtils.retrofitInstance.create(ApiService::class.java)
            val call = apiService.reserveTask("Bearer $token", task.id, userId)

            call.enqueue(object : Callback<Void> {
                override fun onResponse(call: Call<Void>, response: Response<Void>) {
                    if (response.isSuccessful) {
                        Toast.makeText(this@UserTaskActivity, "Task reserved successfully", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(this@UserTaskActivity, "Failed to reserve task", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<Void>, t: Throwable) {
                    Toast.makeText(this@UserTaskActivity, "Network Error: ${t.message}", Toast.LENGTH_SHORT).show()
                }
            })
        }
    }

    private fun completeTask(taskId: Int) {
        val token = intent.getStringExtra("TOKEN")
        if (token != null) {
            val apiService: ApiService = NetworkUtils.retrofitInstance.create(ApiService::class.java)
            val call = apiService.completeTask("Bearer $token", taskId)

            call.enqueue(object : Callback<Void> {
                override fun onResponse(call: Call<Void>, response: Response<Void>) {
                    if (response.isSuccessful) {
                        Toast.makeText(this@UserTaskActivity, "Task completed!", Toast.LENGTH_SHORT).show()
                        fetchTasks(token) // Refresh the task list
                    } else {
                        Toast.makeText(this@UserTaskActivity, "Failed to complete task", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<Void>, t: Throwable) {
                    Toast.makeText(this@UserTaskActivity, "Network error: ${t.message}", Toast.LENGTH_SHORT).show()
                }
            })
        }
    }

    private fun showReservationRequests() {
        val token = intent.getStringExtra("TOKEN")
        if (token != null) {
            val apiService: ApiService = NetworkUtils.retrofitInstance.create(ApiService::class.java)
            val call = apiService.getTasks("Bearer $token")

            call.enqueue(object : Callback<List<Task>> {
                override fun onResponse(call: Call<List<Task>>, response: Response<List<Task>>) {
                    if (response.isSuccessful) {
                        val tasks = response.body()
                        val reservedTask = tasks?.find { it.user_id == getUserId() && it.reserved && !it.completed }

                        if (reservedTask != null) {
                            AlertDialog.Builder(this@UserTaskActivity)
                                .setTitle("Reservation Request")
                                .setMessage("User ${reservedTask.user_id} has reserved your task: ${reservedTask.title}. Do you want to approve?")
                                .setPositiveButton("Approve") { _, _ ->
                                    approveReservation(reservedTask.id, token)
                                }
                                .setNegativeButton("Reject") { dialog, _ ->
                                    dialog.dismiss()
                                    Toast.makeText(this@UserTaskActivity, "Reservation rejected.", Toast.LENGTH_SHORT).show()
                                }
                                .create()
                                .show()
                        } else {
                            showNoReservationMessage()
                        }
                    } else {
                        Toast.makeText(this@UserTaskActivity, "Failed to fetch tasks", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<List<Task>>, t: Throwable) {
                    Toast.makeText(this@UserTaskActivity, "Network error: ${t.message}", Toast.LENGTH_SHORT).show()
                }
            })
        }
    }

    private fun showNoReservationMessage() {
        AlertDialog.Builder(this@UserTaskActivity)
            .setTitle("No Reservations")
            .setMessage("No reservation requests.")
            .setPositiveButton("OK") { dialog, _ ->
                dialog.dismiss()
            }
            .create()
            .show()
    }

    private fun approveReservation(taskId: Int, token: String) {
        val apiService: ApiService = NetworkUtils.retrofitInstance.create(ApiService::class.java)
        val call = apiService.approveTask("Bearer $token", taskId)

        call.enqueue(object : Callback<Void> {
            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                if (response.isSuccessful) {
                    Toast.makeText(this@UserTaskActivity, "Reservation approved!", Toast.LENGTH_SHORT).show()
                    fetchTasks(token) // Refresh the task list
                } else {
                    Toast.makeText(this@UserTaskActivity, "Failed to approve reservation", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<Void>, t: Throwable) {
                Toast.makeText(this@UserTaskActivity, "Network error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun getUserId(): Int {
        // Implement this function to return the actual user ID
        return 1
    }
}
