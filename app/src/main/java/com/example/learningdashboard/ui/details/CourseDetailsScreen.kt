package com.example.learningdashboard.ui.details

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.learningdashboard.AppViewModelProvider
import com.example.learningdashboard.domain.Course
import com.example.learningdashboard.domain.Lesson
import com.example.learningdashboard.ui.components.AppTopBar
import com.example.learningdashboard.ui.components.CourseProgressBar
import com.example.learningdashboard.ui.components.StatusCircle
import com.example.learningdashboard.ui.components.SuccessGreen
import com.example.learningdashboard.ui.components.accentFor

@Composable
fun CourseDetailsScreen(
    onBack: () -> Unit,
    vm: CourseDetailsViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val state by vm.uiState.collectAsStateWithLifecycle()
    val title = (state as? DetailsUiState.Success)?.course?.title.orEmpty()

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = { AppTopBar(title, onBack) }
    ) { padding ->
        Box(Modifier.padding(padding).fillMaxSize()) {
            when (val s = state) {
                DetailsUiState.Loading -> CircularProgressIndicator(Modifier.align(Alignment.Center))
                DetailsUiState.NotFound -> Text("Course not found", Modifier.align(Alignment.Center))
                is DetailsUiState.Success -> Content(s.course, vm::markCompleted)
            }
        }
    }
}

// Rows are intentionally plain (no per-row animation or enter delay) so the list is on screen instantly.
@Composable
private fun Content(course: Course, onComplete: (Int) -> Unit) {
    LazyColumn(contentPadding = PaddingValues(bottom = 24.dp)) {
        item(key = "summary") { Summary(course) }
        item(key = "header") {
            Text("Lessons", style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 4.dp))
        }
        itemsIndexed(course.lessons, key = { _, l -> l.id }) { index, lesson ->
            LessonRow(index + 1, lesson, onComplete = { onComplete(lesson.id) })
        }
    }
}

@Composable
private fun Summary(course: Course) {
    Column(Modifier.fillMaxWidth().padding(16.dp)) {
        Row(verticalAlignment = Alignment.Bottom) {
            Text("${course.progress}%", style = MaterialTheme.typography.headlineMedium)
            Spacer(Modifier.width(8.dp))
            Text("complete", style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 4.dp))
        }
        Spacer(Modifier.height(10.dp))
        CourseProgressBar(course.progress, color = accentFor(course.id))
        Spacer(Modifier.height(8.dp))
        Text(
            "${course.completedLessons} of ${course.totalLessons} lessons completed  ·  by ${course.instructor}",
            style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        if (course.progress == 100) {
            Spacer(Modifier.height(8.dp))
            Text("Course completed", color = SuccessGreen, fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
private fun LessonRow(number: Int, lesson: Lesson, onComplete: () -> Unit) {
    Column {
        Row(
            Modifier.fillMaxWidth().padding(start = 16.dp, end = 8.dp, top = 8.dp, bottom = 8.dp)
                .heightIn(min = 40.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            StatusCircle(done = lesson.isCompleted)
            Spacer(Modifier.width(16.dp))
            Column(Modifier.weight(1f)) {
                Text(lesson.title, style = MaterialTheme.typography.bodyLarge)
                Text("Lesson $number", style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            if (lesson.isCompleted) {
                Text("Completed", style = MaterialTheme.typography.bodySmall, color = SuccessGreen,
                    modifier = Modifier.padding(end = 8.dp))
            } else {
                TextButton(onClick = onComplete) { Text("Mark complete") }
            }
        }
        Box(Modifier.padding(start = 56.dp).fillMaxWidth().height(1.dp)
            .background(MaterialTheme.colorScheme.outline.copy(alpha = 0.6f)))
    }
}
