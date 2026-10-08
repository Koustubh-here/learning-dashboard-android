package com.example.learningdashboard

import com.example.learningdashboard.domain.Course
import com.example.learningdashboard.domain.Lesson
import com.example.learningdashboard.domain.ProgressCalculator
import com.example.learningdashboard.ui.dashboard.DashboardUiState
import com.example.learningdashboard.ui.dashboard.RefreshStatus
import com.example.learningdashboard.ui.dashboard.reduceDashboard
import org.junit.Assert.assertEquals
import org.junit.Test

class DashboardReducerTest {
    private val course = Course(1, "Python", "John", listOf(
        Lesson(1, "a", true), Lesson(2, "b", false), Lesson(3, "c", false), Lesson(4, "d", false)))

    @Test fun progress_is_derived_from_completed_lessons() {
        assertEquals(25, course.progress)
        val after = course.copy(lessons = course.lessons.map { if (it.id == 2) it.copy(isCompleted = true) else it })
        assertEquals(50, after.progress)
    }

    @Test fun progress_handles_empty_course_and_rounding() {
        assertEquals(0, ProgressCalculator.percent(0, 0))
        assertEquals(33, ProgressCalculator.percent(1, 3))
        assertEquals(100, ProgressCalculator.percent(3, 3))
    }

    @Test fun empty_cache_while_loading_shows_Loading() =
        assertEquals(DashboardUiState.Loading, reduceDashboard(emptyList(), RefreshStatus.Loading))

    @Test fun empty_cache_and_failed_refresh_shows_Error() =
        assertEquals(DashboardUiState.Error("boom"), reduceDashboard(emptyList(), RefreshStatus.Failed("boom")))

    @Test fun empty_cache_and_successful_refresh_shows_Empty() =
        assertEquals(DashboardUiState.Empty, reduceDashboard(emptyList(), RefreshStatus.Idle))

    @Test fun cached_data_is_still_shown_when_refresh_fails_offline() {
        val s = reduceDashboard(listOf(course), RefreshStatus.Failed("No internet connection"))
        assertEquals(DashboardUiState.Success(listOf(course), "No internet connection"), s)
    }
}
