package blame.blamegen.core;

/**
 * TOHA HEAVY INDUSTRIES — authored monument, not a district generator.
 *
 * A single deterministic architectural composition centred at (0, -90000):
 * a 6.1 km precinct, a 3.9 km-wide stepped factory-fortress, a 2 km engine
 * stack, thirteen cave mouths, a hollow central nave, exterior machine spines,
 * and a five-frame processional causeway. All proportions and placements are
 * fixed. Only the material weathering resolved by Cube remains seed-dependent.
 *
 * The forms are designed for Minecraft-scale sightlines: a broad dark plinth,
 * pale ruled shells, black setback bands, slit openings, enormous structural
 * braces, and a nearly-ceiling-high core. There is no random cell grid, random
 * district selection, random tower placement, or illuminated billboard.
 */
public final class Toha {
    public static final int CX = 0, CZ = -4608;
    public static final int HALF = 3072;
    public static final int X0 = CX - HALF, X1 = CX + HALF;
    public static final int Z0 = CZ - HALF, Z1 = CZ + HALF;

    private static final int GROUND = 64;
    public static final int TOP = 2030;
    private static final int NAVE_X = 1050;
    private static final int NAVE_BACK = -1050;
    private static final int NAVE_FRONT = 250;

    // Thirteen fixed chambers: five at the foot, four in the second tier,
    // three in the third, and one isolated chamber in the crown.
    private static final int[] CAVE_X = {
        -1350, -690, 0, 690, 1350,
        -1100, -365, 365, 1100,
        -740, 0, 740,
        0
    };
    private static final int[] CAVE_Y = {
        150, 170, 130, 170, 150,
        670, 690, 690, 670,
        1050, 1025, 1050,
        1475
    };
    private static final int[] CAVE_W = {
        180, 260, 480, 260, 180,
        280, 260, 260, 280,
        260, 420, 260,
        300
    };
    private static final int[] CAVE_H = {
        260, 280, 500, 280, 260,
        250, 270, 270, 250,
        300, 360, 300,
        240
    };
    private static final int[] CAVE_FRONT = {
        1350, 1350, 1350, 1350, 1350,
        1130, 1130, 1130, 1130,
        850, 850, 850,
        630
    };

    Toha(Megastructure m) {
        // Intentionally no random geometry seed. The monument has one authored layout.
    }

    /** The authored precinct replaces the old 150 km procedural field. */
    boolean overlaps(Cube c) {
        return c.x0 + 15 >= X0 && c.x0 < X1
                && c.z0 + 15 >= Z0 && c.z0 < Z1;
    }

    void draw(Cube c) {
        if (c.y0 > TOP + 16 || c.y0 + 15 < 0) return;

        drawFoundation(c);
        drawOuterWalls(c);
        drawGatePortal(c);
        drawMainShell(c);
        drawFacade(c);
        drawTerraceBands(c);
        carveSlitApertures(c);
        carveNave(c);
        carveCaves(c);
        drawNaveStructure(c);
        drawGravityFurnace(c);
        drawCaveFrames(c);
        drawCaveAIControls(c);
        drawExteriorSpines(c);
        drawApproach(c);
        drawCourtyardPylons(c);
    }

    // ------------------------------------------------------------------
    // The precinct: a finite floor and low enclosure, leaving negative space
    // between the boundary and the main mass so the building reads as a whole.
    // ------------------------------------------------------------------

    private void drawFoundation(Cube c) {
        if (!c.hitsY(0, 63)) return;
        c.box(-2800, 0, CZ - 2800, 2800, 63, CZ + 2800, Mat.S_TOHA_DECK);

        // Three inset, unlit perimeter courses make the floor feel machined.
        if (c.hitsY(63, 63)) {
            c.box(-2720, 63, CZ - 2720, 2720, 63, CZ - 2704, Mat.S_DARK);
            c.box(-2720, 63, CZ + 2704, 2720, 63, CZ + 2720, Mat.S_DARK);
            c.box(-2720, 63, CZ - 2720, -2704, 63, CZ + 2720, Mat.S_DARK);
            c.box(2704, 63, CZ - 2720, 2720, 63, CZ + 2720, Mat.S_DARK);
        }
    }

    private void drawOuterWalls(Cube c) {
        int y0 = GROUND, y1 = 350;
        // Rear wall and two long flanks.
        c.box(-2750, y0, CZ - 2750, 2750, y1, CZ - 2716, Mat.S_TOHA_PALE);
        c.box(-2750, y0, CZ - 2716, -2716, y1, CZ + 2750, Mat.S_TOHA_MASS);
        c.box(2716, y0, CZ - 2716, 2750, y1, CZ + 2750, Mat.S_TOHA_MASS);

        // South wall is deliberately split around the processional entrance.
        c.box(-2750, y0, CZ + 2716, -700, y1, CZ + 2750, Mat.S_TOHA_PALE);
        c.box(700, y0, CZ + 2716, 2750, y1, CZ + 2750, Mat.S_TOHA_PALE);

        // Heavy, sparse buttresses on the walls. No perimeter clutter field.
        for (int i = 0; i < 5; i++) {
            int x = -2200 + i * 1100;
            c.box(x - 18, 350, CZ - 2755, x + 18, 405, CZ - 2710, Mat.S_TOHA_MASS);
            if (i != 2) {
                c.box(x - 18, 350, CZ + 2710, x + 18, 405, CZ + 2755, Mat.S_TOHA_MASS);
            }
        }
        for (int s = -1; s <= 1; s += 2) {
            for (int i = 0; i < 5; i++) {
                int z = CZ - 2200 + i * 1100;
                c.box(s * 2710 - 28, 350, z - 18, s * 2755 + 28, 405, z + 18, Mat.S_TOHA_MASS);
            }
        }
    }

    /**
     * The south perimeter opening is a portal, not a hole in a fence: two
     * deep side piers and a massive crosshead form the first compressed scale
     * cue before the larger factory mass comes into view.
     */
    private void drawGatePortal(Cube c) {
        int z0 = CZ + 2520, z1 = CZ + 2770;
        c.box(-725, GROUND, z0, -655, GROUND + 520, z1, Mat.S_TOHA_MASS);
        c.box(655, GROUND, z0, 725, GROUND + 520, z1, Mat.S_TOHA_MASS);
        c.box(-725, GROUND + 460, z0, 725, GROUND + 520, z1, Mat.S_TOHA_PALE);
        c.box(-700, GROUND + 438, z0 + 14, 700, GROUND + 452, z1 - 14, Mat.S_TOHA_MASS);

        // Two recessed vertical seams and one restrained central index mark.
        c.box(-711, GROUND + 70, z0 + 24, -705, GROUND + 420, z0 + 30, Mat.S_TOHA_RING);
        c.box(705, GROUND + 70, z0 + 24, 711, GROUND + 420, z0 + 30, Mat.S_TOHA_RING);
        c.box(-48, GROUND + 480, z1 + 1, 48, GROUND + 486, z1 + 3, Mat.BIO_CYAN);
    }

    // ------------------------------------------------------------------
    // Primary silhouette. These are authored volumes, nested and stepped,
    // with no per-cell district selection.
    // ------------------------------------------------------------------

    private void drawMainShell(Cube c) {
        // Broad footing: nearly 4 km wide, with a deep rear heel.
        c.box(-2050, 64, CZ - 2200, 2050, 200, CZ + 1600, Mat.S_TOHA_PALE);
        c.box(-2100, 180, CZ - 2220, 2100, 236, CZ + 1640, Mat.S_TOHA_MASS);

        // The lower black body, broad shoulder, pale middle mass, and receding crown.
        c.box(-1850, 200, CZ - 1900, 1850, 640, CZ + 1350, Mat.S_TOHA_MASS);
        c.box(-1940, 600, CZ - 1980, 1940, 632, CZ + 1420, Mat.S_TOHA_PALE);

        c.box(-1580, 640, CZ - 1700, 1580, 1020, CZ + 1130, Mat.S_TOHA_PALE);
        c.box(-1660, 980, CZ - 1770, 1660, 1018, CZ + 1200, Mat.S_TOHA_MASS);

        c.box(-1320, 1020, CZ - 1440, 1320, 1450, CZ + 850, Mat.S_TOHA_MASS);
        c.box(-1390, 1408, CZ - 1510, 1390, 1448, CZ + 920, Mat.S_TOHA_PALE);

        c.box(-1050, 1450, CZ - 1130, 1050, 1760, CZ + 630, Mat.S_TOHA_PALE);
        c.box(-1150, 1724, CZ - 1230, 1150, 1758, CZ + 710, Mat.S_TOHA_MASS);
        c.box(-800, 1760, CZ - 900, 800, 1880, CZ + 430, Mat.S_TOHA_MASS);

        // A thin, dark crown lip, intentionally close to the immense ceiling above.
        c.box(-900, 1870, CZ - 1010, 900, 1895, CZ + 530, Mat.S_TOHA_PALE);
        c.box(-620, 1895, CZ - 790, 620, 1920, CZ + 310, Mat.S_TOHA_MASS);

        // Rear counter-mass prevents this from reading as a flat stage façade.
        c.box(-970, 440, CZ - 2240, 970, 1600, CZ - 1860, Mat.S_TOHA_PALE);
        c.box(-1250, 760, CZ - 2280, 1250, 820, CZ - 1750, Mat.S_TOHA_MASS);
        c.box(-820, 1290, CZ - 2160, 820, 1760, CZ - 1810, Mat.S_TOHA_MASS);
    }

    private void drawFacade(Cube c) {
        // Thick vertical ribs and deep-set apertures are part of the silhouette,
        // not random decoration. Their absolute pitch makes neighbouring cubes agree.
        facadeBand(c, -1850, 1850, 200, 620, 1350);
        facadeBand(c, -1580, 1580, 640, 1000, 1130);
        facadeBand(c, -1320, 1320, 1020, 1410, 850);
        facadeBand(c, -1050, 1050, 1450, 1720, 630);

        // Narrow apertures are true void cuts applied after the surface bands.

        // Giant blind slots on both flanks continue the façade language around the mass.
        flankSlots(c, -1850, 1350, 200, 640);
        flankSlots(c, -1580, 1130, 640, 1020);
        flankSlots(c, -1320, 850, 1020, 1450);
    }

    private void facadeBand(Cube c, int xMin, int xMax, int yMin, int yMax, int front) {
        if (!c.hits(xMin, yMin, CZ + front, xMax, yMax, CZ + front + 24)) return;
        for (int x = Math.max(xMin, c.x0); x <= Math.min(xMax, c.x0 + 15); x++) {
            if (Math.floorMod(x - xMin, 112) >= 8) continue;
            c.box(x, yMin, CZ + front + 3, x + 5, yMax, CZ + front + 26, Mat.S_TOHA_MASS);
        }
    }

    private void flankSlots(Cube c, int halfWidth, int front, int yMin, int yMax) {
        int xL = -halfWidth, xR = halfWidth;
        for (int z = c.z0; z < c.z0 + 16; z++) {
            int dz = z - CZ;
            if (dz < -front - 200 || dz > front) continue;
            if (Math.floorMod(dz + 2200, 192) >= 18) continue;
            c.box(xL - 1, yMin + 45, z, xL + 3, yMax - 45, z + 1, Mat.COAL);
            c.box(xR - 3, yMin + 45, z, xR + 1, yMax - 45, z + 1, Mat.COAL);
        }
    }

    private void drawTerraceBands(Cube c) {
        rectBand(c, 1980, -2020, 1460, 612, 640, Mat.S_TOHA_MASS);
        rectBand(c, 1695, -1805, 1240, 992, 1018, Mat.S_TOHA_RING);
        rectBand(c, 1430, -1545, 960, 1414, 1448, Mat.S_TOHA_MASS);
        rectBand(c, 1190, -1260, 760, 1728, 1758, Mat.S_TOHA_RING);
    }

    private void rectBand(Cube c, int halfX, int back, int front, int y0, int y1, byte material) {
        int t = 24;
        c.box(-halfX, y0, CZ + back, -halfX + t, y1, CZ + front, material);
        c.box(halfX - t, y0, CZ + back, halfX, y1, CZ + front, material);
        c.box(-halfX, y0, CZ + back, halfX, y1, CZ + back + t, material);
        c.box(-halfX, y0, CZ + front - t, halfX, y1, CZ + front, material);
    }

    /**
     * Real, narrow apertures in the south elevation. The x pitch and alternating
     * row offsets are hand-set, not randomized. Each cut travels 84 blocks into
     * the shell so that the opening reads as a deep black slot from far away.
     */
    private void carveSlitApertures(Cube c) {
        apertureTier(c, -1700, 1700, 270, 575, 1350, 240, 112, 9, 46);
        apertureTier(c, -1460, 1460, 700, 940, 1130, 220, 120, 9, 40);
        apertureTier(c, -1200, 1200, 1080, 1360, 850, 200, 128, 8, 44);
        apertureTier(c, -950, 950, 1500, 1690, 630, 180, 120, 7, 36);
    }

    private void apertureTier(Cube c, int xMin, int xMax, int yMin, int yMax,
                              int front, int pitch, int rowPitch, int width, int height) {
        int index = 0;
        for (int x = xMin + pitch / 2; x <= xMax - pitch / 2; x += pitch, index++) {
            int phase = (index & 1) == 0 ? 0 : rowPitch / 3;
            for (int y = yMin + rowPitch / 2 + phase; y + height <= yMax; y += rowPitch) {
                if (!c.hits(x - width / 2, y, CZ + front - 84,
                            x + width / 2, y + height, CZ + front + 1)) continue;
                c.box(x - width / 2, y, CZ + front - 84,
                      x + width / 2, y + height, CZ + front + 1, Mat.AIR);
            }
        }
    }

    // ------------------------------------------------------------------
    // The interior is a single engineered nave, not empty solid blocks.
    // The cave mouths open into the deep chamber or terminate in side chambers.
    // ------------------------------------------------------------------

    private void carveNave(Cube c) {
        c.box(-NAVE_X, 190, CZ + NAVE_BACK, NAVE_X, 1720, CZ + NAVE_FRONT, Mat.AIR);
    }

    private void carveCaves(Cube c) {
        for (int i = 0; i < CAVE_X.length; i++) {
            int x = CAVE_X[i], y = CAVE_Y[i], w = CAVE_W[i], h = CAVE_H[i];
            int front = CZ + CAVE_FRONT[i];
            int back = CZ + CAVE_FRONT[i] - 1200;
            c.box(x - w / 2, y, back, x + w / 2, y + h, front, Mat.AIR);
        }
    }

    private void drawNaveStructure(Cube c) {
        // Four structural piers, visible from each of the lower cave mouths.
        int[] px = {-1000, -1000, 1000, 1000};
        int[] pz = {-800, 120, -800, 120};
        for (int i = 0; i < px.length; i++) {
            int x = px[i], z = CZ + pz[i];
            c.box(x - 28, 200, z - 28, x + 28, 1650, z + 28, Mat.S_TOHA_MASS);
            for (int y = 320; y < 1600; y += 160) {
                c.box(x - 44, y, z - 44, x + 44, y + 12, z + 44, Mat.S_TOHA_RING);
            }
        }

        // Narrow half-platforms, leaving a long uninterrupted void around the engine.
        int[] floors = {430, 790, 1150, 1490};
        for (int y : floors) {
            c.box(-920, y, CZ - 850, -570, y + 8, CZ + 180, Mat.S_TOHA_DECK);
            c.box(570, y, CZ - 850, 920, y + 8, CZ + 180, Mat.S_TOHA_DECK);
            c.box(-1020, y + 9, CZ - 850, -1012, y + 34, CZ + 180, Mat.IRON_BARS);
            c.box(1012, y + 9, CZ - 850, 1020, y + 34, CZ + 180, Mat.IRON_BARS);
        }

        // Two enormous inclined braces and hanging service lines in the open volume.
        c.beam(-1010, 330, CZ + 170, -620, 1540, CZ - 850, 26, Mat.S_TOHA_MASS);
        c.beam(1010, 330, CZ + 170, 620, 1540, CZ - 850, 26, Mat.S_TOHA_MASS);
        c.beam(-870, 1640, CZ - 830, -820, 630, CZ - 720, 2, Mat.CABLE);
        c.beam(870, 1640, CZ - 830, 820, 630, CZ - 720, 2, Mat.CABLE);
    }

    private void drawGravityFurnace(Cube c) {
        double x = CX + 0.5, z = CZ - 560 + 0.5;
        // One continuous, hollow tapered furnace/spine nearly touches the ceiling.
        c.taper(x, z, 320, 92, 210, TOP, 32, Mat.S_TOHA_RING);
        c.taper(x, z, 274, 52, 230, TOP - 14, 32, Mat.AIR);

        int[] ys = {410, 760, 1110, 1460, 1780, 1980};
        int[] rs = {425, 390, 340, 290, 220, 125};
        for (int i = 0; i < ys.length; i++) {
            int y = ys[i], r = rs[i];
            c.cylinder(x, z, r, r - 26, 1, 1, y, y + 18, 0, 0, Mat.S_TOHA_PALE);
            c.cylinder(x, z, r + 5, r, 1, 1, y + 19, y + 26, 0, 0, Mat.S_TOHA_MASS);
        }

        // Four fixed radial service arms frame the crown without becoming a fantasy ornament.
        c.beam(-600, 1770, CZ - 560, 600, 1770, CZ - 560, 24, Mat.S_TOHA_MASS);
        c.beam(0, 1810, CZ - 1120, 0, 1810, CZ + 30, 22, Mat.S_TOHA_MASS);
        c.box(-80, 1882, CZ - 650, 80, 1902, CZ - 470, Mat.BIO_CYAN);
        c.box(-18, 1972, CZ - 582, 18, 2004, CZ - 546, Mat.SEA_LANTERN);
    }

    private void drawCaveFrames(Cube c) {
        for (int i = 0; i < CAVE_X.length; i++) {
            int x = CAVE_X[i], y = CAVE_Y[i], w = CAVE_W[i], h = CAVE_H[i];
            int f = CZ + CAVE_FRONT[i];
            int l = x - w / 2, r = x + w / 2, top = y + h;

            // Deep outer reveals on all thirteen mouths.
            c.box(l - 28, y - 22, f + 3, l - 8, top + 22, f + 28, Mat.S_TOHA_PALE);
            c.box(r + 8, y - 22, f + 3, r + 28, top + 22, f + 28, Mat.S_TOHA_PALE);
            c.box(l - 20, top + 8, f + 3, r + 20, top + 28, f + 28, Mat.S_TOHA_MASS);
            c.box(l - 16, y - 14, f + 3, r + 16, y + 5, f + 28, Mat.S_TOHA_MASS);

            // Inner black lip provides a readable, deep entrance in monochrome.
            c.box(l - 7, y, f - 10, l + 6, top, f - 2, Mat.COAL);
            c.box(r - 6, y, f - 10, r + 7, top, f - 2, Mat.COAL);
            c.box(l, top - 8, f - 10, r, top + 4, f - 2, Mat.S_DARK);

            // Only the principal lower and crown entries get restrained luminous cues.
            if (i == 2) {
                c.box(x - 65, top - 18, f + 29, x + 65, top - 12, f + 31, Mat.BIO_CYAN);
            } else if (i == 12) {
                c.box(x - 34, top - 18, f + 29, x + 34, top - 12, f + 31, Mat.SEA_LANTERN);
            } else if (i == 5 || i == 8) {
                c.box(l + 12, top - 18, f + 29, l + 42, top - 12, f + 31, Mat.LAMP_RED);
            }
        }
    }

    /**
     * Each of Toha's thirteen cave AIs gets a fixed control slab recessed on the
     * right-hand side of its chamber. The panels are identical enough to belong
     * to one system, but only the primary and crown chambers retain a live cyan
     * indicator: colour remains scarce and the black-and-white silhouette leads.
     */
    private void drawCaveAIControls(Cube c) {
        for (int i = 0; i < CAVE_X.length; i++) {
            int x = CAVE_X[i] + CAVE_W[i] / 2 - 22;
            int y = CAVE_Y[i] + Math.max(22, CAVE_H[i] / 2 - 42);
            int z = CZ + CAVE_FRONT[i] - 200;
            if (!c.hits(x - 12, y, z - 18, x + 12, y + 92, z + 3)) continue;

            c.box(x - 12, y, z - 18, x + 12, y + 92, z, Mat.S_TOHA_MASS);
            c.box(x - 7, y + 12, z + 1, x - 5, y + 76, z + 3, Mat.S_DARK);
            c.box(x + 3, y + 18, z + 1, x + 7, y + 34, z + 3, Mat.ANDESITE);
            c.box(x + 3, y + 42, z + 1, x + 7, y + 48, z + 3, Mat.COAL);
            c.box(x + 3, y + 56, z + 1, x + 7, y + 62, z + 3, Mat.COAL);

            if (i == 2) {
                c.box(x + 1, y + 68, z + 1, x + 7, y + 74, z + 3, Mat.BIO_CYAN);
            } else if (i == 12) {
                c.box(x + 1, y + 68, z + 1, x + 7, y + 74, z + 3, Mat.SEA_LANTERN);
            } else if (i == 5 || i == 8) {
                c.box(x + 1, y + 68, z + 1, x + 7, y + 74, z + 3, Mat.LAMP_RED);
            }
        }
    }

    // ------------------------------------------------------------------
    // Exterior stacks and ribs create a recognisable long-range skyline.
    // ------------------------------------------------------------------

    private void drawExteriorSpines(Cube c) {
        drawSpine(c, -1710, CZ - 1120, 1140, 150, 64);
        drawSpine(c, -1710, CZ - 250, 1510, 165, 64);
        drawSpine(c, -1710, CZ + 640, 920, 130, 58);
        drawSpine(c, 1710, CZ - 1120, 1240, 150, 62);
        drawSpine(c, 1710, CZ - 250, 1630, 165, 60);
        drawSpine(c, 1710, CZ + 640, 1030, 130, 55);

        // Large sloping buttresses tie the spines into the central shells.
        c.beam(-1830, 250, CZ + 680, -1320, 1110, CZ - 1120, 34, Mat.S_TOHA_MASS);
        c.beam(1830, 250, CZ + 680, 1320, 1110, CZ - 1120, 34, Mat.S_TOHA_MASS);
        c.beam(-1780, 670, CZ - 1450, -1050, 1490, CZ - 770, 25, Mat.S_TOHA_PALE);
        c.beam(1780, 670, CZ - 1450, 1050, 1490, CZ - 770, 25, Mat.S_TOHA_PALE);

        // Two exposed ring galleries break the long rectangular mass.
        c.cylinder(CX + 0.5, CZ - 330 + 0.5, 1780, 1695, 1, 1, 850, 880, 0, 0, Mat.S_TOHA_RING);
        c.cylinder(CX + 0.5, CZ - 420 + 0.5, 1460, 1388, 1, 1, 1325, 1348, 0, 0, Mat.S_TOHA_RING);
    }

    private void drawSpine(Cube c, int x, int z, int height, int r0, int r1) {
        c.taper(x + 0.5, z + 0.5, r0, r1, GROUND, height, 32, Mat.S_TOHA_MASS);
        c.taper(x + 0.5, z + 0.5, r0 - 22, Math.max(12, r1 - 14), GROUND + 24, height - 18, 32, Mat.AIR);
        for (int y = GROUND + 160; y < height; y += 160) {
            if (!c.hitsY(y, y + 5)) continue;
            double t = (double) (y - GROUND) / (height - GROUND);
            double r = r0 + (r1 - r0) * t;
            c.cylinder(x + 0.5, z + 0.5, r + 16, r + 5, 1, 1, y, y + 5, 0, 0, Mat.S_TOHA_PALE);
        }
    }

    // ------------------------------------------------------------------
    // The south approach is a deliberate sequence of gateways. Its scale
    // compresses the player before revealing the cave façade.
    // ------------------------------------------------------------------

    private void drawApproach(Cube c) {
        int zBack = CZ + 2760, zFront = CZ + 1460;
        c.box(-640, 63, zFront, 640, 82, zBack, Mat.S_TOHA_DECK);
        c.box(-580, 83, zFront, -565, 86, zBack, Mat.S_TOHA_MASS);
        c.box(565, 83, zFront, 580, 86, zBack, Mat.S_TOHA_MASS);
        c.box(-12, 83, zFront, 12, 84, zBack, Mat.CONC_BLACK);

        int[] offsets = {2630, 2350, 2070, 1790, 1580};
        int[] spans = {1000, 1260, 1510, 1760, 1980};
        int[] heights = {310, 420, 555, 710, 900};
        for (int i = 0; i < offsets.length; i++) {
            approachFrame(c, offsets[i], spans[i], heights[i], i);
        }

        // Two deep feed channels and crossheads parallel the road, not a decorative plaza.
        c.box(-930, 28, CZ + 1830, -830, 60, CZ + 2700, Mat.S_TOHA_MASS);
        c.box(830, 28, CZ + 1830, 930, 60, CZ + 2700, Mat.S_TOHA_MASS);
        for (int z = CZ + 1900; z <= CZ + 2650; z += 150) {
            c.box(-965, 60, z, -800, 65, z + 10, Mat.S_TOHA_RING);
            c.box(800, 60, z, 965, 65, z + 10, Mat.S_TOHA_RING);
        }
    }

    private void approachFrame(Cube c, int offset, int span, int height, int index) {
        int z = CZ + offset;
        int post = Math.max(28, span / 28);
        c.box(-span / 2, GROUND, z - 26, -span / 2 + post, GROUND + height, z + 26, Mat.S_TOHA_MASS);
        c.box(span / 2 - post, GROUND, z - 26, span / 2, GROUND + height, z + 26, Mat.S_TOHA_MASS);
        c.box(-span / 2, GROUND + height - 32, z - 38, span / 2, GROUND + height + 28, z + 38, Mat.S_TOHA_PALE);
        c.box(-span / 2 + 14, GROUND + height - 12, z - 39, span / 2 - 14, GROUND + height - 6, z - 37, Mat.S_DARK);
        if (index == 3 || index == 4) {
            c.box(-span / 2 + 50, GROUND + height - 7, z + 39, span / 2 - 50, GROUND + height - 2, z + 40, Mat.BIO_CYAN);
        }

        // The frames are connected to the ground by restrained diagonal braces.
        c.beam(-span / 2 + post, GROUND + height - 40, z, -span / 2 + post + 120, GROUND + height - 150, z, 8, Mat.S_TOHA_RING);
        c.beam(span / 2 - post, GROUND + height - 40, z, span / 2 - post - 120, GROUND + height - 150, z, 8, Mat.S_TOHA_RING);
    }

    private void drawCourtyardPylons(Cube c) {
        // Four boundary obelisks, all placed by design and aligned to the wall corners.
        drawSpine(c, -2480, CZ - 2440, 600, 155, 85);
        drawSpine(c, 2480, CZ - 2440, 540, 145, 80);
        drawSpine(c, -2480, CZ + 2440, 510, 135, 75);
        drawSpine(c, 2480, CZ + 2440, 640, 170, 82);

        // Isolated transfer columns at the sides; the empty courtyard stays mostly empty.
        c.taper(-2200.5, CZ - 700.5, 82, 45, GROUND, 770, 24, Mat.S_TOHA_PALE);
        c.taper(2200.5, CZ - 700.5, 82, 45, GROUND, 710, 24, Mat.S_TOHA_PALE);
        c.cylinder(-2200.5, CZ - 700.5, 105, 82, 1, 1, 420, 434, 0, 0, Mat.S_TOHA_RING);
        c.cylinder(2200.5, CZ - 700.5, 105, 82, 1, 1, 420, 434, 0, 0, Mat.S_TOHA_RING);
    }
}
