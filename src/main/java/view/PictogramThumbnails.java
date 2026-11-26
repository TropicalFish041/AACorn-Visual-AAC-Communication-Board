package view;

import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.SwingWorker;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;

/** Lazily loads thumbnails for visible pictogram rows, off the Swing event thread. */
final class PictogramThumbnails {
    private static final int SIZE = 36;
    private static final long RETRY_MILLIS = 15_000;
    private final Map<String, ImageIcon> images = new HashMap<>();
    private final Set<String> loading = new HashSet<>();
    private final Map<String, Long> retryAfter = new HashMap<>();
    private final JComponent view;

    PictogramThumbnails(JComponent view) {
        this.view = view;
    }

    ImageIcon get(String location) {
        ImageIcon cached = images.get(location);
        if (cached != null) {
            return cached;
        }
        if (loading.contains(location) || System.currentTimeMillis() < retryAfter.getOrDefault(location, 0L)) {
            return null;
        }
        loading.add(location);
        new SwingWorker<ImageIcon, Void>() {
            @Override
            protected ImageIcon doInBackground() throws java.io.IOException {
                return PictogramIconLoader.readIcon(location, SIZE);
            }

            @Override
            protected void done() {
                try {
                    images.put(location, get());
                    retryAfter.remove(location);
                    view.repaint();
                } catch (InterruptedException exception) {
                    Thread.currentThread().interrupt();
                    retryAfter.put(location, System.currentTimeMillis() + RETRY_MILLIS);
                } catch (ExecutionException exception) {
                    retryAfter.put(location, System.currentTimeMillis() + RETRY_MILLIS);
                    PictogramIconLoader.reportFailure(location, exception.getCause());
                } finally {
                    loading.remove(location);
                }
            }
        }.execute();
        return null;
    }
}
