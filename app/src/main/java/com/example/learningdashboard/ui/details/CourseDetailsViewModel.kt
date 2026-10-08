package com.example.learningdashboard.ui.details

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.learningdashboard.data.repository.CourseRepository
import com.example.learningdashboard.domain.Course
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

sealed interface DetailsUiState {
    data object Loading : DetailsUiState
    data object NotFound : DetailsUiState
    data class Success(val course: Course) : DetailsUiState
}

class CourseDetailsViewModel(
    private val repo: CourseRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {
    private val courseId: Int = checkNotNull(savedStateHandle["courseId"])

    // Progress is derived from lessons in the DB, so marking a lesson updates
    // this screen AND the dashboard automatically (single source of truth).
    val uiState: StateFlow<DetailsUiState> = repo.observeCourse(courseId)
        .map { if (it == null) DetailsUiState.NotFound else DetailsUiState.Success(it) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), DetailsUiState.Loading)

    fun markCompleted(lessonId: Int) {
        viewModelScope.launch { repo.markLessonCompleted(lessonId) }
    }
}
