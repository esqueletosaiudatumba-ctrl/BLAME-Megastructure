package blame.blamegen.core;

/**
 * Brutalist ocean pylon: a chamfered-square hollow shell with ziggurat setbacks, deep-set window
 * grid, board-formed bands, axis fins with neon tip strips, cantilevered volumes with struts,
 * a flared capital that merges into the ceiling plate and an arched plinth at the waterline.
 *
 * Everything is a pure function of (px, pz, yBase, yTop, halfWidth, hash, seaLevel), so every
 * cube that intersects the pylon draws it identically (no seams). Pure Java, no MC imports.
 */
final class Pylon {
    private Pylon() {}

    private static final int STEP = 96;       // ziggurat setback interval
    private static final int WALL = 5;        // shell thickness
    private static final int BAND = 24;       // board-formed band interval
    private static final int FLARE = 120;     // capital flare height under the ceiling

    /** Half width of the shell at height y (ziggurat taper + capital flare). */
    private static int halfAt(int y, int yBase, int yTop, int a, int sea) {
        if (y < sea + 14) return a + 10; // plinth
        int step = (y - yBase) / STEP;
        int hw = Math.max(a / 2 + 6, a - step * 2);
        int toTop = yTop - y;
        if (toTop < FLARE) {
            double t = 1.0 - toTop / (double) FLARE;
            hw += (int) Math.round(t * t * a * 1.4);
        }
        return hw;
    }

    static void draw(Megastructure m, Cube c, int px, int pz, int yBase, int yTop, int a, long hash, int sea) {
        int reach = a * 3 + 40; // flare + cantilever + fins
        if (!c.hits(px - reach, yBase, pz - reach, px + reach, yTop, pz + reach)) return;

        int yLo = Math.max(yBase, c.y0), yHi = Math.min(yTop, c.y0 + 15);
        if (yLo > yHi) return;

        for (int y = yLo; y <= yHi; y++) {
            int hw = halfAt(y, yBase, yTop, a, sea);
            int cham = Math.max(3, hw / 5); // corner chamfer
            boolean plinth = y < sea + 14;
            boolean cap = yTop - y < FLARE;

            // quick per-layer reject against the cube footprint
            if (px + hw < c.x0 || px - hw > c.x0 + 15 || pz + hw < c.z0 || pz - hw > c.z0 + 15) continue;

            byte shell = cap ? Mat.S_MASS : (plinth ? Mat.S_DARK : Mat.S_MASS);
            // band: protruding ring every BAND blocks
            boolean band = ((y - yBase) % BAND) < 2;
            int bh = band ? hw + 1 : hw;

            // hollow chamfered-square shell
            for (int x = Math.max(c.x0, px - bh); x <= Math.min(c.x0 + 15, px + bh); x++) {
                for (int z = Math.max(c.z0, pz - bh); z <= Math.min(c.z0 + 15, pz + bh); z++) {
                    int dx = Math.abs(x - px), dz = Math.abs(z - pz);
                    if (dx + dz > bh + bh - cham) continue; // chamfer
                    int edge = Math.min(bh - dx, bh - dz);       // distance in from the nearest face
                    int cd = (bh + bh - cham) - (dx + dz);       // distance in from the chamfer
                    int depth = Math.min(edge, cd * 2);
                    if (depth < WALL) {
                        c.set(x, y, z, shell);
                    } else {
                        c.set(x, y, z, Mat.AIR);
                    }
                }
            }

            // deep-set window grid (rows every 12, columns every 8), glass behind a reveal
            if (!plinth && !cap && ((y - yBase) % 12) >= 4 && ((y - yBase) % 12) < 10) {
                windows(c, px, pz, hw, y, hash);
            }

            // interior floor every 16 blocks + central ladder core
            if (!plinth && (y - yBase) % 16 == 0 && hw > WALL + 2) {
                int in = hw - WALL;
                c.box(px - in, y, pz - in, px + in, y, pz + in, Mat.S_DECK);
                // stair void next to the core
                c.box(px + 2, y, pz + 2, px + 5, y, pz + 5, Mat.AIR);
            }
        }

        core(c, px, pz, yLo, yHi, yBase, yTop, a, sea);
        plinthArches(c, px, pz, a, sea, hash);
        fins(c, px, pz, yLo, yHi, yBase, yTop, a, sea, hash);
        cantilevers(c, px, pz, yBase, yTop, a, sea, hash);
    }

    /** Windows: carve 4 deep, leave an inner pane of reinforced glass; sparse lit panes. */
    private static void windows(Cube c, int px, int pz, int hw, int y, long hash) {
        int span = hw - 6;
        for (int u = -span; u <= span; u += 8) {
            for (int s = 0; s < 4; s++) {
                int x, z, dx, dz;
                switch (s) {
                    case 0: x = px + u; z = pz - hw; dx = 0; dz = 1; break;
                    case 1: x = px + u; z = pz + hw; dx = 0; dz = -1; break;
                    case 2: x = px - hw; z = pz + u; dx = 1; dz = 0; break;
                    default: x = px + hw; z = pz + u; dx = -1; dz = 0; break;
                }
                // skip windows that are not near this cube
                if (x < c.x0 - 4 || x > c.x0 + 19 || z < c.z0 - 4 || z > c.z0 + 19) continue;
                long wh = H.h(hash, u, y / 12, s);
                boolean lit = H.chance(wh, 0.06);
                for (int w = -1; w <= 1; w++) {
                    int wx = x + (dz != 0 ? w : 0), wz = z + (dx != 0 ? w : 0);
                    for (int d = 0; d < WALL; d++) {
                        int ax = wx + dx * d, az = wz + dz * d;
                        c.set(ax, y, az, d < WALL - 1 ? Mat.AIR : (lit ? Mat.BIO_CYAN : Mat.GLASS_REINFORCED));
                    }
                }
            }
        }
    }

    /** Central ladder core running the full height, with a hatch ring every 64. */
    private static void core(Cube c, int px, int pz, int yLo, int yHi, int yBase, int yTop, int a, int sea) {
        if (px + 3 < c.x0 || px - 3 > c.x0 + 15 || pz + 3 < c.z0 || pz - 3 > c.z0 + 15) return;
        for (int y = Math.max(yLo, sea + 14); y <= yHi; y++) {
            c.box(px - 2, y, pz - 2, px + 2, y, pz + 2, Mat.S_METAL);
            c.box(px - 1, y, pz - 1, px + 1, y, pz + 1, Mat.AIR);
            c.set(px, y, pz - 1, Mat.LADDER_S);
            if ((y - yBase) % 64 == 0) c.set(px, y, pz - 2, Mat.AIR); // access hatch
            if ((y - yBase) % 32 == 8) c.set(px, y, pz + 1, Mat.LAMP_AMBER);
        }
    }

    /** Arches cut through the plinth at the waterline on each face. */
    private static void plinthArches(Cube c, int px, int pz, int a, int sea, long hash) {
        int hw = a + 10;
        int top = sea + 12;
        if (!c.hitsY(sea - 24, top)) return;
        for (int s = 0; s < 4; s++) {
            int ax = (s < 2) ? px - 6 : (s == 2 ? px - hw : px + hw - WALL - 4);
            int az = (s >= 2) ? pz - 6 : (s == 0 ? pz - hw : pz + hw - WALL - 4);
            int bx = (s < 2) ? px + 6 : (s == 2 ? px - hw + WALL + 4 : px + hw);
            int bz = (s >= 2) ? pz + 6 : (s == 0 ? pz - hw + WALL + 4 : pz + hw);
            c.box(ax, sea - 6, az, bx, top - 3, bz, Mat.AIR);
            // arch crown: stepped
            c.box(ax + (s < 2 ? 2 : 0), top - 2, az + (s >= 2 ? 2 : 0), bx - (s < 2 ? 2 : 0), top - 2, bz - (s >= 2 ? 2 : 0), Mat.AIR);
        }
        // waterline copper-patina belt
        c.box(px - hw - 1, sea - 2, pz - hw - 1, px + hw + 1, sea + 1, pz - hw, Mat.COPPER_PATINA);
        c.box(px - hw - 1, sea - 2, pz + hw, px + hw + 1, sea + 1, pz + hw + 1, Mat.COPPER_PATINA);
    }

    /** Axis fins: a thin blade on each face centre, with a neon strip running up its leading edge. */
    private static void fins(Cube c, int px, int pz, int yLo, int yHi, int yBase, int yTop, int a, int sea, long hash) {
        for (int s = 0; s < 4; s++) {
            int y0 = yLo, y1 = yHi;
            for (int y = y0; y <= y1; y++) {
                int hw = halfAt(y, yBase, yTop, a, sea);
                if (yTop - y < FLARE / 2) continue; // fins stop under the flare
                int len = 6 + (int) ((H.u(H.h(hash, s, y / STEP)) * 6));
                byte neon = (s & 1) == 0 ? Mat.BIO_CYAN : Mat.BIO_MAGENTA;
                int x0, x1, z0, z1, nx, nz;
                if (s < 2) { // north/south fins
                    int zf = s == 0 ? pz - hw - len : pz + hw + 1;
                    x0 = px - 1; x1 = px + 1; z0 = zf; z1 = zf + len - 1;
                    nx = px; nz = s == 0 ? z0 : z1;
                } else {     // west/east fins
                    int xf = s == 2 ? px - hw - len : px + hw + 1;
                    x0 = xf; x1 = xf + len - 1; z0 = pz - 1; z1 = pz + 1;
                    nx = s == 2 ? x0 : x1; nz = pz;
                }
                if (x1 < c.x0 || x0 > c.x0 + 15 || z1 < c.z0 || z0 > c.z0 + 15) continue;
                c.box(x0, y, z0, x1, y, z1, Mat.S_MASS);
                c.set(nx, y, nz, neon);
            }
        }
    }

    /** Cantilevered volumes every ~160 blocks, each carried by a diagonal strut. */
    private static void cantilevers(Cube c, int px, int pz, int yBase, int yTop, int a, int sea, long hash) {
        int first = sea + 60;
        for (int y = first; y < yTop - FLARE - 40; y += 160) {
            long ch = H.h(hash, 0xCA17, y);
            int side = (int) (ch & 3);
            int hw = halfAt(y, yBase, yTop, a, sea);
            int w = 14 + (int) (H.u(ch >>> 3) * 10), h = 10 + (int) (H.u(ch >>> 9) * 6), d = 16 + (int) (H.u(ch >>> 17) * 12);
            int ox = side == 2 ? -(hw + d) : (side == 3 ? hw : -w / 2);
            int oz = side == 0 ? -(hw + d) : (side == 1 ? hw : -w / 2);
            int sx = (side >= 2) ? d : w, sz = (side >= 2) ? w : d;
            int x0 = px + ox, z0 = pz + oz, x1 = x0 + sx, z1 = z0 + sz;
            if (!c.hits(x0 - 2, y - 14, z0 - 2, x1 + 2, y + h + 1, z1 + 2)) continue;
            // hollow volume with a lit slot
            c.box(x0, y, z0, x1, y + h, z1, Mat.S_MASS);
            c.box(x0 + 2, y + 2, z0 + 2, x1 - 2, y + h - 2, z1 - 2, Mat.AIR);
            int mx = (x0 + x1) / 2, mz = (z0 + z1) / 2;
            c.box(side >= 2 ? x0 : mx - 3, y + h / 2, side >= 2 ? mz - 3 : z0, side >= 2 ? x1 : mx + 3, y + h / 2, side >= 2 ? mz + 3 : z1, Mat.LAMP_AMBER);
            // diagonal strut back to the shell
            double bx = side >= 2 ? (side == 2 ? px - hw : px + hw) : mx;
            double bz = side < 2 ? (side == 0 ? pz - hw : pz + hw) : mz;
            c.beam(mx, y, mz, bx, y - 14, bz, 1.5, Mat.S_METAL);
        }
    }
}
