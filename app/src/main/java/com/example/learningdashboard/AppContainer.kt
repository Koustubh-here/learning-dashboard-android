package com.example.learningdashboard

import android.app.Application
import android.content.Context
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.learningdashboard.data.local.AppDatabase
import com.example.learningdashboard.data.remote.FakeAuthApi
import com.example.learningdashboard.data.remote.FakeCourseApi
import com.example.learningdashboard.data.repository.AuthRepository
import com.example.learningdashboard.data.repository.CourseRepository
import com.example.learningdashboard.data.repository.DefaultAuthRepository
import com.example.learningdashboard.data.repository.OfflineFirstCourseRepository
import com.example.learningdashboard.ui.dashboard.DashboardViewModel
import com.example.learningdashboard.ui.details.CourseDetailsViewModel
import com.example.learningdashboard.ui.login.LoginViewModel

/** Manual DI keeps the assignment small; Hilt/Koin is the production upgrade. */
class AppContainer(context: Context) {
    private val db = AppDatabase.create(context)
    val courseRepository: CourseRepository =
        OfflineFirstCourseRepository(FakeCourseApi(context), db.courseDao())
    val authRepository: AuthRepository = DefaultAuthRepository(FakeAuthApi())
}

class LearningApp : Application() {
    lateinit var container: AppContainer
    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
    }
}

object AppViewModelProvider {
    val Factory = viewModelFactory {
        initializer { LoginViewModel(app().container.authRepository) }
        initializer { DashboardViewModel(app().container.courseRepository) }
        initializer { CourseDetailsViewModel(app().container.courseRepository, createSavedStateHandle()) }
    }

    private fun CreationExtras.app() =
        this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as LearningApp
}
