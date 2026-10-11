# BLAME! — Design Research

Living reference for the generator. Sources are linked at the bottom; quotes are from those
sources, everything else is the design conclusion.

## The canon facts that constrain the design

**The City / Megastructure.** *"The City has come to resemble a series of layered, concentric
Dyson spheres, filled with haphazard architecture and largely devoid of life. These layers
compose the supporting scaffold of the City, known as the Megastructure."* The City grew
randomly and exponentially for millennia with no human control. It is not planned — it
*accumulated*.

**The day-night cycle.** *"The underside of each layer periodically illuminates the overside
of the one below to provide a day–night cycle."* This is the single most important visual
detail in the source material, and it is implemented as `undersidePanels` on the ceiling
strata in `Megastructure.drawCeiling`.

**Nihei trained as an architect.** *"His works involve the use of complex architectural
notions (e.g. megastructure in Blame!, arcology in Sidonia) as tropes."* He attended Parsons
School of Design and worked at an architectural design firm. The drawing is done by someone
who knows how buildings are drawn, which is why the reference reads as structures rather than
as shapes.

**Non-humanist architecture.** *"For Nihei, the world is alive in its deadness, but it does
not necessarily live for us."* Kevin Oliver: Nihei *"isn't taking into consideration the
well-being of the city's inhabitants."* Architecture is not for people. There is no
functional reason for anything; things persist because they were built and not destroyed.

**What survives is the ruin of housing.** *"you also see the haunting ruins of housing and
cities that once existed."* The human-scale ruins of habitation are what tell you people were
here — this is `Arrival`'s settlement ruins.

**Scale.** *"these structures always dwarf the..."* Killy is 181cm; everything else is
kilometres. The human figure is the scale reference and it is always tiny.

## Design principles this implies

Ranked by how much they affect the felt experience:

1. **Silhouette before detail.** *"Most of the scenes use simple shapes to make these
   immersive and creative structures."* A bay reads first as a mass against the dark. Do not
   fill every surface.
2. **Negative space is architecture.** *"Vast voids, distant structures, and tiny lights are
   essential."* An empty bay with two distant columns is more BLAME! than a full one.
3. **Distant pinpoints, not uniform light.** A few lamps miles away beat even nearby lighting.
4. **Verticality.** Everything goes up and down forever. Never suggest an edge.
5. **Repetition that breaks.** Modules repeat, then one is missing, tilted, or twice the size.
   This is how the accumulation reads as history rather than as a pattern.
6. **Non-humanist function.** Add structures with no purpose: pipes that end, doors to blank
   walls, stairs to nowhere. These are implemented (`stairs to nowhere`, doors on shafts).
7. **Board-formed concrete.** Horizontal formwork bands and rain streaks. Nihei renders
   concrete with horizontal panel lines.
8. **Layered strata.** The city is layers, not a grid. Undersides are visible undersides.

## What is already implemented

| Principle | Where |
|---|---|
| Day-night underside panels | `Megastructure.drawCeiling` → `undersidePanels` |
| Lightless ceiling strata | `Megastructure.drawCeiling` |
| Sculpted wall facades | `core/Face.java` (7 styles) |
| Brutalist monoliths | `core/Pylon.java` (chamfered, setbacks, fins, reveals) |
| Landmarks | `core/Landmark.java` (WHEEL, CATHEDRAL, MONOLITHS, PIPES, HELIX) |
| Settlement ruins, human scale | `Arrival.settlementRuins` |
| Spires reaching the roof | `Arrival.oceanSpires` → `Pylon.draw` |
| Continuous sky arches | `Arrival.skyArches` |
| Beacons / terminals | `core/Beacons` (in Megastructure.java) |
| Debris and dereliction | debris fields in drawBay / drawMass / drawDeck |

## What is NOT yet implemented (highest value first)

1. **Cyberpunk neon palette.** The v5 handoff asks for coloured glass over sea lanterns.
   Currently only green/amber/red tips exist. The canon city is *not* neon — but the source
   panels on ceiling undersides should read as panels of light, not single blocks.
2. **The Builders.** Autonomous construction machines have a distinct silhouette in the
   source: massive, segmented, with drill/arm appendages. Nothing currently represents them.
3. **Silicon Life growth.** Amorphous accreted matter. `Landmark.MONOLITHS` is close but too
   geometric.
4. **Strata floor.** The handoff asks for a lit floor grid at ~-3300 so the world terminates
   downward as well as up.

## Sources

- https://en.wikipedia.org/wiki/Blame!
- http://journal.finfar.org/articles/speculative-architectures-in-comics
- https://opus.ing/posts/exploring-strange-ultra-futuristic-world-tsutomu-nihei-blame
- https://zahr.substack.com/p/blame-the-dying-earth-masterpiece
- https://carrollnews.org/200840/arts-and-life/niheis-style-is-special
- https://en.wikipedia.org/wiki/List_of_Blame!_characters
- https://en.wikipedia.org/wiki/Tsutomu_Nihei
- https://www.youtube.com/watch?v=PUjwtoAtSD0 (video essay, Saviour)
