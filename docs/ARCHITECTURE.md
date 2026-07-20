# KaeEngine — Detailed Architecture Guide with Code Examples

This document provides an in-depth walkthrough of KaeEngine's architecture with concrete code examples for every major subsystem. It is intended for contributors and developers building games on top of the engine.

---

## Table of Contents

1. [Quick Start — Setting Up a Game](#1-quick-start--setting-up-a-game)
2. [ECS Architecture Deep Dive](#2-ecs-architecture-deep-dive)
3. [Rendering Pipeline](#3-rendering-pipeline)
4. [Physics Simulation](#4-physics-simulation)
5. [Input Handling](#5-input-handling)
6. [Asset Loading](#6-asset-loading)
7. [Audio System](#7-audio-system)
8. [Platform Integration](#8-platform-integration)
9. [Rendering Pipeline Flowchart](#9-rendering-pipeline-flowchart)

---

## 1. Quick Start — Setting Up a Game

### Creating an EngineActivity

Every KaeEngine game extends `EngineActivity`, which handles the Android lifecycle, GLSurfaceView setup, touch forwarding, and sensor registration:

```kotlin
class BreakoutGame : EngineActivity() {

    private lateinit var physicsSystem: PhysicsSystem
    private lateinit var renderSystem: RenderSystem

    override fun onInitializeEngine(config: EngineConfig): Engine {
        val engine = Engine(config)

        // Create and register systems
        physicsSystem = PhysicsSystem().apply {
            gravity = Vec2(0f, -9.81f)
            fixedDeltaTime = 1f / 60f
        }
        renderSystem = RenderSystem()

        engine.addSystem(physicsSystem)
        engine.addSystem(renderSystem)

        // Set up the renderer
        engine.renderer = Renderer()

        return engine
    }

    override fun onEngineCreate() {
        // Load assets, set up the scene
        setupScene()
    }

    override fun onUpdate(deltaTime: Float) {
        // Called each frame for game-specific logic
        handleInput()
    }

    private fun setupScene() {
        val world = engine.ecsWorld

        // Create the paddle
        val paddle = mutableMapOf<String, Any>()
        paddle["name"] = "paddle"
        paddle["components"] = mapOf(
            "Transform2D" to mapOf("x" to 640f, "y" to 100f),
            "SpriteComponent" to mapOf(
                "width" to 128f, "height" to 24f,
                "color" to mapOf("x" to 0.2f, "y" to 0.6f, "z" to 1.0f, "w" to 1.0f)
            ),
            "RigidBody2D" to mapOf(
                "type" to "KINEMATIC",
                "restitution" to 1.0f
            ),
            "BoxCollider2D" to mapOf(
                "halfExtents" to mapOf("x" to 64f, "y" to 12f)
            )
        )
        world.addEntity(paddle)

        // Create the ball
        val ball = mutableMapOf<String, Any>()
        ball["name"] = "ball"
        ball["components"] = mapOf(
            "Transform2D" to mapOf("x" to 640f, "y" to 200f),
            "SpriteComponent" to mapOf(
                "width" to 16f, "height" to 16f,
                "color" to mapOf("x" to 1.0f, "y" to 1.0f, "z" to 1.0f, "w" to 1.0f)
            ),
            "RigidBody2D" to mapOf(
                "type" to "DYNAMIC",
                "mass" to 0.1f,
                "restitution" to 1.0f,
                "gravityScale" to 0f
            ),
            "CircleCollider2D" to mapOf("radius" to 8f)
        )
        world.addEntity(ball)
    }

    private fun handleInput() {
        val input = inputManager
        if (input.isTouching(0)) {
            val touchX = input.getTouchPosition(0).x
            // Move paddle to touch position
            physicsSystem.updateBodyTransform(
                paddleEntity,
                Vec2(touchX, 100f),
                0f
            )
        }
    }
}
```

### Configuring the Engine

`EngineConfig` provides sensible defaults:

```kotlin
val config = EngineConfig(
    targetFPS = 60,
    fixedTimeStep = 1f / 60f,
    width = 1280,
    height = 720,
    title = "My Game",
    clearColorR = 0.1f,
    clearColorG = 0.1f,
    clearColorB = 0.1f,
    clearColorA = 1.0f,
    enablePhysics = true,
    gravity = -9.81f,
    maxFrameSkip = 5,
    enableVSync = false
)
```

### Registering Systems

Systems are added via `engine.addSystem()` and sorted by priority:

```kotlin
// Physics runs first (priority -10)
engine.addSystem(PhysicsSystem())

// Custom game logic at default priority (0)
engine.addSystem(EnemyAISystem())
engine.addSystem(SpawningSystem())

// Rendering runs last (priority 100)
engine.addSystem(RenderSystem())
```

---

## 2. ECS Architecture Deep Dive

### The World

The `World` is a simple entity container:

```kotlin
class World {
    private val entities = mutableListOf<Any>()

    fun addEntity(entity: Any) { entities.add(entity) }
    fun removeEntity(entity: Any) { entities.remove(entity) }
    fun getEntities(): List<Any> = entities.toList()
    fun entityCount(): Int = entities.size
    fun clear() { entities.clear() }
}
```

Entities are stored as generic `Any` objects. In practice, they are `MutableMap<String, Any>` instances where keys are component type names and values are component data maps.

### The Component Interface

All components implement `Component`:

```kotlin
interface Component {
    fun initialize() {}    // Called when component is added
    fun update(dt: Float) {}  // Called each fixed timestep
    fun render(interp: Float) {}  // Called each render frame
    fun destroy() {}  // Called when removed
}
```

### The System Interface

Systems implement `System`:

```kotlin
interface System {
    fun initialize() {}
    fun update(deltaTime: Float) {}
    fun render(interpolation: Float) {}
    fun shutdown() {}
    fun getPriority(): Int = 0
}
```

### System Priority and Execution Order

The `Engine` flushes system changes and sorts by priority before each update:

```kotlin
// Engine.kt
private fun flushSystemChanges() {
    if (!systemsDirty) return

    for (system in systemsToAdd) {
        if (system !in systems) {
            systems.add(system)
        }
    }
    systemsToAdd.clear()

    for (system in systemsToRemove) {
        systems.remove(system)
    }
    systemsToRemove.clear()

    systems.sortBy { it.getPriority() }
    systemsDirty = false
}
```

**Priority assignments in the engine:**

| System | Priority | Purpose |
|--------|----------|---------|
| `PhysicsSystem` | -10 | Run physics before game logic |
| Custom systems | 0 | Game logic |
| `RenderSystem` | 100 | Draw after all updates |

### Querying Entities

Systems query the world by type:

```kotlin
class EnemyAISystem : System {
    override fun update(deltaTime: Float) {
        // Get all entities, filter for specific component types
        val entities = world.getEntities()

        // Find entities with Transform2D component
        val transformEntities = entities.filterIsInstance<Transform2D>()

        // Find entities with RigidBody2D component
        val physicsEntities = entities.filterIsInstance<RigidBody2D>()

        // Find entities with SpriteComponent
        val sprites = entities.filterIsInstance<SpriteComponent>()
            .sortedBy { it.layer }  // Sort by render layer
    }
}
```

### Creating Components

```kotlin
// Transform2D — position and rotation
val transform = Transform2D(
    position = Vec2(100f, 200f),
    rotation = 45f
)

// RigidBody2D — physics body
val body = RigidBody2D.dynamic(
    mass = 1f,
    restitution = 0.7f,
    friction = 0.3f
)

// Or use factory methods:
val staticBody = RigidBody2D.static(restitution = 0.5f)
val kinematicBody = RigidBody2D.kinematic()

// BoxCollider2D — axis-aligned box
val box = BoxCollider2D(
    halfExtents = Vec2(32f, 32f),
    offset = Vec2(0f, 0f)
)

// CircleCollider2D
val circle = CircleCollider2D(
    radius = 16f,
    offset = Vec2(0f, 0f)
)

// CapsuleCollider2D
val capsule = CapsuleCollider2D(
    tipA = Vec2(0f, -0.5f),
    tipB = Vec2(0f, 0.5f),
    radius = 0.25f
)

// SpriteComponent
val sprite = SpriteComponent(
    texture = myTexture,
    width = 64f,
    height = 64f,
    color = Vec4(1f, 0f, 0f, 1f),  // Red tint
    layer = 1,
    rotation = 30f
)

// PhysicsMaterial — preset materials
val bouncy = PhysicsMaterial.BOUNCY     // restitution=0.9, friction=0.1
val rubber = PhysicsMaterial.RUBBER     // restitution=0.8, friction=0.9
val ice = PhysicsMaterial.ICE           // restitution=0.1, friction=0.02
val slippery = PhysicsMaterial.SLIPPERY // restitution=0.3, friction=0.05
```

### Applying Forces and Impulses

```kotlin
// Continuous force (applied each frame until cleared)
body.applyForce(Vec2(100f, 0f))  // Push right

// Instant impulse (velocity change)
body.applyImpulse(Vec2(0f, 500f))  // Jump

// Torque (rotational force)
body.applyTorque(10f)  // Spin clockwise

// Clear forces (called automatically after integration)
body.clearForces()
```

### Sleep System

Bodies that have very low velocity for 60 consecutive frames are put to sleep to save CPU:

```kotlin
// In PhysicsWorld.integrate():
if (body.velocity.lengthSquared() < 0.001f &&
    kotlin.math.abs(body.angularVelocity) < 0.001f) {
    body.sleepCounter++
    if (body.sleepCounter > 60) {
        body.velocity = Vec2.ZERO
        body.angularVelocity = 0f
        body.sleep()
    }
} else {
    body.sleepCounter = 0
}
```

Sleeping bodies skip force integration and collision detection. They wake up when another body collides with them.

---

## 3. Rendering Pipeline

### Shader Setup

The default vertex shader:

```glsl
#version 300 es
layout(location = 0) in vec2 aPosition;
layout(location = 1) in vec2 aTexCoord;
layout(location = 2) in vec4 aColor;

uniform mat4 uProjection;
uniform mat4 uView;

out vec2 vTexCoord;
out vec4 vColor;

void main() {
    vTexCoord = aTexCoord;
    vColor = aColor;
    gl_Position = uProjection * uView * vec4(aPosition, 0.0, 1.0);
}
```

The default fragment shader:

```glsl
#version 300 es
precision mediump float;

in vec2 vTexCoord;
in vec4 vColor;

uniform sampler2D uTexture;
out vec4 fragColor;

void main() {
    fragColor = texture(uTexture, vTexCoord) * vColor;
}
```

### Creating Custom Shaders

```kotlin
val vertexShader = """
    #version 300 es
    layout(location = 0) in vec2 aPosition;
    layout(location = 1) in vec2 aTexCoord;
    layout(location = 2) in vec4 aColor;
    uniform mat4 uProjection;
    uniform mat4 uView;
    uniform float uTime;  // Custom uniform
    out vec2 vTexCoord;
    out vec4 vColor;
    void main() {
        vTexCoord = aTexCoord;
        vColor = aColor;
        vec2 pos = aPosition;
        pos.y += sin(aPosition.x * 0.01 + uTime) * 10.0;  // Wave effect
        gl_Position = uProjection * uView * vec4(pos, 0.0, 1.0);
    }
""".trimIndent()

val fragmentShader = """
    #version 300 es
    precision mediump float;
    in vec2 vTexCoord;
    in vec4 vColor;
    uniform sampler2D uTexture;
    uniform float uTime;
    out vec4 fragColor;
    void main() {
        vec4 texColor = texture(uTexture, vTexCoord);
        vec4 tint = vec4(
            sin(uTime) * 0.5 + 0.5,
            cos(uTime * 0.7) * 0.5 + 0.5,
            sin(uTime * 1.3) * 0.5 + 0.5,
            1.0
        );
        fragColor = texColor * vColor * tint;
    }
""".trimIndent()

val shader = Shader(vertexShader, fragmentShader)
shader.use()
shader.setFloat("uTime", totalTime)
```

### Sprite Batching

```kotlin
// Set up a camera
val camera = OrthographicCamera(1280f, 720f)
camera.position = Vec3(640f, 360f, 0f)

// Create a sprite batch
val spriteBatch = SpriteBatch(maxSprites = 10000)

// In your render method:
spriteBatch.begin(camera)

// Draw sprites (automatically batched when using the same texture)
for (i in 0 until 1000) {
    spriteBatch.draw(
        texture = brickTexture,
        x = (i % 20) * 64f,
        y = (i / 20) * 32f,
        width = 64f,
        height = 32f,
        color = Vec4(1f, 0.5f, 0.2f, 1f),  // Orange tint
        rotation = 0f
    )
}

// Draw with rotation
spriteBatch.draw(
    texture = playerTexture,
    x = playerX,
    y = playerY,
    width = 32f,
    height = 32f,
    color = Vec4.WHITE,
    rotation = playerAngle,
    originX = 0.5f,  // Rotate around center
    originY = 0.5f
)

// Draw a texture region (from atlas)
spriteBatch.draw(
    texture = atlasTexture,
    region = SpriteRegion(u = 0.0f, v = 0.0f, u2 = 0.25f, v2 = 0.25f),
    x = 100f,
    y = 100f,
    width = 64f,
    height = 64f
)

spriteBatch.end()
```

### Camera Operations

```kotlin
val camera = OrthographicCamera(1280f, 720f)

// Position the camera
camera.position = Vec3(640f, 360f, 0f)

// Zoom
camera.zoomBy(0.5f)   // Zoom in 2x
camera.zoomTo(2.0f)   // Set zoom to 2x
camera.zoomBy(2.0f)   // Zoom out 2x

// Update viewport (on screen rotation)
camera.setViewport(1920f, 1080f)

// Get matrices for custom shaders
val projectionMatrix = camera.getProjectionMatrix()
val viewMatrix = camera.getViewMatrix()
```

### Mesh Rendering

```kotlin
// Create a mesh (e.g., a triangle)
val mesh = Mesh(
    vertices = floatArrayOf(
        // x,    y,    z,    u,    v,    r, g, b, a
        -0.5f, -0.5f, 0f,   0f,   0f,   1f, 0f, 0f, 1f,
         0.5f, -0.5f, 0f,   1f,   0f,   0f, 1f, 0f, 1f,
         0.0f,  0.5f, 0f,   0.5f, 1f,   0f, 0f, 1f, 1f
    ),
    indices = shortArrayOf(0, 1, 2),
    vertexLayout = VertexLayout(
        attributes = listOf(
            VertexAttribute("aPosition", 3, 0),
            VertexAttribute("aTexCoord", 2, 12),
            VertexAttribute("aColor", 4, 20)
        ),
        stride = 36
    )
)

// Render with a model matrix
val modelMatrix = Mat4.translation(100f, 200f, 0f) *
                  Mat4.rotation(45f, Vec3(0f, 0f, 1f)) *
                  Mat4.scale(2f, 2f, 1f)

meshRenderer.render(mesh, shader, modelMatrix, camera)
```

---

## 4. Physics Simulation

### Setting Up Physics

```kotlin
// Create a PhysicsSystem
val physicsSystem = PhysicsSystem().apply {
    gravity = Vec2(0f, -9.81f)
    fixedDeltaTime = 1f / 60f
    enableDebugDraw = true  // Show collision shapes
}

engine.addSystem(physicsSystem)

// Set collision callbacks
physicsSystem.physicsWorld.onCollisionEnter = { entityA, entityB, contacts ->
    Log.d("Physics", "Collision started between $entityA and $entityB")
    for (contact in contacts) {
        Log.d("Physics", "  Contact at ${contact.point}, normal=${contact.normal}, depth=${contact.depth}")
    }
}

physicsSystem.physicsWorld.onCollisionStay = { entityA, entityB, contacts ->
    // Called each frame while colliding
}

physicsSystem.physicsWorld.onCollisionExit = { entityA, entityB ->
    Log.d("Physics", "Collision ended between $entityA and $entityB")
}
```

### Creating Physics Bodies

```kotlin
// Create entities with physics components
fun createBall(position: Vec2): MutableMap<String, Any> {
    return mutableMapOf<String, Any>(
        "name" to "ball",
        "components" to mapOf(
            "Transform2D" to mapOf(
                "x" to position.x,
                "y" to position.y
            ),
            "RigidBody2D" to mapOf(
                "type" to "DYNAMIC",
                "mass" to 0.1f,
                "restitution" to 1.0f,  // Perfectly elastic
                "gravityScale" to 0f     // No gravity for ball
            ),
            "CircleCollider2D" to mapOf(
                "radius" to 8f
            ),
            "SpriteComponent" to mapOf(
                "width" to 16f,
                "height" to 16f,
                "color" to mapOf("x" to 1f, "y" to 1f, "z" to 1f, "w" to 1f)
            )
        )
    )
}

// Register with physics system
physicsSystem.addBody(
    entity = ballEntity,
    body = RigidBody2D.dynamic(mass = 0.1f, restitution = 1.0f),
    collider = CircleCollider2D(radius = 8f),
    position = Vec2(640f, 360f)
)

// Give the ball an initial velocity
ballBody.velocity = Vec2(200f, 300f)
```

### Physics Simulation Pseudocode

The complete physics step (`PhysicsWorld.step(dt)`) in pseudocode:

```
FUNCTION step(dt):
    // 1. INTEGRATE FORCES
    FOR EACH body IN bodies:
        IF body.isSleeping OR body.type != DYNAMIC:
            CONTINUE

        // Apply gravity
        body.velocity += gravity * body.gravityScale * dt

        // Apply accumulated forces
        body.velocity += body.force * body.inverseMass * dt

        // Apply angular forces
        body.angularVelocity += body.torque * body.inverseMass * dt

        // Apply damping
        body.velocity *= 0.999
        body.angularVelocity *= 0.999

        // Clear forces for next frame
        body.clearForces()

        // Sleep check
        IF body.velocity.lengthSquared() < 0.001
           AND abs(body.angularVelocity) < 0.001:
            body.sleepCounter++
            IF body.sleepCounter > 60:
                body.velocity = ZERO
                body.angularVelocity = 0
                body.sleep()
        ELSE:
            body.sleepCounter = 0

    // 2. BROAD PHASE — rebuild quadtree
    quadtree.clear()
    FOR EACH body IN bodies:
        bounds = body.collider.getBounds(body.position, body.rotation)
        quadtree.insert(body.entity, bounds)

    // 3. NARROW PHASE + RESOLUTION
    currentCollisionPairs = emptySet()

    FOR EACH body IN bodies:
        IF body.isSleeping: CONTINUE

        bounds = body.collider.getBounds(body.position, body.rotation)
        candidates = quadtree.query(bounds)

        FOR EACH candidate IN candidates:
            IF candidate.entity == body.entity: CONTINUE

            // Create canonical pair key (ordered by entity ID)
            pairKey = normalizePair(body.entity, candidate.entity)
            IF pairKey already tested: CONTINUE

            // Run narrow phase collision test
            result = CollisionDetector.testCollision(
                body.collider, body.position, body.rotation,
                candidate.collider, candidate.position, candidate.rotation
            )

            IF result.colliding:
                currentCollisionPairs.add(pairKey)

                // Resolve collision (unless trigger)
                IF NOT body.isTrigger AND NOT candidate.isTrigger:
                    FOR EACH contact IN result.contactPoints:
                        CollisionResolver.resolve(
                            body, body.position,
                            candidate, candidate.position,
                            contact
                        )
                        // Apply positional correction
                        (corrA, corrB) = CollisionResolver
                            .calculatePositionalCorrection(
                                body.inverseMass, candidate.inverseMass,
                                contact.normal, contact.depth
                            )
                        IF body.type == DYNAMIC: body.position += corrA
                        IF candidate.type == DYNAMIC: candidate.position += corrB

                // Fire collision events
                IF pairKey IN previousCollisionPairs:
                    onCollisionStay(body.entity, candidate.entity, contacts)
                ELSE:
                    onCollisionEnter(body.entity, candidate.entity, contacts)

    // Fire exit events
    FOR EACH pair IN previousCollisionPairs:
        IF pair NOT IN currentCollisionPairs:
            onCollisionExit(pair.entityA, pair.entityB)

    previousCollisionPairs = currentCollisionPairs

    // 4. UPDATE POSITIONS FROM VELOCITIES
    FOR EACH body IN bodies:
        IF body.isSleeping: CONTINUE
        IF body.type == DYNAMIC OR body.type == KINEMATIC:
            body.position += body.velocity * dt
            body.rotation += body.angularVelocity * dt
```

### Impulse Resolution Algorithm

The impulse resolution (`CollisionResolver.resolve()`) in pseudocode:

```
FUNCTION resolve(bodyA, posA, bodyB, posB, contact):
    IF bodyA.isTrigger OR bodyB.isTrigger: RETURN
    IF both are STATIC: RETURN

    invMassA = bodyA.inverseMass
    invMassB = bodyB.inverseMass
    invMassSum = invMassA + invMassB
    IF invMassSum <= 0: RETURN

    normal = contact.normal

    // Relative velocity at contact point
    relVel = bodyB.velocity - bodyA.velocity
    velAlongNormal = relVel.dot(normal)

    // Do not resolve if separating
    IF velAlongNormal > 0: RETURN

    // Restitution (use minimum of the two)
    e = min(bodyA.restitution, bodyB.restitution)

    // Impulse scalar: j = -(1 + e) * v_n / (1/mA + 1/mB)
    j = -(1 + e) * velAlongNormal / invMassSum

    // Apply impulse
    impulse = normal * j
    IF bodyA.type == DYNAMIC:
        bodyA.velocity -= impulse * invMassA
    IF bodyB.type == DYNAMIC:
        bodyB.velocity += impulse * invMassB

    // Friction impulse
    tangentVel = relVel - normal * velAlongNormal
    tangentLen = tangentVel.length()
    IF tangentLen > epsilon:
        tangent = tangentVel / tangentLen
        frictionCoeff = sqrt(bodyA.friction * bodyB.friction)
        jt = -tangentVel.dot(tangent) / invMassSum

        // Coulomb's law: clamp friction impulse
        IF abs(jt) > j * frictionCoeff:
            jt = -j * frictionCoeff * sign(jt)

        frictionImpulse = tangent * jt
        IF bodyA.type == DYNAMIC:
            bodyA.velocity -= frictionImpulse * invMassA
        IF bodyB.type == DYNAMIC:
            bodyB.velocity += frictionImpulse * invMassB

    // Positional correction
    correction = max(penetration - SLOP, 0) / invMassSum * PERCENT * normal
    posA -= correction * invMassA
    posB += correction * invMassB
```

### Ray Casting

```kotlin
// Cast a ray from origin in a direction
val result = physicsSystem.rayCast(
    origin = Vec2(100f, 500f),
    direction = Vec2(0f, -1f),  // Downward
    maxDistance = 1000f
)

if (result != null) {
    Log.d("Physics", "Hit entity at ${result.point}")
    Log.d("Physics", "  Normal: ${result.normal}")
    Log.d("Physics", "  Distance: ${result.distance}")
}
```

---

## 5. Input Handling

### Touch Input

```kotlin
class MyGameSystem : System {
    override fun update(deltaTime: Float) {
        val input = inputManager  // Reference to InputManager

        // Check if any touch is active
        if (input.isTouching(0)) {
            val position = input.getTouchPosition(0)
            val delta = input.getTouchDelta(0)

            // Move an entity to the touch position
            paddleTransform.position = Vec2(position.x, paddleTransform.position.y)

            // Use delta for velocity-based movement
            paddleBody.velocity = Vec2(delta.x / deltaTime, 0f)
        }

        // Gesture detection
        if (input.isTap()) {
            // Launch ball
            ballBody.applyImpulse(Vec2(0f, 500f))
        }

        if (input.isLongPress()) {
            // Charge attack
        }

        if (input.isPinchDetected) {
            val scale = input.pinchScale
            camera.zoomBy(scale)
        }

        if (input.isSwipeDetected) {
            when (input.swipeDirection) {
                SwipeDirection.LEFT -> movePlayerLeft()
                SwipeDirection.RIGHT -> movePlayerRight()
                SwipeDirection.UP -> jump()
                SwipeDirection.DOWN -> duck()
                SwipeDirection.NONE -> {}
            }
        }
    }
}
```

### Multi-Touch

```kotlin
// Track multiple fingers
for (i in 0 until input.touchCount) {
    if (input.isTouching(i)) {
        val pos = input.getTouchPosition(i)
        val delta = input.getTouchDelta(i)
        processFinger(i, pos, delta)
    }
}

// Touch point data
data class TouchPoint(
    var x: Float,       // Current X
    var y: Float,       // Current Y
    var prevX: Float,   // Previous frame X
    var prevY: Float,   // Previous frame Y
    var startX: Float,  // Touch-down X
    var startY: Float,  // Touch-down Y
    var isActive: Boolean,
    var downTime: Long  // Timestamp of touch-down
) {
    val deltaX: Float get() = x - prevX
    val deltaY: Float get() = y - prevY
    val totalDeltaX: Float get() = x - startX
    val totalDeltaY: Float get() = y - startY
}
```

### Accelerometer

```kotlin
class TiltControlSystem : System {
    override fun update(deltaTime: Float) {
        val accel = inputManager.accelerometerInput

        // Raw accelerometer values (device-relative)
        val rawX = inputManager.accelerometerX
        val rawY = inputManager.accelerometerY
        val rawZ = inputManager.accelerometerZ

        // Smoothed values
        val smoothX = accel.smoothedX
        val smoothY = accel.smoothedY

        // Use tilt to control paddle
        val tiltForce = Vec2(smoothX * 500f, 0f)
        paddleBody.applyForce(tiltForce)
    }
}
```

---

## 6. Asset Loading

### Async Asset Loading

```kotlin
class GameActivity : EngineActivity() {

    override fun onEngineCreate() {
        // Async load — callback fires when ready
        assetManager.load("textures/bricks.png", AssetType.TEXTURE) { texture ->
            if (texture != null) {
                brickTexture = texture as Texture
                // Texture is now available for rendering
            }
        }

        // Load multiple assets
        assetManager.load("audio/bounce.wav", AssetType.AUDIO) { /* ... */ }
        assetManager.load("audio/music.mp3", AssetType.AUDIO) { /* ... */ }
        assetManager.load("scenes/level1.json", AssetType.JSON) { /* ... */ }
    }
}
```

### Synchronous Loading

```kotlin
// Block until loaded — use for critical startup assets
val texture = assetManager.loadSync("textures/player.png", AssetType.TEXTURE) as? Texture
val shaderSource = assetManager.loadSync("shaders/custom.glsl", AssetType.SHADER) as? String
```

### Asset Caching

```kotlin
// Check if an asset is cached
if (assetManager.contains("textures/bricks.png")) {
    val cachedTexture = assetManager.get<Texture>("textures/bricks.png")
}

// Manually unload
assetManager.unload("textures/bricks.png")  // Recycles bitmap

// Unload everything
assetManager.unloadAll()
```

### JSON Scene Format

```json
{
  "name": "level_1",
  "entities": [
    {
      "name": "player",
      "components": {
        "Transform2D": { "x": 640, "y": 360 },
        "SpriteComponent": {
          "width": 32, "height": 32,
          "color": { "x": 1.0, "y": 1.0, "z": 1.0, "w": 1.0 },
          "layer": 2
        },
        "RigidBody2D": { "mass": 1.0, "restitution": 0.3 },
        "BoxCollider2D": { "halfExtents": { "x": 16, "y": 16 } }
      }
    },
    {
      "name": "platform",
      "components": {
        "Transform2D": { "x": 640, "y": 50 },
        "RigidBody2D": { "type": "STATIC" },
        "BoxCollider2D": { "halfExtents": { "x": 640, "y": 10 } }
      }
    },
    {
      "name": "enemy_1",
      "components": {
        "Transform2D": { "x": 200, "y": 400 },
        "SpriteComponent": {
          "width": 48, "height": 48,
          "color": { "x": 1.0, "y": 0.0, "z": 0.0, "w": 1.0 }
        },
        "RigidBody2D": { "mass": 0.5, "restitution": 0.5 },
        "CircleCollider2D": { "radius": 24 }
      }
    }
  ]
}
```

### Loading a Scene

```kotlin
val sceneLoader = SceneLoader(assetManager)

// Load and parse scene
val scene = sceneLoader.loadScene("scenes/level1.json", engine.ecsWorld)
if (scene != null) {
    Log.d("Game", "Loaded scene '${scene.name}' with ${scene.entities.size} entities")
    for (entityData in scene.entities) {
        Log.d("Game", "  Entity: ${entityData.name}")
        for ((compType, compData) in entityData.components) {
            Log.d("Game", "    $compType: $compData")
        }
    }
}
```

---

## 7. Audio System

### Playing Sound Effects

```kotlin
// Initialize audio (done automatically by EngineActivity)
audioManager.initialize(maxStreams = 16)

// Load sounds
audioManager.loadSound("bounce", R.raw.bounce_sound)
audioManager.loadSound("explosion", R.raw.explosion_sound)
audioManager.loadSound("pickup", R.raw.pickup_sound)

// Play sounds
val streamId = audioManager.playSound("bounce", loop = false, priority = 1, rate = 1.0f)

// Control playback
audioManager.stopSound(streamId)
audioManager.pauseSound(streamId)
audioManager.resumeSound(streamId)
audioManager.setSoundVolume(streamId, 0.8f, 0.8f)

// Volume control
audioManager.setMasterVolume(0.8f)
audioManager.setSfxVolume(0.6f)
audioManager.setMusicVolume(0.4f)
```

### Background Music

```kotlin
// Start background music (loops by default)
audioManager.playMusic(R.raw.background_music, loop = true)

// Control music
audioManager.pauseMusic()
audioManager.resumeMusic()
audioManager.stopMusic()

// Check if playing
if (audioManager.isMusicPlaying()) {
    // ...
}
```

### Spatial Audio

```kotlin
val spatialAudio = SpatialAudio(audioManager)

// Set listener position (usually the player or camera)
spatialAudio.listenerPosition = playerPosition

// Update spatial audio each frame
val activeSounds = listOf(
    Pair(soundId1, soundPosition1),
    Pair(soundId2, soundPosition2),
    Pair(soundId3, soundPosition3)
)
spatialAudio.update(activeSounds)

// Manual panning calculation
val (leftVol, rightVol) = spatialAudio.calculatePanning(
    listenerPos = camera.position.xy(),
    soundPos = enemyPosition,
    maxDistance = 500f
)
```

---

## 8. Platform Integration

### Android Lifecycle Mapping

```
Android Activity          Engine
─────────────────         ──────
onCreate()          →     Initialize managers, create Engine
onResume()          →     Engine.resume(), register sensors
onPause()           →     Engine.pause(), unregister sensors
onDestroy()         →     Engine.shutdown()
```

### GLSurfaceView Integration

`EngineView` extends `GLSurfaceView` and manages the OpenGL context:

```kotlin
class EngineView(context: Context) : GLSurfaceView(context) {
    init {
        setEGLContextClientVersion(3)          // OpenGL ES 3.0
        setEGLConfigChooser(8, 8, 8, 8, 16, 0) // RGBA8888 + depth
    }

    fun initialize(engine: Engine) {
        setRenderer(EngineGLRenderer(engine))
        renderMode = RENDERMODE_CONTINUOUSLY
    }
}

private class EngineGLRenderer(private val engine: Engine) : GLSurfaceView.Renderer {
    private var startTimeNanos: Long = 0L

    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
        startTimeNanos = System.nanoTime()
        GLES30.glClearColor(0f, 0f, 0f, 1f)
        GLES30.glEnable(GLES30.GL_BLEND)
        GLES30.glBlendFunc(GLES30.GL_SRC_ALPHA, GLES30.GL_ONE_MINUS_SRC_ALPHA)
        GLES30.glDisable(GLES30.GL_DEPTH_TEST)
    }

    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
        GLES30.glViewport(0, 0, width, height)
        engine.renderer.setViewport(width, height)
    }

    override fun onDrawFrame(gl: GL10?) {
        val currentTimeNanos = System.nanoTime()
        val deltaTime = (currentTimeNanos - startTimeNanos) / 1_000_000_000f
        startTimeNanos = currentTimeNanos
        engine.mainLoop(deltaTime)
    }
}
```

### Immersive Mode

```kotlin
// EngineActivity automatically enables immersive mode on focus:
private fun hideSystemUI() {
    window.decorView.systemUiVisibility = (
        View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
            or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
            or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
            or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
            or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
            or View.SYSTEM_UI_FLAG_FULLSCREEN
    )
}
```

---

## 9. Rendering Pipeline Flowchart

### Complete Frame Lifecycle

```
┌──────────────────────────────────────────────────────────────────┐
│                     Android OpenGL Thread                         │
│                                                                   │
│  EngineGLRenderer.onDrawFrame()                                   │
│  │                                                                │
│  ├─ Calculate deltaTime from System.nanoTime()                    │
│  │                                                                │
│  └─ engine.mainLoop(deltaTime)                                   │
│     │                                                             │
│     ├─ ┌─────────────────────────────────────┐                   │
│     │  │       TimeManager.update(dt)         │                   │
│     │  │                                      │                   │
│     │  │  accumulator += realDeltaTime        │                   │
│     │  │  while accumulator >= fixedDt:       │                   │
│     │  │    accumulator -= fixedDt            │                   │
│     │  │    steps++                           │                   │
│     │  │  return steps                        │                   │
│     │  └─────────────────────────────────────┘                   │
│     │                                                             │
│     ├─ [FOR EACH STEP]                                           │
│     │  │                                                          │
│     │  └─ engine.update(fixedDt)                                 │
│     │     │                                                       │
│     │     ├─ flushSystemChanges()                                │
│     │     │   └─ Add/remove pending systems                      │
│     │     │      Sort by priority                                 │
│     │     │                                                       │
│     │     └─ FOR EACH system (sorted by priority):               │
│     │        │                                                    │
│     │        ├─ PhysicsSystem.update(dt)  [priority -10]         │
│     │        │   ├─ Integrate forces                             │
│     │        │   ├─ Rebuild quadtree                             │
│     │        │   ├─ Broad phase queries                          │
│     │        │   ├─ Narrow phase collision tests                 │
│     │        │   ├─ Impulse resolution                           │
│     │        │   ├─ Positional correction                        │
│     │        │   ├─ Fire collision events                        │
│     │        │   └─ Update positions from velocities             │
│     │        │                                                    │
│     │        ├─ CustomGameSystem.update(dt)  [priority 0]       │
│     │        │   └─ Game-specific logic                          │
│     │        │                                                    │
│     │        └─ RenderSystem.update(dt)  [priority 100]          │
│     │            └─ (No-op for render systems)                   │
│     │                                                             │
│     └─ engine.render(interpolation)                              │
│        │                                                          │
│        ├─ renderer.beginFrame()                                  │
│        │   └─ glClearColor + glClear                             │
│        │                                                          │
│        └─ FOR EACH system:                                       │
│           │                                                       │
│           └─ system.render(interpolation)                        │
│              │                                                    │
│              └─ RenderSystem.render(world, interp)               │
│                  │                                                │
│                  ├─ Get entities from world                       │
│                  ├─ Filter for SpriteComponent                   │
│                  ├─ Sort by layer                                 │
│                  │                                                │
│                  ├─ spriteBatch.begin(camera)                    │
│                  │   └─ Reset batch, set camera matrices         │
│                  │                                                │
│                  ├─ FOR EACH sprite:                              │
│                  │   ├─ If different texture → flush()           │
│                  │   ├─ If batch full → flush()                  │
│                  │   └─ batch.addSprite(x, y, u, v, rgba)       │
│                  │                                                │
│                  ├─ spriteBatch.end()                            │
│                  │   └─ flush() if remaining sprites             │
│                  │                                                │
│                  └─ flush():                                      │
│                      ├─ Upload vertex data to VBO                │
│                      ├─ Bind texture to unit 0                   │
│                      ├─ glDrawElements(GL_TRIANGLES, ...)        │
│                      └─ Reset batch                               │
│                                                                   │
│        renderer.endFrame()                                       │
│        └─ (Buffer swap handled by GLSurfaceView)                 │
│                                                                   │
└──────────────────────────────────────────────────────────────────┘
```

### Physics Step Detail

```
PhysicsWorld.step(dt)
  │
  ├─ 1. FORCE INTEGRATION
  │     ┌──────────────────────────────────────┐
  │     │ FOR EACH body in bodies:              │
  │     │   IF sleeping OR not DYNAMIC: skip    │
  │     │                                       │
  │     │   // Semi-implicit Euler               │
  │     │   v += gravity * gravityScale * dt    │
  │     │   v += force * inverseMass * dt       │
  │     │   ω += torque * inverseMass * dt      │
  │     │   v *= damping (0.999)                │
  │     │   ω *= damping (0.999)                │
  │     │                                       │
  │     │   clearForces()                       │
  │     │                                       │
  │     │   // Sleep check                      │
  │     │   IF |v| < 0.001 AND |ω| < 0.001:    │
  │     │     sleepCounter++                    │
  │     │     IF sleepCounter > 60: sleep()     │
  │     │   ELSE:                               │
  │     │     sleepCounter = 0                  │
  │     └──────────────────────────────────────┘
  │
  ├─ 2. BROAD PHASE
  │     ┌──────────────────────────────────────┐
  │     │ quadtree.clear()                      │
  │     │ FOR EACH body:                        │
  │     │   aabb = collider.getBounds(pos, rot) │
  │     │   quadtree.insert(entity, aabb)       │
  │     │                                       │
  │     │ // Quadtree subdivision:              │
  │     │ // IF node.entities > 8 AND depth < 5 │
  │     │ //   Split into 4 quadrants           │
  │     │ //   Re-insert entities into children │
  │     └──────────────────────────────────────┘
  │
  ├─ 3. NARROW PHASE + RESOLUTION
  │     ┌──────────────────────────────────────┐
  │     │ FOR EACH body:                        │
  │     │   IF sleeping: skip                   │
  │     │   aabb = collider.getBounds(pos, rot) │
  │     │   candidates = quadtree.query(aabb)   │
  │     │                                       │
  │     │   FOR EACH candidate:                 │
  │     │     IF same entity: skip              │
  │     │     IF pair already tested: skip      │
  │     │                                       │
  │     │     // Dispatch by collider types:    │
  │     │     result = testCollision(           │
  │     │       colliderA, posA, rotA,          │
  │     │       colliderB, posB, rotB           │
  │     │     )                                 │
  │     │                                       │
  │     │     IF result.colliding:              │
  │     │       // Impulse resolution:          │
  │     │       relVel = vB - vA                │
  │     │       vn = relVel · normal            │
  │     │       IF vn > 0: skip (separating)    │
  │     │       e = min(eA, eB)                 │
  │     │       j = -(1+e) * vn / (1/mA+1/mB)  │
  │     │       vA -= j * normal * (1/mA)       │
  │     │       vB += j * normal * (1/mB)       │
  │     │                                       │
  │     │       // Friction:                    │
  │     │       tangent = project(relVel)       │
  │     │       jt = clamp(jt, -j*μ, j*μ)      │
  │     │       vA -= jt * tangent * (1/mA)     │
  │     │       vB += jt * tangent * (1/mB)     │
  │     │                                       │
  │     │       // Positional correction:       │
  │     │       corr = max(depth-slop,0)        │
  │     │              / ΣinvMass * % * normal  │
  │     │       posA -= corr * invMassA         │
  │     │       posB += corr * invMassB         │
  │     │                                       │
  │     │       // Fire events:                 │
  │     │       IF pair in prev: onStay()       │
  │     │       ELSE: onEnter()                 │
  │     │                                       │
  │     │ // Exit events:                       │
  │     │ FOR EACH pair in previous:            │
  │     │   IF pair NOT in current: onExit()    │
  │     │                                       │
  │     │ previousPairs = currentPairs          │
  │     └──────────────────────────────────────┘
  │
  └─ 4. POSITION UPDATE
        ┌──────────────────────────────────────┐
        │ FOR EACH body:                        │
        │   IF sleeping: skip                   │
        │   IF DYNAMIC OR KINEMATIC:            │
        │     pos += velocity * dt              │
        │     rot += angularVelocity * dt       │
        └──────────────────────────────────────┘
```

---

## Appendix: Event System

### Publishing and Subscribing

```kotlin
val eventBus = EventBus()

// Define event types
data class CollisionEvent(val entityA: Any, val entityB: Any)
data class ScoreChangedEvent(val newScore: Int)
data class LevelCompletedEvent(val levelNumber: Int)

// Subscribe (type-safe)
val subscription = eventBus.subscribe<CollisionEvent> { event ->
    Log.d("Events", "Collision: ${event.entityA} <-> ${event.entityB}")
}

// Publish
eventBus.publish(CollisionEvent(playerEntity, enemyEntity))
eventBus.publish(ScoreChangedEvent(1000))

// Unsubscribe
eventBus.unsubscribe(subscription)

// Cleanup
eventBus.clear()
```

### Object Pool Usage

```kotlin
// Create a pool with a factory function
val bulletPool = ObjectPool(
    factory = { BulletEntity() },
    initialCapacity = 64
)

// Pre-allocate objects
bulletPool.preload(32)

// Obtain an object (reuses from pool if available)
val bullet = bulletPool.obtain()
bullet.position = playerPosition
bullet.velocity = aimDirection * 500f

// Return to pool when done
bulletPool.free(bullet)

// Statistics
Log.d("Pool", "Active: ${bulletPool.getActiveCount()}")
Log.d("Pool", "Pooled: ${bulletPool.getPoolSize()}")
Log.d("Pool", "Total: ${bulletPool.getTotalCount()}")
```
