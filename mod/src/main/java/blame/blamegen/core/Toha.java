package blame.blamegen.core;

/**
 * TOHA HEAVY INDUSTRIES — authored monument, not a district generator.
 *
 * A single deterministic architectural composition centred at (0, -4608):
 * a 6.1 km precinct, a 3.9 km-wide stepped factory-fortress, a 2 km engine
 * stack, thirteen cave mouths with dedicated AI machines, a hollow central nave, a ceiling-height service mast, exterior machine spines,
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

    /**
     * Front-facing orthographic review plane for each deliberately stepped shell.
     * The tiers recede in depth, so a single fixed-Z slice cannot show the elevation.
     */
    private static final int[] SHELL_Y = {64, 640, 1020, 1450, 1760, 1920};
    private static final int[] SHELL_R = {2200, 1900, 1600, 1320, 1050, 820};

    static int cylinderRadiusAtY(int y) {
        if (y < 640) return 2200;
        if (y < 1020) return 1900;
        if (y < 1450) return 1600;
        if (y < 1760) return 1320;
        if (y < 1920) return 1050;
        return 820;
    }

    static int cylinderFrontOffset(int radius, int x) {
        double dx = x + 0.5 - CX;
        return (int) Math.ceil(Math.sqrt(Math.max(0.0, radius * (double) radius - dx * dx))) + 12;
    }

    static int caveRadius(int i) {
        return i < 5 ? 2200 : i < 9 ? 1900 : i < 12 ? 1600 : 1320;
    }

    static int caveFrontAt(int i, int x) {
        return cylinderFrontOffset(caveRadius(i), x);
    }

    public static int projectedFrontZ(int x, int y) {
        return CZ + cylinderFrontOffset(cylinderRadiusAtY(y), x);
    }
    private static final int NAVE_X = 1050;
    private static final int NAVE_BACK = -1050;
    private static final int NAVE_FRONT = 250;

    // Thirteen fixed chambers: five at the foot, four in the second tier,
    // three in the third, and one isolated chamber in the crown.
    static final int[] CAVE_X = {
        -1500, -630, 0, 790, 1510,
        -1130, -345, 500, 1170,
        -820, 120, 780,
        190
    };
    static final int[] CAVE_Y = {
        160, 220, 130, 175, 190,
        700, 680, 730, 650,
        1070, 1000, 1130,
        1505
    };
    static final int[] CAVE_W = {
        220, 190, 480, 300, 160,
        230, 260, 320, 190,
        230, 460, 280,
        280
    };
    static final int[] CAVE_H = {
        300, 220, 500, 310, 240,
        240, 280, 250, 310,
        290, 330, 260,
        240
    };
    static final int[] CAVE_FRONT = {
        1622, 2121, 2212, 2066, 1612,
        1540, 1881, 1845, 1509,
        1387, 1608, 1409,
        1319
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
        drawAsymmetricTransferMass(c);
        drawFacade(c);
        drawTerraceBands(c);
        carveSlitApertures(c);
        carveNave(c);
        carveCaves(c);
        drawNaveStructure(c);
        drawGravityFurnace(c);
        drawCaveMachines(c);
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
        // A four-block deck plate sits on an immense but sparse girder grid. The
        // old 64-block solid slab consumed billions of blocks without adding silhouette.
        c.box(-2800, 60, CZ - 2800, 2800, 63, CZ + 2800, Mat.S_TOHA_DECK);

        // Main orthogonal girders are 8 blocks wide and 12 high at 512-block spacing.
        if (c.hitsY(48, 59)) {
            for (int x = -2560; x <= 2560; x += 512) {
                if (x + 4 >= c.x0 && x - 4 <= c.x0 + 15)
                    c.box(x - 4, 48, CZ - 2800, x + 4, 59, CZ + 2800, Mat.S_TOHA_MASS);
            }
            for (int z = -2560; z <= 2560; z += 512) {
                int wz = CZ + z;
                if (wz + 4 >= c.z0 && wz - 4 <= c.z0 + 15)
                    c.box(-2800, 48, wz - 4, 2800, 59, wz + 4, Mat.S_TOHA_MASS);
            }
        }

        // Three inset, unlit perimeter courses make the deck feel machined.
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
        // Immense shells are vertical wall envelopes, not solid rectangular volumes.
        // The interior belongs to the nave, service bridges and cave chambers.
        // A shallow rear plinth projects forward only below the entry tier. Above it,
        // the main face steps back to the actual cave-mouth plane so the openings are real.
        wallShell(c, -2050, 64, CZ - 2200, 2050, 128, CZ + 1600, 24, Mat.S_TOHA_PALE);
        wallShell(c, -2100, 128, CZ - 2220, 2100, 200, CZ + 1350, 24, Mat.S_TOHA_PALE);
        rectBand(c, 2100, -2220, 1350, 180, 236, Mat.S_TOHA_MASS);

        // The lower black body, broad shoulder, pale middle mass, and receding crown.
        wallShell(c, -1850, 200, CZ - 1900, 1850, 640, CZ + 1350, 28, Mat.S_TOHA_MASS);
        rectBand(c, 1940, -1980, 1420, 600, 632, Mat.S_TOHA_PALE);

        wallShell(c, -1580, 640, CZ - 1700, 1580, 1020, CZ + 1130, 24, Mat.S_TOHA_PALE);
        rectBand(c, 1660, -1770, 1200, 980, 1018, Mat.S_TOHA_MASS);

        wallShell(c, -1320, 1020, CZ - 1440, 1320, 1450, CZ + 850, 24, Mat.S_TOHA_MASS);
        rectBand(c, 1390, -1510, 920, 1408, 1448, Mat.S_TOHA_PALE);

        wallShell(c, -1050, 1450, CZ - 1130, 1050, 1760, CZ + 630, 24, Mat.S_TOHA_PALE);
        rectBand(c, 1150, -1230, 710, 1724, 1758, Mat.S_TOHA_MASS);
        wallShell(c, -800, 1760, CZ - 900, 800, 1880, CZ + 430, 20, Mat.S_TOHA_MASS);

        // A thin, dark crown lip, intentionally close to the immense ceiling above.
        rectBand(c, 900, -1010, 530, 1870, 1895, Mat.S_TOHA_PALE);
        wallShell(c, -620, 1895, CZ - 790, 620, 1920, CZ + 310, 16, Mat.S_TOHA_MASS);

        // Rear counter-masses are similarly hollow wall envelopes; their offset
        // position changes the silhouette without filling billions of blocks.
        wallShell(c, -970, 440, CZ - 2240, 970, 1600, CZ - 1860, 24, Mat.S_TOHA_PALE);
        rectBand(c, 1250, -2280, -1750, 760, 820, Mat.S_TOHA_MASS);
        wallShell(c, -820, 1290, CZ - 2160, 820, 1760, CZ - 1810, 16, Mat.S_TOHA_MASS);
    }

    /**
     * One-sided service massing is intentional: the main shell is immense, but it
     * must not read as a mirrored palace. This eastern transfer buttress and the
     * western upper cantilever break the symmetry with functional industrial bulk.
     */
    private void drawAsymmetricTransferMass(Cube c) {
        // A broad, setback transfer mass grows out of the east shoulder.
        wallShell(c, 1510, 720, CZ - 1650, 1840, 1110, CZ + 1040, 24, Mat.S_TOHA_MASS);
        rectBand(c, 1900, -1410, 900, 1040, 1190, Mat.S_TOHA_PALE);
        wallShell(c, 1570, 1160, CZ - 1320, 1880, 1460, CZ + 720, 24, Mat.S_TOHA_MASS);

        // A deep slot cuts the exposed end wall; it is a machine access trench, not a window.
        c.box(1834, 790, CZ + 260, 1842, 1040, CZ + 720, Mat.AIR);
        c.box(1834, 1075, CZ + 260, 1842, 1092, CZ + 720, Mat.S_TOHA_PALE);

        // The east transfer mass is pinned into the rear shell by a long diagonal brace.
        c.beam(1810, 770, CZ + 980, 1600, 1190, CZ - 1540, 38, Mat.S_TOHA_PALE);
        c.beam(1850, 820, CZ + 940, 1650, 1110, CZ - 1460, 14, Mat.S_TOHA_RING);

        // Opposite side: a single upper cantilever, deliberately not mirrored.
        wallShell(c, -1770, 1480, CZ - 1080, -1040, 1560, CZ + 420, 16, Mat.S_TOHA_MASS);
        c.box(-1840, 1510, CZ - 980, -1764, 1550, CZ + 360, Mat.S_TOHA_PALE);
        c.beam(-1710, 1485, CZ + 380, -1090, 1670, CZ - 900, 28, Mat.S_TOHA_RING);
    }

    /**
     * Draw four vertical perimeter walls for an enormous architectural envelope.
     * Top/bottom surfaces are deliberately omitted: the interior stays hollow and
     * is crossed by authored girders, service platforms and machine components.
     */
    private void wallShell(Cube c, int x0, int y0, int z0, int x1, int y1, int z1,
                           int thickness, byte material) {
        int t = Math.max(1, thickness);
        c.box(x0, y0, z0, x1, y1, z0 + t - 1, material);
        c.box(x0, y0, z1 - t + 1, x1, y1, z1, material);
        c.box(x0, y0, z0 + t, x0 + t - 1, y1, z1 - t, material);
        c.box(x1 - t + 1, y0, z0 + t, x1, y1, z1 - t, material);
    }

    private void drawFacade(Cube c) {
        // Sparse, oversized load ribs. A fixed irregular rhythm avoids a barcode /
        // apartment-grid effect and lets the cave mouths dominate the facade.
        frontRibs(c, new int[] {-1800, -1270, -900, -390, 390, 1060, 1740},
                200, 620, 1350, new int[] {42, 58, 36, 52, 46, 64, 48});
        frontRibs(c, new int[] {-1500, -820, -610, -30, 230, 830, 1460},
                640, 1000, 1130, new int[] {54, 38, 62, 44, 50, 36, 58});
        frontRibs(c, new int[] {-1230, -940, -540, -220, 420, 1020, 1270},
                1020, 1410, 850, new int[] {40, 58, 34, 46, 60, 38, 52});
        frontRibs(c, new int[] {-990, -700, -400, 420, 700, 990},
                1450, 1720, 630, new int[] {42, 30, 56, 48, 34, 44});

        // One massive off-centre lintel on the middle tier, interrupted by the
        // main cave and the displaced right-hand service entrance.
        c.box(-1520, 1004, CZ + 1090, -520, 1028, CZ + 1190, Mat.S_TOHA_MASS);
        c.box(720, 1004, CZ + 1090, 1510, 1028, CZ + 1190, Mat.S_TOHA_MASS);
        c.box(-1490, 1000, CZ + 1190, -540, 1006, CZ + 1210, Mat.S_TOHA_PALE);

        // Giant blind slots on both flanks continue the façade language around the mass.
        flankSlots(c, -1850, 1350, 200, 640);
        flankSlots(c, -1580, 1130, 640, 1020);
        flankSlots(c, -1320, 850, 1020, 1450);
    }

    private void frontRibs(Cube c, int[] xs, int yMin, int yMax, int front, int[] widths) {
        for (int i = 0; i < xs.length; i++) {
            int x = xs[i], w = widths[i];
            if (!c.hits(x - w / 2, yMin, CZ + front + 1,
                        x + w / 2, yMax, CZ + front + 96)) continue;
            c.box(x - w / 2, yMin, CZ + front + 1,
                  x + w / 2, yMax, CZ + front + 72, Mat.S_TOHA_MASS);
            // A narrow pale arris gives each rib a concrete edge rather than stripes.
            c.box(x - 2, yMin + 24, CZ + front + 73,
                  x + 2, yMax - 24, CZ + front + 84, Mat.S_TOHA_PALE);
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

    /**
     * Twelve cave AIs have their own chamber machine, and the principal cave
     * frames the gravity furnace rather than obscuring it. These are fixed,
     * authored mechanisms with different silhouettes, not a random prop set.
     */
    private void drawCaveMachines(Cube c) {
        for (int i = 0; i < CAVE_X.length; i++) {
            int x = CAVE_X[i];
            int y = CAVE_Y[i] + CAVE_H[i] / 2;
            int front = CZ + CAVE_FRONT[i];
            int z = front - 380;
            int r = Math.max(30, Math.min(110, CAVE_W[i] / 4));
            int halfY = Math.max(48, CAVE_H[i] / 3);
            int y0 = Math.max(CAVE_Y[i] + 16, y - halfY);
            int y1 = Math.min(CAVE_Y[i] + CAVE_H[i] - 16, y + halfY);

            if (i == 2) {
                // The primary cave is a sightline to the central gravity furnace.
                // Two side frames mark its AI's machinery while the middle stays open.
                for (int side : new int[] {-1, 1}) {
                    double sx = x + side * 150.5;
                    c.taper(sx, z + 0.5, 30, 22, y0, y1, 24, Mat.S_TOHA_MASS);
                    c.taper(sx, z + 0.5, 18, 12, y0 + 16, y1 - 16, 24, Mat.AIR);
                    c.beam((int) sx, y0 + 12, z + 36, (int) sx, y1 - 10, z - 60, 7, Mat.CABLE);
                    c.cylinder(sx, z + 0.5, 38, 30, 1, 1, y + 20, y + 28, 0, 0, Mat.S_TOHA_RING);
                }
                c.beam(x - 200, y + halfY - 8, z, x + 200, y + halfY - 8, z, 12, Mat.S_TOHA_PALE);
                continue;
            }

            switch (i % 4) {
                case 0: {
                    // Tall tapered spindle with two heavy bearing collars.
                    c.taper(x + 0.5, z + 0.5, r, Math.max(18, r * 0.55), y0, y1, 24, Mat.S_TOHA_MASS);
                    c.taper(x + 0.5, z + 0.5, Math.max(12, r - 12),
                            Math.max(8, r * 0.55 - 10), y0 + 12, y1 - 12, 24, Mat.AIR);
                    c.cylinder(x + 0.5, z + 0.5, r + 18, r + 5, 1, 1, y0, y0 + 9, 0, 0, Mat.S_TOHA_PALE);
                    c.cylinder(x + 0.5, z + 0.5, r + 14, r + 4, 1, 1, y1 - 8, y1, 0, 0, Mat.S_TOHA_RING);
                    c.beam(x - r - 24, y, z + 40, x + r + 24, y, z + 40, 8, Mat.S_TOHA_MASS);
                    break;
                }
                case 1: {
                    // An exposed gyro-ring, held by two narrow load-bearing arms.
                    c.cylinder(x + 0.5, z + 0.5, r + 24, r + 10, 1, 1, y - 18, y - 9, 0, 0, Mat.S_TOHA_PALE);
                    c.cylinder(x + 0.5, z + 0.5, r + 30, r + 14, 1, 1, y + 8, y + 18, 0, 0, Mat.S_TOHA_RING);
                    for (int side : new int[] {-1, 1}) {
                        c.taper(x + side * (r + 10) + 0.5, z + 0.5, 18, 14,
                                y0, y1, 20, Mat.S_TOHA_MASS);
                        c.beam(x + side * (r + 10), y, z + 24,
                                x + side * (r + 10), y, z - 80, 6, Mat.CABLE);
                    }
                    break;
                }
                case 2: {
                    // Suspended rectangular service cage, open at the top and bottom.
                    wallShell(c, x - r, y0, z - 42, x + r, y1, z + 42, 8, Mat.S_TOHA_MASS);
                    c.box(x - r - 20, y0 + 12, z - 8, x - r - 12, y1 - 12, z + 8, Mat.S_TOHA_PALE);
                    c.box(x + r + 12, y0 + 12, z - 8, x + r + 20, y1 - 12, z + 8, Mat.S_TOHA_PALE);
                    c.beam(x - r, y1 + 36, z, x + r, y1 + 36, z, 10, Mat.S_TOHA_RING);
                    c.beam(x - r - 8, y1 + 30, z, x - r - 8, y1 + 68, z - 48, 3, Mat.CABLE);
                    c.beam(x + r + 8, y1 + 30, z, x + r + 8, y1 + 68, z - 48, 3, Mat.CABLE);
                    break;
                }
                default: {
                    // Paired pressure vessels linked by a low service bridge.
                    int vesselR = Math.max(16, r * 35 / 100);
                    for (int side : new int[] {-1, 1}) {
                        double sx = x + side * r * 3.0 / 5.0 + 0.5;
                        c.taper(sx, z + 0.5, vesselR + 8, vesselR, y0, y1, 24, Mat.S_TOHA_RING);
                        c.taper(sx, z + 0.5, Math.max(8, vesselR - 8),
                                Math.max(6, vesselR - 13), y0 + 10, y1 - 10, 24, Mat.AIR);
                        c.cylinder(sx, z + 0.5, vesselR + 12, vesselR + 4, 1, 1,
                                y - 6, y + 2, 0, 0, Mat.S_TOHA_PALE);
                    }
                    c.box(x - r, y - 10, z + 34, x + r, y - 2, z + 58, Mat.S_TOHA_MASS);
                    c.beam(x - r, y + 16, z - 4, x + r, y + 16, z - 4, 6, Mat.CABLE);
                    break;
                }
            }
        }
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

        // One off-axis mast rises nearly to the ceiling. Its height and position
        // break the regular crown line and make Toha read as an industrial vessel.
        drawSpine(c, 2040, CZ - 900, 1975, 210, 78);
        c.beam(1990, 690, CZ + 840, 2040, 1810, CZ - 900, 30, Mat.S_TOHA_PALE);
        c.beam(2160, 1060, CZ + 280, 2040, 1910, CZ - 900, 16, Mat.S_TOHA_RING);
        c.beam(2040, 1840, CZ - 900, 900, 1780, CZ - 790, 24, Mat.S_TOHA_MASS);
        c.box(2020, 1940, CZ - 920, 2060, 1952, CZ - 880, Mat.BIO_CYAN);

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
