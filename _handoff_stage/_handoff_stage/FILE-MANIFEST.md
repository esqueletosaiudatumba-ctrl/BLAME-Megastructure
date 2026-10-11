# FILE MANIFEST

READ FIRST (in this order)
1. `HANDOFF_BUILD_STATE.md` - where the Forge mod-jar build stopped and how to fix it.
2. `HANDOFF_2026-10-11.md` - the session delta: v1 -> v3 -> v4 generator changes, verification, the FML registry fix, packaging.
3. `HANDOFF.md` - the original project architecture.

## Mod source (`mod/`) - generator version 4
- `mod/build.gradle` - **NEW** (written from scratch; the handoff zip had none)
- `mod/settings.gradle`, `gradle.properties`, `gradle/scripts/dependencies.gradle` - modified
- `core/Face.java` - **NEW**: sculpted shaft-wall facades (7 styles)
- `core/Landmark.java` - **NEW**: 5 landmark types (PIPES, MONOLITHS, WHEEL, CATHEDRAL, HELIX)
- `core/Mat.java`, `core/Cube.java`, `core/Megastructure.java`, `core/Arrival.java`,
  `core/Styles.java`, `BlameCubeGenerator.java` - modified
- everything else in `mod/` - unchanged

## Tools
- `tools/verify/SeamCheck.java` - containment bands, extreme tiers, spawn
- `tools/verify/Probe.java` - determinism + ceiling wells + block census
- `tools/verify/Survey.java` - coarse-cell parameters + camera coordinates
- `tools/verify/LandmarkSurvey.java` - **NEW**: landmark cells for cameras
- `tools/verify/NbtRegistryFix.java` - **NEW**: strips the stale CubicChunks FML registries from level.dat
- `tools/preview/Preview.java` - modified
- `tools/package-instance.ps1` - stages the ElyPrism zip
- `tools/make-zip.ps1` - zips with spec-compliant forward slashes

## Docs and renders
- `docs/SHADERS_AND_VIEW_DISTANCE.md`
- `docs/previews-v2/`, `v3/`, `v4/` - schematic renders (not game screenshots)
- `docs/previews-baseline/` - v1 baseline for before/after comparison

## Verification output (`verification-v4/`)
- `seam-and-containment.txt` - SEAMCHECK PASS
- `determinism-and-census.txt` - determinism PASS, census
- `coarse-cell-survey.txt`, `landmark-survey.txt`, `bench.txt`

## Instance packaging (`instance-ready-to-package/`)
Complete ElyPrism layout except `minecraft/mods/blamegen-1.0.0.jar`, which cannot be built here
(see HANDOFF_BUILD_STATE.md). The save's `level.dat` is already registry-fixed.

## Deliberately NOT included
- `mod/build/`, `build/`, `.gradle` caches - large, regenerable
- the reference images and `blame_megastructure_ai_inventory.md` (outside the project dir)