package com.example.mycomposableapplication

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.expandIn
import androidx.compose.animation.shrinkOut
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
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun DefaultPreview3() {

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.BottomEnd
    ) {

        var buttonState by remember { mutableStateOf(ButtonState.IDLE) }

        AnimatedVisibility(
            visible = buttonState == ButtonState.EXPANDED,
            // By Default, `scaleIn` uses the center as its pivot point. When used with a vertical
            // expansion from the vertical center, the content will be growing from the center of
            // the vertically expanding layout.
            enter = expandIn(expandFrom = Alignment.CenterEnd),
            // By Default, `scaleOut` uses the center as its pivot point. When used with an
            // ExitTransition that shrinks towards the center, the content will be shrinking both
            // in terms of scale and layout size towards the center.
            exit = shrinkOut()
        ) {
            Box(
                Modifier
                    .fillMaxSize()
                    .background(color = Color.Red)
            )
        }

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
                )
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
                    )
                },
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
