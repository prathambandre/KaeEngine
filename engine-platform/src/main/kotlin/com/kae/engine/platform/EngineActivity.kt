package com.kae.engine.platform

import android.annotation.SuppressLint
import android.app.Activity
import android.content.pm.ActivityInfo
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Build
import android.os.Bundle
import android.view.MotionEvent
import android.view.SurfaceHolder
import android.view.View
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import com.kae.engine.assets.AssetManager
import com.kae.engine.core.Engine
import com.kae.engine.core.EngineConfig
import com.kae.engine.input.InputManager
import com.kae.engine.audio.AudioManager
import com.kae.engine.scene.SceneManager

@SuppressLint("MissingSuperCall")
abstract class EngineActivity : AppCompatActivity(), SurfaceHolder.Callback, SensorEventListener {

    lateinit var engineView: EngineView
    lateinit var engine: Engine
    lateinit var inputManager: InputManager
    lateinit var assetManager: AssetManager
    lateinit var audioManager: AudioManager
    val sceneManager: SceneManager get() = engine.sceneManager

    private var sensorManager: SensorManager? = null
    private var accelerometer: Sensor? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE

        inputManager = InputManager()
        assetManager = AssetManager(this)
        audioManager = AudioManager(this)

        val config = EngineConfig()
        engine = onInitializeEngine(config)

        engineView = EngineView(this)
        engineView.initialize(engine)
        setContentView(engineView)

        sensorManager = getSystemService(SENSOR_SERVICE) as? SensorManager
        accelerometer = sensorManager?.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

        onEngineCreate()
    }

    override fun onResume() {
        super.onResume()
        engineView.onResume()
        engine.resume()
        sensorManager?.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_GAME)
        onResumeEngine()
    }

    override fun onPause() {
        super.onPause()
        engineView.onPause()
        engine.pause()
        sensorManager?.unregisterListener(this)
        onPauseEngine()
    }

    override fun onDestroy() {
        super.onDestroy()
        engine.shutdown()
        audioManager.destroy()
        onDestroyEngine()
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        val pointerIndex = event.actionIndex
        val pointerId = event.getPointerId(pointerIndex)

        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN, MotionEvent.ACTION_POINTER_DOWN -> {
                inputManager.onTouchEvent(
                    InputManager.ACTION_DOWN,
                    event.getX(pointerIndex),
                    event.getY(pointerIndex),
                    pointerId
                )
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_POINTER_UP -> {
                inputManager.onTouchEvent(
                    InputManager.ACTION_UP,
                    event.getX(pointerIndex),
                    event.getY(pointerIndex),
                    pointerId
                )
            }
            MotionEvent.ACTION_MOVE -> {
                for (i in 0 until event.pointerCount) {
                    val id = event.getPointerId(i)
                    inputManager.onTouchEvent(
                        InputManager.ACTION_MOVE,
                        event.getX(i),
                        event.getY(i),
                        id
                    )
                }
            }
            MotionEvent.ACTION_CANCEL -> {
                for (i in 0 until event.pointerCount) {
                    val id = event.getPointerId(i)
                    inputManager.onTouchEvent(
                        InputManager.ACTION_CANCEL,
                        event.getX(i),
                        event.getY(i),
                        id
                    )
                }
            }
        }
        return true
    }

    override fun onSensorChanged(event: SensorEvent) {
        if (event.sensor.type == Sensor.TYPE_ACCELEROMETER) {
            inputManager.accelerometerX = event.values[0]
            inputManager.accelerometerY = event.values[1]
            inputManager.accelerometerZ = event.values[2]
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

    override fun surfaceCreated(holder: SurfaceHolder) {
        engine.start()
    }

    override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {
        engine.renderer?.setViewport(width, height)
    }

    override fun surfaceDestroyed(holder: SurfaceHolder) {
        engine.stop()
    }

    open fun onEngineCreate() {}
    open fun onResumeEngine() {}
    open fun onPauseEngine() {}
    open fun onDestroyEngine() {}
    abstract fun onInitializeEngine(config: EngineConfig): Engine
    abstract fun onUpdate(deltaTime: Float)

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) {
            hideSystemUI()
        }
    }

    private fun hideSystemUI() {
        @Suppress("DEPRECATION")
        window.decorView.systemUiVisibility = (
            View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_FULLSCREEN
            )
    }

    fun requestPermissionsCompat(permissions: Array<String>, requestCode: Int) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(permissions, requestCode)
        }
    }
}
