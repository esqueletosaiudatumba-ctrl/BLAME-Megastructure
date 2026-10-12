package blame.blamegen.core;

/** Regression checks for Toha's narrow spire, enclosing machine valley, and access network. */
public final class TohaMonumentCheck {
    private static int failures;

    private static void check(boolean condition, String message) {
        if (!condition) {
            failures++;
            System.out.println("FAIL: " + message);
        }
    }

    private static Cube cubeAt(int x, int y, int z) {
        return new Cube(Math.floorDiv(x, 16), Math.floorDiv(y, 16), Math.floorDiv(z, 16));
    }

    public static void main(String[] args) {
        Toha toha = new Megastructure(0x424C414D45L).toha;
        int czCube = Math.floorDiv(Toha.CZ, 16);

        check(toha.overlaps(cubeAt(0, 100, Toha.CZ)), "spire centre must be in monument bounds");
        check(!toha.overlaps(new Cube(256, 10, czCube)), "monument X bound must end at +4096");
        check(!toha.overlaps(cubeAt(0, 10, Toha.Z1)), "monument Z bound must end at its far edge");

        check(Toha.spireRadiusAtY(100) > 400,
                "the lower spire must have a substantial industrial plinth");
        check(Toha.spireRadiusAtY(1800) < 75,
                "the crown must narrow to a spire, not remain a wide drum");
        check(Toha.spireRadiusAtY(1800) < Toha.spireRadiusAtY(100) / 5.0,
                "the monument silhouette must become dramatically narrower with height");

        // Check a real shell sample through the thick outer skin and a true hollow interior.
        int shellY = 400;
        int shellZ = Toha.CZ + (int) Math.round(Toha.spireRadiusAtY(shellY)) - 10;
        Cube shell = cubeAt(0, shellY, shellZ);
        toha.draw(shell);
        check(Mat.isStructure(shell.get(0, shellY, shellZ)),
                "the tapered outer spire shell must occupy its authored surface");
        Cube innerVoid = cubeAt(240, 500, Toha.CZ);
        toha.draw(innerVoid);
        check(innerVoid.get(240, 500, Toha.CZ) == Mat.AIR,
                "the hollow spire must preserve open internal volume");

        // A valley cliff should frame the view from far below, with open negative space
        // between the wall and the spire.
        Cube cliff = cubeAt(-3240, 500, Toha.CZ + 150);
        toha.draw(cliff);
        check(Mat.isStructure(cliff.get(-3240, 500, Toha.CZ + 150)),
                "the west megastructure valley wall must be a real cliff mass");
        Cube valleyAir = cubeAt(1450, 100, Toha.CZ + 1700);
        toha.draw(valleyAir);
        check(valleyAir.get(1450, 100, Toha.CZ + 1700) == Mat.AIR,
                "the canyon must remain open beyond the central machinery yards");

        // Massive south gate masses leave a clear central portal and approach path.
        int southZ = Toha.CZ + 3560;
        Cube gatePier = cubeAt(-700, 544, southZ);
        toha.draw(gatePier);
        check(Mat.isStructure(gatePier.get(-700, 544, southZ)),
                "the valley entrance must have a monumental gate pier");
        Cube gateOpening = cubeAt(0, 100, southZ);
        toha.draw(gateOpening);
        check(gateOpening.get(0, 100, southZ) == Mat.AIR,
                "the centre of the valley gate must remain passable");

        // Each of the thirteen chambers must have a genuine surface opening, aligned to
        // one of four different sides as the spire tapers.
        int openCaves = 0;
        for (int i = 0; i < Toha.CAVE_Y.length; i++) {
            int x = Toha.caveCenterX(i);
            int y = Toha.CAVE_Y[i];
            int z = Toha.caveCenterZ(i);
            int side = Toha.caveDirection(i);
            if (side == 0) z += 4;
            else if (side == 1) x += 4;
            else if (side == 2) z -= 4;
            else x -= 4;
            Cube mouth = cubeAt(x, y, z);
            toha.draw(mouth);
            if (mouth.get(x, y, z) == Mat.AIR) openCaves++;
            else check(false, "authored AI cave mouth " + i + " must remain open");
        }
        check(openCaves == 13, "all thirteen staggered AI chambers must remain traversable");

        // Service circulation: helical treads and at least one transverse bridge must be
        // made of actual half-height slabs, not decorative linework alone.
        int stairY = 500;
        int stairX = Toha.spiralXAtY(stairY), stairZ = Toha.spiralZAtY(stairY);
        Cube stair = cubeAt(stairX, stairY, stairZ);
        toha.draw(stair);
        check(stair.get(stairX, stairY, stairZ) == Mat.SLAB_BOTTOM,
                "the outer helix must have real slab treads at walkable elevations");

        int bridgeY = 555;
        Cube bridge = cubeAt(-2000, bridgeY, Toha.CZ);
        toha.draw(bridge);
        check(bridge.get(-2000, bridgeY, Toha.CZ) == Mat.SLAB_BOTTOM,
                "cross-valley paths must connect the cliff infrastructure to the spire");

        // Machine yard equipment must be block-real and richly detailed at human scale.
        Cube yard = cubeAt(-930, 69, Toha.CZ + 520);
        toha.draw(yard);
        check(yard.get(-930, 69, Toha.CZ + 520) == Mat.SLAB_TOP,
                "the machine yard must contain slab-topped service modules");

        int markX = -6, markY = 274;
        int markZ = Toha.projectedFrontZ(markX, markY) - 12 + 2;
        Cube inscription = cubeAt(markX, markY, markZ);
        toha.draw(inscription);
        check(inscription.get(markX, markY, markZ) == Mat.COAL,
                "the spire must retain the matte-black 東亜重工 hull mark");

        System.out.println(failures == 0
                ? "TOHA MONUMENT CHECK PASS"
                : "TOHA MONUMENT CHECK FAILURES=" + failures);
        if (failures != 0) System.exit(1);
    }
}
