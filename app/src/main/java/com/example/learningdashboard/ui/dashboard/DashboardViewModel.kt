package com.example.learningdashboard.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.learningdashboard.data.repository.CourseRepository
import com.example.learningdashboard.domain.Course
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

sealed interface RefreshStatus {
    data object Loading : RefreshStatus
    data object Idle : RefreshStatus
    data class Failed(val message: String) : RefreshStatus
}

sealed interface DashboardUiState {
    data object Loading : DashboardUiState
    data object Empty : DashboardUiState
    data class Error(val message: String) : DashboardUiState
    /** [staleMessage] != null => showing cached data while the latest refresh failed. */
    data class Success(val courses: List<Course>, val staleMessage: String? = null) : DashboardUiState
}

/** Pure function => trivially unit-testable. Cache (Room) wins over network failures. */
fun reduceDashboard(courses: List<Course>, refresh: RefreshStatus): DashboardUiState = when {
    courses.isNotEmpty() -> DashboardUiState.Success(
        courses, (refresh as? RefreshStatus.Failed)?.message
    )
    refresh is RefreshStatus.Loading -> DashboardUiState.Loading
    refresh is RefreshStatus.Failed -> DashboardUiState.Error(refresh.message)
    else -> DashboardUiState.Empty
}

class DashboardViewModel(private val repo: CourseRepository) : ViewModel() {
    private val refreshStatus = MutableStateFlow<RefreshStatus>(RefreshStatus.Loading)

    val uiState: StateFlow<DashboardUiState> =
        combine(repo.observeCourses(), refreshStatus, ::reduceDashboard)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), DashboardUiState.Loading)

    init { refresh() }

    fun refresh() {
        viewModelScope.launch {
            refreshStatus.value = RefreshStatus.Loading
            refreshStatus.value = repo.refresh().fold(
                onSuccess = { RefreshStatus.Idle },
                onFailure = { RefreshStatus.Failed(it.message ?: "Something went wrong") }
            )
        }
    }
}
