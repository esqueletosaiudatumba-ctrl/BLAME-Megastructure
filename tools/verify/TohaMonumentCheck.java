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

        Cube shell = new Cube(Math.floorDiv(1100, 16), Math.floorDiv(300, 16),
                Math.floorDiv(Toha.CZ + 1340, 16));
        toha.draw(shell);
        check(Mat.isStructure(shell.get(1100, 300, Toha.CZ + 1340)),
                "authored lower front wall must occupy its designed sample");

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
            int z = Toha.CZ + Toha.CAVE_FRONT[i] - 8;
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

        Cube aperture = new Cube(Math.floorDiv(-860, 16), Math.floorDiv(370, 16),
                Math.floorDiv(Toha.CZ + 1310, 16));
        toha.draw(aperture);
        check(aperture.get(-860, 370, Toha.CZ + 1310) == Mat.AIR,
                "the south façade needs a genuine, deep slit aperture");

        Cube aiPanel = new Cube(Math.floorDiv(215, 16), Math.floorDiv(410, 16),
                Math.floorDiv(Toha.CZ + 1140, 16));
        toha.draw(aiPanel);
        check(Mat.isStructure(aiPanel.get(215, 410, Toha.CZ + 1140)),
                "the principal cave must contain its fixed AI control slab");

        check(Toha.projectedFrontZ(500) == Toha.CZ + 1350,
                "lower elevation must project its own forward shell");
        check(Toha.projectedFrontZ(800) == Toha.CZ + 1130,
                "middle elevation must account for shell setback");
        check(Toha.projectedFrontZ(1200) == Toha.CZ + 850,
                "upper elevation must account for shell setback");

        Cube transferMass = new Cube(Math.floorDiv(1830, 16), Math.floorDiv(800, 16),
                Math.floorDiv(Toha.CZ + 950, 16));
        toha.draw(transferMass);
        check(Mat.isStructure(transferMass.get(1830, 800, Toha.CZ + 950)),
                "the deliberately asymmetric eastern transfer wall must be present");
        check(transferMass.get(1700, 800, Toha.CZ + 950) == Mat.AIR,
                "the eastern transfer mass must remain hollow behind its wall");

        Cube engine = new Cube(Math.floorDiv(260, 16), Math.floorDiv(500, 16),
                Math.floorDiv(Toha.CZ - 560, 16));
        toha.draw(engine);
        check(Mat.isStructure(engine.get(260, 500, Toha.CZ - 560)),
                "gravity furnace shell must be present");

        Cube caveMachine = new Cube(Math.floorDiv(-1465, 16), Math.floorDiv(310, 16),
                Math.floorDiv(Toha.CZ + 970, 16));
        toha.draw(caveMachine);
        check(Mat.isStructure(caveMachine.get(-1465, 310, Toha.CZ + 970)),
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
