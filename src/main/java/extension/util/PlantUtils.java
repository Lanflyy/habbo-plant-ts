package extension.util;

import extension.entity.PET_TYPES;
import gearth.extensions.parsers.HEntity;
import gearth.extensions.parsers.HEntityType;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class PlantUtils {
    private PlantUtils() {
        // Utility class, prevent instantiation
    }
    
    public static boolean isPlant(HEntity entity) {
    	if(entity.getEntityType() != HEntityType.PET) {
    		return false;
    	}
    	Object[] stuff = entity.getStuff();
    	if(stuff[0] == PET_TYPES.PLANT) {
    		return true;
    	}
    	return false;
    }
    
    public static boolean isDeadPlant(HEntity entity) {
    	if(!isPlant(entity)) {
    		log.error("Checking if dead plant, but it is not even a plant !");
    		return true;
    	}
        Object[] stuff = entity.getStuff();
        if(stuff.length <= 8) {
        	log.error("Checking if dead plant, but stuff length is {}, expected at least 9", stuff.length);
        	return true;
        }
        return stuff.length > 8 && Boolean.TRUE.equals(stuff[8]);
    }
}
