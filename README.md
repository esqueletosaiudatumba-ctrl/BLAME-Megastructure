# BLAME! Megastructure — Minecraft / CubicChunks

A Forge 1.12.2 + CubicChunks project that procedurally generates an immense, deterministic, BLAME!-inspired megastructure. The generator aims for colossal industrial architecture, deep vertical spaces, oppressive emptiness, rare inhabited fragments, and stark artificial light.

## Start here

- [Grand goal and visual direction](GRAND_GOAL_VISION.md) — creative target and embedded visual references.
- [Agent instructions](AGENTS.md) — repository layout, hard rules, build/verification workflow.
- [Current handoff](HANDOFF_2026-10-11.md) — latest project status and next steps.
- [Build and verification status](HANDOFF_BUILD_STATE.md) — latest Forge/RFG state, offline checks, and remaining in-game verification.
- [Toha monument design](docs/TOHA_MONUMENT_DESIGN.md) — the fixed, manga-faithful megastructure landmark and its dimensions.
- [Shader and view-distance notes](docs/SHADERS_AND_VIEW_DISTANCE.md) — rendering/performance context.

## Repository map

- `mod/` — Forge 1.12.2 mod and pure-Java world-generation core, including the authored Toha Heavy Industries monument.
- `tools/preview/` — offline renderer for inspecting generated geometry without launching Minecraft.
- `tools/verify/` — determinism, containment/seam, and generator survey checks.
- `tools/forge/` — build/remapping utilities.
- `docs/` — reference material and generated preview renders.
- `instance/`, `instance-pkg/` — launcher-instance and packaging assets.
- `raw-data/` — source/reference data.

## Development principles

1. Read `AGENTS.md` and `GRAND_GOAL_VISION.md` before changing generator or visual content.
2. Keep `mod/src/main/java/blame/blamegen/core/` pure Java: no Minecraft imports.
3. Preserve deterministic output for the same seed and coordinates.
4. Check feature containment and seam margins when adding geometry.
5. Never pair the legacy v1 `blamegen` adapter with the newer v3/v4+ core; rebuild compatible artifacts.

## Offline verification

With JDK 8+ available, compile and run the checks from the repository root (Windows command syntax):

```bat
mkdir build\pv
javac -nowarn -d build\pv mod\src\main\java\blame\blamegen\core\*.java tools\preview\Preview.java tools\verify\*.java
java -cp build\pv blame.blamegen.core.Probe
java -cp build\pv blame.blamegen.core.SeamCheck
java -cp build\pv blame.blamegen.core.Survey
java -cp build\pv blame.blamegen.core.LandmarkSurvey
java -cp build\pv Preview bench 3000
```

These offline checks do not replace launching the Minecraft client. See the current handoff documents for known limitations and the latest status.

## Status

The current handoff documents generator v8 and an authored Toha monument beside the arrival basin. Offline core checks and the Forge 1.12.2 build passed on recent revisions; the newest v8 geometry and packaging edits are being revalidated in GitHub Actions. The Minecraft client has not been launched for final visual, traversal, lighting, or performance sign-off.
