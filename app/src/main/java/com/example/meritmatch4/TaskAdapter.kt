package com.example.meritmatch4

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import android.graphics.Color


class TaskAdapter(
    private var tasks: MutableList<Task>,
    private val onReserveClick: (Task) -> Unit,
    private val onCompleteClick: (Int) -> Unit
) : RecyclerView.Adapter<TaskAdapter.TaskViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_task, parent, false)
        return TaskViewHolder(view)
    }

    override fun onBindViewHolder(holder: TaskViewHolder, position: Int) {
        val task = tasks[position]
        holder.bind(task)
    }

    override fun getItemCount() = tasks.size

    fun updateTasks(newTasks: List<Task>) {
        tasks.clear()
        tasks.addAll(newTasks)
        notifyDataSetChanged()
    }

    inner class TaskViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val titleTextView: TextView = itemView.findViewById(R.id.task_title)
        private val descriptionTextView: TextView = itemView.findViewById(R.id.task_description)
        private val karmaPointsTextView: TextView = itemView.findViewById(R.id.task_karma_points)
        private val reserveButton: Button = itemView.findViewById(R.id.reserve_button)
        private val completeButton: Button = itemView.findViewById(R.id.complete_button)

        fun bind(task: Task) {
            titleTextView.text = task.title
            descriptionTextView.text = task.description
            karmaPointsTextView.text = task.karma_points.toString()

            // Update the color of the reserve button based on whether the task is reserved
            reserveButton.setBackgroundColor(
                if (task.reserved) Color.parseColor("#4CAF50") // Green if reserved
                else Color.parseColor("#FF6200EE") // Default color
            )

            // Update the color and visibility of the complete button based on task status
            completeButton.apply {
                visibility = if (task.reserved && !task.completed) View.VISIBLE else View.GONE
                setBackgroundColor(
                    if (task.completed) Color.parseColor("#9E9E9E")
                    else Color.parseColor("#FF6200EE")
                )
                setOnClickListener {
                    onCompleteClick(task.id)
                }
            }

            reserveButton.setOnClickListener {
                onReserveClick(task)
            }
            completeButton.setOnClickListener { onCompleteClick(task.id) }
        }
    }
}
