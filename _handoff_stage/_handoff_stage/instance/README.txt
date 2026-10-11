BLAME! INFINITE MEGASTRUCTURE - Prism / ElyPrism instance

INSTALL
1. In ElyPrism/Prism: Add Instance > Import > choose this ZIP.
2. Launch. The launcher downloads Minecraft 1.12.2 and Forge 14.23.5.2860 (needs Java 8;
   the launcher can auto-download it).
3. Singleplayer > BLAME_Infinite_Megastructure.

Everything else is bundled: CubicChunks (MIT) and blamegen, the mod that generates the
megastructure. No scripts, no downloads by the instance, no commands.

You can also create new worlds: Create New World > More World Options > World Type:
"BLAME! Megastructure". The world starts in creative mode with cheats on so you can fly;
/gamemode 0 for survival.

FIXED: "Forge Mod Loader detected missing/unknown registrie(s)"
-------------------------------------------------------------
The shipped BLAME_Infinite_Megastructure save used to show this on load, for
cubicchunks:storage_format_provider_registry and
cubicchunks:vanilla_compatibility_generators_registry. Cause: the bundled
CubicChunks 0.0.1301.0 does not register those two FML registry names (they belong to a
newer CubicChunks), and the save was created by one. The fix removes the two stale keys
from Data.fml.Registries in level.dat - nothing else in the save is touched.

- The level.dat shipped in THIS zip is already fixed.
- If you see the error on an older save, expert the tool (also in the source handoff):
      java -cp . NbtRegistryFix <path to level.dat>
  It is idempotent and safe to re-run; back up the world first as good practice.
- "Yes" on the prompt also removes them, but doing it deliberately is cleaner.

WHAT IS IN THIS BUILD (generator v3/v4)
- A lightless ceiling of dense strata above the roofscape; only sky-open shafts, vents and
  grid-line slots let daylight in. The deep structure is genuinely dark - raise gamma if you
  want to see more.
- Architectural districts: rationalist grids, octagonal stacks, leaning/tilted masses,
  arcaded towns (vaults + colonnades), and organic shapeless accretions of stacked rotated
  prisms with terraces, blobs and pooled water. Whole districts share one character, so the
  world never reads as one repeated module.
- Landmarks in the previously empty cells: vast pipe forests, floating monoliths, a huge
  free-standing wheel, a hanging cathedral, and a helix with a spiral ramp.
- A sculpted arrival shaft wall (ribcages, arcades of niches, terraces, cliff slots, blast
  holes, pipe-organ fins) and six colossal broken arches spanning the shaft above the beach.
- Expanded arrival: a wide beach crescent and bay inside a ~280-radius ring wall, eight
  tapered ocean spires, and small human settlement ruins along the shoreline and wall base.
- Passageways: X-orientation transit lines (corner-to-corner tubes, angular rib cages,
  cable-stayed shuttles, tilted slab bridges), elevators/spiral stairwells/lifts, debris
  fields, fractured decking, ruptured plates.
- Machines and lore objects: colossal doors, blast doors, checkpoint booths, signage strips,
  stairs to nowhere, abandoned rail viaducts with fallen cars, construction cranes, tank
  clusters, district mega-pipes, server vaults, factory halls, ruined defense stations,
  Safeguard beacons with red/amber/green lamps, copper pipework, rust, vents, rebar.
- New materials: warning lamps, glowing pipe casing, copper patina/raw/weathered, rust
  concrete, biofilm growth (cyan and magenta in deep flooded halls), vents, rebar, dark glass.

STATUS (honest): the pure-Java generator core is verified offline - deterministic output,
containment margins hold, all new materials place, ~0.3 ms per cube. The bundled mod jar is
the previously built one; the v4 mod jar itself has not been rebuilt in this packaging step
(Forge/RFG build environment issue, see HANDOFF_BUILD_STATE.md in the source handoff), so
this instance shows the v1-era worldgen until the mod is rebuilt.

Shaders are NOT included and are NOT recommended: OptiFine 1.12.2 and CubicChunks are known
incompatible upstream, and 1.12.2 shaderpacks load through OptiFine. Weather is locked clear
and noon by design; /weather thunder works manually for storm shots.

World height comes from cubicchunks.cfg (shipped: effectively unlimited).
