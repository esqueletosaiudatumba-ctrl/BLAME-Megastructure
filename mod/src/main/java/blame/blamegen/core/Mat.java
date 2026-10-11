package blame.blamegen.core;

/**
 * Material ids used by the pure-Java generator core. The Minecraft adapter maps
 * each id to a 1.12.2 block state; the preview renderer maps each id to a colour.
 * Ids at or above {@link #STYLE_BASE} are "styles" that are resolved to a concrete
 * material per block position (weathering, banding) in {@link Cube#resolveStyles}.
 */
public final class Mat {
    private Mat() {}

    public static final byte AIR = 0;
    public static final byte CONC_GRAY = 1;
    public static final byte CONC_BLACK = 2;
    public static final byte CONC_LGRAY = 3;
    public static final byte STONE = 4;
    public static final byte STONEBRICK = 5;
    public static final byte STONEBRICK_CRACKED = 6;
    public static final byte ANDESITE = 7;
    public static final byte TERRA_GRAY = 8;
    public static final byte TERRA_BLACK = 9;
    public static final byte TERRA_LGRAY = 10;
    public static final byte COAL = 11;
    public static final byte IRON_BLOCK = 12;
    public static final byte IRON_BARS = 13;
    public static final byte SEA_LANTERN = 14;
    public static final byte GLOWSTONE = 15;
    public static final byte END_ROD_UP = 16;
    public static final byte SAND = 17;
    public static final byte SANDSTONE = 18;
    public static final byte WATER = 19;
    /** Ladder blocks; the suffix is the direction the ladder faces (away from its wall). */
    public static final byte LADDER_N = 20;
    public static final byte LADDER_S = 21;
    public static final byte LADDER_W = 22;
    public static final byte LADDER_E = 23;
    public static final byte SLAB_BOTTOM = 24;
    public static final byte SLAB_TOP = 25;
    public static final byte DOUBLE_SLAB = 26;
    public static final byte GRAVEL = 27;
    public static final byte OBSIDIAN = 28;
    public static final byte END_ROD_DOWN = 29;
    public static final byte GLASS_GRAY = 30;
    /** Industrial warning light: a lit redstone lamp (red). */
    public static final byte LAMP_RED = 31;
    /** Industrial warning light: amber/orange glowstone variant. */
    public static final byte LAMP_AMBER = 32;
    /** Industrial warning light: green/active status. */
    public static final byte LAMP_GREEN = 33;
    /** Conduit and machine casing: prismarine plumbing. */
    public static final byte PIPE = 34;
    /** Oxidised copper casing and ducts: teal-green patina. */
    public static final byte COPPER_PATINA = 35;
    /** Raw copper: orange-brown metal. */
    public static final byte COPPER_RAW = 36;
    /** Weathered copper: green-brown transition. */
    public static final byte COPPER_WEATHERED = 37;
    /** Rust-stained concrete: iron oxide streaks. */
    public static final byte CONC_RUST = 38;
    /** Deep biome growth: faint cyan bioluminescence on structure. */
    public static final byte BIO_CYAN = 39;
    /** Deep biome growth: faint magenta bioluminescence. */
    public static final byte BIO_MAGENTA = 40;
    /** Heavy cable bundle running along structures. */
    public static final byte CABLE = 41;
    /** Ventilation duct: dark metal with grill. */
    public static final byte VENT = 42;
    /** Reinforcement rod / rebar exposed in cracked concrete. */
    public static final byte REBAR = 43;
    /** Reinforced glass: dark tinted panes in structural windows. */
    public static final byte GLASS_REINFORCED = 44;
    public static final byte COUNT = 45;

    public static final byte STYLE_BASE = 64;
    /** Weathered grey concrete with dark patches and vertical streaks: walls and masses. */
    public static final byte S_MASS = 64;
    /** Smooth stone / stone brick with occasional cracks: columns and piers. */
    public static final byte S_COLUMN = 65;
    /** Deck body: concrete with seamless slab skin on top. */
    public static final byte S_DECK = 66;
    /** Near-black structural members: beams, ribs, cables. */
    public static final byte S_DARK = 67;
    /** Pale arrival-zone stone. */
    public static final byte S_PALE = 68;
    /** Metallic machinery accents. */
    public static final byte S_METAL = 69;
    /** Rust-streaked concrete for deep/abyssal zones. */
    public static final byte S_RUST_MASS = 70;
    /** Copper/pipework for utility corridors. */
    public static final byte S_PIPEWORK = 71;
    /** Heavily weathered stone with biological colonisation. */
    public static final byte S_BIO_STONE = 72;

    /**
     * Structure that a placed object can hang from or stand on: everything that is neither
     * air nor water nor an open railing.
     */
    public static boolean isStructure(byte m) {
        return m != AIR && m != WATER && m != IRON_BARS
                && m != LADDER_N && m != LADDER_S && m != LADDER_W && m != LADDER_E;
    }

    public static boolean isSolid(byte m) {
        return m != AIR && m != WATER && m != IRON_BARS && m != END_ROD_UP && m != END_ROD_DOWN
                && m != LADDER_N && m != LADDER_S && m != LADDER_W && m != LADDER_E;
    }
}
