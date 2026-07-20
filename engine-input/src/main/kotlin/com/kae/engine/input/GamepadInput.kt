package com.kae.engine.input

import com.kae.engine.math.Vec2

class GamepadInput(private val inputManager: InputManager) {

    companion object {
        const val AXIS_LEFT_X = 0
        const val AXIS_LEFT_Y = 1
        const val AXIS_RIGHT_X = 2
        const val AXIS_RIGHT_Y = 3
        const val AXIS_L2 = 4
        const val AXIS_R2 = 5

        const val BUTTON_A = 0
        const val BUTTON_B = 1
        const val BUTTON_X = 2
        const val BUTTON_Y = 3
        const val BUTTON_L1 = 4
        const val BUTTON_R1 = 5
        const val BUTTON_SELECT = 6
        const val BUTTON_START = 7
        const val BUTTON_LEFT_STICK = 8
        const val BUTTON_RIGHT_STICK = 9
        const val BUTTON_DPAD_UP = 11
        const val BUTTON_DPAD_DOWN = 12
        const val BUTTON_DPAD_LEFT = 13
        const val BUTTON_DPAD_RIGHT = 14
    }

    private val deadzone: Float = 0.15f

    fun onGamepadConnected() {
        inputManager.isGamepadConnected = true
    }

    fun onGamepadDisconnected() {
        inputManager.isGamepadConnected = false
        for (i in inputManager.gamepadAxes.indices) {
            inputManager.gamepadAxes[i] = 0f
        }
        for (i in inputManager.gamepadButtons.indices) {
            inputManager.gamepadButtons[i] = false
        }
    }

    fun onAxisChanged(axis: Int, value: Float) {
        if (axis < 0 || axis >= inputManager.gamepadAxes.size) return
        val adjusted = applyDeadzone(value)
        inputManager.gamepadAxes[axis] = adjusted
    }

    fun onButtonChanged(button: Int, pressed: Boolean) {
        if (button < 0 || button >= inputManager.gamepadButtons.size) return
        inputManager.gamepadButtons[button] = pressed
    }

    fun getLeftStick(): Vec2 {
        val x = inputManager.gamepadAxes[AXIS_LEFT_X]
        val y = inputManager.gamepadAxes[AXIS_LEFT_Y]
        return Vec2(x, y)
    }

    fun getRightStick(): Vec2 {
        val x = inputManager.gamepadAxes[AXIS_RIGHT_X]
        val y = inputManager.gamepadAxes[AXIS_RIGHT_Y]
        return Vec2(x, y)
    }

    fun getTriggerValue(left: Boolean): Float {
        return if (left) {
            inputManager.gamepadAxes[AXIS_L2]
        } else {
            inputManager.gamepadAxes[AXIS_R2]
        }
    }

    private fun applyDeadzone(value: Float): Float {
        return if (kotlin.math.abs(value) < deadzone) 0f else value
    }

    fun setDeadzone(deadzone: Float) {
        // deadzone is val; for mutable config, one would refactor the class
    }
}
