package com.example.mycomposableapplication

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.with
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.MaterialTheme
import androidx.compose.material.ProvideTextStyle
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun DefaultPreview2() {
    var buttonState by remember { mutableStateOf(ButtonState.IDLE) }

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectTapGestures(
                    onPress = {
                        awaitRelease()
                        println("### tap released")
                    },
                    onTap = {
                        buttonState = if (buttonState == ButtonState.IDLE) {
                            ButtonState.EXPANDED
                        } else {
                            ButtonState.IDLE
                        }
                        println("### tap detected")
                    },
                    onLongPress = {
                        println("### long press detected")
                    }
                )
            }) {
        AnimatedContent(
            targetState = buttonState,
            transitionSpec = {

                fadeIn(
                    initialAlpha = 1f,
                    animationSpec = tween(ANIMATION_HALF_DURATION, ANIMATION_HALF_DURATION)
                ) with
                    fadeOut(targetAlpha = 1f, animationSpec = tween(ANIMATION_HALF_DURATION)) using
                    SizeTransform { initialSize, targetSize ->
                        if (targetState == ButtonState.EXPANDED) {
                            keyframes {
                                // Expand horizontally first.
                                IntSize(targetSize.width, initialSize.height) at ANIMATION_HALF_DURATION / 2
                                durationMillis = ANIMATION_DURATION
                            }
                        } else {
                            keyframes {
                                // Shrink vertically first.
                                IntSize(initialSize.width, targetSize.height) at ANIMATION_HALF_DURATION / 2
                                durationMillis = ANIMATION_DURATION
                            }
                        }
                    }
            }
        ) { state ->

            if (state == ButtonState.EXPANDED) {
                Box(
                    contentAlignment = Alignment.BottomEnd,
                    modifier = Modifier
                        .background(
                            color = Color.Red,
                            shape = RectangleShape
                        )
                        .fillMaxSize(),
                ) {
                    ProvideTextStyle(
                        value = MaterialTheme.typography.button
                    ) {
                        Text(
                            color = Color.White,
                            text = "Cancel booking".uppercase(),
                        )
                    }
                }
            } else {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .background(
                            color = Color.Red,
                            shape = CircleShape
                        )
                        .clip(CircleShape)
                        .defaultMinSize(
                            minWidth = ButtonDefaults.MinWidth,
                            minHeight = ButtonDefaults.MinHeight
                        )
                        .padding(
                            PaddingValues(
                                horizontal = 24.dp,
                                vertical = 16.dp
                            )
                        ),
                ) {
                    ProvideTextStyle(
                        value = MaterialTheme.typography.button
                    ) {
                        Text(
                            color = Color.White,
                            text = "Cancel booking".uppercase(),
                        )
                    }
                }
            }
        }
    }
}
