package blame.blamegen.core;

/**
 * Regression guard for feature containment (see Containment in Megastructure doc):
 * every transit tube must lie inside its owner bay-tier's evaluation band, and
 * generation must not crash at extreme tiers. Run offline; fails loudly.
 */
public class SeamCheck {
    static final long SEED = 0x424C414D45L;
    static int failures = 0;

    static void check(boolean cond, String msg) {
        if (!cond) { failures++; System.out.println("FAIL: " + msg); }
    }

    public static void main(String[] a) {
        Megastructure m = new Megastructure(SEED);
        // 1. Tube bounds over a wide bay/tier range (mirror drawBay's hash logic).
        for (int i = -50; i <= 50; i++)
            for (int k = -50; k <= 50; k++)
                for (int j = -20; j <= 10; j++) {
                    if (j >= Megastructure.TOP_TIER) continue;
                    int kd = m.kind(i, k, j);
                    if (kd == Megastructure.K_BLOCK) continue;
                    int ty0 = m.tierY(j), ty1 = m.tierY(j + 1);
                    Megastructure.Deck d = m.deck(i, k, j);
                    long hb = H.h(m.seed, 400, i, k, j);
                    if (!H.chance(H.h(hb, 9), 0.12 + Math.max(0, Megastructure.altitude(ty0)) * 0.15)) continue;
                    long ht = H.h(hb, 10);
                    int yt = d.top + H.range(H.h(ht, 2), 10, Math.max(11, (ty1 - d.top) / 2));
                    int ty = m.tierY(j + H.range(H.h(ht, 6), 0, 1)) + H.range(H.h(ht, 7), -30, 30);
                    check(yt >= ty0 - 56 && yt <= ty1 + 56,
                            "tube yt out of band i=" + i + " k=" + k + " j=" + j + " yt=" + yt);
                    check(ty >= ty0 - 56 && ty <= ty1 + 56,
                            "tube ty out of band i=" + i + " k=" + k + " j=" + j + " ty=" + ty);
                }
        System.out.println("tube band check done");

        // 2. Extreme tiers: deep abyss and sky spires must generate without error.
        int[][] cubes = {{0, -200, 0}, {5, -190, -7}, {-30, 300, 40}, {10, 410, -3}, {0, 500, 0}};
        for (int[] q : cubes) {
            Cube c = new Cube(q[0], q[1], q[2]);
            m.generate(c);
            int solid = 0;
            for (byte b : c.b) if (b != Mat.AIR) solid++;
            System.out.println("cube " + q[0] + "," + q[1] + "," + q[2] + " solid=" + solid);
        }

        // 3. Arrival wall ring: gate/causeway/spire columns spot-check (no crash, spawn sane).
        System.out.println("spawnY=" + m.spawnY());
        check(m.spawnY() > 62 && m.spawnY() < 70, "spawnY out of beach range: " + m.spawnY());

        System.out.println(failures == 0 ? "SEAMCHECK PASS" : "SEAMCHECK FAILURES=" + failures);
        if (failures > 0) System.exit(1);
    }
}
