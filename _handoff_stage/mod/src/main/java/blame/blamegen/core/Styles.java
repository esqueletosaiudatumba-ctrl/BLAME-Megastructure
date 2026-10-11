package blame.blamegen.core;

/** Resolves style ids into concrete materials: stains, streaks, banding and cracks. */
final class Styles {
    private Styles() {}

    static byte resolve(long seed, byte style, int x, int y, int z) {
        long ph = H.h(seed, 9001, x, y, z);
        switch (style) {
            case Mat.S_MASS: {
                // Large dark stains, vertical rain streaks and faint horizontal panel courses.
                double stain = H.noise3(seed ^ 0x51, x, y * 0.5, z, 23);
                if (stain < 0.18) return Mat.CONC_BLACK;
                if (stain > 0.86) return Mat.TERRA_GRAY;
                long col = H.h(seed, 9002, x, z);
                if ((col & 31) == 0 && H.noise2(seed ^ 0x52, x + z, y, 37) > 0.45) return Mat.TERRA_BLACK;
                if (Math.floorMod(y, 32) == 0) return Mat.TERRA_GRAY;
                if ((ph & 127) == 0) return Mat.TERRA_LGRAY;
                return Mat.CONC_GRAY;
            }
            case Mat.S_COLUMN: {
                if (Math.floorMod(y, 24) == 0) return Mat.STONEBRICK;
                double n = H.noise3(seed ^ 0x61, x, y * 0.3, z, 19);
                if (n < 0.22) return Mat.ANDESITE;
                if (n > 0.8) return (ph & 7) == 0 ? Mat.STONEBRICK_CRACKED : Mat.STONEBRICK;
                return Mat.STONE;
            }
            case Mat.S_DECK: {
                double n = H.noise3(seed ^ 0x71, x, y, z, 29);
                if (n < 0.3) return Mat.CONC_GRAY;
                if (n > 0.85) return Mat.TERRA_GRAY;
                return Mat.STONE;
            }
            case Mat.S_DARK: {
                double n = H.noise3(seed ^ 0x81, x, y, z, 17);
                if (n < 0.2) return Mat.COAL;
                if (n > 0.8) return Mat.TERRA_BLACK;
                return Mat.CONC_BLACK;
            }
            case Mat.S_PALE: {
                double n = H.noise3(seed ^ 0x91, x, y * 0.5, z, 31);
                if (n < 0.2) return Mat.TERRA_LGRAY;
                if (n > 0.82) return Mat.STONE;
                if (Math.floorMod(y, 48) == 0) return Mat.CONC_GRAY;
                return Mat.CONC_LGRAY;
            }
            case Mat.S_METAL: {
                return (ph & 3) == 0 ? Mat.COAL : Mat.IRON_BLOCK;
            }
            case Mat.S_RUST_MASS: {
                // Deep abyssal concrete: heavy rust streaks, black mould, copper patina bleed.
                double alt = (y - 64) / 3000.0;
                double n = H.noise3(seed ^ 0xA1, x, y * 0.4, z, 19);
                if (n < 0.15) return Mat.CONC_BLACK;
                if (n < 0.3) return Mat.CONC_RUST;
                if (n > 0.85) return Mat.COPPER_PATINA;
                if (alt < -0.5 && (ph & 63) == 0) return Mat.BIO_CYAN;
                return Mat.CONC_GRAY;
            }
            case Mat.S_PIPEWORK: {
                // Copper pipes, oxidised patina, raw copper, weathered transitions.
                double n = H.noise3(seed ^ 0xB1, x, y * 0.6, z, 13);
                if (n < 0.2) return Mat.COPPER_PATINA;
                if (n < 0.4) return Mat.COPPER_WEATHERED;
                if (n < 0.6) return Mat.COPPER_RAW;
                if (n > 0.9) return Mat.PIPE;
                return Mat.IRON_BLOCK;
            }
            case Mat.S_BIO_STONE: {
                // Stone colonised by faint bioluminescent growth in deep flooded zones.
                double n = H.noise3(seed ^ 0xC1, x, y * 0.3, z, 23);
                if (n < 0.1) return Mat.BIO_CYAN;
                if (n < 0.18) return Mat.BIO_MAGENTA;
                if (n > 0.88) return Mat.STONEBRICK_CRACKED;
                return Mat.STONE;
            }
            default:
                return Mat.CONC_GRAY;
        }
    }
}
