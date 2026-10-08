package com.example.learningdashboard.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
abstract class CourseDao {
    @Transaction
    @Query("SELECT * FROM courses ORDER BY id")
    abstract fun observeCourses(): Flow<List<CourseWithLessons>>

    @Transaction
    @Query("SELECT * FROM courses WHERE id = :id")
    abstract fun observeCourse(id: Int): Flow<CourseWithLessons?>

    // Upsert (UPDATE, not DELETE+INSERT) so FK cascade never wipes local lesson state.
    @Upsert
    abstract suspend fun upsertCourses(courses: List<CourseEntity>)

    // IGNORE keeps the user's locally completed lessons when server data is re-fetched.
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    abstract suspend fun insertLessons(lessons: List<LessonEntity>)

    @Query("UPDATE lessons SET completed = :completed WHERE id = :lessonId")
    abstract suspend fun setLessonCompleted(lessonId: Int, completed: Boolean)

    @Transaction
    open suspend fun sync(courses: List<CourseEntity>, lessons: List<LessonEntity>) {
        upsertCourses(courses)
        insertLessons(lessons)
    }
}
