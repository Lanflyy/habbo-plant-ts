package extension.features;

import gearth.extensions.ExtensionForm;
import gearth.extensions.parsers.HEntity;
import gearth.extensions.parsers.HEntityType;
import gearth.protocol.HMessage;
import gearth.protocol.HPacket;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import extension.util.NotificationUtils;
import extension.util.PlantUtils;

@Slf4j
@RequiredArgsConstructor
public final class PlantManagerFeature {

    private static final String TREAT_COMMAND = ":plants";
    private static final String COMPOST_COMMAND = ":plants compost";
    private static final String ABORT_COMMAND = ":plants abort";
    private static final int PROCESS_DELAY_MS = 600;

    private final ExtensionForm extension;
    private final Map<Integer, HEntity> plants = new ConcurrentHashMap<>();
    private volatile boolean processing;
    private volatile boolean initialized;
    private volatile int currentRoomId = -1;

    public void install() {
        extension.intercept(HMessage.Direction.TOCLIENT, "Users", this::handleUsers);
        extension.intercept(HMessage.Direction.TOCLIENT, "UserRemove", this::handleUserRemove);
        extension.intercept(HMessage.Direction.TOSERVER, "Chat", this::handleChat);
        extension.intercept(HMessage.Direction.TOSERVER, "GetGuestRoom", this::handleGetGuestRoom);
        extension.intercept(HMessage.Direction.TOSERVER, "Quit", this::handleQuit);
        extension.intercept(HMessage.Direction.TOSERVER, "RemovePetFromFlat", this::handleRemovePetFromFlat);
        log.info("[Plants] Extension installed");
    }

    public void reset() {
        plants.clear();
        processing = false;
        initialized = false;
        currentRoomId = -1;
        log.info("[Plants] Extension reset, clearing everything..");
    }

    private void handleUsers(HMessage hMessage) {
        HPacket packet = hMessage.getPacket();
        try {
            packet.resetReadIndex();
            HEntity[] entities = HEntity.parse(packet);
            int addedCount = 0;

            // Only clear plants if this is a full room user list (not a single user/plant update)
            if (entities.length > 1) {
                log.trace("[Users] Detected full room user list ({} entities). Clearing plants. Plants before clear: {}", entities.length, plants.keySet());
                plants.clear();
            }

            log.trace("[Users] handleUsers called. Current plants before update: {}", plants.keySet());

            for (HEntity entity : entities) {
            	if (PlantUtils.isPlant(entity)) {
                    plants.put(entity.getId(), entity);
                    addedCount++;
                }
            }

            log.trace("[Users] handleUsers after update. Plants now: {}", plants.keySet());

            initialized = true;
            log.debug("[Users] Parsed {} entities. Added {} pets. Plants in memory: {}", entities.length, addedCount, plants.size());
        } catch (Exception e) {
            log.debug("[Users] Failed to parse room entities", e);
        }
    }

    private void handleChat(HMessage hMessage) {
        HPacket packet = hMessage.getPacket();
        packet.resetReadIndex();
        String text = packet.readString();

        if (!isCommand(text)) {
            return;
        }

        hMessage.setBlocked(true);
        log.debug("[Chat] Command received: {}", text);

        if (!initialized) {
        	NotificationUtils.showSystemNotificationToUser(extension, "Error: Room not initialized yet! Please wait for the room to load or reload it.");
            log.debug("[Chat] Command rejected because the room is not initialized");
            return;
        }

        if (TREAT_COMMAND.equals(text)) {
            processing = true;
            long livingCount = plants.values().stream().filter(plant -> !PlantUtils.isDeadPlant(plant)).count();
            long deadCount = plants.size() - livingCount;
            StringBuilder msg = new StringBuilder("Treating plants started... (");
            msg.append(livingCount).append(" living");
            if (deadCount > 0) {
                msg.append(", ").append(deadCount).append(" dead ignored)");
            } else {
                msg.append(")");
            }
            NotificationUtils.showSystemNotificationToUser(extension, msg.toString());
            log.debug("[Plants] Treat command started. Plants in memory: {}", plants.size());
            processPlants(ActionCommandType.TREAT);
        } else if (COMPOST_COMMAND.equals(text)) {
            processing = true;
            NotificationUtils.showSystemNotificationToUser(extension, "Composting plants started... (Found " + plants.size() + " plants)");
            log.debug("[Plants] Compost command started. Plants in memory: {}", plants.size());
            processPlants(ActionCommandType.COMPOST);
        } else if (ABORT_COMMAND.equals(text)) {
            processing = false;
            log.debug("[Plants] Abort command executed");
        }
    }

    private boolean isCommand(String text) {
        return TREAT_COMMAND.equals(text) || COMPOST_COMMAND.equals(text) || ABORT_COMMAND.equals(text);
    }

    private void processPlants(ActionCommandType actionType) {
        Thread processThread = new Thread(() -> {
            int count = 0;

            for (Map.Entry<Integer, HEntity> plantEntry : plants.entrySet()) {
                if (!processing) {
                    break;
                }

                HEntity plant = plantEntry.getValue();
                boolean processed = false;
                if(actionType == ActionCommandType.COMPOST) {
                	processed = processCompostPlant(plant);
                	if(processed) {
                		count++;
                	}
                	sleep();
                } else if (actionType == ActionCommandType.TREAT) {
                	processed = processTreatPlant(plant);
					if(processed) {
                		count++;
                	}
					sleep();
				} else {
					log.error("Unknown Action "+actionType);
					continue;
				}
            }

            if (processing) {
                NotificationUtils.showSystemNotificationToUser(extension, "All plants have been " + actionType.getVerb() + " (" + count + ")");
                log.debug("[Plants] Finished. {} plants {}", count, actionType.getVerb());
                processing = false;
            } else {
            	NotificationUtils.showSystemNotificationToUser(extension, "Plant processing aborted.");
                log.debug("[Plants] Processing aborted");
            }
        }, "plants-processor");
        processThread.setDaemon(true);
        processThread.start();
    }
    
    private boolean processCompostPlant(HEntity plant) {
    	if(!PlantUtils.isDeadPlant(plant)) {
    		// Cannot compost living plant. Just exit
    		return false;
    	}
    	String packetHeader = "CompostPlant";
    	boolean sent = extension.sendToServer(new HPacket(packetHeader, HMessage.Direction.TOSERVER, plant.getId()));
    	log.debug("[{}] Plant {} {}", packetHeader, plant.getId(), sent ? "sent" : "failed");
        if (sent) {
            plants.remove(plant.getId());
            return true;
        }
        return false;
    }
    
    private boolean processTreatPlant(HEntity plant) {
    	if(PlantUtils.isDeadPlant(plant)) {
    		// Cannot treat dead plant. Just exit
    		return false;
    	}
    	String packetHeader = "RespectPet";
    	boolean sent = extension.sendToServer(new HPacket(packetHeader, HMessage.Direction.TOSERVER, plant.getId()));
    	log.debug("[{}] Plant {} {}", packetHeader, plant.getId(), sent ? "sent" : "failed");
        if (sent) {
            plants.remove(plant.getId()); //TODO check this part
            return true;
        }
        return false;
    }

    private void sleep() {
        try {
            Thread.sleep(PROCESS_DELAY_MS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.debug("[Plants] Processing sleep interrupted", e);
        }
    }

    private void handleGetGuestRoom(HMessage hMessage) {
        HPacket packet = hMessage.getPacket();
        packet.resetReadIndex();
        int roomId = packet.readInteger();
        int requestType = packet.readInteger();

        if (requestType == 0) {
            log.trace("[GetGuestRoom] Room {} requested. Clearing plants. Plants before clear: {}", roomId, plants.keySet());
            plants.clear();
            processing = false;
            initialized = false;
            currentRoomId = roomId;
            log.debug("[GetGuestRoom] Room {} requested. Plants cleared and initialization reset", roomId);
        } else {
            log.debug("[GetGuestRoom] Background room request ignored. Room: {}, type: {}", roomId, requestType);
        }
    }

    private void handleQuit(HMessage hMessage) {
        log.trace("[Quit] Room quit. Clearing plants. Plants before clear: {}", plants.keySet());
        plants.clear();
        processing = false;
        initialized = false;
        log.debug("[Quit] Room state cleared");
    }

    @Deprecated()
    private void handleRemovePetFromFlat(HMessage hMessage) {
        HPacket packet = hMessage.getPacket();
        packet.resetReadIndex();
        int petId = packet.readInteger();

        if (plants.remove(petId) != null) {
            log.debug("[RemovePetFromFlat] Plant {} removed. Plants left: {}", petId, plants.size());
        } else {
            log.debug("[RemovePetFromFlat] Pet {} removed but was not tracked", petId);
        }
    }

    private void handleUserRemove(HMessage hMessage) {
        HPacket packet = hMessage.getPacket();
        packet.resetReadIndex();

        try {
            String idStr = packet.readString();
            int enityIndexId = Integer.parseInt(idStr);
            
            List<HEntity> foundPlants = plants.values()
            		.stream()
            		.filter(plant -> enityIndexId == plant.getIndex())
            		.collect(Collectors.toList());
            
            if(foundPlants.size() > 1) {
            	// avoid anything incautious, not doing anything
            	log.error("More than one index found for the removed entity, not removing it from memory");
            	return;
            }
            if (foundPlants.size() == 0) {
            	log.debug("[UserRemove] User {} removed but was not tracked as a plant", enityIndexId);
            	return;
            }
            HEntity uniqueFoundPlant = foundPlants.get(0);
            plants.remove(uniqueFoundPlant.getId());
            log.debug("[UserRemove] Plant {} removed (via UserRemove). Plants left: {}", enityIndexId, plants.size());
        } catch (Exception e) {
            log.debug("[UserRemove] Failed to parse UserRemove packet or remove plant", e);
        }
    }

    @Getter
    private enum ActionCommandType {
        TREAT("treated"),
        COMPOST("composted"),
        ABORT("aborted");
        
        private String verb;
        private ActionCommandType(String verb) {
        	this.verb = verb;
    	}
    }
}
