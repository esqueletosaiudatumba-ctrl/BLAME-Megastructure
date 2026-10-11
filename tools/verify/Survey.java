package blame.blamegen.core;

/** Exact coarse-cell survey: replicate drawCoarse/shaft/seaHall/roundVoid hashes. */
public class Survey {
    static final long SEED = 0x424C414D45L;

    public static void main(String[] a) {
        Megastructure m = new Megastructure(SEED);
        int[][] cells = {{0,-1},{1,-1},{1,0},{0,1},{-1,0},{1,1},{-1,-1},{2,0},{0,-2},{2,-1}};
        for (int[] cc : cells) {
            int cx = cc[0], cz = cc[1];
            long hc = H.h(m.seed, 600, cx, cz);
            double r = H.u(H.h(hc, 1));
            if (r < 0.55) {
                int R = H.range(H.h(hc, 2), 60, 200);
                int wt = H.range(H.h(hc, 3), 4, 9);
                int lim = Megastructure.CELL / 2 - R - wt - 24;
                int ox = cx * Megastructure.CELL + H.range(H.h(hc, 4), -lim, lim);
                int oz = cz * Megastructure.CELL + H.range(H.h(hc, 5), -lim, lim);
                int ja = H.range(H.h(hc, 6), -14, 3);
                int jb = H.chance(H.h(hc, 10), 0.5) ? Megastructure.TOP_TIER
                        : Math.min(Megastructure.TOP_TIER, ja + H.range(H.h(hc, 7), 4, 20));
                int ya = m.tierY(ja);
                int yb = jb == Megastructure.TOP_TIER ? m.tierY(Megastructure.TOP_TIER) + 60 : m.tierY(jb);
                System.out.println("SHAFT cell(" + cx + "," + cz + ") R=" + R + " c=" + ox + "," + oz
                        + " y=" + ya + ".." + yb + (jb == Megastructure.TOP_TIER ? " OPEN" : " capped")
                        + (H.chance(H.h(hc, 8), 0.35) ? " flooded" : "")
                        + (H.chance(H.h(hc, 9), 0.4) ? " ribcage" : " shell"));
            } else if (r < 0.66) {
                int R = H.range(H.h(hc, 2), 80, 170);
                int lim = Megastructure.CELL / 2 - R - 30;
                int ox = cx * Megastructure.CELL + H.range(H.h(hc, 4), -lim, lim);
                int oz = cz * Megastructure.CELL + H.range(H.h(hc, 5), -lim, lim);
                int oy = m.tierY(H.range(H.h(hc, 6), -12, Megastructure.TOP_TIER - 2)) + H.range(H.h(hc, 7), -40, 40);
                System.out.println("VOID cell(" + cx + "," + cz + ") R=" + R + " c=" + ox + "," + oz + "," + oy);
            } else if (r < 0.74) {
                int hx = H.range(H.h(hc, 2), 140, 220), hz = H.range(H.h(hc, 3), 140, 220);
                int hh = H.range(H.h(hc, 4), 70, 140);
                int lim = Megastructure.CELL / 2 - Math.max(hx, hz) - 20;
                int ox = cx * Megastructure.CELL + H.range(H.h(hc, 5), -lim, lim);
                int oz = cz * Megastructure.CELL + H.range(H.h(hc, 6), -lim, lim);
                int floorY = m.tierY(H.range(H.h(hc, 7), -14, 4)) + 4;
                System.out.println("SEA cell(" + cx + "," + cz + ") c=" + ox + "," + oz + " floorY=" + floorY + " h=" + hh);
            } else {
                System.out.println("NONE cell(" + cx + "," + cz + ")");
            }
        }
    }
}
