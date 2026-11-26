package view;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.SwingWorker;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

/** Lazily loads thumbnails for visible pictogram rows, off the Swing event thread. */
final class PictogramThumbnails {
    private static final int SIZE = 36;

    // A null entry means a thumbnail is loading or could not be loaded.
    private final Map<String, ImageIcon> images = new HashMap<>();
    private final JComponent view;

    PictogramThumbnails(JComponent view) {
        this.view = view;
    }

    ImageIcon get(String location) {
        if (!images.containsKey(location)) {
            images.put(location, null);
            new SwingWorker<ImageIcon, Void>() {
                @Override
                protected ImageIcon doInBackground() {
                    return load(location);
                }

                @Override
                protected void done() {
                    try {
                        ImageIcon thumbnail = get();
                        images.put(location, thumbnail);
                        view.repaint();
                    } catch (InterruptedException exception) {
                        Thread.currentThread().interrupt();
                    } catch (java.util.concurrent.ExecutionException exception) {
                        // Keep the tile's text when no thumbnail is available.
                    }
                }
            }.execute();
        }
        return images.get(location);
    }

    private static ImageIcon load(String location) {
        try {
            URI uri = URI.create(location);
            if (!"https".equals(uri.getScheme()) || !"static.arasaac.org".equals(uri.getHost())
                    || !uri.getPath().matches("/pictograms/[0-9]+/[0-9]+_500\\.png")) {
                return null;
            }
            URL url = uri.toURL();
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setInstanceFollowRedirects(false);
            connection.setConnectTimeout(4_000);
            connection.setReadTimeout(4_000);
            try (var stream = connection.getInputStream()) {
                BufferedImage image = ImageIO.read(stream);
                if (image == null) {
                    return null;
                }
                double scale = (double) SIZE / Math.max(image.getWidth(), image.getHeight());
                int width = Math.max(1, (int) (image.getWidth() * scale));
                int height = Math.max(1, (int) (image.getHeight() * scale));
                return new ImageIcon(image.getScaledInstance(width, height, Image.SCALE_SMOOTH));
            } finally {
                connection.disconnect();
            }
        } catch (IOException | IllegalArgumentException exception) {
            return null;
        }
    }
}
