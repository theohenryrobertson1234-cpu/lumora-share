package com.lumoralabs.share.ui.theme

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

object LumoraPalette {
    val Blue = Color(0xFF247BFF)
    val Pink = Color(0xFFEC4899)
    val Violet = Color(0xFFA855F7)
    val Obsidian = Color(0xFF080D18)
    val Midnight = Color(0xFF101A2B)
    val DeepBlue = Color(0xFF172A46)
    val Platinum = Color(0xFFEAF1FC)
    val Silver = Color(0xFF98A8BE)
    val Surface = Color(0xFF111B2C)
    val SurfaceElevated = Color(0xFF17243A)
    val Border = Color(0xFF263650)
    val Success = Color(0xFF42D6A4)
    val Warning = Color(0xFFFFC66D)
    val Danger = Color(0xFFFF718B)
}

@Composable
fun LumoraTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = darkColorScheme(
            primary = LumoraPalette.Blue,
            secondary = LumoraPalette.Pink,
            tertiary = LumoraPalette.Violet,
            background = LumoraPalette.Obsidian,
            surface = LumoraPalette.Surface,
            onPrimary = Color.White,
            onSecondary = Color.White,
            onBackground = LumoraPalette.Platinum,
            onSurface = LumoraPalette.Platinum
        ),
        content = content
    )
}

@Composable
fun rememberLumoraGradient(): Brush {
    val transition = rememberInfiniteTransition(label = "lumora-gradient")
    val phase by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 9000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "gradient-phase"
    )
    return Brush.linearGradient(
        colors = listOf(
            LumoraPalette.Blue,
            LumoraPalette.Violet,
            LumoraPalette.Pink,
            LumoraPalette.Blue
        ),
        start = Offset(phase * 420f, (1f - phase) * 260f),
        end = Offset(720f - phase * 300f, 360f + phase * 180f)
    )
}

@Composable
fun LumoraGradientPanel(
    modifier: Modifier = Modifier,
    shape: RoundedCornerShape = RoundedCornerShape(28.dp),
    content: @Composable BoxScope.() -> Unit
) {
    val gradient = rememberLumoraGradient()
    Box(
        modifier = modifier
            .clip(shape)
            .background(gradient),
        content = content
    )
}

@Composable
fun LumoraGradientButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    val gradient = rememberLumoraGradient()
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(17.dp))
            .background(
                if (enabled) gradient else Brush.linearGradient(
                    listOf(Color(0xFF344155), Color(0xFF344155))
                )
            ),
        shape = RoundedCornerShape(17.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color.Transparent,
            disabledContainerColor = Color.Transparent,
            contentColor = Color.White,
            disabledContentColor = LumoraPalette.Silver
        ),
        elevation = ButtonDefaults.buttonElevation(
            defaultElevation = 0.dp,
            pressedElevation = 0.dp
        )
    ) {
        Text(text = text, style = MaterialTheme.typography.labelLarge)
    }
}
