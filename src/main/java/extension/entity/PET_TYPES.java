package extension.entity;

import lombok.Getter;

@Getter
public enum PET_TYPES {
	HORSE(15),
	PLANT(16);
	
	private int petType;
	
	PET_TYPES(int petType) {
		this.petType = petType;
	}
}
