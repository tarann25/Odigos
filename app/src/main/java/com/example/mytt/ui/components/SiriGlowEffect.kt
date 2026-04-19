package com.example.mytt.ui.components

import android.graphics.BlurMaskFilter
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun SiriGlowEffect(
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 24.dp, // Default matches our dialogs
    strokeWidth: Dp = 5.dp, // Reduced by 50%
    glowIntensity: Float = 1.0f
) {
    val density = LocalDensity.current
    val strokeWidthPx = with(density) { strokeWidth.toPx() }
    val cornerRadiusPx = with(density) { cornerRadius.toPx() }

    // 1. Apple Intelligence / Siri Gradient Colors
    val colors = listOf(
        Color(0xFF32D4F4), // Cyan
        Color(0xFF5A46F2), // Deep Purple/Blue
        Color(0xFFF05191), // Magenta/Pink
        Color(0xFFFDB44E), // Orange/Gold
        Color(0xFF32D4F4)  // Close loop
    )

    // 2. Animate Rotation
    val infiniteTransition = rememberInfiniteTransition(label = "glow_rotation")
    val angle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "angle"
    )

    // 3. Animate Pulse (Breathing)
    // We don't use this directly in the drawing below in the snippet provided, 
    // but we can use it to modulate stroke width or alpha if desired.
    // For now, sticking to the user's snippet logic which defines 'pulse' but the usage 
    // might be implicit or I'll apply it to the outer blur radius for extra effect.
    val pulse by infiniteTransition.animateFloat(
        initialValue = 5f, // Reduced pulse range
        targetValue = 10f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    Canvas(modifier = modifier.fillMaxSize()) {
        val sweepBrush = Brush.sweepGradient(
            colors = colors,
            center = center
        )

        drawIntoCanvas { canvas ->
            canvas.save()
            // Removed: canvas.rotate(...) - This was spinning the box!

            val paint = Paint().apply {
                style = PaintingStyle.Stroke
                strokeCap = StrokeCap.Round
                
                val colorInts = colors.map { it.toArgb() }.toIntArray()
                val shader = android.graphics.SweepGradient(center.x, center.y, colorInts, null)
                
                // Matrix Rotation for the Shader ONLY
                val matrix = android.graphics.Matrix()
                matrix.preRotate(angle, center.x, center.y)
                shader.setLocalMatrix(matrix)
                
                this.shader = shader
                alpha = glowIntensity
            }
            
            // Layer 1: Outer Ambient Glow (Wide & Soft)
            paint.strokeWidth = strokeWidthPx * 4
            paint.asFrameworkPaint().maskFilter = BlurMaskFilter(
                with(density) { (15.dp).toPx() }, // Reduced blur
                BlurMaskFilter.Blur.NORMAL
            )
            canvas.drawRoundRect(
                left = 0f, top = 0f, right = size.width, bottom = size.height,
                radiusX = cornerRadiusPx, radiusY = cornerRadiusPx,
                paint = paint
            )

            // Layer 2: Core Glow (Medium) - Modulating with pulse for breathing effect
            paint.strokeWidth = strokeWidthPx * 2
            paint.asFrameworkPaint().maskFilter = BlurMaskFilter(
                with(density) { pulse.dp.toPx() }, // Pulsing blur
                BlurMaskFilter.Blur.NORMAL
            )
            canvas.drawRoundRect(
                left = 0f, top = 0f, right = size.width, bottom = size.height,
                radiusX = cornerRadiusPx, radiusY = cornerRadiusPx,
                paint = paint
            )

            // Layer 3: Sharp Core Line
            paint.strokeWidth = strokeWidthPx
            paint.asFrameworkPaint().maskFilter = BlurMaskFilter(
                with(density) { 1.dp.toPx() }, // Sharper
                BlurMaskFilter.Blur.NORMAL
            )
            canvas.drawRoundRect(
                left = 0f, top = 0f, right = size.width, bottom = size.height,
                radiusX = cornerRadiusPx, radiusY = cornerRadiusPx,
                paint = paint
            )

            canvas.restore()
        }
    }
}