BLAME! INFINITE MEGASTRUCTURE — Prism / ElyPrism instance

PACKAGE STATUS — READ BEFORE LAUNCHING
--------------------------------------
This package contains Minecraft 1.12.2, Forge 14.23.5.2860, CubicChunks
0.0.1301.0-SNAPSHOT, the registry-fixed BLAME save, and the matching
blamegen-1.0.0.jar for the authored Toha monument build.

The source generator and JAR build have passed GitHub Actions offline tests on recent revisions; generator v11 is being revalidated after hollow-shell and cave-machine changes:
TohaMonumentCheck, Probe (determinism), SeamCheck, Survey, LandmarkSurvey,
the preview renderer, and the Forge Gradle build. This is not yet an in-game
visual/performance sign-off: launch Minecraft and inspect the monument before
calling the instance fully verified.

INSTALL
-------
1. In ElyPrism/Prism: Add Instance > Import > choose this ZIP.
2. Launch using Java 8 (64-bit). The launcher can auto-download it.
3. Select Singleplayer > BLAME_Infinite_Megastructure, or create a NEW world
   and choose the "BLAME! Megastructure" world type.
4. Use the included generator JAR; do not replace it with an older blamegen JAR.
5. In the BLAME world, use:
     /blame list
     /blame tp toha-gate
     /blame tp toha-center
   The gate landmark places you at the monumental south portal. The centre
   landmark places you within the principal cave mouth, facing the gravity furnace.

TOHA HEAVY INDUSTRIES — AUTHORED MONUMENT
-----------------------------------------
Toha is now a single fixed cylindrical megastructure, not the old randomized
150 km industrial district grid. Its anchor is X=0, Z=-4608; the 5.6 km circular
precinct rises from Y=60 and the stepped hull reaches Y=1950, with its off-axis
service mast rising to Y≈1975 near the artificial ceiling.
The lower hull is 4.4 km in diameter, built from a true hollow annular wall; the
higher tiers step inward to form broad circular shoulders. The deck is a thin
circular plate on concentric girders and radial spokes. Thirteen staggered cave
mouths follow the cylinder surface and contain fixed spindle/gyro/cage/pressure-
vessel mechanisms. A hollow nave, gravity furnace, six exterior machine spines,
off-axis mast, asymmetric east transfer mass, west cantilever, curved retaining
wall, and monumental southern processional gate complete the authored landmark.

Read GRAND_GOAL_VISION.md and docs/TOHA_MONUMENT_DESIGN.md inside this package for
the full visual north star and authored monument design contract.

FIXED: "Forge Mod Loader detected missing/unknown registrie(s)"
---------------------------------------------------------------
The shipped BLAME_Infinite_Megastructure level.dat had stale FML registry
entries for cubicchunks:storage_format_provider_registry and
cubicchunks:vanilla_compatibility_generators_registry. The entries were removed
from Data.fml.Registries; no other level.dat content was changed. If using an
older save copy, back it up before applying any registry repair.

IMPORTANT WORLDGEN NOTE
-----------------------
The generator creates the authored monument as chunks are generated in the
BLAME world type. Minecraft does NOT overwrite already saved/generated chunks
when a mod update changes world generation. The bundled save is packaged with
its level.dat and metadata, not a pre-built monument schematic; use a new BLAME
world if you have already generated chunks across the monument site. In a fresh
world, the new monument appears around the site described above.

TECHNICAL PROFILE
-----------------
- Minecraft 1.12.2
- Forge 14.23.5.2860
- CubicChunks 1.12.2-0.0.1301.0-SNAPSHOT
- World type: BLAME! Megastructure
- Height configured by minecraft/config/cubicchunks.cfg
- Included save: BLAME_Infinite_Megastructure
- Shaders are not included. OptiFine 1.12.2 and CubicChunks can be troublesome together.

PACKAGE REPRODUCTION
--------------------
The committed root ZIP is regenerated from instance-pkg by
.github/workflows/update-instance-package.yml. Treat the result as the installable
base profile. In-game traversal, lighting, block mapping, and performance should
be checked in the actual Minecraft client before final release.
