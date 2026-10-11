package blame.blamegen.core;

/**
 * TOHA HEAVY INDUSTRIES: a fixed industrial super-complex, 150 km x 150 km, roofed by the
 * strata ceiling. It sits north of the arrival basin; its south gate is at (CX, Z1).
 *
 * Layout (all pure functions of position and seed, so cubes agree with each other):
 *  - a solid deck (y 0..63) across the whole square, with the lattice below y 0 untouched;
 *  - a 120-thick perimeter wall to y 1960 with a gate in every side, corner bastions, and a
 *    lit TOHA sign above the south gate facing the beach;
 *  - a 156 x 156 grid of plant cells (960 blocks) separated by lit roads. Every cell owns one
 *    district inside a 888 x 888 inner square (36-block margin), so a cube evaluates at most
 *    four cells: furnace stacks, machine halls, tank farms, cooling towers, pipework,
 *    gantry yards, slag canals, container yards. The outer ring of cells stays open as the
 *    perimeter corridor;
 *  - the Central Spire at the exact centre, rising to the strata roof.
 */
public final class Toha {
    public static final int CX = 0, CZ = -90000;
    public static final int HALF = 74880;
    /** Region bounds: X0..X1-1, Z0..Z1-1. */
    public static final int X0 = CX - HALF, X1 = CX + HALF, Z0 = CZ - HALF, Z1 = CZ + HALF;
    static final int CELL = 960, NC = 2 * HALF / CELL; // 156 cells
    static final int M = 36, N = CELL - 2 * M;         // inner square 888
    static final int WALL = 120, WALL_TOP = 1960;
    static final int DECK_TOP = 63, GROUND = 64;
    static final int GATE_HALF = 400, GATE_H = 700;
    static final int TOP_LIMIT = 2075;                 // strata underside is at least ~2080
    static final int SPIRE_TOP = 2040;

    private static final String[][] FONT = {
        {"11111", "00100", "00100", "00100", "00100", "00100", "00100"}, // T
        {"01110", "10001", "10001", "10001", "10001", "10001", "01110"}, // O
        {"10001", "10001", "10001", "11111", "10001", "10001", "10001"}, // H
        {"01110", "10001", "10001", "11111", "10001", "10001", "10001"}  // A
    };

    private static final byte[] BOX_MATS = {
        Mat.S_TOHA_MASS, Mat.TERRA_BLACK, Mat.S_DARK, Mat.S_TOHA_PALE,
        Mat.CONC_GRAY, Mat.ANDESITE, Mat.CONC_BLACK, Mat.S_TOHA_MASS
    };
    private static final byte[] PIPE_MATS = {Mat.CABLE, Mat.S_DARK, Mat.ANDESITE, Mat.S_TOHA_MASS};

    private final long seed;

    Toha(Megastructure m) {
        this.seed = m.seed ^ 0x70A4EA5C0DEL;
    }

    /** Does this cube touch the complex footprint? */
    boolean overlaps(Cube c) {
        return c.x0 + 15 >= X0 && c.x0 < X1 && c.z0 + 15 >= Z0 && c.z0 < Z1;
    }

    void draw(Cube c) {
        if (c.y0 > TOP_LIMIT) return;
        deck(c);
        wall(c);
        spire(c);
        if (!c.hitsY(GROUND, TOP_LIMIT)) return;
        roads(c);
        portals(c);
        int i0 = Math.floorDiv(c.x0 - X0, CELL), i1 = Math.floorDiv(c.x0 + 15 - X0, CELL);
        int j0 = Math.floorDiv(c.z0 - Z0, CELL), j1 = Math.floorDiv(c.z0 + 15 - Z0, CELL);
        for (int j = j0; j <= j1; j++)
            for (int i = i0; i <= i1; i++) cell(c, i, j);
    }

    // ------------------------------------------------------------------ deck and roads

    private void deck(Cube c) {
        if (!c.hitsY(0, DECK_TOP)) return;
        c.box(Math.max(c.x0, X0), 0, Math.max(c.z0, Z0), Math.min(c.x0 + 15, X1 - 1), DECK_TOP, Math.min(c.z0 + 15, Z1 - 1), Mat.S_TOHA_DECK);
    }

    private void roads(Cube c) {
        if (!c.hitsY(DECK_TOP, 96)) return;
        boolean surface = c.hitsY(DECK_TOP, DECK_TOP);
        for (int z = c.z0; z < c.z0 + 16; z++) {
            if (z < Z0 || z >= Z1) continue;
            int lz = Math.floorMod(z - Z0, CELL);
            boolean rz = lz < 28 || lz >= CELL - 28;
            for (int x = c.x0; x < c.x0 + 16; x++) {
                if (x < X0 || x >= X1) continue;
                int lx = Math.floorMod(x - X0, CELL);
                boolean rx = lx < 28 || lx >= CELL - 28;
                if (!rx && !rz) continue;
                if (surface) {
                    byte m = Mat.S_DARK;
                    // dashed centre lines along each road
                    if (rx && !rz && (lx == 0 || lx == CELL - 1) && ((lz >> 3) & 1) == 0) m = Mat.CONC_LGRAY;
                    if (rz && !rx && (lz == 0 || lz == CELL - 1) && ((lx >> 3) & 1) == 0) m = Mat.CONC_LGRAY;
                    c.set(x, DECK_TOP, z, m);
                }
                // light poles at 96-block pitch on the road centre lines (not in intersections)
                boolean pole = false;
                int pitch = 0;
                if (rx && !rz && (lx == 0 || lx == CELL - 1) && Math.floorMod(lz, 96) < 2) { pole = true; pitch = Math.floorDiv(lz, 96); }
                if (rz && !rx && (lz == 0 || lz == CELL - 1) && Math.floorMod(lx, 96) < 2) { pole = true; pitch = Math.floorDiv(lx, 96); }
                if (pole) {
                    for (int y = GROUND; y <= 87; y++) c.set(x, y, z, Mat.ANDESITE);
                    byte lamp = (pitch & 1) == 0 ? Mat.LAMP_AMBER : Mat.BIO_CYAN;
                    for (int y = 88; y <= 90; y++) c.set(x, y, z, lamp);
                }
            }
        }
    }

    // ------------------------------------------------------------------ perimeter wall and sign

    private void wall(Cube c) {
        boolean near = c.x0 < X0 + WALL || c.x0 + 15 >= X1 - WALL || c.z0 < Z0 + WALL || c.z0 + 15 >= Z1 - WALL;
        // corner bastions reach 590 blocks in
        boolean bast = c.x0 < X0 + 600 && c.z0 < Z0 + 600 || c.x0 + 15 >= X1 - 600 && c.z0 < Z0 + 600
            || c.x0 < X0 + 600 && c.z0 + 15 >= Z1 - 600 || c.x0 + 15 >= X1 - 600 && c.z0 + 15 >= Z1 - 600;
        if (!near && !bast) return;
        if (near) {
            byte w = Mat.S_TOHA_PALE;
            c.box(X0, GROUND, Z0, X0 + WALL - 1, WALL_TOP, Z1 - 1, w);
            c.box(X1 - WALL, GROUND, Z0, X1 - 1, WALL_TOP, Z1 - 1, w);
            c.box(X0, GROUND, Z0, X1 - 1, WALL_TOP, Z0 + WALL - 1, w);
            c.box(X0, GROUND, Z1 - WALL, X1 - 1, WALL_TOP, Z1 - 1, w);
            // vertical grooves every 40 blocks on both faces of every wall
            for (int x = c.x0; x < c.x0 + 16; x++) {
                if (Math.floorMod(x - X0, 40) >= 4 || x < X0 || x >= X1) continue;
                c.box(x, 200, Z0, x, 1900, Z0 + 2, Mat.AIR);
                c.box(x, 200, Z0 + WALL - 3, x, 1900, Z0 + WALL - 1, Mat.AIR);
                c.box(x, 200, Z1 - WALL, x, 1900, Z1 - WALL + 2, Mat.AIR);
                c.box(x, 200, Z1 - 3, x, 1900, Z1 - 1, Mat.AIR);
            }
            for (int z = c.z0; z < c.z0 + 16; z++) {
                if (Math.floorMod(z - Z0, 40) >= 4 || z < Z0 || z >= Z1) continue;
                c.box(X0, 200, z, X0 + 2, 1900, z, Mat.AIR);
                c.box(X0 + WALL - 3, 200, z, X0 + WALL - 1, 1900, z, Mat.AIR);
                c.box(X1 - WALL, 200, z, X1 - WALL + 2, 1900, z, Mat.AIR);
                c.box(X1 - 3, 200, z, X1 - 1, 1900, z, Mat.AIR);
            }
            // lit strips along the inside of the wall, every 160 blocks of height
            for (int y = 320; y <= 1920; y += 320) {
                if (!c.hitsY(y, y)) continue;
                byte lamp = (y / 320 & 1) == 0 ? Mat.LAMP_AMBER : Mat.BIO_CYAN;
                c.box(X0 + WALL, y, Z0 + WALL - 1, X1 - WALL - 1, y, Z0 + WALL - 1, lamp);
                c.box(X0 + WALL, y, Z1 - WALL, X1 - WALL - 1, y, Z1 - WALL, lamp);
                c.box(X0 + WALL - 1, y, Z0 + WALL, X0 + WALL - 1, y, Z1 - WALL - 1, lamp);
                c.box(X1 - WALL, y, Z0 + WALL, X1 - WALL, y, Z1 - WALL - 1, lamp);
            }
            gates(c);
            sign(c);
        }
        // bastions
        int bx = X0 + 300, bX = X1 - 301, bz = Z0 + 300, bZ = Z1 - 301;
        bastion(c, bx, bz);
        bastion(c, bX, bz);
        bastion(c, bx, bZ);
        bastion(c, bX, bZ);
    }

    private void bastion(Cube c, int x, int z) {
        if (!c.hits(x - 300, GROUND, z - 300, x + 300, TOP_LIMIT, z + 300)) return;
        double cx = x + 0.5, cz = z + 0.5;
        c.cylinder(cx, cz, 290, 0, 1, 1, GROUND, SPIRE_TOP, 0, 0, Mat.S_TOHA_TANK);
        for (int y = 200; y < SPIRE_TOP; y += 160) {
            if (!c.hitsY(y, y + 1)) continue;
            c.cylinder(cx, cz, 294, 289, 1, 1, y, y + 1, 0, 0, (y / 160 & 1) == 0 ? Mat.LAMP_RED : Mat.LAMP_AMBER);
        }
    }

    private void gates(Cube c) {
        // tunnels
        c.box(CX - GATE_HALF, GROUND, Z0, CX + GATE_HALF, GROUND + GATE_H, Z0 + WALL - 1, Mat.AIR);
        c.box(CX - GATE_HALF, GROUND, Z1 - WALL, CX + GATE_HALF, GROUND + GATE_H, Z1 - 1, Mat.AIR);
        c.box(X0, GROUND, CZ - GATE_HALF, X0 + WALL - 1, GROUND + GATE_H, CZ + GATE_HALF, Mat.AIR);
        c.box(X1 - WALL, GROUND, CZ - GATE_HALF, X1 - 1, GROUND + GATE_H, CZ + GATE_HALF, Mat.AIR);
        // lit frames on both faces: lintel and two posts
        int top = GROUND + GATE_H;
        for (int zf : new int[]{Z0, Z0 + WALL - 1, Z1 - WALL, Z1 - 1}) {
            c.box(CX - GATE_HALF - 6, top + 1, zf, CX + GATE_HALF + 6, top + 3, zf, Mat.LAMP_AMBER);
            c.box(CX - GATE_HALF - 3, GROUND, zf, CX - GATE_HALF - 1, top + 3, zf, Mat.LAMP_RED);
            c.box(CX + GATE_HALF + 1, GROUND, zf, CX + GATE_HALF + 3, top + 3, zf, Mat.LAMP_RED);
        }
        for (int xf : new int[]{X0, X0 + WALL - 1, X1 - WALL, X1 - 1}) {
            c.box(xf, top + 1, CZ - GATE_HALF - 6, xf, top + 3, CZ + GATE_HALF + 6, Mat.LAMP_AMBER);
            c.box(xf, GROUND, CZ - GATE_HALF - 3, xf, top + 3, CZ - GATE_HALF - 1, Mat.LAMP_RED);
            c.box(xf, GROUND, CZ + GATE_HALF + 1, xf, top + 3, CZ + GATE_HALF + 3, Mat.LAMP_RED);
        }
    }

    /** "TOHA" in 120-block pixels, set into the south wall's outer face above the gate. */
    private void sign(Cube c) {
        final int zf = Z1 - 1, P = 120, y0 = 900;
        if (!c.hits(CX - 1500, 860, zf - 6, CX + 1500, 1780, zf)) return;
        c.box(CX - 1500, 860, zf - 6, CX + 1500, 1780, zf, Mat.CONC_BLACK);
        c.box(CX - 1500, 860, zf, CX + 1500, 867, zf, Mat.LAMP_RED);
        c.box(CX - 1500, 1773, zf, CX + 1500, 1780, zf, Mat.LAMP_RED);
        c.box(CX - 1500, 860, zf, CX - 1493, 1780, zf, Mat.LAMP_RED);
        c.box(CX + 1493, 860, zf, CX + 1500, 1780, zf, Mat.LAMP_RED);
        for (int L = 0; L < 4; L++) {
            int lx0 = CX - 1380 + L * 6 * P;
            if (!c.hits(lx0, y0, zf - 3, lx0 + 5 * P - 1, y0 + 7 * P - 1, zf)) continue;
            for (int row = 0; row < 7; row++)
                for (int col = 0; col < 5; col++) {
                    if (FONT[L][row].charAt(col) != '1') continue;
                    int x = lx0 + col * P, y = y0 + (6 - row) * P;
                    c.box(x, y, zf - 3, x + P - 1, y + P - 1, zf, Mat.LAMP_AMBER);
                }
        }
    }

    // ------------------------------------------------------------------ central spire

    private void spire(Cube c) {
        if (!c.hits(CX - 520, GROUND, CZ - 520, CX + 520, TOP_LIMIT, CZ + 520)) return;
        double cx = CX + 0.5, cz = CZ + 0.5, r0 = 420, r1 = 70;
        int ht = SPIRE_TOP - GROUND;
        c.taper(cx, cz, r0, r1, GROUND, SPIRE_TOP, 32, Mat.S_TOHA_RING);
        c.taper(cx, cz, r0 - 14, r1 - 10, GROUND + 12, SPIRE_TOP - 30, 32, Mat.AIR);
        for (int y = GROUND + 100; y < SPIRE_TOP; y += 100) {
            if (!c.hitsY(y, y)) continue;
            double rr = r0 + (r1 - r0) * (y - GROUND) / (double) ht;
            c.cylinder(cx, cz, rr + 7, rr, 1, 1, y, y, 0, 0, (y / 100 & 1) == 0 ? Mat.LAMP_AMBER : Mat.BIO_CYAN);
        }
        for (int k = 0; k < 50; k++) {
            int y = GROUND + k * 40, L = (int) (500 * (1 - k / 50.0));
            if (L < 30) break;
            if (!c.hitsY(y, y + 39)) continue;
            c.box(CX - 5, y, CZ - L, CX + 5, y + 39, CZ + L, Mat.S_TOHA_MASS);
            c.box(CX - L, y, CZ - 5, CX + L, y + 39, CZ + 5, Mat.S_TOHA_MASS);
            c.box(CX - 5, y + 39, CZ - L, CX + 5, y + 39, CZ - L, Mat.LAMP_RED);
            c.box(CX - 5, y + 39, CZ + L, CX + 5, y + 39, CZ + L, Mat.LAMP_RED);
            c.box(CX - L, y + 39, CZ - 5, CX - L, y + 39, CZ + 5, Mat.LAMP_RED);
            c.box(CX + L, y + 39, CZ - 5, CX + L, y + 39, CZ + 5, Mat.LAMP_RED);
        }
        c.sphere(cx, SPIRE_TOP - 20, cz, 50, 0, Mat.GLOWSTONE);
    }


    // ------------------------------------------------------------------ road portals

    /** Portal frames spanning the lit roads: two piers, a deep girder and a cyan under-strip. */
    private void portals(Cube c) {
        if (!c.hitsY(GROUND, 330)) return;
        final int P = 240;
        for (int pass = 0; pass < 2; pass++) {
            // pass 0: roads that run along Z (centre lines at x = X0 + r*CELL); pass 1: along X.
            int a0 = pass == 0 ? c.x0 : c.z0, a1 = a0 + 15;
            int b0 = pass == 0 ? c.z0 : c.x0;
            int lo = pass == 0 ? X0 : Z0, blo = pass == 0 ? Z0 : X0;
            int r0 = Math.floorDiv(a0 - lo - 34 + CELL - 1, CELL), r1 = Math.floorDiv(a1 - lo + 34, CELL);
            for (int r = Math.max(1, r0); r <= Math.min(NC - 1, r1); r++) {
                int rc = lo + r * CELL;
                if (rc - 34 > a1 || rc + 34 < a0) continue;
                int q0 = Math.floorDiv(b0 - blo - 8, P), q1 = Math.floorDiv(b0 + 15 - blo + 8, P);
                for (int q = q0; q <= q1; q++) {
                    long hh = H.h(seed, 0x9071, pass, r, q);
                    if (!H.chance(hh, 0.55)) continue;
                    int pos = blo + q * P + P / 2;
                    if (pos < blo + WALL + 60 || pos > blo + 2 * HALF - WALL - 60) continue;
                    int ht = 110 + (int) (H.u(H.h(hh, 3)) * 5) * 24;
                    if (pass == 0) {
                        if (!c.hits(rc - 34, GROUND, pos - 6, rc + 34, GROUND + ht + 24, pos + 6)) continue;
                        c.box(rc - 33, GROUND, pos - 3, rc - 29, GROUND + ht, pos + 3, Mat.S_TOHA_MASS);
                        c.box(rc + 29, GROUND, pos - 3, rc + 33, GROUND + ht, pos + 3, Mat.S_TOHA_MASS);
                        c.box(rc - 34, GROUND + ht, pos - 4, rc + 34, GROUND + ht + 12, pos + 4, Mat.S_TOHA_MASS);
                        c.box(rc - 30, GROUND + ht - 1, pos - 1, rc + 30, GROUND + ht - 1, pos + 1, Mat.BIO_CYAN);
                        c.box(rc - 33, GROUND + ht + 13, pos - 1, rc - 29, GROUND + ht + 15, pos + 1, Mat.LAMP_RED);
                        c.box(rc + 29, GROUND + ht + 13, pos - 1, rc + 33, GROUND + ht + 15, pos + 1, Mat.LAMP_RED);
                    } else {
                        if (!c.hits(pos - 6, GROUND, rc - 34, pos + 6, GROUND + ht + 24, rc + 34)) continue;
                        c.box(pos - 3, GROUND, rc - 33, pos + 3, GROUND + ht, rc - 29, Mat.S_TOHA_MASS);
                        c.box(pos - 3, GROUND, rc + 29, pos + 3, GROUND + ht, rc + 33, Mat.S_TOHA_MASS);
                        c.box(pos - 4, GROUND + ht, rc - 34, pos + 4, GROUND + ht + 12, rc + 34, Mat.S_TOHA_MASS);
                        c.box(pos - 1, GROUND + ht - 1, rc - 30, pos + 1, GROUND + ht - 1, rc + 30, Mat.BIO_CYAN);
                        c.box(pos - 1, GROUND + ht + 13, rc - 33, pos + 1, GROUND + ht + 15, rc - 29, Mat.LAMP_RED);
                        c.box(pos - 1, GROUND + ht + 13, rc + 29, pos + 1, GROUND + ht + 15, rc + 33, Mat.LAMP_RED);
                    }
                }
            }
        }
    }

    // ---- monolith slabs: the black screentone towers

    private void monoliths(Cube c, int ox, int oz, long h) {
        int n = H.range(H.h(h, 2), 3, 5);
        int slot = N / n;
        boolean alongX = (h & 1) == 0;
        for (int k = 0; k < n; k++) {
            int w = H.range(H.h(h, 10 + k), 70, Math.min(150, slot - 20)), d = H.range(H.h(h, 20 + k), 36, 64);
            int ht = H.range(H.h(h, 30 + k), 850, 1950);
            int a = k * slot + (slot - w) / 2;
            int bpos = H.range(H.h(h, 50 + k), 20, N - d - 20);
            int x0 = alongX ? ox + a : ox + bpos, z0 = alongX ? oz + bpos : oz + a;
            int sx = alongX ? w : d, sz = alongX ? d : w;
            slab(c, x0, z0, x0 + sx - 1, z0 + sz - 1, GROUND + ht, h, k, alongX);
        }
    }

    private void slab(Cube c, int x0, int z0, int x1, int z1, int top, long h, int k, boolean alongX) {
        if (!c.hits(x0 - 2, GROUND, z0 - 2, x1 + 2, top + 40, z1 + 2)) return;
        byte body = (k & 1) == 0 ? Mat.S_TOHA_MASS : Mat.S_TOHA_PALE;
        c.box(x0 - 2, GROUND, z0 - 2, x1 + 2, GROUND + 24, z1 + 2, Mat.S_TOHA_MASS);
        c.box(x0, GROUND + 25, z0, x1, top, z1, body);
        c.box(x0 - 2, top + 1, z0 - 2, x1 + 2, top + 10, z1 + 2, Mat.S_TOHA_MASS);
        c.box(x0 + 3, top + 11, z0 + 3, x1 - 3, top + 24, z1 - 3, Mat.S_TOHA_MASS);
        if (alongX) {
            for (int x = Math.max(c.x0, x0 + 6); x < Math.min(c.x0 + 16, x1 - 6); x++) {
                if (Math.floorMod(x - x0 - 6, 20) >= 3) continue;
                c.box(x, GROUND + 25, z0 - 2, x, top - 20, z0 - 1, body);
                c.box(x, GROUND + 25, z1 + 1, x, top - 20, z1 + 2, body);
            }
        } else {
            for (int z = Math.max(c.z0, z0 + 6); z < Math.min(c.z0 + 16, z1 - 6); z++) {
                if (Math.floorMod(z - z0 - 6, 20) >= 3) continue;
                c.box(x0 - 2, GROUND + 25, z, x0 - 1, top - 20, z, body);
                c.box(x1 + 1, GROUND + 25, z, x1 + 2, top - 20, z, body);
            }
        }
        // sparse lit windows on a 24 x 56 grid: mostly amber, the odd cyan
        for (int wy = GROUND + 120; wy < top - 60; wy += 56) {
            if (!c.hitsY(wy, wy + 11)) continue;
            if (alongX) {
                for (int x = Math.max(c.x0, x0 + 8); x < Math.min(c.x0 + 16, x1 - 12); x++) {
                    if (Math.floorMod(x - x0, 24) >= 6) continue;
                    long hw = H.h(h, 0x77, Math.floorDiv(x - x0, 24) * 131L + k, wy);
                    if (!H.chance(hw, 0.07)) continue;
                    byte m = (hw >> 40 & 7) == 0 ? Mat.BIO_CYAN : Mat.LAMP_AMBER;
                    c.box(x, wy, z0 - 1, x, wy + 11, z0, m);
                    c.box(x, wy, z1, x, wy + 11, z1 + 1, m);
                }
            } else {
                for (int z = Math.max(c.z0, z0 + 8); z < Math.min(c.z0 + 16, z1 - 12); z++) {
                    if (Math.floorMod(z - z0, 24) >= 6) continue;
                    long hw = H.h(h, 0x77, Math.floorDiv(z - z0, 24) * 131L + k, wy);
                    if (!H.chance(hw, 0.07)) continue;
                    byte m = (hw >> 40 & 7) == 0 ? Mat.BIO_CYAN : Mat.LAMP_AMBER;
                    c.box(x0 - 1, wy, z, x0, wy + 11, z, m);
                    c.box(x1, wy, z, x1 + 1, wy + 11, z, m);
                }
            }
        }
        // one deep vertical cyan slit up each long face
        int mid = alongX ? (x0 + x1) / 2 : (z0 + z1) / 2;
        if (alongX) {
            if (mid >= c.x0 && mid < c.x0 + 16) {
                c.box(mid, GROUND + 60, z0 - 1, mid, top - 40, z0, Mat.BIO_CYAN);
                c.box(mid, GROUND + 60, z1, mid, top - 40, z1 + 1, Mat.BIO_CYAN);
            }
        } else if (mid >= c.z0 && mid < c.z0 + 16) {
            c.box(x0 - 1, GROUND + 60, mid, x0, top - 40, mid, Mat.BIO_CYAN);
            c.box(x1, GROUND + 60, mid, x1 + 1, top - 40, mid, Mat.BIO_CYAN);
        }
        int bx = (x0 + x1) / 2, bz = (z0 + z1) / 2;
        c.box(bx - 2, top + 25, bz - 2, bx + 2, top + 34, bz + 2, Mat.LAMP_RED);
    }

    // ------------------------------------------------------------------ plant cells

    private void cell(Cube c, int i, int j) {
        if (i < 1 || j < 1 || i >= NC - 1 || j >= NC - 1) return; // perimeter corridor
        int ox = X0 + i * CELL + M, oz = Z0 + j * CELL + M;
        if (!c.hits(ox, GROUND, oz, ox + N - 1, TOP_LIMIT, oz + N - 1)) return;
        double d = Math.max(Math.abs(i - 77.5), Math.abs(j - 77.5));
        long h = H.h(seed, 0x70A, i, j);
        if (d < 4) { plaza(c, ox, oz); return; }
        clutter(c, ox, oz, h);
        double r = H.u(H.h(h, 1));
        if (r < 0.18) stacks(c, ox, oz, h);
        else if (r < 0.34) hall(c, ox, oz, h);
        else if (r < 0.46) tankFarm(c, ox, oz, h);
        else if (r < 0.55) coolingTowers(c, ox, oz, h);
        else if (r < 0.65) pipework(c, ox, oz, h);
        else if (r < 0.74) gantry(c, ox, oz, h);
        else if (r < 0.80) canal(c, ox, oz, h);
        else if (r < 0.86) yard(c, ox, oz, h);
        else monoliths(c, ox, oz, h);
    }

    /** Forecourt around the spire: a regular grid of fat black obelisks with small lit caps. */
    private void plaza(Cube c, int ox, int oz) {
        final int sp = 192;
        int a0 = Math.max(0, Math.floorDiv(c.x0 - ox - 20, sp)), a1 = Math.min(N / sp - 1, Math.floorDiv(c.x0 + 15 - ox, sp));
        int b0 = Math.max(0, Math.floorDiv(c.z0 - oz - 20, sp)), b1 = Math.min(N / sp - 1, Math.floorDiv(c.z0 + 15 - oz, sp));
        for (int b = b0; b <= b1; b++)
            for (int a = a0; a <= a1; a++) {
                int x = ox + a * sp + 70, z = oz + b * sp + 70;
                int ht = 70 + (int) (H.u(H.h(seed, 0x91A, x, z)) * 150);
                if (!c.hits(x - 2, GROUND, z - 2, x + 15, GROUND + ht + 6, z + 15)) continue;
                c.box(x - 2, GROUND, z - 2, x + 15, GROUND + 6, z + 15, Mat.S_TOHA_MASS);
                c.box(x, GROUND + 7, z, x + 13, GROUND + ht, z + 13, Mat.S_TOHA_MASS);
                c.box(x + 5, GROUND + ht + 1, z + 5, x + 8, GROUND + ht + 3, z + 8, ((a + b) & 3) == 0 ? Mat.LAMP_AMBER : Mat.BIO_CYAN);
            }
    }

    // ---- ground clutter: low machinery blocks that stop the deck reading as an empty plane

    private void clutter(Cube c, int ox, int oz, long h) {
        if (!c.hitsY(GROUND, GROUND + 44)) return;
        final int sp = 56;
        int a0 = Math.max(0, Math.floorDiv(c.x0 - ox - 40, sp)), a1 = Math.min(N / sp - 1, Math.floorDiv(c.x0 + 15 - ox, sp));
        int b0 = Math.max(0, Math.floorDiv(c.z0 - oz - 40, sp)), b1 = Math.min(N / sp - 1, Math.floorDiv(c.z0 + 15 - oz, sp));
        for (int b = b0; b <= b1; b++)
            for (int a = a0; a <= a1; a++) {
                long hs = H.h(h, 0xC1, a, b);
                if (!H.chance(hs, 0.32)) continue;
                int w = H.range(H.h(hs, 1), 14, 38), d = H.range(H.h(hs, 2), 14, 38), ht = H.range(H.h(hs, 3), 6, 38);
                int x0 = ox + a * sp + H.range(H.h(hs, 4), 0, sp - w), z0 = oz + b * sp + H.range(H.h(hs, 5), 0, sp - d);
                if (!c.hits(x0, GROUND, z0, x0 + w, GROUND + ht + 8, z0 + d)) continue;
                byte m = BOX_MATS[(int) (H.h(hs, 6) & 7)];
                c.box(x0, GROUND, z0, x0 + w - 1, GROUND + ht, z0 + d - 1, m);
                int kind = (int) (H.h(hs, 7) & 7);
                if (kind == 0) c.box(x0 + w / 2 - 2, GROUND + ht + 1, z0 + d / 2 - 2, x0 + w / 2 + 2, GROUND + ht + 7, z0 + d / 2 + 2, Mat.VENT);
                else if (kind == 1) c.box(x0 + 2, GROUND + ht + 1, z0 + 2, x0 + 3, GROUND + ht + 2, z0 + 3, Mat.LAMP_AMBER);
                else if (kind == 2) c.box(x0, GROUND + ht - 2, z0, x0 + w - 1, GROUND + ht - 1, z0, Mat.BIO_CYAN);
                else if (kind == 3) c.box(x0 + 3, GROUND + ht + 1, z0 + 3, x0 + w - 4, GROUND + ht + 3, z0 + 5, Mat.CABLE);
            }
    }

    // ---- furnace stacks

    private void stack(Cube c, int sx, int sz, int r, int ht, byte ring) {
        int top = GROUND + ht;
        if (!c.hits(sx - 2 * r, GROUND, sz - 2 * r, sx + 2 * r, top + r, sz + 2 * r)) return;
        double cx = sx + 0.5, cz = sz + 0.5, r1 = r * 0.6;
        c.cylinder(cx, cz, r * 1.9, 0, 1, 1, GROUND, GROUND + 36, 0, 0, Mat.S_DARK);
        c.taper(cx, cz, r, r1, GROUND + 36, top, 24, Mat.S_TOHA_RING);
        c.taper(cx, cz, r - 6, r1 - 6, GROUND + 40, top, 24, Mat.AIR);
        for (int y = GROUND + 60; y < top - 10; y += 96) {
            if (!c.hitsY(y, y)) continue;
            double rr = r + (r1 - r) * (y - GROUND - 36) / (double) (top - GROUND - 36);
            c.cylinder(cx, cz, rr + 3, rr, 1, 1, y, y, 0, 0, ring);
        }
        c.cylinder(cx, cz, r1 - 6, 0, 1, 1, top - 10, top - 4, 0, 0, Mat.GLOWSTONE);
        c.blob(cx, top + r1, cz, r1 * 0.5, r1, r1 * 0.5, Mat.LAMP_AMBER);
    }

    private void stacks(Cube c, int ox, int oz, long h) {
        int n = H.range(H.h(h, 2), 2, 5);
        for (int k = 0; k < n; k++) {
            int r = H.range(H.h(h, 10 + k), 26, 64), ht = H.range(H.h(h, 20 + k), 700, 1850);
            int sx = ox + H.range(H.h(h, 30 + k), 2 * r, N - 2 * r), sz = oz + H.range(H.h(h, 40 + k), 2 * r, N - 2 * r);
            stack(c, sx, sz, r, ht, (k & 1) == 0 ? Mat.LAMP_RED : Mat.LAMP_AMBER);
        }
    }

    // ---- machine hall

    private void hall(Cube c, int ox, int oz, long h) {
        int w = H.range(H.h(h, 10), 520, 860), d = H.range(H.h(h, 11), 280, 520), ht = H.range(H.h(h, 12), 110, 300);
        int x0 = ox + H.range(H.h(h, 13), 0, N - w), z0 = oz + H.range(H.h(h, 14), 0, N - d);
        int x1 = x0 + w, z1 = z0 + d, top = GROUND + ht;
        if (!c.hits(x0, GROUND, z0, x1, top + 16, z1)) return;
        c.box(x0, GROUND, z0, x1, top, z1, Mat.S_TOHA_MASS);
        c.box(x0 + 6, GROUND, z0 + 6, x1 - 6, top - 8, z1 - 6, Mat.AIR);
        for (int x = x0; x <= x1; x += 40) c.box(x, top + 1, z0, x + 1, top + 6, z1, Mat.ANDESITE);
        for (int x = x0 + 60; x < x1 - 60; x += 80) c.box(x, top + 1, z0 + d / 2 - 6, x + 11, top + 12, z0 + d / 2 + 5, Mat.VENT);
        // lit window slots on both long faces
        int wy = GROUND + ht / 2, wh = Math.max(6, ht / 6);
        if (c.hits(x0, wy, z0, x1, wy + wh, z0 + 5) || c.hits(x0, wy, z1 - 5, x1, wy + wh, z1))
            for (int x = x0 + 16; x < x1 - 24; x += 32) {
                c.box(x, wy, z0, x + 15, wy + wh, z0 + 5, Mat.LAMP_AMBER);
                c.box(x, wy, z1 - 5, x + 15, wy + wh, z1, Mat.LAMP_AMBER);
            }
        // doors
        c.box(x0 + w / 2 - 40, GROUND, z0, x0 + w / 2 + 40, GROUND + 60, z0 + 5, Mat.AIR);
        c.box(x0 + w / 2 - 40, GROUND, z1 - 5, x0 + w / 2 + 40, GROUND + 60, z1, Mat.AIR);
        // interior: column grid, molten channel, crane rails
        if (c.hits(x0 + 6, GROUND, z0 + 6, x1 - 6, top - 8, z1 - 6)) {
            for (int x = x0 + 64; x < x1 - 20; x += 64)
                for (int z = z0 + 64; z < z1 - 20; z += 64)
                    c.box(x, GROUND, z, x + 3, top - 8, z + 3, Mat.ANDESITE);
            int zc = z0 + d / 2;
            c.box(x0 + 20, GROUND, zc - 1, x1 - 20, GROUND, zc + 2, Mat.GLOWSTONE);
            int rail = top - 30;
            c.box(x0 + 6, rail, z0 + 20, x1 - 6, rail + 1, z0 + 22, Mat.ANDESITE);
            c.box(x0 + 6, rail, z1 - 22, x1 - 6, rail + 1, z1 - 20, Mat.ANDESITE);
            int cx = x0 + H.range(H.h(h, 15), 40, w - 40);
            c.box(cx, rail - 2, z0 + 20, cx + 5, rail, z1 - 20, Mat.S_DECK);
            c.box(cx + 1, rail - 8, zc - 2, cx + 3, rail - 3, zc + 2, Mat.LAMP_RED);
        }
    }

    // ---- tank farm

    private void tankFarm(Cube c, int ox, int oz, long h) {
        int nx = H.range(H.h(h, 10), 4, 6), nz = H.range(H.h(h, 11), 4, 6), sp = 148;
        int sx0 = ox + (N - (nx - 1) * sp) / 2, sz0 = oz + (N - (nz - 1) * sp) / 2;
        for (int b = 0; b < nz; b++)
            for (int a = 0; a < nx; a++) {
                long ht0 = H.h(h, 100 + a, b);
                int r = H.range(H.h(ht0, 1), 22, 34), ht = H.range(H.h(ht0, 2), 60, 170);
                int tx = sx0 + a * sp, tz = sz0 + b * sp, top = GROUND + ht;
                if (!c.hits(tx - r - 12, GROUND, tz - r - 12, tx + r + sp, top + r, tz + r + sp)) continue;
                double cx = tx + 0.5, cz = tz + 0.5;
                c.cylinder(cx, cz, r, r - 3, 1, 1, GROUND, top, 0, 0, Mat.S_TOHA_TANK);
                c.cylinder(cx, cz, r, 0, 1, 1, top, top + 2, 0, 0, Mat.S_TOHA_TANK);
                c.blob(cx, top + 2, cz, r * 0.8, r * 0.3, r * 0.8, Mat.S_TOHA_TANK);
                c.cylinder(cx, cz, r + 3, r, 1, 1, top - 4, top - 4, 0, 0, Mat.LAMP_AMBER);
                c.cylinder(cx, cz, r + 6, r + 4, 1, 1, GROUND + ht / 2, GROUND + ht / 2, 0, 0, Mat.S_DECK);
                if (a + 1 < nx) c.box(tx + r, GROUND + 10, tz - 1, tx + sp - r, GROUND + 13, tz + 2, Mat.CABLE);
                if (b + 1 < nz) c.box(tx - 1, GROUND + 18, tz + r, tx + 2, GROUND + 21, tz + sp - r, Mat.S_DARK);
            }
    }

    // ---- cooling towers

    private void coolingTowers(Cube c, int ox, int oz, long h) {
        for (int k = 0; k < 2; k++) {
            int rw = H.range(H.h(h, 10 + k), 90, 115), ht = H.range(H.h(h, 20 + k), 400, 700);
            int tx = ox + 222 + k * 444, tz = oz + H.range(H.h(h, 30 + k), 230, N - 230);
            int top = GROUND + ht;
            if (!c.hits(tx - 215, GROUND, tz - 215, tx + 215, top + 2, tz + 215)) continue;
            double cx = tx + 0.5, cz = tz + 0.5, yw = ht * 0.7, kk = ht * 0.35;
            for (int y = 0; y < ht; y += 20) {
                if (!c.hitsY(GROUND + y, GROUND + y + 19)) continue;
                double t = (y + 10 - yw) / kk, r = rw * 0.82 * Math.sqrt(1 + t * t);
                c.cylinder(cx, cz, r, r - 5, 1, 1, GROUND + y, GROUND + y + 19, 0, 0, Mat.S_TOHA_TANK);
            }
            c.cylinder(cx, cz, rw * 0.82 * Math.sqrt(1 + (ht - yw) * (ht - yw) / (kk * kk)) + 2, rw * 0.8, 1, 1, top - 1, top, 0, 0, Mat.LAMP_RED);
            c.cylinder(cx, cz, rw * 1.2, 0, 1, 1, GROUND, GROUND + 2, 0, 0, Mat.LAMP_AMBER);
            // base arches
            for (int s = 0; s < 4; s++) {
                double ang = s * Math.PI / 2;
                int ax = (int) Math.round(tx + Math.cos(ang) * rw * 1.8), az = (int) Math.round(tz + Math.sin(ang) * rw * 1.8);
                c.box(ax - 12, GROUND, az - 12, ax + 12, GROUND + 40, az + 12, Mat.AIR);
            }
        }
    }

    // ---- pipework and refinery columns

    private void pipework(Cube c, int ox, int oz, long h) {
        int rows = H.range(H.h(h, 20), 3, 5);
        int zc = oz + H.range(H.h(h, 21), 120, N - 200), xc = ox + H.range(H.h(h, 22), 120, N - 200);
        int hgt = GROUND + 28 + rows * 9 + 4;
        if (c.hits(ox + 24, GROUND, zc, ox + N - 25, hgt, zc + rows * 6 + 3)) {
            for (int r = 0; r < rows; r++) {
                byte m = PIPE_MATS[(int) (H.h(h, 23 + r) & 3)];
                int y = GROUND + 28 + r * 9, z = zc + r * 6;
                c.box(ox + 24, y, z, ox + N - 25, y + 3, z + 3, m);
                for (int x = ox + 24; x < ox + N - 30; x += 96) c.box(x, y - 1, z - 1, x + 1, y + 4, z + 4, Mat.ANDESITE);
            }
            for (int x = ox + 24; x < ox + N - 30; x += 48) c.box(x, GROUND, zc, x + 3, hgt, zc + rows * 6 + 3, Mat.ANDESITE);
        }
        if (c.hits(xc, GROUND, oz + 24, xc + rows * 6 + 3, hgt, oz + N - 25)) {
            for (int r = 0; r < rows; r++) {
                byte m = PIPE_MATS[(int) (H.h(h, 33 + r) & 3)];
                int y = GROUND + 40 + r * 9, x = xc + r * 6;
                c.box(x, y, oz + 24, x + 3, y + 3, oz + N - 25, m);
            }
            for (int z = oz + 24; z < oz + N - 30; z += 48) c.box(xc, GROUND, z, xc + rows * 6 + 3, hgt + 12, z + 3, Mat.ANDESITE);
        }
        int n = H.range(H.h(h, 40), 3, 6);
        for (int k = 0; k < n; k++) {
            int r = H.range(H.h(h, 50 + k), 12, 20), ht = H.range(H.h(h, 60 + k), 150, 520);
            int px = ox + H.range(H.h(h, 70 + k), 40, N - 40), pz = oz + H.range(H.h(h, 80 + k), 40, N - 40);
            int top = GROUND + ht;
            if (!c.hits(px - r - 10, GROUND, pz - r - 10, px + r + 10, top + 12, pz + r + 10)) continue;
            double cx = px + 0.5, cz = pz + 0.5;
            c.cylinder(cx, cz, r, r - 3, 1, 1, GROUND, top, 0, 0, Mat.ANDESITE);
            c.cylinder(cx, cz, r, 0, 1, 1, top, top + 1, 0, 0, Mat.ANDESITE);
            for (int y = GROUND + 30; y < top; y += 40) {
                if (!c.hitsY(y, y + 1)) continue;
                c.cylinder(cx, cz, r + 7, r, 1, 1, y, y, 0, 0, Mat.S_DECK);
                c.cylinder(cx, cz, r + 8, r + 7, 1, 1, y + 1, y + 1, 0, 0, Mat.IRON_BARS);
            }
            c.blob(cx, top + 6, cz, 5, 6, 5, Mat.LAMP_RED);
        }
    }

    // ---- gantry yard

    private void gantry(Cube c, int ox, int oz, long h) {
        int[] ts = new int[3];
        for (int a = 0; a < 3; a++) ts[a] = N / 6 + a * (N / 3);
        int[][] hts = new int[3][3];
        for (int b = 0; b < 3; b++)
            for (int a = 0; a < 3; a++) hts[b][a] = H.range(H.h(h, 30 + a * 3 + b), 500, 1100);
        for (int b = 0; b < 3; b++)
            for (int a = 0; a < 3; a++) {
                int tx = ox + ts[a], tz = oz + ts[b], ht = hts[b][a], top = GROUND + ht;
                // tower
                if (c.hits(tx - 16, GROUND, tz - 16, tx + 16, top + 2, tz + 16)) {
                    for (int s = 0; s < 4; s++) {
                        int px = tx + ((s & 1) == 0 ? -14 : 12), pz = tz + ((s & 2) == 0 ? -14 : 12);
                        c.box(px, GROUND, pz, px + 2, top, pz + 2, Mat.ANDESITE);
                    }
                    for (int y = GROUND; y <= top; y += 48) {
                        if (!c.hitsY(y, y + 1)) continue;
                        c.box(tx - 14, y, tz - 14, tx + 13, y + 1, tz - 12, Mat.ANDESITE);
                        c.box(tx - 14, y, tz + 12, tx + 13, y + 1, tz + 14, Mat.ANDESITE);
                        c.box(tx - 14, y, tz - 14, tx - 12, y + 1, tz + 13, Mat.ANDESITE);
                        c.box(tx + 12, y, tz - 14, tx + 14, y + 1, tz + 13, Mat.ANDESITE);
                    }
                    for (int y = GROUND; y + 96 <= top; y += 96) {
                        if (!c.hitsY(y, y + 96)) continue;
                        double lo = (y / 96 & 1) == 0 ? -14 : 14;
                        c.beam(tx + lo, y, tz - 13, tx - lo, y + 96, tz - 13, 1.2, Mat.ANDESITE);
                        c.beam(tx + lo, y, tz + 13, tx - lo, y + 96, tz + 13, 1.2, Mat.ANDESITE);
                        c.beam(tx - 13, y, tz + lo, tx - 13, y + 96, tz - lo, 1.2, Mat.ANDESITE);
                        c.beam(tx + 13, y, tz + lo, tx + 13, y + 96, tz - lo, 1.2, Mat.ANDESITE);
                    }
                    c.box(tx - 16, top, tz - 16, tx + 16, top + 2, tz + 16, Mat.S_DECK);
                    c.box(tx - 15, top + 3, tz - 15, tx - 13, top + 6, tz - 13, Mat.LAMP_RED);
                    c.box(tx + 13, top + 3, tz + 13, tx + 15, top + 6, tz + 15, Mat.LAMP_RED);
                }
                // bridges to the next tower in +x and +z
                if (a < 2) {
                    int yb = GROUND + H.range(H.h(h, 60 + a * 3 + b), 150, 450), x1 = tx + 14, x2 = ox + ts[a + 1] - 14;
                    if (c.hits(x1, yb - 1, tz - 4, x2, yb + 3, tz + 4)) {
                        c.box(x1, yb, tz - 4, x2, yb + 1, tz + 4, Mat.S_DECK);
                        c.box(x1, yb + 2, tz - 4, x2, yb + 3, tz - 4, Mat.IRON_BARS);
                        c.box(x1, yb + 2, tz + 4, x2, yb + 3, tz + 4, Mat.IRON_BARS);
                        c.box(x1, yb - 1, tz, x2, yb - 1, tz, Mat.LAMP_AMBER);
                    }
                }
                if (b < 2) {
                    int yb = GROUND + H.range(H.h(h, 70 + a * 3 + b), 150, 450), z1 = tz + 14, z2 = oz + ts[b + 1] - 14;
                    if (c.hits(tx - 4, yb - 1, z1, tx + 4, yb + 3, z2)) {
                        c.box(tx - 4, yb, z1, tx + 4, yb + 1, z2, Mat.S_DECK);
                        c.box(tx - 4, yb + 2, z1, tx - 4, yb + 3, z2, Mat.IRON_BARS);
                        c.box(tx + 4, yb + 2, z1, tx + 4, yb + 3, z2, Mat.IRON_BARS);
                        c.box(tx, yb - 1, z1, tx, yb - 1, z2, Mat.LAMP_AMBER);
                    }
                }
            }
    }

    // ---- slag canal

    private void canal(Cube c, int ox, int oz, long h) {
        int cz = oz + H.range(H.h(h, 10), 280, 560);
        if (c.hits(ox, 24, cz - 51, ox + N - 1, DECK_TOP, cz + 51)) {
            c.box(ox, 24, cz - 50, ox + N - 1, DECK_TOP, cz + 50, Mat.AIR);
            c.box(ox, 24, cz - 50, ox + N - 1, 58, cz + 50, Mat.WATER);
            for (int x = ox; x < ox + N; x += 16) {
                c.set(x, DECK_TOP, cz - 51, Mat.LAMP_AMBER);
                c.set(x, DECK_TOP, cz + 51, Mat.LAMP_AMBER);
            }
            for (int x = ox + 110; x < ox + N - 20; x += 220) {
                c.box(x, 62, cz - 51, x + 13, 63, cz + 51, Mat.S_DECK);
                c.box(x, 64, cz - 51, x, 66, cz + 51, Mat.IRON_BARS);
                c.box(x + 13, 64, cz - 51, x + 13, 66, cz + 51, Mat.IRON_BARS);
            }
        }
        stack(c, ox + 180, cz - 160, 56, H.range(H.h(h, 11), 280, 420), Mat.LAMP_RED);
        stack(c, ox + N - 200, cz + 170, 62, H.range(H.h(h, 12), 280, 420), Mat.LAMP_AMBER);
    }

    // ---- container yard

    private void yard(Cube c, int ox, int oz, long h) {
        final int px = 32, pz = 16;
        int a0 = Math.max(0, Math.floorDiv(c.x0 - ox, px)), a1 = Math.min(N / px - 1, Math.floorDiv(c.x0 + 15 - ox, px));
        int b0 = Math.max(0, Math.floorDiv(c.z0 - oz, pz)), b1 = Math.min(N / pz - 1, Math.floorDiv(c.z0 + 15 - oz, pz));
        for (int b = b0; b <= b1; b++) {
            if (b % 5 == 4) { // rail aisle
                c.box(ox, GROUND, oz + b * pz + 3, ox + N - 1, GROUND, oz + b * pz + 3, Mat.ANDESITE);
                c.box(ox, GROUND, oz + b * pz + 8, ox + N - 1, GROUND, oz + b * pz + 8, Mat.ANDESITE);
                continue;
            }
            for (int a = a0; a <= a1; a++) {
                long hs = H.h(h, 50, a, b);
                if (!H.chance(H.h(hs, 1), 0.72)) continue;
                int lv = H.range(H.h(hs, 2), 1, 4);
                byte m = BOX_MATS[(int) (H.h(hs, 3) & 7)];
                c.box(ox + a * px, GROUND, oz + b * pz, ox + a * px + 27, GROUND + lv * 10 - 1, oz + b * pz + 11, m);
            }
        }
        for (int k = 0; k < 4; k++) {
            int mx = ox + H.range(H.h(h, 70 + k), 20, N - 20), mz = oz + H.range(H.h(h, 80 + k), 20, N - 20);
            if (!c.hits(mx - 3, GROUND, mz - 3, mx + 5, GROUND + 175, mz + 5)) continue;
            c.box(mx, GROUND, mz, mx + 2, GROUND + 170, mz + 2, Mat.ANDESITE);
            c.box(mx - 3, GROUND + 171, mz - 3, mx + 5, GROUND + 174, mz + 5, Mat.LAMP_AMBER);
        }
    }
}
