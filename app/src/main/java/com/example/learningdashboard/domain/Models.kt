package com.example.learningdashboard.domain

import kotlin.math.roundToInt

data class Lesson(val id: Int, val title: String, val isCompleted: Boolean)

data class Course(
    val id: Int,
    val title: String,
    val instructor: String,
    val lessons: List<Lesson>
) {
    val totalLessons: Int get() = lessons.size
    val completedLessons: Int get() = lessons.count { it.isCompleted }
    val progress: Int get() = ProgressCalculator.percent(completedLessons, totalLessons)
}

/** Single place for progress math so UI/list/details can never disagree. */
object ProgressCalculator {
    fun percent(completed: Int, total: Int): Int =
        if (total <= 0) 0 else (completed * 100.0 / total).roundToInt().coerceIn(0, 100)
}
