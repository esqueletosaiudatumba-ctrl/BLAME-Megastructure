# HANDOFF: BLAME! Infinite Megastructure

Read this first. It describes the current state, what has been verified and what has not, and the next steps. The original project brief is in `docs/legacy/CLAUDE.md` and `docs/legacy/PROJECT_BRIEF.md`. The goals there still apply: a huge, oppressive BLAME!-style megastructure, a sunlit beach arrival locked to noon, a ready-to-import Prism/ElyPrism instance, and a deterministic, reproducible source.

## The big change in this pass

The earlier approach was about 14,664 command `stream/` functions triggered by World Primer, plus a Windows-only PowerShell bootstrap. It has been **replaced** by a small Forge mod, `blamegen`, that registers a CubicChunks world type named `BLAME`. Its generator builds the megastructure natively, one 16³ cube at a time, as the game generates terrain.

Why the old approach was dropped:
- Architecture popped in around the player.
- The "already built" markers fell outside their cells.
- The bootstrap script ran only on Windows.
- World Primer timing was unverified.
- The 14,664 functions could not be regenerated from source.

What the new approach gives:
- The world is infinite and deterministic: the same seed gives the same world, in any generation order.
- There is no command streaming and no bootstrap step.
- CubicChunks is MIT-licensed, so its jar can be bundled in the instance.

The old materials are kept for reference in `docs/legacy/`. The 14,664 stream functions themselves were not carried over because they are superseded.

## Repository layout

```
mod/                                  Forge 1.12.2 mod (RetroFuturaGradle, CleanroomMC TemplateDevEnv)
  src/main/java/blame/blamegen/
    BlameGen.java                     @Mod; registers the world type and server events
    BlameWorldType.java               WorldType "BLAME" + ICubicWorldType (overworld only)
    BlameCubeGenerator.java           CubicChunks ICubeGenerator: Mat id -> 1.12.2 IBlockState; Void biome everywhere
    WorldEvents.java                  new world: spawn (0,68,64), noon locked, no weather or mobs, peaceful;
                                      first join: places the player facing north toward the gate
    ClientEvents.java                 fog follows skylight (black inside the structure, blue in daylight)
    core/                             PURE JAVA generator, no Minecraft imports (shared with the preview tool)
      Megastructure.java              lattice, building volumes, decks, columns, walls, shafts, spheres, flooded halls, spires
      Arrival.java                    fixed beach, bay, shaft wall, gate, causeway, catwalks, broken ring
      Cube.java                       16³ buffer + clipped raster primitives (box, cylinder/ellipse, sphere, beam)
      Styles.java                     weathering: stains, streaks, banding
      H.java                          hashing and value noise
      Mat.java                        material ids
tools/preview/Preview.java            offline renderer for the core (perspective, slices, sections, bench)
tools/verify/                         offline verification for the core, same package (compile with the core):
  Probe.java                          determinism, ceiling-well fraction, block census per feature zone
  SeamCheck.java                      transit-tube containment bands, extreme-tier generation, spawn sanity
  Survey.java                         exact coarse-cell survey (shaft/void/sea/none + parameters for cameras)
docs/previews/                        schematic renders from Preview (NOT in-game screenshots)
docs/previews-v2/                     v2 renders: expanded bay, ocean spires, ribcage shaft, ruins, causeway
docs/previews-v3/                     v3 renders: organic, arcaded and octagonal districts
docs/SHADERS_AND_VIEW_DISTANCE.md     view-distance tuning, safe perf mods, storm shots, why shaders stay optional
docs/previews/                        schematic renders from Preview (NOT in-game screenshots)
docs/legacy/                          old brief, known issues, earlier generator, audit script, previous AI transcript
instance/                             Prism instance template files (see "Packaging")
```

## Generator design (core/Megastructure.java)

- **Coarse cells** (1024 blocks square). Each one holds a single major feature:
  - A megashaft (radius 60–200, spanning many tiers). Half of them stay open to the sky at the roofscape, so daylight reaches deep into the structure. Shafts can have perforated shells or ribcage walls, rings of lamps on ledges, bridges with central platforms, long cables, and sometimes a flooded "interior sea" at the bottom.
  - A spherical void with a ring walkway and a suspended core.
  - A flooded hall with a forest of columns.
  - Nothing.
  - Cell (0,0) is reserved for the arrival.
- **Lattice:** an irregular orthogonal grid. Lines in X and Z have jittered spacing (96–288 blocks). Tiers in Y are about 256 blocks tall, jittered, with tier 0 at Y = 64.
- **What fills each bay-tier.** 3D noise clusters voids into chasms; the rest are frames or blocks.
  - **Block:** a building volume. Shapes are box (with stepped parts), elliptical drum, cantilevered heavy top, or a cluster of towers with sky bridges. Each has facades (bands, slit windows, pilasters, rare lit windows), service corridors every 8 blocks of height that open as tiny doors, and roof machinery.
  - **Frame:** a deck (full with internal corridors, holed, cantilevered, or lattice), edge beams, a column at the corner node (square, drum, cluster, or hollow service tower), cables, catwalks, diagonal ribs, and hanging masses or towers.
  - **Void:** catwalks, cables, and suspended masses.
- **Great walls** sit on runs of grid lines. They are hollow with service floors, and have pilasters, cornices, doors, and huge vertical slots.
- **Altitude.** Below Y −1000 the structure gets denser and heavier, with more walls and masses. Above that it gets sparser, with more catwalks. `TOP_TIER = 8` puts the roofscape at about Y 2100. Above it there are only spires, which reach up to about Y 6900 and carry lamps on top, plus floating plates. There is no bottom: the structure continues for as far as the world height allows.
- **Containment.** Every feature stays inside the region its owner handles. A cube only evaluates owners within a margin: 72 blocks horizontally and 56 vertically for the lattice, and its own coarse cell for the major features. If you add a feature that crosses these limits, raise the margins, or cubes will show cut-off seams. New features obey this: transit tubes run corner-to-corner inside their owner bay (±1 tier, like the existing diagonal ribs); elevators/stairwells stay within their column node; beacons recompute their shaft/wall parameters from the same cell hashes, so every intersecting cube draws them identically.
- **Determinism.** Everything is hashed from (seed, coordinates). The arrival uses a fixed internal seed, so every world has the same beach. Verified offline: identical cubes from two generator instances, plus a block census (new lamps, pipes, patina, vents, rebar, glass, rust, biofilm all place; ceiling well fraction ~0.28 in a sampled cell).
- **Speed.** The preview benchmark measured about 0.27 ms per random cube and ~0.55 ms per arrival cube.
- **Districts.** Bay volumes pick an architectural character from low-frequency noise (`district`), so whole districts are rationalist grids, octagonal stacks, leaning/tilted squares, arcaded towns, or organic accretions of stacked rotated and sheared prisms with vaults, terraces, blobs, arcades and pooled water. New raster primitives in `Cube`: `prism` (circle/octagon/square, rotation, shear, hollow), `tiltSlab` (rotated plaque), `archRow` (stepped arcade), `blob` (ellipsoid), `ring` (hollow shell). Everything is clamped to the owner bay's footprint + margin, so no cube seams. In shapeless districts transit lines become tilted slab bridges and suspended cable platforms instead of identical cylindrical tubes.

## Arrival (core/Arrival.java)

- **Beach and bay.** A vast beach crescent and a wide, shallow bay (water at Y ≤ 62) sit inside an irregular ring wall: radius about 280, 44 blocks thick, open to the sky to the top of the world. The player spawns at (0, 65, 64), facing north (yaw 180).
- **Ocean spires.** Eight tapered monolithic columns rise from the bay water (heights ~180–460), with oxidised-copper bands, internal ladder shafts, lamp-crowned platforms and cable stays.
- **Settlement ruins.** Hundreds of small ruin clusters (collapsed domes, foundation posts, pipe clusters, stairwells, rubble with rebar) dot the shoreline near sea level, plus lean-tos at the outer wall base.
- **Wall.** The wall rises to about Y 1600, except for a notch to the north-east (top around Y 230) and a breach to the east (Y 118–380) that shows the dark lattice behind it. Its inner face has pilasters, ledges every 96 blocks, dark recesses with rare lamps, ring corridors inside it every 32 blocks, and a ladder from the beach (south wall) up to the first ledge and a door at Y 163.
- **Gate (north).** The opening is 53 wide and spans Y 66–295, flanked by grooved piers up to Y 610 with lines of lamps. A sand ramp leads up to it, and lamps line the passage.
- **Causeway.** It runs from the gate to Z −500 (Y 65), with railings, end-rod lamps, and support pylons, and ends at a landing platform. From there a ladder shaft goes down to Y −200.
- **Overhead.** Catwalks cross the shaft at Y 150, 205, 290, 480, 760 and 1100, with hanging cables. A broken cantilevered ring at Y 232 juts from the west wall. A small pier with rails stands in the bay.

## What has been verified (and how)

1. **Real dedicated server test, passed — for generator version 1.** Setup: Forge 1.12.2-14.23.5.2860, `CubicChunks-1.12.2-0.0.1301.0-SNAPSHOT-all.jar`, and `blamegen-1.0.0.jar`, with `level-type=BLAME` in `server.properties`. The log showed:
   - `Registered world type 'BLAME' (generator version 1)`
   - CubicChunks `Initializing world ... with type blame.blamegen.BlameWorldType`
   - `BLAME world created: spawn at 0 68 64, clear noon locked`
   - Spawn area generated, then `Done (8.8s)`, with no exceptions from blamegen.

   The log also contains `MixinItemMap was not found` and IntegratedServer warnings. These come from CubicChunks itself on a dedicated server and are harmless.
2. **Offline verification of generator version 2 (current core).** `tools/verify` results on this machine: determinism PASS (identical cubes from two instances); ceiling well fraction ~0.28 in a sampled cell with the arrival shaft always open; block census confirms all new materials place (warning lamps, copper pipework/patina, vents, rebar, dark glass, abyssal rust, sea-hall biofilm); transit-tube containment bands hold over 101×101×31 bay-tiers; extreme tiers (±3200..8000) generate without errors; spawnY = 65 on dry sand. `SeamCheck` and `Probe` are kept in `tools/verify/` — re-run them after any core edit.
3. **Mod build:** `./gradlew build` succeeds and produces `mod/build/libs/blamegen-1.0.0.jar`. (Last run predates the v2 core + adapter changes; rebuild before any server test.)
4. **Offline renders** of the generator core, made with `tools/preview` (see `docs/previews/` for v1, `docs/previews-v2/` for v2: expanded bay, ocean spires, ribcage shaft interior + section, shoreline ruins, causeway). These are schematic renders, not game screenshots.

## What has NOT been verified (do these next)

- **Rebuild the mod jar and re-run the dedicated server test for generator version 2.** The v1 server pass predates the ceiling strata, the expanded arrival, the new materials/adapter states, and all lattice features. Rebuild (`cd mod`, `./gradlew build`), load the new jar with CubicChunks, confirm `generator version 2` in the log and clean generation around spawn, then refresh `dist/` and the instance jar.
- **The Minecraft client has never been launched with this mod.** That means none of the following has been checked:
  - the fog handler (`ClientEvents`);
  - whether lighting from blocks placed by the generator (sea lanterns, glowstone) actually propagates in CubicChunks;
  - how things look in game;
  - client performance;
  - the first-join placement facing the gate.
- **The instance has not been imported into Prism/ElyPrism.**
- **No player has walked the world.** Traversal (gate → causeway → landing → ladder) and water containment have not been checked in game.
- **CubicChunks also made the Nether and End cubic.** With an ICubicWorldType, CC initialised dimensions −1 and 1 as cubic, using the vanilla generators through its compatibility layer. This is probably fine but untested.

### How to test the client headlessly (this worked for installation in the orb)

```
# Java: Gradle/RFG needs JDK 25 (it runs the build); the game/server need Java 8.
pip install portablemc
portablemc --main-dir ~/mc/client --work-dir ~/mc/client/game start forge:1.12.2-14.23.5.2860 -u Tester
#   copy CubicChunks + blamegen jars into ~/mc/client/game/mods first
#   run under Xvfb (sudo apt-get install xvfb xdotool mesa-utils libgl1-mesa-dri);
#   offline username login works with portablemc; add --server localhost to auto-join a test server
# Server: java8 -jar forge-1.12.2-14.23.5.2860.jar nogui with server.properties:
#   level-type=BLAME, online-mode=false, max-tick-time=-1; op the offline UUID (OfflinePlayer:<name> md5 v3 UUID)
# Take screenshots with F2 (xdotool key F2) or `import -window root`; tp around via the server console.
```

## Building

```
cd mod
JAVA_HOME=<jdk25> ./gradlew build          # first run downloads MC + decompiles (~3 min)
# output: build/libs/blamegen-1.0.0.jar
```
- The CubicChunks dependency comes from Modrinth maven (`maven.modrinth:cubicchunks:1.12.2-0.0.1301.0`), deobfuscated by RFG. It is set in `gradle/scripts/dependencies.gradle`.
- The mod's `Tags` class is generated as `blame.blamegen.Tags`.

### Preview tool (fast visual iteration without Minecraft)

```
javac -d /tmp/pv mod/src/main/java/blame/blamegen/core/*.java tools/preview/Preview.java
java -cp /tmp/pv Preview persp out.png 0.5 69.6 64.5 180 -12 80 960 540 300   # spawn view
java -cp /tmp/pv Preview section out.png 2600 0 0 4000 9000 6                    # tall vertical section
java -cp /tmp/pv Preview slice out.png 70 0 0 1400 2                             # horizontal slice
java -cp /tmp/pv Preview bench 3000
```

### Verify tools (determinism, containment, census — no Minecraft)

```
javac -d /tmp/pv mod/src/main/java/blame/blamegen/core/*.java tools/preview/Preview.java tools/verify/*.java
java -cp /tmp/pv blame.blamegen.core.Probe       # determinism + ceiling wells + block census
java -cp /tmp/pv blame.blamegen.core.SeamCheck   # tube containment bands + extreme tiers + spawn
java -cp /tmp/pv blame.blamegen.core.Survey      # coarse-cell parameters (use for preview cameras)
```
- The preview's lighting is fake: a surface counts as lit if it has open sky above it.
- Fog turns dark when the camera is not under open sky, which roughly matches the mod's client fog.
- If the camera is placed inside solid structure, the image comes out black.

## Packaging (Prism / ElyPrism instance)

`instance/` holds the template. The instance ZIP must have `instance.cfg` and `mmc-pack.json` at its root, with this layout:
```
instance.cfg, mmc-pack.json (MC 1.12.2 + Forge 14.23.5.2860), blame.png
minecraft/mods/CubicChunks-1.12.2-0.0.1301.0-SNAPSHOT-all.jar   (MIT, bundled; sha1 39045bec72ea7400b91d81c0a14f3d8a685b266d)
minecraft/mods/blamegen-1.0.0.jar
minecraft/config/cubicchunks.cfg
minecraft/options.txt
minecraft/saves/BLAME_Infinite_Megastructure/   (level.dat + data/ created by the real server, generated chunks stripped)
```
- No PowerShell, World Primer, or downloads are needed. CubicChunks requires Forge ≥ 14.23.5.2859, so the Forge version is pinned to 2860.
- The shipped `level.dat` was patched to creative mode with cheats on, so the player can fly through a structure several kilometres tall. Switch to survival with `/gamemode 0`.
- **Shaders are not included.** OptiFine and CubicChunks compatibility was never verified, so do not add OptiFine as a required mod.

## Known issues and ideas, in priority order

1. Run the client test above. Take screenshots of the beach at spawn, the gate, the causeway, a megashaft, and the roofscape (about Y 2100), and fix whatever looks wrong.
2. **Readability in the dark.** Outside the arrival and the sky-open shafts there is no skylight, so the structure relies on lamps. Tune these lamp densities against real screenshots: lit windows (1/64 facade recesses), column lamps, deck underside lamps, and shaft lamp rings.
3. **The structure can still read as a grid of boxes.** The next design levers are more curvature (broken elliptical shells around bays, tilted ribs), non-orthogonal megawalls, and larger scale variation between districts.
4. **Traversal.** A guaranteed human-scale route from the causeway landing into the lattice is not designed yet. Catwalks and corridors exist but are random.
5. **Height range.** The world height comes from `cubicchunks.cfg`: `defaultMinHeight`/`defaultMaxHeight` when the world is created. The test world used CubicChunks' defaults (±2^30, effectively infinite). The old brief asked for −4096..8192. Either works with the generator.
6. **Feature margins.** If you add a big feature, respect the containment margins (see Generator design), or cubes will show seams.
