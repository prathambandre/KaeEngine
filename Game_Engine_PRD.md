# Product Requirements Document (PRD)
## Project: KaeEngine — Custom 2D/3D Game Engine in Kotlin (OpenGL ES / Vulkan)

**Version:** 1.0
**Owner:** [Your Name]
**Status:** Draft
**Target Platform:** Android (with architecture allowing future desktop/JVM port)

---

## 1. Overview

### 1.1 Summary
KaeEngine is a custom-built game engine written in Kotlin, targeting Android, with a rendering backend built on OpenGL ES 3.x (with an optional Vulkan backend for advanced GPU control). Unlike using Unity or Unreal, this project builds every core subsystem — rendering pipeline, physics, input, asset loading, scene graph, and audio — from scratch to demonstrate systems-level engineering depth.

### 1.2 Motivation
- Showcase low-level systems programming ability (rendering pipelines, memory management, math libraries) rather than framework usage.
- Build a portfolio-defining project that signals senior-level engineering capability to recruiters and collaborators.
- Create a reusable, lightweight engine for future personal game projects without third-party engine overhead.

### 1.3 Non-Goals (v1)
- Not competing with Unity/Unreal in feature completeness.
- No visual editor/GUI tooling in v1 (code-first engine).
- No networking/multiplayer support in v1.
- No iOS support in v1 (Android-only, though core math/logic layer should be portable).

---

## 2. Goals & Success Metrics

| Goal | Metric |
|---|---|
| Functional rendering pipeline | Render 10,000+ sprites/meshes at 60 FPS on mid-range device |
| Working physics | Stable 2D rigid-body simulation (collision, gravity, restitution) at 60 Hz |
| Usable by others | A sample game (e.g., simple platformer) built entirely using the engine's public API |
| Code quality | >70% test coverage on core math/physics modules |
| Community traction | Well-documented GitHub repo with README, architecture diagrams, and demo APK/video |

---

## 3. Target Users

1. **Primary:** Yourself — as the game developer building sample games on top of the engine.
2. **Secondary:** Other indie/hobbyist Android developers who might fork or learn from the engine.
3. **Tertiary:** Recruiters/engineers reviewing the GitHub repo as a portfolio piece.

---

## 4. System Architecture

### 4.1 High-Level Modules

```
KaeEngine/
├── core/           # Engine loop, ECS, time management
├── math/           # Vectors, matrices, quaternions, transforms
├── render/         # OpenGL ES/Vulkan abstraction, shaders, render graph
├── physics/         # Broad/narrow-phase collision, rigid body dynamics
├── input/          # Touch, sensor, gamepad abstraction
├── assets/         # Asset loading (textures, models, audio, JSON scene data)
├── audio/          # SoundPool/OpenSL ES wrapper
├── scene/          # Scene graph, entity-component-system (ECS)
└── platform/       # Android-specific glue (Activity, Surface, lifecycle)
```

### 4.2 Architecture Pattern
- **Entity-Component-System (ECS)** for game object composition (avoids deep inheritance hierarchies, enables data-oriented design for performance).
- **Render Graph** abstraction so the rendering backend (GLES vs Vulkan) can be swapped without touching game logic.
- **Fixed timestep game loop** with interpolation for rendering (decouples physics from frame rate).

### 4.3 Rendering Pipeline
- OpenGL ES 3.2 as the primary backend (broad device compatibility).
- Vulkan backend as a stretch goal / v2 milestone (better GPU control, modern API practice).
- Shader abstraction layer (GLSL for GLES, SPIR-V for Vulkan).
- Batched sprite rendering + instanced mesh rendering for performance.

### 4.4 Physics System
- Custom 2D physics first (AABB/SAT collision detection, impulse-based resolution).
- Spatial partitioning (quadtree) for broad-phase collision performance.
- 3D physics (basic rigid body + simple mesh colliders) as a v2 stretch goal.

---

## 5. Functional Requirements

### 5.1 Core Engine Loop
- FR1: Fixed timestep update loop (e.g., 60Hz) decoupled from render framerate.
- FR2: Pause/resume lifecycle handling tied to Android Activity lifecycle.
- FR3: Frame time budget logging/profiling hooks.

### 5.2 Rendering
- FR4: Load and render 2D sprites with transparency and batching.
- FR5: Load and render basic 3D meshes (OBJ/GLTF) with basic Phong/PBR shading.
- FR6: Camera system supporting orthographic (2D) and perspective (3D) projection.
- FR7: Texture atlas support for sprite batching efficiency.

### 5.3 Physics
- FR8: AABB and circle collision detection.
- FR9: Impulse-based collision resolution with restitution/friction.
- FR10: Gravity and basic force application (velocity/acceleration integration).
- FR11: Trigger volumes (non-physical collision events).

### 5.4 Input
- FR12: Touch input abstraction (tap, drag, multi-touch gestures).
- FR13: Accelerometer/gyroscope input support.
- FR14: Bluetooth gamepad support (optional, stretch).

### 5.5 Asset Management
- FR15: Async asset loading (textures, audio, models) off the main thread.
- FR16: Simple scene description format (JSON) for defining levels/entities declaratively.
- FR17: Asset caching and memory management (unload unused assets).

### 5.6 Audio
- FR18: Play/stop/loop sound effects and background music.
- FR19: Volume/spatial audio controls (basic panning based on 2D position).

### 5.7 Developer Experience
- FR20: Public API documented with KDoc.
- FR21: Sample game(s) included in repo demonstrating engine usage.
- FR22: Debug overlay (FPS counter, entity count, physics wireframe toggle).

---

## 6. Non-Functional Requirements

| Category | Requirement |
|---|---|
| Performance | 60 FPS with 10k+ sprites or 500+ physics bodies on a mid-tier device (e.g., Snapdragon 6-series) |
| Memory | Engine core footprint < 50MB at runtime (excluding game assets) |
| Compatibility | Android API 24+ (Android 7.0+), OpenGL ES 3.0+ required |
| Build size | Engine library (AAR) under 5MB excluding sample assets |
| Code quality | Kotlin idiomatic, modular Gradle structure (multi-module: `:engine-core`, `:engine-render`, etc.) |
| Testing | Unit tests for math library, physics resolution, ECS core |

---

## 7. Milestone Roadmap

### Phase 0 — Foundation (Week 1–2)
- Set up multi-module Gradle project structure.
- Implement math library (Vec2, Vec3, Mat4, Quaternion) with unit tests.
- Set up GLSurfaceView + basic render loop rendering a triangle.

### Phase 1 — Core Rendering (Week 3–5)
- Sprite batch renderer (2D).
- Texture loading + atlas support.
- Camera system (ortho projection).
- Basic ECS implementation (Entity, Component, System interfaces).

### Phase 2 — Physics & Input (Week 6–8)
- AABB/circle collision detection.
- Impulse resolution + gravity.
- Touch input system.
- Quadtree spatial partitioning.

### Phase 3 — Assets & Audio (Week 9–10)
- Async asset loader.
- JSON scene format + loader.
- Audio playback wrapper (SoundPool/OpenSL ES).

### Phase 4 — Sample Game + Polish (Week 11–12)
- Build a complete sample platformer/breakout-style game using only the engine's public API.
- Debug overlay, profiling tools.
- Documentation, architecture diagrams, README, demo video/APK.

### Phase 5 — Stretch Goals (Week 13+)
- 3D mesh rendering + basic PBR shading.
- Vulkan backend (swap-in renderer).
- Basic 3D physics.
- Gamepad support.

---

## 8. Risks & Mitigations

| Risk | Mitigation |
|---|---|
| Scope creep (engine dev is a bottomless pit) | Strict phase gating — do not start Phase N+1 until sample deliverable of Phase N works |
| Performance issues on real devices | Profile early and often on a real mid-tier device, not just emulator |
| Physics instability (jitter, tunneling) | Start with simple shapes only (AABB/circle); defer polygon collision to stretch goals |
| Burnout on infrastructure before anything "fun" exists | Get a visible triangle/sprite on screen in week 1 to maintain motivation |
| Vulkan complexity derailing timeline | Treat Vulkan as fully optional v2 — ship GLES version first |

---

## 9. Deliverables for GitHub

1. **Multi-module Kotlin repo** (`engine-core`, `engine-render`, `engine-physics`, `sample-game`).
2. **README.md** with:
   - Architecture diagram (module dependency graph)
   - GIF/video demo of sample game running
   - Performance benchmarks (FPS vs entity count graph)
   - Build/run instructions
3. **ARCHITECTURE.md** — deep dive into ECS design, render graph, physics loop.
4. **Sample APK** in Releases tab.
5. **CI pipeline** (GitHub Actions) running unit tests + lint on push.

---

## 10. Open Questions

- Should ECS be a from-scratch implementation, or reference an existing pattern (e.g., Artemis-inspired) for structure while writing all code independently?
- Is Vulkan worth the time investment for v1, or should it be explicitly deferred to keep scope realistic?
- Should the engine target only Android, or should the `math`/`physics`/`core` modules be written as pure Kotlin (no Android dependencies) to enable a future JVM desktop port?

---

*This PRD is a living document — update phase progress and open questions as the project evolves.*
