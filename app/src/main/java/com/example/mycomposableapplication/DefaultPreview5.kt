package com.example.mycomposableapplication

import android.graphics.Paint
import android.graphics.Typeface
import android.os.Build
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationVector2D
import androidx.compose.animation.core.AnimationVector3D
import androidx.compose.animation.core.TweenSpec
import androidx.compose.animation.core.TwoWayConverter
import androidx.compose.animation.core.VectorConverter
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.drag
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Button
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
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch


const val INITIAL = 1
const val DRAG = 2
const val FINAL = 3
const val DISPOSE = 4

const val ANIMATION_DURATION_MS = 300

val cancelCallback: () -> Unit = {
    println("### start cancel request")
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun DefaultPreviewY() {
    val anchor = remember { Animatable(Offset.Zero, Offset.VectorConverter) }
    val textOffset = remember { Animatable(DpOffset.Zero, DpOffset.VectorConverter) }

    var cancellingState by remember { mutableStateOf(INITIAL) }
    var buttonTouchPosition by remember { mutableStateOf(Offset.Zero) }
    var isCancelButtonVisible by remember { mutableStateOf(true) }
    var isCancelButtonDisposed by remember { mutableStateOf(false) }
    var text by remember { mutableStateOf("Cancel booking") }

    var cancelButtonParams by remember {
        mutableStateOf(ParentPositionParams(Offset.Zero, IntSize(0, 0)))
    }
    var dropAreaParams by remember {
        mutableStateOf(ParentPositionParams(Offset.Zero, IntSize(0, 0)))
    }

    val initFloatingParams = FloatingParams(1f, 1f, 0f)
    val floatingButtonParams = remember { Animatable(initFloatingParams, FloatingParamsConverter) }

    val screenHeight = LocalConfiguration.current.screenHeightDp.dp

    LaunchedEffect(cancellingState) {
//        println("### cancellingState: $cancellingState")
        when (cancellingState) {
            DRAG -> {
                launch {
                    val scaleFactor = 1.2f
                    floatingButtonParams.animateTo(
                        FloatingParams(scaleFactor, scaleFactor, 1f),
                        animationSpec = TweenSpec(ANIMATION_DURATION_MS)
                    )
                }
                text = "Cancel booking"
            }

            FINAL -> {
                launch {
                    floatingButtonParams.animateTo(
                        initFloatingParams.copy(overlayAlpha = 1f),
                        animationSpec = TweenSpec(ANIMATION_DURATION_MS)
                    )
                }
                text = "Drop to cancel"
            }

            INITIAL -> {
                launch {
                    floatingButtonParams.animateTo(initFloatingParams, animationSpec = TweenSpec(ANIMATION_DURATION_MS))
                }
                launch {
                    anchor.animateTo(buttonTouchPosition, animationSpec = TweenSpec(ANIMATION_DURATION_MS))
                    isCancelButtonVisible = true
                }
                text = "Cancel booking"
            }

            DISPOSE -> {
                val scaleFactor = 30f
                launch {
                    floatingButtonParams.animateTo(
                        FloatingParams(scaleFactor, scaleFactor, 1f),
                        animationSpec = TweenSpec(700)
                    )
                    isCancelButtonDisposed = true
                }

                launch {
                    textOffset.animateTo(
                        DpOffset(0.dp, screenHeight / 2 - 50.dp),
                        animationSpec = TweenSpec(ANIMATION_DURATION_MS * 2)
                    )
                }

                launch {
                    cancelCallback.invoke()
                }

                text = "Canceling..."
            }
        }
    }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.BottomEnd
    ) {

        Row {
            Button(
                shape = RoundedCornerShape(64.dp),
                modifier = Modifier
                    .padding(vertical = 100.dp)
                    .size(80.dp, 48.dp)
                    .background(
                        color = Color.Black,
                        shape = CircleShape
                    ),
                onClick = { }) {
                ProvideTextStyle(
                    value = MaterialTheme.typography.button
                ) {
                    Text(
                        color = Color.White,
                        text = "Stub".uppercase(),
                    )
                }
            }

            StaticCancelButton(
                cancelButtonParams,
                anchor,
                { isPressed ->
                    cancellingState = when {
                        isPressed -> DRAG
                        cancellingState == DRAG -> INITIAL
                        else -> DISPOSE
                    }
                    isCancelButtonVisible = false
                },
                {
                    buttonTouchPosition = it
                },
                {
                    cancelButtonParams = it

                    val pointer = cancelButtonParams.offset.plus(anchor.value)
                    val isHit = with(dropAreaParams) {
                        pointer.y > offset.y - 32
                    }
                    if (cancellingState != DISPOSE) {
                        if (isHit) {
                            if (cancellingState == DRAG) {
                                cancellingState = FINAL
                            }
                        } else {
                            if (cancellingState == FINAL) {
                                cancellingState = DRAG
                            }
                        }
                    }
                },
                isCancelButtonVisible,
                isCancelButtonDisposed,
                { desiredOffset ->

                    println("### drop area params: $dropAreaParams")

                    val hitDelta = 32
                    val pointer = cancelButtonParams.offset.plus(desiredOffset)
                    val isHit = with(dropAreaParams) {
                        pointer.y > offset.y - hitDelta
                    }
                    if (isHit) {
                        Offset(
                            x = (dropAreaParams.size.width - cancelButtonParams.size.width) / 2f,
                            y = dropAreaParams.size.height / 2
                                + cancelButtonParams.size.height
                                + cancelButtonParams.offset.y
                                + hitDelta
                        ).minus(cancelButtonParams.offset).plus(buttonTouchPosition)
                    } else {
                        desiredOffset
                    }
                }
            )
        }

        FloatingCancelButton(
            anchor,
            cancelButtonParams,
            floatingButtonParams,
            { dropAreaParams = it },
            buttonTouchPosition,
            text.uppercase(),
            textOffset
        )
    }
}

private data class ParentPositionParams(
    val offset: Offset,
    val size: IntSize,
)

data class FloatingParams(
    val scaleX: Float,
    val scaleY: Float,
    val overlayAlpha: Float
)

object FloatingParamsConverter : TwoWayConverter<FloatingParams, AnimationVector3D> {
    override val convertFromVector: (AnimationVector3D) -> FloatingParams
        get() = { vector: AnimationVector3D ->
            FloatingParams(scaleX = vector.v1, scaleY = vector.v2, overlayAlpha = vector.v3)
        }

    override val convertToVector: (FloatingParams) -> AnimationVector3D
        get() = { params: FloatingParams ->
            AnimationVector3D(params.scaleX, params.scaleY, params.overlayAlpha)
        }
}


@OptIn(ExperimentalAnimationApi::class)
@Composable
private fun FloatingCancelButton(
    anchor: Animatable<Offset, AnimationVector2D>,
    params: ParentPositionParams,
    floatingParams: Animatable<FloatingParams, AnimationVector3D>,
    onParamsChanged: (ParentPositionParams) -> Unit,
    touch: Offset,
    text: String,
    textOffset: Animatable<DpOffset, AnimationVector2D>,
) {
    val topLeftBase = params.offset.plus(anchor.value).minus(touch)

    Canvas(modifier = Modifier.fillMaxSize()) {
        val overlayAlpha = (0x9f * floatingParams.value.overlayAlpha).toInt()
        drawRect(Color(0xff, 0xff, 0xff, overlayAlpha))
    }

    ProvideTextStyle(value = MaterialTheme.typography.button) {
        val textAlpha = (0xff * floatingParams.value.overlayAlpha).toInt()
        val textColor = Color(0, 0, 0, textAlpha)
        Text(
            text = "Hold to cancel".uppercase(),
            color = textColor,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 32.dp)
                .composed {
                    onGloballyPositioned {
                        onParamsChanged.invoke(
                            ParentPositionParams(offset = it.positionInRoot(), size = it.size)
                        )
                    }
                }
        )
    }

    Canvas(modifier = Modifier.fillMaxSize()) {
        drawRoundRect(
            color = Color.Red,
            topLeft = topLeftBase.copy(
                x = topLeftBase.x - params.size.width * (floatingParams.value.scaleX - 1) / 2,
                y = topLeftBase.y - params.size.height * (floatingParams.value.scaleY - 1) / 2
            ),
            size = Size(
                width = params.size.width * floatingParams.value.scaleX,
                height = params.size.height * floatingParams.value.scaleY
            ),
            cornerRadius = CornerRadius(64.dp.toPx(), 64.dp.toPx())
        )

        val sizePx = 14f.sp.toPx()
        val paint = Paint().apply {
            isAntiAlias = true
            textAlign = Paint.Align.CENTER
            textSize = sizePx
            typeface = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                Typeface.create(null, FontWeight.Medium.weight, false)
            } else {
                Typeface.create(Typeface.MONOSPACE, Typeface.NORMAL)
            }
            letterSpacing = 0.078f
            color = Color.White.toArgb()
        }

        drawContext.canvas.nativeCanvas.drawText(
            text,
            topLeftBase.x + params.size.width / 2,
            topLeftBase.y + params.size.height / 2 - (paint.descent() + paint.ascent()) / 2 - textOffset.value.y.toPx(),
            paint
        )
    }
}

@Composable
private fun StaticCancelButton(
    buttonParams: ParentPositionParams,
    anchor: Animatable<Offset, AnimationVector2D>,
    onPressed: (Boolean) -> Unit,
    onTouch: (Offset) -> Unit,
    onParamsChanged: (ParentPositionParams) -> Unit,
    isVisible: Boolean,
    isDisposed: Boolean,

    dropAreaSnapper: (Offset) -> Offset
) {
    if (isDisposed) return

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .padding(
                horizontal = 48.dp,
                vertical = 100.dp
            )
            .composed {
                if (isVisible) {
                    background(
                        color = Color.Red,
                        shape = RoundedCornerShape(64.dp)
                    )
                } else {
                    this
                }.onGloballyPositioned {
                    onParamsChanged.invoke(
                        buttonParams.copy(offset = it.positionInRoot(), size = it.size)
                    )
                }
            }
            .pointerInput(Unit) {
                coroutineScope {
                    while (true) {
                        val pointer = awaitPointerEventScope {
                            awaitFirstDown().also {
                                val touchPosition = it.position
                                if (isVisible) {
                                    launch {
                                        anchor.snapTo(touchPosition)
                                    }
                                    onTouch.invoke(it.position)
                                    onPressed(true)
                                }
                            }
                        }
                        awaitPointerEventScope {
                            if (isVisible) {
                                drag(pointer.id) { change ->
                                    launch {
                                        val newPosition = dropAreaSnapper(change.position)
                                        if (newPosition != change.position) {
                                            anchor.animateTo(newPosition)
                                        } else {
                                            anchor.snapTo(newPosition)
                                        }
                                    }
                                }
                            }
                        }
                        if (isVisible) {
                            onPressed(false)
                        }
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
//                letterSpacing = 0.078.em,
                color = if (isVisible) Color.White else Color.Transparent,
                text = "Cancel booking".uppercase(),
            )
        }
    }
}
