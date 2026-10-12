# Toha Heavy Industries — authored monument

## Intent

Toha is one fixed architectural set piece, not the previous 150 km randomized industrial field. The surrounding BLAME! world remains deterministic and procedural; the Toha site itself is an authored silhouette with intentionally chosen dimensions, elevations, openings, and approach sequence. Its kilometre-scale shells are hollow wall envelopes rather than solid boxes, so the central voids, side chambers and exterior view corridors are real geometry.

The target is manga-faithful architectural grammar: an immense stepped cylindrical hull, pale ruled concrete, near-black annular setbacks, deep cave-like voids, sparse slit apertures, heavy braces, mechanical ring courses, a long empty approach, and an off-axis core/mast that nearly meets the dark ceiling. The cave mouths are deliberately staggered rather than mirrored, with a heavy east-side transfer mass and a separate west-side cantilever to break the temple-like symmetry. Avoid generic industrial-city repetition, randomized structures, billboard signage, saturated lights, or decorative clutter.

## Fixed placement and scale

| Element | Design |
|---|---|
| Anchor | X=0, Z=-4608 |
| Worldgen footprint | X [-3072, 3072), Z [-7680, -1536); rectangular broad-phase bound only |
| Maximum authored height | Y=2030 (the ceiling underside begins no lower than about Y=2080) |
| Main building width | 4,400-block lower-hull diameter, with stepped rings of 3,800 / 3,200 / 2,640 / 2,100 / 1,640 blocks |
| Main building depth | Same as its 4,400-block circular lower hull; projecting machinery extends farther on the east side |
| Precinct deck | 5,600-block circular plate, Y=60–63, on concentric girders and radial spokes at Y=48–59 |
| Site edge | Low outer walls with a deliberate south entrance gap |
| Orientation | Monumental façade faces +Z (south); player approaches from the arrival basin |

The site is now next to the arrival basin rather than 90,000 blocks away. The empty span between the arrival gate and Toha is intentional: the building should dominate the view before the player reaches it, while the /blame tp landmarks provide reliable direct access.

## Authored silhouette

1. **Footing:** a 5.6 km circular deck plate on radial and concentric girders, with a dark annular rim and a separate circular retaining wall interrupted by the monumental southern gate.
2. **Lower hull:** a 4.4 km-diameter dark cylindrical annulus from Y=64–639, with real hollow interior and a broad projecting circumferential shoulder.
3. **Middle hull:** a pale cylindrical annulus, 3.8 km diameter from Y=640–1019, inset sharply from the lower hull.
4. **Upper hull:** a dark 3.2 km-diameter cylindrical annulus from Y=1020–1449, with a continuous curve and few apertures.
5. **Crown:** concentric pale/dark cylindrical tiers of 2.64 km diameter at Y=1450–1759, 2.10 km at Y=1760–1919, and 1.64 km at Y=1920–1950, stepped inward toward the artificial ceiling.
6. **Core engine:** a hollow, tapered industrial shaft from Y=210 to Y=2030, with several concentric ring-courses and radial service arms.
7. **Exterior spines:** six fixed, hollow tapered machines fused into the flanks, plus four smaller boundary pylons. A broad eastern transfer mass and a westward crown cantilever break the otherwise immense main shell. They support the skyline without becoming a forest of unrelated towers.
8. **Terrace bands:** oversized horizontal collars at the main elevation changes. These emphasise constructional layers and interrupt the otherwise uninterrupted vertical faces.

## Cylindrical hull geometry

The source anchor is `CX=0, CZ=-4608`. The circular deck has a 2,800-block radius. The primary hull uses true annular walls rather than a square box or a filled cylinder:

| Vertical tier | Elevation | Outer radius | Wall thickness | Material intent |
|---|---:|---:|---:|---|
| Lower hull | Y=64–639 | 2,200 | 36 | near-black monolith |
| Second hull | Y=640–1019 | 1,900 | 36 | pale ruled concrete |
| Third hull | Y=1020–1449 | 1,600 | 36 | near-black mass |
| Fourth hull | Y=1450–1759 | 1,320 | 36 | pale shell |
| Crown lower tier | Y=1760–1919 | 1,050 | 32 | dark machine shell |
| Crown cap | Y=1920–1950 | 820 | 28 | pale cap |

Each tier clears its own interior radius and draws only the annular wall, so the central volume stays genuinely empty until the authored nave, platforms, furnace, braces and chambers are added. Circumferential collars at the setbacks exaggerate the layer transitions; eight irregular axial ribs break the outline without creating a repeated apartment/window grid. The upper service mast and east transfer module intentionally breach the smooth circle.

The X/Z footprint remains a conservative rectangular overlap bound to avoid expensive circular tests during chunk selection. It is not the shape of the structure. Circularity is established by the deck, retaining wall, primary hull, collar courses, cave-mouth depth, and radial structure.

## Broken orbital galleries and brutal facade cuts

Generator v13 retains the six principal shell diameters and all thirteen fixed AI cave mouths, but interrupts the smooth drum silhouette with four hand-authored tiers of broken orbital gallery. The courses sit at Y=466–491 (outer/inner radii 2,520/2,376), 910–940 (2,185/2,042), 1,365–1,395 (1,810/1,685), and 1,630–1,658 (1,495/1,372). Each is a collection of unequal angular segments, not a continuous ring: the south-facing arcs split around the approach, and large gaps remain. Short outriggers transfer load to the hull; four hollow-section diagonal compression members stitch separate tiers together at non-mirrored angles.

Eight tall, shallow curved fins are placed against the authored surface at selected x positions. Every x-column is projected to its tier's actual circular surface, so a fin follows the hull rather than becoming a flat wall. Contrasting arrises catch light, but there is no regular window-grid rhythm. Their placement is deliberately uneven: heavier lower flanks, differently proportioned middle cheeks, a broken upper pair, and a short crown blade.

The repeated-pitch slit helper is replaced by twenty-seven fixed aperture records. Each slit has its own x, bottom elevation, width, height, shell radius, and cut depth (86–112 blocks). Openings remain real voids cut into the curved wall; none is selected from the world seed. This creates an authored negative-space rhythm without compromising the thirteen larger cave portals.

Generator v14 widened the Toha shell's screentone rule pitch from 32 to 96 blocks. Generator v15 added the manga's identifying 東亜重工 mark as four vertical block-raster glyphs attached directly to the outer cylinder at the southeast-facing lower hull. V16 changed the strokes to matte-black COAL blocks. V17 increases the glyph cells from 6 to 10 blocks so the characters survive the multi-kilometre view, and updates depth culling to follow the stepped hull surface at each tier transition. Every glyph pixel remains attached to the local circular radius; there is no flat billboard. The regression test checks a real black glyph block, and the placement leaves the thirteen major cave portals intact.

At multi-kilometre scale, the old 32-block rule made otherwise monolithic concrete read like tightly spaced bars. The wider pitch keeps ruled surfaces and deterministic mineral weathering, while letting each facade mass read as a large continuous surface from a distance. The floor/deck keeps its finer marking scale.

## Thirteen fixed cave mouths

The curved hull contains thirteen authored cave mouths: five in the lower tier, four in the second, three in the third, and one isolated crown mouth. Each opening is projected to the cylinder surface at its actual X position; the front is not a single flat plane. Their horizontal centres, heights and widths are intentionally irregular; the grouping is legible from a distance without becoming a repeated window grid. The openings have fixed centres, widths and heights, and their face offsets are computed from the cylinder radius rather than a shared flat-front Z coordinate. Their deep square cuts, dark inner reveals, heavy lintels, and sparse local lamps create a strong readable rhythm. They are architectural rooms/passages, not a random cave-placement algorithm.

The large lower central mouth is the primary arrival into the interior. Side mouths are deep service cavities. Upper mouths are smaller and more isolated so the façade does not read like a regular hotel or apartment tower. Each chamber has a fixed recessed AI control slab; the primary and crown chambers retain tiny live indicators, while the other panels stay dark.

## Chamber mechanisms and upper mast

The thirteen chamber AIs have fixed mechanical apparatus inside their cave volumes; these are deliberate, coordinate-authored parts of the monument, not a random prop scatter. Four authored machine grammars recur through the subordinate caves: hollow tapered spindles with bearing collars, exposed gyro rings held by service arms, open rectangular maintenance cages, and paired pressure vessels joined by low service bridges. Their dimensions follow the cave envelope so they remain attached to the chamber architecture.

The principal cave is kept as a sightline into the gravity furnace. Its AI machinery is split into two flanking mechanisms rather than a centre-blocking prop. The recessed control panel and rare local status lights remain visible from the entry without turning the whole façade into a neon display.

One off-axis service mast rises to Y≈1975, close to the ceiling but not merged into it. Long, heavy braces pin it back into the upper mass; a small cyan index lamp marks the mast near its crown. The mast intentionally breaks the otherwise stepped roofline, making the silhouette feel industrial and unbalanced rather than like a symmetrical temple.

## Interior

The central hollow gravity furnace is the focal machine, rising to just below the artificial ceiling. It uses a thick tapered shell, a hollow bore, six stepped ring courses, and sparse service arms rather than neon ornament.

The central nave is a true open void within the wall shells; major tiers do not contain solid kilometre-wide interiors. Its perimeter is carried by deep wall envelopes, terrace collars and discrete structural members. Four ring-collared structural piers, partial service platforms, two inclined braces, hanging lines, and the hollow gravity furnace are visible from the lower cave mouths. Floors do not fill the chamber: a large uninterrupted void around the engine is essential to the scale.

The core shell, side spines, main tiers and transfer masses have hollow interiors. The precinct platform is a four-block plate supported by an orthogonal grid of eight-block-wide, twelve-block-high girders at 512-block spacing. Preserve this sparse load-bearing logic when changing the material palette or expanding the geometry. The large shape should still look strong as a black silhouette; detail comes second.

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
- World integration and generator version (v12): mod/src/main/java/blame/blamegen/core/Megastructure.java
- Landmark access: /blame tp toha-gate, /blame tp toha-south, /blame tp toha-north, /blame tp toha-east, /blame tp toha-west, /blame tp toha-center
- Regression check: tools/verify/TohaMonumentCheck.java
- The GitHub Actions workflow compiles the pure-Java core and runs the monument, determinism, seam, survey, and benchmark checks before attempting the Forge mod build.

Do not call this a verified in-game monument until a compatible Forge 1.12.2 + CubicChunks build launches and the structure is inspected in-client. An offline schematic render and passing core tests do not prove in-game lighting, block mapping, traversal, or performance.
