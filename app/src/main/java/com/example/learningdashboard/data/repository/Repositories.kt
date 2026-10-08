package com.example.learningdashboard.data.repository

import com.example.learningdashboard.data.local.CourseDao
import com.example.learningdashboard.data.local.CourseEntity
import com.example.learningdashboard.data.local.CourseWithLessons
import com.example.learningdashboard.data.local.LessonEntity
import com.example.learningdashboard.data.remote.AuthApi
import com.example.learningdashboard.data.remote.CourseApi
import com.example.learningdashboard.data.remote.CourseDto
import com.example.learningdashboard.domain.Course
import com.example.learningdashboard.domain.Lesson
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlin.math.roundToInt

interface CourseRepository {
    fun observeCourses(): Flow<List<Course>>
    fun observeCourse(id: Int): Flow<Course?>
    suspend fun refresh(): Result<Unit>
    suspend fun markLessonCompleted(lessonId: Int)
}

/** Offline-first: Room is the single source of truth; the network only refreshes it. */
class OfflineFirstCourseRepository(
    private val api: CourseApi,
    private val dao: CourseDao
) : CourseRepository {

    override fun observeCourses(): Flow<List<Course>> =
        dao.observeCourses().map { list -> list.map { it.toDomain() } }

    override fun observeCourse(id: Int): Flow<Course?> =
        dao.observeCourse(id).map { it?.toDomain() }

    override suspend fun refresh(): Result<Unit> = try {
        val dtos = api.getCourses()
        dao.sync(dtos.map { CourseEntity(it.id, it.title, it.instructor) },
            dtos.flatMap { it.toLessonEntities() })
        Result.success(Unit)
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        Result.failure(e)
    }

    override suspend fun markLessonCompleted(lessonId: Int) =
        dao.setLessonCompleted(lessonId, true)
}

interface AuthRepository {
    suspend fun login(email: String, password: String): Result<Unit>
}

class DefaultAuthRepository(private val api: AuthApi) : AuthRepository {
    override suspend fun login(email: String, password: String): Result<Unit> = try {
        Result.success(api.login(email, password))
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        Result.failure(e)
    }
}

/**
 * The mock payload only has "lessons: N" and "progress: P", so we synthesize N lessons
 * and mark the first round(P% of N) as completed. A real API would return the lesson list.
 */
internal fun CourseDto.toLessonEntities(): List<LessonEntity> {
    val done = (progress * lessons / 100.0).roundToInt()
    return (1..lessons).map { i ->
        LessonEntity(id = id * 1000 + i, courseId = id, title = "Lesson $i", position = i, completed = i <= done)
    }
}

internal fun CourseWithLessons.toDomain() = Course(
    id = course.id,
    title = course.title,
    instructor = course.instructor,
    lessons = lessons.sortedBy { it.position }.map { Lesson(it.id, it.title, it.completed) }
)
