package view;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.SwingWorker;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;

/** Loads pictogram images without blocking Swing's event dispatch thread. */
final class PictogramIconLoader {
    private static final int ICON_SIZE = 80;
    private static final int TIMEOUT_MILLIS = 10_000;
    private final Map<String, ImageIcon> cache = new HashMap<>();

    /** Called on the event dispatch thread; labels remain usable without images. */
    void load(String location, JButton button) {
        ImageIcon cached = cache.get(location);
        if (cached != null) {
            button.setIcon(cached);
            return;
        }
        new SwingWorker<ImageIcon, Void>() {
            @Override
            protected ImageIcon doInBackground() throws IOException {
                return readIcon(location, ICON_SIZE);
            }

            @Override
            protected void done() {
                try {
                    ImageIcon icon = get();
                    cache.put(location, icon);
                    button.setIcon(icon);
                    button.setToolTipText(null);
                } catch (InterruptedException exception) {
                    Thread.currentThread().interrupt();
                } catch (ExecutionException exception) {
                    button.setToolTipText("Image unavailable. Check the connection or customize this tile.");
                    reportFailure(location, exception.getCause());
                }
            }
        }.execute();
    }

    /** Shared decoder for the main-board icons and caregiver thumbnails. */
    static ImageIcon readIcon(String location, int size) throws IOException {
        URL url;
        if (location.startsWith("https://")) {
            URI uri;
            try {
                uri = URI.create(location);
            } catch (IllegalArgumentException exception) {
                throw new IOException("Invalid pictogram URL", exception);
            }
            if (!"static.arasaac.org".equalsIgnoreCase(uri.getHost())) {
                throw new IOException("Expected an ARASAAC image URL");
            }
            url = uri.toURL();
        } else {
            String resource = location.startsWith("/") ? location : "/" + location;
            url = PictogramIconLoader.class.getResource(resource);
        }
        if (url == null) {
            throw new IOException("Image resource not found");
        }
        URLConnection connection = url.openConnection();
        connection.setConnectTimeout(TIMEOUT_MILLIS);
        connection.setReadTimeout(TIMEOUT_MILLIS);
        if (connection instanceof HttpURLConnection) {
            HttpURLConnection http = (HttpURLConnection) connection;
            http.setInstanceFollowRedirects(true);
            http.setRequestProperty("User-Agent", "AACorn/1.0 (educational AAC project)");
            http.setRequestProperty("Accept", "image/png,image/*;q=0.9");
        }
        try (InputStream stream = connection.getInputStream()) {
            BufferedImage image = ImageIO.read(stream);
            if (image == null) {
                throw new IOException("Response is not a supported image");
            }
            double scale = (double) size / Math.max(image.getWidth(), image.getHeight());
            int width = Math.max(1, (int) (image.getWidth() * scale));
            int height = Math.max(1, (int) (image.getHeight() * scale));
            return new ImageIcon(image.getScaledInstance(width, height, Image.SCALE_SMOOTH));
        } finally {
            if (connection instanceof HttpURLConnection) {
                ((HttpURLConnection) connection).disconnect();
            }
        }
    }

    static void reportFailure(String location, Throwable exception) {
        System.err.println("Pictogram image unavailable: " + location + " (" + exception + ")");
    }
}
