package blame.blamegen.core;

/**
 * Ocean spires: brutalist chamfered-square pylons standing in the arrival bay.
 *
 * <p>Each pylon rises from the seabed, pierces the waterline on a plinth of arches, and
 * climbs to merge with the strata roof. The design language is Nihei's brutalist
 * monoliths: board-formed concrete, chamfered corners, ziggurat setbacks, deep-set window
 * grids behind a reveal, vertical axis fins, cantilevered volumes on diagonal struts, and
 * a flared capital. Neon tip strips and glass pockets pick out the fins and openings.
 *
 * <p>Everything is derived from the caller's hash, so the same seed always yields the same
 * skyline. All work is confined to the pylon's own footprint, so no cube seams appear.
 */
final class Pylon {
    /** Chamfer for the octagonal plan: |dx|+|dz| <= a * (1 + CH). Regular octagon at 1.4142. */
    private static final double CH = 0.4142;
    /** Vertical period of the setback steps and of the board-formed bands. */
    private static final int BAND = 24;

    private Pylon() {}

    /**
     * Draws one pylon.
     *
     * @param m         the owning megastructure (for altitude/datum queries)
     * @param c         the cube to write into
     * @param px        centre X
     * @param pz        centre Z
     * @param yBase     bottom of the pylon (seabed)
     * @param yTop      top of the pylon (just past the ceiling plate)
     * @param halfWidth base half-width in blocks
     * @param hash      deterministic hash for this pylon
     * @param seaLevel  water surface Y, for the plinth of arches
     */
    static void draw(Megastructure m, Cube c, int px, int pz, int yBase, int yTop,
                     int halfWidth, long hash, int seaLevel) {
        double r0 = halfWidth;
        // Reject early: footprint plus fins/cantilevers, full height.
        if (!c.hits(px - halfWidth * 3 - 32, yBase - 8, pz - halfWidth * 3 - 32,
                px + halfWidth * 3 + 32, yTop + 16, pz + halfWidth * 3 + 32)) return;

        int wall = 3 + H.range(H.h(hash, 1), 0, 3);          // shell thickness
        int plinthH = seaLevel - yBase + 4;                  // plinth height above seabed
        int topY = Math.min(yTop, m.ceilDatum(px, pz) + 10);
        int shaftTop = topY - (10 + H.range(H.h(hash, 2), 0, 12)); // capital starts here

        // ---- plinth: arches at the waterline -------------------------------------
        plinth(c, px, pz, yBase, seaLevel, halfWidth, hash, wall);

        // ---- shaft with setbacks --------------------------------------------------
        drawShaft(c, m, px, pz, seaLevel, shaftTop, halfWidth, wall, hash);

        // ---- capital: flared merge into the ceiling plate --------------------------
        capital(c, px, pz, shaftTop, topY, halfWidth, wall, hash);
    }

    // ------------------------------------------------------------------ plinth

    private static void plinth(Cube c, int px, int pz, int yBase, int seaLevel,
                               int halfWidth, long hash, int wall) {
        int top = seaLevel + 4;
        int widen = 6 + H.range(H.h(hash, 3), 0, 6);
        int a = halfWidth + widen;
        // Solid chamfered base.
        int ay = Math.max(yBase, c.y0), by = Math.min(top, c.y0 + 15);
        if (by < ay) return;
        for (int y = ay; y <= by; y++)
            shell(c, px, pz, a, wall + 2, true, y, y, hash);
        // Arches: ring of pillars on the plinth rim with a gap (arch) between each pair.
        int np = 8 + H.range(H.h(hash, 4), 0, 8);
        if (np < 4) np = 4;
        double off = H.u(H.h(hash, 5));
        int y0 = seaLevel + 1, y1 = top;
        for (int q = 0; q < np; q++) {
            double ang = 2 * Math.PI * (q + off) / np;
            double rr = a - wall * 0.5;
            int fx = (int) Math.round(px + Math.cos(ang) * rr);
            int fz = (int) Math.round(pz + Math.sin(ang) * rr);
            // Pillar.
            c.box(fx - 2, y0, fz - 2, fx + 2, y1, fz + 2, Mat.S_MASS);
            // Spring the arch toward the next pillar.
            double ang2 = 2 * Math.PI * (q + 1 + off) / np;
            int tx = (int) Math.round(px + Math.cos(ang2) * rr);
            int tz = (int) Math.round(pz + Math.sin(ang2) * rr);
            c.box(Math.min(fx, tx) - 2, y1 - 1, Math.min(fz, tz) - 2, Math.max(fx, tx) + 2, y1, Math.max(fz, tz) + 2, Mat.S_DARK);
        }
        // Dark waterline stain.
        int yst = seaLevel - 2;
        if (c.hitsY(yst, yst)) shell(c, px, pz, a, 1, true, yst, yst, hash);
    }

    // ------------------------------------------------------------------ shaft

    private static void drawShaft(Cube c, Megastructure m, int px, int pz,
                                  int yFrom, int yTo, int halfWidth, int wall, long hash) {
        if (yTo <= yFrom) return;
        // Ziggurat setbacks: the half-width steps in every BAND blocks of height.
        for (int q = 0; q < 64; q++) {
            int y0 = yFrom + q * BAND, y1 = Math.min(yTo, yFrom + (q + 1) * BAND);
            if (y1 <= y0) break;
            // Step function: two narrowings, deterministic.
            double step = 1.0 - 0.16 * Math.min(q, 5) * (0.6 + H.u(H.h(hash, 10, q)) * 0.8);
            int a = Math.max(8, (int) Math.round(halfWidth * step));
            // Wall.
            int ay = Math.max(y0, c.y0), by = Math.min(y1, c.y0 + 15);
            for (int y = ay; y <= by; y++)
                shell(c, px, pz, a, wall, false, y, y, hash);
            // Board-formed concrete bands and rain streaks.
            int bandY = y0 + H.range(H.h(hash, 11, q), 0, BAND - 2);
            if (bandY >= c.y0 && bandY <= c.y0 + 15 && bandY < y1) {
                shell(c, px, pz, a + 1, 1, false, bandY, bandY, hash);
                // rain streaks: vertical dark runs on the outer corners
                for (int s = 0; s < 4; s++) {
                    double ang = Math.PI / 4 + s * Math.PI / 2;
                    int fx = (int) Math.round(px + Math.cos(ang) * a * 0.7071);
                    int fz = (int) Math.round(pz + Math.sin(ang) * a * 0.7071);
                    int from = Math.max(y0, c.y0), to = Math.min(y1 - 1, c.y0 + 15);
                    for (int y = from; y <= to; y += 2)
                        c.set(fx, y, fz, Mat.S_DARK);
                }
            }
            // Window grid: deep-set pockets with glass behind a reveal.
            windows(c, px, pz, a, y0, y1, wall, hash, q);
            // Axis fins with neon tip strips.
            fins(c, px, pz, a, y0, y1, wall, hash, q);
            // Cantilevered volumes on diagonal struts.
            cantilevers(c, px, pz, a, y0, y1, hash, q);
            // Interior floors every 16 blocks.
            int f0 = y0 + Math.floorMod(-y0, 16);
            for (int y = f0; y < y1; y += 16) {
                if (c.hitsY(y, y)) floor(c, px, pz, a, wall, y, hash, q);
            }
        }
        // Central ladder core, lit by intermittent lamps.
        coreLadder(c, px, pz, wall, yFrom, yTo, hash);
    }

    // ------------------------------------------------------------------ components

    /** One height slice of the hollow chamfered-square shell. Hollow when requested. */
    private static void shell(Cube c, int px, int pz, int a, int wall, boolean solid,
                              int ylo, int yhi, long hash) {
        if (yhi < c.y0 || ylo > c.y0 + 15) return;
        int x0 = Math.max(px - a - 1, c.x0), x1 = Math.min(px + a + 1, c.x0 + 15);
        int z0 = Math.max(pz - a - 1, c.z0), z1 = Math.min(pz + a + 1, c.z0 + 15);
        int ya = Math.max(ylo, c.y0), yb = Math.min(yhi, c.y0 + 15);
        double lim = a * (1.0 + CH);
        for (int yy = ya; yy <= yb; yy++)
            for (int x = x0; x <= x1; x++)
                for (int z = z0; z <= z1; z++) {
                    int dx = Math.abs(x - px), dz = Math.abs(z - pz);
                    if (dx > a || dz > a || dx + dz > lim) continue;
                    if (solid) { c.set(x, yy, z, Mat.S_MASS); continue; }
                    int ox = a - dx, oz = a - dz;
                    int od = (int) (ox + oz - (a - lim) * 0.5);
                    // On the shell boundary?
                    if (dx + dz >= lim - wall || dx >= a - wall || dz >= a - wall) {
                        c.set(x, yy, z, Mat.S_MASS);
                    } else if (od <= wall + 1 && H.chance(H.h(hash, 30, x, z), 0.5)) {
                        c.set(x, yy, z, Mat.S_PALE);
                    }
                }
    }

    /** Deep-set window grid: reveal of lining, glass at the back, rare service lamps. */
    private static void windows(Cube c, int px, int pz, int a, int y0, int y1,
                                int wall, long hash, int q) {
        int rowStep = 4 + H.range(H.h(hash, 20, q), 0, 4);
        int colStep = 4 + H.range(H.h(hash, 21, q), 0, 3);
        int depth = 5; // reveal
        int ay = Math.max(y0, c.y0), by = Math.min(y1, c.y0 + 15);
        for (int y = ay; y <= by; y++) {
            if (Math.floorMod(y, rowStep) != 0) continue;
            for (int side = 0; side < 4; side++) {
                // side: 0=+x, 1=-x, 2=+z, 3=-z
                int fixed = side < 2 ? (side == 0 ? pz : pz) : px;
                int alongLo, alongHi;
                int base = pz + (side < 2 ? 0 : 0);
                alongLo = (side < 2 ? pz - a : px - a);
                alongHi = (side < 2 ? pz + a : px + a);
                for (int t = alongLo + colStep; t < alongHi; t += colStep) {
                    long hw = H.h(hash, 22, q, y, t);
                    if (!H.chance(hw, 0.55)) continue;
                    for (int d = 1; d <= depth; d++) {
                        int bx = side < 2 ? px + (side == 0 ? a - d : -(a - d)) : t;
                        int bz = side < 2 ? t : pz + (side == 2 ? a - d : -(a - d));
                        if (bx < c.x0 || bx > c.x0 + 15 || bz < c.z0 || bz > c.z0 + 15) continue;
                        if (d < depth) c.set(bx, y + 1, bz, Mat.S_DARK); // reveal lining
                        else c.set(bx, y + 1, bz, Mat.GLASS_REINFORCED);
                    }
                    // Rare service lamp behind the glass.
                    if (H.chance(H.h(hw, 3), 0.12)) {
                        int lx = side < 2 ? px + (side == 0 ? a - depth - 1 : -(a - depth - 1)) : t;
                        int lz = side < 2 ? t : pz + (side == 2 ? a - depth - 1 : -(a - depth - 1));
                        if (lx >= c.x0 && lx <= c.x0 + 15 && lz >= c.z0 && lz <= c.z0 + 15)
                            c.set(lx, y + 1, lz, Mat.SEA_LANTERN);
                    }
                }
            }
        }
    }

    /** Vertical axis fins on the four faces, capped with neon tip strips. */
    private static void fins(Cube c, int px, int pz, int a, int y0, int y1,
                             int wall, long hash, int q) {
        int nf = 1 + H.range(H.h(hash, 40, q), 0, 2);
        int finW = 2 + H.range(H.h(hash, 41, q), 0, 2);
        int ay = Math.max(y0, c.y0), by = Math.min(y1, c.y0 + 15);
        for (int f = 0; f < nf; f++) {
            for (int side = 0; side < 4; side++) {
                long hf = H.h(hash, 42, q, f, side);
                // Offset along the face.
                double u = H.u(hf);
                int off = (int) Math.round((u - 0.5) * 2 * (a - finW));
                int fx = side < 2 ? px + (side == 0 ? a + 1 : -(a + 1)) : px + off;
                int fz = side < 2 ? pz + off : pz + (side == 2 ? a + 1 : -(a + 1));
                // Fin body.
                for (int y = ay; y <= by; y++) {
                    int x0 = Math.max(fx - finW, c.x0), x1 = Math.min(fx + finW, c.x0 + 15);
                    int z0 = Math.max(fz - 1, c.z0), z1 = Math.min(fz + 1, c.z0 + 15);
                    for (int x = x0; x <= x1; x++)
                        for (int z = z0; z <= z1; z++) c.set(x, y, z, Mat.S_DARK);
                }
                // Neon tip strip at the top of this band.
                int tipY = Math.min(y1, ay + (int) ((by - ay) * 0.9));
                if (tipY >= c.y0 && tipY <= c.y0 + 15) {
                    int x0 = Math.max(fx - finW, c.x0), x1 = Math.min(fx + finW, c.x0 + 15);
                    int z0 = Math.max(fz - 1, c.z0), z1 = Math.min(fz + 1, c.z0 + 15);
                    for (int x = x0; x <= x1; x++)
                        for (int z = z0; z <= z1; z++) c.set(x, tipY, z, Mat.LAMP_GREEN);
                }
            }
        }
    }

    /** Cantilevered volumes hanging off the shaft, tied by diagonal struts. */
    private static void cantilevers(Cube c, int px, int pz, int a, int y0, int y1,
                                    long hash, int q) {
        int n = H.range(H.h(hash, 50, q), 0, 3);
        for (int i = 0; i < n; i++) {
            long hq = H.h(hash, 51, q, i);
            int side = H.range(H.h(hq, 1), 0, 3);
            int cy = y0 + H.range(H.h(hq, 2), 4, Math.max(5, (y1 - y0) - 8));
            int w = H.range(H.h(hq, 3), 3, 10);
            int d = H.range(H.h(hq, 4), 2, 5);
            int off = (int) Math.round((H.u(H.h(hq, 5)) - 0.5) * 2 * (a - w));
            // Volume sticking out from the face.
            int bx = side < 2 ? (side == 0 ? a - 1 : -(a + d)) : px + off;
            int bz = side < 2 ? pz + off : (side == 2 ? a - 1 : -(a + d));
            int bx1 = side < 2 ? bx + d : bx + w;
            int bz1 = side < 2 ? bz + w : bz + d;
            if (side == 1) bx1 = -(a + d);
            if (side == 3) bz1 = -(a + d);
            // Normalise to a min/max pair.
            int mxx = Math.min(bx, bx1), Mxx = Math.max(bx, bx1);
            int mzz = Math.min(bz, bz1), Mzz = Math.max(bz, bz1);
            int ya = Math.max(cy, c.y0), yb = Math.min(Math.min(cy + 4, c.y0 + 15), y1);
            if (yb < ya) continue;
            // Diagonal struts up to the shaft.
            int stTop = Math.min(cy + 12, y1);
            for (int yy = Math.max(cy, c.y0); yy <= Math.min(stTop, c.y0 + 15); yy++) {
                double t = (yy - cy) / (double) Math.max(1, stTop - cy);
                int sx = side < 2 ? (side == 0 ? a - 1 : -(a - 1)) : (int) (px + off + (Mxx - px - off) * t);
                int sz = side < 2 ? (int) (pz + off + (Mzz - pz - off) * t) : (side == 2 ? a - 1 : -(a - 1));
                c.set(sx, yy, sz, Mat.S_DARK);
            }
            // The volume.
            for (int x = Math.max(mxx, c.x0); x <= Math.min(Mxx, c.x0 + 15); x++)
                for (int z = Math.max(mzz, c.z0); z <= Math.min(Mzz, c.z0 + 15); z++)
                    for (int y = ya; y <= yb; y++) c.set(x, y, z, Mat.S_PALE);
            // Underside neon.
            if (ya - 1 >= c.y0 && ya - 1 <= c.y0 + 15)
                for (int x = Math.max(mxx, c.x0); x <= Math.min(Mxx, c.x0 + 15); x++)
                    for (int z = Math.max(mzz, c.z0); z <= Math.min(Mzz, c.z0 + 15); z++)
                        c.set(x, ya - 1, z, Mat.LAMP_GREEN);
        }
    }

    /** Interior floor slab every 16 blocks with a ladder core and a hatch. */
    private static void floor(Cube c, int px, int pz, int a, int wall, int y,
                              long hash, int q) {
        int x0 = Math.max(px - a + wall, c.x0), x1 = Math.min(px + a - wall, c.x0 + 15);
        int z0 = Math.max(pz - a + wall, c.z0), z1 = Math.min(pz + a - wall, c.x0 + 15);
        for (int x = x0; x <= x1; x++)
            for (int z = z0; z <= z1; z++) c.set(x, y, z, Mat.S_DECK);
        // Ladder core.
        c.box(px, y + 1, pz, px, Math.min(y + 8, c.y0 + 15), pz, Mat.LADDER_N);
        if (c.hitsY(y + 1, y + 1)) c.set(px, y + 1, pz, Mat.SEA_LANTERN);
    }

    /** Central ladder core with lamps, climbing the whole shaft. */
    private static void coreLadder(Cube c, int px, int pz, int wall,
                                   int yFrom, int yTo, long hash) {
        int x0 = Math.max(px - 1, c.x0), x1 = Math.min(px + 1, c.x0 + 15);
        int z0 = Math.max(pz - 1, c.x0), z1 = Math.min(pz + 1, c.x0 + 15);
        int ay = Math.max(yFrom, c.y0), by = Math.min(yTo, c.y0 + 15);
        for (int y = ay; y <= by; y++) {
            if (y > Math.min(yTo, c.y0 + 15)) break;
            for (int x = x0; x <= x1; x++)
                for (int z = z0; z <= z1; z++) {
                    c.set(x, y, z, Mat.LADDER_N);
                }
            if (Math.floorMod(y, 16) == 0) c.set(px, y, pz, Mat.SEA_LANTERN);
        }
    }

    /** Flared capital: setbacks widen again, merging into the ceiling plate. */
    private static void capital(Cube c, int px, int pz, int yFrom, int yTo,
                                int halfWidth, int wall, long hash) {
        if (yTo <= yFrom) return;
        int steps = 3;
        int h = Math.max(6, (yTo - yFrom) / steps);
        int ay = Math.max(yFrom, c.y0);
        for (int s = 0; s < steps; s++) {
            int y0 = yFrom + s * h, y1 = Math.min(yTo, yFrom + (s + 1) * h);
            if (y1 <= y0) break;
            int a = (int) Math.round(halfWidth * (0.66 + 0.34 * s / (steps - 1)));
            int ya = Math.max(y0, c.y0), yb = Math.min(y1, c.y0 + 15);
            for (int y = ya; y <= yb; y++)
                shell(c, px, pz, a, wall, false, y, y, hash);
        }
        // Neon ring at the throat.
        int ry = yFrom + h;
        if (ry >= c.y0 && ry <= c.y0 + 15) {
            int a = (int) Math.round(halfWidth * 0.66);
            shell(c, px, pz, a + 1, 1, false, ry, ry, hash);
        }
    }
}
