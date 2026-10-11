package blame.blamegen.core;

/**
 * One-of-a-kind landmarks for the coarse cells that would otherwise be empty (the r >= 0.74
 * branch of {@link Megastructure#drawCoarse}). Five types, each a single silhouette that
 * cannot be mistaken for the rationalist lattice:
 *
 *  - PIPES    a forest of enormous pipes crossing the whole cell
 *  - MONOLITHS floating slabs hanging on nothing, in a slow drift
 *  - WHEEL     a vast free-standing wheel with rim, spokes and hub
 *  - CATHEDRAL a hanging shell of ribs and window slits suspended from the ceiling
 *  - HELIX     a stem with bridges and a spiral ramp climbing it
 *
 * Every landmark derives all geometry from (seed, cx, cz) so it is identical in every
 * cube that touches it, and all of it is kept inside the cell footprint (origin is
 * jittered inward by the size of the landmark), so no cube seams appear.
 */
final class Landmark {
    private Landmark() {}

    static final int L_PIPES = 0, L_MONOLITHS = 1, L_WHEEL = 2, L_CATHEDRAL = 3, L_HELIX = 4;

    static final String[] NAMES = {"PIPES", "MONOLITHS", "WHEEL", "CATHEDRAL", "HELIX"};

    /** Picks the type from the same hash stream drawCoarse already consumed. */
    static int type(long hc) {
        double r = H.u(H.h(hc, 8));
        if (r < 0.24) return L_PIPES;
        if (r < 0.48) return L_MONOLITHS;
        if (r < 0.68) return L_WHEEL;
        if (r < 0.84) return L_CATHEDRAL;
        return L_HELIX;
    }

    /** Centre of the landmark in the cell, as {cx, cy, cz}. */
    static int[] centre(Megastructure m, int cx, int cz, long hc) {
        int t = type(hc);
        int reach = 190 + t * 22; // each type needs a slightly different envelope
        int lim = Megastructure.CELL / 2 - reach;
        int limY = t == L_MONOLITHS || t == L_CATHEDRAL ? 120 : 260;
        return new int[]{
                cx * Megastructure.CELL + H.range(H.h(hc, 20), -lim, lim),
                m.tierY(H.range(H.h(hc, 21), -12, Megastructure.TOP_TIER - 2)) + H.range(H.h(hc, 22), -limY, limY),
                cz * Megastructure.CELL + H.range(H.h(hc, 23), -lim, lim)
        };
    }

    /** Draws the landmark for this cell into the cube. */
    static void draw(Megastructure m, Cube c, int cx, int cz, long hc) {
        int t = type(hc);
        int[] cc = centre(m, cx, cz, hc);
        switch (t) {
            case L_PIPES: pipes(m, c, cc, hc); return;
            case L_MONOLITHS: monoliths(m, c, cc, hc); return;
            case L_WHEEL: wheel(m, c, cc, hc); return;
            case L_CATHEDRAL: cathedral(m, c, cc, hc); return;
            default: helix(m, c, cc, hc); return;
        }
    }

    /** Human-readable line for the survey tool. */
    static String describe(Megastructure m, int cx, int cz, long hc) {
        int t = type(hc);
        int[] cc = centre(m, cx, cz, hc);
        return NAMES[t] + " cell(" + cx + "," + cz + ") c=" + cc[0] + "," + cc[1] + "," + cc[2];
    }

    // -------------------------------------------------------------- PIPES

    private static void pipes(Megastructure m, Cube c, int[] cc, long hc) {
        int n = H.range(H.h(hc, 30), 7, 13);
        int span = Math.min(Megastructure.CELL - 80, 700);
        int y0 = cc[1] - 120, y1 = cc[1] + 120;
        for (int q = 0; q < n; q++) {
            long hq = H.h(hc, 31, q);
            // Each pipe runs essentially the full length of the cell, tilted slightly.
            int dy = H.range(H.h(hq, 1), -100, 100);
            int x0 = cc[0] - span / 2, x1 = cc[0] + span / 2;
            int z0 = cc[2] + H.range(H.h(hq, 2), -160, 160);
            int y = cc[1] + H.range(H.h(hq, 3), -110, 110);
            double r = H.range(H.h(hq, 4), 4, 12);
            c.tube(x0, y, z0, x1, y + dy, z0 + H.range(H.h(hq, 5), -40, 40), r, r - 1.5, Mat.S_PIPEWORK);
            // Saddles + junction boxes at intervals.
            for (int s = 90; s < span; s += 90) {
                int px = x0 + s;
                c.box(px - 3, y + dy * s / span - 3, z0 - 3, px + 3, y + dy * s / span + 3, z0 + 3, Mat.S_DARK);
                if (H.chance(H.h(hq, 100 + s), 0.4)) {
                    int jx = y + dy * s / span;
                    c.box(px, jx - 5, z0 - 2, px, jx + 5, z0 + 2, Mat.COPPER_PATINA);
                    c.set(px, jx + 6, z0, Mat.LAMP_RED);
                }
            }
        }
        // A few smaller bundles running the other way.
        for (int q = 0; q < 5; q++) {
            long hq = H.h(hc, 40, q);
            int z = cc[2] - span / 2 + H.range(H.h(hq, 1), 0, span);
            int y = cc[1] + H.range(H.h(hq, 2), -100, 100);
            int x = cc[0] + H.range(H.h(hq, 3), -120, 120);
            c.tube(cc[0] + H.range(H.h(hq, 4), -140, 140), y, z - 120,
                    x, y + H.range(H.h(hq, 5), -60, 60), z + 120, 3, 1, Mat.CABLE);
        }
    }

    // -------------------------------------------------------------- MONOLITHS

    private static void monoliths(Megastructure m, Cube c, int[] cc, long hc) {
        int n = H.range(H.h(hc, 30), 5, 9);
        for (int q = 0; q < n; q++) {
            long hq = H.h(hc, 31, q);
            int w = H.range(H.h(hq, 1), 34, 78);
            int d = H.range(H.h(hq, 2), 34, 78);
            int h = H.range(H.h(hq, 3), 14, 46);
            int x = cc[0] + H.range(H.h(hq, 4), -220, 220);
            int y = cc[1] + H.range(H.h(hq, 5), -150, 150);
            int z = cc[2] + H.range(H.h(hq, 6), -220, 220);
            boolean tilted = H.chance(H.h(hq, 7), 0.6);
            double rot = H.u(H.h(hq, 8)) * Math.PI;
            if (!tilted) {
                c.box(x - w / 2, y - h / 2, z - d / 2, x + w / 2, y + h / 2, z + d / 2, Mat.S_COLUMN);
            } else {
                // A rotated, tilted slab: cast as a sheared prism so it reads as adrift.
                c.prism(x, z, Math.max(w, d) / 2.0, rot, 0.06, false, 2, y - h / 2, y + h / 2, Mat.S_COLUMN);
            }
            // A lamp strip on the underside, and a stub of missing edge.
            c.box(x - w / 2 + 4, y - h / 2 - 1, z - 2, x + w / 2 - 4, y - h / 2 - 1, z + 2, Mat.S_DARK);
            if (H.chance(H.h(hq, 9), 0.5)) c.set(x, y - h / 2 - 2, z, Mat.LAMP_AMBER);
            int miss = H.range(H.h(hq, 10), 2, 6);
            c.box(x + w / 2 - miss, y - h / 2, z - d / 2, x + w / 2, y + h / 2, z + d / 2, Mat.AIR);
        }
    }

    // -------------------------------------------------------------- WHEEL

    private static void wheel(Megastructure m, Cube c, int[] cc, long hc) {
        int R = H.range(H.h(hc, 30), 110, 190);
        int rimT = H.range(H.h(hc, 31), 6, 14);
        int spokes = H.range(H.h(hc, 32), 9, 20);
        int cy = cc[1];
        boolean broken = H.chance(H.h(hc, 33), 0.7);
        double gapA = H.u(H.h(hc, 34)) * Math.PI * 2, gapW = H.u(H.h(hc, 35)) * 1.1;
        double cx = cc[0] + 0.5, cz = cc[2] + 0.5;
        // Rim: a vertical annulus, as two concentric cylinders snapped to ints.
        c.cylinder(cx, cz, R, R - rimT, 1, 1, cy - R, cy + R, 0, 0, Mat.S_DARK);
        // Inner ring, offset, so the wheel has depth.
        c.cylinder(cx, cz, R - rimT / 2.0, R - rimT, 1, 1, cy - R + 4, cy + R - 4, 0, 0, Mat.S_PALE);
        // Spokes.
        for (int q = 0; q < spokes; q++) {
            double a = q * Math.PI * 2 / spokes;
            int sx = (int) (cx + Math.cos(a) * (R - rimT / 2.0));
            int sz = (int) (cz + Math.sin(a) * (R - rimT / 2.0));
            c.beam(cx, cy, cz, sx, cy, sz, 2, Mat.S_DARK);
            c.beam(sx, cy, sz, cx + Math.cos(a) * 2, cy, cz + Math.sin(a) * 2, 5, Mat.S_PALE);
            // Hub ends of the spokes carry lamps.
            if (q % 3 == 0) c.set(sx, cy, sz, Mat.LAMP_AMBER);
        }
        // Hub: a drum on a shaft, with an open bay in the side.
        int hr = H.range(H.h(hc, 36), 14, 26);
        c.cylinder(cx, cz, hr + 6, 0, 1, 1, cy - hr, cy + hr, 0, 0, Mat.S_DARK);
        c.cylinder(cx, cz, hr, hr - 5, 1, 1, cy - hr, cy + hr, gapA, 1.9, Mat.AIR);
        c.set((int) cx, cy + hr + 1, (int) cz, Mat.LAMP_RED);
        c.box((int) cx - 2, cy - hr - 30, (int) cz - 2, (int) cx + 2, cy - hr, (int) cz + 2, Mat.S_COLUMN);
        if (broken) {
            // Knock a bite out of the rim on one side.
            int bx2 = (int) (cx + Math.cos(gapA) * R), bz2 = (int) (cz + Math.sin(gapA) * R);
            c.box(bx2 - 16, cy - R, bz2 - 16, bx2 + 16, cy + 16, bz2 + 16, Mat.AIR);
            for (int q = 0; q < 6; q++) {
                long hq = H.h(hc, 100 + q);
                c.set(bx2 + H.range(H.h(hq, 1), -18, 18), cy + H.range(H.h(hq, 2), -R, R),
                        bz2 + H.range(H.h(hq, 3), -18, 18), Mat.REBAR);
            }
        }
    }

    // -------------------------------------------------------------- CATHEDRAL

    private static void cathedral(Megastructure m, Cube c, int[] cc, long hc) {
        int R = H.range(H.h(hc, 30), 70, 130);
        int hh = H.range(H.h(hc, 31), 90, 200);
        double cx = cc[0] + 0.5, cz = cc[2] + 0.5;
        int cy = cc[1];
        // Shell: an ellipsoid dome, hollow, with the floor left open.
        c.blob(cx, cy, cz, R * 0.8, hh * 0.5, R * 0.8, Mat.S_PALE);
        c.blob(cx, cy, cz, R * 0.8 - 4, hh * 0.5 - 3, R * 0.8 - 4, Mat.AIR);
        // Ribs: a ring of vertical fins inside the shell, plus horizontal courses.
        int ribs = H.range(H.h(hc, 32), 10, 18);
        for (int q = 0; q < ribs; q++) {
            double a = q * Math.PI * 2 / ribs + H.u(H.h(hc, 33, q)) * 0.3;
            int rx = (int) (cx + Math.cos(a) * (R * 0.72));
            int rz = (int) (cz + Math.sin(a) * (R * 0.72));
            c.box(rx - 1, cy - hh / 2 + 4, rz - 1, rx + 1, cy + hh / 2 - 4, rz + 1, Mat.S_DARK);
            if (q % 4 == 0) c.set(rx, cy - hh / 2 + 5, rz, Mat.SEA_LANTERN);
        }
        for (int y = cy - hh / 2 + 12; y < cy + hh / 2 - 8; y += 22) {
            c.cylinder(cx, cz, R * 0.74, R * 0.66, 1, 1, y, y, H.u(H.h(hc, 40, Math.floorDiv(y, 22))) * 6.28, 1.0, Mat.S_DARK);
        }
        // Window slits high on the shell.
        int ns = H.range(H.h(hc, 34), 14, 26);
        for (int q = 0; q < ns; q++) {
            long hq = H.h(hc, 50, q);
            double a = H.u(hq) * Math.PI * 2;
            int sy = cy - hh / 2 + 18 + H.range(H.h(hq, 1), 0, hh - 40);
            int sx = (int) (cx + Math.cos(a) * R * 0.72), sz = (int) (cz + Math.sin(a) * R * 0.72);
            c.box(sx, sy, sz, sx, sy + 8, sz, Mat.AIR);
            if (H.chance(H.h(hq, 2), 0.35)) c.set(sx, sy, sz, Mat.GLASS_REINFORCED);
        }
        // Suspension: a thick forest of beams up into the ceiling.
        for (int q = 0; q < 10; q++) {
            double a = q * Math.PI / 5;
            int rx = (int) (cx + Math.cos(a) * R * 0.7), rz = (int) (cz + Math.sin(a) * R * 0.7);
            c.beam(rx, cy + hh / 2, rz, rx + 20, cy + hh / 2 + 90, rz + 14, 2, Mat.CABLE);
            c.set(rx, cy + hh / 2 + 1, rz, Mat.LAMP_RED);
        }
        // Broken floor: only part of a plaza remains.
        c.box((int) cx - R / 2, cy - hh / 2 + 1, (int) cz - R / 2, (int) cx + R / 2, cy - hh / 2 + 3, (int) cz + R / 2, Mat.S_DECK);
        int gapW = H.range(H.h(hc, 36), 12, 40);
        c.box((int) cx - R / 2 + H.range(H.h(hc, 37), 0, R), cy - hh / 2 + 1, (int) cz - R / 2,
                (int) cx - R / 2 + H.range(H.h(hc, 37), 0, R) + gapW, cy - hh / 2 + 4, (int) cz + R / 2, Mat.AIR);
    }

    // -------------------------------------------------------------- HELIX

    private static void helix(Megastructure m, Cube c, int[] cc, long hc) {
        int R = H.range(H.h(hc, 30), 20, 42);
        int hh = H.range(H.h(hc, 31), 200, 460);
        double cx = cc[0] + 0.5, cz = cc[2] + 0.5;
        int base = cc[1] - hh / 2;
        // Stem.
        c.cylinder(cx, cz, R * 0.32, R * 0.32 - 3, 1, 1, base, base + hh, 0, 0, Mat.S_MASS);
        // Bulge rings.
        for (int y = base + 20; y < base + hh; y += 34) {
            c.cylinder(cx, cz, R * 0.5, R * 0.32, 1, 1, y, y + 3, 0, 0, Mat.S_DARK);
        }
        // Spiral ramp: one block per 2 steps of angle, rising with the height.
        int turns = H.range(H.h(hc, 32), 4, 9);
        int steps = turns * 28;
        for (int q = 0; q < steps; q++) {
            double t = (double) q / steps;
            double a = t * turns * Math.PI * 2;
            int y = base + (int) (hh * t);
            int rx = (int) (cx + Math.cos(a) * R), rz = (int) (cz + Math.sin(a) * R);
            c.box(rx - 2, y, rz - 2, rx + 2, y, rz + 2, Mat.S_DECK);
            // Outer rail, missing in places.
            if (q % 7 != 3) {
                int ox = (int) (cx + Math.cos(a) * (R + 2)), oz = (int) (cz + Math.sin(a) * (R + 2));
                c.set(ox, y + 1, oz, Mat.IRON_BARS);
            }
            // Occasional break in the ramp itself.
            if (q % 23 != 0 && H.chance(H.h(hc, 100, q), 0.06)) q += 3;
        }
        // Bridges from the ramp out into the void.
        int nb = H.range(H.h(hc, 33), 3, 7);
        for (int q = 0; q < nb; q++) {
            long hq = H.h(hc, 34, q);
            double a = H.u(hq) * Math.PI * 2;
            int y = base + H.range(H.h(hq, 1), 30, hh - 30);
            int rx = (int) (cx + Math.cos(a) * R), rz = (int) (cz + Math.sin(a) * R);
            int len = H.range(H.h(hq, 2), 30, 110);
            int tx = (int) (cx + Math.cos(a) * (R + len)), tz = (int) (cz + Math.sin(a) * (R + len));
            c.beam(rx, y, rz, tx, y - 8, tz, 2, Mat.S_DARK);
            c.beam(tx, y - 8, tz, tx, y - 40, tz, 1, Mat.CABLE);
            c.set(rx, y + 1, rz, Mat.LAMP_GREEN);
        }
        // Crown: a platform with beacons at the top of the stem.
        c.cylinder(cx, cz, R * 0.75, R * 0.32, 1, 1, base + hh, base + hh + 2, 0, 0, Mat.S_DARK);
        c.set((int) cx, base + hh + 3, (int) cz, Mat.LAMP_RED);
        c.set((int) cx, base + hh + 4, (int) cz, Mat.SEA_LANTERN);
    }
}
