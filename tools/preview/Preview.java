import blame.blamegen.core.Cube;
import blame.blamegen.core.Mat;
import blame.blamegen.core.Megastructure;
import blame.blamegen.core.Toha;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.IntStream;

/**
 * Offline preview of the generator core (no Minecraft). Renders perspective ray-cast views,
 * horizontal slices and vertical sections straight from Megastructure, so geometry can be
 * reviewed without launching the game. These are schematic renders, not game screenshots.
 *
 *   persp OUT.png x y z yaw pitch [fov=80] [w=960] [h=540] [dist=420]
 *   slice OUT.png y cx cz size [scale=1]          (horizontal section at height y)
 *   section OUT.png z cx cy width height [scale=1] (vertical section at depth z, X/Y plane)
 *   bench N                                        (time N cube generations)
 *
 * Yaw follows Minecraft: 0 = +Z (south), 90 = -X (west), 180 = -Z (north). Pitch < 0 looks up.
 */
public class Preview {
    static long SEED = 0x424C414D45L; // "BLAME"
    static Megastructure MS;
    /** When enabled, the offline viewer isolates the authored landmark from the world around it. */
    static boolean TOHA_ONLY;
    static final Map<Long, byte[]> CACHE = new ConcurrentHashMap<>();
    static final Set<Long> EMPTY_CUBES = ConcurrentHashMap.newKeySet();

    static int[] COL = new int[Mat.COUNT];
    static {
        COL[Mat.CONC_GRAY] = 0x7d7d73; COL[Mat.CONC_BLACK] = 0x1c1d22; COL[Mat.CONC_LGRAY] = 0xa8a89f;
        COL[Mat.STONE] = 0x8a8a8a; COL[Mat.STONEBRICK] = 0x7a7a7a; COL[Mat.STONEBRICK_CRACKED] = 0x6f6f6f;
        COL[Mat.ANDESITE] = 0x868889; COL[Mat.TERRA_GRAY] = 0x3b2b25; COL[Mat.TERRA_BLACK] = 0x251710;
        COL[Mat.TERRA_LGRAY] = 0x876a61; COL[Mat.COAL] = 0x151515; COL[Mat.IRON_BLOCK] = 0xdcdcdc;
        COL[Mat.IRON_BARS] = 0x5a5a5a; COL[Mat.SEA_LANTERN] = 0xd8f0ea; COL[Mat.GLOWSTONE] = 0xf0d080;
        COL[Mat.END_ROD_UP] = 0xf0e8e0; COL[Mat.END_ROD_DOWN] = 0xf0e8e0; COL[Mat.SAND] = 0xdbd3a0;
        COL[Mat.SANDSTONE] = 0xd8cb97; COL[Mat.WATER] = 0x3d63d8; COL[Mat.LADDER_N] = 0x8a6a3a;
        COL[Mat.LADDER_S] = 0x8a6a3a; COL[Mat.LADDER_W] = 0x8a6a3a; COL[Mat.LADDER_E] = 0x8a6a3a;
        COL[Mat.SLAB_BOTTOM] = 0x9e9e9e; COL[Mat.SLAB_TOP] = 0x9e9e9e; COL[Mat.DOUBLE_SLAB] = 0xa6a6a6;
        COL[Mat.GRAVEL] = 0x837f7e; COL[Mat.OBSIDIAN] = 0x140f1e; COL[Mat.GLASS_GRAY] = 0x4c4c4c;
        // New materials
        COL[Mat.LAMP_RED] = 0xff2222; COL[Mat.LAMP_AMBER] = 0xffaa00; COL[Mat.LAMP_GREEN] = 0x44ff88;
        COL[Mat.PIPE] = 0x2a7a7a; COL[Mat.COPPER_PATINA] = 0x3aa8a0; COL[Mat.COPPER_RAW] = 0xc86a2a;
        COL[Mat.COPPER_WEATHERED] = 0x6a9a4a; COL[Mat.CONC_RUST] = 0x9a3a2a; COL[Mat.BIO_CYAN] = 0x44ffff;
        COL[Mat.BIO_MAGENTA] = 0xff44aa; COL[Mat.CABLE] = 0x4a4a4a; COL[Mat.VENT] = 0x3a3a3a;
        COL[Mat.REBAR] = 0x5a5a5a; COL[Mat.GLASS_REINFORCED] = 0x1a1a2a;
    }

    static boolean emissive(byte m) {
        return m == Mat.SEA_LANTERN || m == Mat.GLOWSTONE || m == Mat.END_ROD_UP || m == Mat.END_ROD_DOWN
                || m == Mat.LAMP_RED || m == Mat.LAMP_AMBER || m == Mat.LAMP_GREEN
                || m == Mat.BIO_CYAN || m == Mat.BIO_MAGENTA;
    }

    static long cubeKey(int cx, int cy, int cz) {
        return ((long) cx & 0x1FFFFF) << 42 | ((long) cy & 0x1FFFFF) << 21 | ((long) cz & 0x1FFFFF);
    }

    static byte[] cube(int cx, int cy, int cz) {
        long key = cubeKey(cx, cy, cz);
        byte[] b = CACHE.get(key);
        if (b == null) {
            Cube c = new Cube(cx, cy, cz);
            if (TOHA_ONLY) MS.generateMonument(c);
            else MS.generate(c);
            b = c.b;
            if (TOHA_ONLY) {
                boolean empty = true;
                for (byte m : b) {
                    if (m != Mat.AIR) { empty = false; break; }
                }
                if (empty) EMPTY_CUBES.add(key);
            }
            CACHE.put(key, b);
        }
        return b;
    }

    static byte get(int x, int y, int z) {
        byte[] b = cube(x >> 4, y >> 4, z >> 4);
        return b[(y & 15) << 8 | (z & 15) << 4 | (x & 15)];
    }

    public static void main(String[] a) throws Exception {
        if (System.getenv("BLAME_SEED") != null) SEED = Long.parseLong(System.getenv("BLAME_SEED"));
        MS = new Megastructure(SEED);
        switch (a[0]) {
            case "persp": persp(a); break;
            case "slice": slice(a); break;
            case "section": section(a); break;
            case "toha-persp": TOHA_ONLY = true; persp(a); break;
            case "toha-slice": TOHA_ONLY = true; slice(a); break;
            case "toha-section": TOHA_ONLY = true; section(a); break;
            case "toha-elevation": TOHA_ONLY = true; elevation(a); break;
            case "bench": bench(Integer.parseInt(a[1])); break;
            case "spawn": System.out.println("spawnY=" + MS.spawnY()); break;
            default: throw new IllegalArgumentException(a[0]);
        }
    }

    static void bench(int n) {
        long t0 = System.nanoTime();
        java.util.Random r = new java.util.Random(1);
        for (int i = 0; i < n; i++) {
            Cube c = new Cube(r.nextInt(400) - 200, r.nextInt(400) - 200, r.nextInt(400) - 200);
            MS.generate(c);
        }
        long t1 = System.nanoTime();
        for (int i = 0; i < n; i++) {
            Cube c = new Cube(r.nextInt(30) - 15, r.nextInt(10) - 2, r.nextInt(30) - 15);
            MS.generate(c);
        }
        long t2 = System.nanoTime();
        System.out.printf("random cubes: %.3f ms/cube, arrival cubes: %.3f ms/cube%n", (t1 - t0) / 1e6 / n, (t2 - t1) / 1e6 / n);
    }

    static void persp(String[] a) throws Exception {
        String out = a[1];
        double px = Double.parseDouble(a[2]), py = Double.parseDouble(a[3]), pz = Double.parseDouble(a[4]);
        double yaw = Math.toRadians(Double.parseDouble(a[5])), pitch = Math.toRadians(Double.parseDouble(a[6]));
        double fov = Math.toRadians(a.length > 7 ? Double.parseDouble(a[7]) : 80);
        int w = a.length > 8 ? Integer.parseInt(a[8]) : 960, h = a.length > 9 ? Integer.parseInt(a[9]) : 540;
        double maxD = a.length > 10 ? Double.parseDouble(a[10]) : 420;
        // Minecraft look vector.
        double fx = -Math.sin(yaw) * Math.cos(pitch), fy = -Math.sin(pitch), fz = Math.cos(yaw) * Math.cos(pitch);
        double rx = -Math.cos(yaw), rz = -Math.sin(yaw); // right vector
        double ux = fy * rz - 0, uy = fz * rx - fx * rz, uz = 0 - fy * rx; // up = right x forward (approx)
        // Recompute up properly: up = right x forward
        ux = (0 * fz - rz * fy);
        uy = (rz * fx - rx * fz);
        uz = (rx * fy - 0 * fx);
        double tanH = Math.tan(fov / 2), aspect = (double) w / h;
        BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
        // In game, the mod darkens the fog when the player is out of skylight; mimic that.
        DARK = !skyOpen((int) Math.floor(px), (int) Math.floor(py), (int) Math.floor(pz));
        final double ffx = fx, ffy = fy, ffz = fz, fux = ux, fuy = uy, fuz = uz;
        IntStream.range(0, h).parallel().forEach(j -> {
            for (int i = 0; i < w; i++) {
                double sx = (2 * (i + 0.5) / w - 1) * tanH * aspect, sy = (1 - 2 * (j + 0.5) / h) * tanH;
                double dx = ffx + sx * rx + sy * fux, dy = ffy + sy * fuy, dz = ffz + sx * rz + sy * fuz;
                double len = Math.sqrt(dx * dx + dy * dy + dz * dz);
                dx /= len; dy /= len; dz /= len;
                img.setRGB(i, j, trace(px, py, pz, dx, dy, dz, maxD));
            }
        });
        ImageIO.write(img, "png", new File(out));
        System.out.println("wrote " + out + " cubes=" + CACHE.size());
    }

    static boolean DARK;
    static final boolean NIGHT = System.getenv("BLAME_NIGHT") != null;

    static int sky(double dy) {
        if (NIGHT) return 0x090c14;
        if (DARK) return 0x0a0b0e;
        double t = Math.max(0, Math.min(1, dy * 0.5 + 0.5));
        int r = (int) (150 + (120 - 150) * t), g = (int) (185 + (165 - 185) * t), b = (int) (235 + (255 - 235) * t);
        return r << 16 | g << 8 | b;
    }

    static int trace(double ox, double oy, double oz, double dx, double dy, double dz, double maxD) {
        // The Toha-only review mode has a known finite world-space bound. Intersect the ray
        // with that box first, so rays do not visit thousands of empty blocks before reaching it.
        double entry = 0.0;
        double localLimit = maxD;
        if (TOHA_ONLY) {
            double[] interval = rayBox(ox, oy, oz, dx, dy, dz,
                    Toha.X0, 0, Toha.Z0, Toha.X1 - 1, Toha.TOP, Toha.Z1 - 1, maxD);
            if (interval == null) return fogged(sky(dy), 1.0, dy);
            entry = interval[0];
            double exit = interval[1];
            if (exit <= entry) return fogged(sky(dy), 1.0, dy);
            double nudge = Math.min(0.001, (exit - entry) * 0.25);
            entry += nudge;
            ox += dx * entry;
            oy += dy * entry;
            oz += dz * entry;
            localLimit = exit - entry;
        }
        int x = (int) Math.floor(ox), y = (int) Math.floor(oy), z = (int) Math.floor(oz);
        int sx = dx > 0 ? 1 : -1, sy = dy > 0 ? 1 : -1, sz = dz > 0 ? 1 : -1;
        double tdx = Math.abs(dx) < 1.0e-12 ? Double.POSITIVE_INFINITY : Math.abs(1 / dx);
        double tdy = Math.abs(dy) < 1.0e-12 ? Double.POSITIVE_INFINITY : Math.abs(1 / dy);
        double tdz = Math.abs(dz) < 1.0e-12 ? Double.POSITIVE_INFINITY : Math.abs(1 / dz);
        double tmx = Math.abs(dx) < 1.0e-12 ? Double.POSITIVE_INFINITY : (dx > 0 ? (x + 1 - ox) : (ox - x)) * tdx;
        double tmy = Math.abs(dy) < 1.0e-12 ? Double.POSITIVE_INFINITY : (dy > 0 ? (y + 1 - oy) : (oy - y)) * tdy;
        double tmz = Math.abs(dz) < 1.0e-12 ? Double.POSITIVE_INFINITY : (dz > 0 ? (z + 1 - oz) : (oz - z)) * tdz;
        int face = 0;
        double t = 0;
        boolean inWater = false;
        double waterT = -1;
        while (t < localLimit) {
            byte m;
            if (TOHA_ONLY) {
                int cx = x >> 4, cy = y >> 4, cz = z >> 4;
                long key = cubeKey(cx, cy, cz);
                byte[] blocks = cube(cx, cy, cz);
                if (EMPTY_CUBES.contains(key)) {
                    // Jump directly to the next 16-block cube plane instead of
                    // testing every block in large empty rooms and air columns.
                    double tx = Math.abs(dx) < 1.0e-12 ? Double.POSITIVE_INFINITY
                            : (((dx > 0 ? (cx + 1) * 16.0 : cx * 16.0) - ox) / dx);
                    double ty = Math.abs(dy) < 1.0e-12 ? Double.POSITIVE_INFINITY
                            : (((dy > 0 ? (cy + 1) * 16.0 : cy * 16.0) - oy) / dy);
                    double tz = Math.abs(dz) < 1.0e-12 ? Double.POSITIVE_INFINITY
                            : (((dz > 0 ? (cz + 1) * 16.0 : cz * 16.0) - oz) / dz);
                    double next = Math.min(tx, Math.min(ty, tz));
                    if (!Double.isFinite(next) || next >= localLimit) break;
                    t = Math.max(t, next) + 0.001;
                    if (t >= localLimit) break;
                    x = (int) Math.floor(ox + dx * t);
                    y = (int) Math.floor(oy + dy * t);
                    z = (int) Math.floor(oz + dz * t);
                    tmx = Math.abs(dx) < 1.0e-12 ? Double.POSITIVE_INFINITY : (dx > 0 ? (x + 1 - ox) : (ox - x)) * tdx;
                    tmy = Math.abs(dy) < 1.0e-12 ? Double.POSITIVE_INFINITY : (dy > 0 ? (y + 1 - oy) : (oy - y)) * tdy;
                    tmz = Math.abs(dz) < 1.0e-12 ? Double.POSITIVE_INFINITY : (dz > 0 ? (z + 1 - oz) : (oz - z)) * tdz;
                    face = 0;
                    continue;
                }
                m = blocks[(y & 15) << 8 | (z & 15) << 4 | (x & 15)];
            } else {
                m = get(x, y, z);
            }
            if (m == Mat.WATER) {
                if (!inWater) { inWater = true; waterT = t; }
            } else if (m != Mat.AIR) {
                return shade(m, face, x, y, z, t + entry, maxD, inWater ? t - waterT : 0, dy);
            }
            if (tmx < tmy && tmx < tmz) { x += sx; t = tmx; tmx += tdx; face = sx > 0 ? 1 : 2; }
            else if (tmy < tmz) { y += sy; t = tmy; tmy += tdy; face = sy > 0 ? 3 : 4; }
            else { z += sz; t = tmz; tmz += tdz; face = sz > 0 ? 5 : 6; }
        }
        int s = sky(dy);
        if (inWater) s = blend(s, 0x3d63d8, 0.6);
        return fogged(s, 1.0, dy);
    }

    /**
     * Ray/AABB interval, clipped to the viewer's maximum distance. Used only by the
     * isolated preview path; gameplay block generation and ray tracing are unaffected.
     */
    static double[] rayBox(double ox, double oy, double oz, double dx, double dy, double dz,
                           double minX, double minY, double minZ,
                           double maxX, double maxY, double maxZ, double maxD) {
        double near = Double.NEGATIVE_INFINITY, far = Double.POSITIVE_INFINITY;
        double[] o = {ox, oy, oz}, d = {dx, dy, dz};
        double[] lo = {minX, minY, minZ}, hi = {maxX, maxY, maxZ};
        for (int axis = 0; axis < 3; axis++) {
            if (Math.abs(d[axis]) < 1.0e-12) {
                if (o[axis] < lo[axis] || o[axis] > hi[axis]) return null;
                continue;
            }
            double a = (lo[axis] - o[axis]) / d[axis];
            double b = (hi[axis] - o[axis]) / d[axis];
            if (a > b) { double tmp = a; a = b; b = tmp; }
            near = Math.max(near, a);
            far = Math.min(far, b);
            if (far < near) return null;
        }
        if (far < 0 || near > maxD) return null;
        double start = Math.max(0, near);
        double end = Math.min(maxD, far);
        return end < start ? null : new double[] {start, end};
    }

    static boolean colOpen(int x, int y, int z) {
        for (int k = 1; k < 400; k++) {
            byte m = get(x, y + k, z);
            if (m != Mat.AIR && m != Mat.WATER && m != Mat.IRON_BARS) return false;
        }
        return true;
    }

    /**
     * Approximates Minecraft skylight: a surface also reads as lit when light reaches
     * within ~6 blocks sideways, so walls under overhangs are not pure black.
     */
    static boolean skyOpen(int x, int y, int z) {
        return colOpen(x, y, z) || colOpen(x + 6, y, z) || colOpen(x - 6, y, z)
                || colOpen(x, y, z + 6) || colOpen(x, y, z - 6);
    }

    static int shade(byte m, int face, int x, int y, int z, double t, double maxD, double water, double dy) {
        int c = COL[m];
        double f;
        switch (face) {
            case 4: f = 1.0; break;      // hit from above (top face)
            case 3: f = 0.5; break;      // bottom face
            case 1: case 2: f = 0.8; break;
            default: f = 0.65; break;
        }
        double light;
        if (emissive(m)) light = NIGHT ? 1.9 : 1.6;
        else if (TOHA_ONLY) {
            // Isolated art review is a silhouette/material proof, not a skylight simulation.
            // Avoid 5 x 400 voxel probes per pixel; face shading still keeps the volumes readable.
            light = NIGHT ? 0.72 : 1.0;
        } else {
            int nx = x, ny = y, nz = z;
            if (face == 1) nx--; else if (face == 2) nx++; else if (face == 3) ny--; else if (face == 4) ny++; else if (face == 5) nz--; else nz++;
            light = NIGHT ? 0.72 : skyOpen(nx, ny, nz) ? 1.0 : 0.22;
        }
        int r = (int) Math.min(255, ((c >> 16) & 255) * f * light);
        int g = (int) Math.min(255, ((c >> 8) & 255) * f * light);
        int b = (int) Math.min(255, (c & 255) * f * light);
        if (NIGHT && !emissive(m)) { r = (int) (r * 0.62); g = (int) (g * 0.76); b = Math.min(255, (int) (b * 1.05)); }
        int col = r << 16 | g << 8 | b;
        if (water > 0) col = blend(col, 0x2d4bb0, Math.min(0.85, 0.3 + water / 12));
        return fogged(col, t / maxD, dy);
    }

    static int fogged(int col, double frac, double dy) {
        double k = Math.max(0, Math.min(1, (frac - 0.55) / 0.45));
        return blend(col, sky(dy) & 0xFFFFFF, k * 0.9);
    }

    static int blend(int a, int b, double k) {
        int r = (int) (((a >> 16) & 255) * (1 - k) + ((b >> 16) & 255) * k);
        int g = (int) (((a >> 8) & 255) * (1 - k) + ((b >> 8) & 255) * k);
        int bb = (int) ((a & 255) * (1 - k) + (b & 255) * k);
        return r << 16 | g << 8 | bb;
    }

    /**
     * Orthographic front elevation. Because Toha's shells deliberately step backward
     * with height, each tier is sampled on its own front plane; a single Z slice
     * misleadingly shows only the lowest wall and hides the upper cave entrances.
     * Usage: toha-elevation OUT.png cx cy width height [scale=4]
     */
    static void elevation(String[] a) throws Exception {
        String out = a[1];
        int cx = Integer.parseInt(a[2]), cy = Integer.parseInt(a[3]);
        int width = Integer.parseInt(a[4]), height = Integer.parseInt(a[5]);
        int scale = a.length > 6 ? Integer.parseInt(a[6]) : 4;
        int nw = width / scale, nh = height / scale;
        BufferedImage img = new BufferedImage(nw, nh, BufferedImage.TYPE_INT_RGB);
        IntStream.range(0, nh).parallel().forEach(j -> {
            int y = cy + height / 2 - j * scale;
            int z = Toha.projectedFrontZ(y);
            for (int i = 0; i < nw; i++) {
                int x = cx - width / 2 + i * scale;
                byte m = get(x, y, z);
                int col = m == Mat.AIR ? 0x0b0c12 : COL[m];
                img.setRGB(i, j, col);
            }
        });
        ImageIO.write(img, "png", new File(out));
        System.out.println("wrote " + out + " orthographic tiers=" + nh + "x" + nw + " cubes=" + CACHE.size());
    }

    static void slice(String[] a) throws Exception {
        String out = a[1];
        int y = Integer.parseInt(a[2]), cx = Integer.parseInt(a[3]), cz = Integer.parseInt(a[4]), size = Integer.parseInt(a[5]);
        int scale = a.length > 6 ? Integer.parseInt(a[6]) : 1;
        int n = size / scale;
        BufferedImage img = new BufferedImage(n, n, BufferedImage.TYPE_INT_RGB);
        IntStream.range(0, n).parallel().forEach(j -> {
            for (int i = 0; i < n; i++) {
                int x = cx - size / 2 + i * scale, z = cz - size / 2 + j * scale;
                byte m = get(x, y, z);
                int col = m == Mat.AIR ? 0x101018 : COL[m];
                img.setRGB(i, j, col);
            }
        });
        ImageIO.write(img, "png", new File(out));
        System.out.println("wrote " + out);
    }

    static void section(String[] a) throws Exception {
        String out = a[1];
        int z = Integer.parseInt(a[2]), cx = Integer.parseInt(a[3]), cy = Integer.parseInt(a[4]);
        int wd = Integer.parseInt(a[5]), ht = Integer.parseInt(a[6]);
        int scale = a.length > 7 ? Integer.parseInt(a[7]) : 1;
        int nw = wd / scale, nh = ht / scale;
        BufferedImage img = new BufferedImage(nw, nh, BufferedImage.TYPE_INT_RGB);
        IntStream.range(0, nh).parallel().forEach(j -> {
            for (int i = 0; i < nw; i++) {
                int x = cx - wd / 2 + i * scale, y = cy + ht / 2 - j * scale;
                byte m = get(x, y, z);
                img.setRGB(i, j, m == Mat.AIR ? 0x101018 : COL[m]);
            }
        });
        ImageIO.write(img, "png", new File(out));
        System.out.println("wrote " + out);
    }
}
