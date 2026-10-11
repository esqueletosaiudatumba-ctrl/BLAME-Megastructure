# WIP v5 (does NOT compile yet) — branch `wip-v5`

Requested by user: (1) `/blame tp` commands to landmarks, (2) beach 10x bigger (done as S=sqrt(10) horizontal scale),
(3) ocean spires reaching the strata roof, detailed brutalist, (4) looks, (5) everything touches strata roof AND a new
strata floor, (6) cyberpunk lighting (neon glass over sea lanterns; no sunlight).

## Done in this branch (uncommitted-to-master)
- `Arrival.java`: scale constant S, ARC_K, R_MAX, SPAWN_Z=202, scaled ring/bay/gate/causeway/catwalks/pier, wall climbs to
  the ceiling, `inBasin/oculus/deepInside`, continuous sky arches, spire list `SPIRES` calling `Pylon.draw(...)`.
- `Megastructure.java`: GENERATOR_VERSION=5, generate() order (basin stamped before ceiling, lattice skipped deep inside),
  `ceilWell` uses `arrival.oculus` (sky opening over south beach only).
- `Face.java`: SECTOR scaled by 1/S.

## Still to do
1. **Write `core/Pylon.java`** (referenced by Arrival, missing): `static void draw(Megastructure m, Cube c, int px, int pz,
   int yBase, int yTop, int halfWidth, long hash, int seaLevel)`. Design: chamfered-square hollow shell, ziggurat setbacks,
   axis fins with neon tip strips, deep-set window grid (glass behind 5-block reveal), board-formed concrete bands, rain streaks,
   cantilevered volumes with diagonal struts, flared capital merging into the ceiling plate, plinth with arches at the waterline,
   interior floors every 16 + central ladder core.
2. Strata floor: `FLOOR_BASE ~ -3300`, plate + strata below, lit floor grid; skip lattice below it. Make shafts span floor->ceiling,
   extend top-tier columns / drawSky to the ceiling, add floor-to-roof megacolumns (reuse Pylon).
3. Cyberpunk lights: new Mats (coloured glass cyan/magenta/amber/red/purple + accent concrete), adapter mappings in
   `BlameCubeGenerator`, Preview colours, and a `Neon` post-pass (coloured glass over sea lantern, strips, sign panels).
4. `/blame` command (list, tp <landmark>): needs core landmark enumeration (Landmark.centre uses the WORLD seed via
   Megastructure(world.getSeed())), a safe-ground finder that generates cubes to find a floor, command registered in
   FMLServerStartingEvent. Spires/gate/beach coordinates: `Arrival.spires()`, SPAWN_X/Z, gateInnerZ.
5. WorldEvents: set world spawn on WorldEvent.Load too (old saves keep old spawn). Users must delete old cubes / make a new world
   after the generator change.
6. Run Probe/SeamCheck/bench + Preview renders, then merge to master (CI builds jar, packages instance).
