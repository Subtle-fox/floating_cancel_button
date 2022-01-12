package com.example.mycomposableapplication

import android.graphics.Paint
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationVector2D
import androidx.compose.animation.core.AnimationVector3D
import androidx.compose.animation.core.TweenSpec
import androidx.compose.animation.core.TwoWayConverter
import androidx.compose.animation.core.VectorConverter
import androidx.compose.animation.core.animateValueAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.drag
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
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
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.layout
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch


const val INITIAL = 1
const val DRAG = 2
const val FINAL = 3
const val DISPOSE = 4

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun DefaultPreviewY() {
    val anchor = remember { Animatable(Offset.Zero, Offset.VectorConverter) }
    var animationState by remember { mutableStateOf(INITIAL) }
    var buttonTouchPosition by remember { mutableStateOf(Offset.Zero) }

    var buttonParams by remember {
        mutableStateOf(
            ButtonParamsY(
                Offset.Zero,
                IntSize(0, 0),
            )
        )
    }

    val initFillInParams = FillInParams(1f, 1f, 0f)
    val fillInParams = remember { Animatable(initFillInParams, FillInConverter) }

    LaunchedEffect(animationState) {
        when (animationState) {
            DRAG -> {
                val scaleFactor = 1.2f
                fillInParams.animateTo(FillInParams(scaleFactor, scaleFactor, 1f), animationSpec = TweenSpec(700))
            }

            FINAL -> {
                fillInParams.animateTo(initFillInParams, animationSpec = TweenSpec(700))
            }

            INITIAL -> {
                launch {
                    fillInParams.animateTo(initFillInParams, animationSpec = TweenSpec(700))
                }
                launch {
                    anchor.animateTo(buttonTouchPosition, animationSpec = TweenSpec(700))
                }
            }
        }
    }

    var buttonParamsX = ButtonParamsX(Offset.Zero, IntSize(0, 0))

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.BottomEnd
    ) {
        FillInRect(anchor, buttonParams, fillInParams, {
            buttonParamsX = it

            println("### buttonParamsX: ${buttonParamsX}")

        }, buttonTouchPosition)

        CancelButtonY(
            buttonParams,
            anchor,
            { isPressed ->
                animationState = when {
                    isPressed -> DRAG
                    animationState == DRAG -> INITIAL
                    else -> DISPOSE
                }
            },
            {
                buttonTouchPosition = it
            },
            {
                buttonParams = it


                val pointer = buttonParams.parentOffset.plus(anchor.value).minus(buttonTouchPosition)
                val isHit = with(buttonParamsX) {
                    pointer.y > parentOffset.y && (pointer.x > parentOffset.x && pointer.x < parentOffset.x + size.width)
                }
                if (isHit) {
                    if (animationState != FINAL) {
                        animationState = FINAL
                    }
                } else {
                    if (animationState == FINAL) {
                        animationState = DRAG
                    }
                }
            }
        )
    }
}

private data class ButtonParamsX(
    val parentOffset: Offset,
    val size: IntSize,
)

private data class ButtonParamsY(
    val parentOffset: Offset,
    val size: IntSize,
)

data class FillInParamsY(
    val scaleX: Float,
    val scaleY: Float,
    val overlayAlpha: Float
)

@Composable
fun ParamsAnimationY(targetSize: FillInParams): FillInParams {
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

@OptIn(ExperimentalAnimationApi::class)
@Composable
private fun FillInRect(
    anchor: Animatable<Offset, AnimationVector2D>,
    params: ButtonParamsY,
    fillInParams: Animatable<FillInParams, AnimationVector3D>,
    onParamsChanged: (ButtonParamsX) -> Unit,
    touch: Offset
) {


    if (params.parentOffset != Offset.Zero && anchor.value != Offset.Zero) {
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
                    .composed {
                        onGloballyPositioned {
                            onParamsChanged.invoke(
                                ButtonParamsX(parentOffset = it.positionInRoot(), size = it.size)
                            )
                        }
                    }
            )
        }

        Canvas(
            modifier = Modifier.fillMaxSize()

        ) {
            drawRect(Color(0, 0, 0, (0x1f * fillInParams.value.overlayAlpha).toInt()))

            val topLeftBase = params.parentOffset.plus(anchor.value).minus(touch)
            drawRoundRect(
                Color.Red,
                topLeft = topLeftBase.copy(
                    x = topLeftBase.x - params.size.width * (fillInParams.value.scaleX - 1) / 2,
                    y = topLeftBase.y - params.size.height * (fillInParams.value.scaleY - 1) / 2
                ),
                size = Size(
                    params.size.width.toFloat() * fillInParams.value.scaleX,
                    params.size.height.toFloat() * fillInParams.value.scaleY
                ),
                cornerRadius = CornerRadius(64.dp.toPx(), 64.dp.toPx())
            )

            val sizePx = 14f.sp.toPx()
            val paint = Paint().apply {
                textAlign = Paint.Align.CENTER
                textSize = sizePx
                color = Color(0xFF0018A8).toArgb()
            }

            drawContext.canvas.nativeCanvas.drawText(
                "Android CCC".uppercase(),
                params.parentOffset.plus(anchor.value.minus(touch)).x + params.size.width / 2 - sizePx / 2 * 0,
                params.parentOffset.plus(anchor.value.minus(touch)).y + params.size.height / 2,
//                center.x,
//                center.y,
                paint
            )
        }

        if (false) {
            val xxx = params.parentOffset.plus(anchor.value)
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .layout { measurable, constraints ->
                        val placeable = measurable.measure(constraints)
                        val height = params.size.height
                        layout(params.size.width, height) {
                            placeable.placeRelative(
                                xxx.x.toInt() - constraints.maxWidth + params.size.width,
                                xxx.y.toInt() - constraints.maxHeight + params.size.height
                            )
                        }
                    }

            ) {

                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .background(
                            color = Color.Red,
                            shape = RoundedCornerShape(64.dp)
                        )
                        .padding(
                            PaddingValues(
                                horizontal = 24.dp,
                                vertical = 16.dp
                            )
                        )
//                    .size(
//                        params.size.width.dp,
//                        params.size.height.dp,
//                    )
                ) {
                    ProvideTextStyle(value = MaterialTheme.typography.button) {
                        Text(
                            color = Color.Black,
                            text = "Canceling...".uppercase(),
                            textAlign = TextAlign.End
                        )
                    }
                }
            }
        }

    }
}

@Composable
private fun CancelButtonY(
    buttonParams: ButtonParamsY,
    anchor: Animatable<Offset, AnimationVector2D>,
    onPressed: (Boolean) -> Unit,
    onTouch: (Offset) -> Unit,
//    onDragChange: (Offset) -> Unit,
    onParamsChanged: (ButtonParamsY) -> Unit,
) {
    var touch = Offset.Zero

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .offset(x = (-48).dp)
            .padding(
//                horizontal = 48.dp,
                vertical = 100.dp
            )
            .background(
                color = Color.Red,
                shape = RoundedCornerShape(64.dp)
            )
            .composed {
                onGloballyPositioned {
                    onParamsChanged.invoke(
                        buttonParams.copy(parentOffset = it.positionInRoot(), size = it.size)
                    )
                }
            }
            .pointerInput(Unit) {
                coroutineScope {
                    while (true) {
                        val pointer = awaitPointerEventScope {
                            awaitFirstDown().also {
                                val touchPosition = it.position
                                println("### touchPosition ${touchPosition}")

                                touch = touchPosition
//                                val newParams = buttonParams.copy(touchOffset = touchPosition)
                                launch {
                                    anchor.snapTo(touchPosition)
                                }
//                                onParamsChanged.invoke(newParams)

                                onTouch.invoke(it.position)
                                onPressed(true)
                            }
                        }
                        awaitPointerEventScope {
                            drag(pointer.id) { change ->
                                launch {
                                    println("### ${buttonParams}")
//                                    anchor.snapTo(change.position.plus(buttonParams.touchOffset))
                                    anchor.snapTo(change.position)
                                }
                            }
                        }
                        onPressed(false)
                    }
                }
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
