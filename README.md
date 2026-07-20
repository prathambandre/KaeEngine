# KaeEngine — Custom 2D/3D Game Engine in Kotlin

[![Kotlin](https://img.shields.io/badge/Kotlin-1.9.22-7F52FF?logo=kotlin)](https://kotlinlang.org)
[![Android](https://img.shields.io/badge/Android-24%2B-green?logo=android)](https://developer.android.com)
[![OpenGL ES](https://img.shields.io/badge/OpenGL%20ES-3.x-red)](https://www.khronos.org/opengles/)
[![License](https://img.shields.io/badge/License-MIT-blue.svg)](LICENSE)

A custom-built game engine written in Kotlin, targeting Android, with a rendering backend built on OpenGL ES 3.x. Built from scratch to demonstrate systems-level engineering depth.

![Demo](docs/demo.gif)

## Features

- **ECS Architecture** — Entity-Component-System with component-based storage for cache-friendly iteration
- **Rendering** — OpenGL ES 3.x with batched sprite rendering, texture atlases, and mesh rendering
- **Physics** — 2D rigid-body simulation with AABB/circle/capsule collision, impulse resolution, and quadtree spatial partitioning
- **Math Library** — Vec2/Vec3/Vec4, Mat4, Quaternion, Transform — all pure Kotlin, no dependencies
- **Input** — Touch gestures, accelerometer, gamepad abstraction
- **Audio** — SoundPool + MediaPlayer wrapper with 2D spatial audio
- **Assets** — Async asset loading with LRU caching and JSON scene format
- **Platform** — Android Activity lifecycle, GLSurfaceView integration, immersive mode

## Architecture

```
KaeEngine/
├── engine-math/       # Pure Kotlin math library (Vec2, Vec3, Mat4, Quaternion)
├── engine-core/       # Engine loop, ECS foundation, time management, events
├── engine-scene/      # Entity-Component-System (World, Component interface)
├── engine-render/     # OpenGL ES rendering, shaders, sprite batching, cameras
├── engine-physics/    # 2D collision detection, resolution, quadtree
├── engine-input/      # Touch, accelerometer, gamepad input
├── engine-assets/     # Async asset loading, caching, JSON scene loader
├── engine-audio/      # Sound effects, music, spatial audio
├── engine-platform/   # Android Activity/Surface/lifecycle integration
└── sample-game/       # Breakout-style demo game
```

### Module Dependency Graph

```
engine-platform
├── engine-render ──── engine-core ──── engine-math
├── engine-input ───── engine-core
├── engine-assets ──── engine-core
├── engine-audio ───── engine-core
├── engine-physics ─── engine-scene ── engine-math
└── engine-scene ───── engine-math
```

## Performance

| Metric | Target | Achieved |
|--------|--------|----------|
| Sprite rendering | 10,000+ @ 60 FPS | ✅ |
| Physics bodies | 500+ @ 60 Hz | ✅ |
| Engine memory | < 50MB | ✅ |
| Build size (AAR) | < 5MB | ✅ |

## Getting Started

### Prerequisites
- Android Studio Hedgehog (2023.1.1) or later
- JDK 17
- Android SDK 34
- Device/emulator with OpenGL ES 3.0+

### Build & Run
```bash
git clone https://github.com/yourusername/KaeEngine.git
cd KaeEngine
./gradlew assembleDebug
```

Install on device:
```bash
adb install sample-game/build/outputs/apk/debug/sample-game-debug.apk
```

### Run Tests
```bash
./gradlew test              # Unit tests
./gradlew lint              # Lint checks
```

## Sample Game

The included **Breakout** demo showcases:
- Sprite batch rendering with colored bricks
- Physics-based ball and paddle collision
- Touch input for paddle control
- Score tracking and lives system

### Controls
- **Touch/Drag** — Move paddle
- **Tap** — Launch ball

## Tech Stack

| Component | Technology |
|-----------|-----------|
| Language | Kotlin 1.9 |
| Build | Gradle 8.2 (Kotlin DSL) |
| Rendering | OpenGL ES 3.x |
| Physics | Custom 2D engine |
| Target | Android API 24+ (7.0+) |
| Testing | JUnit 5 |

## Project Structure

Each module follows standard Android/Kotlin layout:
```
module-name/
├── build.gradle.kts
├── src/
│   ├── main/
│   │   ├── kotlin/com/kae/engine/module/
│   │   │   └── *.kt
│   │   ├── res/
│   │   └── AndroidManifest.xml
│   └── test/
│       └── kotlin/com/kae/engine/module/
│           └── *Test.kt
```

## Roadmap

- [x] Phase 0 — Foundation (math library, basic render loop)
- [x] Phase 1 — Core Rendering (sprite batching, cameras, ECS)
- [x] Phase 2 — Physics & Input (collision, quadtree, touch)
- [x] Phase 3 — Assets & Audio (async loading, scenes, sound)
- [x] Phase 4 — Sample Game + Polish
- [ ] Phase 5 — 3D rendering, Vulkan backend

## Architecture Decisions

See [ARCHITECTURE.md](ARCHITECTURE.md) for deep-dive into:
- ECS design and component storage
- Render graph and pipeline
- Physics simulation loop
- Fixed timestep game loop

For detailed code examples and walkthroughs, see [docs/ARCHITECTURE.md](docs/ARCHITECTURE.md).

## License

MIT License — see [LICENSE](LICENSE) for details.

## Author

Built as a portfolio project demonstrating senior-level systems programming — rendering pipelines, memory management, physics simulation, and ECS architecture.
