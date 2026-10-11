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

## Update (Toha Heavy Industries, generator v6)
- New `core/Toha.java`: a fixed 150 km x 150 km complex centred at (0, -90000), x/z = +-74880 around it. Spawn is
  ~15 km south of its south wall (z = -15120). Hooked in `Megastructure.generate`: inside the footprint, for cubes with
  y0 >= 0, the lattice/walls/coarse/beacons are skipped and `Toha.draw` runs; below y 0 the abyss lattice is untouched.
  Height stays under the strata ceiling (everything <= y 2075).
- Contents: deck (y 0..63); 120-thick perimeter wall to y 1960 with a 800 x 700 gate in each side, grooved faces, lit
  strips, corner bastions; a lit TOHA sign (120-block pixels) on the outer face of the south wall above the gate;
  central spire (r 420 -> 70, to y 2040) with fins; 156 x 156 grid of 960-block cells separated by lit roads, each owning
  one district (furnace stacks, machine halls, tank farms, cooling towers, pipework, gantry yards, slag canals,
  container yards); the 8 x 8 cells around the spire are a lamp-obelisk plaza.
- Verified offline on top of master (opencode's Pylon): Probe determinism PASS, SeamCheck PASS, bench 0.145 random /
  0.301 arrival, Toha cubes 0.072 ms. Renders in `docs/previews-v6/` (sign section + two in-complex views).
- NOT done: SeamCheck has no Toha-specific containment check yet (features are bounded to the 888-block cell inner square by
  construction; eyeball in renders); no `/blame tp toha` command; no neon/lighting pass; the south wall is not visible
  from the beach because the lattice occludes it. Mod jar still has to be built by CI.

## Update (`/blame` command)
- `BlameCommand.java` + registration in `BlameGen.serverStarting`: `/blame list`, `/blame tp <landmark>`,
  `/blame tp <x> <y> <z>`. Landmarks: beach, toha-gate, toha-south, toha-north, toha-east, toha-west, toha-center.
  Permission level 0 (works with cheats off). Only runs in the BLAME overworld. A floor-finder reads blocks around the
  target (+-48 y) so the cubic world generates them, then snaps to solid floor with 2 blocks of headroom.
- Type-checked only against hand-written stubs of the 1.12.2 API (no MC jar in the sandbox); the real compile happens in CI.
  If CI fails, the likely suspects are the `CommandBase` signatures (`getTabCompletions`, `checkPermission`).
- Not covered: ocean spires as landmarks (their standing height is not known without reading the world), a Toha sign viewpoint.

## Update (Toha look pass, v7 WIP; session cut off by usage limit)
- New styles `S_TOHA_MASS/PALE/DECK/RING/TANK` (Mat 73-77, Styles.java): near-black screentone masses, hatched pale concrete, tiled deck plates, ring-only curved shells. Toha no longer uses the orange/teal camo styles or the white/black speckle `S_METAL` (now ANDESITE).
- `Toha.java`: road portal gantries, ground clutter blocks, fat-obelisk plaza, new `monoliths` district (black slabs with pilasters, sparse lit windows, cyan slits), fewer/cooler wall lights.
- `Preview.java`: `BLAME_NIGHT=1` env renders night (moonlit blue-grey, emissives glow). Use `-Xmx5g` and maxD <= 2200 or it runs out of memory.
- Renders in `docs/previews-v7/` (gate_approach, spire, monoliths, stacks, hall) were made BEFORE the last style fix (RING/TANK) so stacks/tanks there still look speckled.
- NOT done: re-render after RING/TANK change; Probe/SeamCheck/bench re-run (portals/clutter/monoliths unverified for containment and cost); GENERATOR_VERSION not bumped; not compiled in CI.
