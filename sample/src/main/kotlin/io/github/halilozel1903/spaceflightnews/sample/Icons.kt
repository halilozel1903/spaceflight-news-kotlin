package io.github.halilozel1903.spaceflightnews.sample

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke

/** Small drawn icons, so the sample needs no icon library. */
@Composable
fun SearchIcon(color: Color, modifier: Modifier = Modifier) {
    Canvas(modifier) {
        val s = size.minDimension
        val stroke = s * 0.1f
        drawCircle(color, radius = s * 0.3f, center = Offset(s * 0.42f, s * 0.42f), style = Stroke(width = stroke))
        drawLine(color, Offset(s * 0.64f, s * 0.64f), Offset(s * 0.88f, s * 0.88f), strokeWidth = stroke, cap = StrokeCap.Round)
    }
}

@Composable
fun CloseIcon(color: Color, modifier: Modifier = Modifier) {
    Canvas(modifier) {
        val s = size.minDimension
        val stroke = s * 0.1f
        drawLine(color, Offset(s * 0.22f, s * 0.22f), Offset(s * 0.78f, s * 0.78f), strokeWidth = stroke, cap = StrokeCap.Round)
        drawLine(color, Offset(s * 0.78f, s * 0.22f), Offset(s * 0.22f, s * 0.78f), strokeWidth = stroke, cap = StrokeCap.Round)
    }
}

@Composable
fun BackIcon(color: Color, modifier: Modifier = Modifier) {
    Canvas(modifier) {
        val s = size.minDimension
        val stroke = s * 0.1f
        drawLine(color, Offset(s * 0.2f, s * 0.5f), Offset(s * 0.82f, s * 0.5f), strokeWidth = stroke, cap = StrokeCap.Round)
        drawLine(color, Offset(s * 0.2f, s * 0.5f), Offset(s * 0.46f, s * 0.24f), strokeWidth = stroke, cap = StrokeCap.Round)
        drawLine(color, Offset(s * 0.2f, s * 0.5f), Offset(s * 0.46f, s * 0.76f), strokeWidth = stroke, cap = StrokeCap.Round)
    }
}

@Composable
fun ArrowOutIcon(color: Color, modifier: Modifier = Modifier) {
    Canvas(modifier) {
        val s = size.minDimension
        val stroke = s * 0.1f
        drawLine(color, Offset(s * 0.25f, s * 0.75f), Offset(s * 0.75f, s * 0.25f), strokeWidth = stroke, cap = StrokeCap.Round)
        drawLine(color, Offset(s * 0.4f, s * 0.25f), Offset(s * 0.75f, s * 0.25f), strokeWidth = stroke, cap = StrokeCap.Round)
        drawLine(color, Offset(s * 0.75f, s * 0.25f), Offset(s * 0.75f, s * 0.6f), strokeWidth = stroke, cap = StrokeCap.Round)
    }
}
