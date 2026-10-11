package blame.blamegen.core;

/** Deterministic integer hashing and cheap value noise. No floating-point libm calls in hashing. */
public final class H {
    private H() {}

    public static long mix(long z) {
        z = (z ^ (z >>> 30)) * 0xbf58476d1ce4e5b9L;
        z = (z ^ (z >>> 27)) * 0x94d049bb133111ebL;
        return z ^ (z >>> 31);
    }

    public static long h(long seed, long a) {
        return mix(seed ^ mix(a + 0x9E3779B97F4A7C15L));
    }

    public static long h(long seed, long a, long b) {
        return mix(h(seed, a) ^ mix(b * 0xC2B2AE3D27D4EB4FL + 0x165667B19E3779F9L));
    }

    public static long h(long seed, long a, long b, long c) {
        return mix(h(seed, a, b) ^ mix(c * 0x27D4EB2F165667C5L + 0x85EBCA77C2B2AE63L));
    }

    public static long h(long seed, long a, long b, long c, long d) {
        return mix(h(seed, a, b, c) ^ mix(d * 0x9E3779B97F4A7C15L + 0x632BE59BD9B4E019L));
    }

    /** Uniform double in [0,1). */
    public static double u(long hash) {
        return (hash >>> 11) * 0x1.0p-53;
    }

    /** Uniform int in [lo, hi] inclusive. */
    public static int range(long hash, int lo, int hi) {
        return lo + (int) Long.remainderUnsigned(hash, (long) (hi - lo + 1));
    }

    public static boolean chance(long hash, double p) {
        return u(hash) < p;
    }

    private static double fade(double t) {
        return t * t * (3 - 2 * t);
    }

    /** 2D value noise in [0,1) with the given cell size. */
    public static double noise2(long seed, double x, double z, double cell) {
        double fx = x / cell, fz = z / cell;
        long ix = (long) Math.floor(fx), iz = (long) Math.floor(fz);
        double tx = fade(fx - ix), tz = fade(fz - iz);
        double a = u(h(seed, ix, iz)), b = u(h(seed, ix + 1, iz));
        double c = u(h(seed, ix, iz + 1)), d = u(h(seed, ix + 1, iz + 1));
        double ab = a + (b - a) * tx, cd = c + (d - c) * tx;
        return ab + (cd - ab) * tz;
    }

    /** 3D value noise in [0,1). */
    public static double noise3(long seed, double x, double y, double z, double cell) {
        double fx = x / cell, fy = y / cell, fz = z / cell;
        long ix = (long) Math.floor(fx), iy = (long) Math.floor(fy), iz = (long) Math.floor(fz);
        double tx = fade(fx - ix), ty = fade(fy - iy), tz = fade(fz - iz);
        double r0, r1;
        {
            double a = u(h(seed, ix, iy, iz)), b = u(h(seed, ix + 1, iy, iz));
            double c = u(h(seed, ix, iy, iz + 1)), d = u(h(seed, ix + 1, iy, iz + 1));
            double ab = a + (b - a) * tx, cd = c + (d - c) * tx;
            r0 = ab + (cd - ab) * tz;
        }
        {
            double a = u(h(seed, ix, iy + 1, iz)), b = u(h(seed, ix + 1, iy + 1, iz));
            double c = u(h(seed, ix, iy + 1, iz + 1)), d = u(h(seed, ix + 1, iy + 1, iz + 1));
            double ab = a + (b - a) * tx, cd = c + (d - c) * tx;
            r1 = ab + (cd - ab) * tz;
        }
        return r0 + (r1 - r0) * ty;
    }

    public static int floorDiv(int a, int b) {
        return Math.floorDiv(a, b);
    }
}
