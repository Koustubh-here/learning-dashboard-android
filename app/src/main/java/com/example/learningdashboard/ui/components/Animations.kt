package com.example.learningdashboard.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

val SuccessGreen = Color(0xFF1E8E3E)

private val Accents = listOf(
    Color(0xFF1A73E8), Color(0xFF188038), Color(0xFFE37400), Color(0xFF9334E6), Color(0xFFD93025)
)

fun accentFor(id: Int): Color = Accents[(id - 1).mod(Accents.size)]

/** "Python Programming" -> "PP". Stand-in for a course thumbnail. */
fun monogram(title: String): String =
    title.split(" ").filter { it.isNotBlank() }.take(2).joinToString("") { it.first().uppercase() }

/** Flat 4dp progress bar. Animates only when the value CHANGES (no replay from 0 on every screen). */
@Composable
fun CourseProgressBar(
    percent: Int,
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.primary
) {
    val fraction by animateFloatAsState(percent / 100f, tween(350), label = "progress")
    Box(
        modifier
            .fillMaxWidth()
            .height(4.dp)
            .clip(RoundedCornerShape(2.dp))
            .background(MaterialTheme.colorScheme.outline)
    ) {
        Box(Modifier.fillMaxHeight().fillMaxWidth(fraction).background(color))
    }
}

/** Filled green check when done, thin grey ring when pending. Drawn, so no icon dependency. */
@Composable
fun StatusCircle(done: Boolean, size: Dp = 24.dp) {
    val ring = MaterialTheme.colorScheme.outline
    Canvas(Modifier.size(size)) {
        val w = this.size.width
        if (done) {
            drawCircle(SuccessGreen)
            val check = Path().apply {
                moveTo(w * 0.28f, h(w) * 0.52f)
                lineTo(w * 0.44f, h(w) * 0.68f)
                lineTo(w * 0.74f, h(w) * 0.36f)
            }
            drawPath(check, Color.White, style = Stroke(w * 0.1f, cap = StrokeCap.Round, join = StrokeJoin.Round))
        } else {
            val stroke = w * 0.08f
            drawCircle(ring, radius = w / 2 - stroke / 2, style = Stroke(stroke))
        }
    }
}

private fun h(w: Float) = w // square canvas

@Composable
private fun BackArrow(color: Color) {
    Canvas(Modifier.size(24.dp)) {
        val s = this.size.width
        val stroke = Stroke(s * 0.09f, cap = StrokeCap.Round, join = StrokeJoin.Round)
        drawLine(color, Offset(s * 0.85f, s * 0.5f), Offset(s * 0.15f, s * 0.5f), stroke.width, StrokeCap.Round)
        val head = Path().apply {
            moveTo(s * 0.45f, s * 0.2f); lineTo(s * 0.15f, s * 0.5f); lineTo(s * 0.45f, s * 0.8f)
        }
        drawPath(head, color, style = stroke)
    }
}

/** Plain white top bar with a hairline divider, like most production apps. */
@Composable
fun AppTopBar(title: String, onBack: (() -> Unit)? = null) {
    Surface(color = MaterialTheme.colorScheme.surface) {
        Column(Modifier.statusBarsPadding()) {
            Row(Modifier.fillMaxWidth().height(56.dp), verticalAlignment = Alignment.CenterVertically) {
                if (onBack != null) {
                    IconButton(onClick = onBack) { BackArrow(MaterialTheme.colorScheme.onSurface) }
                } else {
                    Spacer(Modifier.width(16.dp))
                }
                Text(
                    title, style = MaterialTheme.typography.titleLarge,
                    maxLines = 1, overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f).padding(end = 16.dp)
                )
            }
            Box(Modifier.fillMaxWidth().height(1.dp).background(MaterialTheme.colorScheme.outline))
        }
    }
}
