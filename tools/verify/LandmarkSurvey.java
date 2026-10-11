package blame.blamegen.core;

/** Lists landmark cells near the origin (type + centre) so preview cameras can be aimed at them. */
public class LandmarkSurvey {
    public static void main(String[] a) {
        Megastructure m = new Megastructure(0x424C414D45L);
        for (int cx = -4; cx <= 4; cx++)
            for (int cz = -4; cz <= 4; cz++) {
                if (cx == 0 && cz == 0) continue;
                long hc = H.h(m.seed, 600, cx, cz);
                if (H.u(H.h(hc, 1)) >= 0.74) System.out.println(Landmark.describe(m, cx, cz, hc));
            }
    }
}
