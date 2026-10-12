package blame.blamegen.core;

/**
 * Deterministic, infinite BLAME!-style megastructure. Pure Java: no Minecraft classes.
 *
 * Layout, from large to small:
 *  - Coarse cells (1024 blocks square) host one major feature each: a megashaft that
 *    pierces many tiers, a spherical void, a flooded hall, a landmark, or nothing.
 *    Cell (0,0) is the arrival shaft with the beach (see {@link Arrival}). The landmark
 *    branch (see {@link Landmark}) fills what used to be empty: pipe forests, drifting
 *    monoliths, a vast wheel, a hanging cathedral, a helix.
 *  - An irregular orthogonal lattice: grid lines with jittered spacing (96..288) in X and Z
 *    and global tiers (strata) of jittered height (~200..312) in Y. Each bay-tier gets a deck
 *    of some type (full with service corridors, holed, cantilevered, lattice or void), a column
 *    at its corner node, edge beams, hanging cables, catwalks, diagonal ribs and occasional
 *    suspended masses or towers. Diagonal X-orientation transit lines, elevators/stairwells
 *    on major columns, debris fields and fractured decking break up the orthogonal grid.
 *  - Districts carry an architectural character (see {@link #district}): most bays are the
 *    rationalist grid, but whole districts are octagonal stacks, leaning/tilted squares,
 *    arcaded towns, or organic accretions of stacked rotated and sheared prisms with vaults,
 *    terraces, blobs and pooled water, so the world never reads as one repeated module.
 *  - Great walls occupy runs of grid lines over several bays and tiers, with colossal doors,
 *    blast doors, checkpoint booths, signage strips and stairs that end at blank wall.
 *  - A lightless ceiling of dense strata closes over the whole structure just above the
 *    roofscape; only wells over sky-open shafts, vent slots and grid-line slots let daylight in.
 *  - Safeguard beacons (terminal nodes with red/amber/green lamps) mark shaft tiers and
 *    wall nodes. New materials: warning lamps, copper pipework/patina, rust, biofilm
 *    growth, vents, rebar, dark reinforced glass.
 *  - Parameters drift with altitude: a heavy, flooded abyss below, a sparse sky-lattice above.
 *
 * Every feature is contained in the region its owner is responsible for, so a cube only has
 * to evaluate the owners near it. All randomness is hashed from (seed, coordinates), so the
 * same seed always produces the same world regardless of generation order.
 */
public final class Megastructure {
    public static final int GENERATOR_VERSION = 16;

    static final int GRID = 192, GRID_J = 48;
    static final int TIER = 256, TIER_J = 56;
    static final int CELL = 1024;
    static final int MARGIN_XZ = 72, MARGIN_Y = 56;
    /** First tier above the roofscape; above it only spires and floating plates remain. */
    static final int TOP_TIER = 8;

    // ---------------------------------------------------------------- lightless ceiling
    /**
     * The ceiling strata sit just above the roofscape (tier 8, about Y 2100) and cut the whole
     * structure off from natural light: below them only the wells and vents let daylight in.
     * The base plate is always solid where the ceiling covers a column, so "no sky light" is
     * absolute; the layers above it are what you see when you look up from a lit well.
     */
    static final int CEIL_BASE = 2140, CEIL_J = 60, CEIL_T = 64, CEIL_PLATE = 6;
    /** Radius of the hole punched over a coarse cell whose megashaft stays open to the sky. */
    static final int WELL_R = 430, WELL_CELL = 512;

    final long seed;
    final Arrival arrival;
    final Beacons beacons;
    final Toha toha;

    public Megastructure(long worldSeed) {
        this.seed = H.mix(worldSeed ^ 0x8A3F1C2B5D4E6F70L);
        this.arrival = new Arrival(this);
        this.beacons = new Beacons(this);
        this.toha = new Toha(this);
    }

    // ------------------------------------------------------------------ grid helpers

    int lineX(int i) {
        return i * GRID + H.range(H.h(seed, 101, i), -GRID_J, GRID_J);
    }

    int lineZ(int k) {
        return k * GRID + H.range(H.h(seed, 102, k), -GRID_J, GRID_J);
    }

    int tierY(int j) {
        return 64 + j * TIER + (j == 0 ? 0 : H.range(H.h(seed, 103, j), -TIER_J, TIER_J));
    }

    int bayX(int x) {
        int i = Math.floorDiv(x, GRID);
        if (x < lineX(i)) return i - 1;
        if (x >= lineX(i + 1)) return i + 1;
        return i;
    }

    int bayZ(int z) {
        int k = Math.floorDiv(z, GRID);
        if (z < lineZ(k)) return k - 1;
        if (z >= lineZ(k + 1)) return k + 1;
        return k;
    }

    int tierOf(int y) {
        int j = Math.floorDiv(y - 64, TIER);
        if (y < tierY(j)) return j - 1;
        if (y >= tierY(j + 1)) return j + 1;
        return j;
    }

    /** -1 deep abyss .. 0 near the beach .. +1 high sky-lattice, smooth in y. */
    static double altitude(int y) {
        double a = (y - 64) / 3000.0;
        return Math.max(-1, Math.min(1, a));
    }

    /** Y coordinate the player should stand at on the beach (feet). */
    public int spawnY() {
        return arrival.spawnY();
    }

    // ------------------------------------------------------------------ entry point

    /**
     * Offline art-review path: render only the fixed Toha monument, excluding
     * the surrounding world lattice, Arrival basin, and artificial ceiling.
     * Gameplay continues to use generate(Cube).
     */
    public void generateMonument(Cube c) {
        // The perspective ray caster traverses large empty distances. Resolve styles
        // only for intersecting cubes so empty rays remain cheap.
        if (!toha.overlaps(c)) return;
        toha.draw(c);
        c.resolveStyles(seed);
    }

    public void generate(Cube c) {
        // Deep inside the arrival basin nothing of the lattice survives, so do not draw it just
        // to overwrite it. Everything else is drawn first and the basin is stamped over it.
        // The authored Toha monument replaces the lattice above y 0 inside its finite precinct.
        boolean inToha = c.y0 >= 0 && toha.overlaps(c);
        if (!arrival.deepInside(c) && !inToha) {
            drawLattice(c);
            drawWalls(c);
            drawCoarse(c);
        }
        arrival.draw(c);
        if (inToha) toha.draw(c);
        drawCeiling(c); // after the basin: the roof closes over the spires, the oculus stays open
        if (!arrival.deepInside(c) && !inToha) beacons.draw(c);
        c.resolveStyles(seed);
    }

    // ------------------------------------------------------------------ lightless ceiling

    /** Height of the underside of the ceiling plate at this column, or -1 if the ceiling is absent. */
    int ceilDatum(int x, int z) {
        return CEIL_BASE + (int) Math.round((H.noise2(seed ^ 0xC1, x, z, 300) - 0.5) * 2 * CEIL_J);
    }

    /** True when a light well is open above this column, so natural light still reaches down. */
    boolean ceilWell(int x, int z) {
        int cx = Math.floorDiv(x, CELL), cz = Math.floorDiv(z, CELL);
        if (arrival.oculus(x, z)) return true; // the sky opening over the beach
        boolean basin = arrival.inBasin(x, z);
        if (!basin && isSkyShaft(cx, cz)) {
            double dx = x - (cx * CELL + CELL / 2), dz = z - (cz * CELL + CELL / 2);
            if (dx * dx + dz * dz < WELL_R * WELL_R) return true;
        }
        int vx = Math.floorDiv(x, WELL_CELL), vz = Math.floorDiv(z, WELL_CELL);
        long hv = H.h(seed, 620, vx, vz);
        if (H.chance(hv, 0.20)) {
            int ox = vx * WELL_CELL + H.range(H.h(hv, 1), 40, WELL_CELL - 40);
            int oz = vz * WELL_CELL + H.range(H.h(hv, 2), 40, WELL_CELL - 40);
            double dx = x - ox, dz = z - oz;
            int r = H.range(H.h(hv, 3), 16, 62);
            if (dx * dx + dz * dz < r * r) return true;
        }
        // Narrow slots along every third grid line: shafts of daylight between the districts.
        int lx = Math.floorMod(x - lineX(bayX(x)), 3 * GRID);
        int lz = Math.floorMod(z - lineZ(bayZ(z)), 3 * GRID);
        if (lx < 8 || lz < 8) {
            long hs = H.h(seed, 621, Math.floorDiv(x, 3 * GRID), Math.floorDiv(z, 3 * GRID));
            return H.chance(hs, 0.35);
        }
        return false;
    }

    /** True if this coarse cell has a megashaft that stays open to the sky (for ceilWell). */
    private boolean isSkyShaft(int cx, int cz) {
        if (cx == 0 && cz == 0) return false;
        long hc = H.h(seed, 600, cx, cz);
        double r = H.u(H.h(hc, 1));
        return r < 0.55 && H.chance(H.h(hc, 10), 0.5);
    }

    /** Does the lightless ceiling close over this column? Used by the preview for fake skylight. */
    public boolean blocksSkylight(int x, int z) {
        return !ceilWell(x, z);
    }

    void drawCeiling(Cube c) {
        int lo = CEIL_BASE - CEIL_J - CEIL_T - 32, hi = CEIL_BASE + CEIL_J + CEIL_T + 32;
        if (!c.hitsY(lo, hi)) return;
        for (int z = c.z0; z < c.z0 + 16; z++)
            for (int x = c.x0; x < c.x0 + 16; x++) {
                if (ceilWell(x, z)) continue;
                int d = ceilDatum(x, z);
                int t = CEIL_T / 2 + (int) (H.noise2(seed ^ 0xC2, x, z, 120) * CEIL_T);
                int y1 = Math.min(d + t, c.y0 + 15);
                int p1 = Math.min(d + CEIL_PLATE, c.y0 + 15);
                // Base plate: gap-free, so no natural light gets through.
                for (int y = Math.max(d, c.y0); y <= p1; y++) c.set(x, y, z, Mat.S_MASS);
                // Canon: "the underside of each layer periodically illuminates the overside of
                // the one below to provide a day-night cycle." The underside of the base plate
                // carries an irregular array of lighting panels; this is the only artificial
                // sky the deep structure has.
                if (d - 1 >= c.y0 && d - 1 <= c.y0 + 15) undersidePanels(c, x, z, d - 1);
                // Dense strata above: layers with air gaps, ribs and hanging fittings.
                for (int y = p1 + 1; y <= y1; y++) {
                    int ly = y - d - CEIL_PLATE;
                    int layer = Math.floorDiv(ly, 14), in = Math.floorMod(ly, 14);
                    long hl = H.h(seed, 630, x, z, layer);
                    if (in < 4 + H.range(H.h(hl, 1), 0, 3)) {
                        c.set(x, y, z, Math.floorMod(x + z * 3, 26) == 0 ? Mat.S_DARK : Mat.S_MASS);
                        if (in == 0) c.set(x, y, z, Mat.S_DECK);
                    } else if (in == 0 && Math.floorMod(x * 7 + z, 17) < 3) {
                        c.set(x, y, z, Mat.S_DARK);
                    }
                }
            }
    }

    // ------------------------------------------------------------------ decks

    /**
     * Lighting panels on the underside of the ceiling plate: the artificial sky that the canon
     * Megastructure uses to give the layer below a day-night cycle. Panels are arranged in
     * irregular bands of sea-lantern bays separated by dark framing, and occasionally a single
     * amber panel. Density drifts so some districts read as daylight and others as gloom.
     */
    private void undersidePanels(Cube c, int x, int z, int y) {
        // District-scale density so whole regions share one quality of light.
        long hd = H.h(seed, 0x551, Math.floorDiv(x, 256), Math.floorDiv(z, 256));
        double density = 0.20 + H.u(hd) * 0.55;
        // Band structure: a band is a run of lit bays between dark framing strips.
        long hb = H.h(seed, 0x552, x, z);
        if (!H.chance(hb, density)) return;
        byte mat = H.chance(H.h(hb, 1), 0.08) ? Mat.LAMP_AMBER : Mat.SEA_LANTERN;
        c.set(x, y, z, mat);
    }

    static final int D_NONE = 0, D_FULL = 1, D_HOLED = 2, D_PARTIAL = 3, D_LATTICE = 4, D_BLOCK = 5;
    static final int K_VOID = 0, K_FRAME = 1, K_BLOCK = 2;

    static final class Deck {
        int type, top, thick, xa, xb, za, zb;
        int bottom() { return top - thick + 1; }
    }

    /**
     * What occupies bay (i,k) in tier j: a void (part of a chasm), an open frame of decks and
     * columns, or a solid building volume. Voids come from 3D noise so they cluster into
     * chasms several bays wide and several tiers deep; blocks dominate dense districts.
     */
    int kind(int i, int k, int j) {
        if (j >= TOP_TIER) return K_VOID;
        double alt = altitude(tierY(j));
        double v = H.noise3(seed ^ 0x41, i, j * 1.6, k, 2.3);
        double chasm = 0.30 + Math.max(0, alt) * 0.25 - Math.max(0, -alt) * 0.08;
        if (v < chasm) return K_VOID;
        double dens = H.noise2(seed ^ 0x42, i, k, 4.0);
        double pBlock = 0.55 + (dens - 0.5) * 0.7 - Math.max(0, alt) * 0.3 + Math.max(0, -alt) * 0.15;
        return H.chance(H.h(seed, 401, i, k, j), pBlock) ? K_BLOCK : K_FRAME;
    }

    Deck deck(int i, int k, int j) {
        Deck d = new Deck();
        int kd = kind(i, k, j);
        if (kd == K_BLOCK) {
            Blk b = block(i, k, j);
            d.type = D_BLOCK;
            d.xa = b.xa; d.xb = b.xb; d.za = b.za; d.zb = b.zb;
            d.top = b.y0 + 8;
            d.thick = 9;
            return d;
        }
        long hd = H.h(seed, 300, i, k, j);
        int ty = tierY(j);
        double alt = altitude(ty);
        double pVoid = kd == K_VOID ? 1.0 : 0.12 + Math.max(0, alt) * 0.2;
        double r = H.u(H.h(hd, 1));
        if (r < pVoid) d.type = D_NONE;
        else if (r < pVoid + (1 - pVoid) * 0.45) d.type = D_FULL;
        else if (r < pVoid + (1 - pVoid) * 0.62) d.type = D_HOLED;
        else if (r < pVoid + (1 - pVoid) * 0.84) d.type = D_PARTIAL;
        else d.type = D_LATTICE;
        d.top = ty + H.range(H.h(hd, 2), -10, 10) * 2;
        int[] th = {6, 8, 10, 12, 12, 14, 16, 20, 24};
        d.thick = th[H.range(H.h(hd, 3), 0, th.length - 1)];
        if (alt < -0.3) d.thick += 6;
        if (d.type == D_LATTICE) d.thick = Math.min(d.thick, 8);
        int bx0 = lineX(i), bx1 = lineX(i + 1) - 1, bz0 = lineZ(k), bz1 = lineZ(k + 1) - 1;
        int w = bx1 - bx0, dz = bz1 - bz0;
        d.xa = bx0 + (H.chance(H.h(hd, 4), 0.55) ? 0 : H.range(H.h(hd, 5), 4, w / 4));
        d.xb = bx1 - (H.chance(H.h(hd, 6), 0.55) ? 0 : H.range(H.h(hd, 7), 4, w / 4));
        d.za = bz0 + (H.chance(H.h(hd, 8), 0.55) ? 0 : H.range(H.h(hd, 9), 4, dz / 4));
        d.zb = bz1 - (H.chance(H.h(hd, 10), 0.55) ? 0 : H.range(H.h(hd, 11), 4, dz / 4));
        return d;
    }

    // ------------------------------------------------------------------ building volumes

    static final class Blk {
        int xa, xb, za, zb, y0, y1, parts, style, shape;
        boolean alongX;
        int[] tops;
        long h;
    }

    Blk block(int i, int k, int j) {
        Blk b = new Blk();
        long hb = H.h(seed, 410, i, k, j);
        b.h = hb;
        int bx0 = lineX(i), bx1 = lineX(i + 1) - 1, bz0 = lineZ(k), bz1 = lineZ(k + 1) - 1;
        int w = bx1 - bx0, dz = bz1 - bz0;
        b.xa = bx0 + (H.chance(H.h(hb, 1), 0.4) ? 0 : H.range(H.h(hb, 2), 3, Math.min(28, w / 4)));
        b.xb = bx1 - (H.chance(H.h(hb, 3), 0.4) ? 0 : H.range(H.h(hb, 4), 3, Math.min(28, w / 4)));
        b.za = bz0 + (H.chance(H.h(hb, 5), 0.4) ? 0 : H.range(H.h(hb, 6), 3, Math.min(28, dz / 4)));
        b.zb = bz1 - (H.chance(H.h(hb, 7), 0.4) ? 0 : H.range(H.h(hb, 8), 3, Math.min(28, dz / 4)));
        int ty0 = tierY(j), ty1 = tierY(j + 1);
        b.y0 = ty0 + H.range(H.h(hb, 9), -24, 8);
        int gapTop = H.chance(H.h(hb, 10), 0.35) ? 0 : H.range(H.h(hb, 11), 8, 90);
        b.y1 = Math.max(b.y0 + 40, ty1 + H.range(H.h(hb, 12), -24, 8) - gapTop);
        b.parts = H.range(H.h(hb, 13), 1, 3);
        b.alongX = (b.xb - b.xa) >= (b.zb - b.za);
        b.tops = new int[b.parts];
        int tall = H.range(H.h(hb, 14), 0, b.parts - 1);
        for (int p = 0; p < b.parts; p++)
            b.tops[p] = p == tall ? b.y1 : b.y1 - H.range(H.h(hb, 20 + p), 0, (b.y1 - b.y0) / 2);
        b.style = H.range(H.h(hb, 15), 0, 3);
        int sh = H.range(H.h(hb, 16), 0, 9);
        b.shape = sh <= 4 ? 0 : sh <= 6 ? 1 : sh == 7 ? 2 : 3;
        if (Math.min(b.xb - b.xa, b.zb - b.za) < 48 && b.shape == 3) b.shape = 0;
        return b;
    }

    // ------------------------------------------------------------------ shapeless districts

    /**
     * Architectural character of the district containing bay (i,k), from low-frequency noise so
     * whole districts read as one accreted construction campaign:
     *  0 rationalist grid, 1 octagonal stack, 2 leaning / tilted, 3 arcaded town, 4 organic blob.
     */
    int district(int i, int k) {
        double n = H.noise2(seed ^ 0x77, i, k, 0.85);
        if (n < 0.32) return 0;
        if (n < 0.52) return 1;
        if (n < 0.68) return 2;
        if (n < 0.84) return 3;
        return 4;
    }

    /**
     * A shapeless volume: stacked, rotated, sheared prisms of alternating plan shape, fused
     * into one leaning accretion with arcades, terraces and accreted blobs. Everything is
     * clamped to the bay's evaluation band (see the Containment note in the class doc).
     */
    void drawShapeless(Cube c, int i, int k, int j, int bx0, int bx1, int bz0, int bz1,
                       int ty0, int ty1, long hb, int style) {
        long hs = H.h(hb, 90);
        int hall = Math.min(ty1 + 40, tierY(j + 1) + 40);
        int base = ty0 - 30;
        if (base >= hall) return;
        // Provenance: lean into the district wind, then wander per layer.
        double lean = (H.u(H.h(hs, 1)) - 0.5) * 0.16;
        double rot = H.u(H.h(hs, 2)) * Math.PI;
        int layers = 3 + H.range(H.h(hs, 3), 0, 4);
        int top = base;
        int prevKind = -1, prevR = 0;
        int limit = Math.min(bx1 - bx0, bz1 - bz0) / 2 + MARGIN_XZ;
        for (int L = 0; L < layers; L++) {
            long hl = H.h(hs, 100 + L);
            int kind;
            switch (style) {
                case 1: kind = 1; break;                          // octagonal stack
                case 2: kind = 2; break;                          // leaning / tilted squares
                case 4: kind = H.chance(H.h(hl, 1), 0.45) ? 0 : H.range(H.h(hl, 2), 0, 2); break;
                default: kind = H.range(H.h(hl, 1), 0, 2); break; // mixed district
            }
            if (kind == prevKind && H.chance(H.h(hl, 3), 0.55))
                kind = (kind + 1 + H.range(H.h(hl, 4), 0, 1)) % 3;
            int lw = Math.min(bx1 - bx0, bz1 - bz0);
            int r = Math.max(8, Math.min(lw / 2 - 4,
                    H.range(H.h(hl, 5), lw / 5, Math.max(lw / 5 + 1, lw / 2 - 4))));
            if (r <= prevR && H.chance(H.h(hl, 7), 0.4)) r = prevR + H.range(H.h(hl, 8), 0, 8); // accretion
            int grow = H.range(H.h(hl, 6), 30, Math.max(31, Math.min(190, (hall - top) / 2)));
            // Containment: the leaning prism must stay inside the bay footprint + margin.
            int reach = (int) Math.ceil(r + 1 + Math.abs(lean * 1.6) * grow);
            while (reach > limit && grow > 16) {
                grow = grow / 2;
                reach = (int) Math.ceil(r + 1 + Math.abs(lean * 1.6) * grow);
            }
            if (reach > limit) break;
            r = Math.min(r, limit - 1);
            int ltop = Math.min(top + grow, hall);
            double cx = (bx0 + bx1) / 2.0 + H.range(H.h(hl, 9), -10, 10);
            double cz = (bz0 + bz1) / 2.0 + H.range(H.h(hl, 10), -10, 10);
            byte skin = kind == 0 ? Mat.S_MASS : (kind == 1 ? Mat.S_COLUMN : Mat.S_PALE);
            c.prism(cx, cz, r, rot, kind == 2 ? lean * 1.6 : lean, false, kind, top, ltop, skin);
            // Vault at the springing: a vaulted floor over the previous layer.
            c.prism(cx, cz, Math.max(3, r - 4), rot + 0.3, 0, true, Math.max(0, kind - 1), ltop + 1, ltop + 2, Mat.S_DARK);
            // Terrace lip on one side: a rotated plaque, so the silhouette never repeats.
            if (H.chance(H.h(hl, 11), 0.7)) {
                double a = H.u(H.h(hl, 12)) * Math.PI * 2;
                int px = (int) (cx + Math.cos(a) * (r - 2)), pz = (int) (cz + Math.sin(a) * (r - 2));
                c.tiltSlab(px + 1, ltop + 3, pz + 1, r * 0.9, 3, a, Mat.S_DECK);
                c.tiltSlab(px + 1, ltop + 4, pz + 1, r * 0.9, 1, a, Mat.IRON_BARS);
                c.set(px, ltop + 4, pz, Mat.SEA_LANTERN);
            }
            // ===== ARCADED TERRACE (style 3): a colonnade along the sprung vault. =====
            if (style == 3 && H.chance(H.h(hl, 19), 0.75)) {
                long ha = H.h(hl, 300);
                int span = H.range(H.h(ha, 1), 8, 16);
                int rise = H.range(H.h(ha, 2), 5, 12);
                double a = H.u(H.h(ha, 3)) * Math.PI * 2;
                int ax = (int) (cx + Math.cos(a) * (r * 0.85)), az = (int) (cz + Math.sin(a) * (r * 0.85));
                int bx = (int) (cx + Math.cos(a + 1.6) * (r * 0.85)), bz = (int) (cz + Math.sin(a + 1.6) * (r * 0.85));
                c.archRow(ax, ltop + 4, az, bx, ltop + 4, bz, span, rise, 1.5, Mat.S_COLUMN);
                // Walkway above the arcade.
                c.archRow(ax, ltop + 4 + rise, az, bx, ltop + 4 + rise, bz, span, 0, 1.5, Mat.S_DECK);
                // Lamps between the arches at human height.
                c.set(ax, ltop + 6, az, Mat.GLOWSTONE);
                c.set(bx, ltop + 6, bz, Mat.GLOWSTONE);
            }
            // Blobs fused to the mass (organic districts), inside the bay.
            if (style == 4 && H.chance(H.h(hl, 13), 0.6)) {
                double a = H.u(H.h(hl, 14)) * Math.PI * 2;
                int rr = H.range(H.h(hl, 15), 5, Math.max(6, r / 2));
                int bxx = (int) (cx + Math.cos(a) * (r * 0.8)), bzz = (int) (cz + Math.sin(a) * (r * 0.8));
                int mid = top + H.range(H.h(hl, 16), 6, Math.max(7, ltop - top - 6));
                c.blob(bxx + 0.5, mid + 0.5, bzz + 0.5, rr * 0.7, rr, rr * 0.7, Mat.S_MASS);
                if (H.chance(H.h(hl, 17), 0.5)) c.set(bxx, mid + rr, bzz, Mat.SEA_LANTERN);
            }
            // Windows: sparse slits, some dark glass, never a full grid.
            if (H.chance(H.h(hl, 18), 0.8)) {
                for (int y = top + 6; y <= ltop - 4; y += 9) {
                    for (int q = 0; q < 5; q++) {
                        long hq = H.h(hl, 200 + y / 9 * 8 + q);
                        double a = H.u(hq) * Math.PI * 2;
                        double d = r * (0.4 + H.u(H.h(hq, 1)) * 0.5);
                        int wx = (int) Math.floor(cx + Math.cos(a) * d);
                        int wz = (int) Math.floor(cz + Math.sin(a) * d);
                        if (!c.hits(wx, y, wz, wx, y + 2, wz)) continue;
                        if (H.chance(H.h(hq, 2), 0.15)) c.set(wx, y, wz, Mat.GLASS_REINFORCED);
                        else c.set(wx, y, wz, Mat.AIR);
                        c.set(wx, y - 1, wz, Mat.S_DARK);
                    }
                }
            }
            prevKind = kind; prevR = r;
            top = ltop + 2;
            if (top >= hall - 20) break;
        }
        // Crown: rotated plaque, antenna, warning lamps, legible from a distance.
        if (top < hall) {
            int cr = Math.max(6, Math.min(bx1 - bx0, bz1 - bz0) / 5);
            double cx = (bx0 + bx1) / 2.0, cz = (bz0 + bz1) / 2.0;
            double a = H.u(H.h(hs, 30)) * Math.PI;
            c.tiltSlab(cx, top + 3, cz, cr * 2, cr * 1.2, a, Mat.S_DECK);
            c.set((int) cx, top + 4, (int) cz, Mat.LAMP_RED);
            c.set((int) cx, top + 5, (int) cz, Mat.SEA_LANTERN);
            for (int q = 0; q < 3; q++) {
                double aa = a + q * 2.1;
                c.set((int) (cx + Math.cos(aa) * cr), top + 4, (int) (cz + Math.sin(aa) * cr), Mat.LAMP_AMBER);
            }
        }
        // Water pooled in the base terrace: a still sheet reflecting the lamps above.
        int waterY = base + H.range(H.h(hs, 40), 0, 6);
        if (waterY + 3 < top && c.hitsY(waterY, waterY + 3)) {
            int wr = Math.max(6, (int) (Math.min(bx1 - bx0, bz1 - bz0) * 0.28));
            double cx = (bx0 + bx1) / 2.0 + H.range(H.h(hs, 41), -8, 8);
            double cz = (bz0 + bz1) / 2.0 + H.range(H.h(hs, 42), -8, 8);
            c.ring(cx, cz, wr, wr, waterY, waterY + 2, 2, Mat.WATER);
        }
    }

    void drawBlock(Cube c, Blk b) {
        if (!c.hits(b.xa - 2, b.y0 - 6, b.za - 2, b.xb + 2, b.y1 + 8, b.zb + 2)) return;
        switch (b.shape) {
            case 1: drawDrum(c, b); return;
            case 2: drawCantilever(c, b); return;
            case 3: drawTowers(c, b); return;
            default: break;
        }
        int len = b.alongX ? b.xb - b.xa : b.zb - b.za;
        for (int p = 0; p < b.parts; p++) {
            int s0 = (b.alongX ? b.xa : b.za) + len * p / b.parts;
            int s1 = (b.alongX ? b.xa : b.za) + len * (p + 1) / b.parts;
            int xa = b.alongX ? s0 : b.xa, xb = b.alongX ? s1 : b.xb;
            int za = b.alongX ? b.za : s0, zb = b.alongX ? b.zb : s1;
            boxVolume(c, xa, xb, za, zb, b.y0, b.tops[p], b.style, H.h(b.h, 100 + p));
        }
    }

    /** One box volume with facades, service corridors and roof machinery. */
    void boxVolume(Cube c, int xa, int xb, int za, int zb, int y0, int top, int style, long hp) {
        if (!c.hits(xa - 2, y0, za - 2, xb + 2, top + 8, zb + 2)) return;
        c.box(xa, y0, za, xb, top, zb, Mat.S_MASS);
        facade(c, true, xa, -1, za, zb, y0, top, style, hp);
        facade(c, true, xb, 1, za, zb, y0, top, style, hp);
        facade(c, false, za, -1, xa, xb, y0, top, style, hp);
        facade(c, false, zb, 1, xa, xb, y0, top, style, hp);
        // ===== COLOSSAL GROUND DOOR (§3): recessed metal gates at the base. =====
        if (H.chance(H.h(hp, 6), 0.5) && top - y0 > 30) {
            long hg = H.h(hp, 7);
            // Door on a long face when possible (xFace: plane x = f, spanning z).
            boolean xFace = H.chance(H.h(hg, 1), (zb - za) >= (xb - xa) ? 0.85 : 0.15);
            int span = xFace ? zb - za : xb - xa;
            if (span > 30) {
                int gw = H.range(H.h(hg, 2), 6, Math.min(14, span / 3));
                int gh = H.range(H.h(hg, 3), 7, 12);
                int g0 = (xFace ? za : xa) + H.range(H.h(hg, 4), 6, Math.max(7, span - gw - 6));
                boolean ajar = H.chance(H.h(hg, 5), 0.4);
                if (xFace) {
                    int f = H.chance(H.h(hg, 6), 0.5) ? xa : xb;
                    int out = f == xa ? -1 : 1;
                    c.box(f, y0, g0 - 1, f + out, y0 + gh + 1, g0 + gw + 1, Mat.S_DARK);
                    if (ajar) {
                        c.box(f, y0, g0, f + out, y0 + gh, g0 + gw / 2 - 1, Mat.AIR);
                        c.box(f, y0, g0 + gw / 2, f + out, y0 + gh, g0 + gw, Mat.S_METAL);
                    } else {
                        c.box(f, y0, g0, f + out, y0 + gh, g0 + gw, Mat.S_METAL);
                        c.box(f, y0, g0 + gw / 2 - 1, f + out, y0 + gh, g0 + gw / 2, Mat.S_DARK);
                    }
                    c.set(f + out, y0 + gh + 1, g0 + gw / 2, Mat.LAMP_AMBER);
                } else {
                    int f = H.chance(H.h(hg, 6), 0.5) ? za : zb;
                    int out = f == za ? -1 : 1;
                    c.box(g0 - 1, y0, f, g0 + gw + 1, y0 + gh + 1, f + out, Mat.S_DARK);
                    if (ajar) {
                        c.box(g0, y0, f, g0 + gw / 2 - 1, y0 + gh, f + out, Mat.AIR);
                        c.box(g0 + gw / 2, y0, f, g0 + gw, y0 + gh, f + out, Mat.S_METAL);
                    } else {
                        c.box(g0, y0, f, g0 + gw, y0 + gh, f + out, Mat.S_METAL);
                        c.box(g0 + gw / 2 - 1, y0, f, g0 + gw / 2, y0 + gh, f + out, Mat.S_DARK);
                    }
                    c.set(g0 + gw / 2, y0 + gh + 1, f + out, Mat.LAMP_AMBER);
                }
            }
        }
        int oz = H.range(H.h(hp, 1), 4, 20), ox = H.range(H.h(hp, 2), 6, 28);
        int lvl0 = y0 + 4;
        int ya = lvl0 + Math.max(0, Math.floorDiv(c.y0 - 3 - lvl0 + 7, 8)) * 8;
        for (int y = ya; y <= Math.min(top - 5, c.y0 + 15); y += 8) {
            for (int z = za + oz; z + 2 <= zb - 2; z += 24) {
                c.box(xa, y, z, xb, y + 2, z + 2, Mat.AIR);
                for (int x = xa + 6; x <= xb - 2; x += 13)
                    if (H.chance(H.h(hp, 5, x, y * 31 + z), 0.25)) c.set(x, y + 3, z + 1, Mat.GLOWSTONE);
            }
            for (int x = xa + ox; x + 2 <= xb - 2; x += 32) c.box(x, y, za, x + 2, y + 2, zb, Mat.AIR);
        }
        if (c.hitsY(top + 1, top + 8)) {
            int n = H.range(H.h(hp, 3), 1, 5);
            for (int q = 0; q < n; q++) {
                long hq = H.h(hp, 40 + q);
                int mx = H.range(H.h(hq, 1), xa + 2, Math.max(xa + 2, xb - 10));
                int mz = H.range(H.h(hq, 2), za + 2, Math.max(za + 2, zb - 10));
                int mw = H.range(H.h(hq, 3), 3, 9), md = H.range(H.h(hq, 4), 3, 9), mh = H.range(H.h(hq, 5), 2, 7);
                c.box(mx, top + 1, mz, Math.min(xb, mx + mw), top + mh, Math.min(zb, mz + md), H.chance(H.h(hq, 6), 0.5) ? Mat.S_METAL : Mat.S_DARK);
            }
            // Ventilation ducts and exhaust stacks on the roof.
            int nv = H.range(H.h(hp, 4), 1, 3);
            for (int q = 0; q < nv; q++) {
                long hq = H.h(hp, 50 + q);
                int mx = H.range(H.h(hq, 1), xa + 2, Math.max(xa + 2, xb - 6));
                int mz = H.range(H.h(hq, 2), za + 2, Math.max(za + 2, zb - 6));
                int mh = H.range(H.h(hq, 3), 2, 6);
                c.box(mx, top + 1, mz, Math.min(xb, mx + 3), top + mh, Math.min(zb, mz + 3), Mat.VENT);
                if (H.chance(H.h(hq, 4), 0.4)) c.set(mx + 1, top + mh + 1, mz + 1, Mat.LAMP_RED);
            }
        }
    }

    /** Elliptical drum building with dark bands, vertical slits and cross corridors. */
    void drawDrum(Cube c, Blk b) {
        double cx = (b.xa + b.xb + 1) * 0.5, cz = (b.za + b.zb + 1) * 0.5;
        double rx = (b.xb - b.xa + 1) * 0.5, rz = (b.zb - b.za + 1) * 0.5;
        double ro = Math.min(rx, rz), ex = rx / ro, ez = rz / ro;
        c.cylinder(cx, cz, ro, 0, ex, ez, b.y0, b.y1, 0, 0, Mat.S_MASS);
        int first = b.y0 + Math.floorMod(-b.y0, 8);
        int start = first + Math.max(0, Math.floorDiv(c.y0 - 7 - first + 7, 8)) * 8;
        for (int y = start; y <= Math.min(b.y1, c.y0 + 15); y += 8) {
            c.cylinder(cx, cz, ro + 1, ro - 1, ex, ez, y, y, 0, 0, Mat.S_DARK);
            int cy = y + 4;
            if (cy + 2 < b.y1) {
                c.box(b.xa, cy, (int) cz - 1, b.xb, cy + 2, (int) cz + 1, Mat.AIR);
                c.box((int) cx - 1, cy, b.za, (int) cx + 1, cy + 2, b.zb, Mat.AIR);
            }
        }
        int ns = Math.max(12, (int) (2 * Math.PI * ro / 5));
        for (int q = 0; q < ns; q++) {
            double a = q * 2 * Math.PI / ns;
            int x = (int) Math.floor(cx + Math.cos(a) * ex * (ro - 0.5)), z = (int) Math.floor(cz + Math.sin(a) * ez * (ro - 0.5));
            if (x < c.x0 - 1 || x > c.x0 + 16 || z < c.z0 - 1 || z > c.z0 + 16) continue;
            for (int y = Math.max(b.y0 + 2, c.y0); y <= Math.min(b.y1 - 2, c.y0 + 15); y++)
                if (Math.floorMod(y - b.y0, 8) >= 2 && Math.floorMod(y - b.y0, 8) <= 6) {
                    c.set(x, y, z, Mat.AIR);
                    if ((H.h(b.h, 9, q, y) & 127) == 0) c.set(x - (int) Math.signum(Math.cos(a)), y, z - (int) Math.signum(Math.sin(a)), Mat.SEA_LANTERN);
                }
        }
        if (c.hitsY(b.y1 + 1, b.y1 + 6)) {
            c.cylinder(cx, cz, ro * 0.35, 0, ex, ez, b.y1 + 1, b.y1 + 5, 0, 0, Mat.S_METAL);
        }
    }

    /** Narrow base carrying a much wider, heavier top: an overhang with a ribbed underside. */
    void drawCantilever(Cube c, Blk b) {
        int ix = (b.xb - b.xa) * 3 / 10, iz = (b.zb - b.za) * 3 / 10;
        int ym = b.y0 + (b.y1 - b.y0) * (40 + (int) (H.u(H.h(b.h, 30)) * 30)) / 100;
        boxVolume(c, b.xa + ix, b.xb - ix, b.za + iz, b.zb - iz, b.y0, ym, b.style, H.h(b.h, 31));
        boxVolume(c, b.xa, b.xb, b.za, b.zb, ym + 1, b.y1, (b.style + 1) & 3, H.h(b.h, 32));
        for (int x = b.xa + 4; x < b.xb; x += 10) c.box(x, ym - 3, b.za, x + 1, ym, b.zb, Mat.S_DARK);
        for (int x = b.xa + 9; x < b.xb; x += 20)
            for (int z = b.za + 9; z < b.zb; z += 20)
                if (H.chance(H.h(b.h, 33, x, z), 0.5)) c.set(x, ym + 1, z, Mat.SEA_LANTERN);
    }

    /** Several slender towers of different heights linked by thin sky bridges. */
    void drawTowers(Cube c, Blk b) {
        int n = H.range(H.h(b.h, 40), 2, 5);
        int[] tx = new int[n], tz = new int[n], ts = new int[n], tt = new int[n];
        for (int q = 0; q < n; q++) {
            long hq = H.h(b.h, 41, q);
            ts[q] = H.range(H.h(hq, 1), 7, 15);
            tx[q] = H.range(H.h(hq, 2), b.xa + ts[q], Math.max(b.xa + ts[q], b.xb - ts[q]));
            tz[q] = H.range(H.h(hq, 3), b.za + ts[q], Math.max(b.za + ts[q], b.zb - ts[q]));
            tt[q] = b.y0 + 60 + (int) (H.u(H.h(hq, 4)) * Math.max(1, b.y1 - b.y0 - 60));
            boxVolume(c, tx[q] - ts[q], tx[q] + ts[q], tz[q] - ts[q], tz[q] + ts[q], b.y0, tt[q], H.range(H.h(hq, 5), 0, 3), hq);
        }
        for (int q = 0; q + 1 < n; q++) {
            long hq = H.h(b.h, 60, q);
            int y = b.y0 + 20 + H.range(H.h(hq, 1), 0, Math.max(0, Math.min(tt[q], tt[q + 1]) - b.y0 - 30));
            int ax = tx[q], az = tz[q], bx = tx[q + 1], bz = tz[q + 1];
            c.box(Math.min(ax, bx), y, az - 1, Math.max(ax, bx), y + 3, az + 1, Mat.S_MASS);
            c.box(bx - 1, y, Math.min(az, bz), bx + 1, y + 3, Math.max(az, bz), Mat.S_MASS);
            c.box(Math.min(ax, bx), y + 1, az, Math.max(ax, bx), y + 2, az, Mat.AIR);
            c.box(bx, y + 1, Math.min(az, bz), bx, y + 2, Math.max(az, bz), Mat.AIR);
        }
    }

    /**
     * Facade detail on the face plane of a building. xFace: plane x = f, spanning z in [s0,s1];
     * otherwise plane z = f spanning x. out is the outward normal sign.
     */
    void facade(Cube c, boolean xFace, int f, int out, int s0, int s1, int y0, int y1, int style, long h) {
        int lo = Math.min(f - out, f + out), hi = Math.max(f - out, f + out);
        if (xFace ? !c.hits(lo, y0, s0, hi, y1, s1) : !c.hits(s0, y0, lo, s1, y1, hi)) return;
        int ya = Math.max(y0, c.y0), yb = Math.min(y1, c.y0 + 15);
        int sa = Math.max(s0, xFace ? c.z0 : c.x0), sb = Math.min(s1, (xFace ? c.z0 : c.x0) + 15);
        int pil = 12 + (int) (h & 7);
        for (int y = ya; y <= yb; y++) {
            int ly = y - y0;
            boolean band = ly % 8 == 0;
            for (int s = sa; s <= sb; s++) {
                int ls = s - s0;
                int fx = xFace ? f : s, fz = xFace ? s : f;
                if (ls % pil == 0 && ly > 0) {
                    int ox = xFace ? f + out : s, oz = xFace ? s : f + out;
                    c.set(ox, y, oz, Mat.S_DARK);
                }
                if (band) {
                    c.set(fx, y, fz, Mat.S_DARK);
                    continue;
                }
                boolean recess;
                switch (style) {
                    case 0: recess = (ly % 8 == 3 || ly % 8 == 4) && ls % 4 == 2; break;
                    case 1: recess = (ly % 16 >= 4 && ly % 16 <= 11) && ls % 6 == 3; break;
                    case 2: recess = ly % 8 == 4 && ls % 12 >= 2 && ls % 12 <= 9; break;
                    default: recess = false; break;
                }
                if (recess && ly > 2 && y < y1 - 1) {
                    // A few dark, reflective panes among the open recesses: dead windows.
                    if (H.chance(H.h(h, 8, s, y), 0.12)) {
                        c.set(fx, y, fz, Mat.GLASS_REINFORCED);
                        continue;
                    }
                    c.set(fx, y, fz, Mat.AIR);
                    if ((H.h(h, 7, s, y) & 63) == 0) {
                        int ix = xFace ? f - out : s, iz = xFace ? s : f - out;
                        c.set(ix, y, iz, Mat.SEA_LANTERN);
                    }
                }
            }
        }
    }

    // ------------------------------------------------------------------ lattice

    void drawLattice(Cube c) {
        int i0 = bayX(c.x0 - MARGIN_XZ), i1 = bayX(c.x0 + 15 + MARGIN_XZ);
        int k0 = bayZ(c.z0 - MARGIN_XZ), k1 = bayZ(c.z0 + 15 + MARGIN_XZ);
        int j0 = tierOf(c.y0 - MARGIN_Y), j1 = tierOf(c.y0 + 15 + MARGIN_Y);
        for (int j = j0; j <= j1; j++)
            for (int i = i0; i <= i1; i++)
                for (int k = k0; k <= k1; k++)
                    drawBay(c, i, k, j);
    }

    void drawBay(Cube c, int i, int k, int j) {
        int bx0 = lineX(i), bx1 = lineX(i + 1) - 1;
        int bz0 = lineZ(k), bz1 = lineZ(k + 1) - 1;
        int ty0 = tierY(j), ty1 = tierY(j + 1);
        if (!c.hits(bx0 - MARGIN_XZ, ty0 - MARGIN_Y, bz0 - MARGIN_XZ, bx1 + MARGIN_XZ, ty1 + MARGIN_Y, bz1 + MARGIN_XZ)) return;
        if (j >= TOP_TIER) {
            drawSky(c, i, k, j, bx0, bz0, ty0, ty1);
            return;
        }
        long hb = H.h(seed, 400, i, k, j);
        double alt = altitude(ty0);
        int kd = kind(i, k, j);
        if (kd == K_BLOCK) {
            Blk b = block(i, k, j);
            int ds = district(i, k);
            if (ds != 0) {
                // Shapeless districts replace the repeating building volumes entirely.
                drawShapeless(c, i, k, j, bx0, bx1, bz0, bz1, ty0, ty1, b.h, ds);
            } else {
                drawBlock(c, b);
            }
            if (hasColumn(i, k, j)) drawColumn(c, i, k, j, bx0, bz0, ty0, ty1, b.y0 + 8);
            return;
        }
        Deck d = deck(i, k, j);
        Deck up = deck(i, k, j + 1);

        // Masses and towers first, so decks and beams read as passing in front of them.
        double pMass = (kd == K_VOID ? 0.16 : 0.08) + Math.max(0, -alt) * 0.12;
        if (H.chance(H.h(hb, 1), pMass)) drawMass(c, hb, bx0, bx1, bz0, bz1, d, up, ty0, ty1);

        drawDeck(c, d, H.h(hb, 2));

        // Edge beams on this bay's min-z and min-x edges.
        boolean colA = hasColumn(i, k, j), colB = hasColumn(i + 1, k, j), colC = hasColumn(i, k + 1, j);
        long hbeam = H.h(hb, 3);
        int bhw = H.range(H.h(hbeam, 1), 1, 3), bh = H.range(H.h(hbeam, 2), 4, 10);
        if (d.type != D_NONE || (colA && colB)) {
            if (d.type != D_NONE || H.chance(H.h(hbeam, 3), 0.5))
                c.box(bx0, d.top - bh + 1, bz0 - bhw, bx1, d.top, bz0 + bhw, Mat.S_DARK);
        }
        if (d.type != D_NONE || (colA && colC)) {
            if (d.type != D_NONE || H.chance(H.h(hbeam, 4), 0.5))
                c.box(bx0 - bhw, d.top - bh + 1, bz0, bx0 + bhw, d.top, bz1, Mat.S_DARK);
        }

        // Column at the bay's min corner node.
        if (colA) drawColumn(c, i, k, j, bx0, bz0, ty0, ty1, d.top);

        // Cables hanging from the ceiling deck of the tier above into this tier.
        if (up.type != D_NONE && up.type != D_LATTICE) {
            int n = H.range(H.h(hb, 4), 0, alt > 0.3 ? 6 : 3);
            for (int q = 0; q < n; q++) {
                long hc = H.h(hb, 50 + q);
                int x = H.range(H.h(hc, 1), up.xa + 2, Math.max(up.xa + 2, up.xb - 2));
                int z = H.range(H.h(hc, 2), up.za + 2, Math.max(up.za + 2, up.zb - 2));
                int topY = up.bottom() - 1;
                int len = H.range(H.h(hc, 3), 12, Math.max(13, Math.min(220, topY - d.top - 6)));
                int botY = topY - len;
                if (H.chance(H.h(hc, 4), 0.6)) {
                    c.box(x, botY, z, x, topY, z, Mat.IRON_BARS);
                } else {
                    c.box(x, botY, z, x + 1, topY, z + 1, Mat.S_DARK);
                }
                if (H.chance(H.h(hc, 5), 0.4)) c.box(x - 1, botY - 3, z - 1, x + 2, botY - 1, z + 2, Mat.S_METAL);
                if (H.chance(H.h(hc, 6), 0.18)) c.set(x, botY - 4, z, Mat.SEA_LANTERN);
            }
        }

        // Catwalk along one of this bay's edges, between corner nodes.
        if (H.chance(H.h(hb, 5), (kd == K_VOID ? 0.32 : 0.15) + Math.max(0, alt) * 0.3)) {
            long hk = H.h(hb, 6);
            int span = ty1 - d.top;
            if (span > 50) {
                int yc = d.top + H.range(H.h(hk, 1), 20, span - 24);
                if (H.chance(H.h(hk, 2), 0.5)) catwalkX(c, bx0, bx1, bz0, yc);
                else catwalkZ(c, bz0, bz1, bx0, yc);
            }
        }

        // Diagonal rib across the bay, corner node to opposite corner node one tier up.
        if (H.chance(H.h(hb, 7), 0.07 + Math.max(0, alt) * 0.08)) {
            long hr = H.h(hb, 8);
            double hw = H.range(H.h(hr, 1), 2, 3);
            int ya = d.top + H.range(H.h(hr, 2), 0, 60);
            int yb = ty1 - H.range(H.h(hr, 3), 0, 40);
            if (H.chance(H.h(hr, 4), 0.5)) c.beam(bx0, ya, bz0, bx1, yb, bz1, hw, Mat.S_DARK);
            else c.beam(bx1, ya, bz0, bx0, yb, bz1, hw, Mat.S_DARK);
        }

        // ===== X-ORIENTATION TRANSIT TUBES: diagonal and multi-directional passages =====
        // Corner-to-corner diagonals across this bay (optionally climbing a tier), so
        // routing breaks the orthogonal grid while staying inside the owner bay's
        // containment region (same rule as the diagonal ribs above).
        //
        // In shapeless districts the transit lines are tilted angular slabs and cables
        // rather than identical cylindrical tubes: the two bays disagree about the route,
        // so the routing never reads as a repeating part.
        int dsHere = district(i, k);
        if (dsHere != 0 && H.chance(H.h(hb, 9), 0.12 + Math.max(0, alt) * 0.15)) {
            long ht = H.h(hb, 10);
            int c0 = H.range(H.h(ht, 4), 0, 3), c1 = H.range(H.h(ht, 5), 0, 3);
            if (c1 == c0) c1 = (c0 + 1) & 3;
            int sx = (c0 & 1) != 0 ? bx1 : bx0, sz = (c0 & 2) != 0 ? bz1 : bz0;
            int tx = (c1 & 1) != 0 ? bx1 : bx0, tz = (c1 & 2) != 0 ? bz1 : bz0;
            int yt = d.top + H.range(H.h(ht, 2), 10, Math.max(11, (ty1 - d.top) / 2));
            int ty = tierY(j + H.range(H.h(ht, 6), 0, 1)) + H.range(H.h(ht, 7), -30, 30);
            boolean tubeType = H.chance(H.h(ht, 1), 0.5);
            if (tubeType) {
                // Tilted slab bridge: a swept plaque, half slabbing, half railing.
                int steps = (int) Math.sqrt((tx - sx) * (tx - sx) + (tz - sz) * (tz - sz) + (ty - yt) * (ty - yt)) / 3;
                for (int s = 0; s <= steps; s++) {
                    double t = steps == 0 ? 0 : (double) s / steps;
                    int px = sx + (int) ((tx - sx) * t), py = yt + (int) ((ty - yt) * t);
                    int pz = sz + (int) ((tz - sz) * t);
                    double a = Math.atan2(tz - sz, tx - sx);
                    c.tiltSlab(px + 0.5, py + 0.5, pz + 0.5, 5, 3, a, Mat.S_DECK);
                    if ((s & 3) == 0) c.set(px, py + 1, pz, Mat.IRON_BARS);
                    if ((s & 7) == 0) c.set(px, py + 2, pz, Mat.LAMP_AMBER);
                }
            } else {
                // Cable stays and a suspended platform: the ragged shuttle route.
                c.beam(sx, yt, sz, (sx + tx) / 2, (yt + ty) / 2, (sz + tz) / 2, 1, Mat.CABLE);
                c.beam((sx + tx) / 2, (yt + ty) / 2, (sz + tz) / 2, tx, ty, tz, 1, Mat.CABLE);
                int mx = (sx + tx) / 2, my = (yt + ty) / 2, mz = (sz + tz) / 2;
                c.tiltSlab(mx + 0.5, my + 0.5, mz + 0.5, 7, 5, Math.atan2(tz - sz, tx - sx), Mat.S_DECK);
                c.set(mx, my + 2, mz, Mat.SEA_LANTERN);
                c.set(mx, my + 1, mz, Mat.LAMP_RED);
            }
        } else if (H.chance(H.h(hb, 9), 0.12 + Math.max(0, alt) * 0.15)) {
            long ht = H.h(hb, 10);
            int tubeType = H.range(H.h(ht, 1), 0, 2);
            int c0 = H.range(H.h(ht, 4), 0, 3), c1 = H.range(H.h(ht, 5), 0, 3);
            if (c1 == c0) c1 = (c0 + 1) & 3;
            int sx = (c0 & 1) != 0 ? bx1 : bx0, sz = (c0 & 2) != 0 ? bz1 : bz0;
            int tx = (c1 & 1) != 0 ? bx1 : bx0, tz = (c1 & 2) != 0 ? bz1 : bz0;
            int yt = d.top + H.range(H.h(ht, 2), 10, Math.max(11, (ty1 - d.top) / 2));
            int hw = H.range(H.h(ht, 3), 3, 5); // integer radius
            // Level or climbing one tier only: a tube diving a full tier would leave
            // the owner tier's evaluation margin (see Containment in the class doc).
            int ty = tierY(j + H.range(H.h(ht, 6), 0, 1)) + H.range(H.h(ht, 7), -30, 30);
            if (tubeType == 0) {
                // Cylindrical transit tube (hollow, walkable)
                c.tube(sx, yt, sz, tx, ty, tz, hw, hw - 2, Mat.S_PIPEWORK);
                // Interior walkway
                c.tube(sx, yt, sz, tx, ty, tz, Math.max(1, hw - 2), 0, Mat.S_DECK);
                // Service lights along tube
                c.tube(sx, yt, sz, tx, ty, tz, hw, hw - 1, Mat.LAMP_AMBER);
            } else if (tubeType == 1) {
                // Angular rib cage: exposed structural ribs along a diagonal
                c.beam(sx, yt, sz, tx, ty, tz, hw + 1, Mat.S_DARK);
                int steps = (int) Math.sqrt((tx - sx) * (tx - sx) + (tz - sz) * (tz - sz) + (ty - yt) * (ty - yt)) / 8;
                for (int s = 1; s < steps; s++) {
                    double t = (double) s / steps;
                    int rx = sx + (int) ((tx - sx) * t);
                    int ry = yt + (int) ((ty - yt) * t);
                    int rz = sz + (int) ((tz - sz) * t);
                    c.box(rx - 2, ry - 1, rz - 2, rx + 2, ry + 1, rz + 2, Mat.S_DARK);
                    if ((s & 3) == 0) c.set(rx, ry + 2, rz, Mat.LAMP_RED);
                }
            } else {
                // Cable-stayed transit: thick cables with a walkable platform at midpoint
                int mx = (sx + tx) / 2, my = (yt + ty) / 2, mz = (sz + tz) / 2;
                c.beam(sx, yt, sz, mx, my, mz, 2, Mat.CABLE);
                c.beam(mx, my, mz, tx, ty, tz, 2, Mat.CABLE);
                c.box(mx - 4, my - 1, mz - 4, mx + 4, my + 2, mz + 4, Mat.S_DECK);
                c.box(mx - 4, my + 2, mz - 4, mx + 4, my + 2, mz + 4, Mat.IRON_BARS);
                c.set(mx, my + 3, mz, Mat.SEA_LANTERN);
            }
        }

        // ===== ABANDONED RAIL VIADUCT (§6): twin rail lines on a narrow bridge, =====
        // sometimes ending mid-air at unfinished work, sometimes with a lone car.
        if (H.chance(H.h(hb, 13), 0.10 + Math.max(0, alt) * 0.10)) {
            long hr2 = H.h(hb, 14);
            int c0 = H.range(H.h(hr2, 4), 0, 3), c1 = H.range(H.h(hr2, 5), 0, 3);
            if (c1 == c0) c1 = (c0 + 1) & 3;
            int sx = (c0 & 1) != 0 ? bx1 : bx0, sz = (c0 & 2) != 0 ? bz1 : bz0;
            int tx = (c1 & 1) != 0 ? bx1 : bx0, tz = (c1 & 2) != 0 ? bz1 : bz0;
            int ry = d.top + H.range(H.h(hr2, 2), 12, Math.max(13, (ty1 - d.top) / 2));
            boolean brokenEnd = H.chance(H.h(hr2, 6), 0.35);
            int ex = brokenEnd ? sx + (int) ((tx - sx) * (0.4 + H.u(H.h(hr2, 7)) * 0.3)) : tx;
            int ez = brokenEnd ? sz + (int) ((tz - sz) * (0.4 + H.u(H.h(hr2, 7)) * 0.3)) : tz;
            railRun(c, sx, ry, sz, ex, ry, ez);
            // Support pylons down toward the deck.
            int plen = (int) Math.sqrt((ex - sx) * (ex - sx) + (ez - sz) * (ez - sz));
            for (int q = 12; q < plen - 6; q += 24) {
                double t = (double) q / plen;
                int px = sx + (int) ((ex - sx) * t), pz = sz + (int) ((ez - sz) * t);
                c.box(px - 1, d.top, pz - 1, px + 1, ry - 1, pz + 1, Mat.S_DARK);
            }
            if (brokenEnd) {
                // Snapped rails + buffer rubble where construction stopped.
                c.box(ex - 2, ry, ez - 2, ex + 2, ry + 2, ez + 2, Mat.S_DARK);
                c.set(ex, ry + 3, ez, Mat.REBAR);
                c.set(ex, ry + 1, ez, Mat.LAMP_RED);
            } else if (H.chance(H.h(hr2, 8), 0.30)) {
                // A lone freight car abandoned on the bridge.
                int mx = (sx + ex) / 2, mz = (sz + ez) / 2;
                c.box(mx - 4, ry + 1, mz - 2, mx + 4, ry + 4, mz + 2, Mat.S_METAL);
                c.box(mx - 4, ry, mz - 1, mx + 4, ry, mz + 1, Mat.S_DARK);
                c.box(mx - 3, ry + 5, mz - 1, mx + 3, ry + 5, mz + 1, Mat.S_DARK);
                c.set(mx, ry + 2, mz - 2, Mat.GLASS_REINFORCED);
            }
        }

        // ===== CONSTRUCTION CRANE (§5): mast + jib + suspended load, frozen work. =====
        if (d.type != D_NONE && H.chance(H.h(hb, 15), 0.10)) {
            long hk = H.h(hb, 16);
            int px = H.range(H.h(hk, 1), d.xa + 8, Math.max(d.xa + 9, d.xb - 8));
            int pz = H.range(H.h(hk, 2), d.za + 8, Math.max(d.za + 9, d.zb - 8));
            int mh = H.range(H.h(hk, 3), 24, Math.max(25, Math.min(70, ty1 - d.top - 10)));
            int by = d.top;
            c.box(px - 1, by, pz - 1, px + 1, by + mh, pz + 1, Mat.S_DARK);
            int jib = H.range(H.h(hk, 4), 14, 30);
            int dir = H.range(H.h(hk, 5), 0, 3);
            int jx = px + (dir == 0 ? jib : dir == 1 ? -jib : 0);
            int jz = pz + (dir == 2 ? jib : dir == 3 ? -jib : 0);
            c.beam(px, by + mh, pz, jx, by + mh, jz, 1, Mat.S_DARK);
            c.beam(px, by + mh, pz, px - (jx - px) / 3, by + mh, pz - (jz - pz) / 3, 1, Mat.S_DARK);
            int drop = H.range(H.h(hk, 6), 8, Math.max(9, mh - 6));
            c.box(jx, by + mh - drop, jz, jx, by + mh - 1, jz, Mat.CABLE);
            // Suspended load: a beam bundle that never arrived.
            c.box(jx - 3, by + mh - drop - 3, jz - 1, jx + 3, by + mh - drop - 1, jz + 1, Mat.S_METAL);
            c.set(px, by + mh + 1, pz, Mat.LAMP_RED);
            c.set(jx, by + mh - drop - 4, jz, Mat.LAMP_AMBER);
        }

        // ===== TANK CLUSTER (§8, §12): cylindrical reservoirs with pipe links. =====
        if (d.type != D_NONE && H.chance(H.h(hb, 17), 0.12)) {
            long ht2 = H.h(hb, 18);
            int n = H.range(H.h(ht2, 1), 2, 5);
            int prevX = 0, prevZ = 0, prevY = 0;
            for (int q = 0; q < n; q++) {
                long hq = H.h(ht2, 100 + q);
                int tx = H.range(H.h(hq, 1), d.xa + 10, Math.max(d.xa + 11, d.xb - 10));
                int tz = H.range(H.h(hq, 2), d.za + 10, Math.max(d.za + 11, d.zb - 10));
                int tr = H.range(H.h(hq, 3), 4, 8);
                int th = H.range(H.h(hq, 4), 10, Math.max(11, Math.min(30, ty1 - d.top - 6)));
                c.cylinder(tx + 0.5, tz + 0.5, tr + 0.5, 0, 1, 1, d.top, d.top + th, 0, 0, Mat.S_COLUMN);
                c.cylinder(tx + 0.5, tz + 0.5, tr + 1.5, tr - 0.5, 1, 1, d.top + th / 2, d.top + th / 2 + 1, 0, 0, Mat.S_DARK);
                c.box(tx - 1, d.top + th + 1, tz - 1, tx + 1, d.top + th + 2, tz + 1, Mat.S_DARK);
                c.box(tx, d.top + 1, tz + tr, tx, d.top + th - 1, tz + tr, Mat.LADDER_N);
                if (H.chance(H.h(hq, 5), 0.3)) {
                    // Open tank: dark water inside.
                    c.cylinder(tx + 0.5, tz + 0.5, tr - 0.5, 0, 1, 1, d.top + th - 3, d.top + th - 1, 0, 0, Mat.WATER);
                }
                if (q > 0) c.beam(prevX, prevY, prevZ, tx, d.top + 3, tz, 1, Mat.S_PIPEWORK);
                prevX = tx; prevZ = tz; prevY = d.top + 3;
            }
        }

        // ===== DISTRICT MEGA-PIPE (§8): one enormous pipe along the bay edge. =====
        if (d.type != D_NONE && H.chance(H.h(hb, 19), 0.10)) {
            long hm2 = H.h(hb, 20);
            boolean alongX = H.chance(H.h(hm2, 1), 0.5);
            int py = d.top + H.range(H.h(hm2, 2), 4, 14);
            int pr = H.range(H.h(hm2, 3), 3, 6);
            int off = H.range(H.h(hm2, 4), 6, 20);
            int fx = alongX ? bx0 : bx0 + off, fz = alongX ? bz0 + off : bz0;
            int tx = alongX ? bx1 : bx0 + off, tz = alongX ? bz0 + off : bz1;
            double len = Math.sqrt((tx - fx) * (tx - fx) + (tz - fz) * (tz - fz));
            if (len > 40) {
                // Pipe shell (two parallel runs: flow + return).
                c.beam(fx, py, fz, tx, py, tz, pr, Mat.S_PIPEWORK);
                c.beam(fx, py + pr + 2, fz, tx, py + pr + 2, tz, pr - 1, Mat.S_PIPEWORK);
                // Saddles + valve manifolds.
                for (int q = 10; q < len - 5; q += 26) {
                    double t = q / len;
                    int qx = fx + (int) ((tx - fx) * t), qz = fz + (int) ((tz - fz) * t);
                    c.box(qx - pr, d.top, qz - pr, qx + pr, py - pr, qz + pr, Mat.S_DARK);
                    if (H.chance(H.h(hm2, 100 + q), 0.4)) {
                        c.box(qx - 1, py + pr + 1, qz - 1, qx + 1, py + pr + 4, qz + 1, Mat.S_METAL);
                        c.set(qx, py + pr + 5, qz, Mat.LAMP_GREEN);
                    }
                }
            }
        }
        // Debris fields and fallen structural elements in void bays.
        if (kd == K_VOID && H.chance(H.h(hb, 11), 0.4 + Math.max(0, -alt) * 0.2)) {
            long hd = H.h(hb, 12);
            int debrisCount = H.range(H.h(hd, 1), 3, 12);
            for (int q = 0; q < debrisCount; q++) {
                long hq = H.h(hd, 100 + q);
                int dx = H.range(H.h(hq, 1), bx0 + 4, bx1 - 4);
                int dz = H.range(H.h(hq, 2), bz0 + 4, bz1 - 4);
                int dy = H.range(H.h(hq, 3), d.top + 4, Math.min(ty1 - 4, d.top + 80));
                int w = H.range(H.h(hq, 4), 4, 18);
                int d2 = H.range(H.h(hq, 5), 4, 18);
                int h2 = H.range(H.h(hq, 6), 2, 8);
                // Fallen slab / rib
                if (H.chance(hq, 0.6)) {
                    c.box(dx - w/2, dy - h2, dz - d2/2, dx + w/2, dy, dz + d2/2,
                          H.chance(hq, 0.5) ? Mat.S_MASS : Mat.S_DARK);
                    // Exposed rebar
                    for (int x = dx - w/2 + 1; x < dx + w/2; x += 2)
                        c.set(x, dy + 1, dz, Mat.REBAR);
                } else {
                    // Twisted column fragment
                    c.box(dx - 1, dy - h2, dz - 1, dx + 1, dy, dz + 1, Mat.S_COLUMN);
                }
            }
        }
    }

    /**
     * Above the roofscape: a few slender spires rising to very different heights, each
     * crowned with a lamp, and the occasional thin plate hung from a spire.
     */
    void drawSky(Cube c, int i, int k, int j, int x, int z, int ty0, int ty1) {
        long hn = H.h(seed, 210, i, k);
        if (!H.chance(hn, 0.14)) return;
        int r = H.range(H.h(hn, 1), 2, 5);
        double u = H.u(H.h(hn, 2));
        int top = tierY(TOP_TIER) + (int) (u * u * 4800);
        if (ty0 > top + 16) return;
        int ya = ty0, yb = Math.min(ty1, top);
        if (!c.hits(x - 60, ya, z - 60, x + 60, Math.min(ty1, top + 16), z + 60)) return;
        if (yb >= ya) {
            c.box(x - r, ya, z - r, x + r, yb, z + r, Mat.S_COLUMN);
            int first = ya + Math.floorMod(-ya, 48);
            for (int y = first; y <= yb; y += 48) c.box(x - r - 1, y, z - r - 1, x + r + 1, y + 1, z + r + 1, Mat.S_DARK);
        }
        if (top >= ty0 && top < ty1) {
            c.box(x - r - 2, top + 1, z - r - 2, x + r + 2, top + 2, z + r + 2, Mat.S_DARK);
            c.box(x - 1, top + 3, z - 1, x + 1, top + 5, z + 1, Mat.SEA_LANTERN);
            c.box(x, top + 6, z, x, top + 16, z, Mat.IRON_BARS);
        }
        long hp = H.h(seed, 211, i, k, j);
        if (H.chance(hp, 0.25)) {
            int py = ty0 + H.range(H.h(hp, 1), 10, Math.max(11, Math.min(ty1, top) - ty0 - 10));
            if (py < top - 10) {
                int pw = H.range(H.h(hp, 2), 14, 58), pd = H.range(H.h(hp, 3), 14, 58);
                c.box(x - pw, py - 2, z - pd, x + pw, py, z + pd, Mat.S_MASS);
                c.box(x - pw, py + 1, z - pd, x + pw, py + 1, z - pd, Mat.IRON_BARS);
                c.box(x - pw, py + 1, z + pd, x + pw, py + 1, z + pd, Mat.IRON_BARS);
                for (int q = -pw + 6; q < pw; q += 12) c.box(x + q, py - 5, z - pd, x + q + 1, py - 3, z + pd, Mat.S_DARK);
            }
        }
    }

    boolean hasColumn(int i, int k, int j) {
        long hn = H.h(seed, 200, i, k);
        if (!H.chance(hn, 0.82)) return false;
        double alt = altitude(tierY(j));
        return H.chance(H.h(seed, 201, i, k, j), 0.72 - Math.max(0, alt) * 0.2);
    }

    void drawColumn(Cube c, int i, int k, int j, int x, int z, int ty0, int ty1, int deckTop) {
        long hn = H.h(seed, 200, i, k);
        double alt = altitude(ty0);
        double u = H.u(H.h(hn, 1));
        double scale = alt < 0 ? 1 - alt * 0.5 : 1 - alt * 0.45;
        int r = (int) Math.round((4 + u * u * u * 22) * scale);
        r = Math.max(2, Math.min(28, r));
        int shape = H.range(H.h(hn, 2), 0, 3);
        int ya = ty0 - 30, yb = ty1 + 6;
        if (!c.hits(x - r - 4, ya, z - r - 4, x + r + 4, yb, z + r + 4)) return;
        switch (shape) {
            case 0: { // square, with pilaster strips
                c.box(x - r, ya, z - r, x + r, yb, z + r, Mat.S_COLUMN);
                if (r >= 5) {
                    for (int o = -r + 2; o <= r - 2; o += 4) {
                        c.box(x + r + 1, ya, z + o, x + r + 1, yb, z + o, Mat.S_DARK);
                        c.box(x - r - 1, ya, z + o, x - r - 1, yb, z + o, Mat.S_DARK);
                        c.box(x + o, ya, z + r + 1, x + o, yb, z + r + 1, Mat.S_DARK);
                        c.box(x + o, ya, z - r - 1, x + o, yb, z - r - 1, Mat.S_DARK);
                    }
                }
                break;
            }
            case 1: { // round drum with dark rings
                c.cylinder(x + 0.5, z + 0.5, r + 0.5, 0, 1, 1, ya, yb, 0, 0, Mat.S_COLUMN);
                int first = ya + Math.floorMod(-ya, 16);
                for (int y = first; y <= yb; y += 16) {
                    if (c.hitsY(y, y + 1)) c.cylinder(x + 0.5, z + 0.5, r + 1.5, 0, 1, 1, y, y + 1, 0, 0, Mat.S_DARK);
                }
                break;
            }
            case 2: { // cluster of four shafts tied by plates
                int s = Math.max(2, r / 3), o = Math.max(s + 2, (int) (r * 0.7));
                for (int sx = -1; sx <= 1; sx += 2)
                    for (int sz = -1; sz <= 1; sz += 2)
                        c.box(x + sx * o - s, ya, z + sz * o - s, x + sx * o + s, yb, z + sz * o + s, Mat.S_COLUMN);
                int first = ya + Math.floorMod(-ya, 40);
                for (int y = first; y <= yb; y += 40)
                    c.box(x - o - s, y, z - o - s, x + o + s, y + 2, z + o + s, Mat.S_DARK);
                break;
            }
            default: { // hollow service tower with floors and doors at deck level
                int rr = Math.max(r, 6);
                c.cylinder(x + 0.5, z + 0.5, rr + 0.5, rr - 2.5, 1, 1, ya, yb, 0, 0, Mat.S_MASS);
                int first = ya + Math.floorMod(-ya, 12);
                for (int y = first; y <= yb; y += 12)
                    c.cylinder(x + 0.5, z + 0.5, rr - 2.5, 0, 1, 1, y, y, 0, 0, Mat.S_DECK);
                // doors on four sides at the deck level
                c.box(x - 1, deckTop + 1, z - rr - 1, x + 1, deckTop + 3, z + rr + 1, Mat.AIR);
                c.box(x - rr - 1, deckTop + 1, z - 1, x + rr + 1, deckTop + 3, z + 1, Mat.AIR);
                c.box(x - rr + 3, deckTop + 1, z - rr + 3, x + rr - 3, deckTop + 3, z + rr - 3, Mat.AIR);
                c.set(x, deckTop + 4, z, Mat.GLOWSTONE);
                break;
            }
        }
        // Collar at deck level.
        if (shape != 3) c.box(x - r - 2, deckTop - 3, z - r - 2, x + r + 2, deckTop, z + r + 2, Mat.S_DARK);
        // Sparse lamps on the faces just above the deck.
        long hl = H.h(seed, 202, i, k, j);
        if (H.chance(hl, 0.55 + Math.max(0, -alt) * 0.2 - Math.max(0, alt) * 0.25)) {
            int y = deckTop + H.range(H.h(hl, 1), 3, 9);
            int rr = (shape == 2) ? (int) (r * 0.7) + Math.max(2, r / 3) : (shape == 3 ? Math.max(r, 6) : r);
            c.set(x + rr + (shape == 0 && r >= 5 ? 1 : 0), y, z, Mat.SEA_LANTERN);
            c.set(x - rr - (shape == 0 && r >= 5 ? 1 : 0), y, z, Mat.SEA_LANTERN);
            if (H.chance(H.h(hl, 2), 0.5)) {
                c.set(x, y, z + rr, Mat.SEA_LANTERN);
                c.set(x, y, z - rr, Mat.SEA_LANTERN);
            }
        }

        // ===== ELEVATORS, VERTICAL SHAFT LIFTS, STAIRWELLS =====
        // Major columns (r >= 8) get vertical transport infrastructure
        if (r >= 8 && H.chance(H.h(seed, 220, i, k, j), 0.35 + Math.max(0, -alt) * 0.15)) {
            long he = H.h(seed, 221, i, k, j);
            int liftType = H.range(H.h(he, 1), 0, 2);
            int shaftR = Math.min(r + 2, 12);
            int liftYa = ya + 20, liftYb = yb - 10;
            if (liftYa < liftYb - 20) {
                if (liftType == 0) {
                    // Elevator shaft: hollow cylinder with platform at each tier
                    c.cylinder(x + 0.5, z + 0.5, shaftR, shaftR - 2, 1, 1, liftYa, liftYb, 0, 0, Mat.S_DARK);
                    // Interior: vertical cable + platform stops
                    for (int tierJ = j; tierJ <= j + 1 && tierY(tierJ) <= liftYb; tierJ++) {
                        int ty = tierY(tierJ);
                        if (ty >= liftYa && ty <= liftYb) {
                            c.box(x - 1, ty - 2, z - 1, x + 1, ty, z + 1, Mat.S_METAL);
                            c.box(x - 1, ty + 1, z - 1, x + 1, ty + 1, z + 1, Mat.IRON_BARS);
                            c.set(x, ty - 1, z, Mat.LAMP_GREEN);
                            c.set(x, ty + 2, z, Mat.LAMP_AMBER);
                        }
                    }
                    // Counterweight cables
                    for (int dy = 0; dy < 4; dy++) {
                        c.box(x + shaftR + dy, liftYa, z - 1, x + shaftR + dy, liftYb, z + 1, Mat.CABLE);
                    }
                } else if (liftType == 1) {
                    // Spiral stairwell wrapped around column exterior
                    int steps = (liftYb - liftYa) / 2;
                    for (int s = 0; s < steps; s++) {
                        int sy = liftYa + s * 2;
                        double angle = s * Math.PI / 4;
                        int sx = (int) Math.floor(x + Math.cos(angle) * (r + 3));
                        int sz = (int) Math.floor(z + Math.sin(angle) * (r + 3));
                        c.box(sx - 1, sy, sz - 1, sx + 1, sy + 1, sz + 1, Mat.S_DECK);
                        if ((s & 3) == 0) c.set(sx, sy + 1, sz, Mat.LAMP_AMBER);
                        // Handrail
                        c.set(sx, sy + 2, sz, Mat.S_DARK);
                    }
                } else {
                    // Industrial lift: open cage on guide rails
                    c.box(x - shaftR, liftYa, z - shaftR, x + shaftR, liftYb, z + shaftR, Mat.AIR);
                    // Guide rails at corners
                    for (int gx = -1; gx <= 1; gx += 2)
                        for (int gz = -1; gz <= 1; gz += 2) {
                            c.box(x + gx * (shaftR - 1), liftYa, z + gz * (shaftR - 1),
                                  x + gx * (shaftR - 1), liftYb, z + gz * (shaftR - 1), Mat.S_DARK);
                        }
                    // Lift platform at a deterministic position
                    int py = liftYa + H.range(H.h(he, 2), 20, Math.max(21, liftYb - liftYa - 20));
                    c.box(x - shaftR + 1, py - 1, z - shaftR + 1, x + shaftR - 1, py + 1, z + shaftR - 1, Mat.S_METAL);
                    c.box(x - shaftR + 1, py + 2, z - shaftR + 1, x + shaftR - 1, py + 2, z + shaftR - 1, Mat.IRON_BARS);
                    c.set(x, py + 3, z, Mat.LAMP_RED);
                    // Call buttons at each deck level
                    for (int tierJ = j; tierJ <= j + 1 && tierY(tierJ) <= liftYb; tierJ++) {
                        int ty = tierY(tierJ);
                        if (ty >= liftYa && ty <= liftYb && H.chance(H.h(he, 10 + tierJ), 0.4)) {
                            c.set(x - shaftR - 1, ty + 1, z, Mat.LAMP_RED);
                            c.set(x + shaftR + 1, ty + 1, z, Mat.LAMP_GREEN);
                        }
                    }
                }
            }
        }
    }

    void drawDeck(Cube c, Deck d, long h) {
        if (d.type == D_NONE) return;
        int bot = d.bottom();
        if (!c.hits(d.xa - 1, bot - 4, d.za - 1, d.xb + 1, d.top + 2, d.zb + 1)) return;
        switch (d.type) {
            case D_FULL:
            case D_HOLED:
                c.box(d.xa, bot, d.za, d.xb, d.top, d.zb, Mat.S_DECK);
                break;
            case D_PARTIAL: {
                boolean alongX = H.chance(H.h(h, 1), 0.5);
                boolean fromLow = H.chance(H.h(h, 2), 0.5);
                double f = 0.3 + H.u(H.h(h, 3)) * 0.4;
                int ax = Math.max(d.xa, c.x0), bx = Math.min(d.xb, c.x0 + 15);
                int az = Math.max(d.za, c.z0), bz = Math.min(d.zb, c.z0 + 15);
                int ay = Math.max(bot, c.y0), by = Math.min(d.top, c.y0 + 15);
                for (int x = ax; x <= bx; x++)
                    for (int z = az; z <= bz; z++) {
                        int along = alongX ? x : z, across = alongX ? z : x;
                        int lo = alongX ? d.xa : d.za, hi = alongX ? d.xb : d.zb;
                        int edge = lo + (int) (f * (hi - lo)) + H.range(H.h(h, 4, Math.floorDiv(across, 4)), -4, 4);
                        boolean keep = fromLow ? along <= edge : along >= edge;
                        if (!keep) continue;
                        for (int y = ay; y <= by; y++) c.set(x, y, z, Mat.S_DECK);
                    }
                break;
            }
            case D_LATTICE: {
                int s = H.range(H.h(h, 5), 8, 14);
                c.box(d.xa, bot, d.za, d.xb, d.top, d.za + 1, Mat.S_DARK);
                c.box(d.xa, bot, d.zb - 1, d.xb, d.top, d.zb, Mat.S_DARK);
                c.box(d.xa, bot, d.za, d.xa + 1, d.top, d.zb, Mat.S_DARK);
                c.box(d.xb - 1, bot, d.za, d.xb, d.top, d.zb, Mat.S_DARK);
                for (int x = d.xa + s; x < d.xb; x += s) c.box(x, bot, d.za, x + 1, d.top, d.zb, Mat.S_DARK);
                for (int z = d.za + s; z < d.zb; z += s) c.box(d.xa, d.top - 1, z, d.xb, d.top, z + 1, Mat.S_DARK);
                return;
            }
            default:
                return;
        }
        // Seamless slab skin on top of the walkable surface.
        if (c.hitsY(d.top, d.top)) {
            int ax = Math.max(d.xa, c.x0), bx = Math.min(d.xb, c.x0 + 15);
            int az = Math.max(d.za, c.z0), bz = Math.min(d.zb, c.z0 + 15);
            for (int x = ax; x <= bx; x++)
                for (int z = az; z <= bz; z++)
                    if (c.get(x, d.top, z) == Mat.S_DECK) c.set(x, d.top, z, Mat.DOUBLE_SLAB);
        }
        // Downstand ribs under the slab.
        for (int z = d.za + 6; z < d.zb - 2; z += 12)
            c.box(d.xa, bot - 3, z, d.xb, bot - 1, z + 1, Mat.S_DARK);
        // Underside lamp grid, sparse.
        if (c.hitsY(bot, bot)) {
            int gx0 = d.xa + 12 + Math.floorMod(c.x0 - d.xa - 12, 24) - 24;
            for (int x = gx0; x <= c.x0 + 15; x += 24) {
                if (x < c.x0 || x < d.xa || x > d.xb) continue;
                int gz0 = d.za + 9 + Math.floorMod(c.z0 - d.za - 9, 24) - 24;
                for (int z = gz0; z <= c.z0 + 15; z += 24) {
                    if (z < c.z0 || z < d.za || z > d.zb) continue;
                    if (H.chance(H.h(h, 77, x, z), 0.3) && c.get(x, bot, z) != Mat.AIR) c.set(x, bot, z, Mat.SEA_LANTERN);
                }
            }
        }
        // Service corridors inside thick slabs, opening at the slab edges as tiny doors.
        if (d.thick >= 10 && d.type != D_PARTIAL) {
            int cy0 = bot + 2, cy1 = bot + 4;
            if (c.hitsY(cy0, cy1 + 1)) {
                int oz = H.range(H.h(h, 8), 3, 16), ox = H.range(H.h(h, 9), 5, 24);
                for (int z = d.za + oz; z + 2 <= d.zb; z += 20) c.box(d.xa, cy0, z, d.xb, cy1, z + 2, Mat.AIR);
                for (int x = d.xa + ox; x + 2 <= d.xb; x += 28) c.box(x, cy0, d.za, x + 2, cy1, d.zb, Mat.AIR);
                for (int z = d.za + oz; z + 2 <= d.zb; z += 20)
                    for (int x = d.xa + 7; x <= d.xb; x += 14)
                        if (H.chance(H.h(h, 78, x, z), 0.35) && c.get(x, cy1 + 1, z + 1) != Mat.AIR)
                            c.set(x, cy1 + 1, z + 1, Mat.GLOWSTONE);
            }
        }
        if (d.type == D_HOLED) {
            double cx = (d.xa + d.xb) * 0.5 + (H.u(H.h(h, 10)) - 0.5) * (d.xb - d.xa) * 0.3;
            double cz = (d.za + d.zb) * 0.5 + (H.u(H.h(h, 11)) - 0.5) * (d.zb - d.za) * 0.3;
            double rx = (d.xb - d.xa) * (0.18 + H.u(H.h(h, 12)) * 0.18);
            double rz = (d.zb - d.za) * (0.18 + H.u(H.h(h, 13)) * 0.18);
            double rad = Math.min(rx, rz);
            double gap = H.chance(H.h(h, 14), 0.5) ? 0.9 : 0;
            c.cylinder(cx, cz, rad, 0, rx / rad, rz / rad, bot - 4, d.top + 1, 0, 0, Mat.AIR);
            // Broken ring railing round the hole.
            c.cylinder(cx, cz, rad + 1, rad, rx / rad, rz / rad, d.top + 1, d.top + 1, H.u(H.h(h, 15)) * 6.28, gap, Mat.IRON_BARS);
        }

        // ===== RUPTURED PLATES (§1): a blast/break right through a full deck, =====
        // rebar fringe, debris cone below, warning lamps on the rim.
        if ((d.type == D_FULL) && H.chance(H.h(h, 82), 0.10)) {
            long hr = H.h(h, 83);
            double cx = d.xa + H.range(H.h(hr, 1), 12, Math.max(13, (d.xb - d.xa) - 12));
            double cz = d.za + H.range(H.h(hr, 2), 12, Math.max(13, (d.zb - d.za) - 12));
            double rad = H.range(H.h(hr, 3), 4, 9);
            c.cylinder(cx, cz, rad, 0, 1, 1, bot - 2, d.top + 1, 0, 0, Mat.AIR);
            // Bent rebar fringe round the rim.
            int fringe = (int) rad + 1;
            for (int a = 0; a < 12; a++) {
                double t = a * Math.PI / 6 + H.u(H.h(hr, 4, a)) * 0.3;
                int fx = (int) (cx + Math.cos(t) * fringe), fz = (int) (cz + Math.sin(t) * fringe);
                int fh = H.range(H.h(hr, 5, a), 1, 4);
                for (int q = 0; q < fh; q++) c.set(fx, d.top + 1 + q, fz, Mat.REBAR);
            }
            // Debris cone underneath + rim lamps.
            for (int q = 0; q < 8; q++) {
                long hq = H.h(hr, 100 + q);
                int dx = (int) cx + H.range(H.h(hq, 1), -(int) rad - 4, (int) rad + 4);
                int dz = (int) cz + H.range(H.h(hq, 2), -(int) rad - 4, (int) rad + 4);
                c.box(dx - 1, bot - 6 - H.range(H.h(hq, 3), 0, 6), dz - 1, dx + 1, bot - 4, dz + 1, Mat.S_DARK);
            }
            for (int a = 0; a < 4; a++) {
                double t = a * Math.PI / 2 + 0.4;
                c.set((int) (cx + Math.cos(t) * (rad + 2)), d.top + 1, (int) (cz + Math.sin(t) * (rad + 2)), Mat.LAMP_RED);
            }
        }

        // ===== FRACTURED DECKING & DEBRIS ON DECK SURFACES =====
        if (c.hitsY(d.top, d.top) && H.chance(H.h(h, 80), 0.18 + Math.max(0, -altitude(d.top)) * 0.1)) {
            int ax = Math.max(d.xa, c.x0), bx = Math.min(d.xb, c.x0 + 15);
            int az = Math.max(d.za, c.z0), bz = Math.min(d.zb, c.z0 + 15);
            for (int x = ax; x <= bx; x++)
                for (int z = az; z <= bz; z++) {
                    if (c.get(x, d.top, z) != Mat.DOUBLE_SLAB && c.get(x, d.top, z) != Mat.S_DECK) continue;
                    long hp = H.h(h, 81, x, z);
                    double r = H.u(hp);
                    if (r < 0.02) {
                        // Hole punched through deck
                        c.set(x, d.top, z, Mat.AIR);
                        c.set(x, d.top - 1, z, Mat.AIR);
                        if (H.chance(hp, 0.3)) c.set(x, d.top - 2, z, Mat.REBAR);
                    } else if (r < 0.05) {
                        // Cracked slab
                        c.set(x, d.top, z, Mat.S_MASS);
                        if (H.chance(hp, 0.4)) c.set(x, d.top + 1, z, Mat.REBAR);
                    } else if (r < 0.08) {
                        // Debris pile on deck
                        c.set(x, d.top + 1, z, H.chance(hp, 0.5) ? Mat.S_DARK : Mat.S_MASS);
                        c.set(x, d.top + 2, z, H.chance(hp, 0.3) ? Mat.PIPE : Mat.CABLE);
                    } else if (r < 0.11) {
                        // Dust and grit settled on the slab (§14).
                        c.set(x, d.top + 1, z, Mat.GRAVEL);
                    }
                }
        }
    }

    void drawMass(Cube c, long hb, int bx0, int bx1, int bz0, int bz1, Deck d, Deck up, int ty0, int ty1) {
        long hm = H.h(hb, 90);
        int bw = bx1 - bx0, bd = bz1 - bz0;
        int ceiling = up.type != D_NONE ? up.bottom() - 1 : ty1 - 20;
        int floor = d.type != D_NONE ? d.top + 1 : tierY(tierOf(d.top)) - 10;
        int span = ceiling - floor;
        if (span < 40) return;
        double uPick = H.u(H.h(hm, 12));
        if (uPick < 0.18) {
            // ===== SKELETAL INCOMPLETE FRAME (§2): construction frozen mid-build. =====
            // A grid of bare columns with one partial slab and rebar studs; it never
            // connects to anything. Clamped to the owner tier's evaluation band.
            long hf = H.h(hm, 13);
            int fBase = Math.max(floor, ty0 - 40);
            int cols = H.range(H.h(hf, 1), 2, 4);
            int gx0 = bx0 + 8 + H.range(H.h(hf, 2), 0, Math.max(0, bw - 40));
            int gz0 = bz0 + 8 + H.range(H.h(hf, 3), 0, Math.max(0, bd - 40));
            int step = H.range(H.h(hf, 4), 10, 16);
            int fTop = fBase + H.range(H.h(hf, 5), 24, Math.max(25, Math.min(120, span - 20)));
            int slabY = fBase + H.range(H.h(hf, 6), 8, Math.max(9, fTop - fBase - 12));
            for (int ix = 0; ix < cols; ix++)
                for (int iz = 0; iz < cols; iz++) {
                    if (!H.chance(H.h(hf, 20, ix, iz), 0.8)) continue;
                    int px = gx0 + ix * step, pz = gz0 + iz * step;
                    int ph = H.chance(H.h(hf, 21, ix, iz), 0.7) ? fTop : slabY;
                    c.box(px - 1, fBase, pz - 1, px + 1, ph, pz + 1, Mat.S_COLUMN);
                    // Rebar studs where the next section was never poured.
                    for (int q = 0; q < 3; q++)
                        c.set(px - 1 + q, ph + 1 + (q & 1), pz, Mat.REBAR);
                }
            // One partial slab, ragged edge.
            c.box(gx0 - 2, slabY, gz0 - 2, gx0 + cols * step - H.range(H.h(hf, 7), 2, 10), slabY + 2, gz0 + cols * step, Mat.S_DECK);
            if (H.chance(H.h(hf, 8), 0.4)) c.set(gx0, slabY + 3, gz0, Mat.LAMP_AMBER);
            return;
        }
        if (H.chance(H.h(hm, 1), 0.35)) {
            // Tower: hollow drum floor to ceiling; rust-eaten in the deep abyss.
            byte skin = floor < -1000 ? Mat.S_RUST_MASS : Mat.S_MASS;
            int r = H.range(H.h(hm, 2), 10, Math.max(11, Math.min(34, Math.min(bw, bd) / 4)));
            double cx = bx0 + r + 6 + H.u(H.h(hm, 3)) * Math.max(1, bw - 2 * r - 12);
            double cz = bz0 + r + 6 + H.u(H.h(hm, 4)) * Math.max(1, bd - 2 * r - 12);
            if (!c.hits((int) cx - r - 3, floor, (int) cz - r - 3, (int) cx + r + 3, ceiling, (int) cz + r + 3)) return;
            c.cylinder(cx, cz, r, r - 3, 1, 1, floor, ceiling, 0, 0, skin);
            int first = floor + Math.floorMod(-floor, 14);
            for (int y = first; y <= ceiling; y += 14) {
                c.cylinder(cx, cz, r + 1, r - 1, 1, 1, y, y + 1, 0, 0, Mat.S_DARK);
                c.cylinder(cx, cz, r - 3, 0, 1, 1, y, y, 0, 0, Mat.S_DECK);
            }
            // Vertical window slits.
            double gapA = H.u(H.h(hm, 5)) * 6.28;
            for (int s = 0; s < 6; s++) {
                double a = gapA + s * Math.PI / 3;
                int wx = (int) Math.floor(cx + Math.cos(a) * (r - 1.5)), wz = (int) Math.floor(cz + Math.sin(a) * (r - 1.5));
                c.box(wx, floor + 6, wz, wx, ceiling - 6, wz, Mat.AIR);
            }
            return;
        }
        // Box mass, hanging from the ceiling or standing on the deck.
        int w = H.range(H.h(hm, 6), 24, Math.max(25, (int) (bw * 0.6)));
        int dd = H.range(H.h(hm, 7), 24, Math.max(25, (int) (bd * 0.6)));
        int hgt = H.range(H.h(hm, 8), 30, Math.max(31, Math.min(170, span - 10)));
        int x0 = bx0 + 6 + H.range(H.h(hm, 9), 0, Math.max(0, bw - w - 12));
        int z0 = bz0 + 6 + H.range(H.h(hm, 10), 0, Math.max(0, bd - dd - 12));
        boolean hanging = up.type != D_NONE && H.chance(H.h(hm, 11), 0.6);
        int y1 = hanging ? ceiling : floor + hgt, y0 = y1 - hgt;
        int x1 = x0 + w, z1 = z0 + dd;
        if (!c.hits(x0 - 2, y0 - 6, z0 - 2, x1 + 2, y1, z1 + 2)) return;
        c.box(x0, y0, z0, x1, y1, z1, floor < -1000 ? Mat.S_RUST_MASS : Mat.S_MASS);
        c.box(x0 + 3, y0 + 3, z0 + 3, x1 - 3, y1 - 3, z1 - 3, Mat.AIR);
        int first = y0 + 3 + 7;
        for (int y = first; y < y1 - 3; y += 7) c.box(x0 + 3, y, z0 + 3, x1 - 3, y, z1 - 3, Mat.S_DECK);
        // Window slits on two facades, rare lit rooms; some sealed with dark glass.
        for (int y = y0 + 5; y < y1 - 3; y += 7) {
            for (int x = x0 + 3; x < x1 - 2; x += 5) {
                if (H.chance(H.h(hm, 201, x, y), 0.15)) {
                    c.box(x, y, z0, x, y + 1, z0 + 2, Mat.GLASS_REINFORCED);
                    continue;
                }
                c.box(x, y, z0, x, y + 1, z0 + 2, Mat.AIR);
                c.box(x, y, z1 - 2, x, y + 1, z1, Mat.AIR);
                if (H.chance(H.h(hm, 200, x, y), 0.04)) c.set(x, y - 1, z0 + 3, Mat.SEA_LANTERN);
            }
        }
        // Pilasters.
        for (int z = z0 + 4; z < z1; z += 8) {
            c.box(x0 - 1, y0, z, x0 - 1, y1, z, Mat.S_DARK);
            c.box(x1 + 1, y0, z, x1 + 1, y1, z, Mat.S_DARK);
        }
        if (hanging) {
            // Machinery under the hanging mass.
            for (int q = 0; q < 5; q++) {
                long hq = H.h(hm, 300 + q);
                int mx = H.range(H.h(hq, 1), x0 + 2, x1 - 6), mz = H.range(H.h(hq, 2), z0 + 2, z1 - 6);
                int mh = H.range(H.h(hq, 3), 2, 6);
                c.box(mx, y0 - mh, mz, mx + 3, y0 - 1, mz + 3, Mat.S_METAL);
            }
        }

        // ===== SERVER VAULT ANNEX (§5, §16 Netsphere proxy): data racks on one face. =====
        if ((x1 - x0) > 40 && (y1 - y0) > 40 && H.chance(H.h(hm, 22), 0.30)) {
            long hv = H.h(hm, 23);
            boolean north = H.chance(H.h(hv, 1), 0.5);
            int vz0 = north ? z0 - 14 : z1, vz1 = north ? z0 : z1 + 14;
            int vy0 = y0 + H.range(H.h(hv, 2), 4, Math.max(5, (y1 - y0) / 2));
            int vy1 = Math.min(y1 - 4, vy0 + H.range(H.h(hv, 3), 10, 22));
            int vx0 = x0 + H.range(H.h(hv, 4), 4, Math.max(5, (x1 - x0) / 2));
            int vx1 = Math.min(x1 - 2, vx0 + H.range(H.h(hv, 5), 12, 30));
            if (vy1 > vy0 + 6 && vx1 > vx0 + 6) {
                c.box(vx0 - 1, vy0 - 1, vz0 - (north ? 1 : 0), vx1 + 1, vy1 + 1, vz1 + (north ? 0 : 1), Mat.S_DARK);
                c.box(vx0, vy0, vz0, vx1, vy1, vz1, Mat.AIR);
                // Server racks along the back wall with status lamps.
                int rz = north ? vz0 : vz1;
                for (int x = vx0 + 1; x <= vx1 - 1; x += 2)
                    for (int y = vy0 + 1; y <= vy1 - 1; y += 2) {
                        c.set(x, y, rz, Mat.S_DARK);
                        long hl = H.h(hv, 100 + x * 7 + y);
                        double u = H.u(hl);
                        if (u < 0.55) c.set(x, y + 1, rz, Mat.LAMP_GREEN);
                        else if (u < 0.7) c.set(x, y + 1, rz, Mat.LAMP_RED);
                        else if (u < 0.8) c.set(x, y + 1, rz, Mat.LAMP_AMBER);
                    }
                // Control console + dark screens at the room's middle.
                int cxm = (vx0 + vx1) / 2, czm = (vz0 + vz1) / 2;
                c.box(cxm - 2, vy0, czm, cxm + 2, vy0 + 1, czm, Mat.S_METAL);
                c.box(cxm - 2, vy0 + 2, czm, cxm + 2, vy0 + 2, czm, Mat.GLASS_REINFORCED);
                c.set(cxm, vy0 + 3, czm, Mat.LAMP_GREEN);
            }
        }

        // ===== FACTORY MACHINE HALL (§9): press lines inside the hollow mass. =====
        if ((x1 - x0) > 50 && (y1 - y0) > 50 && H.chance(H.h(hm, 24), 0.25)) {
            long hf = H.h(hm, 25);
            int fy = y0 + H.range(H.h(hf, 1), 6, Math.max(7, (y1 - y0) / 3));
            if (fy + 12 < y1 - 4) {
                // Hall floor already exists (mass floor slabs every 7); add press line.
                for (int x = x0 + 6; x + 8 < x1 - 4; x += 16) {
                    c.box(x, fy + 1, z0 + 6, x + 3, fy + 7, z0 + 9, Mat.S_METAL);
                    c.box(x, fy + 8, z0 + 6, x + 3, fy + 8, z0 + 9, Mat.S_DARK);
                    c.box(x + 1, fy + 1, z1 - 9, x + 4, fy + 6, z1 - 6, Mat.S_DARK);
                    c.set(x + 1, fy + 7, z0 + 7, Mat.LAMP_AMBER);
                    // Hook block on an overhead rail.
                    c.box(x0 + 3, fy + 10, z0 + 5, x1 - 3, fy + 10, z1 - 5, Mat.S_DARK);
                    c.box(x + 1, fy + 9, z0 + 7, x + 1, fy + 10, z0 + 7, Mat.CABLE);
                }
            }
        }

        // ===== EXTERNAL STAIR TO A SEALED DOOR (§2 nonsense): climbs the mass face. =====
        if ((y1 - y0) > 60 && H.chance(H.h(hm, 26), 0.30)) {
            long he = H.h(hm, 27);
            boolean west = H.chance(H.h(he, 1), 0.5);
            int fx = west ? x0 : x1;
            int fz = z0 + H.range(H.h(he, 2), 6, Math.max(7, (z1 - z0) - 30));
            int sy = y0 + H.range(H.h(he, 3), 4, 20);
            int room = z1 - 2 - fz;
            if (room >= 10) {
            int steps = Math.min(H.range(H.h(he, 4), 10, 22), room);
            // Treads ride one block off the face so they are not buried in the mass.
            int cx = west ? fx - 2 : fx + 2;
            for (int q = 0; q < steps; q++) {
                int yy = sy + q;
                if (yy > y1 - 6) break;
                c.box(cx - 1, yy, fz + q, cx + 1, yy, fz + q, Mat.S_DECK);
                c.box(cx + (west ? -1 : 1), yy + 1, fz + q, cx + (west ? -1 : 1), yy + 1, fz + q, Mat.IRON_BARS);
            }
            // Sealed door where it lands: slab, no opening.
            int ly = sy + steps;
            if (ly < y1 - 4) {
                c.box(fx, ly, fz + steps - 2, fx, ly + 4, fz + steps + 2, Mat.S_METAL);
                c.set(fx, ly + 5, fz + steps, Mat.LAMP_RED);
            }
            }
        }

        // ===== DEBRIS FIELDS AROUND MASSES =====
        // Clamped to the owner tier's evaluation band (see Containment in the class doc).
        if (H.chance(H.h(hm, 20), 0.25 + Math.max(0, -altitude(floor)) * 0.15)) {
            long hd = H.h(hm, 21);
            int count = H.range(H.h(hd, 1), 5, 20);
            for (int q = 0; q < count; q++) {
                long hq = H.h(hd, 100 + q);
                int dx = H.range(H.h(hq, 1), x0 - 10, x1 + 10);
                int dz = H.range(H.h(hq, 2), z0 - 10, z1 + 10);
                int dy = H.range(H.h(hq, 3), Math.max(floor - 20, ty0 - 40), Math.min(y1, ceiling));
                int dw = H.range(H.h(hq, 4), 2, 12);
                int d2 = H.range(H.h(hq, 5), 2, 12);
                int h2 = H.range(H.h(hq, 6), 1, 6);
                byte mat = H.chance(hq, 0.5) ? Mat.S_MASS : (H.chance(hq, 0.5) ? Mat.S_DARK : Mat.S_RUST_MASS);
                c.box(dx - dw/2, dy - h2, dz - d2/2, dx + dw/2, dy, dz + d2/2, mat);
                if (H.chance(hq, 0.3)) {
                    for (int x = dx - dw/2 + 1; x < dx + dw/2; x += 2)
                        c.set(x, dy + 1, dz, Mat.REBAR);
                }
                if (H.chance(hq, 0.15)) c.set(dx, dy + 2, dz, Mat.LAMP_RED);
            }
        }
    }

    void catwalkX(Cube c, int xa, int xb, int z, int y) {
        if (!c.hits(xa, y - 2, z - 2, xb, y + 2, z + 2)) return;
        c.box(xa, y, z - 2, xb, y, z + 2, Mat.SLAB_TOP);
        c.box(xa, y - 1, z - 2, xb, y - 1, z - 2, Mat.S_DARK);
        c.box(xa, y - 1, z + 2, xb, y - 1, z + 2, Mat.S_DARK);
        c.box(xa, y + 1, z - 2, xb, y + 1, z - 2, Mat.IRON_BARS);
        c.box(xa, y + 1, z + 2, xb, y + 1, z + 2, Mat.IRON_BARS);
        c.box(xa, y + 1, z - 1, xb, y + 3, z + 1, Mat.AIR);
        int first = xa + Math.floorMod(-xa, 4);
        for (int x = first; x <= xb; x += 4) c.box(x, y - 1, z - 1, x, y - 1, z + 1, Mat.S_DARK);
        int f16 = xa + Math.floorMod(8 - xa, 16);
        for (int x = f16; x <= xb; x += 16) c.set(x, y + 2, z - 2, Mat.END_ROD_UP);
    }

    void catwalkZ(Cube c, int za, int zb, int x, int y) {
        if (!c.hits(x - 2, y - 2, za, x + 2, y + 2, zb)) return;
        c.box(x - 2, y, za, x + 2, y, zb, Mat.SLAB_TOP);
        c.box(x - 2, y - 1, za, x - 2, y - 1, zb, Mat.S_DARK);
        c.box(x + 2, y - 1, za, x + 2, y - 1, zb, Mat.S_DARK);
        c.box(x - 2, y + 1, za, x - 2, y + 1, zb, Mat.IRON_BARS);
        c.box(x + 2, y + 1, za, x + 2, y + 1, zb, Mat.IRON_BARS);
        c.box(x - 1, y + 1, za, x + 1, y + 3, zb, Mat.AIR);
        int first = za + Math.floorMod(-za, 4);
        for (int z = first; z <= zb; z += 4) c.box(x - 1, y - 1, z, x + 1, y - 1, z, Mat.S_DARK);
        int f16 = za + Math.floorMod(8 - za, 16);
        for (int z = f16; z <= zb; z += 16) c.set(x - 2, y + 2, z, Mat.END_ROD_UP);
    }

    // ------------------------------------------------------------------ great walls

    /**
     * Abandoned twin-rail track between two points at one height: dark ballast
     * bed, sleeper rows, and twin rail lines (iron bars read as rails).
     */
    void railRun(Cube c, int ax, int y, int az, int bx, int ey, int bz) {
        double dx = bx - ax, dz = bz - az;
        double len = Math.sqrt(dx * dx + dz * dz);
        if (len < 8) return;
        double nx = -dz / len, nz = dx / len; // transverse unit
        int steps = (int) len;
        if (!c.hits(Math.min(ax, bx) - 3, y - 2, Math.min(az, bz) - 3,
                Math.max(ax, bx) + 3, y + 2, Math.max(az, bz) + 3)) return;
        for (int q = 0; q <= steps; q++) {
            double t = (double) q / steps;
            int px = ax + (int) (dx * t), pz = az + (int) (dz * t);
            // Ballast + sleepers every 3.
            if (q % 3 == 0) {
                c.box(px - 2, y - 1, pz - 2, px + 2, y - 1, pz + 2, Mat.S_DARK);
            } else {
                c.set(px, y - 1, pz, Mat.S_DARK);
            }
            // Twin rails.
            int r1x = (int) Math.round(px + nx), r1z = (int) Math.round(pz + nz);
            int r2x = (int) Math.round(px - nx), r2z = (int) Math.round(pz - nz);
            c.set(r1x, y, r1z, Mat.IRON_BARS);
            c.set(r2x, y, r2z, Mat.IRON_BARS);
        }
    }

    void drawWalls(Cube c) {
        int jb0 = Math.floorDiv(tierOf(c.y0 - MARGIN_Y), 3), jb1 = Math.floorDiv(tierOf(c.y0 + 15 + MARGIN_Y), 3);
        // Walls on X lines (planes of constant x).
        int i0 = bayX(c.x0 - 30), i1 = bayX(c.x0 + 15 + 30) + 1;
        int kb0 = Math.floorDiv(bayZ(c.z0 - 30), 4), kb1 = Math.floorDiv(bayZ(c.z0 + 15 + 30), 4);
        for (int i = i0; i <= i1; i++)
            for (int kb = kb0; kb <= kb1; kb++)
                for (int jb = jb0; jb <= jb1; jb++)
                    wall(c, true, i, kb, jb);
        int k0 = bayZ(c.z0 - 30), k1 = bayZ(c.z0 + 15 + 30) + 1;
        int ib0 = Math.floorDiv(bayX(c.x0 - 30), 4), ib1 = Math.floorDiv(bayX(c.x0 + 15 + 30), 4);
        for (int k = k0; k <= k1; k++)
            for (int ib = ib0; ib <= ib1; ib++)
                for (int jb = jb0; jb <= jb1; jb++)
                    wall(c, false, k, ib, jb);
    }

    void wall(Cube c, boolean xLine, int line, int runBlock, int tierBlock) {
        long hw = H.h(seed, xLine ? 500 : 501, line, runBlock, tierBlock);
        double alt = altitude(tierY(tierBlock * 3));
        if (!H.chance(hw, 0.10 + Math.max(0, -alt) * 0.12 - Math.max(0, alt) * 0.06)) return;
        int ra = runBlock * 4 + H.range(H.h(hw, 1), 0, 1), rb = runBlock * 4 + 3 - H.range(H.h(hw, 2), 0, 1);
        int ja = tierBlock * 3 + H.range(H.h(hw, 3), 0, 1), jb = tierBlock * 3 + 2;
        int thick = H.range(H.h(hw, 4), 16, 40), half = thick / 2;
        int lc = xLine ? lineX(line) : lineZ(line);
        int sa = xLine ? lineZ(ra) : lineX(ra), sb = (xLine ? lineZ(rb + 1) : lineX(rb + 1)) - 1;
        int ya = tierY(ja) - 20, yb = tierY(jb + 1) + 10;
        int a0 = lc - half, a1 = lc + half;
        if (xLine) {
            if (!c.hits(a0 - 3, ya, sa, a1 + 3, yb, sb)) return;
        } else {
            if (!c.hits(sa, ya, a0 - 3, sb, yb, a1 + 3)) return;
        }
        // Body, hollow interior with service floors.
        wallBox(c, xLine, a0, a1, sa, sb, ya, yb, Mat.S_MASS);
        wallBox(c, xLine, a0 + 4, a1 - 4, sa + 4, sb - 4, ya + 4, yb - 4, Mat.AIR);
        int first = ya + 4 + 8;
        for (int y = first; y < yb - 4; y += 8) wallBox(c, xLine, a0 + 4, a1 - 4, sa + 4, sb - 4, y, y, Mat.S_DECK);
        // Pilasters on both faces.
        int off = H.range(H.h(hw, 5), 0, 13);
        for (int s = sa + off; s <= sb; s += 14) {
            wallBox(c, xLine, a0 - 2, a0 - 1, s, s + 2, ya, yb, Mat.S_MASS);
            wallBox(c, xLine, a1 + 1, a1 + 2, s, s + 2, ya, yb, Mat.S_MASS);
        }
        // Cornices and small doors at each tier level.
        for (int j = ja; j <= jb + 1; j++) {
            int ty = tierY(j);
            wallBox(c, xLine, a0 - 3, a1 + 3, sa, sb, ty - 3, ty - 1, Mat.S_DARK);
            int dOff = H.range(H.h(hw, 10, j), 0, 39);
            for (int s = sa + 6 + dOff; s + 2 < sb - 6; s += 40) {
                wallBox(c, xLine, a0 - 3, a1 + 3, s, s + 1, ty + 1, ty + 3, Mat.AIR);
                if (H.chance(H.h(hw, 11, j, s), 0.5)) {
                    if (xLine) {
                        c.set(a0 - 1, ty + 5, s, Mat.SEA_LANTERN);
                        c.set(a1 + 1, ty + 5, s + 1, Mat.SEA_LANTERN);
                    } else {
                        c.set(s, ty + 5, a0 - 1, Mat.SEA_LANTERN);
                        c.set(s + 1, ty + 5, a1 + 1, Mat.SEA_LANTERN);
                    }
                }
            }
            // ===== COLOSSAL DOORS / BLAST DOORS (§3, §10): one per tier run. =====
            // A tall metal leaf assembly set into the wall face; some stand ajar,
            // some are sealed blast doors with hazard lamps, flanked by checkpoints.
            if (H.chance(H.h(hw, 12, j), 0.35)) {
                long hg = H.h(hw, 13, j);
                int gw = H.range(H.h(hg, 1), 6, 12), gh = H.range(H.h(hg, 2), 8, 14);
                int gs = H.range(H.h(hg, 3), sa + 10, Math.max(sa + 11, sb - gw - 10));
                int gy = ty;
                boolean blast = H.chance(H.h(hg, 4), 0.35);
                boolean ajar = !blast && H.chance(H.h(hg, 5), 0.5);
                byte leaf = blast ? Mat.S_DARK : Mat.S_METAL;
                // Recess + leaves on both faces.
                for (int f = 0; f < 2; f++) {
                    int fo = f == 0 ? a0 - 3 : a1 + 1;
                    int fi = f == 0 ? a0 : a1;
                    wallBox(c, xLine, fo, f == 0 ? a0 - 1 : a1 + 3, gs - 1, gs + gw + 1, gy, gy + gh + 2, Mat.S_DARK);
                    if (ajar && f == 0) {
                        // One leaf slid aside: opening on the left half, leaf stacked right.
                        wallBox(c, xLine, fi, fi, gs + gw / 2, gs + gw, gy, gy + gh, leaf);
                        wallBox(c, xLine, fo, fi, gs, gs + gw / 2 - 1, gy, gy + gh, Mat.AIR);
                    } else if (!blast) {
                        wallBox(c, xLine, fi, fi, gs, gs + gw / 2 - 1, gy, gy + gh, leaf);
                        wallBox(c, xLine, fi, fi, gs + gw / 2, gs + gw, gy, gy + gh, leaf);
                        wallBox(c, xLine, fi, fi, gs + gw / 2 - 1, gs + gw / 2, gy, gy + gh, Mat.S_DARK);
                    } else {
                        // Sealed blast door: banded slab, no opening.
                        wallBox(c, xLine, fi, fi, gs, gs + gw, gy, gy + gh, leaf);
                        for (int by = gy + 2; by < gy + gh; by += 3)
                            wallBox(c, xLine, fi, fi, gs, gs + gw, by, by, Mat.S_METAL);
                    }
                    // Hazard lamps above the gate.
                    if (xLine) {
                        c.set(f == 0 ? a0 - 1 : a1 + 1, gy + gh + 1, gs - 1, blast ? Mat.LAMP_RED : Mat.LAMP_AMBER);
                        c.set(f == 0 ? a0 - 1 : a1 + 1, gy + gh + 1, gs + gw + 1, blast ? Mat.LAMP_RED : Mat.LAMP_AMBER);
                    } else {
                        c.set(gs - 1, gy + gh + 1, f == 0 ? a0 - 1 : a1 + 1, blast ? Mat.LAMP_RED : Mat.LAMP_AMBER);
                        c.set(gs + gw + 1, gy + gh + 1, f == 0 ? a0 - 1 : a1 + 1, blast ? Mat.LAMP_RED : Mat.LAMP_AMBER);
                    }
                }
                // Checkpoint booths flanking the door on both faces.
                for (int f = 0; f < 2; f++) {
                    int bx = xLine ? (f == 0 ? a0 - 5 : a1 + 5) : gs - 5;
                    int bz = xLine ? gs - 5 : (f == 0 ? a0 - 5 : a1 + 5);
                    c.box(bx - 1, gy, bz - 1, bx + 1, gy + 3, bz + 1, Mat.S_MASS);
                    c.box(bx, gy + 1, bz, bx, gy + 2, bz, Mat.AIR);
                    c.box(bx - 1, gy + 3, bz - 1, bx + 1, gy + 3, bz + 1, Mat.S_DARK);
                    c.set(bx, gy + 2, bz, Mat.GLASS_REINFORCED);
                    c.set(bx, gy + 4, bz, Mat.LAMP_GREEN);
                }
            }
            // ===== SIGNAGE STRIPS (§15): amber guide-light lines along the face. =====
            if (H.chance(H.h(hw, 14, j), 0.5)) {
                int sy = ty + 6;
                if (c.hitsY(sy, sy)) {
                    for (int s = sa + 4; s <= sb - 4; s += 8) {
                        if (!H.chance(H.h(hw, 15, j, s), 0.7)) continue;
                        if (xLine) {
                            c.set(a0 - 1, sy, s, Mat.LAMP_AMBER);
                            if (H.chance(H.h(hw, 16, j, s), 0.3)) c.set(a1 + 1, sy, s + 4, Mat.LAMP_AMBER);
                        } else {
                            c.set(s, sy, a0 - 1, Mat.LAMP_AMBER);
                            if (H.chance(H.h(hw, 16, j, s), 0.3)) c.set(s + 4, sy, a1 + 1, Mat.LAMP_AMBER);
                        }
                    }
                }
            }
        }
        // A few enormous vertical slots.
        if (H.chance(H.h(hw, 6), 0.5)) {
            int s = H.range(H.h(hw, 7), sa + 20, Math.max(sa + 21, sb - 30));
            int w = H.range(H.h(hw, 8), 6, 14);
            int y0 = ya + H.range(H.h(hw, 9), 30, Math.max(31, (yb - ya) / 2));
            wallBox(c, xLine, a0 - 3, a1 + 3, s, s + w, y0, Math.min(yb - 20, y0 + 160), Mat.AIR);
        }
        // ===== STAIR TO NOWHERE (§2): a flight up the face ending at blank wall. =====
        if (H.chance(H.h(hw, 17), 0.30)) {
            long hs = H.h(hw, 18);
            int ss = H.range(H.h(hs, 1), sa + 8, Math.max(sa + 9, sb - 30));
            int sy = tierY(ja) + H.range(H.h(hs, 2), 0, 60);
            int steps = H.range(H.h(hs, 3), 8, 18);
            for (int q = 0; q < steps; q++) {
                int st = ss + q, yy = sy + q;
                if (xLine) {
                    c.box(a0 - 4, yy, st, a0 - 1, yy, st, Mat.S_DECK);
                    c.box(a0 - 4, yy + 1, st, a0 - 4, yy + 1, st, Mat.IRON_BARS);
                } else {
                    c.box(st, yy, a0 - 4, st, yy, a0 - 1, Mat.S_DECK);
                    c.box(st, yy + 1, a0 - 4, st, yy + 1, a0 - 4, Mat.IRON_BARS);
                }
            }
            // It ends at plain wall. One amber lamp marks the absurdity.
            if (xLine) c.set(a0 - 1, sy + steps + 1, ss + steps - 1, Mat.LAMP_AMBER);
            else c.set(ss + steps - 1, sy + steps + 1, a0 - 1, Mat.LAMP_AMBER);
        }
    }

    private void wallBox(Cube c, boolean xLine, int a0, int a1, int s0, int s1, int y0, int y1, byte m) {
        if (xLine) c.box(a0, y0, s0, a1, y1, s1, m);
        else c.box(s0, y0, a0, s1, y1, a1, m);
    }

    // ------------------------------------------------------------------ coarse features

    void drawCoarse(Cube c) {
        int cx = Math.floorDiv(c.x0 + CELL / 2, CELL), cz = Math.floorDiv(c.z0 + CELL / 2, CELL);
        if (cx == 0 && cz == 0) return; // arrival cell
        long hc = H.h(seed, 600, cx, cz);
        double r = H.u(H.h(hc, 1));
        if (r < 0.55) shaft(c, cx, cz, hc);
        else if (r < 0.66) roundVoid(c, cx, cz, hc);
        else if (r < 0.74) seaHall(c, cx, cz, hc);
        else Landmark.draw(this, c, cx, cz, hc);
    }

    void shaft(Cube c, int cx, int cz, long hc) {
        int R = H.range(H.h(hc, 2), 60, 200);
        int wt = H.range(H.h(hc, 3), 4, 9);
        int lim = CELL / 2 - R - wt - 24;
        int ox = cx * CELL + H.range(H.h(hc, 4), -lim, lim), oz = cz * CELL + H.range(H.h(hc, 5), -lim, lim);
        int ja = H.range(H.h(hc, 6), -14, 3);
        int jb = H.chance(H.h(hc, 10), 0.5) ? TOP_TIER : Math.min(TOP_TIER, ja + H.range(H.h(hc, 7), 4, 20));
        // Shafts that reach the roofscape stay open to the sky, carrying daylight deep down.
        int ya = tierY(ja), yb = jb == TOP_TIER ? tierY(TOP_TIER) + 60 : tierY(jb);
        int outer = R + wt + 2;
        if (!c.hits(ox - outer, ya - 12, oz - outer, ox + outer, yb + 4, oz + outer)) return;
        double ccx = ox + 0.5, ccz = oz + 0.5;
        boolean flooded = H.chance(H.h(hc, 8), 0.35);
        boolean ribcage = H.chance(H.h(hc, 9), 0.4);
        // Void.
        c.cylinder(ccx, ccz, R, 0, 1, 1, ya, yb, 0, 0, Mat.AIR);
        // Wall: perforated shell, or open ribcage of vertical ribs.
        if (!ribcage) {
            c.cylinder(ccx, ccz, R + wt, R, 1, 1, ya, yb, 0, 0, Mat.S_MASS);
            // Perforations in an irregular angular / vertical bucket pattern.
            if (c.hits(ox - outer, ya, oz - outer, ox + outer, yb, oz + outer)) {
                int ay = Math.max(ya + 16, c.y0), by = Math.min(yb, c.y0 + 15);
                for (int z = c.z0; z < c.z0 + 16; z++)
                    for (int x = c.x0; x < c.x0 + 16; x++) {
                        double dx = x + 0.5 - ccx, dz = z + 0.5 - ccz;
                        double d = Math.sqrt(dx * dx + dz * dz);
                        if (d < R || d >= R + wt) continue;
                        int ab = (int) Math.floor((Math.atan2(dz, dx) + Math.PI) * R / 14.0);
                        for (int y = ay; y <= by; y++) {
                            int yb2 = Math.floorDiv(y - ya, 36);
                            if (H.chance(H.h(hc, 700, ab, yb2), 0.22) && Math.floorMod(y - ya, 36) > 6) c.set(x, y, z, Mat.AIR);
                        }
                    }
            }
        } else {
            int nr = Math.max(12, (int) (2 * Math.PI * R / 22));
            for (int q = 0; q < nr; q++) {
                double a = q * 2 * Math.PI / nr;
                int rx = (int) Math.floor(ccx + Math.cos(a) * (R + 2)), rz = (int) Math.floor(ccz + Math.sin(a) * (R + 2));
                c.box(rx - 2, ya, rz - 2, rx + 2, yb, rz + 2, Mat.S_DARK);
            }
        }
        // Inner pilasters and ring ledges with a ring of lamps receding into the depth.
        int np = Math.max(16, (int) (2 * Math.PI * R / 18));
        if (!ribcage) {
            for (int q = 0; q < np; q++) {
                double a = q * 2 * Math.PI / np;
                int px = (int) Math.floor(ccx + Math.cos(a) * (R - 1.5)), pz = (int) Math.floor(ccz + Math.sin(a) * (R - 1.5));
                c.box(px - 1, ya, pz - 1, px + 1, yb, pz + 1, Mat.S_COLUMN);
            }
        }
        int ledgeStep = 64;
        int first = ya + Math.floorMod(-ya, ledgeStep);
        for (int y = first; y <= yb; y += ledgeStep) {
            if (!c.hitsY(y - 1, y + 2)) continue;
            c.cylinder(ccx, ccz, R + 0.5, R - 5, 1, 1, y - 1, y, 0, 0, Mat.S_DARK);
            c.cylinder(ccx, ccz, R - 5, R - 6, 1, 1, y + 1, y + 1, H.u(H.h(hc, 20, y)) * 6.28, 0.5, Mat.IRON_BARS);
            if (H.chance(H.h(hc, 21, y), 0.6)) {
                for (int q = 0; q < np; q++) {
                    double a = (q + 0.5) * 2 * Math.PI / np;
                    c.set((int) Math.floor(ccx + Math.cos(a) * (R - 0.5)), y + 2, (int) Math.floor(ccz + Math.sin(a) * (R - 0.5)), Mat.SEA_LANTERN);
                }
            }
        }
        // Bridges across the void, with a central platform and a long cable below it.
        // Some are broken: they end mid-air with rebar fringe and a fallen segment.
        for (int j = ja + 1; j < jb; j++) {
            long hj = H.h(hc, 30, j);
            if (!H.chance(hj, 0.5)) continue;
            int y = tierY(j) + H.range(H.h(hj, 1), -20, 40);
            if (!c.hitsY(y - 140, y + 4)) continue;
            int mode = H.range(H.h(hj, 2), 0, 2);
            boolean broken = H.chance(H.h(hj, 4), 0.25);
            if (!broken) {
                if (mode != 1) catwalkX(c, ox - R, ox + R, oz, y);
                if (mode != 0) catwalkZ(c, oz - R, oz + R, ox, y);
            } else {
                // Stub from one wall, ending in a snapped fringe over the drop.
                int reach = H.range(H.h(hj, 5), R / 3, R * 2 / 3);
                if (mode != 1) {
                    int dir = H.chance(H.h(hj, 6), 0.5) ? 1 : -1;
                    int xa = dir > 0 ? ox - R : ox + R - reach, xb = dir > 0 ? ox - R + reach : ox + R;
                    if (c.hits(xa, y - 2, oz - 2, xb, y + 2, oz + 2)) {
                        c.box(xa, y, oz - 2, xb, y, oz + 2, Mat.SLAB_TOP);
                        c.box(xa, y - 1, oz - 2, xb, y - 1, oz - 2, Mat.S_DARK);
                        c.box(xa, y - 1, oz + 2, xb, y - 1, oz + 2, Mat.S_DARK);
                        int ex = dir > 0 ? xb : xa;
                        for (int q = 0; q < 4; q++) c.set(ex - dir * q, y + (q & 1), oz - 2 + (q & 1) * 4, Mat.REBAR);
                        // Fallen segment tangled below.
                        c.box(ex - dir * 8, y - 14, oz - 3, ex + dir * 2, y - 12, oz + 3, Mat.S_DARK);
                        c.set(ex - dir * 4, y - 11, oz, Mat.LAMP_RED);
                    }
                } else {
                    int dir = H.chance(H.h(hj, 6), 0.5) ? 1 : -1;
                    int za = dir > 0 ? oz - R : oz + R - reach, zb = dir > 0 ? oz - R + reach : oz + R;
                    if (c.hits(ox - 2, y - 2, za, ox + 2, y + 2, zb)) {
                        c.box(ox - 2, y, za, ox + 2, y, zb, Mat.SLAB_TOP);
                        c.box(ox - 2, y - 1, za, ox - 2, y - 1, zb, Mat.S_DARK);
                        c.box(ox + 2, y - 1, za, ox + 2, y - 1, zb, Mat.S_DARK);
                        int ez = dir > 0 ? zb : za;
                        for (int q = 0; q < 4; q++) c.set(ox - 2 + (q & 1) * 4, y + (q & 1), ez - dir * q, Mat.REBAR);
                        c.box(ox - 3, y - 14, ez - dir * 8, ox + 3, y - 12, ez + dir * 2, Mat.S_DARK);
                        c.set(ox, y - 11, ez - dir * 4, Mat.LAMP_RED);
                    }
                }
            }
            c.cylinder(ccx, ccz, 7, 0, 1, 1, y - 2, y, 0, 0, Mat.S_DARK);
            c.cylinder(ccx, ccz, 6, 0, 1, 1, y, y, 0, 0, Mat.DOUBLE_SLAB);
            int len = H.range(H.h(hj, 3), 40, 130);
            c.box(ox, y - len, oz, ox, y - 3, oz, Mat.IRON_BARS);
            c.set(ox, y - len - 1, oz, Mat.SEA_LANTERN);
        }
        // ===== DOORS TO NOWHERE (§2): door slabs high on the shaft wall, no access. =====
        if (!ribcage && H.chance(H.h(hc, 60), 0.6)) {
            int nd = H.range(H.h(hc, 61), 1, 3);
            for (int q = 0; q < nd; q++) {
                long hq = H.h(hc, 62, q);
                double a = H.u(hq) * 2 * Math.PI;
                int dx = (int) Math.floor(ccx + Math.cos(a) * (R - 1.5));
                int dz = (int) Math.floor(ccz + Math.sin(a) * (R - 1.5));
                int dy = ya + H.range(H.h(hq, 1), 30, Math.max(31, yb - ya - 30));
                int dw = H.range(H.h(hq, 2), 4, 8), dh = H.range(H.h(hq, 3), 6, 10);
                c.box(dx - 2, dy - 1, dz - 2, dx + 2, dy + dh + 1, dz + 2, Mat.S_DARK);
                c.box(dx, dy, dz, dx, dy + dh - 1, dz, Mat.AIR);
                c.box(dx - dw / 2, dy, dz, dx - 1, dy + dh - 1, dz, Mat.S_METAL);
                c.box(dx + 1, dy, dz, dx + dw / 2, dy + dh - 1, dz, Mat.S_METAL);
                if (H.chance(H.h(hq, 4), 0.5)) c.set(dx, dy + dh, dz, Mat.LAMP_AMBER);
            }
        }
        // ===== RUINED DEFENSE STATION (§10): bunker overlooking the void. =====
        if (H.chance(H.h(hc, 63), 0.45)) {
            long hb2 = H.h(hc, 64);
            double a = H.u(hb2) * 2 * Math.PI;
            int bx = (int) Math.floor(ccx + Math.cos(a) * (R - 4));
            int bz = (int) Math.floor(ccz + Math.sin(a) * (R - 4));
            int by = ya + H.range(H.h(hb2, 1), 40, Math.max(41, yb - ya - 60));
            c.box(bx - 3, by, bz - 3, bx + 3, by + 3, bz + 3, Mat.S_MASS);
            c.box(bx - 2, by + 1, bz - 2, bx + 2, by + 2, bz + 2, Mat.AIR);
            // Firing slit facing the void (toward shaft center).
            int fx = bx + (int) Math.signum(ccx - bx), fz = bz + (int) Math.signum(ccz - bz);
            c.box(Math.min(bx, fx) - 1, by + 1, Math.min(bz, fz) - 1,
                    Math.max(bx, fx) + 1, by + 2, Math.max(bz, fz) + 1, Mat.AIR);
            c.set(bx, by + 3, bz, Mat.S_DARK);
            c.set(fx, by, fz, Mat.LAMP_RED);
            c.set(bx, by + 4, bz, Mat.IRON_BARS);
        }
        // ===== BROKEN PIPELINE STUB (§8): pipe jutting from the wall, spilling cables. =====
        if (H.chance(H.h(hc, 65), 0.5)) {
            long hp = H.h(hc, 66);
            double a = H.u(hp) * 2 * Math.PI;
            int px = (int) Math.floor(ccx + Math.cos(a) * (R + wt - 1));
            int pz = (int) Math.floor(ccz + Math.sin(a) * (R + wt - 1));
            int py = ya + H.range(H.h(hp, 1), 30, Math.max(31, yb - ya - 40));
            int jx = px + (int) (Math.cos(a) * -18), jz = pz + (int) (Math.sin(a) * -18);
            c.beam(px, py, pz, jx, py - 6, jz, 2, Mat.S_PIPEWORK);
            c.beam(jx, py - 6, jz, jx, py - 26, jz, 1, Mat.CABLE);
            c.box(jx - 1, py - 30, jz - 1, jx + 1, py - 27, jz + 1, Mat.S_METAL);
            if (H.chance(H.h(hp, 2), 0.5)) c.set(jx, py - 31, jz, Mat.LAMP_RED);
        }
        // Long cables hanging from ledges.
        int nc = H.range(H.h(hc, 40), 6, 18);
        for (int q = 0; q < nc; q++) {
            long hq = H.h(hc, 41, q);
            double a = H.u(hq) * 2 * Math.PI;
            int x = (int) Math.floor(ccx + Math.cos(a) * (R - 8)), z = (int) Math.floor(ccz + Math.sin(a) * (R - 8));
            int top = first + H.range(H.h(hq, 1), 1, Math.max(1, (yb - first) / ledgeStep)) * ledgeStep;
            int len = H.range(H.h(hq, 2), 30, 260);
            c.box(x, top - len, z, x, top - 2, z, Mat.IRON_BARS);
            c.box(x - 2, top - 1, z - 2, x + 2, top - 1, z + 2, Mat.S_DARK);
        }
        // Flooded bottom: an interior sea inside the shaft.
        if (flooded) {
            c.cylinder(ccx, ccz, R + wt, 0, 1, 1, ya - 12, ya - 1, 0, 0, Mat.S_MASS);
            c.cylinder(ccx, ccz, R + wt, R, 1, 1, ya, ya + 16, 0, 0, Mat.S_MASS);
            c.cylinder(ccx, ccz, R, 0, 1, 1, ya, ya + 9, 0, 0, Mat.WATER);
            // Islands of fallen structure.
            for (int q = 0; q < 5; q++) {
                long hq = H.h(hc, 50, q);
                double a = H.u(hq) * 6.28, rr = H.u(H.h(hq, 1)) * (R - 20);
                int ix = (int) (ccx + Math.cos(a) * rr), iz = (int) (ccz + Math.sin(a) * rr);
                int s = H.range(H.h(hq, 2), 3, 10);
                c.box(ix - s, ya, iz - s, ix + s, ya + 10 + H.range(H.h(hq, 3), 0, 6), iz + s, Mat.S_MASS);
                c.set(ix, ya + 11 + H.range(H.h(hq, 3), 0, 6), iz, Mat.SEA_LANTERN);
            }
        }
    }

    void roundVoid(Cube c, int cx, int cz, long hc) {
        int R = H.range(H.h(hc, 2), 80, 170);
        int lim = CELL / 2 - R - 30;
        int ox = cx * CELL + H.range(H.h(hc, 4), -lim, lim), oz = cz * CELL + H.range(H.h(hc, 5), -lim, lim);
        int oy = tierY(H.range(H.h(hc, 6), -12, TOP_TIER - 2)) + H.range(H.h(hc, 7), -40, 40);
        if (!c.hits(ox - R - 6, oy - R - 6, oz - R - 6, ox + R + 6, oy + R + 6, oz + R + 6)) return;
        c.sphere(ox + 0.5, oy + 0.5, oz + 0.5, R + 4, R, Mat.S_PALE);
        c.sphere(ox + 0.5, oy + 0.5, oz + 0.5, R, 0, Mat.AIR);
        // Equatorial ring walkway with lamps, and a single suspended core.
        c.cylinder(ox + 0.5, oz + 0.5, R + 1, R - 8, 1, 1, oy - 1, oy, 0, 0, Mat.S_DARK);
        c.cylinder(ox + 0.5, oz + 0.5, R - 8, R - 9, 1, 1, oy + 1, oy + 1, 0, 0, Mat.IRON_BARS);
        int np = (int) (2 * Math.PI * R / 20);
        for (int q = 0; q < np; q++) {
            double a = q * 2 * Math.PI / np;
            c.set((int) Math.floor(ox + 0.5 + Math.cos(a) * (R - 3)), oy + 1, (int) Math.floor(oz + 0.5 + Math.sin(a) * (R - 3)), Mat.SEA_LANTERN);
        }
        int core = R / 6;
        c.sphere(ox + 0.5, oy + 0.5, oz + 0.5, core, 0, Mat.S_DARK);
        c.box(ox, oy + core, oz, ox, oy + R, oz, Mat.IRON_BARS);
        c.set(ox, oy - core - 1, oz, Mat.SEA_LANTERN);
        // Four access tunnels from the ring outwards.
        for (int q = 0; q < 4; q++) {
            int dx = q == 0 ? 1 : q == 1 ? -1 : 0, dz = q == 2 ? 1 : q == 3 ? -1 : 0;
            int ax = ox + dx * (R - 8), az = oz + dz * (R - 8), bx = ox + dx * (R + 60), bz = oz + dz * (R + 60);
            c.box(Math.min(ax, bx) - (dz != 0 ? 1 : 0), oy + 1, Math.min(az, bz) - (dx != 0 ? 1 : 0),
                    Math.max(ax, bx) + (dz != 0 ? 1 : 0), oy + 3, Math.max(az, bz) + (dx != 0 ? 1 : 0), Mat.AIR);
        }
    }

    void seaHall(Cube c, int cx, int cz, long hc) {
        int hx = H.range(H.h(hc, 2), 140, 220), hz = H.range(H.h(hc, 3), 140, 220);
        int hh = H.range(H.h(hc, 4), 70, 140);
        int lim = CELL / 2 - Math.max(hx, hz) - 20;
        int ox = cx * CELL + H.range(H.h(hc, 5), -lim, lim), oz = cz * CELL + H.range(H.h(hc, 6), -lim, lim);
        int floorY = tierY(H.range(H.h(hc, 7), -14, 4)) + 4;
        if (!c.hits(ox - hx - 8, floorY - 14, oz - hz - 8, ox + hx + 8, floorY + hh + 12, oz + hz + 8)) return;
        c.box(ox - hx - 6, floorY - 12, oz - hz - 6, ox + hx + 6, floorY + hh + 10, oz + hz + 6, Mat.S_MASS);
        c.box(ox - hx, floorY, oz - hz, ox + hx, floorY + hh, oz + hz, Mat.AIR);
        c.box(ox - hx, floorY, oz - hz, ox + hx, floorY + 7, oz + hz, Mat.WATER);
        // Forest of thin columns standing in the water; deep halls grow biofilm.
        byte colSkin = floorY < -200 ? Mat.S_BIO_STONE : Mat.S_COLUMN;
        int step = H.range(H.h(hc, 8), 18, 30);
        for (int x = ox - hx + step / 2; x < ox + hx; x += step)
            for (int z = oz - hz + step / 2; z < oz + hz; z += step) {
                long hq = H.h(hc, 60, x, z);
                if (!H.chance(hq, 0.8)) continue;
                int jx = x + H.range(H.h(hq, 1), -3, 3), jz = z + H.range(H.h(hq, 2), -3, 3);
                c.box(jx - 1, floorY, jz - 1, jx + 1, floorY + hh, jz + 1, colSkin);
                if (H.chance(H.h(hq, 3), 0.12)) c.set(jx, floorY + 8, jz + 2, Mat.SEA_LANTERN);
            }
        // Ceiling lamps, very sparse, and access doors on two walls.
        for (int q = 0; q < 12; q++) {
            long hq = H.h(hc, 70, q);
            c.set(ox + H.range(H.h(hq, 1), -hx, hx), floorY + hh, oz + H.range(H.h(hq, 2), -hz, hz), Mat.SEA_LANTERN);
        }
        c.box(ox - 1, floorY + 8, oz - hz - 7, ox + 1, floorY + 11, oz - hz, Mat.AIR);
        c.box(ox - 1, floorY + 8, oz + hz, ox + 1, floorY + 11, oz + hz + 7, Mat.AIR);
        c.box(ox - 2, floorY + 7, oz - hz - 7, ox + 2, floorY + 7, oz - hz + 30, Mat.DOUBLE_SLAB);

        // ===== OCEAN SPIRES / MONOLITHIC COLUMNS RISING FROM WATER =====
        // Massive columns rising from the flooded floor up through the hall and beyond
        long hsp = H.h(hc, 80);
        int spireCount = H.range(H.h(hsp, 1), 3, 8);
        for (int q = 0; q < spireCount; q++) {
            long hq = H.h(hsp, 100 + q);
            int sx = ox + H.range(H.h(hq, 1), -hx + 20, hx - 20);
            int sz = oz + H.range(H.h(hq, 2), -hz + 20, hz - 20);
            int sr = H.range(H.h(hq, 3), 8, 24);
            int sTop = floorY + hh + H.range(H.h(hq, 4), 40, 200);
            // Tapered monolithic column
            c.taper(sx + 0.5, sz + 0.5, sr, Math.max(2, sr / 3), floorY - 4, sTop, 8, Mat.S_COLUMN);
            // Oxidised copper bands
            for (int y = floorY + H.range(H.h(hq, 5), 0, 15); y < sTop; y += H.range(H.h(hq, 6), 20, 40)) {
                if (c.hitsY(y - 2, y + 2)) {
                    c.cylinder(sx + 0.5, sz + 0.5, sr + 1, sr - 1, 1, 1, y, y + 1, 0, 0, Mat.COPPER_PATINA);
                }
            }
            // Internal ladder shaft
            c.box(sx - 1, floorY, sz - 1, sx + 1, sTop, sz + 1, Mat.AIR);
            c.box(sx, floorY + 1, sz, sx, sTop - 2, sz, Mat.LADDER_N);
            // Platform at top with beacon
            c.box(sx - sr - 2, sTop - 1, sz - sr - 2, sx + sr + 2, sTop + 1, sz + sr + 2, Mat.S_DARK);
            c.box(sx - sr, sTop, sz - sr, sx + sr, sTop, sz + sr, Mat.DOUBLE_SLAB);
            c.set(sx, sTop + 1, sz, Mat.LAMP_RED);
            c.set(sx, sTop + 2, sz, Mat.LAMP_AMBER);
            // Cable stays from spire to ceiling
            int ex = ox + H.range(H.h(hq, 7), -hx, hx);
            int ez = oz + H.range(H.h(hq, 8), -hz, hz);
            c.beam(sx, sTop, sz, ex, floorY + hh, ez, 1, Mat.CABLE);
        }

        // Fallen rib debris in the water
        long hdr = H.h(hc, 90);
        if (H.chance(hdr, 0.5)) {
            int count = H.range(H.h(hdr, 1), 8, 20);
            for (int q = 0; q < count; q++) {
                long hq = H.h(hdr, 100 + q);
                int dx = ox + H.range(H.h(hq, 1), -hx + 10, hx - 10);
                int dz = oz + H.range(H.h(hq, 2), -hz + 10, hz - 10);
                int dy = floorY + H.range(H.h(hq, 3), 0, 6);
                int w = H.range(H.h(hq, 4), 6, 20);
                int d2 = H.range(H.h(hq, 5), 6, 20);
                c.box(dx - w/2, dy, dz - d2/2, dx + w/2, dy + 2, dz + d2/2, Mat.S_DARK);
                for (int x = dx - w/2 + 1; x < dx + w/2; x += 3)
                    c.set(x, dy + 2, dz, Mat.REBAR);
            }
        }
    }
}

// ========================================================================
// SAFEGUARD BEACONS: terminal nodes scattered at structural nodes
// ========================================================================

final class Beacons {
    private final Megastructure m;

    Beacons(Megastructure m) {
        this.m = m;
    }

    void draw(Cube c) {
        // Beacons at megashaft centers
        int cx0 = Math.floorDiv(c.x0 + Megastructure.CELL / 2, Megastructure.CELL);
        int cz0 = Math.floorDiv(c.z0 + Megastructure.CELL / 2, Megastructure.CELL);
        for (int cx = cx0 - 1; cx <= cx0 + 1; cx++)
            for (int cz = cz0 - 1; cz <= cz0 + 1; cz++)
                if (cx != 0 || cz != 0)
                    drawShaftBeacons(c, cx, cz);

        // Beacons at wall intersections
        int i0 = m.bayX(c.x0 - Megastructure.MARGIN_XZ), i1 = m.bayX(c.x0 + 15 + Megastructure.MARGIN_XZ);
        int k0 = m.bayZ(c.z0 - Megastructure.MARGIN_XZ), k1 = m.bayZ(c.z0 + 15 + Megastructure.MARGIN_XZ);
        for (int i = i0; i <= i1; i++)
            for (int k = k0; k <= k1; k++)
                drawWallBeacons(c, i, k);
    }

    private void drawShaftBeacons(Cube c, int cx, int cz) {
        long hc = H.h(m.seed, 600, cx, cz);
        double r = H.u(H.h(hc, 1));
        if (r >= 0.55) return;
        int R = H.range(H.h(hc, 2), 60, 200);
        int lim = Megastructure.CELL / 2 - R - 9 - 24;
        int ox = cx * Megastructure.CELL + H.range(H.h(hc, 4), -lim, lim);
        int oz = cz * Megastructure.CELL + H.range(H.h(hc, 5), -lim, lim);
        int ja = H.range(H.h(hc, 6), -14, 3);
        int jb = H.chance(H.h(hc, 10), 0.5) ? Megastructure.TOP_TIER : Math.min(Megastructure.TOP_TIER, ja + H.range(H.h(hc, 7), 4, 20));
        int ya = m.tierY(ja), yb = jb == Megastructure.TOP_TIER ? m.tierY(Megastructure.TOP_TIER) + 60 : m.tierY(jb);
        double ccx = ox + 0.5, ccz = oz + 0.5;
        if (!c.hits(ox - R - 9, ya - 12, oz - R - 9, ox + R + 9, yb + 4, oz + R + 9)) return;

        for (int j = ja + 1; j < jb; j++) {
            long hj = H.h(hc, 30, j);
            if (!H.chance(hj, 0.3)) continue;
            int y = m.tierY(j) + H.range(H.h(hj, 1), -10, 30);
            if (!c.hitsY(y - 4, y + 4)) continue;
            for (int q = 0; q < 4; q++) {
                double a = q * Math.PI / 2 + H.u(H.h(hj, 2)) * 0.3;
                int bx = (int) Math.floor(ccx + Math.cos(a) * (R - 5));
                int bz = (int) Math.floor(ccz + Math.sin(a) * (R - 5));
                drawBeacon(c, bx, y, bz, hj, q);
            }
        }
    }

    private void drawWallBeacons(Cube c, int i, int k) {
        int bx0 = m.lineX(i), bz0 = m.lineZ(k);
        int j0 = m.tierOf(c.y0 - Megastructure.MARGIN_Y), j1 = m.tierOf(c.y0 + 15 + Megastructure.MARGIN_Y);
        for (int j = j0; j <= j1; j++) {
            long hn = H.h(m.seed, 200, i, k);
            if (!H.chance(hn, 0.15)) continue;
            int ty = m.tierY(j);
            int deckTop = ty + H.range(H.h(m.seed, 300, i, k, j), -10, 10) * 2;
            if (m.hasColumn(i, k, j) && c.hits(bx0 - 6, deckTop - 5, bz0 - 6, bx0 + 6, deckTop + 8, bz0 + 6))
                drawBeacon(c, bx0, deckTop + 2, bz0, H.h(m.seed, 202, i, k, j), 0);
        }
    }

    private void drawBeacon(Cube c, int x, int y, int z, long h, int variant) {
        c.box(x - 1, y - 1, z - 1, x + 1, y, z + 1, Mat.S_DARK);
        c.set(x, y + 1, z, Mat.S_DARK);
        byte lamp = (variant & 3) == 0 ? Mat.LAMP_RED : (variant & 3) == 1 ? Mat.LAMP_AMBER : Mat.LAMP_GREEN;
        c.set(x, y + 2, z, lamp);
        for (int dy = 3; dy <= 7; dy++) {
            c.set(x, y + dy, z, (dy & 1) == 0 ? Mat.CABLE : Mat.S_DARK);
        }
        for (int d = 0; d < 4; d++) {
            int dx = d == 0 ? 3 : d == 1 ? -3 : 0;
            int dz = d == 2 ? 3 : d == 3 ? -3 : 0;
            if (c.hits(x + dx - 1, y, z + dz - 1, x + dx + 1, y + 2, z + dz + 1)) {
                c.set(x + dx, y + 1, z + dz, Mat.LAMP_AMBER);
                c.set(x + dx, y + 2, z + dz, Mat.LAMP_GREEN);
            }
        }
    }
}

// ========================================================================
// X-ORIENTATION PASSAGEWAYS / TRANSIT TUBES (diagonal, multi-directional)
// ========================================================================
// Integrated into drawLattice/drawBay below

// ========================================================================
// ELEVATORS, VERTICAL SHAFT LIFTS, STAIRWELLS
// ========================================================================
// Integrated into drawColumn

// ========================================================================
// DEBRIS FIELDS, FALLEN RIBS, FRACTURED DECKING
// ========================================================================
// Integrated into drawMass, drawDeck, drawCoarse

// ========================================================================
// OCEAN SPIRES / MONOLITHIC COLUMNS IN BAY
// ========================================================================
// Integrated into Arrival and seaHall

// ========================================================================
// SETTLEMENT RUINS NEAR BEACH
// ========================================================================
// Integrated into Arrival.columns()
