package blame.blamegen.core;

/**
 * A 16x16x16 block buffer at a world-space origin, with clipped raster primitives.
 * Every primitive first rejects by bounding box, so layout code can issue draw calls
 * for whole structures and only the parts that touch this cube cost anything.
 * Index layout matches CubePrimer: y << 8 | z << 4 | x.
 */
public final class Cube {
    public final int x0, y0, z0;
    public final byte[] b = new byte[4096];

    public Cube(int cubeX, int cubeY, int cubeZ) {
        this.x0 = cubeX << 4;
        this.y0 = cubeY << 4;
        this.z0 = cubeZ << 4;
    }

    public boolean hits(int minX, int minY, int minZ, int maxX, int maxY, int maxZ) {
        return maxX >= x0 && minX <= x0 + 15 && maxY >= y0 && minY <= y0 + 15 && maxZ >= z0 && minZ <= z0 + 15;
    }

    public boolean hitsY(int minY, int maxY) {
        return maxY >= y0 && minY <= y0 + 15;
    }

    public void set(int x, int y, int z, byte m) {
        int lx = x - x0, ly = y - y0, lz = z - z0;
        if ((lx | ly | lz) >= 0 && lx < 16 && ly < 16 && lz < 16) {
            b[ly << 8 | lz << 4 | lx] = m;
        }
    }

    public byte get(int x, int y, int z) {
        int lx = x - x0, ly = y - y0, lz = z - z0;
        if ((lx | ly | lz) >= 0 && lx < 16 && ly < 16 && lz < 16) {
            return b[ly << 8 | lz << 4 | lx];
        }
        return Mat.AIR;
    }

    /** Solid axis-aligned box, inclusive bounds. */
    public void box(int minX, int minY, int minZ, int maxX, int maxY, int maxZ, byte m) {
        int ax = Math.max(minX, x0) - x0, bx = Math.min(maxX, x0 + 15) - x0;
        int ay = Math.max(minY, y0) - y0, by = Math.min(maxY, y0 + 15) - y0;
        int az = Math.max(minZ, z0) - z0, bz = Math.min(maxZ, z0 + 15) - z0;
        if (ax > bx || ay > by || az > bz) return;
        for (int y = ay; y <= by; y++)
            for (int z = az; z <= bz; z++) {
                int base = y << 8 | z << 4;
                for (int x = ax; x <= bx; x++) b[base | x] = m;
            }
    }

    /** Box that only overwrites air (used for attachments that must not cut through structure). */
    public void boxIfAir(int minX, int minY, int minZ, int maxX, int maxY, int maxZ, byte m) {
        int ax = Math.max(minX, x0) - x0, bx = Math.min(maxX, x0 + 15) - x0;
        int ay = Math.max(minY, y0) - y0, by = Math.min(maxY, y0 + 15) - y0;
        int az = Math.max(minZ, z0) - z0, bz = Math.min(maxZ, z0 + 15) - z0;
        if (ax > bx || ay > by || az > bz) return;
        for (int y = ay; y <= by; y++)
            for (int z = az; z <= bz; z++) {
                int base = y << 8 | z << 4;
                for (int x = ax; x <= bx; x++) if (b[base | x] == Mat.AIR) b[base | x] = m;
            }
    }

    /**
     * Vertical cylinder or annulus (inner radius ri may be 0) centred at (cx, cz), with an
     * optional angular gap: blocks whose angle lies in [gapA, gapA + gapW) radians are skipped.
     * Elliptical: rx/rz scale the radii.
     */
    public void cylinder(double cx, double cz, double ro, double ri, double ex, double ez,
                         int minY, int maxY, double gapA, double gapW, byte m) {
        int minX = (int) Math.floor(cx - ro * ex) - 1, maxX = (int) Math.ceil(cx + ro * ex) + 1;
        int minZ = (int) Math.floor(cz - ro * ez) - 1, maxZ = (int) Math.ceil(cz + ro * ez) + 1;
        if (!hits(minX, minY, minZ, maxX, maxY, maxZ)) return;
        int ax = Math.max(minX, x0), bx = Math.min(maxX, x0 + 15);
        int ay = Math.max(minY, y0), by = Math.min(maxY, y0 + 15);
        int az = Math.max(minZ, z0), bz = Math.min(maxZ, z0 + 15);
        double ro2 = ro * ro, ri2 = ri * ri;
        for (int z = az; z <= bz; z++) {
            double dz = (z + 0.5 - cz) / ez;
            for (int x = ax; x <= bx; x++) {
                double dx = (x + 0.5 - cx) / ex;
                double d2 = dx * dx + dz * dz;
                if (d2 >= ro2 || d2 < ri2) continue;
                if (gapW > 0) {
                    double a = Math.atan2(dz, dx) - gapA;
                    a -= Math.floor(a / (2 * Math.PI)) * 2 * Math.PI;
                    if (a < gapW) continue;
                }
                for (int y = ay; y <= by; y++) b[(y - y0) << 8 | (z - z0) << 4 | (x - x0)] = m;
            }
        }
    }

    /** Sphere (or spherical shell when ri > 0). */
    public void sphere(double cx, double cy, double cz, double ro, double ri, byte m) {
        int minX = (int) Math.floor(cx - ro) - 1, maxX = (int) Math.ceil(cx + ro) + 1;
        int minY = (int) Math.floor(cy - ro) - 1, maxY = (int) Math.ceil(cy + ro) + 1;
        int minZ = (int) Math.floor(cz - ro) - 1, maxZ = (int) Math.ceil(cz + ro) + 1;
        if (!hits(minX, minY, minZ, maxX, maxY, maxZ)) return;
        double ro2 = ro * ro, ri2 = ri * ri;
        for (int y = Math.max(minY, y0); y <= Math.min(maxY, y0 + 15); y++)
            for (int z = Math.max(minZ, z0); z <= Math.min(maxZ, z0 + 15); z++)
                for (int x = Math.max(minX, x0); x <= Math.min(maxX, x0 + 15); x++) {
                    double dx = x + 0.5 - cx, dy = y + 0.5 - cy, dz = z + 0.5 - cz;
                    double d2 = dx * dx + dy * dy + dz * dz;
                    if (d2 < ro2 && d2 >= ri2) b[(y - y0) << 8 | (z - z0) << 4 | (x - x0)] = m;
                }
    }

    /**
     * Square-section tube along an arbitrary axis, with a true transverse cross-section
     * (unlike {@link #beam}, which measures in the axis-aligned frame). ri &gt; 0 leaves the
     * interior hollow, so a shell plus a carve gives a walkable passageway.
     */
    public void tube(double ax, double ay, double az, double bx, double by, double bz,
                     double ro, double ri, byte m) {
        double dx = bx - ax, dy = by - ay, dz = bz - az;
        if (ro <= 0) return;
        int minX = (int) Math.floor(Math.min(ax, bx) - ro) - 1, maxX = (int) Math.ceil(Math.max(ax, bx) + ro) + 1;
        int minY = (int) Math.floor(Math.min(ay, by) - ro) - 1, maxY = (int) Math.ceil(Math.max(ay, by) + ro) + 1;
        int minZ = (int) Math.floor(Math.min(az, bz) - ro) - 1, maxZ = (int) Math.ceil(Math.max(az, bz) + ro) + 1;
        if (!hits(minX, minY, minZ, maxX, maxY, maxZ)) return;
        double len = Math.sqrt(dx * dx + dy * dy + dz * dz);
        if (len < 1e-9) return;
        // Orthonormal basis with u,v perpendicular to the axis.
        double ux, uy, uz;
        if (Math.abs(dx) <= Math.abs(dy) && Math.abs(dx) <= Math.abs(dz)) { ux = 0; uy = -dz; uz = dy; }
        else if (Math.abs(dy) <= Math.abs(dz)) { ux = -dz; uy = 0; uz = dx; }
        else { ux = dy; uy = -dx; uz = 0; }
        double lu = Math.sqrt(ux * ux + uy * uy + uz * uz);
        ux /= lu; uy /= lu; uz /= lu;
        double vx = dy * uz - dz * uy, vy = dz * ux - dx * uz, vz = dx * uy - dy * ux;
        for (int y = Math.max(minY, y0); y <= Math.min(maxY, y0 + 15); y++)
            for (int z = Math.max(minZ, z0); z <= Math.min(maxZ, z0 + 15); z++)
                for (int x = Math.max(minX, x0); x <= Math.min(maxX, x0 + 15); x++) {
                    double px = x + 0.5 - ax, py = y + 0.5 - ay, pz = z + 0.5 - az;
                    double t = (px * dx + py * dy + pz * dz) / len;
                    if (t < 0 || t > len) continue;
                    double qx = px - (t / len) * dx, qy = py - (t / len) * dy, qz = pz - (t / len) * dz;
                    double ou = Math.abs(qx * ux + qy * uy + qz * uz);
                    double ov = Math.abs(qx * vx + qy * vy + qz * vz);
                    double d = Math.max(ou, ov);
                    if (d <= ro && d > ri) b[(y - y0) << 8 | (z - z0) << 4 | (x - x0)] = m;
                }
    }

    /**
     * Tapered vertical column: circular cross-section shrinking linearly from r0 at minY to
     * r1 at maxY. Rasterised in bands so a spire keeps a stepped, machined silhouette.
     */
    public void taper(double cx, double cz, double r0, double r1, int minY, int maxY, int band, byte m) {
        if (Math.max(r0, r1) <= 0) return;
        int minX = (int) Math.floor(cx - Math.max(r0, r1)) - 1, maxX = (int) Math.ceil(cx + Math.max(r0, r1)) + 1;
        int minZ = (int) Math.floor(cz - Math.max(r0, r1)) - 1, maxZ = (int) Math.ceil(cz + Math.max(r0, r1)) + 1;
        if (!hits(minX, minY, minZ, maxX, maxY, maxZ) || maxY < minY) return;
        int step = Math.max(1, band);
        for (int yb = minY; yb <= maxY; yb += step) {
            int ye = Math.min(maxY, yb + step - 1);
            double t = maxY == minY ? 0 : (double) (ye - minY) / (maxY - minY);
            double r = r0 + (r1 - r0) * t;
            int yc = Math.max(yb, y0), yd = Math.min(ye, y0 + 15);
            if (yd < yc || r <= 0) continue;
            int ax = Math.max(minX, x0), bx = Math.min(maxX, x0 + 15);
            int az = Math.max(minZ, z0), bz = Math.min(maxZ, z0 + 15);
            double ro2 = r * r;
            for (int z = az; z <= bz; z++) {
                double dz = z + 0.5 - cz;
                for (int x = ax; x <= bx; x++) {
                    double dx = x + 0.5 - cx;
                    if (dx * dx + dz * dz > ro2) continue;
                    int base = (z - z0) << 4 | (x - x0);
                    for (int y = yc; y <= yd; y++) b[(y - y0) << 8 | base] = m;
                }
            }
        }
    }

    /**
     * Vertical circular wall shell with caller-defined thickness.
     * Unlike a solid prism followed by an air carve, this writes only the annular wall.
     * This is useful for huge hollow cylinders such as Toha Heavy Industries.
     */
    public void shellPrism(double cx, double cz, double outerR, double innerR,
                           int minY, int maxY, byte material) {
        if (outerR <= 0 || innerR < 0 || innerR >= outerR || maxY < minY) return;
        int minX = (int) Math.floor(cx - outerR) - 1;
        int maxX = (int) Math.ceil(cx + outerR) + 1;
        int minZ = (int) Math.floor(cz - outerR) - 1;
        int maxZ = (int) Math.ceil(cz + outerR) + 1;
        if (!hits(minX, minY, minZ, maxX, maxY, maxZ)) return;
        int ax = Math.max(minX, x0), bx = Math.min(maxX, x0 + 15);
        int az = Math.max(minZ, z0), bz = Math.min(maxZ, z0 + 15);
        int ay = Math.max(minY, y0), by = Math.min(maxY, y0 + 15);
        double outer2 = outerR * outerR, inner2 = innerR * innerR;
        for (int z = az; z <= bz; z++) {
            double dz = z + 0.5 - cz;
            for (int x = ax; x <= bx; x++) {
                double dx = x + 0.5 - cx;
                double d2 = dx * dx + dz * dz;
                if (d2 > outer2 || d2 < inner2) continue;
                int base = (z - z0) << 4 | (x - x0);
                for (int y = ay; y <= by; y++) {
                    b[(y - y0) << 8 | base] = material;
                }
            }
        }
    }

    /**
     * Vertical prism with an arbitrary cross-section, optional rotation about Y, optional
     * shear (leaning) and optional hollow interior. Used for the shapeless districts:
     * octagons, tilted squares, drums and leaning masses that never repeat.
     *   kind 0 = circle, 1 = octagon, 2 = square (rot = rotation).
     *   shear shifts the cross-section along x by shear per block of height, so a prism
     *   can lean without leaving the axis-aligned raster.
     */
    public void prism(double cx, double cz, double r, double rot, double shear, boolean hollow,
                      int kind, int minY, int maxY, byte m) {
        int lean = (int) Math.ceil(Math.abs(shear) * Math.max(0, maxY - minY));
        int minX = (int) Math.floor(cx - r) - 3, maxX = (int) Math.ceil(cx + r) + 3 + lean;
        int minZ = (int) Math.floor(cz - r) - 3, maxZ = (int) Math.ceil(cz + r) + 3;
        if (!hits(minX, minY, minZ, maxX, maxY, maxZ)) return;
        double rc = r * (1.0 + Math.sqrt(2)); // octagon inradius bound
        double cos = Math.cos(rot), sin = Math.sin(rot);
        for (int y = Math.max(minY, y0); y <= Math.min(maxY, y0 + 15); y++) {
            double cx2 = cx + shear * (y - minY);
            for (int z = Math.max(minZ, z0); z <= Math.min(maxZ, z0 + 15); z++) {
                double dz0 = z + 0.5 - cz;
                for (int x = Math.max(minX, x0); x <= Math.min(maxX, x0 + 15); x++) {
                    double dx = x + 0.5 - cx2;
                    double rx = dx * cos + dz0 * sin, rz = -dx * sin + dz0 * cos;
                    double a = Math.abs(rx), d = Math.abs(rz);
                    boolean inside;
                    switch (kind) {
                        case 1: inside = a <= r && d <= r && a + d <= rc; break;
                        case 2: inside = a <= r && d <= r; break;
                        default: {
                            double d2 = rx * rx + rz * rz;
                            inside = hollow ? (d2 <= r * r && d2 >= (r - 3) * (r - 3)) : d2 <= r * r;
                            break;
                        }
                    }
                    if (!inside) continue;
                    b[(y - y0) << 8 | (z - z0) << 4 | (x - x0)] = m;
                }
            }
        }
    }

    /** Rotated (tilted) square plaque, one slab thick. */
    public void tiltSlab(double cx, double cy, double cz, double w, double d, double rot, byte m) {
        double rad = Math.hypot(w, d);
        int minX = (int) Math.floor(cx - rad) - 2, maxX = (int) Math.ceil(cx + rad) + 2;
        int minZ = (int) Math.floor(cz - rad) - 2, maxZ = (int) Math.ceil(cz + rad) + 2;
        int iy = (int) Math.floor(cy) - y0;
        if (iy < 0 || iy > 15) return;
        if (!hits(minX, (int) Math.floor(cy), minZ, maxX, (int) Math.ceil(cy), maxZ)) return;
        double cos = Math.cos(rot), sin = Math.sin(rot);
        for (int z = Math.max(minZ, z0); z <= Math.min(maxZ, z0 + 15); z++) {
            double dz = z + 0.5 - cz;
            for (int x = Math.max(minX, x0); x <= Math.min(maxX, x0 + 15); x++) {
                double dx = x + 0.5 - cx;
                double rx = dx * cos + dz * sin, rz = -dx * sin + dz * cos;
                if (Math.abs(rx) <= w / 2 && Math.abs(rz) <= d / 2)
                    b[iy << 8 | (z - z0) << 4 | (x - x0)] = m;
            }
        }
    }

    /** Ellipsoid: an organic accreted blob fused onto the structure. */
    public void blob(double cx, double cy, double cz, double rx, double ry, double rz, byte m) {
        int minX = (int) Math.floor(cx - rx) - 1, maxX = (int) Math.ceil(cx + rx) + 1;
        int minY = (int) Math.floor(cy - ry) - 1, maxY = (int) Math.ceil(cy + ry) + 1;
        int minZ = (int) Math.floor(cz - rz) - 1, maxZ = (int) Math.ceil(cz + rz) + 1;
        if (!hits(minX, minY, minZ, maxX, maxY, maxZ)) return;
        double rx2 = rx * rx, ry2 = ry * ry, rz2 = rz * rz;
        for (int y = Math.max(minY, y0); y <= Math.min(maxY, y0 + 15); y++) {
            double fy = (y + 0.5 - cy) * (y + 0.5 - cy) / ry2;
            for (int z = Math.max(minZ, z0); z <= Math.min(maxZ, z0 + 15); z++) {
                double fz = (z + 0.5 - cz) * (z + 0.5 - cz) / rz2;
                for (int x = Math.max(minX, x0); x <= Math.min(maxX, x0 + 15); x++) {
                    double fx = (x + 0.5 - cx) * (x + 0.5 - cx) / rx2;
                    if (fx + fz + fy <= 1.0) b[(y - y0) << 8 | (z - z0) << 4 | (x - x0)] = m;
                }
            }
        }
    }

    /** Elliptic hollow shell: terraces, arcade drums, drums with an open ring. */
    public void ring(double cx, double cz, double rx, double rz, int minY, int maxY, int thick, byte m) {
        int minX = (int) Math.floor(cx - rx) - 1, maxX = (int) Math.ceil(cx + rx) + 1;
        int minZ = (int) Math.floor(cz - rz) - 1, maxZ = (int) Math.ceil(cz + rz) + 1;
        if (!hits(minX, minY, minZ, maxX, maxY, maxZ)) return;
        double ix = Math.max(1, rx - thick), iz = Math.max(1, rz - thick);
        for (int y = Math.max(minY, y0); y <= Math.min(maxY, y0 + 15); y++)
            for (int z = Math.max(minZ, z0); z <= Math.min(maxZ, z0 + 15); z++)
                for (int x = Math.max(minX, x0); x <= Math.min(maxX, x0 + 15); x++) {
                    double fx = (x + 0.5 - cx) / rx, fz = (z + 0.5 - cz) / rz;
                    double ox = (x + 0.5 - cx) / ix, oz = (z + 0.5 - cz) / iz;
                    if (fx * fx + fz * fz <= 1.0 && ox * ox + oz * oz >= 1.0)
                        b[(y - y0) << 8 | (z - z0) << 4 | (x - x0)] = m;
                }
    }

    /**
     * Row of arches between two 3D points: piers every {@code span} blocks with a stepped
     * arch rising to {@code rise}. Blocky by design; reads as an arcade at Minecraft scale.
     */
    public void archRow(double ax, double ay, double az, double bx, double by, double bz,
                        int span, int rise, double hw, byte m) {
        double dx = bx - ax, dy = by - ay, dz = bz - az;
        double len = Math.sqrt(dx * dx + dy * dy + dz * dz);
        if (len < 4 || span < 3) return;
        int n = (int) (len / span);
        if (n < 1) return;
        int minX = (int) Math.floor(Math.min(ax, bx)) - 2, maxX = (int) Math.ceil(Math.max(ax, bx)) + 2;
        int minY = (int) Math.floor(Math.min(ay, by)) - 2, maxY = (int) Math.ceil(Math.max(ay, by) + rise) + 2;
        int minZ = (int) Math.floor(Math.min(az, bz)) - 2, maxZ = (int) Math.ceil(Math.max(az, bz)) + 2;
        if (!hits(minX, minY, minZ, maxX, maxY, maxZ)) return;
        int bw = (int) Math.floor(hw);
        for (int q = 0; q <= n; q++) {
            double t = (double) q / n;
            int ix = (int) Math.floor(ax + dx * t), iz = (int) Math.floor(az + dz * t);
            for (int h = 0; h < rise; h++) {
                int iy = (int) Math.floor(ay + dy * t) + h - y0;
                if (iy < 0 || iy > 15) continue;
                for (int ox = -bw; ox <= bw; ox++)
                    for (int oz = -bw; oz <= bw; oz++) {
                        int xx = ix + ox - x0, zz = iz + oz - z0;
                        if ((xx | zz) >= 0 && xx < 16 && zz < 16) b[iy << 8 | zz << 4 | xx] = m;
                    }
            }
            if (q < n) {
                for (int s = 1; s < span && s < len; s++) {
                    double ts = (double) s / span;
                    double tt = (double) (q + ts) / n;
                    int h = (int) Math.round(rise * Math.sin(Math.PI * ts));
                    if (h <= 0) continue;
                    int ix2 = (int) Math.floor(ax + dx * tt), iz2 = (int) Math.floor(az + dz * tt);
                    int base = (int) Math.floor(ay + dy * tt);
                    for (int hh = 0; hh < h; hh++) {
                        int iy = base + hh - y0;
                        if (iy < 0 || iy > 15) continue;
                        int xx = ix2 - x0, zz = iz2 - z0;
                        if ((xx | zz) < 0 || xx >= 16 || zz >= 16) continue;
                        b[iy << 8 | zz << 4 | xx] = m;
                    }
                }
            }
        }
    }

    /**
     * Straight beam between two points with a square cross-section of half-width hw
     * (Chebyshev distance perpendicular to the axis, measured in the axis-aligned frame).
     */
    public void beam(double ax, double ay, double az, double bx, double by, double bz, double hw, byte m) {
        int minX = (int) Math.floor(Math.min(ax, bx) - hw) - 1, maxX = (int) Math.ceil(Math.max(ax, bx) + hw) + 1;
        int minY = (int) Math.floor(Math.min(ay, by) - hw) - 1, maxY = (int) Math.ceil(Math.max(ay, by) + hw) + 1;
        int minZ = (int) Math.floor(Math.min(az, bz) - hw) - 1, maxZ = (int) Math.ceil(Math.max(az, bz) + hw) + 1;
        if (!hits(minX, minY, minZ, maxX, maxY, maxZ)) return;
        double dx = bx - ax, dy = by - ay, dz = bz - az;
        double len2 = dx * dx + dy * dy + dz * dz;
        if (len2 < 1e-9) return;
        for (int y = Math.max(minY, y0); y <= Math.min(maxY, y0 + 15); y++)
            for (int z = Math.max(minZ, z0); z <= Math.min(maxZ, z0 + 15); z++)
                for (int x = Math.max(minX, x0); x <= Math.min(maxX, x0 + 15); x++) {
                    double px = x + 0.5 - ax, py = y + 0.5 - ay, pz = z + 0.5 - az;
                    double t = (px * dx + py * dy + pz * dz) / len2;
                    if (t < 0 || t > 1) continue;
                    double qx = px - t * dx, qy = py - t * dy, qz = pz - t * dz;
                    if (Math.abs(qx) <= hw && Math.abs(qy) <= hw && Math.abs(qz) <= hw)
                        b[(y - y0) << 8 | (z - z0) << 4 | (x - x0)] = m;
                }
    }

    /** Resolve style ids into concrete materials, using position hashes and low-frequency noise. */
    public void resolveStyles(long seed) {
        for (int ly = 0; ly < 16; ly++)
            for (int lz = 0; lz < 16; lz++)
                for (int lx = 0; lx < 16; lx++) {
                    int i = ly << 8 | lz << 4 | lx;
                    byte m = b[i];
                    if (m < Mat.STYLE_BASE) continue;
                    b[i] = Styles.resolve(seed, m, x0 + lx, y0 + ly, z0 + lz);
                }
    }
}
