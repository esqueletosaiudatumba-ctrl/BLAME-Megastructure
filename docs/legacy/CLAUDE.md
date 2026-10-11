# CLAUDE.md — BLAME! Infinite Megastructure takeover brief

You are the project's **lead Minecraft world-generation engineer, architectural designer, and QA owner**. Continue the existing project in this directory. Do not restart from a blank world or return only a plan. Inspect the actual files first, identify the weakest technical and visual points, then make and verify concrete improvements.

## The user's goal

Create a genuinely enormous, explorable Minecraft environment inspired by the Megastructure from Tsutomu Nihei's *BLAME!*: ancient, impersonal, hostile, beautiful, strange, and almost impossibly large. The player must feel tiny. It must not read as a normal futuristic city, a boxy mega-base, a uniform grid of rooms, or a collection of unrelated landmarks.

The essential contrast is:
- **Absurd scale:** walls as horizons; shafts disappearing above and below; giant structural ribs, suspended masses, deep voids, stacked infrastructure, visible layers extending beyond sight.
- **Claustrophobia:** narrow service passages, low ceilings, cramped access corridors, ladders, thin bridges, and constricted thresholds within the gigantic space.
- **Emptiness:** long periods without enemies, mobs, other people, or activity. Let silence and negative space do real work.
- **Hostility:** architecture is indifferent to humans; drops, sealed paths, dead ends, unreachable infrastructure, oppressive geometry. Keep the intended exploration route fair and readable.
- **Strangeness with coherence:** repeating motifs are allowed, but their layout and scale change organically. Use broken ellipses, irregular shells, diagonal/offset ribs, shifted structural bays, hanging fragments, unfinished arcs and voids—not a monotonous square grid and not random noise.
- **Beauty:** major silhouettes, long sightlines, layered occlusion, carefully placed light and contrast. Details are subordinate to massing and composition.
- **Age:** huge parts remain functional/intact; show selective damage, not generic rubble or fantasy overgrowth.

The player specifically complained that an earlier version was **too small, pathetic, ugly, and not procedural enough**. Address scale, architectural composition, and real procedural generation before decorative details.

## Current implementation target

- Launcher: ElyPrism / Prism Launcher style instance archive.
- Game: Minecraft Java **1.12.2**, chosen because the usable CubicChunks line is for this legacy version. Do not switch it back to Java 26.3; that version cannot simply run this CubicChunks build.
- Mod loader: Forge. The active `mmc-pack.json` currently says **14.23.5.2847**, but the reference kit README says **14.23.5.2860**. This is a known inconsistency: verify the exact CubicChunks and World Primer compatibility and make the configuration/README agree. Do not blindly change the version without checking compatibility.
- Vertical range requested in `minecraft/config/cubicchunks.cfg`: `defaultMinHeight=-4096`, `defaultMaxHeight=8192`.
- Core mods expected by the bootstrap script: CubicChunks 1.12.2 and World Primer 1.12.2. OptiFine + BSL is optional and must be treated as optional until the base game launches cleanly.
- Required quality target: a bright, open-air **beach arrival area locked to clear noon**, with sky visible above it; beyond the beach, the huge architecture is darker and increasingly oppressive. The current setup function sets time to 6000, disables daylight/weather cycles, clears weather, disables mob spawning, and teleports to about `(0, 66, 210)`.

## What is in this directory

This folder is intentionally also the root of a Prism instance. Keep `instance.cfg`, `mmc-pack.json`, `bootstrap.ps1`, `minecraft/`, `blame.png`, and the overview image at this level so it remains importable as an instance.

- `minecraft/saves/BLAME_Infinite_Megastructure/` — current world shell and generated legacy function files. Inspect the full directory before changing world/runtime assumptions.
- `minecraft/config/cubicchunks.cfg` — extended vertical height configuration.
- `minecraft/config/worldprimer.cfg` — player-join setup and timed dispatch configuration.
- `bootstrap.ps1` — first-launch downloading of CubicChunks, World Primer and optional visual extras. Audit URLs, version compatibility, failure behavior, security and user experience.
- `minecraft/saves/BLAME_Infinite_Megastructure/data/functions/blame/` — the currently generated functions. In the delivered archive this contains approximately **14,664 `stream/` functions** plus dispatch and setup functions.
- `project_source/reference_kit/BLAME_CubicChunks_Procedural_Kit/source/build_cubic_pack.py` — an earlier procedural generator, accompanied by the 123-function fixed-sector kit under that reference kit. **Important:** this earlier generator does not exactly reproduce the 14,664-function streaming build in the active instance. Treat that mismatch as technical debt, not as proof the current instance can be regenerated reproducibly. Your job should be to reconcile them into a deterministic, documented source of truth.
- `CLAUDE.md`, `PROJECT_BRIEF.md`, `KNOWN_ISSUES.md`, and `VALIDATION_CHECKLIST.md` — project direction and takeover notes.

## Current architecture / procedural system to investigate

The active archive includes 20 dispatcher functions and a very large number of `stream/` cell functions. Most stream files place a bounded local cell and mark a point with `minecraft:structure_void`; the dispatcher functions use spatial `execute`/`detect` checks to decide when a cell should be built. World Primer schedules dispatcher functions and calls setup on player join. This is meant to build sectors near the player's loaded area without one enormous all-at-once operation.

Do not assume this works just because the README says so. Trace the full execution chain:
1. Prism pre-launch script and downloaded mod files.
2. Forge/CubicChunks version and whether the world actually opens as a cubic world.
3. World Primer config syntax/trigger cadence for this exact World Primer build.
4. Player join -> `blame:setup` -> dispatcher schedule -> spatial check -> corresponding stream cell -> no-double-build marker.
5. Vertical behavior both above and below the origin, including negative Y and loaded columns.
6. Re-entry into generated areas, save/reload behavior, performance, and marker placement.

Be particularly alert to legacy 1.12.2 command syntax, mod-specific World Primer commands, 1.12 block IDs and metadata, command/function recursion, `/fill` volume limits, and functions that may be called repeatedly. Verify whether the marker position is inside the cell and whether it is written only after successful construction. Make the system idempotent, bounded, and debuggable.

## Architecture direction for the next iteration

Design a procedural **spatial system**, not a list of uniformly repeated rooms. Build at several scales:

1. **Arrival / beach:** expansive irregular pale sand and a shallow bay; clear noon, open sky, bright but restrained embedded lights, distant immense gateway framing the view. Keep the beach intentionally calm and readable.
2. **Threshold:** gateway piers hundreds of blocks tall, a wide but ominous mouth, increasingly compressed human-sized routes and stair/lift infrastructure.
3. **Near field:** one or two truly monumental connected voids; an enormous shaft; thin catwalks; segmented hollow drums; service passages; a few accessible routes; several occluding layers.
4. **Mid field:** broken elliptical shells and rings, offset slabs, column forests, huge diagonal or bent ribs, suspended plates, hanging cables, industrial bridges, conversion-tower-like structures and occasional sealed cavities.
5. **Far field:** silhouettes that overlap and disappear into distance, partial structures at improbable heights, huge dark gaps. Their function should be unclear but the structural language consistent.
6. **Vertical journey:** major motifs repeated at different scales across many kilometers above and below the beach, with distinct high-altitude and subterranean compositions. CubicChunks should enable real high/low exploration, not merely change the config number.
7. **Hidden discoveries:** a few deliberate anomalies (an interior sea, refuge, unusual round void or structure) separated by long quiet journeys; do not turn the whole thing into an amusement park.

Use deterministic seeds and reusable parameterized motifs, but add low-frequency spatial variation and deliberate composition. Avoid perfect square grids, solid giant boxes, too much glowing trim, and dense decoration that hides scale. Use human-scale doors, railings, service platforms and lights as reference objects against massive construction.

## Critical user experience requirements

- The user wants a **ready-to-import instance**, not a kit requiring them to create a new world, copy files, or run commands manually.
- Final delivery should be a single valid Prism/ElyPrism **instance ZIP** with `instance.cfg` and `mmc-pack.json` at archive root. It is fine to provide source separately, but the ready instance must be the main artifact.
- The world should be preconfigured to open directly into a working starting experience. If CubicChunks' storage format requires a new cubic world, automate the necessary initialization or include a valid compatible world; don't hand back a blank/template world and call it done.
- First launch may require Internet for mod downloads; explain only unavoidable external requirements. Don't make shader-download failures prevent the base instance from launching.
- Use a supported shader path only after the core instance boots. Validate shader compatibility with CubicChunks/Forge 1.12.2; do not assume any current Iris/Sodium shader solution works on this version. If OptiFine is incompatible, don't force it into `mods`; provide the best compatible alternative or leave a clear optional setup path.
- Save a backup before edits. Do not overwrite the only copy. Distinguish generated output from verified in-game behavior.

## Work plan — act, do not ask permission

1. **Audit** the active instance, current function chain, configs, and generator source. Produce a brief bug list ranked by severity.
2. **Reconcile the source of truth.** Create one deterministic generator that can fully recreate the stream-function output, dispatcher tables, configs, overview image and packaging from parameters/seed. Record seed and generator version. Make reruns reproducible.
3. **Improve the architecture macroscale-first.** Enlarge the actual visual experience and improve spatial relationships before adding small details. Integrate organic curvature, broken loops, overlapping structures, long sightlines and human-scale references. A screenshot/overview should show convincing depth and scale rather than a flat map of coordinates.
4. **Improve streaming reliability.** Bound command volume, add a clear build status/debug mode, avoid repeated placement, verify the dispatcher coverage spans all intended XYZ ranges, and ensure the origin beach appears reliably on first load.
5. **Improve the launch experience.** Correct Forge/mod version inconsistencies, make bootstrap idempotent and fail gracefully, avoid installing optional shader components as required dependencies, and verify that the archive imports as an instance.
6. **Validate** the ZIP structure, configs, references, command syntax assumptions, file counts, duplicate functions, bounds, and generator reproducibility. If a Minecraft client can be run, test a fresh import and walk the beach/near structure plus a high and low point. If it cannot, state exactly what was file-validated and what remains untested.
7. **Deliver** the revised instance ZIP plus a clear change log and, if possible, a separate source ZIP. Keep this `CLAUDE.md` updated as the project evolves.

## Acceptance criteria

- One-click-ish ElyPrism instance import; no manual file copying or command execution by the user.
- Opens to an attractive daylit beach with clear skies and intended spawn coordinates.
- Monumental structures visible immediately from the beach, not hidden until hours of wandering.
- Exploration reveals much more architecture both horizontally and across the configured vertical range.
- Organic composition, irregular broken shells, layered views, huge voids, human-scale traversals, sparse purposeful lighting and varied empty spaces.
- New architecture can be generated reproducibly from source; the active instance and generator no longer diverge.
- Launch chain and world generation are either actually tested or clearly marked untested. Never claim a Minecraft client test if none happened.
