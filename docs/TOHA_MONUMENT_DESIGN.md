# Toha Heavy Industries — spire in the industrial valley

## Design goal

Generator v19 replaces the previous broad, stepped cylinder with a **narrow hollow spire inside a megastructure-scale canyon**. The tower is the central silhouette, but the valley supplies the scale: stratified walls rise beside it, equipment bays are cut into the cliffs, interrupted catwalks climb the walls, and slab-backed routes pass through yards, gantries, lift towers and chambers. The space should feel like a cross-section through an immense, older machine city rather than a freestanding skyscraper on a round plaza.

The design uses the visual language of *BLAME!* without reproducing individual manga panels: a thin tapering black mass, huge pale structural cuts, severe dark concrete, long vertical voids, asymmetrical bridges, irregular caverns, small cold indicator lights and machine assemblies that seem to have an industrial purpose. For Minecraft-scale detailing, the authored palette now also uses slabs, stairs, iron trapdoors, rails, pistons, dispensers and hoppers alongside tanks, vent grills, pipework, bars and cables. These are structural and mechanical details, not decorative scatter.

The environment should be **densely infrastructural but not uniformly full**. Machines are concentrated along routes, in equipment yards and inside deep wall apertures. Huge open spans remain between them so the view can communicate the canyon's depth and the spire's height.

## Fixed placement and envelope

| Element | Authored value |
|---|---|
| Anchor | X=0, Z=-4608 |
| World-generation bounds | X [-4096, 4096), Z [-8704, -512) |
| Geometry height | Y=64 through Y=2030 |
| Spire radius | approximately 420 blocks at the foot, narrowing to 18 blocks at the crown |
| Spire interior | carved hollow; roughly 384-block lower radius tapering to a 3-block bore near the top |
| Valley walls | inner edges around X=-2940 and X=+2940, with wall thickness around 120 blocks |
| Valley length | about 7,120 blocks between the near and far wall faces |
| Cliff access | broken platforms at Y=210, 455, 735, 1015, 1300, 1585 and 1870 |
| Gravity furnace | offset 36 blocks to the north of the spire centre; hollow taper from radius 118 to 13 |
| AI chambers | thirteen fixed chambers between Y=120 and Y=1912 |

The large X/Z owner bounds are a conservative broad-phase region for the canyon, not a filled rectangular prism. Every large mass is separately bounded and clipped by the current 16³ Cube before raster work. Toha geometry is fixed and independent of the world seed; only materials resolved through `Styles` may vary.

## Composition from far to near

### 1. Canyon envelope

The valley's east and west walls are thick dark mass shells on the order of three kilometres from the centreline. They reach from the low canyon floor to around Y=2008, and read as immense cliff-like megastructure walls rather than natural rock. Large rectangular machinery mouths, irregular slots and massive lintels interrupt the inner faces. Some openings reveal darkness; others have deep side reveals and pale structural lips.

Long vertical ribs join the cliff faces to the roofward mass. Their widths and heights vary by authored position; not every rib reaches the same level. Deep vertical slots, narrow black service seams and exposed shafts make the walls read as multiple layers of infrastructure. The north end remains fractured with unequal wall sections, while the south gate has a clear central opening framed by tall piers and a crosshead. Both directions retain a route into the canyon.

### 2. The spire

The central body is a single hollow, tapered mast, not a stack of wide drums. Its outer skin narrows continuously from a radius of about 420 at Y=64 to 18 at Y=2030. The body is formed from a dark industrial shell with an inner air carve. Seven irregular pale/dark longitudinal ribs climb in short, bounded segments so that the tower feels reinforced without becoming a regular fluted column.

Thin course marks punctuate the height. Broken orbital maintenance decks step outward from the skin at unequal intervals, with different angular spans and elevations. Their arcs never become complete, continuous balconies. Short brackets transfer load back to the shell, and some platforms are attached to the external helix or to one of the bridge landings.

The south-east-facing lower spire carries the matte-black `東亜重工` mark in compact 3-block raster cells, projected to the local tapered surface. The mark remains small and part of the architecture; it is not a floating sign.

### 3. Hollow interior and gravity furnace

The shell's interior is a real void. An eccentric hollow gravity furnace stands within it, slightly north of the geometric centreline. Its tapered dark casing, open bore, narrow course rings, radial arms and pressure-line bands provide a second vertical focus behind the outer spire. The interior floors are partial arcs, not full horizontal slabs that would seal the void.

At seven heights, transverse slab bridges reach from the cliff-side routes through the spire shell to the corresponding internal gallery. The bridges are separated by large vertical gaps and use structural undersides, railings, and short truss spans. Their central ends meet partial platforms around the furnace, not a solid central shaft. Preserve the open sightline around the machine.

### 4. Thirteen AI chambers

Thirteen authored chambers are placed at uneven heights from Y=120 to Y=1912. Their dimensions shrink with the spire. Each opening is a real void cut through the shell and faces one of four directions; later chambers are moved off the centreline where necessary to avoid the furnace and preserve a clear doorway. The openings have pale side jambs, heavy dark lintels, occasional slab sills, recessed control faces and sparse status indicators.

Each chamber uses one of four fixed machine grammars:
- paired pressure vessels with collars and hopper-like feeder blocks;
- a gyro/rotor frame with metal crossbars, piston details and a maintenance rail;
- a rack cage with repeated half-height slab floors and dark reinforced glass;
- a hollow tapered spindle with bearing collars and attached pipework.

The chambers are not independently randomized. Their position, orientation, dimensions and machine type are hand-authored and regression-tested.

## Traversal and machine logistics

### Exterior and cliff routes

A helical maintenance path follows the spire from the lower shell towards the crown. It uses short overlapping half-slabs as treads, periodic posts and rare cyan route lights. The helix is deliberately steep and industrial; it is not a decorative smooth staircase. Chamber mouths are carved after the helix so a landing cannot seal a doorway.

Both cliff walls have seven levels of broken half-slab/catwalk access. Their exposed brackets, intermittent rails and gaps imply a route that has been partly damaged or extended over centuries. Lift-like equipment towers attach to the walls at authored intervals. These are hollow tapered frames with repeated ring courses, small machinery details and top landings.

### Ground logistics

The valley floor is a set of wide structural plates, not a flat circular plaza. Nine unequal plate bands are divided by open trenches. Some trenches have short grated or slab bridges, while narrow stepping lanes and black expansion joints indicate routes across the plates.

A south causeway and a north causeway terminate at separate low portals through the foot of the spire. Both have paired load members, underside cross-ties, modest route lamps and copper utility marks. The interior path continues between the two base entrances.

### Machine yard, cargo and gantries

Twenty fixed machine modules form several non-mirrored clusters around the spire. Each module has a footing, four load-bearing legs, a crosshead, a slab-topped service landing and visible connections to a second module or a service route. Equipment types include pressure-tank pairs, rotor rings, framed vent racks, transformer stacks, dispenser-like control blocks, hopper-like feeders, trapdoor covers and rare cyan status lamps.

Four ground-level paired cargo rails cross the valley along slab foundations. Raised conveyors at multiple elevations carry tray-like channels between machine rows. Their repeated supports have copper collars and piston-like details. Two large and one smaller loading gantries suspend hopper-like lifting heads on cables; their offset legs and crossheads avoid a perfectly symmetrical frame. Access walks cling to each conveyor at a different height.

Conduits and routes must appear to lead somewhere: a yard module, a wall aperture, a gantry or the spire. Do not add isolated machinery merely to increase block count. Keep the industrial density around the equipment routes, with wider negative-space bands in the canyon between major clusters.

## Minecraft block language

The generator maps its material IDs to real Minecraft 1.12.2 block states. The added mechanical materials include dispensers, hoppers, pistons, iron trapdoors, rails and stone-brick stairs. Alongside them, use the existing stone half-slabs, iron bars, reinforced dark glass, vents, raw/oxidized copper-colour blocks, tanks, rebar, concrete variants and rare sea-lantern/cyan indicators.

- **Primary mass:** near-black concrete-style surfaces and dark ring shells.
- **Structural cuts:** pale ruled concrete and battered slab lips.
- **Walking surface:** bottom/top slabs, short stair segments and framed landing pads.
- **Load structure:** heavy ribs, trusses, iron blocks and ring-collared piers.
- **Mechanical detail:** pressure cylinders, hopper feeders, piston-driven rotor assemblies, dispensers, trapdoor covers, vents, piping and rails.
- **Lighting:** restrained cyan/sea lanterns at route markers and key machine states; do not outline every edge in neon.
- **Weathering:** keep deterministic mineral variation and the low-saturation charcoal/ash/pale-concrete palette.

When a new block ID is added to `Mat.java`, also add its 1.12.2 mapping in `BlameCubeGenerator.java` and preview colour in `tools/preview/Preview.java`. Increase `Mat.COUNT` accordingly and check both `isStructure` and `isSolid` for non-blocking types such as rails.

## Pathfinding landmarks

The `/blame` teleport landmarks have been retargeted to the new spatial layout:
- `/blame tp gate`: southern valley mouth, looking towards the spire.
- `/blame tp south`: southern causeway.
- `/blame tp north`: far causeway.
- `/blame tp east` and `/blame tp west`: east/west cliff catwalks at Y=455.
- `/blame tp center`: base apron beside the entrance.
- Short aliases, `/blame tp toha <part>`, `/blame goto <destination>`, `/blame destinations`, and XYZ teleporting remain available.

A new world is needed to see changed world-generation geometry in positions whose cubes were already saved.

## Verification

`tools/verify/TohaMonumentCheck.java` verifies:
- the expanded valley owner bounds and narrow spire profile;
- shell material and hollow interior;
- real valley wall masses and clear canyon negative space;
- passable south gate;
- all thirteen directional AI chamber openings;
- a helical slab tread and transverse slab bridge;
- a slab-clad module roof and the spire inscription.

GitHub Actions compiles the pure-Java core, runs Toha regression checks, `Probe`, `SeamCheck`, `Survey`, `LandmarkSurvey` and the benchmark, then produces a large batch of isolated Toha perspectives, plans, sections and elevations. A preview is still an offline schematic, not proof of Minecraft lighting, collisions or in-game traversal. Those require an actual Forge 1.12.2 + CubicChunks client run.

## Source of truth

- Generator: `mod/src/main/java/blame/blamegen/core/Toha.java`
- Material IDs: `mod/src/main/java/blame/blamegen/core/Mat.java`
- Minecraft block mapping: `mod/src/main/java/blame/blamegen/BlameCubeGenerator.java`
- Offline art renderer: `tools/preview/Preview.java`
- Preview contact sheets: generated by `tools/preview/ContactSheet.java`
- Regression checks: `tools/verify/TohaMonumentCheck.java`
