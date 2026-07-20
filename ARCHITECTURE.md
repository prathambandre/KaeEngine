# KaeEngine Architecture

This document provides a comprehensive technical overview of KaeEngine's internal architecture, covering all major subsystems, design decisions, and data flow.

## Table of Contents

1. [Overview](#1-overview)
2. [Module Structure](#2-module-structure)
3. [ECS Architecture](#3-ecs-architecture)
4. [Rendering Pipeline](#4-rendering-pipeline)
5. [Physics Simulation](#5-physics-simulation)
6. [Game Loop](#6-game-loop)
7. [Input System](#7-input-system)
8. [Asset Pipeline](#8-asset-pipeline)
9. [Audio System](#9-audio-system)
10. [Platform Integration](#10-platform-integration)
11. [Data Flow Diagram](#11-data-flow-diagram)
12. [Memory Management](#12-memory-management)

---

## 1. Overview

### Design Philosophy

KaeEngine follows a **data-oriented, modular** design philosophy. Rather than deep inheritance hierarchies, the engine composes behavior from independent subsystems communicating through well-defined interfaces. The core principles are:

- **Composition over inheritance** — Game objects are assembled from `Component` instances, not class hierarchies.
- **Separation of concerns** — Each Gradle module owns a single responsibility (rendering, physics, input, etc.).
- **Decoupled subsystems** — The physics loop runs independently from the render loop via a fixed-timestep accumulator.
- **Platform abstraction** — Pure Kotlin modules (`engine-math`, `engine-core`, `engine-scene`) have zero Android dependencies, enabling future desktop ports.
- **Performance by default** — Sprite batching, quadtree spatial partitioning, object pooling, and texture atlasing are built-in, not afterthoughts.

### Design Principles

1. **Interface-driven** — Key extension points (`System`, `Component`, `RendererInterface`) are interfaces, allowing custom implementations.
2. **Minimal coupling** — Modules depend only on the layers below them; no circular dependencies.
3. **Predictable execution** — Systems execute in priority order; the game loop guarantees deterministic update ordering.
4. **Resource safety** — All GPU resources (shaders, textures, VBOs) have explicit lifecycle management via `destroy()`/`dispose()` methods.

---

## 2. Module Structure

### engine-math

**Responsibility:** Pure Kotlin math primitives — no Android or third-party dependencies.

| Type | Description |
|------|-------------|
| `Vec2`, `Vec3`, `Vec4` | Immutable-style 2D/3D/4D vectors with operator overloads |
| `Mat4` | 4x4 matrix for transformations (projection, view, model) |
| `Quaternion` | Rotation representation avoiding gimbal lock |
| `Transform` | Position + rotation + scale composite |
| `MathUtils` | Clamp, lerp, interpolation, degree/radian conversion |

All math types use Kotlin operator overloading (`+`, `-`, `*`, `dot`, `cross`) for expressive, readable code. Matrices use column-major layout matching OpenGL conventions.

### engine-core

**Responsibility:** Engine lifecycle, system orchestration, time management, events, and object pooling.

| Class | Purpose |
|-------|---------|
| `Engine` | Top-level orchestrator — owns the world, systems list, and main loop |
| `System` | Interface for all update/render systems with priority ordering |
| `TimeManager` | Fixed-timestep accumulator with FPS tracking and pause support |
| `EngineConfig` | Immutable configuration (resolution, FPS target, physics settings) |
| `RendererInterface` | Abstract rendering backend contract |
| `EventBus` | Publish/subscribe event system with type-safe handlers |
| `ObjectPool<T>` | Generic object pool to reduce GC pressure |
| `Logger` | Singleton logger with debug/info/warn/error levels |

### engine-scene

**Responsibility:** Entity-Component-System foundation.

| Class | Purpose |
|-------|---------|
| `World` | Entity container — stores all game objects |
| `Component` | Interface for all attachable behaviors/data |

### engine-render

**Responsibility:** OpenGL ES 3.x rendering pipeline.

| Class | Purpose |
|-------|---------|
| `Renderer` | OpenGL state management, default shaders, sprite batch integration |
| `SpriteBatch` | Batched 2D sprite rendering with VAO/VBO/EBO |
| `RenderBatch` | CPU-side vertex data accumulation (up to 10,000 sprites) |
| `Shader` | GLSL shader compilation, linking, uniform setting |
| `Texture` | OpenGL texture loading and binding |
| `TextureAtlas` | Region-based sub-texture definitions |
| `OrthographicCamera` | 2D orthographic projection with zoom |
| `PerspectiveCamera` | 3D perspective projection |
| `Mesh` | Vertex/index buffer management for 3D geometry |
| `MeshRenderer` | Draw call management with model/view/projection matrices |
| `DebugRenderer` | Wireframe overlay for physics/debug visualization |
| Components | `SpriteComponent`, `MeshComponent`, `Material`, `CameraComponent` |

### engine-physics

**Responsibility:** 2D rigid-body physics simulation.

| Class | Purpose |
|-------|---------|
| `PhysicsWorld` | Simulation step — integration, broad phase, narrow phase, resolution |
| `PhysicsSystem` | ECS System wrapper with its own fixed-timestep accumulator |
| `RigidBody2D` | Mass, velocity, forces, damping, sleep state |
| `Collider2D` | Abstract base for `BoxCollider2D`, `CircleCollider2D`, `CapsuleCollider2D` |
| `CollisionDetector` | AABB-AABB, circle-circle, AABB-circle, SAT rotated-box, capsule tests |
| `CollisionResolver` | Impulse-based resolution with friction and positional correction |
| `Quadtree` | Spatial partitioning for broad-phase candidate generation |
| `AABB` | Axis-aligned bounding box with intersection, containment, merge |
| Components | `Transform2D`, `PhysicsMaterial`, `GravityComponent`, `CollisionFilter`, `SensorTag` |

### engine-input

**Responsibility:** Touch, gesture, accelerometer, and gamepad input abstraction.

| Class | Purpose |
|-------|---------|
| `InputManager` | Central input state — touch points, gesture flags, accelerometer axes |
| `GestureDetector` | Tap, double-tap, long-press, pinch, and swipe recognition |
| `AccelerometerInput` | Smoothed accelerometer data with configurable filtering |
| `GamepadInput` | Bluetooth gamepad axis/button mapping |

### engine-assets

**Responsibility:** Async asset loading, caching, and scene deserialization.

| Class | Purpose |
|-------|---------|
| `AssetManager` | Central asset registry — load, cache, unload, retrieve |
| `AssetLoader` | Background thread loading for textures, audio, JSON, shaders |
| `TextureLoader` | Bitmap decoding and OpenGL texture upload |
| `SceneLoader` | JSON scene file parsing into World entities |
| `AssetCache` | LRU eviction policy for loaded assets |

### engine-audio

**Responsibility:** Sound effects and music playback.

| Class | Purpose |
|-------|---------|
| `AudioManager` | SoundPool (SFX) and MediaPlayer (music) wrapper |
| `MusicPlayer` | Background music loop management |
| `SpatialAudio` | 2D positional audio panning and distance attenuation |
| `SoundComponent` | ECS component for entity-attached sounds |

### engine-platform

**Responsibility:** Android Activity/Surface integration and device utilities.

| Class | Purpose |
|-------|---------|
| `EngineActivity` | Abstract Activity — lifecycle, touch forwarding, sensor registration |
| `EngineView` | GLSurfaceView — EGL setup, render thread, frame timing |
| `DeviceCapabilities` | GPU/renderer info queries |
| `ScreenUtils` | Display metrics and density utilities |
| `FileUtils` | Internal/external storage path resolution |
| `PermissionHelper` | Runtime permission request wrappers |

---

## 3. ECS Architecture

### Entity Design

Entities in KaeEngine are lightweight identifiers stored in a `World`. Each entity is represented as a mutable map of named components:

```kotlin
// Entities are generic maps — no rigid class hierarchy
val entity = mutableMapOf<String, Any>()
entity["name"] = "player"
entity["components"] = mapOf(
    "Transform2D" to mapOf("x" to 100f, "y" to 200f),
    "RigidBody2D" to mapOf("mass" to 1f, "restitution" to 0.5f)
)
world.addEntity(entity)
```

The `World` class stores entities as `List<Any>`, and systems query for entities by type using `filterIsInstance<T>()`.

### Component Storage

Components are interfaces implementing `Component`:

```kotlin
interface Component {
    fun initialize() {}
    fun update(deltaTime: Float) {}
    fun render(interpolation: Float) {}
    fun destroy() {}
}
```

Concrete components include `RigidBody2D`, `BoxCollider2D`, `SpriteComponent`, `Transform2D`, `PhysicsMaterial`, etc. Each component holds its own data and optional lifecycle callbacks.

### System Execution Order

Systems implement the `System` interface and are sorted by priority before each update cycle:

```kotlin
interface System {
    fun initialize() {}
    fun update(deltaTime: Float) {}
    fun render(interpolation: Float) {}
    fun shutdown() {}
    fun getPriority(): Int = 0
}
```

The `Engine` class sorts systems by priority on each frame:

```kotlin
private fun flushSystemChanges() {
    // ... add/remove pending systems ...
    systems.sortBy { it.getPriority() }
}
```

**Priority assignments:**

| System | Priority | Rationale |
|--------|----------|-----------|
| `PhysicsSystem` | -10 | Must run first — updates positions |
| Custom game systems | 0 | Default — game logic runs after physics |
| `RenderSystem` | 100 | Runs last — draws current state |

### Query Mechanism

Systems query the `World` for entities with specific component types:

```kotlin
// In a system's update/render method:
val entities = world.getEntities()
val sprites = entities.filterIsInstance<SpriteComponent>()
    .sortedBy { it.layer }
```

This approach is simple and sufficient for the engine's target scale (hundreds to low thousands of entities). For larger entity counts, an archetype-based storage system could be added as an optimization.

---

## 4. Rendering Pipeline

### OpenGL ES 3.x Setup

The `EngineView` (a `GLSurfaceView` subclass) configures the EGL context:

```kotlin
setEGLContextClientVersion(3)          // OpenGL ES 3.0
setEGLConfigChooser(8, 8, 8, 8, 16, 0) // RGBA8888, depth 16
renderMode = RENDERMODE_CONTINUOUSLY     // Continuous rendering
```

On surface creation, the renderer enables alpha blending and disables depth testing (2D mode):

```kotlin
GLES30.glEnable(GLES30.GL_BLEND)
GLES30.glBlendFunc(GLES30.GL_SRC_ALPHA, GLES30.GL_ONE_MINUS_SRC_ALPHA)
GLES30.glDisable(GLES30.GL_DEPTH_TEST)
```

### Shader Compilation and Linking

The `Shader` class wraps GLSL compilation:

```kotlin
class Shader(vertexSource: String, fragmentSource: String) {
    var programId: Int = GLUtils.createProgram(vertexSource, fragmentSource)
    private val uniformCache = HashMap<String, Int>()

    fun use() { GLES30.glUseProgram(programId) }

    fun setMat4(name: String, matrix: Mat4) {
        val location = getUniformLocation(name)
        // ... upload 4x4 float matrix via glUniformMatrix4fv
    }

    private fun getUniformLocation(name: String): Int {
        return uniformCache.getOrPut(name) {
            GLES30.glGetUniformLocation(programId, name)
        }
    }
}
```

The default sprite shader (`Renderer.kt:15-41`) uses:
- **Vertex attributes:** `aPosition` (vec2), `aTexCoord` (vec2), `aColor` (vec4)
- **Uniforms:** `uProjection` (mat4), `uView` (mat4), `uTexture` (sampler2D)
- **Fragment:** Texture sample multiplied by vertex color for tinting

### Sprite Batching

`SpriteBatch` is the core 2D renderer, batching up to 10,000 sprites into a single draw call per texture:

**Vertex layout (interleaved, 32 bytes per vertex):**

| Offset | Attribute | Type |
|--------|-----------|------|
| 0 | Position (x, y) | 2 × float |
| 8 | TexCoord (u, v) | 2 × float |
| 16 | Color (r, g, b, a) | 4 × float |

**Batching algorithm:**

1. `begin(camera)` — Reset the vertex buffer, bind the camera's projection/view matrices.
2. `draw(texture, ...)` — If the texture differs from the current batch texture, `flush()` first. Then append 4 vertices (quad) to the `RenderBatch`.
3. `end()` — Flush any remaining sprites.

**Flush cycle:**

1. Upload vertex data to the VBO via `glBufferSubData`.
2. Bind the current texture to unit 0.
3. Issue `glDrawElements(GL_TRIANGLES, spriteCount * 6, ...)`.
4. Reset the batch.

The index buffer is pre-generated at initialization — each sprite uses 6 indices (2 triangles), with vertices indexed as `[0,1,2, 2,3,0]`.

**Rotation support:** When `rotation != 0`, `addRotatedSprite()` applies a 2D rotation matrix to each vertex around the specified origin before uploading.

### Camera System

**OrthographicCamera** (2D):
- Defines a view frustum via `left/right/bottom/top` bounds.
- Supports zoom via `zoomBy(factor)` and `zoomTo(z)`.
- Generates `projectionMatrix = Mat4.ortho(...)` and `viewMatrix = Mat4.translation(-position)`.

**PerspectiveCamera** (3D):
- Standard perspective projection with FOV, aspect ratio, near/far planes.
- Look-at view matrix generation.

### Mesh Rendering

`MeshRenderer` draws 3D geometry with full MVP matrix support:

```kotlin
fun render(mesh: Mesh, shader: Shader, modelMatrix: Mat4, camera: Camera) {
    shader.use()
    shader.setMat4("uProjection", camera.getProjectionMatrix())
    shader.setMat4("uView", camera.getViewMatrix())
    shader.setMat4("uModel", modelMatrix)
    mesh.bind()
    GLES30.glDrawElements(GL_TRIANGLES, mesh.indexCount, GL_UNSIGNED_SHORT, 0)
    mesh.unbind()
}
```

Instanced rendering is supported via `renderInstanced()` for repeated geometry.

### Render Pipeline Flowchart

```
EngineGLRenderer.onDrawFrame()
  │
  ├─ Calculate deltaTime
  │
  └─ Engine.mainLoop(deltaTime)
       │
       ├─ TimeManager.update(deltaTime)
       │   └─ Returns number of fixed steps
       │
       ├─ [For each step]
       │   └─ Engine.update(fixedDeltaTime)
       │       └─ For each System (sorted by priority):
       │           └─ system.update(dt)
       │               └─ PhysicsSystem: integrate → broad phase → narrow phase → resolve
       │
       └─ Engine.render(interpolation)
           ├─ Renderer.beginFrame()   → glClear
           ├─ For each System:
           │   └─ system.render(interpolation)
           │       └─ RenderSystem.render(world, interp):
           │           ├─ Sort sprites by layer
           │           ├─ SpriteBatch.begin(camera)
           │           ├─ For each SpriteComponent:
           │           │   └─ SpriteBatch.draw(texture, x, y, w, h, ...)
           │           └─ SpriteBatch.end() → flush → glDrawElements
           └─ Renderer.endFrame()
```

---

## 5. Physics Simulation

### Fixed Timestep Integration

The `PhysicsSystem` maintains its own accumulator separate from the engine's `TimeManager`:

```kotlin
override fun update(deltaTime: Float) {
    accumulator += deltaTime
    var steps = 0
    while (accumulator >= fixedDeltaTime && steps < MAX_STEPS) {
        world.step(fixedDeltaTime)
        accumulator -= fixedDeltaTime
        steps++
    }
}
```

This ensures physics always advances at a fixed rate (default 60 Hz) regardless of frame rate.

### Semi-Implicit Euler Integration

The `PhysicsWorld.integrate()` method applies forces to velocity before updating position (semi-implicit Euler, more stable than explicit Euler):

```
1. Apply gravity:        velocity += gravity * gravityScale * dt
2. Apply accumulated:    velocity += force * inverseMass * dt
3. Apply torque:         angularVelocity += torque * inverseMass * dt
4. Apply damping:        velocity *= 0.999
5. Clear forces for next frame
6. Sleep check:          If velocity < threshold for 60 frames → sleep
```

### Broad Phase (Quadtree)

The quadtree provides O(n log n) candidate pair generation instead of O(n²):

```
1. Clear quadtree
2. For each body: insert into quadtree with AABB bounds
3. For each body: query quadtree with its AABB → candidate list
4. For each candidate pair: run narrow phase
```

The quadtree subdivides when a node exceeds `maxEntities` (8) and depth < `maxDepth` (5). Nodes split into four quadrants (NE, NW, SW, SE), and entities are re-inserted into children that fully contain them.

### Narrow Phase

`CollisionDetector` dispatches based on collider type pairs:

| Pair | Algorithm |
|------|-----------|
| Circle vs Circle | Distance check |
| AABB vs AABB | Overlap test |
| AABB vs Circle | Closest-point-on-AABB to circle center |
| Rotated Box vs Rotated Box | SAT (Separating Axis Theorem) |
| Capsule vs Circle | Closest-point-on-segment |
| Capsule vs Capsule | Segment-segment closest points |
| Capsule vs Box | Edge closest-point tests |

All collision tests return a `CollisionResult` containing contact points with penetration depth and collision normal.

### Impulse-Based Resolution

`CollisionResolver.resolve()` applies impulse-based velocity correction:

```
1. Relative velocity along collision normal
2. If separating (v_n > 0) → skip
3. Impulse magnitude: j = -(1 + e) * v_n / (1/mA + 1/mB)
4. Apply impulse: vA -= j * normal * inverseMassA
                  vB += j * normal * inverseMassB
5. Friction impulse (Coulomb's law clamped to max friction)
6. Positional correction (prevent sinking with slop and percentage)
```

**Positional correction** uses the constant-offset approach:
```
correction = max(penetration - slop, 0) / invMassSum * percent * normal
posA -= correction * invMassA
posB += correction * invMassB
```

### Collision Events

The `PhysicsWorld` fires collision lifecycle callbacks:

```kotlin
var onCollisionEnter: ((Entity, Entity, List<ContactPoint>) -> Unit)? = null
var onCollisionStay:  ((Entity, Entity, List<ContactPoint>) -> Unit)? = null
var onCollisionExit:  ((Entity, Entity) -> Unit)? = null
```

These track collision pairs across frames to detect new contacts,持续 contacts, and separation.

### Body Types

| Type | Mass | Responds to Forces | Moves |
|------|------|-------------------|-------|
| `STATIC` | 0 | No | No |
| `DYNAMIC` | > 0 | Yes | Yes |
| `KINEMATIC` | 0 | No | Yes (velocity-driven) |

---

## 6. Game Loop

### Fixed Timestep Accumulator Pattern

KaeEngine uses the classic fixed-timestep pattern to decouple physics from rendering:

```kotlin
// In Engine.mainLoop(deltaTime):
fun mainLoop(deltaTime: Float) {
    val steps = time.update(deltaTime)        // Returns step count
    val interpolation = time.getInterpolation() // For render smoothing

    repeat(steps) {
        update(time.fixedDeltaTime)            // Fixed-rate logic
    }

    render(interpolation)                      // Variable-rate rendering
}
```

**TimeManager accumulator logic:**

```
accumulator += realDeltaTime
while (accumulator >= fixedTimeStep && steps < maxFrameSkip):
    accumulator -= fixedTimeStep
    steps++
```

The `maxFrameSkip` (default 5) prevents spiral-of-death when the device can't keep up.

### Render Interpolation

`TimeManager.getInterpolation()` returns:
```
interpolation = accumulator / fixedTimeStep
```

This value (0.0 to 1.0) represents how far between the last physics state and the next one the current render frame falls. Render systems can use this to interpolate entity positions for smooth visual motion even when physics runs at a lower rate than rendering.

### Decoupling Physics from Rendering

```
Frame N (real dt = 18ms, fixed dt = 16.67ms):
  accumulator: 0 + 18 = 18ms
  step 1: accumulator 18 - 16.67 = 1.33ms
  render interpolation: 1.33 / 16.67 = 0.08

Frame N+1 (real dt = 14ms):
  accumulator: 1.33 + 14 = 15.33ms
  steps: 0 (accumulator < fixedTimeStep)
  render interpolation: 15.33 / 16.67 = 0.92

Frame N+2 (real dt = 20ms):
  accumulator: 15.33 + 20 = 35.33ms
  step 1: 35.33 - 16.67 = 18.66ms
  step 2: 18.66 - 16.67 = 1.99ms
  render interpolation: 1.99 / 16.67 = 0.12
```

---

## 7. Input System

### Touch Event Processing

`EngineActivity` forwards Android `MotionEvent` data to `InputManager`:

```kotlin
override fun onTouchEvent(event: MotionEvent): Boolean {
    when (event.actionMasked) {
        ACTION_DOWN, ACTION_POINTER_DOWN ->
            inputManager.onTouchDown(pointerId, x, y)
        ACTION_UP, ACTION_POINTER_UP ->
            inputManager.onTouchUp(pointerId, x, y)
        ACTION_MOVE ->
            for (i in 0 until event.pointerCount) {
                inputManager.onTouchMove(id, x, y)
            }
    }
}
```

`InputManager` tracks up to 10 simultaneous touch points, each with current position, previous position, start position, and active state.

### Gesture Detection

`GestureDetector` runs inside `InputManager.update()` and detects:

| Gesture | Detection Logic |
|---------|----------------|
| Tap | Touch down → up within 200ms, < 10px movement |
| Double tap | Two taps within 300ms |
| Long press | Touch held > 500ms without movement |
| Pinch | Two-finger distance change exceeds threshold |
| Swipe | Quick directional movement > 50px |

### Accelerometer Integration

`EngineActivity` registers as a `SensorEventListener`:

```kotlin
sensorManager = getSystemService(SENSOR_SERVICE) as? SensorManager
accelerometer = sensorManager?.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

override fun onSensorChanged(event: SensorEvent) {
    inputManager.accelerometerX = event.values[0]
    inputManager.accelerometerY = event.values[1]
    inputManager.accelerometerZ = event.values[2]
}
```

`AccelerometerInput` provides smoothed, low-pass-filtered values for game use.

---

## 8. Asset Pipeline

### Async Loading with Thread Pool

`AssetManager.load()` delegates to `AssetLoader.loadAsync()`:

```kotlin
fun <T> load(assetPath: String, type: AssetType<T>, callback: (T?) -> Unit) {
    val existing = cache[assetPath]
    if (existing != null) {
        callback(existing as? T)  // Cache hit — immediate
        return
    }
    isLoading = true
    assetLoader.loadAsync(assetPath, type) { result ->
        if (result != null) cache[assetPath] = result as Any
        callback(result)
    }
}
```

Synchronous loading is also available via `loadSync()` for assets needed immediately at startup.

### LRU Caching

`AssetCache` implements an LRU eviction policy:

- Loaded assets are stored in a `LinkedHashMap` (insertion-ordered).
- On cache miss, the least-recently-used entry is evicted if cache exceeds capacity.
- `unload()` explicitly removes an asset and recycles Bitmap resources.
- `unloadAll()` clears the entire cache, recycling all Bitmaps.

### JSON Scene Format

`SceneLoader` parses JSON scene files into `World` entities:

```json
{
  "name": "level1",
  "entities": [
    {
      "name": "player",
      "components": {
        "Transform2D": { "x": 100, "y": 200 },
        "RigidBody2D": { "mass": 1, "restitution": 0.5 },
        "BoxCollider2D": { "halfExtents": { "x": 16, "y": 16 } }
      }
    },
    {
      "name": "ground",
      "components": {
        "Transform2D": { "x": 640, "y": 50 },
        "RigidBody2D": { "type": "STATIC" },
        "BoxCollider2D": { "halfExtents": { "x": 640, "y": 10 } }
      }
    }
  ]
}
```

The loader creates entity maps and adds them to the `World`. Game code then processes these maps to instantiate typed components.

---

## 9. Audio System

### SoundPool for SFX

`AudioManager` uses Android's `SoundPool` for low-latency sound effects:

```kotlin
soundPool = SoundPool.Builder()
    .setMaxStreams(16)
    .setAudioAttributes(
        AudioAttributes.Builder()
            .setUsage(USAGE_GAME)
            .setContentType(CONTENT_TYPE_SONIFICATION)
            .build()
    )
    .build()
```

Sounds are loaded by resource ID and referenced by name. Playback returns a stream ID for per-instance control (volume, pause, stop, rate).

### MediaPlayer for Music

Background music uses `MediaPlayer` for streaming playback:

```kotlin
mediaPlayer = MediaPlayer.create(context, resId)?.apply {
    isLooping = loop
    setVolume(volume, volume)
    start()
}
```

### 2D Spatial Audio Panning

`SpatialAudio` calculates left/right volume based on the angle between listener and sound source:

```kotlin
fun calculatePanning(listenerPos: Vec2, soundPos: Vec2, maxDistance: Float):
    Pair<Float, Float> {
    val dx = soundPos.x - listenerPos.x
    val dy = soundPos.y - listenerPos.y
    val distance = sqrt(dx * dx + dy * dy)
    val attenuation = 1f - (distance / maxDistance).coerceIn(0f, 1f)
    val angle = atan2(dy, dx)
    val normalizedAngle = (angle + PI) / (2 * PI)
    return Pair(attenuation * (1 - normalizedAngle), attenuation * normalizedAngle)
}
```

Distance attenuation follows a linear falloff with configurable rolloff exponent.

---

## 10. Platform Integration

### Android Activity Lifecycle

`EngineActivity` bridges Android lifecycle to engine lifecycle:

```
onCreate()  → Create InputManager, AssetManager, AudioManager
            → onInitializeEngine(config) [abstract — game sets up systems]
            → Create EngineView, set as content view
            → Register accelerometer sensor

onResume()  → EngineView.onResume() [resume GL thread]
            → Engine.resume()
            → Register sensor listener

onPause()   → EngineView.onPause() [pause GL thread]
            → Engine.pause()
            → Unregister sensor listener

onDestroy() → Engine.shutdown()
            → AudioManager.destroy()
```

### GLSurfaceView Rendering Thread

`EngineView` extends `GLSurfaceView` and creates a separate rendering thread:

```kotlin
private class EngineGLRenderer(private val engine: Engine) : GLSurfaceView.Renderer {
    private var startTimeNanos: Long = 0L

    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
        startTimeNanos = System.nanoTime()
        // Initialize GL state
    }

    override fun onDrawFrame(gl: GL10?) {
        val currentTimeNanos = System.nanoTime()
        val deltaTime = (currentTimeNanos - startTimeNanos) / 1_000_000_000f
        startTimeNanos = currentTimeNanos
        engine.mainLoop(deltaTime)
    }
}
```

The render mode is `RENDERMODE_CONTINUOUSLY`, calling `onDrawFrame` at the display refresh rate (typically 60 Hz). The engine's `TimeManager` handles the difference between display rate and fixed physics rate.

### Surface Creation/Destruction

```kotlin
override fun surfaceCreated(holder: SurfaceHolder) { engine.start() }
override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {
    engine.renderer.setViewport(width, height)
}
override fun surfaceDestroyed(holder: SurfaceHolder) { engine.stop() }
```

---

## 11. Data Flow Diagram

### Per-Frame Data Flow

```
┌─────────────────────────────────────────────────────────────┐
│                    Android Platform Layer                    │
│  EngineActivity → onTouchEvent/onSensorChanged               │
│  EngineView.Renderer → onDrawFrame → deltaTime              │
└──────────────────┬──────────────────────────────────────────┘
                   │
                   ▼
┌─────────────────────────────────────────────────────────────┐
│                        Engine Core                           │
│  Engine.mainLoop(deltaTime)                                  │
│  ├─ TimeManager.update(dt) → steps, interpolation           │
│  ├─ [For each step] Engine.update(fixedDt)                  │
│  │   └─ Systems.forEach { system.update(dt) }              │
│  └─ Engine.render(interpolation)                            │
│      └─ Systems.forEach { system.render(interp) }          │
└──────────────────┬──────────────────────────────────────────┘
                   │
     ┌─────────────┼─────────────┬──────────────┐
     ▼             ▼             ▼              ▼
┌──────────┐ ┌──────────┐ ┌──────────┐ ┌──────────────┐
│ Physics  │ │  Input   │ │  Audio   │ │    Render     │
│ System   │ │ Manager  │ │ Manager  │ │    System     │
│          │ │          │ │          │ │               │
│ Integrate│ │ Touch    │ │ Spatial  │ │ Sort sprites  │
│ Broad    │ │ Gestures │ │ Panning  │ │ Batch begin   │
│ Narrow   │ │ Accel    │ │ SFX/Music│ │ Draw sprites  │
│ Resolve  │ │ Gamepad  │ │          │ │ Batch end     │
└────┬─────┘ └──────────┘ └──────────┘ └───────┬───────┘
     │                                          │
     ▼                                          ▼
┌──────────────────┐              ┌──────────────────────┐
│   PhysicsWorld   │              │    OpenGL ES 3.x     │
│  (entities,      │              │   VAO/VBO/EBO        │
│   rigid bodies,  │              │   Shader program     │
│   quadtree)      │              │   Texture binding    │
└──────────────────┘              │   glDrawElements     │
                                  └──────────────────────┘
```

### Input Data Flow

```
Android MotionEvent
  │
  ▼
EngineActivity.onTouchEvent()
  │
  ▼
InputManager.onTouchDown/Move/Up()
  │
  ├─ TouchPoint[] updated (position, delta, active)
  │
  ▼
GestureDetector.update()
  │
  ├─ Tap, DoubleTap, LongPress, Pinch, Swipe detected
  │
  ▼
Game systems query InputManager each frame
  │
  ├─ inputManager.isTouching(0)
  ├─ inputManager.getTouchPosition(0)
  ├─ inputManager.isTap()
  └─ inputManager.getPinchScale()
```

### Physics Data Flow

```
PhysicsSystem.update(deltaTime)
  │
  ▼
PhysicsWorld.step(dt)
  │
  ├─ 1. INTEGRATE FORCES
  │     For each DYNAMIC body:
  │       velocity += gravity * gravityScale * dt
  │       velocity += force * inverseMass * dt
  │       angularVelocity += torque * inverseMass * dt
  │       velocity *= damping
  │       clearForces()
  │
  ├─ 2. BROAD PHASE
  │     quadtree.clear()
  │     For each body: quadtree.insert(entity, aabb)
  │
  ├─ 3. NARROW PHASE + RESOLUTION
  │     For each body:
  │       candidates = quadtree.query(body.aabb)
  │       For each candidate:
  │         result = CollisionDetector.testCollision(...)
  │         If colliding:
  │           CollisionResolver.resolve(bodyA, bodyB, contact)
  │           Apply positional correction
  │           Fire collision events (enter/stay/exit)
  │
  └─ 4. UPDATE POSITIONS
        For each DYNAMIC/KINEMATIC body:
          position += velocity * dt
          rotation += angularVelocity * dt
```

---

## 12. Memory Management

### Object Pools

`ObjectPool<T>` reduces GC pressure for frequently allocated/deallocated objects:

```kotlin
val bulletPool = ObjectPool(factory = { Bullet() }, initialCapacity = 64)
bulletPool.preload(32)  // Pre-allocate 32 bullets

// Usage:
val bullet = bulletPool.obtain()
// ... use bullet ...
bulletPool.free(bullet)
```

The pool tracks active vs. pooled counts and supports bulk operations (`freeAll`, `clear`).

### Texture Caching

`AssetManager` caches loaded `Bitmap` objects in a `Map<String, Any>`:

- On load, the texture is stored by path.
- On `unload()`, `Bitmap.recycle()` is called to free native memory.
- On `unloadAll()`, all bitmaps are recycled and the cache is cleared.

### GPU Resource Lifecycle

All OpenGL resources follow explicit creation/destruction:

| Resource | Create | Destroy |
|----------|--------|---------|
| Shader program | `GLUtils.createProgram()` | `GLUtils.deleteProgram()` |
| Texture | `glGenTextures()` | `glDeleteTextures()` |
| VAO | `glGenVertexArrays()` | `glDeleteVertexArrays()` |
| VBO/EBO | `glGenBuffers()` | `glDeleteBuffers()` |

The `Renderer.destroy()`, `SpriteBatch.dispose()`, and `Shader.destroy()` methods ensure proper cleanup when the engine shuts down.

### Asset Lifecycle

```
AssetManager.load(path, type, callback)
  │
  ├─ Cache hit → return cached object (no allocation)
  │
  └─ Cache miss → AssetLoader.loadAsync()
       │
       ├─ Decode bitmap / parse JSON / load binary
       ├─ Store in cache map
       └─ Invoke callback on main thread

AssetManager.unload(path)
  │
  ├─ Remove from cache
  └─ If Bitmap: recycle() → free native pixel data

AssetManager.shutdown()
  │
  ├─ unloadAll() → recycle all bitmaps, clear cache
  └─ AssetLoader.shutdown() → shutdown thread pool
```

### Memory Budget

| Category | Budget | Notes |
|----------|--------|-------|
| Engine core | < 10MB | Systems, world, event bus, pools |
| Textures (cached) | < 30MB | LRU eviction at boundary |
| Audio buffers | < 5MB | SoundPool streaming |
| Physics data | < 5MB | Bodies, quadtree nodes |
| **Total** | **< 50MB** | Target for mid-range devices |
