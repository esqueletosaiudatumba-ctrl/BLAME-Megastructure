package blame.blamegen.core;

import java.util.Arrays;

/** Offline verification: determinism, ceiling wells, and new-feature block census. */
public class Probe {
    static final long SEED = 0x424C414D45L;

    public static void main(String[] a) {
        Megastructure m1 = new Megastructure(SEED);
        Megastructure m2 = new Megastructure(SEED);
        int found = 0;

        // 1. Determinism: same cube from two instances must be identical.
        int[][] det = {{0, 4, 4}, {3, -12, -7}, {-40, 130, 25}, {60, -30, -60}, {0, 0, 0}};
        boolean ok = true;
        for (int[] q : det) {
            Cube c1 = new Cube(q[0], q[1], q[2]);
            Cube c2 = new Cube(q[0], q[1], q[2]);
            m1.generate(c1);
            m2.generate(c2);
            if (!Arrays.equals(c1.b, c2.b)) { ok = false; System.out.println("NONDETERMINISTIC " + Arrays.toString(q)); }
        }
        System.out.println("determinism: " + (ok ? "PASS" : "FAIL"));

        // 2. Ceiling wells.
        System.out.println("well(arrival 0,64)=" + m1.ceilWell(0, 64) + " (want true)");
        System.out.println("blocksSkylight(2000,0)=" + m1.blocksSkylight(2000, 0) + " (want true)");
        int wells = 0, n = 0;
        for (int x = 512; x < 1536; x += 8)
            for (int z = 512; z < 1536; z += 8) { n++; if (m1.ceilWell(x, z)) wells++; }
        System.out.printf("well fraction in cell(1,1): %.3f (want 0 < f < 0.5)%n", (double) wells / n);

        // 3. Census over arrival + nearby lattice + deep cubes.
        int[] watch = {Mat.LAMP_RED, Mat.LAMP_AMBER, Mat.LAMP_GREEN, Mat.PIPE, Mat.COPPER_PATINA,
                Mat.COPPER_RAW, Mat.COPPER_WEATHERED, Mat.CONC_RUST, Mat.BIO_CYAN, Mat.BIO_MAGENTA,
                Mat.CABLE, Mat.VENT, Mat.REBAR, Mat.GLASS_REINFORCED, Mat.S_RUST_MASS, Mat.S_PIPEWORK, Mat.S_BIO_STONE};
        String[] names = {"LAMP_RED", "LAMP_AMBER", "LAMP_GREEN", "PIPE", "COPPER_PATINA",
                "COPPER_RAW", "COPPER_WEATHERED", "CONC_RUST", "BIO_CYAN", "BIO_MAGENTA",
                "CABLE", "VENT", "REBAR", "GLASS_REINFORCED", "S_RUST_MASS", "S_PIPEWORK", "S_BIO_STONE"};
        long[] total = new long[watch.length];
        // Arrival cell: beach ring + bay, y -7..40 (cubeY -7..39 padded by oceanSpires y range)
        for (int cx = -32; cx <= 32; cx++)
            for (int cz = -32; cz <= 32; cz++)
                for (int cy = -8; cy <= 40; cy++)
                    census(m1, cx, cy, cz, watch, total);
        System.out.println("--- arrival cell census ---");
        for (int i = 0; i < watch.length; i++) System.out.println(names[i] + "=" + total[i]);
        Arrays.fill(total, 0);
        // Lattice ring around arrival, tiers -6..8.
        for (int cx = -80; cx <= 80; cx += 4)
            for (int cz = -80; cz <= 80; cz += 4) {
                int cellx = Math.floorDiv(cx * 16 + 512, 1024), cellz = Math.floorDiv(cz * 16 + 512, 1024);
                if (cellx == 0 && cellz == 0) continue;
                for (int cy = -8; cy <= 9; cy++)
                    census(m1, cx, cy * 16 >> 4, cz, watch, total);
            }
        System.out.println("--- lattice ring census ---");
        for (int i = 0; i < watch.length; i++) System.out.println(names[i] + "=" + total[i]);
        Arrays.fill(total, 0);
        // Deep abyss (y ~ -1520..-1360): rust masses, bio growth.
        for (int cx = -64; cx <= 64; cx += 8)
            for (int cz = -64; cz <= 64; cz += 8) {
                int cellx = Math.floorDiv(cx * 16 + 512, 1024), cellz = Math.floorDiv(cz * 16 + 512, 1024);
                if (cellx == 0 && cellz == 0) continue;
                for (int cy = -95; cy <= -85; cy++)
                    census(m1, cx, cy, cz, watch, total);
            }
        System.out.println("--- deep abyss census ---");
        for (int i = 0; i < watch.length; i++) System.out.println(names[i] + "=" + total[i]);

        // Locate one open megashaft near the origin for a preview camera.
        for (int cx = 0; cx <= 3 && found < 2; cx++)
            for (int cz = -3; cz <= 3 && found < 2; cz++) {
                if (cx == 0 && cz == 0) continue;
                long hc = H.h(m1.seed, 600, cx, cz);
                if (H.u(H.h(hc, 1)) >= 0.55) continue;
                int R = H.range(H.h(hc, 2), 60, 200);
                int lim = Megastructure.CELL / 2 - R - 9 - 24;
                int ox = cx * Megastructure.CELL + H.range(H.h(hc, 4), -lim, lim);
                int oz = cz * Megastructure.CELL + H.range(H.h(hc, 5), -lim, lim);
                System.out.println("shaft cell(" + cx + "," + cz + ") R=" + R + " at " + ox + "," + oz);
                found++;
            }

        // Find a flooded hall (seaHall) and census it for S_BIO_STONE -> BIO_MAGENTA.
        Arrays.fill(total, 0);
        boolean seaFound = false;
        for (int cx = 1; cx <= 6 && !seaFound; cx++)
            for (int cz = 1; cz <= 6 && !seaFound; cz++) {
                long hc = H.h(m1.seed, 600, cx, cz);
                double r = H.u(H.h(hc, 1));
                if (r < 0.66 || r >= 0.74) continue;
                int hx = H.range(H.h(hc, 2), 140, 220), hz = H.range(H.h(hc, 3), 140, 220);
                int hh = H.range(H.h(hc, 4), 70, 140);
                int lim = Megastructure.CELL / 2 - Math.max(hx, hz) - 20;
                int ox = cx * Megastructure.CELL + H.range(H.h(hc, 5), -lim, lim);
                int oz = cz * Megastructure.CELL + H.range(H.h(hc, 6), -lim, lim);
                int floorY = m1.tierY(H.range(H.h(hc, 7), -14, 4)) + 4;
                System.out.println("seaHall cell(" + cx + "," + cz + ") at " + ox + "," + oz + " floorY=" + floorY);
                for (int x = ox - hx - 8; x <= ox + hx + 8; x += 16)
                    for (int z = oz - hz - 8; z <= oz + hz + 8; z += 16)
                        for (int y = floorY - 16; y <= floorY + hh + 16; y += 16)
                            census(m1, x >> 4, y >> 4, z >> 4, watch, total);
                seaFound = true;
            }
        System.out.println("--- seaHall census ---");
        for (int i = 0; i < watch.length; i++) System.out.println(names[i] + "=" + total[i]);
    }

    static void census(Megastructure m, int cx, int cy, int cz, int[] watch, long[] total) {
        Cube c = new Cube(cx, cy, cz);
        m.generate(c);
        for (byte b : c.b)
            for (int i = 0; i < watch.length; i++)
                if ((b & 0xFF) == watch[i]) { total[i]++; break; }
    }
}
