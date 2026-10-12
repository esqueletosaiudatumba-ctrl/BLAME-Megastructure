import javax.imageio.ImageIO;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

/** Builds a labeled overview sheet from the PNG previews emitted by Preview.java. */
public final class ContactSheet {
    private static final int COLS = 4;
    private static final int CELL_W = 320;
    private static final int CELL_H = 220;
    private static final int PAD = 10;
    private static final int IMAGE_W = CELL_W - 2 * PAD;
    private static final int IMAGE_H = 174;

    private ContactSheet() {}

    public static void main(String[] args) throws Exception {
        if (args.length < 2) {
            throw new IllegalArgumentException("Usage: ContactSheet OUT.png INPUT.png...");
        }
        File output = new File(args[0]);
        List<File> inputs = new ArrayList<File>();
        for (int i = 1; i < args.length; i++) {
            File file = new File(args[i]);
            if (file.isFile() && !file.getCanonicalFile().equals(output.getCanonicalFile())) {
                inputs.add(file);
            }
        }
        inputs.sort((a, b) -> a.getName().compareToIgnoreCase(b.getName()));
        List<BufferedImage> images = new ArrayList<BufferedImage>();
        List<String> labels = new ArrayList<String>();
        for (File input : inputs) {
            BufferedImage image = ImageIO.read(input);
            if (image == null) continue;
            images.add(image);
            labels.add(input.getName().replace(".png", "").replace('-', ' '));
        }
        if (images.isEmpty()) throw new IllegalStateException("No readable preview PNGs found");

        int rows = (images.size() + COLS - 1) / COLS;
        BufferedImage sheet = new BufferedImage(COLS * CELL_W, rows * CELL_H,
                BufferedImage.TYPE_INT_RGB);
        Graphics2D g = sheet.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g.setColor(new Color(13, 15, 20));
        g.fillRect(0, 0, sheet.getWidth(), sheet.getHeight());
        g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 14));
        for (int i = 0; i < images.size(); i++) {
            int x = (i % COLS) * CELL_W;
            int y = (i / COLS) * CELL_H;
            g.setColor(new Color(27, 30, 37));
            g.fillRoundRect(x + 4, y + 4, CELL_W - 8, CELL_H - 8, 10, 10);
            BufferedImage image = images.get(i);
            double factor = Math.min((double) IMAGE_W / image.getWidth(),
                    (double) IMAGE_H / image.getHeight());
            int w = Math.max(1, (int) Math.round(image.getWidth() * factor));
            int h = Math.max(1, (int) Math.round(image.getHeight() * factor));
            int ix = x + (CELL_W - w) / 2;
            int iy = y + PAD + (IMAGE_H - h) / 2;
            g.setColor(Color.BLACK);
            g.fillRect(ix - 1, iy - 1, w + 2, h + 2);
            g.drawImage(image, ix, iy, w, h, null);
            g.setColor(new Color(224, 227, 233));
            String label = labels.get(i);
            if (label.length() > 39) label = label.substring(0, 36) + "...";
            g.drawString(label, x + PAD, y + CELL_H - 22);
        }
        g.dispose();
        ImageIO.write(sheet, "png", output);
        System.out.println("wrote " + output + " (" + images.size() + " previews, "
                + sheet.getWidth() + "x" + sheet.getHeight() + ")");
    }
}
