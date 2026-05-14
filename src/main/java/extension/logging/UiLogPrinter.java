package extension.logging;

import javafx.application.Platform;
import javafx.scene.control.TextArea;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class UiLogPrinter {

    private static final Queue<String> pendingLines = new ConcurrentLinkedQueue<>();
    private static final AtomicBoolean flushScheduled = new AtomicBoolean(false);
    private static volatile TextArea output;

    public static void attach(TextArea logArea) {
        output = logArea;
        scheduleFlush();
    }

    public static void write(String line) {
        if (line == null || line.isEmpty()) {
            return;
        }
        pendingLines.add(line);
        scheduleFlush();
    }

    private static void scheduleFlush() {
        if (output == null || !flushScheduled.compareAndSet(false, true)) {
            return;
        }
        Platform.runLater(UiLogPrinter::flush);
    }

    private static void flush() {
        try {
            TextArea logArea = output;
            if (logArea != null) {
                StringBuilder text = new StringBuilder();
                String line;
                while ((line = pendingLines.poll()) != null) {
                    text.append(line);
                }
                if (text.length() > 0) {
                    logArea.appendText(text.toString());
                }
            }
        } finally {
            flushScheduled.set(false);
            if (output != null && !pendingLines.isEmpty()) {
                scheduleFlush();
            }
        }
    }
}
