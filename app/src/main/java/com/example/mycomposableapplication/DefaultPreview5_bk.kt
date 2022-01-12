//package com.example.mycomposableapplication
//
//import androidx.compose.animation.ExperimentalAnimationApi
//import androidx.compose.animation.core.Animatable
//import androidx.compose.animation.core.AnimationVector2D
//import androidx.compose.animation.core.AnimationVector3D
//import androidx.compose.animation.core.TweenSpec
//import androidx.compose.animation.core.TwoWayConverter
//import androidx.compose.animation.core.VectorConverter
//import androidx.compose.animation.core.animateValueAsState
//import androidx.compose.foundation.Canvas
//import androidx.compose.foundation.background
//import androidx.compose.foundation.gestures.awaitFirstDown
//import androidx.compose.foundation.gestures.drag
//import androidx.compose.foundation.layout.Box
//import androidx.compose.foundation.layout.PaddingValues
//import androidx.compose.foundation.layout.fillMaxSize
//import androidx.compose.foundation.layout.fillMaxWidth
//import androidx.compose.foundation.layout.padding
//import androidx.compose.foundation.shape.RoundedCornerShape
//import androidx.compose.material.MaterialTheme
//import androidx.compose.material.ProvideTextStyle
//import androidx.compose.material.Text
//import androidx.compose.runtime.Composable
//import androidx.compose.runtime.LaunchedEffect
//import androidx.compose.runtime.getValue
//import androidx.compose.runtime.mutableStateOf
//import androidx.compose.runtime.remember
//import androidx.compose.runtime.setValue
//import androidx.compose.ui.Alignment
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.composed
//import androidx.compose.ui.geometry.CornerRadius
//import androidx.compose.ui.geometry.Offset
//import androidx.compose.ui.geometry.Size
//import androidx.compose.ui.graphics.Color
//import androidx.compose.ui.graphics.drawscope.scale
//import androidx.compose.ui.input.pointer.pointerInput
//import androidx.compose.ui.layout.onGloballyPositioned
//import androidx.compose.ui.layout.positionInRoot
//import androidx.compose.ui.text.style.TextAlign
//import androidx.compose.ui.unit.IntSize
//import androidx.compose.ui.unit.dp
//import kotlinx.coroutines.coroutineScope
//import kotlinx.coroutines.launch
//
//
//const val INITIAL = 1
//const val DRAG = 2
//const val FINAL = 3
//const val DISPOSE = 4
//
//@OptIn(ExperimentalAnimationApi::class)
//@Composable
//fun DefaultPreviewY() {
//    val anchor = remember { Animatable(Offset.Zero, Offset.VectorConverter) }
//    var animationState by remember { mutableStateOf(INITIAL) }
//    var touch: Offset = Offset.Zero
//
//    var buttonParams by remember {
//        mutableStateOf(
//            ButtonParamsY(
//                Offset.Zero,
//                IntSize(0, 0),
//                Offset.Zero,
//            )
//        )
//    }
//
//    val initFillInParams = FillInParams(1f, 1f, 0f)
//    val fillInParams = remember { Animatable(initFillInParams, FillInConverter) }
//
//    LaunchedEffect(animationState) {
//        when (animationState) {
//            DRAG -> {
//                val scaleFactor = 1.2f
//                fillInParams.animateTo(FillInParams(scaleFactor, scaleFactor, 1f), animationSpec = TweenSpec(700))
//            }
//
//            FINAL -> {
//                fillInParams.animateTo(initFillInParams, animationSpec = TweenSpec(700))
//            }
//
//            INITIAL -> {
//                launch {
//                    fillInParams.animateTo(initFillInParams, animationSpec = TweenSpec(700))
//                }
//                launch {
//                    anchor.animateTo(Offset.Zero.plus(buttonParams.touchOffset), animationSpec = TweenSpec(700))
//                }
//            }
//        }
//    }
//
//    var buttonParamsX = ButtonParamsX(Offset.Zero, IntSize(0, 0))
//
//    Box(
//        modifier = Modifier.fillMaxSize(),
//        contentAlignment = Alignment.BottomEnd
//    ) {
//        FillInRect(anchor, buttonParams, fillInParams, { buttonParamsX = it })
//        CancelButtonY(
//            buttonParams,
//            anchor,
//            { isPressed ->
//                animationState = when {
//                    isPressed -> DRAG
//                    animationState == DRAG -> INITIAL
//                    else -> DISPOSE
//                }
//            },
//            { touch = it },
////            { anchor = buttonParams.parentOffset.plus(it).minus(touch) },
//            {
//                buttonParams = it
//
//                val pointer = buttonParams.parentOffset.plus(anchor.value).minus(touch)
//                val isHit = with(buttonParamsX) {
//                    pointer.y > parentOffset.y && (pointer.x > parentOffset.x && pointer.x < parentOffset.x + size.width)
//                }
////                println("### position: $position")
//                if (isHit) {
//                    if (animationState != FINAL) {
//                        animationState = FINAL
//                        println("### hit ~in~ rect")
//                    }
//                } else {
//                    if (animationState == FINAL) {
//                        animationState = DRAG
//                        println("### hit =out= rect")
//                    }
//                }
//            }
//        )
//    }
//}
//
//private data class ButtonParamsX(
//    val parentOffset: Offset,
//    val size: IntSize,
//)
//
//private data class ButtonParamsY(
//    val parentOffset: Offset,
//    val size: IntSize,
//    val touchOffset: Offset,
//)
//
//data class FillInParamsY(
//    val scaleX: Float,
//    val scaleY: Float,
//    val overlayAlpha: Float
//)
//
//@Composable
//fun ParamsAnimationY(targetSize: FillInParams): FillInParams {
//    val animSize: FillInParams by animateValueAsState(targetSize,
//        TwoWayConverter(
//            convertToVector = { params: FillInParams ->
//                AnimationVector3D(params.scaleX, params.scaleX, params.overlayAlpha)
//            },
//            convertFromVector = { vector: AnimationVector3D ->
//                FillInParams(scaleX = vector.v1, scaleY = vector.v2, overlayAlpha = vector.v3)
//            }
//        )
//    )
//    return animSize
//}
//
//@OptIn(ExperimentalAnimationApi::class)
//@Composable
//private fun FillInRect(
//    anchor: Animatable<Offset, AnimationVector2D>,
//    params: ButtonParamsY,
//    fillInParams: Animatable<FillInParams, AnimationVector3D>,
//
//    onParamsChanged: (ButtonParamsX) -> Unit
//) {
//    if (params.parentOffset != Offset.Zero && anchor.value != Offset.Zero) {
//        ProvideTextStyle(
//            value = MaterialTheme.typography.button
//        ) {
//            Text(
//                text = "Hold to cancel".uppercase(),
//                color = Color(0, 0, 0, (fillInParams.value.overlayAlpha * 4 * 0x1f).toInt()),
//                textAlign = TextAlign.Center,
//                modifier = Modifier
//                    .fillMaxWidth()
//                    .padding(bottom = 32.dp)
//                    .composed {
//                        onGloballyPositioned {
//                            onParamsChanged.invoke(
//                                ButtonParamsX(parentOffset = it.positionInRoot(), size = it.size)
//                            )
//                        }
//                    }
//            )
//        }
//
//        Canvas(
//            modifier = Modifier.fillMaxSize()
//
//        ) {
//            drawRect(Color(0, 0, 0, (0x1f * fillInParams.value.overlayAlpha).toInt()))
//
//            scale(
//                fillInParams.value.scaleX,
//                fillInParams.value.scaleY,
//                pivot = Offset(
//                    params.parentOffset.x + params.size.width / 2,
//                    params.parentOffset.y + params.size.height / 2
//                )
//            ) {
//                drawRoundRect(
//                    Color.Red,
//
//                    topLeft = params.parentOffset.plus(anchor.value.minus(params.touchOffset)),
//
//                    size = Size(params.size.width.toFloat(), params.size.height.toFloat()),
//                    cornerRadius = CornerRadius(64.dp.toPx(), 64.dp.toPx())
//                )
//            }
//        }
//    }
//}
//
//@Composable
//private fun CancelButtonY(
//    buttonParams: ButtonParamsY,
//    anchor: Animatable<Offset, AnimationVector2D>,
//    onPressed: (Boolean) -> Unit,
//    onTouch: (Offset) -> Unit,
////    onDragChange: (Offset) -> Unit,
//    onParamsChanged: (ButtonParamsY) -> Unit,
//) {
//    Box(
//        contentAlignment = Alignment.Center,
//        modifier = Modifier
//            .padding(
//                horizontal = 48.dp,
//                vertical = 100.dp
//            )
//            .background(
//                color = Color.Red,
//                shape = RoundedCornerShape(64.dp)
//            )
//            .composed {
//                onGloballyPositioned {
//                    onParamsChanged.invoke(
//                        buttonParams.copy(parentOffset = it.positionInRoot(), size = it.size)
//                    )
//                }
//            }
//            .pointerInput(Unit) {
//                coroutineScope {
//                    while (true) {
//                        val pointer = awaitPointerEventScope {
//                            awaitFirstDown().also {
//                                val touchPosition = it.position
//                                buttonParams
//                                    .copy(touchOffset = touchPosition)
//                                    .also {
//                                        launch {
//                                            anchor.snapTo(touchPosition)
//                                        }
//                                        onParamsChanged.invoke(it)
//                                    }
////                                onTouch.invoke(it.position)
//                                onPressed(true)
//                            }
//                        }
//                        awaitPointerEventScope {
//                            drag(pointer.id) { change ->
//                                launch {
//                                    anchor.snapTo(change.position)
//                                }
//                            }
//                        }
//                        onPressed(false)
//                    }
//                }
//            }
//            .padding(
//                PaddingValues(
//                    horizontal = 24.dp,
//                    vertical = 16.dp
//                )
//            ),
//    ) {
//        ProvideTextStyle(
//            value = MaterialTheme.typography.button
//        ) {
//            Text(
//                color = Color.White,
//                text = "Cancel booking".uppercase(),
//            )
//        }
//    }
//}
