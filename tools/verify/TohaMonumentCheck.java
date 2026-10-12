package blame.blamegen.core;

/** Focused regression checks for the fixed Toha Heavy Industries monument. */
public final class TohaMonumentCheck {
    private static int failures;

    private static void check(boolean condition, String message) {
        if (!condition) {
            failures++;
            System.out.println("FAIL: " + message);
        }
    }

    public static void main(String[] args) {
        Megastructure m = new Megastructure(0x424C414D45L);
        Toha toha = m.toha;
        int czCube = Math.floorDiv(Toha.CZ, 16);

        check(toha.overlaps(new Cube(0, 10, czCube)), "monument centre must be in footprint");
        check(!toha.overlaps(new Cube(192, 10, czCube)), "footprint must end at X=3072");
        check(!toha.overlaps(new Cube(0, 10, Math.floorDiv(Toha.Z1, 16))),
                "footprint must end at Z=Toha.Z1");

        Cube gate = new Cube(Math.floorDiv(-700, 16), Math.floorDiv(544, 16),
                Math.floorDiv(Toha.CZ + 2600, 16));
        toha.draw(gate);
        check(Mat.isStructure(gate.get(-700, 544, Toha.CZ + 2600)),
                "the south gate must have its monumental crosshead");
        check(gate.get(0, 100, Toha.CZ + 2600) == Mat.AIR,
                "the centre of the south gate must remain passable");

        int shellZ = Toha.CZ + Toha.cylinderFrontOffset(2200, 1100) - 18;
        Cube shell = new Cube(Math.floorDiv(1100, 16), Math.floorDiv(300, 16),
                Math.floorDiv(shellZ, 16));
        toha.draw(shell);
        check(Mat.isStructure(shell.get(1100, 300, shellZ)),
                "the lower cylindrical hull must occupy its curved surface sample");
        check(shell.get(1100, 300, Toha.CZ + 1340) == Mat.AIR,
                "the cylindrical hull must remain hollow well inside its lower wall");

        Cube underDeck = new Cube(0, 1, czCube);
        toha.draw(underDeck);
        check(underDeck.get(0, 20, Toha.CZ) == Mat.AIR,
                "the precinct must not be filled as a 64-block solid slab");
        Cube deckSupport = new Cube(0, 3, czCube);
        toha.draw(deckSupport);
        check(Mat.isStructure(deckSupport.get(0, 52, Toha.CZ)),
                "the thin deck must rest on its orthogonal girder grid");

        Cube mouth = new Cube(0, Math.floorDiv(300, 16),
                Math.floorDiv(Toha.CZ + 1286, 16));
        toha.draw(mouth);
        check(mouth.get(0, 300, Toha.CZ + 1286) == Mat.AIR,
                "principal cave must remain open through the lower façade");

        int openCaves = 0;
        for (int i = 0; i < Toha.CAVE_X.length; i++) {
            int x = Toha.CAVE_X[i];
            int y = Toha.CAVE_Y[i] + Toha.CAVE_H[i] / 2;
            // Sample just outside the intended exterior plane: behind-wall AIR is
            // insufficient because a plinth or rib can still block the entrance.
            int z = Toha.CZ + Toha.caveFrontAt(i, x) - 18;
            Cube cave = new Cube(Math.floorDiv(x, 16), Math.floorDiv(y, 16), Math.floorDiv(z, 16));
            toha.draw(cave);
            if (cave.get(x, y, z) == Mat.AIR) openCaves++;
            else check(false, "authored cave mouth " + i + " must remain open");
        }
        check(openCaves == 13, "all thirteen staggered cave mouths must remain traversable voids");

        Cube nave = new Cube(0, Math.floorDiv(800, 16),
                Math.floorDiv(Toha.CZ + 100, 16));
        toha.draw(nave);
        check(nave.get(0, 800, Toha.CZ + 100) == Mat.AIR,
                "central nave must preserve a large open interior");

        int slitX = -870, slitY = 390;
        int slitZ = Toha.CZ + Toha.cylinderFrontOffset(2200, slitX) - 30;
        Cube aperture = new Cube(Math.floorDiv(slitX, 16), Math.floorDiv(slitY, 16),
                Math.floorDiv(slitZ, 16));
        toha.draw(aperture);
        check(aperture.get(slitX, slitY, slitZ) == Mat.AIR,
                "the cylindrical facade needs a genuine, deep slit aperture");

        int aiX = Toha.CAVE_X[2] + Toha.CAVE_W[2] / 2 - 22;
        int aiY = Toha.CAVE_Y[2] + Math.max(22, Toha.CAVE_H[2] / 2 - 42) + 40;
        int aiZ = Toha.CZ + Toha.CAVE_FRONT[2] - 202;
        Cube aiPanel = new Cube(Math.floorDiv(aiX, 16), Math.floorDiv(aiY, 16),
                Math.floorDiv(aiZ, 16));
        toha.draw(aiPanel);
        check(Mat.isStructure(aiPanel.get(aiX, aiY, aiZ)),
                "the principal cave must contain its fixed AI control slab");

        check(Toha.projectedFrontZ(0, 500) == Toha.CZ + Toha.cylinderFrontOffset(2200, 0),
                "lower elevation must follow the circular hull at the centreline");
        check(Toha.projectedFrontZ(800, 800) == Toha.CZ + Toha.cylinderFrontOffset(1900, 800),
                "middle elevation must account for the cylinder's curved surface");
        check(Toha.projectedFrontZ(1200, 1200) == Toha.CZ + Toha.cylinderFrontOffset(1600, 1200),
                "upper elevation must follow the smaller cylindrical tier");

        Cube transferMass = new Cube(Math.floorDiv(2520, 16), Math.floorDiv(800, 16),
                Math.floorDiv(Toha.CZ + 100, 16));
        toha.draw(transferMass);
        check(Mat.isStructure(transferMass.get(2520, 800, Toha.CZ + 100)),
                "the deliberately asymmetric eastern transfer wall must be present");
        check(transferMass.get(2300, 800, Toha.CZ + 100) == Mat.AIR,
                "the eastern transfer mass must remain hollow behind its wall");

        Cube engine = new Cube(Math.floorDiv(260, 16), Math.floorDiv(500, 16),
                Math.floorDiv(Toha.CZ - 560, 16));
        toha.draw(engine);
        check(Mat.isStructure(engine.get(260, 500, Toha.CZ - 560)),
                "gravity furnace shell must be present");

        int machineX = -1465, machineY = 310;
        int machineZ = Toha.CZ + Toha.CAVE_FRONT[0] - 380;
        Cube caveMachine = new Cube(Math.floorDiv(machineX, 16), Math.floorDiv(machineY, 16),
                Math.floorDiv(machineZ, 16));
        toha.draw(caveMachine);
        check(Mat.isStructure(caveMachine.get(machineX, machineY, machineZ)),
                "each subordinate cave must receive its fixed chamber machine");

        Cube crownMast = new Cube(Math.floorDiv(2115, 16), Math.floorDiv(1900, 16),
                Math.floorDiv(Toha.CZ - 900, 16));
        toha.draw(crownMast);
        check(Mat.isStructure(crownMast.get(2115, 1900, Toha.CZ - 900)),
                "the off-axis service mast must rise close to the ceiling");

        System.out.println(failures == 0
                ? "TOHA MONUMENT CHECK PASS"
                : "TOHA MONUMENT CHECK FAILURES=" + failures);
        if (failures != 0) System.exit(1);
    }
}
