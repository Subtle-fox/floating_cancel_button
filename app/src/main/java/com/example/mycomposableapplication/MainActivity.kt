package com.example.mycomposableapplication

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.with
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.MaterialTheme
import androidx.compose.material.ProvideTextStyle
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import com.example.mycomposableapplication.ui.theme.MyComposableApplicationTheme

class MainActivity : ComponentActivity() {
    @ExperimentalMaterialApi
    @ExperimentalAnimationApi
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            DefaultPreviewY()
        }
    }
}

@Composable
fun Greeting(name: String) {
    Text(
        text = "Hello $name!",
    )
}

const val ANIMATION_DURATION = 1000
const val ANIMATION_HALF_DURATION = 1000

@ExperimentalMaterialApi
@ExperimentalAnimationApi
@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    MyComposableApplicationTheme {
        Box(
            contentAlignment = Alignment.CenterEnd,
            modifier = Modifier.fillMaxSize()
        ) {

            val expanded = remember { mutableStateOf(false) }
            Surface(
                color = Color.Transparent,
                onClick = { expanded.value = !expanded.value }
            ) {
                AnimatedContent(
                    targetState = expanded,
                    transitionSpec = {

                        fadeIn(
                            initialAlpha = 1f,
                            animationSpec = tween(ANIMATION_HALF_DURATION, ANIMATION_HALF_DURATION)
                        ) with
                            fadeOut(targetAlpha = 1f, animationSpec = tween(ANIMATION_HALF_DURATION)) using
                            SizeTransform { initialSize, targetSize ->
                                if (targetState.value) {
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
                ) { targetExpanded ->
                    if (targetExpanded.value) {
                        CancelButtonExpanded()
                        CancelButton(
                            text = "Cancel",
                            buttonState = mutableStateOf(ButtonState.EXPANDED),
                            state = ButtonState.EXPANDED
                        )
                    } else {
                        CancelButton(
                            text = "Cancel",
                            buttonState = mutableStateOf(ButtonState.EXPANDED),
                            state = ButtonState.EXPANDED
                        )
                    }
                }
            }
        }
    }
}

enum class ButtonState {
    IDLE, EXPANDED
}


///////////////


@Composable
fun CancelButton(
    text: String,
    buttonState: MutableState<ButtonState>,
    state: ButtonState,
) {
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
                        buttonState.value = if (buttonState.value == ButtonState.IDLE) {
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
            },
    ) {
        ProvideTextStyle(
            value = MaterialTheme.typography.button
        ) {
            Text(
                color = Color.White,
                text = text.uppercase(),
            )
        }
    }
}

/////

@Composable
fun CancelButtonExpanded() {
    Canvas(
        modifier = Modifier
            .background(
                color = Color.Red,
                shape = RectangleShape
            )
            .fillMaxSize(),
        onDraw = {
            drawRect(Color.Red)
        }
    )
}
