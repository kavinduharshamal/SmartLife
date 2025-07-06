package com.example.smartlife.data.TaskDTO


import androidx.room.*
import com.example.smartlife.data.TaskEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TaskDTO {
    @Query("SELECT * FROM tasks ORDER BY dateTime ASC")
    fun getAllTasks(): Flow<List<TaskEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(task: TaskEntity)

    @Update
    suspend fun update(task: TaskEntity)

    @Delete
    suspend fun delete(task: TaskEntity)
}