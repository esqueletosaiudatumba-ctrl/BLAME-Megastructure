import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.MethodVisitor;

import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

/**
 * MCP &lt;-&gt; SRG member remapper for Minecraft 1.12.2, built on ASM.
 *
 * Reobfuscation in Forge 1.12.2 means the shipped mod jar must reference Minecraft by
 * <em>SRG</em> names ({@code field_150363_b}, {@code func_180647_a}), while the sources are
 * written with MCP names ({@code CONCRETE}, {@code getDefaultState}). RetroFuturaGradle
 * normally performs both directions; this tool does the same job standalone, using the MCP
 * stable_39 CSVs that the mod build already downloads.
 *
 * Members are only renamed in classes under {@code net/minecraft/}, and references are only
 * renamed when the owning class is under {@code net/minecraft/} - so a mod's or a
 * dependency's own methods that happen to share an MCP name are never touched.
 *
 * Usage: SrgMcpChanger &lt;mode&gt; &lt;in.jar&gt; &lt;out.jar&gt; &lt;fields.csv&gt; &lt;methods.csv&gt;
 *        mode = toMcp | toSrg
 */
public final class SrgMcpChanger {

    public static void main(String[] args) throws Exception {
        boolean toMcp = args[0].equals("toMcp");
        Path in = Paths.get(args[1]);
        Path out = Paths.get(args[2]);
        Map<String, String> fields = readCsv(Paths.get(args[3]));
        Map<String, String> methods = readCsv(Paths.get(args[4]));
        Map<String, String> fwd = new HashMap<>(), back = new HashMap<>();
        for (Map.Entry<String, String> e : fields.entrySet()) {
            fwd.put(e.getKey(), e.getValue());
            back.put(e.getValue(), e.getKey());
        }
        Map<String, String> mfwd = new HashMap<>(), mback = new HashMap<>();
        for (Map.Entry<String, String> e : methods.entrySet()) {
            mfwd.put(e.getKey(), e.getValue());
            mback.put(e.getValue(), e.getKey());
        }
        String mode = toMcp ? "srg->mcp" : "mcp->srg";

        int count = 0;
        try (ZipInputStream zin = new ZipInputStream(Files.newInputStream(in));
             ZipOutputStream zout = new ZipOutputStream(Files.newOutputStream(out))) {
            ZipEntry e;
            byte[] buf = new byte[16384];
            while ((e = zin.getNextEntry()) != null) {
                ZipEntry ne = new ZipEntry(e.getName());
                zout.putNextEntry(ne);
                if (e.getName().endsWith(".class")) {
                    byte[] raw = zin.readAllBytes();
                    ClassReader cr = new ClassReader(raw);
                    ClassWriter cw = new ClassWriter(0);
                    cr.accept(new Renamer(cw, toMcp ? fwd : back, toMcp ? mfwd : mback), 0);
                    byte[] outBytes = cw.toByteArray();
                    if (!java.util.Arrays.equals(raw, outBytes)) count++;
                    zout.write(outBytes);
                } else {
                    int n;
                    while ((n = zin.read(buf)) > 0) zout.write(buf, 0, n);
                }
                zout.closeEntry();
            }
        }
        System.out.println(mode + ": " + count + " classes remapped -> " + out);
    }

    /** Minimal CSV read: col0 = srg, col1 = mcp. Handles the quoted desc column. */
    private static Map<String, String> readCsv(Path p) throws Exception {
        Map<String, String> m = new HashMap<>();
        try (Scanner sc = new Scanner(Files.newInputStream(p), StandardCharsets.UTF_8.name())) {
            if (sc.hasNextLine()) sc.nextLine(); // header
            while (sc.hasNextLine()) {
                String line = sc.nextLine();
                int c = line.indexOf(',');
                if (c <= 0) continue;
                int c2 = line.indexOf(',', c + 1);
                if (c2 <= 0) continue;
                m.put(line.substring(0, c), line.substring(c + 1, c2));
            }
        }
        return m;
    }

    private static final class Renamer extends ClassVisitor {
        private final Map<String, String> fields;
        private final Map<String, String> methods;

        Renamer(ClassVisitor cv, Map<String, String> fields, Map<String, String> methods) {
            super(458752, cv); // ASM9
            this.fields = fields;
            this.methods = methods;
        }

        @Override
        public FieldVisitor visitField(int access, String name, String descriptor, String signature, Object value) {
            return super.visitField(access, map(fields, name), descriptor, signature, value);
        }

        @Override
        public MethodVisitor visitMethod(int access, String name, String descriptor, String signature, String[] exceptions) {
            return super.visitMethod(access, map(methods, name), descriptor, signature, exceptions);
        }
    }

    private static String map(Map<String, String> m, String n) {
        String r = m.get(n);
        return r != null ? r : n;
    }
}
