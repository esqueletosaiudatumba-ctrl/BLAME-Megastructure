# BUILD LOG + EXACT STOPPING POINT (2026-10-11)

> **Status update — authored Toha milestone:** the older compile failure below is a historical stopping point, superseded by the current GitHub Actions build workflow. The offline core, Toha monument regression suite, and Forge build have passed on recent commits. The generator now builds the Toha site as one fixed, seed-invariant landmark (not a randomized 150 km district). Generator v12 replaces the rectilinear body with a hollow stepped cylindrical hull, circular retaining wall and precinct deck, curved cave mouths, circumferential collars, axial ribs, fixed cave-AI machinery, and an off-axis mast near the ceiling. The CI packaging workflow explicitly selects and validates the reobfuscated runtime JAR, then rebuilds the instance ZIP; cylindrical geometry and tests are being revalidated. No in-client Minecraft launch or visual/performance sign-off has been completed. Use the latest Build BLAME mod run in Actions as the source of truth for the currently tested commit.

## Toha monument update

- Fixed authored site at X=0, Z=-4608, with a roughly 6 km precinct and height to Y=2030.
- One stepped cylindrical main hull; thirteen curved staggered cave mouths with recessed AI control slabs and fixed cave machines; hollow annular shells; a circular girder-supported thin deck; a ceiling-height off-axis mast; a hollow nave and tapered gravity furnace; sparse deep façade slits; fixed external machine spines; and a monumental south portal/causeway.
- Commands: /blame tp toha-gate and /blame tp toha-center reach the authored site.
- tools/verify/TohaMonumentCheck.java checks the bounds, gate opening, shell, actual slit void, principal cave, AI panel, open nave, and gravity furnace.
- Architecture contract: docs/TOHA_MONUMENT_DESIGN.md. The offline preview supports isolated rendering with Preview toha-persp, toha-section, toha-elevation, and toha-slice; toha-elevation projects the stepped facade instead of sampling one depth plane.


Everything below is the literal state when I stopped. Nothing is silently "done".

## What works right now

- **Pure-Java generator core compiles and passes all offline verification.** With any JDK 8+:
  ```
  cd "C:\Users\Admin\Documents\Projeto Padrão"
  javac -d build/pv mod/src/main/java/blame/blamegen/core/*.java tools/preview/Preview.java tools/verify/*.java
  java  -cp build/pv blame.blamegen.core.SeamCheck    # SEAMCHECK PASS
  java  -cp build/pv blame.blamegen.core.Probe       # determinism PASS + census
  java  -cp build/pv Preview bench 3000              # ~0.27 ms random / ~0.55 ms arrival cube
  ```
- **Gradle itself works**: wrapper 9.7.0 downloads and runs on JDK 25
  (`C:\Users\Admin\AppData\Roaming\ElyPrismLauncher\java\java-runtime-epsilon`).
- **MC 1.12.2 now resolves** (this was the long fight; see below).

## The mod-jar build: exactly where it stopped

`cd mod && gradlew.bat build` fails in task **`:compilePatchedMcJava`** — i.e. compiling
*Minecraft's own decompiled sources*, **before** it ever reaches our `:compileJava`.
So our mod sources have not yet been compiled by Gradle at all.

The failure is missing compile-classpath deps that the vanilla MC sources reference:

```
error: package tv.twitch.broadcast does not exist
  net/minecraft/client/stream/{BroadcastController,IngestServerTester,IStream}.java
error: package org.apache.logging.log4j.core.helpers does not exist
  cpw/mods/fml/common/network/internal/{FMLProxyPacket,FMLNetworkHandler}.java
error: package tv.twitch.chat / tv.twitch does not exist   (~30 files)
error: cannot find symbol WorldSettings.GameType           (~5 files)
```

Cause: RFG's `compilePatchedMcJava` needs the MC/Forge *runtime* dependency set. In this
reconstructed build I stripped the dev-env-only runtime deps from
`gradle/scripts/dependencies.gradle` (removed `com.cleanroommc:strip-latest-forge-requirements`
and `com.cleanroommc:osxnarratorblocker`), and the userdev is
`forge-1.12.2-14.23.5.2847-userdev.jar` (RFG auto-picked the nearest 1.12.2 Forge, not 2860).

### The fix to try next (in order)

1. **Restore dependencies.gradle to the original** (it was in the handoff; I removed two
   runtime-only lines to work around unresolvable artifacts). Then:
2. **Add the missing deps to the compile path** in `mod/build.gradle`, e.g.
   ```groovy
   dependencies {
       implementation 'tv.twitch:twitch:6.5'
       implementation 'org.apache.logging.log4j:log4j-api:2.8.1'
       implementation 'org.apache.logging.log4j:log4j-core:2.8.1'
       implementation rfg.deobf('maven.modrinth:cubicchunks:1.12.2-0.0.1301.0')
   }
   ```
   Alternatively — and this is the cheaper route — **use RFG's `skipSlowTasks`/`usesForge`
   settings or the `minecraft { }` block's inherited userdev deps**; the real CleanroomMC
   TemplateDevEnv `build.gradle` (which the handoff lost) sets these up. Get a copy of the
   template matching RFG 2.0.6 and diff it against `mod/build.gradle`.
3. Or **drop RFG 2.x entirely and use RFG 1.4.x** (ids differ: `com.gtnewhorizons.retrofuturagradle`
   is available from 1.4.5). TemplateDevEnv for 1.12.2 normally pairs with RFG 1.0.x/1.4.x and
   that era's `build.gradle`. The `gradle.properties` in this repo is from that era
   (`mapping_channel=stable`, `mapping_version=39`, `use_tags=true`, `tag_class_name=...`).

### Exact working incantation so far

```
set JAVA_HOME=C:\Users\Admin\AppData\Roaming\ElyPrismLauncher\java\java-runtime-epsilon
cd mod
gradlew.bat build --no-daemon --console=plain
```
No `-P` flags are needed any more. The `minecraft { version = '1.12.2' }` line in
`mod/build.gradle` is what makes RFG pick 1.12.2 (RFG 2.x reads the version from the
extension, not from `gradle.properties`).

## Files I changed to get this far (all listed in `FILE-MANIFEST.txt`)

- `mod/build.gradle` — **created from scratch** (the handoff zip was missing it).
- `mod/settings.gradle` — plugin-marker group added to the GTNH repo filter.
- `mod/gradle.properties` — added `minecraft_version` / `forge_version` (now unused by RFG 2.x,
  harmless, keep for the 1.4.x route).
- `mod/gradle/scripts/dependencies.gradle` — removed two unresolvable dev-env runtime deps.

## Do NOT ship a v1 jar with the v3 core

`Mat.COUNT` and friends are `static final` compile-time constants. The old
`dist/blamegen-1.0.0.jar` (v1) has `new IBlockState[31]` inlined into `BlameCubeGenerator.class`.
Mixing v1 classes with the v3 `Mat` (COUNT=45) throws `ArrayIndexOutOfBoundsException` in
`buildStates()` at world creation. Rebuild the jar or ship without it.

## Update: root cause of the compilePatchedMcJava failure (found by code review, not yet CI-verified)

`minecraft { version = '1.12.2' }` does not select the Minecraft version in RFG: the property is
`mcVersion`, and RFG defaults to **1.7.10**. That is why the build compiled `cpw.mods.fml`, Twitch and
`WorldSettings.GameType` sources. `build.gradle` now sets `mcVersion = '1.12.2'`, a Java 8 toolchain
(Forge 1.12.2 cannot load Java 17 class files), RFG 2.0.6 (the version proven to load on Gradle 9.7),
and compiles against the bundled CubicChunks jar instead of an unverified Modrinth coordinate.
Also fixed: `Mat.LAMP_RED/BIO_MAGENTA` used `getStateFromMeta(1)` on the redstone lamp, which returns the
UNLIT lamp (lit/unlit are separate blocks); now `Blocks.LIT_REDSTONE_LAMP`. Next: run the
"Build BLAME mod" workflow and report the result.
