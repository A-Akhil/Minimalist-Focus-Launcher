/*
 * Minimalist Focus Launcher
 * Copyright (C) 2025 A-Akhil
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */

package com.minifocus.launcher.ui

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.outlined.Adjust
import androidx.compose.material.icons.outlined.Eco
import androidx.compose.material.icons.outlined.Shield
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.minifocus.launcher.R

/**
 * Onboarding step definitions.
 *
 * Pager layout: [0=Calendar, 1=Tasks, 2=Home(initial), 3=Drawer]
 *
 * Gesture directions (from user's finger movement):
 *   - "Swipe right" (finger left->right) reveals LOWER page index (left neighbor)
 *   - "Swipe left"  (finger right->left) reveals HIGHER page index (right neighbor)
 *
 * Home(2)->Tasks(1): swipe RIGHT
 * Tasks(1)->Calendar(0): swipe RIGHT
 * Calendar(0)->Home(2): swipe LEFT
 * Home(2)->Drawer(3): swipe LEFT
 */
object OnboardingSteps {
    const val WELCOME = 0
    const val SWIPE_TO_TASKS = 1
    const val SWIPE_TO_CALENDAR = 2
    const val SWIPE_BACK_HOME = 3
    const val SWIPE_TO_DRAWER = 4
    const val LONG_PRESS_TO_PIN = 5
    const val TAP_PIN = 6
    const val COMPLETE = 7
    const val TOTAL = 8
}

// ---- Animation constants ----

private object OnboardingAnim {
    const val SWIPE_SLIDE_DISTANCE = 80f
    const val SWIPE_DURATION_MS = 1100
    const val PULSE_DURATION_MS = 1200
    const val HINT_PULSE_DURATION_MS = 800

    const val SCRIM_ALPHA_WELCOME = 0.82f
    const val SCRIM_ALPHA_COMPLETE = 0.78f
    const val SCRIM_ALPHA_SWIPE = 0.35f

    const val FINGER_DOT_SIZE_DP = 44
    const val FINGER_DOT_ALPHA = 0.85f
    const val FINGER_DOT_FADE_MIN = 0.2f

    const val ARROW_SIZE = 12f
    const val ARROW_SPACING = 26f
    const val ARROW_STROKE_WIDTH = 2.5f
    const val ARROW_OFFSET = 30f

    const val DOT_ACTIVE_SIZE_DP = 8
    const val DOT_INACTIVE_SIZE_DP = 5
}

// ---- Main overlay ----

@Composable
fun OnboardingOverlay(
    step: Int,
    onFinish: () -> Unit,
    onWelcomeDismiss: () -> Unit = {},
    onSkip: () -> Unit = {}
) {
    Box(modifier = Modifier.fillMaxSize()) {
        // Scrim layer
        val scrimAlpha = when (step) {
            OnboardingSteps.WELCOME,
            OnboardingSteps.LONG_PRESS_TO_PIN,
            OnboardingSteps.TAP_PIN -> 0f
            OnboardingSteps.COMPLETE -> OnboardingAnim.SCRIM_ALPHA_COMPLETE
            else -> OnboardingAnim.SCRIM_ALPHA_SWIPE
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = scrimAlpha))
                .then(
                    when (step) {
                        OnboardingSteps.COMPLETE -> Modifier.pointerInput(Unit) {
                            awaitEachGesture { awaitFirstDown(requireUnconsumed = false) }
                        }
                        else -> Modifier
                    }
                )
        )

        // Step-specific content
        when (step) {
            OnboardingSteps.WELCOME ->
                WelcomeScreen(onStart = onWelcomeDismiss, onSkip = onSkip)

            OnboardingSteps.SWIPE_TO_TASKS ->
                SwipeGuide(
                    direction = SwipeDirection.RIGHT,
                    label = stringResource(R.string.onboarding_swipe_to_tasks),
                    step = step,
                    onSkip = onSkip
                )

            OnboardingSteps.SWIPE_TO_CALENDAR ->
                SwipeGuide(
                    direction = SwipeDirection.RIGHT,
                    label = stringResource(R.string.onboarding_swipe_to_calendar),
                    step = step,
                    onSkip = onSkip
                )

            OnboardingSteps.SWIPE_BACK_HOME ->
                SwipeGuide(
                    direction = SwipeDirection.LEFT,
                    label = stringResource(R.string.onboarding_swipe_back_home),
                    step = step,
                    onSkip = onSkip
                )

            OnboardingSteps.SWIPE_TO_DRAWER ->
                SwipeGuide(
                    direction = SwipeDirection.LEFT,
                    label = stringResource(R.string.onboarding_swipe_to_drawer),
                    step = step,
                    onSkip = onSkip
                )

            OnboardingSteps.LONG_PRESS_TO_PIN ->
                FloatingHint(
                    text = stringResource(R.string.onboarding_long_press_to_pin),
                    onSkip = onSkip
                )

            OnboardingSteps.TAP_PIN ->
                FloatingHint(
                    text = stringResource(R.string.onboarding_tap_pin),
                    onSkip = onSkip
                )

            OnboardingSteps.COMPLETE ->
                CompletionScreen(onFinish = onFinish)
        }
    }
}

// ---- Welcome screen ----

@Composable
private fun WelcomeScreen(onStart: () -> Unit, onSkip: () -> Unit) {
    val bgColor = androidx.compose.material3.MaterialTheme.colorScheme.background
    val onBgColor = androidx.compose.material3.MaterialTheme.colorScheme.onBackground

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(bgColor)
            .semantics {
                contentDescription = "Welcome to MiniFocus. Tap Get Started to begin."
            }
    ) {
        // Draw the subtle landscape mountains
        androidx.compose.foundation.Canvas(modifier = Modifier.fillMaxSize()) {
            val h = size.height
            val w = size.width
            val path1 = androidx.compose.ui.graphics.Path().apply {
                moveTo(0f, h * 0.35f)
                lineTo(w * 0.4f, h * 0.28f)
                lineTo(w * 0.7f, h * 0.32f)
                lineTo(w, h * 0.25f)
                lineTo(w, h)
                lineTo(0f, h)
                close()
            }
            val path2 = androidx.compose.ui.graphics.Path().apply {
                moveTo(0f, h * 0.45f)
                lineTo(w * 0.3f, h * 0.38f)
                lineTo(w * 0.8f, h * 0.45f)
                lineTo(w, h * 0.38f)
                lineTo(w, h)
                lineTo(0f, h)
                close()
            }
            drawPath(path1, color = onBgColor.copy(alpha = 0.03f))
            drawPath(path2, color = onBgColor.copy(alpha = 0.02f))
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 32.dp, vertical = 48.dp)
        ) {
            Spacer(modifier = Modifier.weight(0.5f))

            // Real App Icon / Logo without clipping circle
            androidx.compose.foundation.Image(
                painter = androidx.compose.ui.res.painterResource(id = R.drawable.ic_launcher_foreground),
                contentDescription = "MiniFocus Logo",
                colorFilter = androidx.compose.ui.graphics.ColorFilter.tint(onBgColor),
                modifier = Modifier.size(96.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Welcome",
                color = onBgColor,
                fontSize = 40.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "to " + stringResource(R.string.app_name),
                color = onBgColor,
                fontSize = 18.sp,
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.Medium
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Subtle divider line
            Box(
                modifier = Modifier
                    .width(40.dp)
                    .height(2.dp)
                    .background(onBgColor.copy(alpha = 0.5f), RoundedCornerShape(1.dp))
            )

            Spacer(modifier = Modifier.height(48.dp))

            // Feature rows
            Column(
                verticalArrangement = Arrangement.spacedBy(28.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                FeatureRow(
                    icon = Icons.Outlined.Eco,
                    title = "Minimal by design",
                    subtitle = "Everything you need,\nnothing you don't."
                )
                FeatureRow(
                    icon = Icons.Outlined.Adjust,
                    title = "Stay focused",
                    subtitle = "Reduce distractions and\nget more done."
                )
                FeatureRow(
                    icon = Icons.Outlined.Shield,
                    title = "Privacy first",
                    subtitle = "Your data stays on\nyour device."
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = onStart,
                colors = ButtonDefaults.buttonColors(
                    containerColor = onBgColor,
                    contentColor = bgColor
                ),
                shape = RoundedCornerShape(50),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
            ) {
                Text(
                    text = "Get Started",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 16.sp
                )
                Spacer(modifier = Modifier.width(8.dp))
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            StepDots(currentStep = OnboardingSteps.WELCOME)
        }
    }
}

@Composable
private fun FeatureRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String
) {
    val onBgColor = androidx.compose.material3.MaterialTheme.colorScheme.onBackground
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        Box(
            modifier = Modifier
                .size(52.dp)
                .background(onBgColor.copy(alpha = 0.05f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = onBgColor,
                modifier = Modifier.size(24.dp)
            )
        }
        Spacer(modifier = Modifier.width(20.dp))
        Column {
            Text(
                text = title,
                color = onBgColor,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = subtitle,
                color = onBgColor.copy(alpha = 0.7f),
                fontSize = 14.sp,
                lineHeight = 20.sp
            )
        }
    }
}

// ---- Swipe guide ----

private enum class SwipeDirection { LEFT, RIGHT }

@Composable
private fun SwipeGuide(
    direction: SwipeDirection,
    label: String,
    step: Int,
    onSkip: () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "swipe")

    val targetOffset = if (direction == SwipeDirection.RIGHT) {
        OnboardingAnim.SWIPE_SLIDE_DISTANCE
    } else {
        -OnboardingAnim.SWIPE_SLIDE_DISTANCE
    }

    val slideOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = targetOffset,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = OnboardingAnim.SWIPE_DURATION_MS,
                easing = LinearEasing
            ),
            repeatMode = RepeatMode.Restart
        ),
        label = "slide"
    )

    val circleFade by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = OnboardingAnim.SWIPE_DURATION_MS,
                easing = LinearEasing
            ),
            repeatMode = RepeatMode.Restart
        ),
        label = "fade"
    )

    val directionDesc = if (direction == SwipeDirection.RIGHT) "right" else "left"

    Box(
        modifier = Modifier
            .fillMaxSize()
            .semantics { contentDescription = "$label. Swipe $directionDesc to continue." }
    ) {
        // Skip button top-right
        SkipButton(
            onSkip = onSkip,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 32.dp, end = 16.dp)
        )

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.align(Alignment.Center)
        ) {
            Box(
                modifier = Modifier
                    .width(220.dp)
                    .height(80.dp),
                contentAlignment = Alignment.Center
            ) {
                SwipeArrows(direction = direction, progress = slideOffset)

                Box(
                    modifier = Modifier
                        .offset { IntOffset(slideOffset.dp.roundToPx(), 0) }
                        .alpha(circleFade.coerceIn(OnboardingAnim.FINGER_DOT_FADE_MIN, 1f))
                        .size(OnboardingAnim.FINGER_DOT_SIZE_DP.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = OnboardingAnim.FINGER_DOT_ALPHA))
                )
            }

            Spacer(modifier = Modifier.height(36.dp))

            Text(
                text = label,
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            StepDots(currentStep = step)
        }
    }
}

@Composable
private fun SwipeArrows(
    direction: SwipeDirection,
    progress: Float
) {
    val normalizedProgress = (progress / OnboardingAnim.SWIPE_SLIDE_DISTANCE).let {
        if (direction == SwipeDirection.LEFT) -it else it
    }

    Canvas(modifier = Modifier.size(220.dp, 80.dp)) {
        val centerY = size.height / 2f

        for (i in 0..2) {
            val baseX = if (direction == SwipeDirection.RIGHT) {
                size.width / 2f + OnboardingAnim.ARROW_OFFSET + (i * OnboardingAnim.ARROW_SPACING)
            } else {
                size.width / 2f - OnboardingAnim.ARROW_OFFSET - (i * OnboardingAnim.ARROW_SPACING)
            }
            val alpha = (0.1f + normalizedProgress * 0.25f * (3 - i) / 3f).coerceIn(0.06f, 0.35f)

            val path = Path().apply {
                if (direction == SwipeDirection.RIGHT) {
                    moveTo(baseX - OnboardingAnim.ARROW_SIZE, centerY - OnboardingAnim.ARROW_SIZE)
                    lineTo(baseX, centerY)
                    lineTo(baseX - OnboardingAnim.ARROW_SIZE, centerY + OnboardingAnim.ARROW_SIZE)
                } else {
                    moveTo(baseX + OnboardingAnim.ARROW_SIZE, centerY - OnboardingAnim.ARROW_SIZE)
                    lineTo(baseX, centerY)
                    lineTo(baseX + OnboardingAnim.ARROW_SIZE, centerY + OnboardingAnim.ARROW_SIZE)
                }
            }
            drawPath(
                path = path,
                color = Color.White.copy(alpha = alpha),
                style = androidx.compose.ui.graphics.drawscope.Stroke(
                    width = OnboardingAnim.ARROW_STROKE_WIDTH
                )
            )
        }
    }
}

// ---- Floating hint ----

@Composable
private fun FloatingHint(text: String, onSkip: () -> Unit) {
    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        // Skip button top-right
        SkipButton(
            onSkip = onSkip,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 32.dp, end = 16.dp)
        )

        val infiniteTransition = rememberInfiniteTransition(label = "pulse")
        val pulseAlpha by infiniteTransition.animateFloat(
            initialValue = 0.75f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
                animation = tween(OnboardingAnim.HINT_PULSE_DURATION_MS),
                repeatMode = RepeatMode.Reverse
            ),
            label = "pulse"
        )

        Box(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 80.dp)
                .clip(RoundedCornerShape(28.dp))
                .background(Color.Black.copy(alpha = 0.85f))
                .padding(horizontal = 24.dp, vertical = 14.dp)
                .alpha(pulseAlpha)
                .semantics { contentDescription = text }
        ) {
            Text(
                text = text,
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

// ---- Completion screen ----

@Composable
private fun CompletionScreen(onFinish: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .semantics {
                contentDescription = "Onboarding complete. Tap Continue to set up permissions."
            },
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(horizontal = 48.dp)
        ) {
            Text(
                text = stringResource(R.string.onboarding_complete_title),
                color = Color.White,
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = stringResource(R.string.onboarding_complete_subtitle),
                color = Color.White.copy(alpha = 0.65f),
                fontSize = 16.sp,
                textAlign = TextAlign.Center,
                lineHeight = 24.sp
            )

            Spacer(modifier = Modifier.height(44.dp))

            Button(
                onClick = onFinish,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.White,
                    contentColor = Color.Black
                ),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth(0.6f)
                    .height(52.dp)
            ) {
                Text(
                    text = stringResource(R.string.onboarding_continue),
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 16.sp
                )
                Spacer(modifier = Modifier.width(8.dp))
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            StepDots(currentStep = OnboardingSteps.COMPLETE)
        }
    }
}

// ---- Skip button ----

@Composable
private fun SkipButton(onSkip: () -> Unit, modifier: Modifier = Modifier) {
    Button(
        onClick = onSkip,
        modifier = modifier,
        colors = ButtonDefaults.buttonColors(
            containerColor = Color.White.copy(alpha = 0.2f),
            contentColor = Color.White
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Text(
            text = stringResource(R.string.onboarding_skip),
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}

// ---- Step dots ----

@Composable
private fun StepDots(currentStep: Int) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.semantics {
            contentDescription = "Step ${currentStep + 1} of ${OnboardingSteps.TOTAL}"
        }
    ) {
        repeat(OnboardingSteps.TOTAL) { index ->
            Box(
                modifier = Modifier
                    .size(
                        if (index == currentStep) OnboardingAnim.DOT_ACTIVE_SIZE_DP.dp
                        else OnboardingAnim.DOT_INACTIVE_SIZE_DP.dp
                    )
                    .clip(CircleShape)
                    .background(
                        if (index <= currentStep) Color.White
                        else Color.White.copy(alpha = 0.2f)
                    )
            )
        }
    }
}
