package blame.blamegen.core;

/**
 * TOHA HEAVY INDUSTRIES — fixed, coordinate-authored spire inside a megastructure valley.
 *
 * The earlier wide, terraced drum has been replaced by a narrow, tapered industrial
 * mast. A several-kilometre canyon of stratified walls, lift shafts and broken galleries
 * frames it; the valley floor has a service causeway, machine yards and a route that
 * spirals around the spire. The inside is a real hollow space containing partial floors,
 * an eccentric gravity furnace, thirteen AI chambers and linked maintenance paths.
 *
 * All geometry is fixed and deterministic. Material weathering is resolved by Cube.
 */
public final class Toha {
    public static final int CX = 0, CZ = -4608;

    /** The Toha owner deliberately reserves a broad canyon volume, not the building outline. */
    public static final int HALF = 4096;
    public static final int X0 = CX - HALF, X1 = CX + HALF;
    public static final int Z0 = CZ - HALF, Z1 = CZ + HALF;

    private static final int GROUND = 64;
    public static final int TOP = 2030;
    private static final int VALLEY_HALF = 3200;
    private static final int VALLEY_LENGTH = 3560;
    private static final double TAU = Math.PI * 2.0;

    // Nine large floor plates separated by open trenches. The gaps keep the canyon
    // from reading as a flat plaza but give every route a reliable ground datum.
    // Band rows: relative Z min, relative Z max, half-width in X.
    private static final int[][] VALLEY_FLOOR_BANDS = {
        {-3560, -2920, 2380}, {-2740, -2160, 2650}, {-1980, -1410, 2300},
        {-1190, -610, 2860}, {-390, 280, 2280}, {520, 1120, 2800},
        {1370, 1970, 2400}, {2240, 2860, 2860}, {3100, 3560, 2480}
    };

    // Thirteen authored AI chambers. CAVE_X is a local tangent offset; CAVE_DIR is
    // south/east/north/west. Entries shrink with the spire instead of making a window grid.
    static final int[] CAVE_X = {-110, 38, 120, -55, 150, -75, 35, 12, -28, 22, -8, 12, 0};
    static final int[] CAVE_Y = {120, 260, 405, 545, 690, 835, 990, 1145, 1300, 1455, 1608, 1780, 1912};
    static final int[] CAVE_W = {88, 64, 92, 70, 66, 58, 58, 48, 42, 36, 32, 28, 22};
    static final int[] CAVE_H = {112, 96, 120, 102, 92, 94, 92, 76, 70, 62, 52, 44, 34};
    static final int[] CAVE_FRONT = {410, 380, 350, 323, 295, 266, 235, 206, 176, 146, 116, 78, 42};
    private static final int[] CAVE_DIR = {0, 0, 0, 0, 1, 1, 1, 2, 2, 3, 3, 0, 2};
    private static final int[] CAVE_DEPTH = {100, 88, 92, 84, 82, 78, 76, 70, 66, 62, 54, 48, 38};

    // Vertical block-raster mark 東亜重工, welded to the south-east-facing skin.
    // A compact cell size keeps the complete mark on the narrow lower spire.
    private static final String[][] HULL_INSCRIPTION = {
        {
            "00000110000000", "00000110000000", "11111111111111", "00000110000000",
            "00011111111000", "00000110000000", "11111111111111", "00000110000000",
            "00111111111100", "00100110001000", "01100110001100", "10000110000010",
            "00000110000000", "00000110000000"
        },
        {
            "00111111111100", "00100000000100", "00100000000100", "00111111111100",
            "00000111100000", "00000110100000", "11111111111111", "00100110100100",
            "00100110100100", "00100110100100", "00111111111100", "00000110100000",
            "00000110100000", "00111111111100"
        },
        {
            "00011111111000", "00000110000000", "11111111111111", "00000110000000",
            "00111111111100", "00000110000000", "11111111111111", "00100110001000",
            "00111111111100", "00100110001000", "00111111111100", "00100110001000",
            "00111111111100", "11111111111111"
        },
        {
            "11111111111111", "00000011000000", "00000011000000", "00000011000000",
            "00000011000000", "00000011000000", "00000011000000", "00000011000000",
            "00000011000000", "00000011000000", "00000011000000", "00000011000000",
            "00000011000000", "11111111111111"
        }
    };

    Toha(Megastructure m) {
        // The monument layout is authored and independent of the world seed.
    }

    /** Slowly taper the 840-block base into a thin crown; no broad upper drums. */
    static double spireRadiusAtY(int y) {
        double t = Math.max(0.0, Math.min(1.0, (double) (y - GROUND) / (TOP - GROUND)));
        return 420.0 + (18.0 - 420.0) * t;
    }

    static double furnaceRadiusAtY(int y) {
        double t = Math.max(0.0, Math.min(1.0, (double) (y - 130) / (TOP - 130)));
        return 118.0 + (13.0 - 118.0) * t;
    }

    static int cylinderRadiusAtY(int y) {
        return (int) Math.round(spireRadiusAtY(y));
    }

    static int cylinderFrontOffset(int radius, int x) {
        double dx = x + 0.5 - CX;
        return (int) Math.ceil(Math.sqrt(Math.max(0.0, radius * (double) radius - dx * dx))) + 12;
    }

    static int caveRadius(int i) {
        return cylinderRadiusAtY(CAVE_Y[i]);
    }

    static int caveDirection(int i) {
        return CAVE_DIR[i];
    }

    static int caveFrontAt(int i, int x) {
        return cylinderFrontOffset(caveRadius(i), x);
    }

    public static int projectedFrontZ(int x, int y) {
        return CZ + cylinderFrontOffset(cylinderRadiusAtY(y), x);
    }

    static int caveCenterX(int i) {
        int r = caveRadius(i);
        int tangent = CAVE_X[i];
        int normal = (int) Math.round(Math.sqrt(Math.max(0.0, r * (double) r - tangent * (double) tangent)));
        switch (CAVE_DIR[i]) {
            case 1: return CX + normal;
            case 3: return CX - normal;
            default: return CX + tangent;
        }
    }

    static int caveCenterZ(int i) {
        int r = caveRadius(i);
        int tangent = CAVE_X[i];
        int normal = (int) Math.round(Math.sqrt(Math.max(0.0, r * (double) r - tangent * (double) tangent)));
        switch (CAVE_DIR[i]) {
            case 0: return CZ + normal;
            case 2: return CZ - normal;
            default: return CZ + tangent;
        }
    }

    /** The external spiral advances about three blocks around the shell per vertical block. */
    static double spiralAngleAtY(int y) {
        double r = Math.max(18.0, spireRadiusAtY(y));
        return 14.2 * Math.log(420.0 / r);
    }

    static int spiralXAtY(int y) {
        double r = spireRadiusAtY(y) + 7.0, a = spiralAngleAtY(y);
        return CX + (int) Math.round(Math.sin(a) * r);
    }

    static int spiralZAtY(int y) {
        double r = spireRadiusAtY(y) + 7.0, a = spiralAngleAtY(y);
        return CZ + (int) Math.round(Math.cos(a) * r);
    }

    boolean overlaps(Cube c) {
        return c.x0 + 15 >= X0 && c.x0 < X1
                && c.z0 + 15 >= Z0 && c.z0 < Z1;
    }

    void draw(Cube c) {
        if (c.y0 > TOP + 16 || c.y0 + 15 < 0) return;

        drawValleyWalls(c);
        drawValleyEndMasses(c);
        drawValleyFloor(c);
        drawBaseApron(c);
        drawSpireShell(c);
        drawSpireRibsAndCourses(c);
        drawSpiralWalkway(c); // carve the chamber doors after the route so the landings remain open
        carveCaveMouths(c);
        carveBaseIngress(c);
        drawCaveFramesAndMachines(c);
        drawBaseIngressFrames(c);
        drawFurnaceAndInnerPlatforms(c);
        drawCrossValleyLinks(c);
        drawCliffEquipment(c);
        drawMachineYard(c);
        drawApproachCauseway(c);
        drawHullInscription(c);
    }

    private void drawValleyFloor(Cube c) {
        for (int i = 0; i < VALLEY_FLOOR_BANDS.length; i++) {
            int z0 = CZ + VALLEY_FLOOR_BANDS[i][0];
            int z1 = CZ + VALLEY_FLOOR_BANDS[i][1];
            int halfWidth = VALLEY_FLOOR_BANDS[i][2];
            if (!c.hits(-halfWidth, 60, z0, halfWidth, 63, z1)) continue;
            c.box(-halfWidth, 60, z0, halfWidth, 63, z1, Mat.S_TOHA_DECK);
            // Obsidian-black expansion joints and pale battered lips articulate the plates.
            c.box(-halfWidth, 63, z0, halfWidth, 63, z0 + 3, Mat.COAL);
            c.box(-halfWidth, 63, z1 - 3, halfWidth, 63, z1, Mat.S_TOHA_PALE);
            for (int x = -halfWidth + 160; x < halfWidth; x += 320) {
                if (!c.hits(x - 2, 63, z0, x + 2, 63, z1)) continue;
                c.box(x, 63, z0, x + 1, 63, z1, Mat.SLAB_TOP);
            }
        }

        // Cross-cut trenches have occasional grated crossings, drainage conduits, and
        // narrow slab stepping-lines at deliberately uneven distances.
        int[] trenchOffsets = {-2840, -2070, -1320, -520, 390, 1250, 2110, 2990};
        for (int i = 0; i < trenchOffsets.length; i++) {
            int z = CZ + trenchOffsets[i];
            if (!c.hits(-2850, 60, z - 5, 2850, 67, z + 5)) continue;
            c.box(-2850, 60, z - 5, 2850, 63, z + 5, Mat.AIR);
            int bridgeX = (i % 2 == 0) ? -700 : 600;
            c.box(bridgeX - 220, 60, z - 9, bridgeX + 220, 63, z + 9, Mat.SLAB_BOTTOM);
            c.box(bridgeX - 220, 64, z - 12, bridgeX - 214, 71, z - 9, Mat.IRON_BARS);
            c.box(bridgeX + 214, 64, z + 9, bridgeX + 220, 71, z + 12, Mat.IRON_BARS);
        }
    }

    // ---------------------------------------------------------------- valley walls
    // These are the enclosing megastructure, not decoration around a freestanding tower.
    // Their inner faces carry long broken ledges, deep shaft mouths and giant service ribs.
    private void drawValleyWalls(Cube c) {
        int z0 = CZ - VALLEY_LENGTH, z1 = CZ + VALLEY_LENGTH;
        int[] slotZ = {-2720, -1500, -320, 920, 2240, 3100};
        int[] slotY = {250, 420, 760, 340, 880, 520};
        int[] slotH = {250, 360, 300, 420, 330, 250};

        for (int side : new int[] {-1, 1}) {
            int wallOuter = side < 0 ? -3300 : 3180;
            int wallInner = side < 0 ? -3180 : 3300;
            int innerEdge = side < 0 ? -2940 : 2940;
            int outerLo = Math.min(wallOuter, wallInner), outerHi = Math.max(wallOuter, wallInner);
            if (!c.hits(outerLo - 140, GROUND, z0, outerHi + 140, TOP, z1)) continue;

            c.box(outerLo, GROUND, z0, outerHi, TOP - 22, z1, Mat.S_TOHA_MASS);

            // Deep openings interrupt the cliff at irregular intervals. Their lintels
            // are enormous, their reveals dark, and their interiors hold machinery.
            for (int i = 0; i < slotZ.length; i++) {
                int zc = CZ + slotZ[i] + (side < 0 ? 0 : (i % 2 == 0 ? 170 : -140));
                int bottom = slotY[i], top = bottom + slotH[i];
                c.box(outerLo - 2, bottom, zc - 62, outerHi + 2, top, zc + 62, Mat.AIR);
                c.box(outerLo - 4, bottom - 10, zc - 76, outerHi + 4, bottom - 2, zc + 76, Mat.COAL);
                c.box(outerLo - 4, top, zc - 78, outerHi + 4, top + 15, zc + 78, Mat.S_TOHA_PALE);
                c.box(outerLo - 4, bottom, zc - 78, outerHi + 4, top, zc - 67, Mat.S_TOHA_RING);
                c.box(outerLo - 4, bottom, zc + 67, outerHi + 4, top, zc + 78, Mat.S_TOHA_RING);
            }

            // Unequal cliff ribs rise into the artificial roof; some are load paths,
            // others are exposed vertical shafts carrying pipes and lift cages.
            int[] ribZ = {-3350, -2850, -2260, -1660, -940, -210, 540, 1280, 2060, 2740, 3390};
            int[] ribTop = {1640, 1980, 1410, 1900, 1740, 2020, 1480, 1890, 1610, 2000, 1780};
            for (int i = 0; i < ribZ.length; i++) {
                int zc = CZ + ribZ[i];
                int halfW = 32 + (i % 4) * 16;
                int inward = side < 0 ? innerEdge : innerEdge;
                int ribIn = side < 0 ? inward + 80 : inward - 80;
                c.box(Math.min(ribIn, innerEdge), GROUND, zc - halfW,
                       Math.max(ribIn, innerEdge), ribTop[i], zc + halfW, Mat.S_TOHA_RING);
                c.box(Math.min(ribIn, innerEdge), ribTop[i] - 28, zc - halfW - 7,
                       Math.max(ribIn, innerEdge), ribTop[i] - 18, zc + halfW + 7, Mat.S_TOHA_PALE);
                c.box(Math.min(ribIn, innerEdge) + 8, GROUND + 24, zc - 3,
                       Math.max(ribIn, innerEdge) - 8, ribTop[i] - 40, zc + 3, Mat.COAL);
            }

            // Six levels of service path cling to the cliff, broken by deliberate gaps.
            int[] ledgeY = {210, 455, 735, 1015, 1300, 1585, 1870};
            for (int level = 0; level < ledgeY.length; level++) {
                int y = ledgeY[level];
                int lx0 = side < 0 ? -3190 : 2940;
                int lx1 = side < 0 ? -2940 : 3190;
                c.box(lx0, y, z0 + 40, lx1, y, z1 - 40, Mat.SLAB_BOTTOM);
                // Deliberate broken spans make this a dangerous service route rather
                // than a smooth, decorative ring around the valley.
                int[] breaks = {-3000, -1880, -520, 840, 2180, 3060};
                for (int gap = 0; gap < breaks.length; gap++) {
                    if ((gap + level + (side < 0 ? 0 : 1)) % 3 != 0) continue;
                    int gz = CZ + breaks[gap];
                    c.box(lx0 - 2, y, gz - 56, lx1 + 2, y + 1, gz + 56, Mat.AIR);
                }
                int railX = side < 0 ? -2936 : 2936;
                c.box(railX - 2, y + 1, z0 + 40, railX + 2, y + 8, z1 - 40, Mat.IRON_BARS);
                for (int gap = 0; gap < breaks.length; gap++) {
                    if ((gap + level + (side < 0 ? 0 : 1)) % 3 != 0) continue;
                    int gz = CZ + breaks[gap];
                    c.box(railX - 3, y + 1, gz - 60, railX + 3, y + 9, gz + 60, Mat.AIR);
                }
                // Wall brackets repeat at long intervals, not as fine noise.
                for (int k = -3; k <= 3; k++) {
                    int sz = CZ + k * 900 + (level % 2) * 180;
                    if (c.hits(lx0 - 100, y - 24, sz - 14, lx1 + 100, y - 4, sz + 14)) {
                        int bx0 = side < 0 ? -3260 : 3000;
                        int bx1 = side < 0 ? -3080 : 3260;
                        c.box(bx0, y - 26, sz - 10, bx1, y - 4, sz + 10, Mat.S_TOHA_MASS);
                        c.box(bx0, y - 5, sz - 13, bx1, y - 2, sz + 13, Mat.IRON_BLOCK);
                    }
                }
            }
        }
    }

    private void drawValleyEndMasses(Cube c) {
        int northZ = CZ - VALLEY_LENGTH, southZ = CZ + VALLEY_LENGTH;
        if (c.hits(X0, GROUND, southZ - 150, X1, TOP, southZ + 150)) {
            // The southern canyon mouth is a monumental break in the megastructure.
            c.box(-3320, GROUND, southZ - 120, -520, 1510, southZ + 120, Mat.S_TOHA_MASS);
            c.box(520, GROUND, southZ - 120, 3320, 1700, southZ + 120, Mat.S_TOHA_MASS);
            c.box(-620, 1450, southZ - 160, 620, 1610, southZ + 160, Mat.S_TOHA_RING);
            c.box(-540, GROUND, southZ - 95, -390, 1530, southZ + 95, Mat.S_TOHA_PALE);
            c.box(390, GROUND, southZ - 95, 540, 1710, southZ + 95, Mat.S_TOHA_PALE);
            c.box(-520, 1530, southZ - 100, 520, 1540, southZ + 100, Mat.SLAB_BOTTOM);
            for (int x : new int[] {-2800, -1800, -900, 900, 1800, 2800}) {
                int h = 750 + Math.abs(x) / 2;
                c.box(x - 55, GROUND, southZ - 170, x + 55, h, southZ + 170, Mat.S_TOHA_RING);
                c.box(x - 80, h - 20, southZ - 186, x + 80, h - 7, southZ + 186, Mat.S_TOHA_PALE);
            }
        }
        if (c.hits(X0, GROUND, northZ - 150, X1, TOP, northZ + 150)) {
            // The far wall is fractured and higher; its gaps imply further strata beyond.
            for (int i = 0; i < 9; i++) {
                int x = -3300 + i * 825;
                int h = 960 + (i % 4) * 250;
                if (i == 4 || i == 7) continue;
                c.box(x, GROUND, northZ - 140, x + 280 + (i % 3) * 60, h, northZ + 140, Mat.S_TOHA_MASS);
                c.box(x - 28, h - 22, northZ - 160, x + 308 + (i % 3) * 60, h - 6, northZ + 160, Mat.S_TOHA_PALE);
            }
            c.box(-3320, 1570, northZ - 100, -1150, 1700, northZ + 100, Mat.S_TOHA_RING);
            c.box(1200, 1260, northZ - 100, 3320, 1400, northZ + 100, Mat.S_TOHA_RING);
        }
    }

    // ------------------------------------------------------------------- central spire
    private void drawBaseApron(Cube c) {
        if (!c.hits(CX - 760, 48, CZ - 760, CX + 760, 115, CZ + 760)) return;
        c.cylinder(CX + 0.5, CZ + 0.5, 710, 455, 1, 1, 64, 76,
                Math.PI / 2 + 0.26, 0.58, Mat.S_TOHA_MASS);
        c.cylinder(CX + 0.5, CZ + 0.5, 735, 700, 1, 1, 64, 67,
                0, 0, Mat.S_TOHA_PALE);
        c.cylinder(CX + 0.5, CZ + 0.5, 470, 445, 1, 1, 76, 82,
                Math.PI / 2 + 0.26, 0.58, Mat.S_TOHA_RING);
        // Four radial machine aprons interrupt the ring; the south one receives the road.
        for (int deg : new int[] {0, 90, 180, 270}) {
            double a = Math.toRadians(deg);
            int ex = (int) Math.round(Math.sin(a) * 680);
            int ez = (int) Math.round(Math.cos(a) * 680);
            c.box(Math.min(CX, CX + ex) - 15, 70, Math.min(CZ, CZ + ez) - 15,
                   Math.max(CX, CX + ex) + 15, 73, Math.max(CZ, CZ + ez) + 15, Mat.SLAB_BOTTOM);
        }
        for (int y : new int[] {92, 104}) {
            c.cylinder(CX + 0.5, CZ + 0.5, 455, 438, 1, 1, y, y + 4,
                    0, 0.75, Mat.COPPER_PATINA);
        }
    }

    private void drawSpireShell(Cube c) {
        c.taper(CX + 0.5, CZ + 0.5, 420, 18, GROUND, TOP, 8, Mat.S_TOHA_MASS);
        // The annular wall leaves a genuine, tall interior void.
        c.taper(CX + 0.5, CZ + 0.5, 384, 3, GROUND + 6, TOP - 4, 8, Mat.AIR);
    }

    private void drawSpireRibsAndCourses(Cube c) {
        if (!c.hits(CX - 475, GROUND, CZ - 475, CX + 475, TOP, CZ + 475)) return;

        // Six major ribs climb the narrowing silhouette. Segmented spans avoid expensive
        // kilometre-long beam bounds in unrelated cubes.
        double[] angles = {0.20, 1.12, 2.15, 3.02, 3.85, 4.80, 5.55};
        int firstY = Math.max(GROUND, GROUND + Math.floorDiv(Math.max(0, c.y0 - GROUND) - 96, 96) * 96);
        int lastY = Math.min(TOP, c.y0 + 15 + 96);
        for (int y = firstY; y < lastY; y += 96) {
            int y1 = Math.min(TOP, y + 96);
            double r0 = spireRadiusAtY(y) + 4;
            double r1 = spireRadiusAtY(y1) + 4;
            for (int i = 0; i < angles.length; i++) {
                double a = angles[i];
                double x0 = CX + Math.sin(a) * r0, z0 = CZ + Math.cos(a) * r0;
                double x1 = CX + Math.sin(a) * r1, z1 = CZ + Math.cos(a) * r1;
                c.beam(x0, y, z0, x1, y1, z1, (i % 3 == 0 ? 12 : 7),
                        i % 2 == 0 ? Mat.S_TOHA_PALE : Mat.S_TOHA_RING);
            }
        }

        // Broken orbital maintenance rings, each a different arc and radius.
        int[] galleryY = {146, 224, 326, 442, 568, 704, 846, 1002, 1165, 1326, 1488, 1645, 1796, 1926};
        int[] gallerySpan = {215, 150, 190, 138, 210, 132, 196, 148, 180, 122, 156, 118, 130, 88};
        for (int i = 0; i < galleryY.length; i++) {
            int y = galleryY[i];
            if (y < c.y0 - 18 || y > c.y0 + 32) continue;
            double radius = spireRadiusAtY(y) + 30 + (i % 3) * 10;
            double start = Math.toRadians((i * 71 + 18) % 360);
            drawArc(c, radius + 12, Math.max(8, radius - 12), y, y + (i % 2 == 0 ? 7 : 4),
                    start, Math.toRadians(gallerySpan[i]),
                    i % 3 == 0 ? Mat.S_TOHA_PALE : Mat.S_TOHA_RING);
            if (i % 2 == 0) {
                double a = start + Math.toRadians(gallerySpan[i] * 0.65);
                double r0 = spireRadiusAtY(y) - 18, r1 = radius + 6;
                c.beam(CX + Math.sin(a) * r0, y - 20, CZ + Math.cos(a) * r0,
                       CX + Math.sin(a) * r1, y + 2, CZ + Math.cos(a) * r1, 4, Mat.S_TOHA_MASS);
            }
        }

        // Short black/pale screentone courses keep the surface from reading as a smooth tube.
        int[] seamY = {92, 186, 290, 390, 505, 642, 790, 950, 1110, 1270, 1440, 1600, 1755, 1890, 1992};
        for (int y : seamY) {
            if (y < c.y0 - 6 || y > c.y0 + 22) continue;
            double r = spireRadiusAtY(y) + 1;
            c.cylinder(CX + 0.5, CZ + 0.5, r + 3, Math.max(2, r - 3), 1, 1,
                    y, y + 2, 0, 0, (y % 3 == 0) ? Mat.S_TOHA_PALE : Mat.COAL);
        }
    }

    private void drawArc(Cube c, double outer, double inner, int y0, int y1,
                         double start, double span, byte mat) {
        if (span <= 0 || span >= TAU) {
            c.cylinder(CX + 0.5, CZ + 0.5, outer, inner, 1, 1, y0, y1, 0, 0, mat);
            return;
        }
        // Cube.cylinder omits a gap sector. The complement of this gap is the requested arc.
        double end = start + span;
        c.cylinder(CX + 0.5, CZ + 0.5, outer, inner, 1, 1, y0, y1,
                end, TAU - span, mat);
    }

    // ------------------------------------------------------------------- AI cave mouths
    private void carveCaveMouths(Cube c) {
        for (int i = 0; i < CAVE_Y.length; i++) {
            int x = caveCenterX(i), z = caveCenterZ(i), y0 = CAVE_Y[i] - CAVE_H[i] / 2;
            int y1 = CAVE_Y[i] + CAVE_H[i] / 2;
            int halfW = CAVE_W[i] / 2;
            int depth = CAVE_DEPTH[i];
            int side = CAVE_DIR[i];
            if (side == 0) {
                c.box(x - halfW, y0, z - depth, x + halfW, y1, z + 9, Mat.AIR);
            } else if (side == 2) {
                c.box(x - halfW, y0, z - 9, x + halfW, y1, z + depth, Mat.AIR);
            } else if (side == 1) {
                c.box(x - depth, y0, z - halfW, x + 9, y1, z + halfW, Mat.AIR);
            } else {
                c.box(x - 9, y0, z - halfW, x + depth, y1, z + halfW, Mat.AIR);
            }
        }
    }

    /**
     * Two low service portals pass through the otherwise solid toe of the hollow spire.
     * The paired entrances let the north and south causeways meet inside the base.
     */
    private void carveBaseIngress(Cube c) {
        if (!c.hits(-56, GROUND + 1, CZ - 450, 56, 153, CZ + 450)) return;
        c.box(-49, GROUND + 1, CZ + 286, 49, 153, CZ + 451, Mat.AIR);
        c.box(-49, GROUND + 1, CZ - 451, 49, 153, CZ - 286, Mat.AIR);
    }

    private void drawBaseIngressFrames(Cube c) {
        int zSouth = CZ + 432, zNorth = CZ - 432;
        if (c.hits(-72, 64, CZ + 280, 72, 164, CZ + 454)) {
            c.box(-62, 65, CZ + 290, -50, 145, CZ + 446, Mat.S_TOHA_RING);
            c.box(50, 65, CZ + 290, 62, 145, CZ + 446, Mat.S_TOHA_RING);
            c.box(-68, 145, CZ + 286, 68, 160, CZ + 450, Mat.S_TOHA_PALE);
            c.box(-62, 148, CZ + 290, 62, 152, CZ + 446, Mat.COAL);
            c.box(-42, 145, CZ + 443, 42, 148, CZ + 450, Mat.BIO_CYAN);
        }
        if (c.hits(-72, 64, CZ - 454, 72, 164, CZ - 280)) {
            c.box(-62, 65, CZ - 446, -50, 145, CZ - 290, Mat.S_TOHA_RING);
            c.box(50, 65, CZ - 446, 62, 145, CZ - 290, Mat.S_TOHA_RING);
            c.box(-68, 145, CZ - 450, 68, 160, CZ - 286, Mat.S_TOHA_PALE);
            c.box(-62, 148, CZ - 446, 62, 152, CZ - 290, Mat.COAL);
            c.box(-42, 145, CZ - 450, 42, 148, CZ - 443, Mat.COPPER_PATINA);
        }
    }

    private void drawCaveFramesAndMachines(Cube c) {
        for (int i = 0; i < CAVE_Y.length; i++) {
            int x = caveCenterX(i), z = caveCenterZ(i), y0 = CAVE_Y[i] - CAVE_H[i] / 2;
            int y1 = CAVE_Y[i] + CAVE_H[i] / 2;
            int halfW = CAVE_W[i] / 2, dir = CAVE_DIR[i];
            int leftX = x - halfW - 7, rightX = x + halfW + 7;
            int nearZ = z - halfW - 7, farZ = z + halfW + 7;
            if (dir == 0 || dir == 2) {
                int faceZ = dir == 0 ? z + 2 : z - 2;
                c.box(leftX, y0, faceZ - 3, leftX + 7, y1 + 4, faceZ + 3, Mat.S_TOHA_PALE);
                c.box(rightX - 7, y0, faceZ - 3, rightX, y1 + 4, faceZ + 3, Mat.S_TOHA_PALE);
                c.box(leftX, y1, faceZ - 6, rightX, y1 + 10, faceZ + 6, Mat.S_TOHA_RING);
                if (y0 > GROUND + 6)
                    c.box(leftX, y0 - 5, faceZ - 5, rightX, y0 + 1, faceZ + 5, Mat.SLAB_TOP);
            } else {
                int faceX = dir == 1 ? x + 2 : x - 2;
                c.box(faceX - 3, y0, nearZ, faceX + 3, y1 + 4, nearZ + 7, Mat.S_TOHA_PALE);
                c.box(faceX - 3, y0, farZ - 7, faceX + 3, y1 + 4, farZ, Mat.S_TOHA_PALE);
                c.box(faceX - 6, y1, nearZ, faceX + 6, y1 + 10, farZ, Mat.S_TOHA_RING);
            }

            // Recessed control surface and two small indicators sit inside each chamber.
            int ix = x, iz = z;
            if (dir == 0) iz -= Math.max(24, CAVE_DEPTH[i] - 24);
            if (dir == 2) iz += Math.max(24, CAVE_DEPTH[i] - 24);
            if (dir == 1) ix -= Math.max(24, CAVE_DEPTH[i] - 24);
            if (dir == 3) ix += Math.max(24, CAVE_DEPTH[i] - 24);
            c.box(ix - Math.max(4, halfW / 4), CAVE_Y[i] - 16, iz - 3,
                  ix + Math.max(4, halfW / 4), CAVE_Y[i] + 16, iz + 3, Mat.IRON_BLOCK);
            int consoleHalf = Math.max(2, Math.min(5, halfW / 6));
            c.box(ix - consoleHalf, CAVE_Y[i] - 5, iz - 4,
                  ix + consoleHalf, CAVE_Y[i] + 5, iz + 4, Mat.MACHINE_DISPENSER);
            c.box(ix - consoleHalf - 5, CAVE_Y[i] - 6, iz - 3,
                  ix - consoleHalf - 1, CAVE_Y[i] + 6, iz + 3, Mat.IRON_TRAPDOOR);
            c.box(ix - 3, CAVE_Y[i] + 20, iz - 2, ix + 3, CAVE_Y[i] + 27, iz + 2,
                  i == 0 || i == 12 ? Mat.BIO_CYAN : Mat.COAL);
            c.box(ix - 10, CAVE_Y[i] + 24, iz - 2, ix - 6, CAVE_Y[i] + 27, iz + 2,
                  i == 0 ? Mat.LAMP_RED : Mat.COPPER_PATINA);

            drawCaveMachine(c, i, ix, CAVE_Y[i], iz, dir, halfW);
        }
    }

    private void drawCaveMachine(Cube c, int index, int x, int y, int z, int dir, int halfW) {
        int tangent = Math.max(20, halfW - 12);
        int inwardX = dir == 1 ? -1 : dir == 3 ? 1 : 0;
        int inwardZ = dir == 0 ? -1 : dir == 2 ? 1 : 0;
        int mx = x + inwardX * 42, mz = z + inwardZ * 42;

        switch (index % 4) {
            case 0:
                for (int s : new int[] {-1, 1}) {
                    int tx = mx + (dir == 0 || dir == 2 ? s * tangent / 2 : 0);
                    int tz = mz + (dir == 1 || dir == 3 ? s * tangent / 2 : 0);
                    c.cylinder(tx, tz, 13, 8, 1, 1, y - 25, y + 28, 0, 0, Mat.COPPER_RAW);
                    c.cylinder(tx, tz, 19, 13, 1, 1, y - 23, y - 17, 0, 0, Mat.S_TOHA_PALE);
                    c.cylinder(tx, tz, 19, 13, 1, 1, y + 18, y + 24, 0, 0, Mat.S_TOHA_RING);
                }
                break;
            case 1:
                c.ring(mx, mz, 38, 25, y - 22, y - 12, 7, Mat.COPPER_PATINA);
                c.ring(mx, mz, 38, 25, y + 7, y + 17, 7, Mat.S_TOHA_RING);
                c.beam(mx - 42, y - 2, mz, mx + 42, y - 2, mz, 3, Mat.IRON_BLOCK);
                c.beam(mx, y - 2, mz - 29, mx, y - 2, mz + 29, 3, Mat.IRON_BLOCK);
                break;
            case 2:
                c.box(mx - 30, y - 34, mz - 26, mx - 24, y + 38, mz + 26, Mat.IRON_BARS);
                c.box(mx + 24, y - 34, mz - 26, mx + 30, y + 38, mz + 26, Mat.IRON_BARS);
                c.box(mx - 30, y + 30, mz - 26, mx + 30, y + 36, mz + 26, Mat.S_TOHA_MASS);
                c.tube(mx - 18, y - 30, mz, mx + 18, y + 24, mz, 6, 3, Mat.COPPER_WEATHERED);
                break;
            default:
                c.taper(mx, mz, 26, 14, y - 36, y + 42, 8, Mat.S_TOHA_RING);
                c.taper(mx, mz, 17, 7, y - 34, y + 40, 8, Mat.AIR);
                c.cylinder(mx, mz, 32, 24, 1, 1, y + 6, y + 12, 0, 0, Mat.S_TOHA_PALE);
                break;
        }
    }

    // ------------------------------------------------------------------ gravity furnace and hollow nave
    private void drawFurnaceAndInnerPlatforms(Cube c) {
        if (!c.hits(CX - 430, GROUND, CZ - 430, CX + 430, TOP, CZ + 430)) return;
        c.taper(CX + 0.5, CZ - 36 + 0.5, 118, 13, 118, TOP - 12, 10, Mat.S_TOHA_RING);
        c.taper(CX + 0.5, CZ - 36 + 0.5, 89, 3, 124, TOP - 18, 10, Mat.AIR);

        int[] levels = {150, 286, 430, 580, 735, 890, 1045, 1200, 1350, 1490, 1630, 1765, 1885, 1970};
        for (int i = 0; i < levels.length; i++) {
            int y = levels[i];
            if (y < c.y0 - 6 || y > c.y0 + 25) continue;
            double bodyR = spireRadiusAtY(y);
            double coreR = furnaceRadiusAtY(y);
            c.cylinder(CX + 0.5, CZ - 36 + 0.5, coreR + 18, Math.max(3, coreR - 8),
                    1, 1, y, y + 8, 0, 0, i % 2 == 0 ? Mat.S_TOHA_PALE : Mat.S_TOHA_RING);
            if (bodyR > coreR + 82) {
                // Partial ring floors instead of complete slabs. Their mouths alternate
                // around the chamber, producing a real circulation path around the core.
                double outer = bodyR - 12, inner = coreR + 46;
                double gapStart = Math.toRadians((i * 83 + 28) % 360);
                drawArc(c, outer, inner, y, y + 2, gapStart, Math.toRadians(286),
                        Mat.SLAB_BOTTOM);
                if (i % 2 == 0) {
                    c.box(-((int) outer), y + 3, CZ - 4, -(int) (inner), y + 8, CZ + 4, Mat.IRON_BARS);
                    c.box((int) inner, y + 3, CZ - 4, (int) outer, y + 8, CZ + 4, Mat.IRON_BARS);
                }
                // Short spokes are actual supports for the partial platforms.
                c.box(-(int) outer, y - 18, CZ - 4, -(int) inner, y - 5, CZ + 4, Mat.S_TOHA_MASS);
                c.box((int) inner, y - 18, CZ - 4, (int) outer, y - 5, CZ + 4, Mat.S_TOHA_MASS);
                c.box(CX - 4, y - 18, CZ - (int) outer, CX + 4, y - 5, CZ - (int) inner, Mat.S_TOHA_MASS);
                c.box(CX - 4, y - 18, CZ + (int) inner, CX + 4, y - 5, CZ + (int) outer, Mat.S_TOHA_MASS);
            }
        }

        // The furnace is wrapped in exposed pressure lines, supports and suspended machinery.
        for (int y = Math.max(180, c.y0 - 12); y <= Math.min(TOP - 40, c.y0 + 15); y += 12) {
            int r = (int) Math.round(furnaceRadiusAtY(y) + 22);
            if (y % 48 == 12) {
                c.box(-r, y, CZ - 36 - 3, r, y + 2, CZ - 36 + 3, Mat.COPPER_PATINA);
            }
        }
        int[] armY = {250, 690, 1110, 1510, 1840};
        for (int y : armY) {
            if (y < c.y0 - 6 || y > c.y0 + 22) continue;
            int r = (int) Math.round(furnaceRadiusAtY(y));
            c.beam(-r - 35, y, CZ - 36, r + 35, y, CZ - 36, 8, Mat.S_TOHA_MASS);
            c.beam(0, y, CZ - 36 - r - 35, 0, y, CZ - 36 + r + 35, 5, Mat.S_TOHA_PALE);
            c.box(-r - 8, y + 2, CZ - 36 - 9, -r + 8, y + 11, CZ - 36 + 9, Mat.IRON_BLOCK);
            c.box(r - 8, y + 2, CZ - 36 - 9, r + 8, y + 11, CZ - 36 + 9, Mat.IRON_BLOCK);
        }
    }

    // --------------------------------------------------------------------- paths
    private void drawSpiralWalkway(Cube c) {
        if (!c.hits(CX - 470, GROUND, CZ - 470, CX + 470, TOP, CZ + 470)) return;
        int firstY = Math.max(GROUND + 2, c.y0 - 1);
        int lastY = Math.min(TOP - 3, c.y0 + 15);
        for (int y = firstY; y <= lastY; y++) {
            double a = spiralAngleAtY(y);
            double r = spireRadiusAtY(y) + 7.0;
            int x = CX + (int) Math.round(Math.sin(a) * r);
            int z = CZ + (int) Math.round(Math.cos(a) * r);
            // Bottom slabs overlap from tread to tread so this reads as one climbable helix.
            c.tiltSlab(x, y, z, 25, 18, a, Mat.SLAB_BOTTOM);
            if (y % 32 == 0) {
                int railX = CX + (int) Math.round(Math.sin(a) * (r + 12));
                int railZ = CZ + (int) Math.round(Math.cos(a) * (r + 12));
                c.box(railX - 2, y + 1, railZ - 2, railX + 2, y + 9, railZ + 2, Mat.IRON_BARS);
                c.box(railX - 5, y + 8, railZ - 5, railX + 5, y + 10, railZ + 5,
                      y % 64 == 0 ? Mat.COPPER_PATINA : Mat.IRON_BARS);
            }
            if (y % 64 == 0) {
                int lightX = CX + (int) Math.round(Math.sin(a) * (r - 5));
                int lightZ = CZ + (int) Math.round(Math.cos(a) * (r - 5));
                c.box(lightX - 1, y + 1, lightZ - 1, lightX + 1, y + 2, lightZ + 1, Mat.BIO_CYAN);
            }
        }
    }

    private void drawCrossValleyLinks(Cube c) {
        int[] levels = {285, 555, 840, 1135, 1430, 1725, 1920};
        for (int i = 0; i < levels.length; i++) {
            int y = levels[i], r = (int) spireRadiusAtY(y);
            if (!c.hits(-VALLEY_HALF, y - 150, CZ - 50, VALLEY_HALF, y + 16, CZ + 50)) continue;
            int innerEnd = (int) Math.round(furnaceRadiusAtY(y) + 42);
            int leftEnd = -innerEnd, rightStart = innerEnd;

            // Two staggered bridges land on the internal ring platform at each level.
            c.box(-3050, y, CZ - 20, leftEnd, y + 1, CZ + 20, Mat.SLAB_BOTTOM);
            c.box(rightStart, y, CZ - 20, 3050, y + 1, CZ + 20, Mat.SLAB_BOTTOM);
            c.box(-3050, y + 2, CZ - 22, leftEnd, y + 10, CZ - 19, Mat.IRON_BARS);
            c.box(-3050, y + 2, CZ + 19, leftEnd, y + 10, CZ + 22, Mat.IRON_BARS);
            c.box(rightStart, y + 2, CZ - 22, 3050, y + 10, CZ - 19, Mat.IRON_BARS);
            c.box(rightStart, y + 2, CZ + 19, 3050, y + 10, CZ + 22, Mat.IRON_BARS);

            // Heavy diagonals are split into short truss spans to keep cube generation cheap.
            for (int x = -2880; x < -innerEnd; x += 320) {
                int x1 = Math.min(-innerEnd, x + 320);
                int y0 = y - 72 + (int) ((double) (x + 2880) / (2880 - innerEnd) * 48);
                int y1 = y - 72 + (int) ((double) (x1 + 2880) / (2880 - innerEnd) * 48);
                c.beam(x, y0, CZ, x1, y1, CZ, 5, Mat.S_TOHA_MASS);
            }
            for (int x = innerEnd; x < 2880; x += 320) {
                int x1 = Math.min(2880, x + 320);
                int y0 = y - 72 + (int) ((double) (x - innerEnd) / (2880 - innerEnd) * 48);
                int y1 = y - 72 + (int) ((double) (x1 - innerEnd) / (2880 - innerEnd) * 48);
                c.beam(x, y0, CZ, x1, y1, CZ, 5, Mat.S_TOHA_MASS);
            }
            // The external route is joined to a short service porch at the tower wall.
            if (r > innerEnd + 25) {
                c.cylinder(CX + 0.5, CZ + 0.5, r - 8, Math.max(innerEnd, r - 40),
                        1, 1, y - 2, y + 1, Math.toRadians(25 + i * 31), Math.toRadians(0.8),
                        Mat.SLAB_TOP);
            }
        }
    }

    private void drawApproachCauseway(Cube c) {
        int southGateZ = CZ + VALLEY_LENGTH + 220;
        int southBaseZ = CZ + 350;
        int northGateZ = CZ - VALLEY_LENGTH - 220;
        int northBaseZ = CZ - 350;
        drawCausewaySpan(c, southGateZ, southBaseZ);
        drawCausewaySpan(c, northGateZ, northBaseZ);
    }

    /** A slab-backed approach span with ties, restrained lamps, and a framed canyon mouth. */
    private void drawCausewaySpan(Cube c, int gateZ, int baseZ) {
        int zMin = Math.min(gateZ, baseZ), zMax = Math.max(gateZ, baseZ);
        if (!c.hits(-160, GROUND - 20, zMin - 40, 160, GROUND + 64, zMax + 40)) return;

        c.box(-56, GROUND, zMin, 56, GROUND, zMax, Mat.SLAB_BOTTOM);
        c.box(-72, GROUND - 3, zMin, -62, GROUND + 1, zMax, Mat.S_TOHA_MASS);
        c.box(62, GROUND - 3, zMin, 72, GROUND + 1, zMax, Mat.S_TOHA_MASS);
        c.box(-16, GROUND + 1, zMin, 16, GROUND + 1, zMax, Mat.COAL);

        int minZ = Math.max(zMin, c.z0 - 16);
        int maxZ = Math.min(zMax, c.z0 + 31);
        for (int z = minZ; z <= maxZ; z++) {
            if (Math.floorMod(z - zMin, 96) == 0) {
                c.box(-92, GROUND - 18, z - 7, 92, GROUND - 8, z + 7, Mat.S_TOHA_RING);
                c.box(-74, GROUND + 1, z - 3, -61, GROUND + 4, z + 3, Mat.COPPER_PATINA);
                c.box(61, GROUND + 1, z - 3, 74, GROUND + 4, z + 3, Mat.COPPER_RAW);
            }
            if (Math.floorMod(z - zMin, 320) == 0) {
                c.box(-78, GROUND + 1, z - 3, -72, GROUND + 8, z + 3, Mat.SEA_LANTERN);
                c.box(72, GROUND + 1, z - 3, 78, GROUND + 8, z + 3, Mat.SEA_LANTERN);
            }
        }

        // Load frame is at the outer gate end of each approach.
        if (c.hits(-160, GROUND - 3, gateZ - 38, 160, GROUND + 64, gateZ + 38)) {
            c.box(-140, GROUND - 3, gateZ - 28, -124, GROUND + 50, gateZ + 28, Mat.S_TOHA_MASS);
            c.box(124, GROUND - 3, gateZ - 28, 140, GROUND + 50, gateZ + 28, Mat.S_TOHA_MASS);
            c.box(-150, GROUND + 48, gateZ - 34, 150, GROUND + 58, gateZ + 34, Mat.S_TOHA_RING);
            c.box(-132, GROUND + 49, gateZ - 31, -126, GROUND + 55, gateZ + 31, Mat.COPPER_PATINA);
            c.box(126, GROUND + 49, gateZ - 31, 132, GROUND + 55, gateZ + 31, Mat.COPPER_RAW);
        }
    }

    // ---------------------------------------------------------------- machinery in the valley
    private void drawCliffEquipment(Cube c) {
        int[] zOffsets = {-2860, -2050, -840, 460, 1740, 2840};
        int[] heights = {1180, 1820, 1480, 2000, 1640, 1940};
        for (int side : new int[] {-1, 1}) {
            for (int i = 0; i < zOffsets.length; i++) {
                int x = side * 3050, z = CZ + zOffsets[i], h = heights[i];
                if (!c.hits(x - 130, GROUND, z - 130, x + 130, h, z + 130)) continue;
                c.taper(x, z, 84 + (i % 2) * 16, 34, GROUND, h, 16, Mat.S_TOHA_RING);
                c.taper(x, z, 62 + (i % 2) * 16, 20, GROUND + 8, h - 8, 16, Mat.AIR);
                for (int y = Math.max(GROUND, c.y0 - 16); y <= Math.min(h, c.y0 + 15); y += 48) {
                    int r = (int) Math.round(84 + (i % 2) * 16
                            + (34 - (84 + (i % 2) * 16)) * ((double) (y - GROUND) / (h - GROUND)));
                    c.cylinder(x, z, r + 6, Math.max(4, r - 4), 1, 1, y, y + 3,
                            0, 0, y % 96 == 0 ? Mat.COPPER_PATINA : Mat.S_TOHA_PALE);
                    c.box(x - 3, y + 4, z - 3, x + 3, y + 13, z + 3, Mat.IRON_BARS);
                }
                c.box(x - 18, h - 38, z - 18, x + 18, h - 32, z + 18, Mat.SLAB_TOP);
                c.box(x - 6, h - 50, z - 6, x + 6, h - 20, z + 6, Mat.COPPER_RAW);
            }
        }
    }

    private void drawMachineYard(Cube c) {
        // Machine positions are authored by hand and deliberately non-mirrored.
        int[][] modules = {
            {-930, 520, 250, 170, 220}, {870, 560, 220, 200, 280},
            {-1120, -510, 260, 210, 310}, {1050, -650, 190, 240, 205},
            {-640, 1120, 170, 210, 170}, {710, 1260, 230, 160, 240},
            {-1370, -1210, 180, 180, 155}, {1340, 890, 180, 170, 185}
        };
        for (int i = 0; i < modules.length; i++) {
            drawMachineModule(c, modules[i][0], CZ + modules[i][1],
                    modules[i][2], modules[i][3], modules[i][4], i);
        }

        // Surface pipe trenches, maintenance tracks and slab walks stitch the modules together.
        if (c.hits(-1520, GROUND, CZ - 1350, 1520, GROUND + 12, CZ + 1350)) {
            c.box(-1450, GROUND, CZ - 80, -580, GROUND, CZ - 64, Mat.SLAB_TOP);
            c.box(610, GROUND, CZ + 190, 1430, GROUND, CZ + 206, Mat.SLAB_TOP);
            c.box(-1250, GROUND, CZ + 930, -1234, GROUND, CZ + 1390, Mat.SLAB_BOTTOM);
            c.box(1215, GROUND, CZ - 1250, 1231, GROUND, CZ - 740, Mat.SLAB_BOTTOM);
            c.box(-1260, GROUND + 1, CZ - 86, -1248, GROUND + 4, CZ + 940, Mat.COPPER_PATINA);
            c.box(1227, GROUND + 1, CZ - 1260, 1239, GROUND + 4, CZ + 198, Mat.COPPER_RAW);
            for (int i = 0; i < 9; i++) {
                int x = -1320 + i * 330;
                int z = CZ + ((i % 2 == 0) ? 1000 : -1020);
                c.box(x - 60, GROUND, z - 38, x + 60, GROUND + 1, z + 38, Mat.SLAB_BOTTOM);
                c.box(x - 54, GROUND + 1, z - 32, x + 54, GROUND + 5, z - 28, Mat.COAL);
            }
        }
    }

    private void drawMachineModule(Cube c, int x, int z, int width, int depth, int height, int kind) {
        int x0 = x - width / 2, x1 = x + width / 2;
        int z0 = z - depth / 2, z1 = z + depth / 2;
        int y0 = GROUND + 2, y1 = GROUND + height;
        if (!c.hits(x0 - 24, GROUND - 2, z0 - 24, x1 + 24, y1 + 36, z1 + 24)) return;

        c.box(x0 - 16, GROUND, z0 - 16, x1 + 16, GROUND + 4, z1 + 16, Mat.S_TOHA_RING);
        c.box(x0 - 6, GROUND + 4, z0 - 6, x1 + 6, GROUND + 7, z1 + 6, Mat.SLAB_TOP);
        c.box(x0, y0, z0, x1, y0 + 20, z1, Mat.S_TOHA_MASS);

        // Four exposed corner legs and a high crosshead form the service frame.
        int leg = 7 + kind % 4;
        for (int dx : new int[] {x0 + 10, x1 - 10}) {
            for (int dz : new int[] {z0 + 10, z1 - 10}) {
                c.box(dx - leg, y0, dz - leg, dx + leg, y1, dz + leg,
                      kind % 2 == 0 ? Mat.S_TOHA_PALE : Mat.S_TOHA_RING);
                c.box(dx - leg - 2, y1 - 24, dz - leg - 2, dx + leg + 2, y1 - 19, dz + leg + 2, Mat.COPPER_PATINA);
            }
        }
        c.box(x0 - 8, y1, z0 - 8, x1 + 8, y1 + 12, z1 + 8, Mat.S_TOHA_MASS);
        c.box(x0 + 10, y1 + 12, z0 + 10, x1 - 10, y1 + 13, z1 - 10, Mat.SLAB_BOTTOM);

        // Each module has a different fixed grammar: pressure vessels, a rotor cage,
        // a vent rack, or a transformer stack.
        if (kind % 4 == 0) {
            for (int sx : new int[] {-width / 4, width / 4}) {
                int tx = x + sx, tz = z;
                c.box(tx - 10, y0 + 2, tz - 10, tx + 10, y0 + 7, tz + 10, Mat.MACHINE_HOPPER);
                c.cylinder(tx, tz, Math.min(42, width / 5), Math.min(28, width / 7), 1, 1,
                        y0 + 8, y1 - 10, 0, 0, Mat.S_TOHA_TANK);
                c.cylinder(tx, tz, Math.min(46, width / 4), Math.min(28, width / 7), 1, 1,
                        y1 - 20, y1 - 13, 0, 0, Mat.COPPER_RAW);
                c.sphere(tx, y1 - 10, tz, Math.min(28, width / 7), Math.max(8, Math.min(18, width / 10)), Mat.COPPER_PATINA);
            }
        } else if (kind % 4 == 1) {
            c.ring(x, z, width / 3.0, depth / 3.0, y0 + 25, y0 + 48, 8, Mat.COPPER_RAW);
            c.beam(x - width / 3, y0 + 36, z, x + width / 3, y0 + 36, z, 4, Mat.IRON_BLOCK);
            c.beam(x, y0 + 36, z - depth / 3, x, y0 + 36, z + depth / 3, 4, Mat.IRON_BLOCK);
            c.box(x - 6, y0 + 20, z - 6, x + 6, y1 - 20, z + 6, Mat.VENT);
        } else if (kind % 4 == 2) {
            for (int yy = y0 + 24; yy < y1 - 18; yy += 28) {
                c.box(x0 + 12, yy, z0 + 12, x1 - 12, yy + 3, z1 - 12, Mat.SLAB_BOTTOM);
                c.box(x0 + 12, yy + 4, z0 + 12, x0 + 16, yy + 20, z0 + 16, Mat.IRON_BARS);
                c.box(x1 - 16, yy + 4, z1 - 16, x1 - 12, yy + 20, z1 - 12, Mat.IRON_BARS);
                if ((yy - y0) % 56 == 24) {
                    c.box(x0 + 22, yy + 4, z1 + 2, x0 + 40, yy + 7, z1 + 8,
                          Mat.STONE_BRICK_STAIRS);
                }
            }
            c.box(x0 + 12, y0 + 20, z0 - 2, x1 - 12, y1 - 18, z0 + 3, Mat.GLASS_REINFORCED);
        } else {
            c.box(x0 + 16, y0 + 16, z0 + 16, x1 - 16, y1 - 16, z1 - 16, Mat.COAL);
            c.box(x0 + 24, y0 + 30, z0 + 24, x1 - 24, y1 - 30, z1 - 24, Mat.IRON_BLOCK);
            c.box(x - 7, y1 - 46, z1 + 2, x + 7, y1 - 28, z1 + 8, Mat.BIO_CYAN);
            c.box(x - 18, y1 - 52, z1 + 2, x - 11, y1 - 40, z1 + 8, Mat.LAMP_RED);
        }

        // Pipe manifolds deliberately go to a second module or the cliff, never nowhere.
        c.box(x0 - 38, y0 + 32, z, x0 + 8, y0 + 40, z + 8, Mat.COPPER_PATINA);
        c.box(x1 - 8, y0 + 52, z - 8, x1 + 44, y0 + 60, z, Mat.COPPER_RAW);
        c.box(x, y1 + 14, z0 - 28, x + 8, y1 + 22, z1 + 28, Mat.VENT);
        c.box(x - 7, y0 + 28, z0 - 3, x + 7, y0 + 40, z0 + 3, Mat.MACHINE_DISPENSER);
        c.box(x - 11, y1 - 12, z0 - 12, x + 11, y1 - 8, z0 - 8, Mat.IRON_TRAPDOOR);
    }

    private void drawHullInscription(Cube c) {
        final int cell = 3, gap = 3, span = 14 * cell, stride = span + gap;
        final int x0 = -span / 2, x1 = x0 + span - 1;
        final int bottomY = 100, topY = bottomY + 4 * span + 3 * gap + 3;
        if (!c.hits(x0, bottomY, CZ + 250, x1, topY, CZ + 460)) return;

        int xA = Math.max(x0, c.x0), xB = Math.min(x1, c.x0 + 15);
        int yA = Math.max(bottomY, c.y0), yB = Math.min(topY, c.y0 + 15);
        for (int g = 0; g < HULL_INSCRIPTION.length; g++) {
            int glyphBottom = bottomY + (HULL_INSCRIPTION.length - 1 - g) * stride;
            String[] glyph = HULL_INSCRIPTION[g];
            for (int row = 0; row < 14; row++) {
                int y = glyphBottom + (13 - row) * cell;
                if (y > yB || y + cell - 1 < yA) continue;
                for (int col = 0; col < 14; col++) {
                    if (glyph[row].charAt(col) != '1') continue;
                    int px0 = x0 + col * cell;
                    int lo = Math.max(px0, xA), hi = Math.min(px0 + cell - 1, xB);
                    if (lo > hi) continue;
                    for (int x = lo; x <= hi; x++) {
                        int front = projectedFrontZ(x, y) - 12;
                        for (int z = front + 1; z <= front + 3; z++) c.set(x, y, z, Mat.COAL);
                    }
                }
            }
        }
    }
}
