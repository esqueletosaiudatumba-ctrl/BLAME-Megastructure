# AGENTS.md — read this before touching anything

This repo is worked on by multiple AI coding agents (and humans). Follow it so the next agent
does not have to reverse-engineer your session.

## The 60-second orientation

**Creative north star:** before visual or world-content work, read the root-level `GRAND_GOAL_VISION.md`. It defines the intended atmosphere and includes embedded reference images. Keep its creative goals subordinate to the technical hard rules in this file.

**What this is:** a Forge 1.12.2 mod (`blamegen`) that generates an infinite, deterministic,
BLAME!-style world via CubicChunks, with Toha Heavy Industries authored as a fixed monumental set piece.
Deterministic pure-Java core, thin MC adapter.

**Repo layout:**

```
mod/                                   the Forge 1.12.2 mod (RetroFuturaGradle)
  src/main/java/blame/blamegen/
    BlameGen.java                      @Mod; registers the world type + server events
    BlameWorldType.java                WorldType "BLAME", ICubicWorldType (overworld only)
    BlameCubeGenerator.java            ICubeCubeGenerator adapter: Mat id -> 1.12.2 IBlockState
    WorldEvents.java                   spawn, locked clear noon, no weather/mobs, first-join placement
    ClientEvents.java                  fog follows skylight (black inside, blue in daylight)
    core/                              PURE JAVA, no Minecraft imports, safe to copy anywhere
      Megastructure.java               THE WORLD GENERATOR
      Toha.java                        fixed, authored Toha Heavy Industries monument — not a random district generator
      Arrival.java                     fixed beach/bay/gate/causeway + ocean spires + ruins + face + arches
      Face.java                        sculpted shaft-wall facades
      Landmark.java                    5 landmark types for previously-empty coarse cells
      Beacons.java                     Safeguard beacons / terminal nodes (inside Megastructure.java)
      Cube.java                        16^3 buffer + raster primitives (box, prism, tiltSlab, archRow, blob, ring, tube, beam, cylinder, sphere, taper)
      H.java                            hashing + value noise
      Mat.java                         material ids and styles
      Styles.java                      resolves style ids -> concrete materials
tools/
  preview/Preview.java                 offline renderer, no Minecraft. THE dev loop.
  verify/                             offline verification: SeamCheck, Probe, Survey, LandmarkSurvey
  forge/SrgMcpChanger.java             SRG<->MCP jar remapper for building the mod
  verify/NbtRegistryFix.java           strips stale CubicChunks registries from level.dat
  package-instance.ps1, make-zip.ps1   packaging
docs/
  reference-images/                         user-supplied visual references embedded in GRAND_GOAL_VISION.md
  previews-v2|v3|v4/, previews-baseline/   schematic renders (NOT screenshots)
  SHADERS_AND_VIEW_DISTANCE.md         view distance / performance / shaders
  legacy/                              the old Python command-stream generator (do not revive)
```

## Hard rules

1. **`core/` has no Minecraft imports.** Never add one. That is what makes the preview tool and
   the offline verification possible, and it is the fastest part of the dev loop.
2. **Determinism.** Everything is hashed from `(seed, coordinates)`. Never use `Random`,
   `System.currentTimeMillis()`, `Math.random()`, or iteration order of a `HashMap`. Same seed
   must give the same world in any generation order.
3. **Containment.** Every feature stays inside the region its owner handles. The margins are
   `MARGIN_XZ = 72`, `MARGIN_Y = 56` in `Megastructure.java`. If you add a feature that reaches
   further, raise the margins or cubes will show cut-off seams. Add your feature to
   `tools/verify/SeamCheck.java` so this is enforced.
4. **Cost.** Every primitive rejects by bounding box first. Bench with
   `java -cp build/pv Preview bench 3000` and do not regress the ~0.3 ms/cube figure.
5. **Never** ship the old v1 `blamegen` jar with the v3/v4 core: `Mat.COUNT` is a
   `static final` compile-time constant, so a v1 adapter + v3 core = `ArrayIndexOutOfBoundsException`
   at world creation. Rebuild the jar or drop it.
6. **Toha is a fixed monument.** Keep its geometry coordinate-authored and seed-invariant in
   `Toha.java`; do not restore randomized district selection, arbitrary tower placement, or billboard
   dressing. Update `docs/TOHA_MONUMENT_DESIGN.md` and `TohaMonumentCheck` when changing its design.

## The dev loop (no Minecraft needed)

```
set JAVA_HOME=%APPDATA%\ElyPrismLauncher\java\java-runtime-epsilon   # any JDK 8+ works
javac -nowarn -d build/pv mod/src/main/java/blame/blamegen/core/*.java tools/preview/Preview.java tools/verify/*.java
java -cp build/pv blame.blamegen.core.Probe        # determinism, ceiling wells, block census
java -cp build/pv blame.blamegen.core.SeamCheck    # containment bands, extreme tiers, spawn
java -cp build/pv blame.blamegen.core.Survey       # coarse-cell parameters -> camera coords
java -cp build/pv blame.blamegen.core.LandmarkSurvey
java -cp build/pv Preview bench 3000
```

Render (yaw 0 = +Z south, 180 = -Z north; pitch < 0 looks up):

```
java -cp build/pv Preview persp out.png x y z yaw pitch fov w h dist
java -cp build/pv Preview section out.png z cx cy width height [scale]
java -cp build/pv Preview slice   out.png y cx cz size [scale]
```

Use `Survey` / `LandmarkSurvey` first to get camera coordinates that are not buried inside
geometry — a camera inside a slab renders a black image and tells you nothing.

## State of play (2026-10-11)

- **Core is v4 and fully verified offline.** See `HANDOFF_2026-10-11.md`.
- **The mod jar is the blocker.** `mod/build.gradle` had to be written from scratch (the
  original handoff zip lost it). RFG now resolves MC 1.12.2 but fails compiling Minecraft's own
  sources: the decompiled `WorldSettings.java` is missing its inner `GameType` enum and the
  Twitch/log4j deps are not on the patched-MC compile classpath. See
  `HANDOFF_BUILD_STATE.md` for the three candidate fixes — this is the highest-value next task.
- **The client has never been launched** for v3/v4. Fog handler, CubicChunks light propagation,
  in-game look of the shapeless districts, and performance are all untested.
- `tools/forge/SrgMcpChanger.java` is a newer, standalone attempt at the reobf step; it is not
  yet wired into a working build.

## If you change the generator

1. Run the four verification commands above. All must pass.
2. Re-render the previews you are touching into `docs/previews-<next>` so the visual record
   matches the code.
3. Bump `Megastructure.GENERATOR_VERSION` when the *shape* of generated content changes.
4. Update `HANDOFF_2026-10-11.md` (or append a new dated handoff) with what you did,
   and update `FILE-MANIFEST.md` in the packaged handoff if you package one.

## Commit style

Short imperative subject, then a body that says why, not what. Prefix with the area:
`core:`, `arrival:`, `tools:`, `build:`, `docs:`. Example:

```
arrival: add settlement ruins with interior props

The inventory calls for tiny domestic scale against colossal architecture, so
ruins now include lockers, bunks, terminals and crates, not just rubble.
```
