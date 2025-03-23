package fr.theoszanto.mc.crateexpress.models.reward;

public interface CrateRandomReward {
	boolean isRandom();

	CrateReward fixed();
}
