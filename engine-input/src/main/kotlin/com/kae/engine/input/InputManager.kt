package com.kae.engine.input

import com.kae.engine.math.Vec2
import kotlin.math.abs
import kotlin.math.sqrt

class InputManager {

    val touches: Array<TouchPoint> = Array(10) { TouchPoint() }
    var touchCount: Int = 0
        private set

    var isTapDetected: Boolean = false
        internal set
    var isDoubleTapDetected: Boolean = false
        internal set
    var isLongPressDetected: Boolean = false
        internal set
    var isPinchDetected: Boolean = false
        internal set
    var pinchScale: Float = 1f
        internal set
    var isSwipeDetected: Boolean = false
        internal set
    var swipeDirection: SwipeDirection = SwipeDirection.NONE
        internal set

    var accelerometerX: Float = 0f
    var accelerometerY: Float = 0f
    var accelerometerZ: Float = 0f

    var gamepadAxes: FloatArray = FloatArray(6)
        private set
    var gamepadButtons: BooleanArray = BooleanArray(16)
        private set
    var isGamepadConnected: Boolean = false
        internal set

    private val gestureDetector = GestureDetector(this)
    private val accelerometerInput = AccelerometerInput(this)
    private val gamepadInput = GamepadInput(this)

    fun update() {
        for (i in touches.indices) {
            if (touches[i].isActive) {
                touches[i].prevX = touches[i].x
                touches[i].prevY = touches[i].y
            }
        }
        gestureDetector.update()
    }

    fun reset() {
        isTapDetected = false
        isDoubleTapDetected = false
        isLongPressDetected = false
        isPinchDetected = false
        pinchScale = 1f
        isSwipeDetected = false
        swipeDirection = SwipeDirection.NONE
        touchCount = 0
        for (i in touches.indices) {
            touches[i].isActive = false
        }
    }

    fun onTouchEvent(action: Int, x: Float, y: Float, pointerId: Int) {
        if (pointerId < 0 || pointerId >= touches.size) return
        val touch = touches[pointerId]
        when (action) {
            ACTION_DOWN -> {
                touch.prevX = x
                touch.prevY = y
                touch.startX = x
                touch.startY = y
                touch.x = x
                touch.y = y
                touch.isActive = true
                touch.downTime = System.currentTimeMillis()
                if (pointerId >= touchCount) {
                    touchCount = pointerId + 1
                }
            }
            ACTION_MOVE -> {
                touch.prevX = touch.x
                touch.prevY = touch.y
                touch.x = x
                touch.y = y
            }
            ACTION_UP, ACTION_CANCEL -> {
                touch.prevX = touch.x
                touch.prevY = touch.y
                touch.x = x
                touch.y = y
                touch.isActive = false
            }
        }
    }

    fun isTouching(index: Int = 0): Boolean {
        return index in 0 until touches.size && touches[index].isActive
    }

    fun getTouchPosition(index: Int = 0): Vec2 {
        if (index < 0 || index >= touches.size) return Vec2.ZERO
        return Vec2(touches[index].x, touches[index].y)
    }

    fun getTouchDelta(index: Int = 0): Vec2 {
        if (index < 0 || index >= touches.size) return Vec2.ZERO
        return Vec2(touches[index].deltaX, touches[index].deltaY)
    }

    fun isTap(): Boolean = isTapDetected

    fun isLongPress(): Boolean = isLongPressDetected

    fun isButtonDown(button: Int): Boolean {
        if (button < 0 || button >= gamepadButtons.size) return false
        return gamepadButtons[button]
    }

    fun getAxis(axis: Int): Float {
        if (axis < 0 || axis >= gamepadAxes.size) return 0f
        return gamepadAxes[axis]
    }

    fun getGestureDetector(): GestureDetector = gestureDetector

    fun getAccelerometerInput(): AccelerometerInput = accelerometerInput

    fun getGamepadInput(): GamepadInput = gamepadInput

    companion object {
        const val ACTION_DOWN = 0
        const val ACTION_UP = 1
        const val ACTION_MOVE = 2
        const val ACTION_CANCEL = 3
    }
}

data class TouchPoint(
    var x: Float = 0f,
    var y: Float = 0f,
    var prevX: Float = 0f,
    var prevY: Float = 0f,
    var startX: Float = 0f,
    var startY: Float = 0f,
    var isActive: Boolean = false,
    var downTime: Long = 0L
) {
    val deltaX: Float get() = x - prevX
    val deltaY: Float get() = y - prevY
    val totalDeltaX: Float get() = x - startX
    val totalDeltaY: Float get() = y - startY
}

enum class SwipeDirection { NONE, UP, DOWN, LEFT, RIGHT }
