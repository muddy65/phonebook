import javax.swing.SwingWorker;
import java.awt.Component;
import java.awt.Cursor;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.function.Consumer;

/** Runs slow database work away from Swing's event thread. */
public final class BackgroundTask {
    private BackgroundTask() { }

    public static <T> void run(Component owner, Callable<T> work,
                               Consumer<T> completed, Consumer<Exception> failed) {
        Cursor previousCursor = owner == null ? null : owner.getCursor();
        if (owner != null) owner.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

        new SwingWorker<T, Void>() {
            @Override
            protected T doInBackground() throws Exception {
                return work.call();
            }

            @Override
            protected void done() {
                if (owner != null) owner.setCursor(previousCursor);
                try {
                    completed.accept(get());
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                    failed.accept(ex);
                } catch (ExecutionException ex) {
                    Throwable cause = ex.getCause();
                    failed.accept(cause instanceof Exception
                            ? (Exception) cause : new Exception(cause));
                }
            }
        }.execute();
    }
}
