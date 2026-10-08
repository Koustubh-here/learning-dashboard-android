package com.example.learningdashboard

import androidx.lifecycle.SavedStateHandle
import com.example.learningdashboard.data.repository.CourseRepository
import com.example.learningdashboard.domain.Course
import com.example.learningdashboard.domain.Lesson
import com.example.learningdashboard.ui.dashboard.DashboardUiState
import com.example.learningdashboard.ui.dashboard.DashboardViewModel
import com.example.learningdashboard.ui.details.CourseDetailsViewModel
import com.example.learningdashboard.ui.details.DetailsUiState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import java.io.IOException

/** In-memory stand-in for the real repository, so tests need no Android, Room or network. */
class FakeCourseRepository(
    initial: List<Course>,
    private val refreshResult: Result<Unit> = Result.success(Unit)
) : CourseRepository {
    private val courses = MutableStateFlow(initial)

    override fun observeCourses(): Flow<List<Course>> = courses
    override fun observeCourse(id: Int): Flow<Course?> =
        courses.map { list -> list.firstOrNull { it.id == id } }

    override suspend fun refresh(): Result<Unit> = refreshResult

    override suspend fun markLessonCompleted(lessonId: Int) {
        courses.update { list ->
            list.map { c ->
                c.copy(lessons = c.lessons.map { if (it.id == lessonId) it.copy(isCompleted = true) else it })
            }
        }
    }
}

@OptIn(ExperimentalCoroutinesApi::class)
class CourseFlowTest {
    private val dispatcher = UnconfinedTestDispatcher()

    private val course = Course(
        id = 1, title = "Python", instructor = "John",
        lessons = listOf(Lesson(1, "a", true), Lesson(2, "b", false), Lesson(3, "c", false), Lesson(4, "d", false))
    )

    @Before fun setUp() = Dispatchers.setMain(dispatcher)
    @After fun tearDown() = Dispatchers.resetMain()

    @Test
    fun `marking a lesson complete updates course progress`() = runTest {
        val repo = FakeCourseRepository(listOf(course))
        val vm = CourseDetailsViewModel(repo, SavedStateHandle(mapOf("courseId" to 1)))
        backgroundScope.launch(dispatcher) { vm.uiState.collect {} }

        assertEquals(25, (vm.uiState.value as DetailsUiState.Success).course.progress)

        vm.markCompleted(lessonId = 2)

        assertEquals(50, (vm.uiState.value as DetailsUiState.Success).course.progress)
    }

    @Test
    fun `offline refresh failure keeps showing cached courses with a message`() = runTest {
        val repo = FakeCourseRepository(listOf(course), Result.failure(IOException("No internet connection")))
        val vm = DashboardViewModel(repo)
        backgroundScope.launch(dispatcher) { vm.uiState.collect {} }

        assertEquals(
            DashboardUiState.Success(listOf(course), "No internet connection"),
            vm.uiState.value
        )
    }
}