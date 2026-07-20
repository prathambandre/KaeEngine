package com.kae.engine.input

import kotlin.math.abs
import kotlin.math.sqrt

class GestureDetector(private val inputManager: InputManager) {

    private var lastTapTime: Long = 0L
    private var longPressThreshold: Long = 500L
    private var doubleTapThreshold: Long = 300L
    private var swipeThreshold: Float = 100f

    private var activePointerId: Int = -1
    private var longPressTriggered: Boolean = false

    fun update() {
        detectLongPress()
        detectTap()
        detectSwipe()
        detectPinch()
    }

    fun reset() {
        lastTapTime = 0L
        longPressTriggered = false
        activePointerId = -1
    }

    private fun detectLongPress() {
        if (longPressTriggered) return
        for (i in 0 until inputManager.touchCount) {
            val touch = inputManager.touches[i]
            if (!touch.isActive) continue
            val elapsed = System.currentTimeMillis() - touch.downTime
            if (elapsed >= longPressThreshold) {
                val distX = abs(touch.x - touch.startX)
                val distY = abs(touch.y - touch.startY)
                if (distX < swipeThreshold * 0.3f && distY < swipeThreshold * 0.3f) {
                    inputManager.isLongPressDetected = true
                    longPressTriggered = true
                    return
                }
            }
        }
    }

    private fun detectTap() {
        val now = System.currentTimeMillis()
        var tapDetected = false
        for (i in 0 until inputManager.touchCount) {
            val touch = inputManager.touches[i]
            if (touch.isActive) {
                activePointerId = i
            } else if (activePointerId == i && activePointerId >= 0) {
                val elapsed = now - touch.downTime
                val distX = abs(touch.x - touch.startX)
                val distY = abs(touch.y - touch.startY)
                if (elapsed < longPressThreshold && distX < swipeThreshold * 0.3f && distY < swipeThreshold * 0.3f) {
                    tapDetected = true
                    if (now - lastTapTime < doubleTapThreshold) {
                        inputManager.isDoubleTapDetected = true
                    }
                    lastTapTime = now
                }
                activePointerId = -1
            }
        }
        inputManager.isTapDetected = tapDetected
    }

    private fun detectSwipe() {
        for (i in 0 until inputManager.touchCount) {
            val touch = inputManager.touches[i]
            if (touch.isActive) continue

            val totalDX = touch.x - touch.startX
            val totalDY = touch.y - touch.startY
            val totalDist = sqrt(totalDX * totalDX + totalDY * totalDY)
            val elapsed = System.currentTimeMillis() - touch.downTime

            if (totalDist >= swipeThreshold && elapsed < longPressThreshold) {
                inputManager.isSwipeDetected = true
                inputManager.swipeDirection = if (abs(totalDX) > abs(totalDY)) {
                    if (totalDX > 0) SwipeDirection.RIGHT else SwipeDirection.LEFT
                } else {
                    if (totalDY > 0) SwipeDirection.DOWN else SwipeDirection.UP
                }
                return
            }
        }
    }

    private fun detectPinch() {
        var activeCount = 0
        for (i in 0 until inputManager.touchCount) {
            if (inputManager.touches[i].isActive) activeCount++
        }
        if (activeCount < 2) {
            inputManager.isPinchDetected = false
            return
        }

        var firstId = -1
        var secondId = -1
        for (i in 0 until inputManager.touchCount) {
            if (inputManager.touches[i].isActive) {
                if (firstId < 0) firstId = i else { secondId = i; break }
            }
        }
        if (firstId < 0 || secondId < 0) return

        val t1 = inputManager.touches[firstId]
        val t2 = inputManager.touches[secondId]

        val currentDist = sqrt(
            (t2.x - t1.x) * (t2.x - t1.x) +
            (t2.y - t1.y) * (t2.y - t1.y)
        )
        val startDist = sqrt(
            (t2.startX - t1.startX) * (t2.startX - t1.startX) +
            (t2.startY - t1.startY) * (t2.startY - t1.startY)
        )

        if (startDist > 0f) {
            inputManager.isPinchDetected = true
            inputManager.pinchScale = currentDist / startDist
        }
    }

    fun setLongPressThreshold(thresholdMs: Long) {
        longPressThreshold = thresholdMs
    }

    fun setDoubleTapThreshold(thresholdMs: Long) {
        doubleTapThreshold = thresholdMs
    }

    fun setSwipeThreshold(thresholdPx: Float) {
        swipeThreshold = thresholdPx
    }
}
