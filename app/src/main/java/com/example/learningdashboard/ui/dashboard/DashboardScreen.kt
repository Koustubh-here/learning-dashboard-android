package com.example.learningdashboard.ui.dashboard

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.learningdashboard.AppViewModelProvider
import com.example.learningdashboard.domain.Course
import com.example.learningdashboard.ui.components.AppTopBar
import com.example.learningdashboard.ui.components.CourseProgressBar
import com.example.learningdashboard.ui.components.accentFor
import com.example.learningdashboard.ui.components.monogram

@Composable
fun DashboardScreen(
    onCourseClick: (Int) -> Unit,
    vm: DashboardViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val state by vm.uiState.collectAsStateWithLifecycle()

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = { AppTopBar("My courses") }
    ) { padding ->
        AnimatedContent(
            targetState = state,
            contentKey = { it::class },
            transitionSpec = { fadeIn(tween(150)) togetherWith fadeOut(tween(100)) },
            modifier = Modifier.padding(padding).fillMaxSize(),
            label = "dashboardState"
        ) { s ->
            when (s) {
                DashboardUiState.Loading -> LoadingList()
                DashboardUiState.Empty ->
                    Message("No courses yet", "Courses you enrol in will appear here.", "Refresh", vm::refresh)
                is DashboardUiState.Error ->
                    Message("Couldn't load courses", s.message, "Try again", vm::refresh)
                is DashboardUiState.Success -> CourseList(s, onCourseClick, vm::refresh)
            }
        }
    }
}

@Composable
private fun CourseList(s: DashboardUiState.Success, onCourseClick: (Int) -> Unit, onRetry: () -> Unit) {
    Column {
        AnimatedVisibility(
            visible = s.staleMessage != null,
            enter = expandVertically() + fadeIn(),
            exit = shrinkVertically() + fadeOut()
        ) { OfflineBanner(s.staleMessage.orEmpty(), onRetry) }

        LazyColumn(
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item(key = "section") {
                Text("Continue learning", style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(bottom = 4.dp))
            }
            items(s.courses, key = { it.id }) { CourseCard(it) { onCourseClick(it.id) } }
        }
    }
}

@Composable
private fun CourseCard(course: Course, onClick: () -> Unit) {
    val accent = accentFor(course.id)
    Card(
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(0.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(Modifier.padding(14.dp)) {
            Row {
                Box(
                    Modifier.size(72.dp).background(accent.copy(alpha = 0.12f), RoundedCornerShape(8.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(monogram(course.title), color = accent, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                }
                Spacer(Modifier.width(14.dp))
                Column(Modifier.weight(1f)) {
                    Text(course.title, style = MaterialTheme.typography.titleMedium,
                        maxLines = 2, overflow = TextOverflow.Ellipsis)
                    Spacer(Modifier.height(2.dp))
                    Text(course.instructor, style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("${course.totalLessons} lessons", style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            Spacer(Modifier.height(14.dp))
            CourseProgressBar(course.progress)
            Spacer(Modifier.height(10.dp))
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Text("${course.progress}% complete", style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.weight(1f))
                Button(
                    onClick = onClick,
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 18.dp, vertical = 0.dp),
                    modifier = Modifier.height(36.dp)
                ) { Text("Continue", style = MaterialTheme.typography.labelLarge) }
            }
        }
    }
}

@Composable
private fun OfflineBanner(message: String, onRetry: () -> Unit) {
    Surface(color = MaterialTheme.colorScheme.surfaceVariant, modifier = Modifier.fillMaxWidth()) {
        Row(Modifier.padding(start = 16.dp, end = 4.dp), verticalAlignment = Alignment.CenterVertically) {
            Text("Showing saved courses · $message", modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            TextButton(onClick = onRetry) { Text("Retry") }
        }
    }
}

@Composable
private fun LoadingList() {
    val transition = rememberInfiniteTransition(label = "skeleton")
    val alpha by transition.animateFloat(
        initialValue = 0.06f, targetValue = 0.14f,
        animationSpec = infiniteRepeatable(tween(700), RepeatMode.Reverse), label = "alpha"
    )
    val block = MaterialTheme.colorScheme.onSurface.copy(alpha = alpha)
    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        repeat(3) {
            Column(
                Modifier.fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(12.dp))
                    .padding(14.dp)
            ) {
                Row {
                    Box(Modifier.size(72.dp).clip(RoundedCornerShape(8.dp)).background(block))
                    Spacer(Modifier.width(14.dp))
                    Column {
                        Box(Modifier.width(170.dp).height(16.dp).clip(RoundedCornerShape(4.dp)).background(block))
                        Spacer(Modifier.height(8.dp))
                        Box(Modifier.width(110.dp).height(12.dp).clip(RoundedCornerShape(4.dp)).background(block))
                    }
                }
                Spacer(Modifier.height(18.dp))
                Box(Modifier.fillMaxWidth().height(4.dp).clip(RoundedCornerShape(2.dp)).background(block))
            }
        }
    }
}

@Composable
private fun Message(title: String, body: String, action: String, onAction: () -> Unit) {
    Column(
        Modifier.fillMaxSize().padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(title, style = MaterialTheme.typography.titleLarge)
        Spacer(Modifier.height(6.dp))
        Text(body, style = MaterialTheme.typography.bodyMedium, textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.height(20.dp))
        OutlinedButton(onClick = onAction, shape = RoundedCornerShape(8.dp)) { Text(action) }
    }
}
