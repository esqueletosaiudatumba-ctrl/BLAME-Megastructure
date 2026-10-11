# Project Brief — BLAME! Infinite Megastructure

## North-star experience

The map should feel like a very small accessible fragment of an artificial world whose full dimensions are unknowable. Large spaces need to be so huge that the player's field of view cannot encompass them, while movement often funnels through narrow service passages. Emptiness is a feature: long stretches with no mobs, people or obvious purpose should make the structure feel indifferent. It should be hostile without being arbitrarily annoying, beautiful without decorative clutter, and strange according to a consistent underlying structural logic.

## The visual grammar

- Primary masses: dark grey concrete, polished/deepslate-like stone and large near-black surfaces.
- Structural elements: enormous ribs, columns, thin frames, segmented shell arcs, cantilevered slabs, cable-like drops and beam systems.
- Human reference scale: doors, tiny lamps, access hatches, ladders, thin catwalks and service landings.
- Empty space: visible shaft interiors, cavernous distances, open drops and architecture overlapping in several planes.
- Organic-but-engineered layout: seeded pseudo-random shifts, broken ellipses, gaps, partial rings, non-aligned structural bays and varying scales. Avoid square-gridded city blocks.
- Lighting: restrained industrial lights and distant isolated points, with occasional brighter focal views. Leave most structure dark enough to preserve depth.
- Arrival exception: a broad sand beach and shallow sea under open sky with fixed clear noon. It should feel serene and readable, in contrast to the hostile Megastructure just beyond it.

## Target platform

ElyPrism / Prism Launcher instance archive, Minecraft Java 1.12.2 + Forge + CubicChunks, because the practical CubicChunks gameplay version is the legacy 1.12.2 line. Current config requests Y=-4096 through Y=8192. The active `mmc-pack.json` currently names Forge 14.23.5.2847; reference-kit documentation says 14.23.5.2860. Audit compatibility and resolve this discrepancy.

## State and honest caveats

The current archive has a no-mob, clear-noon setup function; a World Primer configuration; a PowerShell first-launch bootstrap; and around 14,664 local stream-cell functions plus dispatch/setup functions. A separate reference procedural kit has a deterministic Python generator that emits a much smaller 123-function fixed-sector version. These two implementations need to be reconciled. The present instance ZIP passed archive integrity checks, but it has not been verified by launching the game client in this environment.

## What “procedural” must mean here

Not just a large prewritten list, not only square sectors, and not only one static room. The generator should create architecture from a deterministic seed and spatial rules, with local sector realization when explored, unique macro landmarks, coherent transitions between regions, and near/mid/far layers. There must be a reproducible source build for the exact deliverable instance.
