package extension.features;

import gearth.extensions.ExtensionForm;
import gearth.extensions.parsers.HEntity;
import extension.util.NotificationUtils;

public interface PlantProcessingHandler {
    boolean shouldProcess(HEntity plant);
    boolean process(HEntity plant);
    default void onFinished(int processedCount) {}

    default void showSystemNotification(ExtensionForm extension, PlantManagerFeature.ActionCommandType actionType, int processedCount) {
        NotificationUtils.showSystemNotificationToUser(extension, "All plants have been " + actionType.getVerb() + " (" + processedCount + ")");
    }
}
