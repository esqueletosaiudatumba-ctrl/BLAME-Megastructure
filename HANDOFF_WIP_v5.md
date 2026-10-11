# v5 status — compiles and passes verification (branch merged to master)

Requested by user: (1) `/blame tp` commands to landmarks, (2) beach 10x bigger, (3) ocean
spires reaching the strata roof, detailed brutalist, (4) looks, (5) everything touches strata
roof AND a new strata floor, (6) cyberpunk lighting.

## Done
- `Arrival.java`: scale constant `S`, `ARC_K`, `R_MAX`, `SPAWN_Z=202`, scaled ring/bay/gate/
  causeway/catwalks/pier, wall climbs to the ceiling, `inBasin`/`oculus`/`deepInside`,
  continuous sky arches, spire list `SPIRES` calling `Pylon.draw(...)`.
- `Megastructure.java`: `GENERATOR_VERSION` bumped, `generate()` order (basin stamped before
  ceiling, lattice skipped deep inside), `ceilWell` uses `arrival.oculus`.
- `Face.java`: SECTOR scaled by 1/S.
- **`core/Pylon.java` written** — this was the missing piece that broke the build.
- Canon day-night underside light panels on the ceiling strata (`drawCeiling`).

## Verification
- Compiles clean with the pure-Java core.
- `SeamCheck` PASS, `Probe` determinism PASS, bench ~0.26 ms/random cube, ~0.48 ms/arrival cube.
- Renders in `docs/previews-v5/` (`sec_full.png` shows a pylon from seabed to roof).

## Still to do
1. Strata floor: `FLOOR_BASE ~ -3300`, plate + strata below, lit floor grid; skip lattice
   below it. Make shafts span floor -> ceiling.
2. Cyberpunk lights: full coloured-glass palette over sea lanterns.
3. `/blame` command (list, tp <landmark>): needs a safe-ground finder that generates cubes to
   find a floor, command registered in FMLServerStartingEvent.
4. WorldEvents: set world spawn on WorldEvent.Load too.
5. **Users must delete old cubes or make a new world after this change** — the generator hash
   space changed, so old save data and new data do not match.

## Update (night time)
- World clock is now locked to midnight (18000) instead of noon: set on world creation and re-applied on every
  `WorldEvent.Load` so old saves switch too. Adapter code only; needs the CI Forge build to be compiled.
- The beach and bay have no sunlight now; the client fog (`ClientEvents`) follows sky colour, which is near-black at night.
