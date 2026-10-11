import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

/**
 * Strips stale CubicChunks registry entries from Minecraft {@code level.dat} files.
 *
 * A world saved by a newer CubicChunks than the one installed carries FML registry
 * entries under {@code Data.fml.Registries} for
 * {@code cubicchunks:storage_format_provider_registry} and
 * {@code cubicchunks:vanilla_compatibility_generators_registry}. CubicChunks 1.12.2
 * 0.0.1301.0 does not register those names, so Forge refuses to load the save without
 * the "missing registries" warning. Removing the two keys is safe: CubicChunks then
 * falls back to its own defaults ({@code cubicchunks:anvil3d} for storage and
 * {@code cubicchunks:default} for compatibility generation, both recorded separately in
 * {@code cubicChunksData.dat}).
 *
 * Usage: NbtRegistryFix &lt;level.dat&gt; [more level.dat...] [--dry]
 */
public final class NbtRegistryFix {

    private static final List<String> DROP = Arrays.asList(
            "cubicchunks:storage_format_provider_registry",
            "cubicchunks:vanilla_compatibility_generators_registry");

    public static void main(String[] args) throws Exception {
        List<String> files = new ArrayList<>();
        boolean dry = false;
        for (String a : args) {
            if (a.equals("--dry")) dry = true;
            else files.add(a);
        }
        boolean any = false;
        for (String f : files) {
            Path p = Paths.get(f);
            byte[] raw = Files.readAllBytes(p);
            Object root = read(new DataInputStream(new GZIPInputStream(
                    new ByteArrayInputStream(raw))));
            int[] removed = {0};
            walk(root, removed);
            if (removed[0] == 0) {
                System.out.println(f + ": clean (no stale CC registries)");
                continue;
            }
            any = true;
            System.out.println(f + ": removing " + removed[0] + " stale registry entr"
                    + (removed[0] == 1 ? "y" : "ies"));
            if (!dry) {
                ByteArrayOutputStream bos = new ByteArrayOutputStream();
                DataOutputStream dos = new DataOutputStream(new GZIPOutputStream(bos));
                write(dos, root);
                dos.flush();
                dos.close(); // finishes the gzip stream and writes its trailer
                Files.write(p, bos.toByteArray());
                System.out.println(f + ": rewritten (" + raw.length + " -> "
                        + bos.size() + " bytes)");
            }
        }
        System.out.println(any ? "FIXED" : "NOTHING TO DO");
    }

    /** Removes {@link #DROP} keys from any compound named "Registries", anywhere in the tree. */
    private static void walk(Object node, int[] removed) {
        if (node instanceof Map<?, ?>) {
            @SuppressWarnings("unchecked") Map<String, Object> m = (Map<String, Object>) node;
            if (m.containsKey("Registries")) {
                Object r = m.get("Registries");
                if (r instanceof Map) {
                    @SuppressWarnings("unchecked") Map<String, Object> reg =
                            (Map<String, Object>) r;
                    for (String k : DROP)
                        if (reg.remove(k) != null) removed[0]++;
                }
            }
            for (Object v : m.values()) walk(v, removed);
        } else if (node instanceof List<?>) {
            for (Object v : (List<?>) node) walk(v, removed);
        }
    }

    // ------------------------------------------------------------------ minimal NBT

    private static Object read(DataInputStream in) throws IOException {
        int type = in.readUnsignedByte();
        if (type == 0) return null;
        in.readUTF(); // root name
        return readPayload(in, type);
    }

    private static Object readPayload(DataInputStream in, int type) throws IOException {
        switch (type) {
            case 1: return in.readByte();
            case 2: return in.readShort();
            case 3: return in.readInt();
            case 4: return in.readLong();
            case 5: return in.readFloat();
            case 6: return in.readDouble();
            case 7: {
                int n = in.readInt();
                byte[] b = new byte[n];
                in.readFully(b);
                return b;
            }
            case 8: return in.readUTF();
            case 9: {
                int itemType = in.readUnsignedByte();
                int n = in.readInt();
                List<Object> l = new ArrayList<>();
                for (int i = 0; i < n; i++)
                    l.add(itemType == 0 ? null : readPayload(in, itemType));
                return l;
            }
            case 10: {
                Map<String, Object> m = new LinkedHashMap<>();
                while (true) {
                    int t = in.readUnsignedByte();
                    if (t == 0) break;
                    String name = in.readUTF();
                    m.put(name, readPayload(in, t));
                }
                return m;
            }
            case 11: {
                int n = in.readInt();
                int[] a = new int[n];
                for (int i = 0; i < n; i++) a[i] = in.readInt();
                return a;
            }
            case 12: {
                int n = in.readInt();
                long[] a = new long[n];
                for (int i = 0; i < n; i++) a[i] = in.readLong();
                return a;
            }
            default: throw new IOException("unknown NBT tag " + type);
        }
    }

    private static int typeOf(Object o) {
        if (o instanceof Map) return 10;
        if (o instanceof List) return 9;
        if (o instanceof String) return 8;
        if (o instanceof Integer) return 3;
        if (o instanceof Long) return 4;
        if (o instanceof Byte) return 1;
        if (o instanceof Short) return 2;
        if (o instanceof Float) return 5;
        if (o instanceof Double) return 6;
        if (o instanceof int[]) return 11;
        if (o instanceof long[]) return 12;
        return 7;
    }

    private static void write(DataOutputStream out, Object node) throws IOException {
        if (!(node instanceof Map))
            throw new IOException("root must be a compound");
        out.writeByte(10);
        out.writeUTF("");
        writePayload(out, node);
    }

    private static void writePayload(DataOutputStream out, Object o) throws IOException {
        switch (typeOf(o)) {
            case 1: out.writeByte((Byte) o); break;
            case 2: out.writeShort((Short) o); break;
            case 3: out.writeInt((Integer) o); break;
            case 4: out.writeLong((Long) o); break;
            case 5: out.writeFloat((Float) o); break;
            case 6: out.writeDouble((Double) o); break;
            case 7: {
                byte[] b = (byte[]) o;
                out.writeInt(b.length);
                out.write(b);
                break;
            }
            case 8: out.writeUTF((String) o); break;
            case 9: {
                List<?> l = (List<?>) o;
                int it = l.isEmpty() ? 0 : typeOf(l.get(0));
                out.writeByte(it);
                out.writeInt(l.size());
                for (Object v : l) if (it != 0) writePayload(out, v);
                break;
            }
            case 10: {
                Map<?, ?> m = (Map<?, ?>) o;
                for (Map.Entry<?, ?> e : m.entrySet()) {
                    Object k = e.getKey();
                    if (!(k instanceof String)) continue;
                    Object v = e.getValue();
                    if (v == null) continue;
                    out.writeByte(typeOf(v));
                    out.writeUTF((String) k);
                    writePayload(out, v);
                }
                out.writeByte(0);
                break;
            }
            case 11: {
                int[] a = (int[]) o;
                out.writeInt(a.length);
                for (int v : a) out.writeInt(v);
                break;
            }
            case 12: {
                long[] a = (long[]) o;
                out.writeInt(a.length);
                for (long v : a) out.writeLong(v);
                break;
            }
            default: throw new IOException("unserialisable " + o);
        }
    }
}
