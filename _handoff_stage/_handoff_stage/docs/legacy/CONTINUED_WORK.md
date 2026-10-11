# Continuation notes — 2026-10-10

## Changes made
- Corrected CubicChunks configuration key to `forceLoadCubicChunks=ALWAYS`, replacing the unrecognized `forceCubicChunksCreation` entry.
- Aligned the Prism metadata's Forge version to 14.23.5.2860, the project's documented target. This is a configuration consistency fix, not proof of a successful launch.
- Made optional OptiFine/BSL downloads opt-in via `BLAME_INSTALL_OPTIONAL_SHADERS=1`; shader compatibility is not verified and should not jeopardize base startup.
- Added `project_source/tools/audit_stream.py` to check dispatch references, unique sentinels, sentinel/detector coordinate agreement, fill volume limits, and vertical extent.
- Corrected the validation checklist: sentinels are deliberately outside visible geometry and should match dispatcher `detect` coordinates, not be inside a fill box.

## Audit result
The existing active stream set has 14,664 cell functions and 20 dispatcher functions. Every cell is referenced exactly once; no dispatcher references are missing; each cell's final `structure_void` sentinel matches its detector coordinate; sentinel positions are unique; no `/fill` exceeds 32,768 blocks. The inspected fill Y extent is -4096 to 6122.

## Still not solved
- The active 14,664-cell stream output still does not have a true seed-driven source generator. The older 123-function reference generator remains a different implementation. This needs a genuine procedural-engine pass; do not describe the current validator as a generator.
- Fresh Minecraft launch, CubicChunks activation, World Primer runtime triggers, spawn appearance, high/low traversal, and shader compatibility have not been tested in-game.
- The workspace's current cell shapes are only as good as the original output. This pass prioritizes a corrected configuration and stronger reproducible validation before risky geometry changes.
