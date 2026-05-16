package extension.util;

/**
 * Simple runtime settings holder for the Plants extension.
 * Settings default values can be changed here; UI updates them at runtime.
 */
public final class PlantSettings {
    private PlantSettings() {}

    private static volatile boolean REQUEST_PET_INFO_BEFORE_TREAT = true;

    public static boolean isRequestPetInfoBeforeTreat() {
        return REQUEST_PET_INFO_BEFORE_TREAT;
    }

    public static void setRequestPetInfoBeforeTreat(boolean v) {
        REQUEST_PET_INFO_BEFORE_TREAT = v;
    }
}
