BLAME! INFINITE MEGASTRUCTURE - Prism / ElyPrism instance

INSTALL
1. In ElyPrism/Prism: Add Instance > Import > choose the instance ZIP from the
   repository root: BLAME_Megastructure_Instance.zip.
2. Launch using Java 8 (64-bit). The launcher can auto-download it.
3. Select Singleplayer > BLAME_Infinite_Megastructure, or create a new world:
   Create New World > More World Options > World Type: "BLAME! Megastructure".

The instance ZIP includes CubicChunks and the matching blamegen-1.0.0.jar.
Offline deterministic, seam, monument, and preview checks and the Forge Gradle
build have passed in GitHub Actions. In-game visual and traversal testing is
still required before calling this fully verified.

TOHA HEAVY INDUSTRIES — AUTHORED MONUMENT
The generator now builds one fixed Toha landmark, not a randomized 150 km
industrial grid. The site is centred at X=0, Z=-4608; the stepped factory
fortress rises to Y=2030 and includes thirteen AI cave chambers, a hollow
central nave, a tapered gravity furnace, fused exterior machine spines, true
deep façade slits, and a monumental south gate/approach.

In the BLAME world, useful commands:
  /blame list
  /blame tp toha-gate
  /blame tp toha-center

IMPORTANT
Minecraft does not regenerate chunks already stored in a save after worldgen
changes. Use a new BLAME world if the monument site has already been generated
in your current save. The bundled save contains its level metadata and fixed
registry state, not a hand-placed copy of Toha.

The shipped BLAME_Infinite_Megastructure level.dat has the stale CubicChunks
FML registry names removed:
  cubicchunks:storage_format_provider_registry
  cubicchunks:vanilla_compatibility_generators_registry
The rest of the level.dat was left unchanged.

Shaders are NOT included. OptiFine 1.12.2 and CubicChunks are known to be
troublesome together. Weather is locked clear/noon by design.
