package extension.entity;

import gearth.protocol.HPacket;

/**
 * Parses the server-side `PetInfo` packet into a typed Java object.
 *
 * Inferred field order (from observed hex dumps):
 * 1. int   petId
 * 2. String name
 * 3. int   level
 * 4. int   maxLevel
 * 5. int   experience
 * 6. int   maxExperience
 * 7. int   energy
 * 8. int   maxEnergy
 * 9. int   nutrition (happiness)
 * 10. int  maxNutrition
 * 11. int  scratchNumber (or age/index)
 * 12. int  ownerId
 * 13. int  ownerIndex (an index usually just before owner details)
 * 14. String ownerName
 * 15. int  unk1
 * 16. int  unk2
 * 17. int  unk3
 * 18. int  unk4
 * 19. boolean unkBoolA
 * 20. int  rarity
 * 21. int  maxWellbeingSeconds
 * 22. int  currentWellbeingSeconds
 * 23. int  unk5
 * 24. boolean unkBoolB
 *
 * Notes:
 * - The mapping above is inferred from your dumps and from common Habbo pet/plant packet layouts.
 * - Confirmed mappings: `petId`, `ownerId`, `maxWellbeingSeconds`, `currentWellbeingSeconds`,
 *   `level`/`maxLevel`, and `rarity`.
 * - The following user-provided items were NOT found explicitly in the dumps and therefore
 *   are NOT assigned to a separate field here: `shape`, `color`, and an explicit `experienceRequired = -1`.
 * - Field names marked `unk*` are present in the packet but their semantic meaning is uncertain
 *   from the available data; they are preserved so downstream code can inspect them.
 */
public class PetInfoEntity {

    private final int petId;
    private final String name;

    private final int level;
    private final int maxLevel;

    private final int experience;
    private final int maxExperience;

    private final int energy;
    private final int maxEnergy;

    private final int nutrition;
    private final int maxNutrition;

    private final int scratchNumber;

    private final int ownerId;
    private final int ownerIndex;
    private final String ownerName;

    private final int unk1;
    private final int unk2;
    private final int unk3;
    private final int unk4;

    private final boolean unkBoolA;

    private final int rarity;

    private final int maxWellbeingSeconds;
    private final int currentWellbeingSeconds;

    private final int unk5;
    private final boolean unkBoolB;

    public PetInfoEntity(HPacket packet) {
        // Read in the observed order
        this.petId = packet.readInteger();
        this.name = packet.readString();

        this.level = packet.readInteger();
        this.maxLevel = packet.readInteger();

        this.experience = packet.readInteger();
        this.maxExperience = packet.readInteger();

        this.energy = packet.readInteger();
        this.maxEnergy = packet.readInteger();

        this.nutrition = packet.readInteger();
        this.maxNutrition = packet.readInteger();

        this.scratchNumber = packet.readInteger();

        this.ownerId = packet.readInteger();
        this.ownerIndex = packet.readInteger();
        this.ownerName = packet.readString();

        this.unk1 = packet.readInteger();
        this.unk2 = packet.readInteger();
        this.unk3 = packet.readInteger();
        this.unk4 = packet.readInteger();

        this.unkBoolA = packet.readBoolean();

        this.rarity = packet.readInteger();

        this.maxWellbeingSeconds = packet.readInteger();
        this.currentWellbeingSeconds = packet.readInteger();

        this.unk5 = packet.readInteger();
        this.unkBoolB = packet.readBoolean();
    }

    public static PetInfoEntity fromPacket(HPacket packet) {
        return new PetInfoEntity(packet);
    }

    public int getPetId() { return petId; }
    public String getName() { return name; }

    public int getLevel() { return level; }
    public int getMaxLevel() { return maxLevel; }

    public int getExperience() { return experience; }
    public int getMaxExperience() { return maxExperience; }

    public int getEnergy() { return energy; }
    public int getMaxEnergy() { return maxEnergy; }

    public int getNutrition() { return nutrition; }
    public int getMaxNutrition() { return maxNutrition; }

    public int getScratchNumber() { return scratchNumber; }

    public int getOwnerId() { return ownerId; }
    public int getOwnerIndex() { return ownerIndex; }
    public String getOwnerName() { return ownerName; }

    public int getUnk1() { return unk1; }
    public int getUnk2() { return unk2; }
    public int getUnk3() { return unk3; }
    public int getUnk4() { return unk4; }

    public boolean isUnkBoolA() { return unkBoolA; }

    public int getRarity() { return rarity; }

    public int getMaxWellbeingSeconds() { return maxWellbeingSeconds; }
    public int getCurrentWellbeingSeconds() { return currentWellbeingSeconds; }

    public int getUnk5() { return unk5; }
    public boolean isUnkBoolB() { return unkBoolB; }

    @Override
    public String toString() {
        return "PetInfoEntity{" +
                "petId=" + petId +
                ", name='" + name + '\'' +
                ", level=" + level +
                ", maxLevel=" + maxLevel +
                ", experience=" + experience +
                ", maxExperience=" + maxExperience +
                ", energy=" + energy +
                ", maxEnergy=" + maxEnergy +
                ", nutrition=" + nutrition +
                ", maxNutrition=" + maxNutrition +
                ", scratchNumber=" + scratchNumber +
                ", ownerId=" + ownerId +
                ", ownerIndex=" + ownerIndex +
                ", ownerName='" + ownerName + '\'' +
                ", rarity=" + rarity +
                ", maxWellbeingSeconds=" + maxWellbeingSeconds +
                ", currentWellbeingSeconds=" + currentWellbeingSeconds +
                '}';
    }
}
