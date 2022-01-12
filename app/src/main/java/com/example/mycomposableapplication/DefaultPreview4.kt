package com.example.mycomposableapplication

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationVector3D
import androidx.compose.animation.core.TweenSpec
import androidx.compose.animation.core.TwoWayConverter
import androidx.compose.animation.core.animateValueAsState
import androidx.compose.animation.core.keyframes
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.MaterialTheme
import androidx.compose.material.ProvideTextStyle
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun DefaultPreview4() {
    CancelationReveal()
}

private data class ButtonParams(
    val parentOffset: Offset,
    val size: IntSize,
)

 data class FillInParams(
    val scaleX: Float,
    val scaleY: Float,
    val overlayAlpha: Float
)

@Composable
private fun ParamsAnimation(targetSize: FillInParams): FillInParams {
    val animSize: FillInParams by animateValueAsState(targetSize,
        TwoWayConverter(
            convertToVector = { params: FillInParams ->
                AnimationVector3D(params.scaleX, params.scaleX, params.overlayAlpha)
            },
            convertFromVector = { vector: AnimationVector3D ->
                FillInParams(scaleX = vector.v1, scaleY = vector.v2, overlayAlpha = vector.v3)
            }
        )
    )
    return animSize
}

 object FillInConverter : TwoWayConverter<FillInParams, AnimationVector3D> {
    override val convertFromVector: (AnimationVector3D) -> FillInParams
        get() = { vector: AnimationVector3D ->
            FillInParams(scaleX = vector.v1, scaleY = vector.v2, overlayAlpha = vector.v3)
        }

    override val convertToVector: (FillInParams) -> AnimationVector3D
        get() = { params: FillInParams ->
            AnimationVector3D(params.scaleX, params.scaleY, params.overlayAlpha)
        }
}

@Composable
private fun CancelationReveal() {
    var isPressed by remember { mutableStateOf(false) }
    var buttonParams by remember {
        mutableStateOf(
            ButtonParams(
                Offset.Zero,
                IntSize(0, 0),
            )
        )
    }

    val initFillInParams = FillInParams(1f, 1f, 0f)
    val fillInParams = remember { Animatable(initFillInParams, FillInConverter) }

    LaunchedEffect(isPressed) {
        if (isPressed) {

            val scaleFactor =
                ((buttonParams.parentOffset.y + buttonParams.size.height) / (buttonParams.size.height / 2))

            fillInParams.animateTo(FillInParams(scaleFactor, scaleFactor, 1f),
                animationSpec = keyframes {
                    FillInParams(1f, 1f, 1f) at 300
                    FillInParams(1f, 1f, 1f) at 500
                    FillInParams(scaleFactor / 2, scaleFactor / 2, 1f) at 1500  //with FastOutLinearInEasing
                    FillInParams(scaleFactor, scaleFactor, 1f) at 2500
                    durationMillis = 2500
                }
            )
        } else {
            fillInParams.animateTo(initFillInParams, animationSpec = TweenSpec(1500))
        }
    }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.BottomEnd
    ) {
        FillInRect(buttonParams, fillInParams)
        CancelButtonX(
            { isPressed = it },
            { buttonParams = it }
        )
    }
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
private fun FillInRect(
    params: ButtonParams,
    fillInParams: Animatable<FillInParams, AnimationVector3D>
) {
    if (params.parentOffset != Offset.Zero /*&& animatable.value > 1.1f*/) {
        ProvideTextStyle(
            value = MaterialTheme.typography.button
        ) {
            Text(
                text = "Hold to cancel".uppercase(),
                color = Color(0, 0, 0, (fillInParams.value.overlayAlpha * 4 * 0x1f).toInt()),
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 32.dp)
            )
        }

        Canvas(modifier = Modifier.fillMaxSize()) {
            drawRect(Color(0, 0, 0, (0x1f * fillInParams.value.overlayAlpha).toInt()))

            scale(
                fillInParams.value.scaleX,
                fillInParams.value.scaleY,
                pivot = Offset(
                    params.parentOffset.x + params.size.width * 0.7f,
                    params.parentOffset.y + params.size.height / 2
                )
            ) {
                drawRoundRect(
                    Color.Red,
                    topLeft = params.parentOffset,
                    size = Size(params.size.width.toFloat(), params.size.height.toFloat()),
                    cornerRadius = CornerRadius(64.dp.toPx(), 64.dp.toPx())
                )
            }
        }


    }
}

@Composable
private fun CancelButtonX(
    onPressed: (Boolean) -> Unit,
    onParamsChanged: (ButtonParams) -> Unit,
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .padding(
                horizontal = 48.dp,
                vertical = 100.dp
            )
            .background(
                color = Color.Red,
                shape = RoundedCornerShape(64.dp)
            )
            .composed {
                this.onGloballyPositioned {
                    onParamsChanged.invoke(
                        ButtonParams(it.positionInRoot(), it.size)
                    )
                }
            }
            .pointerInput(Unit) {
                detectTapGestures(
                    onPress = {
                        awaitRelease()
                        onPressed.invoke(false)
                    },
                    onLongPress = {
                        onPressed.invoke(true)
                    }
                )
            }
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
