package com.example.android.architecture.blueprints.todoapp.Data.Source.local

import android.os.Looper.getMainLooper
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import com.example.android.architecture.blueprints.todoapp.data.source.local.TasksLocalDataSource
import com.example.android.architecture.blueprints.todoapp.data.source.local.ToDoDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.jupiter.api.Assertions.*
import org.junit.runner.RunWith
import com.example.android.architecture.blueprints.todoapp.data.source.TasksDataSource
import com.example.android.architecture.blueprints.todoapp.data.Result.Success
import com.example.android.architecture.blueprints.todoapp.data.Task
import com.example.android.architecture.blueprints.todoapp.data.succeeded
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.TestCoroutineDispatcher
import kotlinx.coroutines.test.TestCoroutineScope
import kotlinx.coroutines.test.runBlockingTest
import org.hamcrest.CoreMatchers.`is`
import org.junit.Assert.assertThat
import org.junit.Test
import org.robolectric.Shadows.shadowOf

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
@MediumTest

class TasksLocalDataSourceTest {

    private lateinit var localDataSource: TasksLocalDataSource
    private lateinit var database: ToDoDatabase

    // Executes each task synchronously using Architecture Components.
    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    @Before
    fun setup() {
        // using an in-memory database for testing, since it doesn't survive killing the process
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            ToDoDatabase::class.java
        )
//            .allowMainThreadQueries()
            .build()

        localDataSource =
            TasksLocalDataSource(
                database.taskDao(),
                Dispatchers.IO
            )
    }

    @After
    fun cleanUp() {
        database.close()
    }

    // runBlocking used here because of https://github.com/Kotlin/kotlinx.coroutines/issues/1204
    // TODO replace with runBlockingTest once issue is resolved
    @Test
    fun saveTask_retrievesTask() = runBlocking {
        // GIVEN - a new task saved in the database
        val newTask = Task("title", "description", false)
        localDataSource.saveTask(newTask)

        // WHEN  - Task retrieved by ID
        val result = localDataSource.getTask(newTask.id)

        // THEN - Same task is returned
        assertThat(result.succeeded, `is`(true))
        result as Success
        assertThat(result.data.title, `is`("title"))
        assertThat(result.data.description, `is`("description"))
        assertThat(result.data.isCompleted, `is`(false))
    }

    @Test
    fun completeTask_retrievedTaskIsComplete() = runBlocking {
        // Given a new task in the persistent repository
        val newTask = Task("title")
        localDataSource.saveTask(newTask)

        // When completed in the persistent repository
        localDataSource.completeTask(newTask)
        val result = localDataSource.getTask(newTask.id)

        // Then the task can be retrieved from the persistent repository and is complete
        assertThat(result.succeeded, `is`(true))
        result as Success
        assertThat(result.data.title, `is`(newTask.title))
        assertThat(result.data.isCompleted, `is`(true))
    }

}