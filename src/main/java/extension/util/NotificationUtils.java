package extension.util;

import gearth.extensions.ExtensionForm;
import gearth.protocol.HMessage;
import gearth.protocol.HPacket;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class NotificationUtils {
	
	private NotificationUtils() {
		// Utility class, prevent instantiation
	}
	
	public static boolean showSystemNotificationToUser(ExtensionForm extension, String message) {
        boolean sent = extension.sendToClient(new HPacket("Whisper", HMessage.Direction.TOCLIENT, -1, message, 0, 30, 0, -1));
        log.debug("[Whisper] {} ({})", message, sent ? "sent" : "failed");
        return sent;
    }
}
