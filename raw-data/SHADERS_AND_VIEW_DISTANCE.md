# View distance, performance, shaders, lightning, clouds

How to see more of the megastructure at once, how to keep it fast, and what is
known (and not known) about shaders. Status: **guidelines only** — none of the
optional mods below are bundled, and none have been tested with this instance.

## The honest summary

- The structure is kilometres tall with open shafts. Vanilla 1.12.2 caps
  horizontal render distance at **32 chunks**; CubicChunks adds a separate
  **vertical** load distance. Raising both is the supported way to see more.
- **OptiFine is not bundled and not recommended.** CubicChunks upstream
  documents OptiFine as incompatible (it replaces the chunk renderer that
  CubicChunks depends on), and the HANDOFF's standing rule applies: do not add
  OptiFine as a required mod until someone verifies it against a real client
  test. Because 1.12.2 shaderpacks load through OptiFine, **shaders are
  effectively unsupported for now** — see "If you try shaders anyway".
- Everything else in this file is safe: config edits, JVM flags, and
  non-rendering performance mods.

## High view distance (supported)

1. **Client (`minecraft/options.txt`):** shipped at `renderDistance:16`.
   Raise to `24`–`32` on a strong machine. Horizontal chunks are 16×16 columns;
   32 chunks = 512 blocks each way — enough to take in the whole arrival bay
   (ring wall radius ~280) from the beach.
2. **Vertical (`minecraft/config/cubicchunks.cfg`):** `verticalCubeLoadDistance`
   (shipped `12`) controls how many 16-block cubes above/below the player the
   server sends. Flying between tiers wants `16`–`32`. It does not change
   rendering, only what is loaded/sent — raising it without raising render
   distance shows little.
3. **Server (`server.properties`, dedicated servers):** `view-distance` above
   `16` multiplies cube traffic fast (each horizontal chunk is a tall stack of
   cubes here). `12`–`16` is the sane range; the shipped config also raises
   `cubesToSendPerTick`/`maxGeneratedCubesPerTick` budgets, keep those.
4. **Clouds:** shipped `clouds:0` (off). Vanilla cloud planes slice through the
   structure and look broken at this scale. `BlameWorldType.getCloudHeight()`
   puts them at Y 2400 (above the roofscape) if you turn them back on.

## Performance (safe mods, Java flags)

Compatible-with-CubicChunks 1.12.2 performance mods (install as ordinary client
mods, all optional, none bundled): **FoamFix**, **BetterFPS**, **VanillaFix**,
**Phosphor** (1.12.2 port), **Surge**, **TexFix**. These do not touch chunk
rendering and are the standard 1.12.2 stack.

Java 8 flags that suit this world (large, tall, lamp-dense):

```
-Xmx4G -Xms4G -XX:+UseG1GC -XX:MaxGCPauseMillis=50 -XX:+UnlockExperimentalVMOptions -XX:+DisableExplicitGC
```

Notes:

- The generator benches at ~0.2–0.6 ms per 16³ cube, so generation is not the
  bottleneck; lighting and mesh building are. If the client stutters while
  flying fast, lower render distance before anything else.
- `gamma:0.6` is shipped in `options.txt`. The deep structure is meant to be
  near-black; raising gamma washes out the fog effect in `ClientEvents`.
- The mod's fog already fades to near-black without skylight, which culls
  distant interior geometry visually. Shaders are not needed for the darkness.

## Lightning and dramatic clouds

- **Weather is locked clear by design** (`doWeatherCycle=false`, noon locked):
  rain would leak through the open shafts and the lightless ceiling assumes a
  fixed sun. Do not enable permanent weather.
- **For storm photography:** `/weather thunder` still works manually (the
  gamerule only stops automatic cycling). Lightning only strikes under open
  sky, so it works on the beach and inside sky-open shafts/wells, not deep in
  the structure. Set it back with `/weather clear`. Thunderstorms darken the
  sky light, and the fog handler follows skylight, so a storm visibly darkens
  the arrival — intended.
- **Dramatic shader clouds** (volumetric overcast, storm fronts) only exist
  inside OptiFine shaderpacks — see below. There is no shader-free way to get
  them on 1.12.2.

## If you try shaders anyway (unsupported, at your own risk)

Requirements: OptiFine 1.12.2 HD U (E3 or later) installed as a client mod,
plus one shaderpack in `minecraft/shaderpacks/`. Expect cubic-chunk rendering
breakage (missing faces, wrong lighting, crashes) — that is the known upstream
incompatibility, not a bug in blamegen. If it works, shaderpacks of this era
with strong atmosphere presets include SEUS Renewed, BSL, Sildur's Vibrant,
and Chocapic13; all render lightning bolts/flashes during thunderstorms and
replace vanilla clouds with volumetric ones. Performance-friendly picks are
Chocapic13 Low/Medium and Sildur's Enhanced Default. Report back what happens:
a verified working combination would update this file and the instance README.

Do not file blamegen bugs for anything observed with OptiFine installed until
the same scene is reproduced without it.
