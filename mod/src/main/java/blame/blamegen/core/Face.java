package blame.blamegen.core;

/**
 * Sculpted inner face of the arrival shaft wall.
 *
 * The wall is not a flat drum. Each angular sector of the shaft gets one of several
 * facade styles — ribcage, arcade of niches, terrace stack, cliff of slots, blast holes,
 * pipe organ fins — and every style is decided from hashes so the whole thing is still
 * deterministic and cheap to evaluate per block column.
 *
 * A "column" of the facade is one angular position; {@link Col} answers "what is here at
 * height y?" with a packed int key, decoded by {@link #push} (how far out of the wall the
 * relief projects), {@link #mat} (material) and {@link #lamp} (does this emit light).
 *
 * Usage: the owning code tests whether a block is inside the facade band
 * (radius within ~60 of the wall face), builds a {@link Col} for its angle, and asks for
 * the relief at each height. Everything stays inside the band, so nothing crosses into
 * the shaft interior.
 */
final class Face {
    /** Facade styles. */
    private static final int F_RIBCAGE = 0, F_ARCADE = 1, F_TERRACE = 2, F_CLIFF = 3,
            F_BLAST = 4, F_ORGAN = 5, F_PLAIN = 6;
    private static final int STYLES = 7;

    /** How far a relief key projects out of the wall, in blocks. */
    static int push(int key) {
        return key & 0x3F;
    }

    /** Material for a relief key, or 0 for "no relief here". */
    static byte mat(int key) {
        int m = (key >>> 6) & 0x1F;
        return MATS[m];
    }

    /** Sentinel material index meaning "leave air" (a hole in the face). */
    private static final int AIR_ENC = 11;

    /** Material index table for {@link #mat}; index 0 is reserved for "no relief". */
    private static final byte[] MATS = new byte[16];
    static {
        MATS[0] = 0;
        MATS[1] = Mat.S_MASS;
        MATS[2] = Mat.S_DARK;
        MATS[3] = Mat.S_PALE;
        MATS[4] = Mat.S_COLUMN;
        MATS[5] = Mat.S_METAL;
        MATS[6] = Mat.S_RUST_MASS;
        MATS[7] = Mat.S_PIPEWORK;
        MATS[8] = Mat.COPPER_PATINA;
        MATS[9] = Mat.REBAR;
        MATS[10] = Mat.SANDSTONE;
        MATS[AIR_ENC] = Mat.AIR;
    }

    /** Does this relief key carry a lamp? */
    static boolean lamp(int key) {
        return (key >>> 11) == 1;
    }

    private final long seed;
    /** Angular size of a sector, in radians. */
    private static final double SECTOR = 0.075;

    Face(long seed) {
        this.seed = seed;
    }

    private static int sector(double theta) {
        return (int) Math.floor(theta / SECTOR);
    }

    /** Per-sector facade style, fixed for the sector. */
    private int styleOf(double theta) {
        int s = sector(theta);
        // Bias toward the plain drum so the wall still reads as mass, with districts of detail.
        long h = H.h(seed, 0xF4C1, s);
        if (!H.chance(h, 0.72)) return F_PLAIN;
        return H.range(H.h(h, 9), 0, STYLES - 1);
    }

    /**
     * One angular position of the wall. Built once per block column, so the constructor is
     * expected to be cheap: a handful of hashes.
     */
    /**
     * One angular position of the wall. Built once per block column, so the constructor is
     * expected to be cheap: a handful of hashes.
     */
    static final class Col {
        private final double theta;
        private final int styleIdx;
        private final long h;
        /** Sector-local angular phase, 0..1. */
        private final double phase;
        private final int ribPitch;
        private final int ribHalf;
        private final int lampStride;

        Col(Face face, double theta) {
            this.theta = theta;
            this.styleIdx = face.styleOf(theta);
            this.h = H.h(face.seed, 0xF4C2, face.sector(theta));
            this.phase = (theta / SECTOR) - face.sector(theta);
            this.ribPitch = 6 + H.range(H.h(h, 1), 0, 5);
            this.ribHalf = 1 + H.range(H.h(h, 2), 0, 3);
            this.lampStride = 3 + H.range(H.h(h, 3), 0, 5);
        }

        /**
         * Packed relief key at height y, or 0 if the wall keeps its radius there.
         * Encodings: bits 0..5 push distance, 6..10 material, bit 11 lamp.
         */
        int at(int y) {
            if (styleIdx == F_PLAIN) return 0;
            int ly = Math.floorMod(y, 8);
            switch (styleIdx) {
                case F_RIBCAGE: {
                    // Broad vertical ribs with dark bands and occasional lamps.
                    boolean rib = Math.floorMod(y, ribPitch) < ribHalf;
                    if (rib) {
                        int key = pack(Math.max(ribHalf, 2), Mat.S_DARK, false);
                        if (Math.floorMod(y, ribPitch * 4) == 0) key = pack(2, Mat.S_PALE, false);
                        return key;
                    }
                    return 0;
                }
                case F_ARCADE: {
                    // Stacked niches: recess blocks in a regular tiered rhythm, a lamp
                    // set in the back of some of them.
                    int tier = Math.floorDiv(y, 14), ty = Math.floorMod(y, 14);
                    long hn = H.h(h, 10, tier);
                    int nicheW = 3 + H.range(H.h(hn, 1), 0, 4);
                    int depth = 10 + H.range(H.h(hn, 2), 8, 34);
                    boolean inNiche = ty >= 3 && ty <= 8 && phase < nicheW / 7.0;
                    if (inNiche) return pack(depth, AIR_ENC, H.chance(H.h(hn, 3), 0.3));
                    if (ty == 9) return pack(2, Mat.S_DARK, false);
                    if (H.chance(H.h(hn, 4), 0.5)) return pack(1, Mat.S_PALE, false);
                    return 0;
                }
                case F_TERRACE: {
                    // Cantilevered slabs every 32 blocks, each with a rail and a lamp.
                    int t = Math.floorDiv(y - 96, 32);
                    int ty = Math.floorMod(y - 96, 32);
                    if (ty == 0) return pack(4 + (int) (phase * 3), Mat.S_DECK, false);
                    if (ty == 1) return pack(5 + (int) (phase * 3), Mat.IRON_BARS, false);
                    if (ty == 2) return pack(3 + (int) (phase * 3), Mat.S_MASS, false);
                    // Vertical supports below each terrace.
                    if (ty > 4 && Math.floorMod(ty, 8) == 0 && phase < 0.2) return pack(6, Mat.S_COLUMN, false);
                    if (ty == 31) return pack(1, Mat.S_DARK, false);
                    return 0;
                }
                case F_CLIFF: {
                    // Tall narrow slots punched into the face, breaking the drum into fins.
                    int slot = Math.floorDiv(y, 12);
                    long hs = H.h(h, 20, slot);
                    int w = 1 + H.range(H.h(hs, 1), 0, 3);
                    int depth = 18 + H.range(H.h(hs, 2), 14, 40);
                    if (phase < w / 8.0) return pack(depth, AIR_ENC, false);
                    // Fin edges.
                    if (phase > w / 8.0 && phase < w / 8.0 + 0.08) return pack(2, Mat.S_DARK, false);
                    return 0;
                }
                case F_BLAST: {
                    // Large irregular blast holes; scorched rim, sometimes a lamp inside.
                    int hole = Math.floorDiv(y, 9);
                    long hb2 = H.h(h, 30, hole);
                    double r = H.u(hb2);
                    if (r < 0.34) {
                        int depth = 20 + H.range(H.h(hb2, 1), 12, 44);
                        return pack(depth, AIR_ENC, hole == Math.floorDiv(96, 9) && r < 0.12);
                    }
                    if (r < 0.44) return pack(3, Mat.S_RUST_MASS, false);
                    return 0;
                }
                case F_ORGAN: {
                    // Pipe-organ fins: tall thin flutes with copper junctions and lamps.
                    int fl = Math.floorMod(y, 5);
                    if (fl < 2) {
                        boolean copper = Math.floorMod(y, 40) < 3;
                        int depth = 7 + (int) (phase * 10);
                        if (copper) return pack(depth, Mat.COPPER_PATINA, false);
                        if (Math.floorMod(y, 40) == 4) return pack(depth, Mat.S_PIPEWORK, false);
                        if (H.chance(H.h(h, 40, Math.floorDiv(y, 160)), 0.35)) return pack(depth, Mat.S_MASS, true);
                        return pack(depth, Mat.S_COLUMN, false);
                    }
                    return 0;
                }
                default:
                    return 0;
            }
        }

        private static int pack(int push, int materialIdx, boolean lamp) {
            int v = Math.max(0, Math.min(0x3F, push));
            v |= (materialIdx & 0x1F) << 6;
            if (lamp) v |= 1 << 11;
            return v;
        }
    }
}
