package com.example.smartlife.model.TaskViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.smartlife.data.TaskDTO.TaskDTO
import com.example.smartlife.data.TaskEntity
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class TaskViewModel(private val taskDao: TaskDTO) : ViewModel() {
    val tasks = taskDao.getAllTasks()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun addTask(task: TaskEntity) = viewModelScope.launch { taskDao.insert(task) }
    fun updateTask(task: TaskEntity) = viewModelScope.launch { taskDao.update(task) }
    fun deleteTask(task: TaskEntity) = viewModelScope.launch { taskDao.delete(task) }
}
