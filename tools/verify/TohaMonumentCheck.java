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

        Cube shell = new Cube(Math.floorDiv(1500, 16), Math.floorDiv(300, 16),
                Math.floorDiv(Toha.CZ + 1000, 16));
        toha.draw(shell);
        check(Mat.isStructure(shell.get(1500, 300, Toha.CZ + 1000)),
                "authored lower shell must occupy its designed sample");

        Cube mouth = new Cube(0, Math.floorDiv(300, 16),
                Math.floorDiv(Toha.CZ + 1286, 16));
        toha.draw(mouth);
        check(mouth.get(0, 300, Toha.CZ + 1286) == Mat.AIR,
                "principal cave must remain open through the lower façade");

        Cube nave = new Cube(0, Math.floorDiv(800, 16),
                Math.floorDiv(Toha.CZ + 100, 16));
        toha.draw(nave);
        check(nave.get(0, 800, Toha.CZ + 100) == Mat.AIR,
                "central nave must preserve a large open interior");

        Cube engine = new Cube(Math.floorDiv(260, 16), Math.floorDiv(500, 16),
                Math.floorDiv(Toha.CZ - 560, 16));
        toha.draw(engine);
        check(Mat.isStructure(engine.get(260, 500, Toha.CZ - 560)),
                "tapered engine shell must be present");

        System.out.println(failures == 0
                ? "TOHA MONUMENT CHECK PASS"
                : "TOHA MONUMENT CHECK FAILURES=" + failures);
        if (failures != 0) System.exit(1);
    }
}
