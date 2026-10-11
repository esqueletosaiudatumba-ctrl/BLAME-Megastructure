# Toha Heavy Industries — authored monument

## Intent

Toha is one fixed architectural set piece, not the previous 150 km randomized industrial field. The surrounding BLAME! world remains deterministic and procedural; the Toha site itself is an authored silhouette with intentionally chosen dimensions, elevations, openings, and approach sequence.

The target is manga-faithful architectural grammar: monumental stepped volumes, pale ruled concrete, near-black setbacks, deep cave-like voids, sparse slit apertures, heavy braces, mechanical ring courses, a long empty approach, and a core that nearly meets the dark ceiling. Avoid generic industrial-city repetition, randomized structures, billboard signage, saturated lights, or decorative clutter.

## Fixed placement and scale

| Element | Design |
|---|---|
| Anchor | X=0, Z=-4608 |
| Worldgen footprint | X [-3072, 3072), Z [-7680, -1536) |
| Maximum authored height | Y=2030 (the ceiling underside begins no lower than about Y=2080) |
| Main building width | About 4,200 blocks at the broad base |
| Main building depth | About 3,860 blocks at the base plinth |
| Precinct deck | 5,600 × 5,600 blocks, Y=0–63 |
| Site edge | Low outer walls with a deliberate south entrance gap |
| Orientation | Monumental façade faces +Z (south); player approaches from the arrival basin |

The site is now next to the arrival basin rather than 90,000 blocks away. The empty span between the arrival gate and Toha is intentional: the building should dominate the view before the player reaches it, while the /blame tp landmarks provide reliable direct access.

## Authored silhouette

1. **Footing:** a broad pale plinth, ~4.2 km wide, with an oversized black course around it. The footprint is deliberate, squared, and readable from very far away.
2. **Lower body:** a dark monolithic shell from roughly Y=200–640, extending beyond the middle floors to form a massive shoulder.
3. **Middle shell:** a pale, deeply ruled volume from Y=640–1020, inset from the lower mass.
4. **Upper shell:** a dark volume from Y=1020–1450, narrower again, with one continuous face and few apertures.
5. **Crown:** a pale/dark stepped stack between Y=1450–1920, with a thin lip close to the artificial ceiling.
6. **Core engine:** a hollow, tapered industrial shaft from Y=210 to Y=2030, with several concentric ring-courses and radial service arms.
7. **Exterior spines:** six fixed, hollow tapered machines fused into the flanks, plus four smaller boundary pylons. They support the skyline without becoming a forest of unrelated towers.
8. **Terrace bands:** oversized horizontal collars at the main elevation changes. These emphasise constructional layers and interrupt the otherwise uninterrupted vertical faces.

## Thirteen fixed cave mouths

The façade contains thirteen authored apertures: five in the lower tier, four in the second, three in the third, and one isolated crown mouth. The openings have fixed centres, widths, heights, and face offsets. Their deep square cuts, dark inner reveals, heavy lintels, and sparse local lamps create a strong readable rhythm. They are architectural rooms/passages, not a random cave-placement algorithm.

The large lower central mouth is the primary arrival into the interior. Side mouths are deep service cavities. Upper mouths are smaller and more isolated so the façade does not read like a regular hotel or apartment tower. Each chamber has a fixed recessed AI control slab; the primary and crown chambers retain tiny live indicators, while the other panels stay dark.

## Interior

The central hollow gravity furnace is the focal machine, rising to just below the artificial ceiling. It uses a thick tapered shell, a hollow bore, six stepped ring courses, and sparse service arms rather than neon ornament.

The central nave is carved out of the solid exterior so the structure has actual negative space. Four ring-collared structural piers, partial service platforms, two inclined braces, hanging lines, and the hollow core engine are visible from the lower cave mouths. Floors do not fill the chamber: a large uninterrupted void around the engine is essential to the scale.

The core shell and side spines have hollow interiors. Preserve this when changing the material palette or expanding the geometry. The large shape should still look strong as a black silhouette; detail comes second.

## South approach

The approach is a narrow processional causeway crossing a mostly empty forecourt. A deep side-pier portal with a heavy pale crosshead anchors the south perimeter opening; it stays clear in the middle for entry. Five fixed industrial frames grow wider and taller toward the façade. Twin recessed feed channels and restrained cyan marks identify the route. The boundary wall is low relative to the building and leaves a wide southern gate, so the monument remains visually dominant.

## Materials and lighting

- **S_TOHA_PALE:** ruled pale concrete for the plinth, inset shells, lintels, and selected braces.
- **S_TOHA_MASS:** near-black monolithic surfaces with sparse panel seams.
- **S_TOHA_RING:** dark curved shells and industrial ring courses.
- **S_TOHA_DECK:** floor and platform plates.
- **S_DARK, COAL, ANDESITE:** deep recesses, support metal, and structural contrast.
- **BIO_CYAN / SEA_LANTERN:** very rare orientation cues at the principal cave, crown, and final approach frame only.
- **LAMP_RED:** a couple of machine status marks, never a continuous neon outline.

Material style resolution may weather the same surfaces differently per seed, but all geometry and structure positions remain fixed.

## Code and verification

- Implementation: mod/src/main/java/blame/blamegen/core/Toha.java
- World integration and generator version: mod/src/main/java/blame/blamegen/core/Megastructure.java
- Landmark access: /blame tp toha-gate, /blame tp toha-south, /blame tp toha-north, /blame tp toha-east, /blame tp toha-west, /blame tp toha-center
- Regression check: tools/verify/TohaMonumentCheck.java
- The GitHub Actions workflow compiles the pure-Java core and runs the monument, determinism, seam, survey, and benchmark checks before attempting the Forge mod build.

Do not call this a verified in-game monument until a compatible Forge 1.12.2 + CubicChunks build launches and the structure is inspected in-client. An offline schematic render and passing core tests do not prove in-game lighting, block mapping, traversal, or performance.
