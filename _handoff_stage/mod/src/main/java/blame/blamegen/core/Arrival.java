package blame.blamegen.core;

/**
 * The arrival: a pale sand beach and shallow bay at the bottom of an immense shaft that is
 * open to the sky. The shaft wall (an irregular ring, ~160 blocks radius, 44 thick) rises
 * far beyond view except for one notch, a breach that exposes the dark lattice behind it,
 * and a colossal gate to the north that leads onto a narrow causeway into the megastructure.
 * Thin catwalks and a broken ring cross the sky above the beach as scale references.
 *
 * Fixed design: it does not depend on the world seed, so every world arrives the same way.
 */
public final class Arrival {
    public static final int SPAWN_X = 0, SPAWN_Z = 64;
    static final int W = 44;
    static final int FOUND_BOT = -96;
    static final int SEA = 62;
    static final int GATE_HALF = 26, GATE_FLOOR = 65, GATE_TOP = 295;
    static final int CAUSEWAY_END = -500;

    private final Megastructure m;
    private final long seed = 0x0B1A3EL; // fixed so the arrival is identical in every world
    private final Face face = new Face(seed);

    Arrival(Megastructure m) {
        this.m = m;
    }

    static double radius(double theta) {
        // Expanded ring wall for a massive coastal expanse
        return 280 + 18 * Math.sin(3 * theta + 0.7) + 12 * Math.sin(5 * theta + 2.1) + 8 * Math.sin(7 * theta + 1.3);
    }

    double bayE(int x, int z) {
        // Broad elliptical bay: the shoreline sits well clear of spawn so the
        // beach reads as a vast coastal expanse, not a cove.
        double ex = x / 230.0, ez = (z + 110) / 165.0;
        return ex * ex + ez * ez + (H.noise2(seed ^ 0xB1, x, z, 48) - 0.5) * 0.25;
    }

    /** Y of the topmost solid block of the beach / seabed at (x, z). */
    public int surface(int x, int z) {
        double e = bayE(x, z);
        if (e < 1) {
            int depth = 1 + (int) (11 * Math.pow(1 - e, 0.6));
            return SEA - depth;
        }
        double s = 63 + Math.min(4.0, (e - 1) * 5) + (H.noise2(seed ^ 0xB2, x, z, 22) - 0.5) * 1.6;
        double r = Math.sqrt((double) x * x + (double) z * z);
        double rt = radius(Math.atan2(z, x));
        if (r > rt - 18) s += (r - (rt - 18)) * 0.25;
        return (int) Math.floor(s);
    }

    public int spawnY() {
        return surface(SPAWN_X, SPAWN_Z) + 1;
    }

    static boolean inArc(double theta, double a0, double a1) {
        double t = theta;
        while (t < a0) t += 2 * Math.PI;
        while (t >= a0 + 2 * Math.PI) t -= 2 * Math.PI;
        return t <= a1;
    }

    int wallTop(double theta) {
        if (inArc(theta, -1.10, -0.55)) {
            int step = (int) Math.floor((theta + 1.10) * 160 / 9);
            return 230 + H.range(H.h(seed, 11, step), -6, 22);
        }
        return 1600 + (int) (300 * Math.sin(2 * theta));
    }

    boolean breach(double theta, int y) {
        if (y < 118 || y > 380) return false;
        int yb = Math.floorDiv(y, 6);
        double a0 = -0.12 + H.range(H.h(seed, 12, yb), 0, 3) * 0.01;
        double a1 = 0.20 - H.range(H.h(seed, 13, yb), 0, 3) * 0.01;
        if (y > 330) {
            double t = (y - 330) / 50.0;
            double mid = (a0 + a1) / 2, half = (a1 - a0) / 2 * (1 - t);
            a0 = mid - half;
            a1 = mid + half;
        }
        return theta >= a0 && theta <= a1;
    }

    public void draw(Cube c) {
        int cx = Math.floorDiv(c.x0 + Megastructure.CELL / 2, Megastructure.CELL);
        int cz = Math.floorDiv(c.z0 + Megastructure.CELL / 2, Megastructure.CELL);
        if (cx != 0 || cz != 0) return;
        if (c.y0 + 15 < FOUND_BOT - 200) return;
        columns(c);
        oceanSpires(c); // Massive spires in the bay/ocean
        gate(c);
        causeway(c);
        ladder(c);
        skyStructures(c);
        skyArches(c);
        pier(c);
    }

    private void columns(Cube c) {
        for (int z = c.z0; z < c.z0 + 16; z++)
            for (int x = c.x0; x < c.x0 + 16; x++) {
                double r = Math.sqrt((double) x * x + (double) z * z) + 0.5;
                double theta = Math.atan2(z + 0.5, x + 0.5);
                double rt = radius(theta);
                if (r >= rt + W + 3) continue;
                double arc = (theta + Math.PI) * 160;
                int top = wallTop(theta);
                // A facade column only where blocks actually fall in the face band, which keeps
                // the per-block cost to the wall ring instead of the whole shaft floor.
                Face.Col fc = (r >= rt - 60 && r < rt + 34) ? face.new Col(theta) : null;
                if (r < rt) {
                    int s = surface(x, z);
                    double e = bayE(x, z);
                    for (int y = c.y0; y < c.y0 + 16; y++) {
                        byte v;
                        if (y > s) {
                            v = (y <= SEA && e < 1) ? Mat.WATER : Mat.AIR;
                            if (fc != null && y <= top - 6 && y > s + 1 && !breach(theta, y)) {
                                // Sculpted inner face: ribs, niches, terraces, slots, fins.
                                int pk = fc.at(y);
                                if (pk != 0) {
                                    int push = Face.push(pk);
                                    byte fm = Face.mat(pk);
                                    double depthR = rt - Math.min(push, 56);
                                    if (r >= depthR - 0.9) {
                                        if (fm == Mat.AIR) v = Mat.AIR;
                                        else if (Face.lamp(pk) && r < depthR - 1.5) v = Mat.SEA_LANTERN;
                                        else v = fm;
                                        if (v != Mat.AIR && r > rt - 2 && fm == Mat.AIR) v = Mat.AIR;
                                    }
                                }
                            }
                            if (y >= 160 && y <= top && r >= rt - 5 && Math.floorMod(y - 160, 96) < 3 && !breach(theta, y)) v = Mat.S_DARK;
                        } else if (y >= s - 3) {
                            v = (e < 1 && H.noise2(seed ^ 0xB3, x, z, 9) > 0.62) ? Mat.GRAVEL : Mat.SAND;
                            if (y == s && e > 1.1 && e < 1.4 && (H.h(seed, 14, x, z) & 127) == 0) v = Mat.SEA_LANTERN;
                        } else if (y >= s - 8) {
                            v = Mat.SANDSTONE;
                        } else if (y >= FOUND_BOT) {
                            v = Mat.S_PALE;
                        } else {
                            if (y >= FOUND_BOT - 3 && Math.floorMod(x, 16) < 2) c.set(x, y, z, Mat.S_DARK);
                            continue;
                        }
                        c.set(x, y, z, v);
                    }
                    if (FOUND_BOT >= c.y0 && FOUND_BOT < c.y0 + 16 && Math.floorMod(x, 32) == 8 && Math.floorMod(z, 32) == 8)
                        c.set(x, FOUND_BOT, z, Mat.SEA_LANTERN);
                    openings(c, x, z, r, rt, theta, arc, top);
                    // Settlement ruins on the beach
                    settlementRuins(c, x, z, r, rt, theta, s, e);
                } else {
                    // Wall ring.
                    for (int y = c.y0; y < c.y0 + 16; y++) {
                        if (y < FOUND_BOT - 3) continue;
                        if (y < FOUND_BOT) {
                            if (Math.floorMod(x, 16) < 2) c.set(x, y, z, Mat.S_DARK);
                            continue;
                        }
                        if (y > top) continue;
                        if (r >= rt + W) {
                            // Outer face: buttresses every ~34 blocks of arc.
                            if (Math.floorMod((int) (arc * (rt + W) / 160), 34) < 5) c.set(x, y, z, Mat.S_MASS);
                            // Settlement ruins at base of outer wall
                            if (y == FOUND_BOT && H.chance(H.h(seed, 0xDEAD, x, z), 0.005)) {
                                long hr = H.h(seed, 0xDEAD, x, z);
                                int h = H.range(hr, 2, 6);
                                for (int yy = y + 1; yy <= y + h && yy < c.y0 + 16; yy++) {
                                    c.set(x, yy, z, H.chance(H.h(hr, 10 + yy), 0.6) ? Mat.S_MASS : Mat.S_DARK);
                                    if (yy == y + 1 && H.chance(H.h(hr, 20), 0.3)) c.set(x, yy, z, Mat.REBAR);
                                }
                            }
                            continue;
                        }
                        byte v = Mat.S_MASS;
                        if (breach(theta, y)) v = Mat.AIR;
                        else if (y >= top - 3) v = Mat.S_DARK;
                        else if (y >= 163 && r >= rt + 18 && r < rt + 21) {
                            int lv = Math.floorMod(y - 163, 32);
                            if (lv < 3) v = Mat.AIR;
                            else if (lv == 3 && Math.floorMod((int) arc, 12) == 0 && r < rt + 19.5) v = Mat.GLOWSTONE;
                        }
                        // Sculpted outer face: the same facade family, recessed rather than raised.
                        if (fc != null && y <= top - 6 && y > FOUND_BOT + 4 && v != Mat.AIR) {
                            int pk = fc.at(y);
                            if (pk != 0) {
                                int push = Face.push(pk);
                                byte fm = Face.mat(pk);
                                double depthR = rt + Math.min(push, 46);
                                if (fm == Mat.AIR) {
                                    if (r <= depthR) v = Mat.AIR;
                                } else if (r <= depthR && r > rt + W - 6) {
                                    v = fm == Mat.S_DECK ? Mat.S_DARK : fm;
                                }
                            }
                        }
                        // Radial passages from ledges into the ring corridor.
                        if (v != Mat.AIR && y >= 163 && Math.floorMod(y - 163, 96) < 3 && r < rt + 19
                                && Math.floorMod((int) arc, 70) < 3) v = Mat.AIR;
                        c.set(x, y, z, v);
                    }
                    openings(c, x, z, r, rt, theta, arc, top);
                }
            }
    }

    /** Dark recesses in the inner face of the shaft wall, some with a lamp at the back. */
    private void openings(Cube c, int x, int z, double r, double rt, double theta, double arc, int top) {
        if (r < rt - 3 || r > rt + 23) return;
        int ab = (int) Math.floor(arc / 22);
        int la = (int) Math.floor(arc) - ab * 22;
        if (la < 4 || la > 13) return;
        for (int y = Math.max(c.y0, 90); y < c.y0 + 16 && y < top - 10; y++) {
            int yb = Math.floorDiv(y - 90, 34), ly = Math.floorMod(y - 90, 34);
            if (ly < 6 || ly > 21) continue;
            long ho = H.h(seed, 20, ab, yb);
            if (!H.chance(ho, 0.07)) continue;
            int depth = H.range(H.h(ho, 1), 8, 22);
            if (r > rt + depth) {
                if (r < rt + depth + 1 && la == 8 && ly == 13 && H.chance(H.h(ho, 2), 0.35)) c.set(x, y, z, Mat.SEA_LANTERN);
                continue;
            }
            c.set(x, y, z, Mat.AIR);
        }
    }

    // ========================================================================
    // SETTLEMENT RUINS: human habitation remnants at base of wall & on beach
    // ========================================================================
    private void settlementRuins(Cube c, int x, int z, double r, double rt, double theta, int s, double e) {
        // Only on dry land near the shoreline, clear of the wall and the water.
        if (e < 1.0 || e > 1.6) return;
        if (r > rt - 4) return; // too close to wall
        if (s < SEA - 1 || s > SEA + 6) return; // only near sea level

        long hr = H.h(seed, 0xDEAD, x, z);
        double ruinChance = (1.6 - e) * 0.010;
        if (!H.chance(hr, ruinChance)) return;

        int type = H.range(H.h(hr, 1), 0, 6);
        int h = H.range(H.h(hr, 2), 3, 10);

        // Build upwards from surface
        for (int y = s + 1; y <= s + h && y < c.y0 + 16; y++) {
            switch (type) {
                case 0: // Collapsed dome dwelling
                    int rr = H.range(H.h(hr, 10), 2, 4);
                    if (y <= s + h * 2 / 3) {
                        c.box(x - rr, y, z - rr, x + rr, y, z + rr, Mat.S_PALE);
                        if (y == s + 1) c.box(x - rr, y, z - rr, x + rr, y, z + rr, Mat.SANDSTONE);
                    } else {
                        double t = (double) (y - s - h * 2 / 3) / (h / 3.0);
                        int cr = (int) (rr * (1 - t));
                        if (cr > 0) c.box(x - cr, y, z - cr, x + cr, y, z + cr, Mat.S_PALE);
                    }
                    if (y == s + 1) c.set(x, y, z, Mat.LAMP_AMBER);
                    break;
                case 1: // Rectangular foundation with corner posts
                    if (y == s + 1) {
                        c.box(x - 2, y, z - 2, x + 2, y, z + 2, Mat.SANDSTONE);
                        for (int dx = -2; dx <= 2; dx += 4)
                            for (int dz = -2; dz <= 2; dz += 4)
                                c.set(x + dx, y + 1, z + dz, Mat.S_DARK);
                    } else if (y < s + h - 1) {
                        for (int dx = -2; dx <= 2; dx += 4)
                            for (int dz = -2; dz <= 2; dz += 4)
                                c.set(x + dx, y, z + dz, Mat.S_DARK);
                    } else {
                        // Roof remnants
                        c.box(x - 3, y, z - 3, x + 3, y, z + 3, Mat.S_MASS);
                        c.set(x, y + 1, z, Mat.REBAR);
                    }
                    break;
                case 2: // Vertical pipe/conduit cluster (utility)
                    c.box(x - 1, y, z - 1, x + 1, y, z + 1, Mat.PIPE);
                    if (y == s + h) c.set(x, y + 1, z, Mat.LAMP_RED);
                    break;
                case 3: // Stairwell descending into sand
                    if (y == s + 1) c.box(x - 1, y, z - 1, x + 1, y, z + 1, Mat.SLAB_TOP);
                    if (y > s + 1 && y < s + h) c.set(x, y, z, Mat.AIR);
                    if (y == s + 2) c.set(x, y - 1, z, Mat.LAMP_GREEN);
                    break;
                case 5: // Shelter interior: lockers, bunk, terminal, crates (§11)
                    if (y == s + 1) {
                        c.box(x - 3, y, z - 2, x + 3, y, z + 2, Mat.SANDSTONE);
                        // Locker row along the back.
                        c.box(x - 3, y + 1, z + 1, x - 1, y + 2, z + 2, Mat.IRON_BLOCK);
                        c.box(x - 3, y + 1, z + 1, x - 3, y + 2, z + 1, Mat.S_DARK);
                        // Bunk + workbench.
                        c.box(x + 1, y + 1, z - 2, x + 3, y + 1, z - 1, Mat.SLAB_BOTTOM);
                        c.box(x + 1, y + 1, z + 1, x + 2, y + 1, z + 2, Mat.DOUBLE_SLAB);
                        // Terminal with a dark screen and one live lamp.
                        c.set(x, y + 1, z, Mat.IRON_BLOCK);
                        c.set(x, y + 2, z, Mat.GLASS_REINFORCED);
                        c.set(x + 1, y + 2, z, Mat.LAMP_GREEN);
                        // Crate stack.
                        c.set(x - 3, y + 1, z - 2, Mat.DOUBLE_SLAB);
                        c.set(x - 3, y + 2, z - 2, Mat.DOUBLE_SLAB);
                    } else if (y == s + 2) {
                        // Partial roof: two slabs and open sky between.
                        c.box(x - 3, y + 1, z - 2, x, y + 1, z + 2, Mat.S_PALE);
                        c.set(x + 2, y + 1, z, Mat.REBAR);
                    }
                    break;
                case 6: // Hab shell: three walls, half a roof, one lamp (§11)
                    if (y == s + 1) {
                        c.box(x - 3, y, z - 2, x + 3, y, z + 2, Mat.SANDSTONE);
                    } else if (y <= s + 3) {
                        c.box(x - 3, y, z - 2, x - 3, y, z + 2, Mat.S_PALE);
                        c.box(x + 3, y, z - 2, x + 3, y, z + 2, Mat.S_PALE);
                        c.box(x - 3, y, z + 2, x + 3, y, z + 2, Mat.S_PALE);
                        if (y == s + 3) {
                            c.box(x - 3, y + 1, z, x + 1, y + 1, z + 2, Mat.S_PALE);
                            c.set(x - 1, y, z, Mat.LAMP_AMBER);
                        }
                    }
                    break;
                default: // Scattered debris / rubble pile
                    c.set(x, y, z, H.chance(H.h(hr, 20 + y), 0.6) ? Mat.S_MASS : Mat.S_DARK);
                    if (y == s + 1 && H.chance(H.h(hr, 30), 0.2)) c.set(x, y, z, Mat.REBAR);
                    break;
            }
        }
    }

    // ========================================================================
    // OCEAN SPIRES: massive monolithic columns rising from the bay/ocean
    // ========================================================================
    private void oceanSpires(Cube c) {
        if (c.y0 + 15 < SEA - 10) return;
        if (c.y0 > SEA + 500) return;

        // Spire positions across the bay water, clear of spawn, pier and gate axis.
        int[][] spires = {
            {-150, -60, 16}, {140, -40, 20}, {-60, -180, 14}, {80, -190, 18},
            {-170, -30, 12}, {180, -20, 15}, {60, -130, 22}, {-110, -120, 13}
        };

        for (int[] sp : spires) {
            int sx = sp[0], sz = sp[1], sr = sp[2];
            if (!c.hits(sx - sr - 5, SEA - 20, sz - sr - 5, sx + sr + 5, SEA + 500, sz + sr + 5)) continue;

            long hs = H.h(seed, 0xBEEF, sx, sz);
            int sTop = SEA + H.range(H.h(hs, 1), 120, 400);

            // Tapered monolithic column rising from seabed
            c.taper(sx + 0.5, sz + 0.5, sr, Math.max(2, sr / 4), SEA - 20, sTop, 6, Mat.S_COLUMN);

            // Weathered bands and copper patina streaks
            for (int y = SEA + H.range(H.h(hs, 2), 0, 20); y < sTop; y += H.range(H.h(hs, 3), 16, 48)) {
                if (c.hitsY(y - 3, y + 3)) {
                    c.cylinder(sx + 0.5, sz + 0.5, sr + 2, sr - 1, 1, 1, y, y + 2, 0, 0,
                               H.chance(H.h(hs, 4, y), 0.5) ? Mat.COPPER_PATINA : Mat.COPPER_WEATHERED);
                }
            }

            // Internal spiral stair
            c.box(sx - 1, SEA - 10, sz - 1, sx + 1, sTop, sz + 1, Mat.AIR);
            for (int y = SEA - 10; y < sTop; y += 2) {
                double a = (y - SEA + 10) * Math.PI / 8;
                int lx = (int) Math.floor(sx + Math.cos(a) * 1.5);
                int lz = (int) Math.floor(sz + Math.sin(a) * 1.5);
                c.set(lx, y, lz, Mat.LADDER_N);
            }

            // Platform at top with warning lights
            c.box(sx - sr - 3, sTop - 2, sz - sr - 3, sx + sr + 3, sTop + 2, sz + sr + 3, Mat.S_DARK);
            c.box(sx - sr, sTop, sz - sr, sx + sr, sTop, sz + sr, Mat.DOUBLE_SLAB);
            c.set(sx, sTop + 1, sz, Mat.LAMP_RED);
            c.set(sx, sTop + 2, sz, Mat.LAMP_AMBER);
            c.set(sx, sTop + 3, sz, Mat.LAMP_GREEN);

            // Cable stays to other spires / wall
            if (H.chance(H.h(hs, 5), 0.4)) {
                int tx = H.range(H.h(hs, 6), -400, 400);
                int tz = H.range(H.h(hs, 7), -400, 400);
                int ty = SEA + H.range(H.h(hs, 8), 60, 200);
                c.beam(sx, sTop, sz, tx, ty, tz, 1, Mat.CABLE);
            }
        }
    }

    private int gateInnerZ() {
        return -(int) Math.floor(radius(-Math.PI / 2));
    }

    private void gate(Cube c) {
        int zi = gateInnerZ();
        int zo = zi - W - 3;
        if (!c.hits(-60, 40, zo - 2, 60, 620, zi + 40)) return;
        // Piers projecting into the shaft, rising far above the opening, grooved.
        for (int s = -1; s <= 1; s += 2) {
            int xa = s < 0 ? -52 : GATE_HALF + 1, xb = s < 0 ? -GATE_HALF - 1 : 52;
            c.box(xa, 40, zi - 4, xb, 610, zi + 34, Mat.S_MASS);
            for (int x = xa + 3; x <= xb - 3; x += 6) c.box(x, 70, zi + 33, x, 600, zi + 34, Mat.AIR);
            for (int z = zi + 2; z <= zi + 30; z += 7) {
                int fx = s < 0 ? xa : xb;
                c.box(fx, 70, z, fx, 600, z, Mat.AIR);
            }
            // A thin vertical line of lamps on each pier face toward the beach.
            int lx = s < 0 ? -40 : 40;
            for (int y = 110; y <= 590; y += 40) c.set(lx, y, zi + 34, Mat.SEA_LANTERN);
        }
        c.box(-52, GATE_TOP + 1, zi - 4, 52, GATE_TOP + 36, zi + 34, Mat.S_DARK);
        // Opening through the piers and the wall.
        c.box(-GATE_HALF, GATE_FLOOR + 1, zo, GATE_HALF, GATE_TOP, zi + 36, Mat.AIR);
        c.box(-GATE_HALF, GATE_FLOOR - 3, zo, GATE_HALF, GATE_FLOOR, zi + 36, Mat.S_DARK);
        c.box(-GATE_HALF, GATE_FLOOR, zo, GATE_HALF, GATE_FLOOR, zi + 36, Mat.DOUBLE_SLAB);
        // Ramp of sand up to the threshold so it is walkable from the beach.
        for (int z = zi + 36; z <= zi + 52; z++)
            for (int x = -GATE_HALF; x <= GATE_HALF; x++) {
                int s = surface(x, z);
                for (int y = s + 1; y <= GATE_FLOOR; y++) c.set(x, y, z, Mat.SAND);
            }
        // Lamps along both sides of the passage at human height.
        for (int z = zo + 2; z <= zi + 34; z += 12) {
            c.set(-GATE_HALF - 1, GATE_FLOOR + 4, z, Mat.SEA_LANTERN);
            c.set(GATE_HALF + 1, GATE_FLOOR + 4, z, Mat.SEA_LANTERN);
        }
    }

    private void causeway(Cube c) {
        int zs = gateInnerZ() - W - 3;
        int ze = CAUSEWAY_END;
        if (!c.hits(-40, -260, ze, 40, 90, zs)) return;
        c.box(-6, GATE_FLOOR - 3, ze, 6, GATE_FLOOR, zs, Mat.S_DARK);
        c.box(-5, GATE_FLOOR, ze, 5, GATE_FLOOR, zs, Mat.DOUBLE_SLAB);
        c.box(-5, GATE_FLOOR + 1, ze, 5, GATE_FLOOR + 8, zs, Mat.AIR);
        c.box(-6, GATE_FLOOR + 1, ze, -6, GATE_FLOOR + 1, zs, Mat.IRON_BARS);
        c.box(6, GATE_FLOOR + 1, ze, 6, GATE_FLOOR + 1, zs, Mat.IRON_BARS);
        for (int z = zs - Math.floorMod(zs, 16); z >= ze; z -= 16) {
            c.set(-6, GATE_FLOOR + 2, z, Mat.END_ROD_UP);
            c.set(6, GATE_FLOOR + 2, z, Mat.END_ROD_UP);
        }
        for (int z = zs - 20; z >= ze + 20; z -= 48) c.box(-1, -180, z - 1, 1, GATE_FLOOR - 4, z + 1, Mat.S_DARK);
        // Landing platform with a ladder shaft down into the lattice below.
        int lz0 = ze, lz1 = ze + 60;
        c.box(-34, GATE_FLOOR - 9, lz0, 34, GATE_FLOOR, lz1, Mat.S_DECK);
        c.box(-34, GATE_FLOOR, lz0, 34, GATE_FLOOR, lz1, Mat.DOUBLE_SLAB);
        c.box(-34, GATE_FLOOR + 1, lz0, 34, GATE_FLOOR + 12, lz1, Mat.AIR);
        for (int x = -34; x <= 34; x += 17) c.set(x, GATE_FLOOR + 1, lz0, Mat.END_ROD_UP);
        int tz = ze + 30, bottom = -200;
        c.box(-2, bottom, tz - 2, 2, GATE_FLOOR, tz + 2, Mat.S_MASS);
        c.box(-1, bottom, tz - 1, 1, GATE_FLOOR + 1, tz + 1, Mat.AIR);
        c.box(0, bottom + 1, tz - 1, 0, GATE_FLOOR, tz - 1, Mat.LADDER_S);
        c.box(-1, GATE_FLOOR + 1, tz - 2, 1, GATE_FLOOR + 1, tz + 2, Mat.IRON_BARS);
        c.box(-1, GATE_FLOOR + 1, tz - 1, 1, GATE_FLOOR + 1, tz - 1, Mat.AIR);
        c.box(-1, bottom - 1, tz - 3, 1, bottom - 1, tz + 3, Mat.S_DARK);
        for (int y = bottom + 8; y < GATE_FLOOR; y += 24) c.set(1, y, tz + 2, Mat.GLOWSTONE);
    }

    private void ladder(Cube c) {
        int zs = (int) Math.floor(radius(Math.PI / 2));
        int s = surface(0, zs - 2);
        if (!c.hits(-2, s - 2, zs - 6, 2, 170, zs + 24)) return;
        c.box(-1, s + 1, zs - 4, 1, 162, zs - 1, Mat.AIR);
        c.box(0, s + 1, zs, 0, 165, zs, Mat.S_MASS);
        c.box(0, s + 1, zs - 1, 0, 162, zs - 1, Mat.LADDER_N);
        c.box(-1, 163, zs - 1, 1, 165, zs + 20, Mat.AIR);
        c.set(0, 167, zs - 1, Mat.SEA_LANTERN);
        c.set(-1, s + 2, zs - 1, Mat.SEA_LANTERN);
    }

    private int wallHit(int along, boolean xAxis, int offset, int sign) {
        for (int t = 0; t < 420; t++) {
            int x = xAxis ? sign * t : offset, z = xAxis ? offset : sign * t;
            double r = Math.sqrt((double) x * x + (double) z * z);
            if (r >= radius(Math.atan2(z, x))) return sign * t;
        }
        return sign * 420;
    }

    private void skyStructures(Cube c) {
        // Catwalks across the shaft: y, axis (true = runs along x), offset.
        int[][] cw = {{150, 1, -60}, {205, 0, 40}, {290, 1, 25}, {480, 0, -70}, {760, 1, 90}, {1100, 0, 10}};
        for (int[] w : cw) {
            int y = w[0], off = w[2];
            boolean xAxis = w[1] == 1;
            if (!c.hits(-240, y - 130, -240, 240, y + 4, 240)) continue;
            int a = wallHit(0, xAxis, off, -1) - 3, b = wallHit(0, xAxis, off, 1) + 3;
            if (xAxis) m.catwalkX(c, a, b, off, y);
            else m.catwalkZ(c, a, b, off, y);
            for (int t = a + 12; t < b - 12; t += 23) {
                long hq = H.h(seed, 30, y, t);
                if (!H.chance(hq, 0.6)) continue;
                int len = H.range(H.h(hq, 1), 12, Math.min(120, y - 90));
                int side = H.chance(H.h(hq, 2), 0.5) ? -2 : 2;
                if (xAxis) c.box(t, y - len, off + side, t, y - 2, off + side, Mat.IRON_BARS);
                else c.box(off + side, y - len, t, off + side, y - 2, t, Mat.IRON_BARS);
            }
        }
        // Broken ring cantilevered from the west wall.
        int ry = 232;
        if (c.hits(-240, ry - 130, -240, 240, ry + 8, 240)) {
            for (int z = c.z0; z < c.z0 + 16; z++)
                for (int x = c.x0; x < c.x0 + 16; x++) {
                    double theta = Math.atan2(z + 0.5, x + 0.5);
                    if (!inArc(theta, 2.3, 2.3 + 2.25)) continue;
                    double r = Math.sqrt((double) x * x + (double) z * z) + 0.5;
                    double rt = radius(theta);
                    // Ragged inner edge.
                    int step = (int) Math.floor((theta + Math.PI) * 160 / 7);
                    double inner = rt - 56 + H.range(H.h(seed, 40, step), -3, 3);
                    double t = (theta - 2.3 + 2 * Math.PI) % (2 * Math.PI);
                    if (t > 2.0) inner += (t - 2.0) / 0.25 * 40; // tapering broken end
                    if (r < inner || r >= rt + 2) continue;
                    for (int y = ry; y <= ry + 7; y++) c.set(x, y, z, Mat.S_MASS);
                    c.set(x, ry + 8, z, Mat.DOUBLE_SLAB);
                    double arc = (theta + Math.PI) * 160;
                    if (Math.floorMod((int) arc, 14) < 2) for (int y = ry - 5; y < ry; y++) c.set(x, y, z, Mat.S_DARK);
                    if (r < inner + 1 && Math.floorMod((int) arc, 9) == 0) {
                        long hq = H.h(seed, 41, (int) arc);
                        int len = H.range(hq, 10, 110);
                        for (int y = ry - len; y < ry; y++) c.set(x, y, z, Mat.IRON_BARS);
                        if (H.chance(H.h(hq, 1), 0.3)) c.set(x, ry - len - 1, z, Mat.SEA_LANTERN);
                    }
                }
        }
    }

    // ========================================================================
    // SKY ARCHES: colossal broken arches spanning the shaft above the beach.
    // These are the strongest scale reference at spawn: they read as ruined
    // monument, not as part of the lattice grid.
    // ========================================================================
    private void skyArches(Cube c) {
        double[] phi = {0.30, 0.95, 1.85, 2.60, 3.55, 4.40};
        int[] cy = {95, 230, 410, 650, 980, 1420};
        for (int q = 0; q < phi.length; q++) {
            long ha = H.h(seed, 0xA4C, q);
            double ux = Math.cos(phi[q]), uz = Math.sin(phi[q]);
            double rr = radius(phi[q]) + 14, rl = radius(phi[q] + Math.PI) + 14;
            int vert = H.range(H.h(ha, 1), 150, 340);
            int t = H.range(H.h(ha, 2), 4, 9);
            int base = cy[q];
            // Up to two gaps break the arch, so it reads as ruin rather than monument.
            double g0 = 0.25 + H.u(H.h(ha, 3)) * 2.6, gw0 = 0.20 + H.u(H.h(ha, 4)) * 0.5;
            double g1 = H.u(H.h(ha, 5)) * Math.PI, gw1 = 0.12 + H.u(H.h(ha, 6)) * 0.35;
            if (!c.hits((int) -Math.max(rr, rl) - t, base - t - 4, (int) -Math.max(rr, rl) - t,
                    (int) Math.max(rr, rl) + t, base + vert + t + 4, (int) Math.max(rr, rl) + t)) continue;
            // Springers: each arch springs from the ring wall, so its ends are embedded.
            int xa0 = -(int) rr, za0 = 0, xa1 = -(int) rl, za1 = 0;
            // Span + legs.
            int steps = 28;
            for (int q2 = 0; q2 <= steps; q2++) {
                double f = (double) q2 / steps;
                int px = (int) (xa0 + (xa1 - xa0) * f);
                int pz = (int) (za0 + (za1 - za0) * f) + (int) (ux * f * 0 + 0);
                double arch = Math.sin(Math.PI * f);
                int h = (int) (vert * arch);
                for (int dy = 0; dy < t; dy++)
                    for (int dxx = -t / 2; dxx <= t / 2; dxx++) {
                        int bx = px + dxx;
                        if (bx < c.x0 - 1 || bx > c.x0 + 16) continue;
                        c.set(bx, base + h + dy, pz + dxx, h > vert * 0.05 ? Mat.S_DARK : Mat.S_MASS);
                    }
                // Gaps.
                if (f > g0 && f < g0 + gw0) {
                    for (int dy = 0; dy < t + 1; dy++)
                        c.set(px, base + h + dy, pz, Mat.AIR);
                }
                if (f > g1 && f < g1 + gw1) c.set(px, base + h, pz, Mat.AIR);
            }
            // Legs down from the springers.
            c.box(xa0 - t, base - t, za0 - t, xa0 + t, base, za0 + t, Mat.S_MASS);
            c.box(xa1 - t, base - t, za1 - t, xa1 + t, base, za1 + t, Mat.S_MASS);
            // Cables hanging from the crown, and lanterns at the springers.
            for (int q3 = 0; q3 < 3; q3++) {
                long hc2 = H.h(ha, 20 + q3);
                double f = 0.3 + H.u(H.h(hc2, 1)) * 0.4;
                int px = (int) (xa0 + (xa1 - xa0) * f);
                int len = H.range(H.h(hc2, 2), 20, Math.min(120, base + vert * (int) (Math.sin(Math.PI * f)) - 90));
                if (len > 6) {
                    c.box(px, base + vert * (int) (Math.sin(Math.PI * f)) - len, za0,
                            px, base + vert * (int) (Math.sin(Math.PI * f)) - 2, za0, Mat.IRON_BARS);
                    if (H.chance(H.h(hc2, 3), 0.4))
                        c.set(px, base + vert * (int) (Math.sin(Math.PI * f)) - len - 1, za0, Mat.SEA_LANTERN);
                }
            }
            c.set(xa0, base + 2, za0, Mat.LAMP_AMBER);
            c.set(xa1, base + 2, za1, Mat.LAMP_AMBER);
        }
    }

    private void pier(Cube c) {
        int x = -40, za = 0, zb = 34;
        if (!c.hits(x - 2, 40, za, x + 2, 70, zb)) return;
        c.box(x - 1, 63, za, x + 1, 63, zb, Mat.DOUBLE_SLAB);
        c.box(x - 1, 64, za, x + 1, 66, zb, Mat.AIR);
        c.box(x - 2, 64, za, x - 2, 64, zb, Mat.IRON_BARS);
        c.box(x + 2, 64, za, x + 2, 64, zb, Mat.IRON_BARS);
        c.box(x - 2, 64, za, x + 2, 64, za, Mat.IRON_BARS);
        for (int z = za; z <= zb; z += 8) {
            c.box(x - 1, 48, z, x - 1, 62, z, Mat.S_DARK);
            c.box(x + 1, 48, z, x + 1, 62, z, Mat.S_DARK);
        }
        c.set(x + 2, 65, za, Mat.END_ROD_UP);
        c.set(x - 2, 65, za, Mat.END_ROD_UP);
    }
}
