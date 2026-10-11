# Validation Checklist

## Packaging / ElyPrism
- [ ] `instance.cfg` and `mmc-pack.json` at root of ZIP; importing as an instance works.
- [x] Active Forge version is aligned to the documented target (14.23.5.2860); CubicChunks runtime compatibility still needs in-game confirmation.
- [ ] Bootstrap downloads core mods to `minecraft/mods/`, verifies real JARs, logs failures and is safe on repeat launch.
- [x] Optional OptiFine/BSL downloads are skipped by default and cannot block the core launch.
- [ ] Instance name and icon are right; no manual create-world/copy-file/command step is needed by the user.

## Runtime
- [ ] Fresh import launches to title screen.
- [ ] CubicChunks is active and high/low Y movement works as intended.
- [ ] Fresh world/save opens without `Unknown data version`, invalid level data or conversion failure.
- [ ] Join triggers setup exactly once or safely idempotently.
- [ ] Spawn at `(0,66,210)` or documented alternative lands on solid beach, not inside a block or over water.
- [ ] Noon (`time 6000`) and clear weather persist through save/reload.
- [ ] Beach surface exposed to open sky; lighting looks bright and attractive.

## Stream / functions
- [x] Every streamed function reference resolves to a file; audit script confirms all 14,664 stream cells are referenced once.
- [ ] World Primer command names and trigger syntax match the actual mod build.
- [ ] Dispatches activate at nearby XYZ regions and work at negative Y.
- [x] File-level check confirms each stream function writes a unique sentinel matching its dispatcher detector. Runtime idempotence still needs in-game confirmation.
- [ ] Marker location is inside the corresponding cell and does not falsely mark a partially built cell as complete.
- [ ] No function call loops or command chains exceed safe/performance limits.
- [x] `/fill` volumes in stream functions are at most 26,400 blocks (under the 32,768 limit). Block registry validity still needs runtime confirmation.
- [ ] The active 14,664-cell output still lacks a true seed-driven generator; see CONTINUED_WORK.md.

## Art / exploration
- [ ] First viewpoint shows a clear, huge composition from the beach.
- [ ] Beach-to-gateway transition is readable.
- [ ] At least one monumental void, shaft and thin catwalk system establish scale.
- [ ] Distinct near-, mid-, and far-field layers overlap with real occlusion.
- [ ] Geometry is organic but structurally coherent; no monotonous grid and no random noise.
- [ ] There are quiet empty areas as well as cramped passages.
- [ ] Large connected motifs extend substantially above/below the beach.
- [ ] Lighting is sparse and purposeful; shaders if included are compatible and optional.

## Reporting honesty
- [ ] Report exact tests performed and results.
- [ ] Separate file-level/archive checks from actual Minecraft playtesting.
- [ ] Attach a screenshot or render if available; don't call a generated schematic an in-game screenshot.
- [ ] Keep a change log and preserve a backup before replacing a release artifact.
