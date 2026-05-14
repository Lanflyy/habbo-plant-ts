package extension.logging;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.AppenderBase;
import ch.qos.logback.core.Layout;
import lombok.Setter;

public class UiLogAppender extends AppenderBase<ILoggingEvent> {

    @Setter
    private Layout<ILoggingEvent> layout;

    @Override
    public void start() {
        if (layout == null) {
            addError("No layout set for " + getName());
            return;
        }
        super.start();
    }

    @Override
    protected void append(ILoggingEvent eventObject) {
        UiLogPrinter.write(layout.doLayout(eventObject));
    }
}
