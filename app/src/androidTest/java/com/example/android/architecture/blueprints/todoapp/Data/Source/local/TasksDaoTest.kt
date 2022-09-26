package com.example.android.architecture.blueprints.todoapp.Data.Source.local

import android.app.Application
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Root
import androidx.test.espresso.matcher.ViewMatchers.assertThat
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.SmallTest
import com.example.android.architecture.blueprints.todoapp.data.Task
import com.example.android.architecture.blueprints.todoapp.data.source.local.ToDoDatabase
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.CoreMatchers.notNullValue
import org.junit.*
import org.junit.runner.RunWith

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
@SmallTest
class TasksDaoTest {

    // Executes each task synchronously using Architecture Components.
    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    private lateinit var dataBase: ToDoDatabase

    @Before
    fun initDb(){
        dataBase = Room.inMemoryDatabaseBuilder(ApplicationProvider.getApplicationContext(),
        ToDoDatabase::class.java).build()
    }

    @After
    fun closeDb(){
        dataBase.close()
    }

    @Test
    fun insertTaskAndGetById() = runBlockingTest {
        // GIVEN - Insert a task.
        val task = Task("title", "description")
        dataBase.taskDao().insertTask(task)
        // WHEN - Get the task by id from the database.
       val loaded = dataBase.taskDao().getTaskById(task.id)
        // THEN - The loaded data contains the expected values.
        Assert.assertThat(loaded as Task, notNullValue())
        assertThat(loaded.id, `is`(task.id))
        assertThat(loaded.title, `is`(task.title))
        assertThat(loaded.description, `is`(task.description))
        assertThat(loaded.isCompleted, `is`(task.isCompleted))

    }

    @Test
    fun updateTaskAndGetById() = runBlockingTest {
        // 1. Insert a task into the DAO.
        val task = Task("title", "description",false,"1")
        dataBase.taskDao().insertTask(task)
        // 2. Update the task by creating a new task with the same ID but different attributes.
        val task2 = Task("title2", "description2",true,"1")
        dataBase.taskDao().updateTask(task2)
        // 3. Check that when you get the task by its ID, it has the updated values.
        val loaded = dataBase.taskDao().getTaskById(task.id)
        Assert.assertThat(loaded as Task, notNullValue())
        assertThat(loaded.id, `is`(task2.id))
        assertThat(loaded.title, `is`(task2.title))
        assertThat(loaded.description, `is`(task2.description))
        assertThat(loaded.isCompleted, `is`(task2.isCompleted))

    }
}