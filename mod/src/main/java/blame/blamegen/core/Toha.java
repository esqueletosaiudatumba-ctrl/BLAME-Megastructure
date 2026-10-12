package blame.blamegen.core;

/**
 * TOHA HEAVY INDUSTRIES — authored monument, not a district generator.
 *
 * A single deterministic architectural composition centred at (0, -4608):
 * a 5.6 km circular precinct, a 4.4 km-diameter lower hull with concentric
 * stepped cylindrical tiers, a 2 km gravity-furnace stack, thirteen surface-
 * curved cave mouths with fixed AI machinery, a hollow nave, a near-ceiling
 * service mast, exterior spines, and a monumental processional causeway.
 * All proportions and placements are fixed. Only material weathering resolved
 * by Cube remains seed-dependent.
 *
 * The forms are designed for Minecraft-scale sightlines: a huge circular hull,
 * broad annular shoulders, pale ruled shells, black setback bands, deep cave
 * mouths, slit apertures, enormous structural braces, and sparse local lights.
 * There is no random cell grid, random
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

    // Fixed angular segments for the broken orbital galleries. Gaps are authored,
    // not selected from a seed, and the south-facing approach is kept visibly open.
    private static final int[] LOWER_GALLERY_ARCS = {24, 66, 116, 154, 210, 274, 330};
    private static final int[] MIDDLE_GALLERY_ARCS = {18, 61, 128, 174, 224, 292, 337};
    private static final int[] UPPER_GALLERY_ARCS = {16, 64, 132, 193, 244, 306, 348};
    private static final int[] CROWN_GALLERY_ARCS = {35, 143, 208, 282, 326};

    // Hand-authored facade apertures: x, bottom Y, width, height, tier radius, cut depth.
    // Unequal spacing and dimensions intentionally reject an apartment/window-grid read.
    private static final int[][] SOUTH_SLITS = {
        {-1840, 290, 12, 82, 2200, 96},
        {-1610, 430, 18, 116, 2200, 108},
        {-1210, 340, 10, 62, 2200, 86},
        {-870, 330, 18, 110, 2200, 104},
        {-560, 465, 12, 74, 2200, 96},
        {370, 290, 12, 58, 2200, 88},
        {610, 438, 16, 94, 2200, 100},
        {1280, 325, 10, 80, 2200, 90},
        {1810, 405, 20, 126, 2200, 112},
        {-1660, 720, 12, 98, 1900, 92},
        {-1420, 875, 16, 70, 1900, 104},
        {-780, 760, 10, 110, 1900, 88},
        {-90, 902, 12, 64, 1900, 100},
        {340, 815, 18, 96, 1900, 102},
        {925, 690, 10, 74, 1900, 96},
        {1450, 820, 18, 112, 1900, 96},
        {-1310, 1095, 12, 88, 1600, 92},
        {-530, 1160, 10, 74, 1600, 88},
        {390, 1315, 12, 84, 1600, 100},
        {1010, 1078, 16, 118, 1600, 96},
        {1370, 1250, 10, 68, 1600, 90},
        {-1110, 1530, 10, 72, 1320, 96},
        {-640, 1660, 12, 58, 1320, 88},
        {620, 1570, 14, 78, 1320, 96},
        {980, 1690, 10, 64, 1320, 86},
        {-890, 1800, 10, 84, 1050, 92},
        {760, 1840, 12, 55, 1050, 88}
    };

    // Canonical hull mark from the manga: four vertical, block-engraved glyphs.
    // Rows are top-to-bottom in reading order, 東亜重工. Each bit becomes a small
    // raised strip that follows the real circular shell instead of a flat sign.
    private static final String[][] HULL_INSCRIPTION = {
        { // 東
            "00000110000000",
            "00000110000000",
            "11111111111111",
            "00000110000000",
            "00011111111000",
            "00000110000000",
            "11111111111111",
            "00000110000000",
            "00111111111100",
            "00100110001000",
            "01100110001100",
            "10000110000010",
            "00000110000000",
            "00000110000000"
        },
        { // 亜
            "00111111111100",
            "00100000000100",
            "00100000000100",
            "00111111111100",
            "00000111100000",
            "00000110100000",
            "11111111111111",
            "00100110100100",
            "00100110100100",
            "00100110100100",
            "00111111111100",
            "00000110100000",
            "00000110100000",
            "00111111111100"
        },
        { // 重
            "00011111111000",
            "00000110000000",
            "11111111111111",
            "00000110000000",
            "00111111111100",
            "00000110000000",
            "11111111111111",
            "00100110001000",
            "00111111111100",
            "00100110001000",
            "00111111111100",
            "00100110001000",
            "00111111111100",
            "11111111111111"
        },
        { // 工
            "11111111111111",
            "00000011000000",
            "00000011000000",
            "00000011000000",
            "00000011000000",
            "00000011000000",
            "00000011000000",
            "00000011000000",
            "00000011000000",
            "00000011000000",
            "00000011000000",
            "00000011000000",
            "00000011000000",
            "11111111111111"
        }
    };

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
        drawMonumentalAccretions(c);
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
        drawHullInscription(c);
    }

    // ------------------------------------------------------------------
    // The precinct: a finite floor and low enclosure, leaving negative space
    // between the boundary and the main mass so the building reads as a whole.
    // ------------------------------------------------------------------

    /**
     * The outside is literally marked 東亜重工 in BLAME!. Reproduce that identifying
     * mark as block strokes welded to the curved hull. A tight broad phase keeps this
     * small authored detail from adding work to unrelated world-generation cubes.
     */
    private void drawHullInscription(Cube c) {
        final int cell = 6, gap = 8, span = 14 * cell, stride = span + gap;
        final int centreX = 1680, x0 = centreX - span / 2, x1 = x0 + span - 1;
        final int bottomY = 84, topY = bottomY + 4 * span + 3 * gap - 1;
        if (c.x0 + 15 < x0 || c.x0 > x1 || c.y0 + 15 < bottomY || c.y0 > topY) return;

        int frontA = projectedFrontZ(Math.max(x0, c.x0), bottomY) - 12;
        int frontB = projectedFrontZ(Math.min(x1, c.x0 + 15), bottomY) - 12;
        int minFront = Math.min(frontA, frontB), maxFront = Math.max(frontA, frontB);
        if (c.z0 + 15 < minFront + 1 || c.z0 > maxFront + 4) return;

        for (int glyphIndex = 0; glyphIndex < HULL_INSCRIPTION.length; glyphIndex++) {
            int glyphBottom = bottomY + (HULL_INSCRIPTION.length - 1 - glyphIndex) * stride;
            if (c.y0 + 15 < glyphBottom || c.y0 > glyphBottom + span - 1) continue;
            String[] glyph = HULL_INSCRIPTION[glyphIndex];
            for (int row = 0; row < 14; row++) {
                int y = glyphBottom + (13 - row) * cell;
                if (y > c.y0 + 15 || y + cell - 1 < c.y0) continue;
                for (int col = 0; col < 14; col++) {
                    if (glyph[row].charAt(col) != '1') continue;
                    int px0 = x0 + col * cell;
                    int lo = Math.max(px0, c.x0), hi = Math.min(px0 + cell - 1, c.x0 + 15);
                    if (lo > hi) continue;
                    for (int x = lo; x <= hi; x++) {
                        int front = projectedFrontZ(x, y) - 12;
                        if (front + 4 < c.z0 || front + 1 > c.z0 + 15) continue;
                        c.box(x, y, front + 1, x, y + cell - 1, front + 4, Mat.COAL);
                    }
                }
            }
        }
    }

    private void drawFoundation(Cube c) {
        // Round four-block plate under the cylindrical hull.
        c.prism(CX + 0.5, CZ + 0.5, 2800, 0, 0, false, 0, 60, 63, Mat.S_TOHA_DECK);

        // Concentric girders and radial spokes carry the deck instead of a solid slab.
        if (c.hitsY(48, 59)) {
            int[] radii = {2700, 2200, 1700, 1200, 700};
            for (int radius : radii) {
                c.cylinder(CX + 0.5, CZ + 0.5, radius + 10, radius - 10,
                        1, 1, 48, 59, 0, 0, Mat.S_TOHA_MASS);
            }
            for (int deg = 0; deg < 360; deg += 30) {
                double a = Math.toRadians(deg);
                int ex = (int) Math.round(Math.sin(a) * 2700);
                int ez = (int) Math.round(Math.cos(a) * 2700);
                c.beam(CX, 54, CZ, CX + ex, 54, CZ + ez, 10, Mat.S_TOHA_MASS);
            }
        }
        c.cylinder(CX + 0.5, CZ + 0.5, 2790, 2768, 1, 1, 63, 63, 0, 0, Mat.S_DARK);
    }

    private void drawOuterWalls(Cube c) {
        // Circular retaining wall with a wide opening aligned to the south portal.
        c.cylinder(CX + 0.5, CZ + 0.5, 2760, 2720, 1, 1,
                GROUND, 350, Math.PI / 2 - 0.28, 0.56, Mat.S_TOHA_PALE);
        c.cylinder(CX + 0.5, CZ + 0.5, 2780, 2760, 1, 1,
                350, 378, Math.PI / 2 - 0.28, 0.56, Mat.S_TOHA_MASS);

        // Sparse buttresses around the ring. The front gate sector remains clear.
        int[] degrees = {35, 65, 115, 145, 200, 230, 270, 310, 340};
        for (int deg : degrees) {
            double a = Math.toRadians(deg);
            int x0 = (int) Math.round(Math.sin(a) * 2735);
            int z0 = CZ + (int) Math.round(Math.cos(a) * 2735);
            int x1 = (int) Math.round(Math.sin(a) * 2800);
            int z1 = CZ + (int) Math.round(Math.cos(a) * 2800);
            c.beam(x0, GROUND + 20, z0, x1, 390, z1, 24, Mat.S_TOHA_MASS);
        }
    }

    /**
     * The south perimeter opening is a portal, not a hole in a fence: two
     * deep side piers and a massive crosshead form the first compressed scale
     * cue before the larger factory mass comes into view.
     */
    private void drawGatePortal(Cube c) {
        int z0 = CZ + 2470, z1 = CZ + 2710;
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
        // Toha's primary mass is a cylindrical hull, stepping inward at immense levels.
        cylinderTier(c, 2200, 36, 64, 639, Mat.S_TOHA_MASS);
        cylinderTier(c, 1900, 36, 640, 1019, Mat.S_TOHA_PALE);
        cylinderTier(c, 1600, 36, 1020, 1449, Mat.S_TOHA_MASS);
        cylinderTier(c, 1320, 36, 1450, 1759, Mat.S_TOHA_PALE);
        cylinderTier(c, 1050, 32, 1760, 1919, Mat.S_TOHA_MASS);
        cylinderTier(c, 820, 28, 1920, 1950, Mat.S_TOHA_PALE);

        // Massive horizontal collars break the silhouette into engineered cylindrical strata.
        c.cylinder(CX + 0.5, CZ + 0.5, 2260, 2220, 1, 1, 250, 276, 0, 0, Mat.S_TOHA_PALE);
        c.cylinder(CX + 0.5, CZ + 0.5, 1940, 1900, 1, 1, 810, 840, 0, 0, Mat.S_TOHA_RING);
        c.cylinder(CX + 0.5, CZ + 0.5, 1640, 1600, 1, 1, 1210, 1240, 0, 0, Mat.S_TOHA_PALE);
    }

    private void cylinderTier(Cube c, int outerRadius, int thickness, int y0, int y1, byte material) {
        double x = CX + 0.5, z = CZ + 0.5;
        // Clear the underlying city inside the shell, then write only the load-bearing wall annulus.
        c.prism(x, z, outerRadius - thickness, 0, 0, false, 0, y0, y1, Mat.AIR);
        c.shellPrism(x, z, outerRadius, outerRadius - thickness, y0, y1, material);
    }

    /**
     * One-sided service massing is intentional: the main shell is immense, but it
     * must not read as a mirrored palace. This eastern transfer buttress and the
     * western upper cantilever break the symmetry with functional industrial bulk.
     */
    private void drawAsymmetricTransferMass(Cube c) {
        // East-side transfer machinery projects through the cylindrical hull in two stages.
        wallShell(c, 2040, 720, CZ - 1450, 2530, 1110, CZ + 720, 32, Mat.S_TOHA_MASS);
        c.box(2050, 1036, CZ - 1420, 2580, 1074, CZ + 760, Mat.S_TOHA_PALE);
        wallShell(c, 2160, 1160, CZ - 1180, 2620, 1460, CZ + 520, 28, Mat.S_TOHA_MASS);

        // Deep vertical service trench in the exposed outer end wall.
        c.box(2516, 790, CZ + 220, 2524, 1040, CZ + 650, Mat.AIR);
        c.box(2516, 1075, CZ + 220, 2524, 1092, CZ + 650, Mat.S_TOHA_PALE);
        c.beam(2440, 770, CZ + 680, 2050, 1190, CZ - 1380, 42, Mat.S_TOHA_PALE);
        c.beam(2480, 820, CZ + 640, 2100, 1110, CZ - 1320, 18, Mat.S_TOHA_RING);

        // Opposite side: a single upper cantilever breaks the cylinder asymmetrically.
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
        drawAxialRibs(c);
    }

    private void drawAxialRibs(Cube c) {
        // Eight fixed, oversized ribs follow the hull's changing radius. They
        // sit between cave groups rather than repeating as apartment-like stripes.
        int[] degrees = {-160, -125, -90, -55, 55, 90, 125, 160};
        for (int deg : degrees) {
            double a = Math.toRadians(deg);
            for (int i = 0; i < SHELL_Y.length - 1; i++) {
                int y0 = SHELL_Y[i], y1 = SHELL_Y[i + 1];
                int r0 = SHELL_R[i] + 34, r1 = SHELL_R[i + 1] + 34;
                double x0 = CX + Math.sin(a) * r0, z0 = CZ + Math.cos(a) * r0;
                double x1 = CX + Math.sin(a) * r1, z1 = CZ + Math.cos(a) * r1;
                c.tube(x0, y0, z0, x1, y1, z1, 30, 16, Mat.S_TOHA_MASS);
                c.tube(x0, y0, z0, x1, y1, z1, 7, 0, Mat.S_TOHA_PALE);
            }
        }
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
        // Circumferential collars create enormous ledges where the hull steps inward.
        ringCourse(c, 2300, 1840, 610, 650, Mat.S_TOHA_MASS);
        ringCourse(c, 2320, 2288, 620, 644, Mat.S_TOHA_PALE);
        ringCourse(c, 2000, 1540, 995, 1035, Mat.S_TOHA_RING);
        ringCourse(c, 1700, 1260, 1425, 1465, Mat.S_TOHA_MASS);
        ringCourse(c, 1400, 1000, 1735, 1775, Mat.S_TOHA_RING);
        ringCourse(c, 1110, 785, 1898, 1932, Mat.S_TOHA_PALE);
    }

    private void ringCourse(Cube c, int outerR, int innerR, int y0, int y1, byte material) {
        c.cylinder(CX + 0.5, CZ + 0.5, outerR, innerR, 1, 1, y0, y1, 0, 0, material);
    }

    /**
     * Fractured orbital galleries, attached facade plates and diagonal load paths
     * break the otherwise perfect concentric silhouette. These are authored elements,
     * not randomized dressing or a procedural selection of tower types.
     */
    private void drawMonumentalAccretions(Cube c) {
        drawSegmentedCourse(c, 2520, 2376, 466, 491, LOWER_GALLERY_ARCS, 34, Mat.S_TOHA_MASS);
        drawSegmentedCourse(c, 2185, 2042, 910, 940, MIDDLE_GALLERY_ARCS, 29, Mat.S_TOHA_RING);
        drawSegmentedCourse(c, 1810, 1685, 1365, 1395, UPPER_GALLERY_ARCS, 33, Mat.S_TOHA_MASS);
        drawSegmentedCourse(c, 1495, 1372, 1630, 1658, CROWN_GALLERY_ARCS, 25, Mat.S_TOHA_PALE);

        // Short, thick outriggers join the galleries to the load-bearing shell.
        radialSupport(c, 26, 2190, 414, 2425, 468, 28, Mat.S_TOHA_RING);
        radialSupport(c, 151, 2190, 365, 2425, 468, 32, Mat.S_TOHA_PALE);
        radialSupport(c, 230, 2190, 405, 2425, 468, 26, Mat.S_TOHA_MASS);
        radialSupport(c, 326, 2190, 345, 2425, 468, 30, Mat.S_TOHA_PALE);

        radialSupport(c, 52, 1890, 865, 2090, 920, 24, Mat.S_TOHA_PALE);
        radialSupport(c, 176, 1890, 830, 2090, 920, 28, Mat.S_TOHA_MASS);
        radialSupport(c, 292, 1890, 880, 2090, 920, 22, Mat.S_TOHA_PALE);

        radialSupport(c, 58, 1590, 1310, 1740, 1380, 22, Mat.S_TOHA_RING);
        radialSupport(c, 198, 1590, 1280, 1740, 1380, 25, Mat.S_TOHA_PALE);
        radialSupport(c, 306, 1590, 1325, 1740, 1380, 21, Mat.S_TOHA_MASS);

        // Curved, almost-flat wall fins catch light at widely separated elevations.
        // Each x-column follows the tier's real circular surface instead of a flat Z plane.
        frontFlankPlate(c, -1995, 2200, 220, 615, 30, 116, Mat.S_TOHA_MASS, Mat.S_TOHA_PALE);
        frontFlankPlate(c, 2020, 2200, 300, 625, 26, 96, Mat.S_TOHA_RING, Mat.S_TOHA_PALE);
        frontFlankPlate(c, -1740, 1900, 665, 1000, 32, 120, Mat.S_TOHA_PALE, Mat.S_TOHA_MASS);
        frontFlankPlate(c, 1705, 1900, 725, 985, 24, 104, Mat.S_TOHA_MASS, Mat.S_TOHA_PALE);
        frontFlankPlate(c, -1400, 1600, 1075, 1425, 28, 112, Mat.S_TOHA_MASS, Mat.S_TOHA_PALE);
        frontFlankPlate(c, 1430, 1600, 1110, 1440, 24, 126, Mat.S_TOHA_RING, Mat.S_TOHA_PALE);
        frontFlankPlate(c, -1170, 1320, 1500, 1730, 20, 100, Mat.S_TOHA_PALE, Mat.S_TOHA_RING);
        frontFlankPlate(c, 1000, 1050, 1770, 1905, 20, 90, Mat.S_TOHA_MASS, Mat.S_TOHA_PALE);

        // Four major diagonal compression members stitch tiers at non-mirrored angles.
        diagonalMember(c, 25, 2165, 285, 34, 1880, 1030, 64, 22, Mat.S_TOHA_MASS);
        diagonalMember(c, 155, 2160, 330, 143, 1585, 1440, 72, 26, Mat.S_TOHA_PALE);
        diagonalMember(c, 222, 1895, 710, 239, 1310, 1735, 56, 20, Mat.S_TOHA_RING);
        diagonalMember(c, 319, 2160, 190, 302, 1580, 1215, 80, 30, Mat.S_TOHA_MASS);
    }

    /**
     * Draw only the angular portion of an annulus that survives the supplied gap.
     * Segment-specific broad-phase checks prevent a short arc from raster-testing the
     * entire square bounding box of a several-kilometre cylinder.
     */
    private void drawSegmentedCourse(Cube c, int outerRadius, int innerRadius,
                                     int y0, int y1, int[] centresDegrees,
                                     int spanDegrees, byte material) {
        double half = Math.toRadians(spanDegrees / 2.0);
        for (int degrees : centresDegrees) {
            double centre = Math.toRadians(degrees);
            double start = centre - half, end = centre + half;
            if (!arcBoundsHit(c, outerRadius, start, end, y0, y1)) continue;
            c.cylinder(CX + 0.5, CZ + 0.5, outerRadius, innerRadius, 1, 1,
                    y0, y1, end, 2 * Math.PI - 2 * half, material);
        }
    }

    private boolean arcBoundsHit(Cube c, int radius, double start, double end, int y0, int y1) {
        double minX = Math.min(CX + 0.5 + radius * Math.cos(start),
                               CX + 0.5 + radius * Math.cos(end));
        double maxX = Math.max(CX + 0.5 + radius * Math.cos(start),
                               CX + 0.5 + radius * Math.cos(end));
        double minZ = Math.min(CZ + 0.5 + radius * Math.sin(start),
                               CZ + 0.5 + radius * Math.sin(end));
        double maxZ = Math.max(CZ + 0.5 + radius * Math.sin(start),
                               CZ + 0.5 + radius * Math.sin(end));
        double quarter = Math.PI / 2.0;
        int first = (int) Math.ceil(start / quarter);
        int last = (int) Math.floor(end / quarter);
        for (int k = first; k <= last; k++) {
            double a = k * quarter;
            double x = CX + 0.5 + radius * Math.cos(a);
            double z = CZ + 0.5 + radius * Math.sin(a);
            minX = Math.min(minX, x); maxX = Math.max(maxX, x);
            minZ = Math.min(minZ, z); maxZ = Math.max(maxZ, z);
        }
        int pad = 3; // account for voxel-centre sampling and primitive rounding
        return c.hits((int) Math.floor(minX) - pad, y0, (int) Math.floor(minZ) - pad,
                      (int) Math.ceil(maxX) + pad, y1, (int) Math.ceil(maxZ) + pad);
    }

    private void radialSupport(Cube c, double degrees, int innerRadius, int innerY,
                               int outerRadius, int outerY, int halfWidth, byte material) {
        double a = Math.toRadians(degrees);
        double x0 = CX + 0.5 + Math.cos(a) * innerRadius;
        double z0 = CZ + 0.5 + Math.sin(a) * innerRadius;
        double x1 = CX + 0.5 + Math.cos(a) * outerRadius;
        double z1 = CZ + 0.5 + Math.sin(a) * outerRadius;
        c.beam(x0, innerY, z0, x1, outerY, z1, halfWidth, material);
    }

    private void frontFlankPlate(Cube c, int centreX, int radius, int y0, int y1,
                                 int halfWidth, int depth, byte material, byte edgeMaterial) {
        int centreFront = CZ + cylinderFrontOffset(radius, centreX);
        int reach = halfWidth * 7 + depth + 24;
        if (!c.hits(centreX - halfWidth, y0, centreFront - reach,
                    centreX + halfWidth, y1, centreFront + depth + reach)) return;
        for (int x = Math.max(centreX - halfWidth, c.x0);
             x <= Math.min(centreX + halfWidth, c.x0 + 15); x++) {
            int front = CZ + cylinderFrontOffset(radius, x);
            c.box(x, y0, front - 14, x, y1, front + depth, material);
        }
        int ridgeX = centreX + Math.max(-halfWidth + 3, halfWidth / 2);
        int ridgeFront = CZ + cylinderFrontOffset(radius, ridgeX);
        c.box(ridgeX - 2, y0 + 18, ridgeFront + depth - 2,
              ridgeX + 2, y1 - 18, ridgeFront + depth + 4, edgeMaterial);
    }

    private void diagonalMember(Cube c, double startDegrees, int startRadius, int startY,
                                double endDegrees, int endRadius, int endY,
                                double outerWidth, double innerWidth, byte material) {
        double a0 = Math.toRadians(startDegrees), a1 = Math.toRadians(endDegrees);
        c.tube(CX + 0.5 + Math.cos(a0) * startRadius, startY,
               CZ + 0.5 + Math.sin(a0) * startRadius,
               CX + 0.5 + Math.cos(a1) * endRadius, endY,
               CZ + 0.5 + Math.sin(a1) * endRadius,
               outerWidth, innerWidth, material);
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
        for (int[] slit : SOUTH_SLITS) {
            int x = slit[0], y = slit[1], width = slit[2], height = slit[3];
            int radius = slit[4], depth = slit[5];
            int front = CZ + cylinderFrontOffset(radius, x);
            if (!c.hits(x - width / 2, y, front - depth,
                        x + width / 2, y + height, front + 1)) continue;
            c.box(x - width / 2, y, front - depth,
                  x + width / 2, y + height, front + 1, Mat.AIR);
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
            int centre = CAVE_X[i], y = CAVE_Y[i], w = CAVE_W[i], h = CAVE_H[i];
            int left = centre - w / 2, right = centre + w / 2;
            if (!c.hits(left, y, CZ + CAVE_FRONT[i] - 2320,
                        right, y + h, CZ + CAVE_FRONT[i] + 16)) continue;
            // Cut each x-column to its own point on the cylindrical surface and deep
            // enough to enter the hollow hull, rather than leaving a blind facade tunnel.
            for (int x = Math.max(left, c.x0); x <= Math.min(right, c.x0 + 15); x++) {
                int front = CZ + caveFrontAt(i, x);
                c.box(x, y, front - 2300, x, y + h, front, Mat.AIR);
            }
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
            int centre = CAVE_X[i], y = CAVE_Y[i], w = CAVE_W[i], h = CAVE_H[i];
            int l = centre - w / 2, r = centre + w / 2, top = y + h;
            if (!c.hits(l - 28, y - 22, CZ + CAVE_FRONT[i] - 220,
                        r + 28, top + 28, CZ + CAVE_FRONT[i] + 36)) continue;

            // The jambs and lintel follow the rounded hull surface column by column.
            for (int x = Math.max(l - 28, c.x0); x <= Math.min(r + 28, c.x0 + 15); x++) {
                int f = CZ + caveFrontAt(i, x);
                if (x >= l - 28 && x <= l - 8)
                    c.box(x, y - 22, f + 3, x, top + 22, f + 28, Mat.S_TOHA_PALE);
                if (x >= r + 8 && x <= r + 28)
                    c.box(x, y - 22, f + 3, x, top + 22, f + 28, Mat.S_TOHA_PALE);
                if (x >= l - 20 && x <= r + 20)
                    c.box(x, top + 8, f + 3, x, top + 28, f + 28, Mat.S_TOHA_MASS);
                if (x >= l - 16 && x <= r + 16)
                    c.box(x, y - 14, f + 3, x, y + 5, f + 28, Mat.S_TOHA_MASS);
                if (x >= l - 7 && x <= l + 6)
                    c.box(x, y, f - 10, x, top, f - 2, Mat.COAL);
                if (x >= r - 6 && x <= r + 7)
                    c.box(x, y, f - 10, x, top, f - 2, Mat.COAL);
                if (x >= l && x <= r)
                    c.box(x, top - 8, f - 10, x, top + 4, f - 2, Mat.S_DARK);
            }

            int fCentre = CZ + caveFrontAt(i, centre);
            if (i == 2) {
                c.box(centre - 65, top - 18, fCentre + 29, centre + 65, top - 12, fCentre + 31, Mat.BIO_CYAN);
            } else if (i == 12) {
                c.box(centre - 34, top - 18, fCentre + 29, centre + 34, top - 12, fCentre + 31, Mat.SEA_LANTERN);
            } else if (i == 5 || i == 8) {
                c.box(l + 12, top - 18, fCentre + 29, l + 42, top - 12, fCentre + 31, Mat.LAMP_RED);
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
        // Six fixed machine spines sit tangent to the cylindrical hull at different heights.
        drawSpine(c, -1990, CZ - 1150, 1140, 150, 64);
        drawSpine(c, -2270, CZ - 100, 1510, 165, 64);
        drawSpine(c, -1990, CZ + 1150, 920, 130, 58);
        drawSpine(c, 1990, CZ - 1150, 1240, 150, 62);
        drawSpine(c, 2270, CZ - 100, 1630, 165, 60);
        drawSpine(c, 1990, CZ + 1150, 1030, 130, 55);

        // An off-axis mast rises near the ceiling and is pinned back by huge braces.
        drawSpine(c, 2040, CZ - 900, 1975, 210, 78);
        c.beam(2160, 690, CZ + 600, 2040, 1810, CZ - 900, 30, Mat.S_TOHA_PALE);
        c.beam(2300, 1060, CZ + 80, 2040, 1910, CZ - 900, 16, Mat.S_TOHA_RING);
        c.beam(2040, 1840, CZ - 900, 900, 1780, CZ - 790, 24, Mat.S_TOHA_MASS);
        c.box(2020, 1940, CZ - 920, 2060, 1952, CZ - 880, Mat.BIO_CYAN);

        // Heavy diagonals carry the external spines into the middle hull collars.
        c.beam(-2270, 250, CZ + 200, -1500, 1110, CZ - 820, 34, Mat.S_TOHA_MASS);
        c.beam(2270, 250, CZ + 200, 1500, 1110, CZ - 820, 34, Mat.S_TOHA_MASS);
        c.beam(-1960, 670, CZ - 1420, -1050, 1490, CZ - 600, 25, Mat.S_TOHA_PALE);
        c.beam(1960, 670, CZ - 1420, 1050, 1490, CZ - 600, 25, Mat.S_TOHA_PALE);

        c.cylinder(CX + 0.5, CZ + 0.5, 1970, 1945, 1, 1, 850, 880, 0, 0, Mat.S_TOHA_RING);
        c.cylinder(CX + 0.5, CZ + 0.5, 1670, 1644, 1, 1, 1325, 1348, 0, 0, Mat.S_TOHA_RING);
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
        int zBack = CZ + 2660, zFront = CZ + 1360;
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
        c.box(-930, 28, CZ + 1720, -830, 60, CZ + 2590, Mat.S_TOHA_MASS);
        c.box(830, 28, CZ + 1720, 930, 60, CZ + 2590, Mat.S_TOHA_MASS);
        for (int z = CZ + 1790; z <= CZ + 2540; z += 150) {
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
        // Four isolated pylons align to the circular perimeter; none blocks the southern portal.
        drawSpine(c, -2200, CZ - 1500, 600, 155, 85);
        drawSpine(c, 2200, CZ - 1500, 540, 145, 80);
        drawSpine(c, -2200, CZ + 1500, 510, 135, 75);
        drawSpine(c, 2200, CZ + 1500, 640, 170, 82);

        // Two tall transfer columns sit on the side arcs of the empty approach ring.
        c.taper(-2320.5, CZ - 200.5, 82, 45, GROUND, 770, 24, Mat.S_TOHA_PALE);
        c.taper(2320.5, CZ - 200.5, 82, 45, GROUND, 710, 24, Mat.S_TOHA_PALE);
        c.cylinder(-2320.5, CZ - 200.5, 105, 82, 1, 1, 420, 434, 0, 0, Mat.S_TOHA_RING);
        c.cylinder(2320.5, CZ - 200.5, 105, 82, 1, 1, 420, 434, 0, 0, Mat.S_TOHA_RING);
    }
}
