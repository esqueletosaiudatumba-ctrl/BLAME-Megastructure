BLAME! INFINITE MEGASTRUCTURE — Prism / ElyPrism instance

PACKAGE STATUS — READ BEFORE LAUNCHING
--------------------------------------
This package contains the Minecraft/Forge profile, CubicChunks, and the registry-fixed
BLAME_Infinite_Megastructure save. It intentionally does NOT bundle blamegen right now.

The previous blamegen JAR in the staged repository was stale and the supplied launch log
shows it crashing during world generation (NoSuchFieldException: LIT). The current source
fix is in master, but a compatible replacement JAR has NOT yet been built and verified.
Do not re-add the old JAR: it is not the current v4 generator and can crash at world creation.

Until a freshly built and validated mod JAR is placed in minecraft/mods/, the BLAME world type
will not be registered and the save cannot be played with this package. This is a deliberate
safety state, not a claim that the playable instance is finished.

INSTALL
-------
1. In ElyPrism/Prism: Add Instance > Import > choose this ZIP.
2. The launcher downloads Minecraft 1.12.2 and Forge 14.23.5.2860.
3. Use Java 8 (64-bit). Automatic Java download is enabled in the instance profile.
4. Before launching the BLAME save, add a freshly built, validated blamegen-1.0.0.jar to
   minecraft/mods/. Do not use the stale jar omitted from this package.

KNOWN BUILD BLOCKER
-------------------
The latest handoff records that Gradle fails during :compilePatchedMcJava, before compiling
the mod's own sources, due to an inconsistent decompiled WorldSettings.GameType and missing
Twitch/log4j classes. See HANDOFF_BUILD_STATE.md in the source repository. The generator's
source fix uses BlockRedstoneLight.getStateFromMeta(1) so it does not depend on reflective
lookup of a private property. It has not yet been validated in an in-game build.

SAVE REGISTRY FIX
-----------------
The shipped BLAME_Infinite_Megastructure level.dat has the stale FML registry entries removed
for:
  cubicchunks:storage_format_provider_registry
  cubicchunks:vanilla_compatibility_generators_registry
This is necessary for the bundled CubicChunks 0.0.1301.0. If you use an older save copy, back
it up before changing it. NbtRegistryFix.java in the source repository documents the repair.

PROJECT VISION AND REFERENCES
-----------------------------
GRAND_GOAL_VISION.md is included with this package, along with the four reference images in
docs/reference-images/. It describes the intended atmosphere and gives future AI agents a
visual north star: incomprehensible industrial scale, cramped traces of habitation, cold
cyan light, mechanical density, long quiet stretches, and deliberate negative space. Read
AGENTS.md and HANDOFF_2026-10-11.md in the source repository before changing the generator.

TECHNICAL PROFILE
-----------------
- Minecraft 1.12.2
- Forge 14.23.5.2860
- CubicChunks 1.12.2-0.0.1301.0-SNAPSHOT
- Procedural world type: BLAME (only available once the matching blamegen mod is installed)
- World height configured for CubicChunks; inspect minecraft/config/cubicchunks.cfg
- Save included: BLAME_Infinite_Megastructure
- Shaders are not included. OptiFine 1.12.2 and CubicChunks are known to be troublesome together.

Do not mark this instance as verified/playable until the matching mod JAR has been rebuilt and
the save has been launched successfully in Minecraft 1.12.2 with this exact CubicChunks version.
