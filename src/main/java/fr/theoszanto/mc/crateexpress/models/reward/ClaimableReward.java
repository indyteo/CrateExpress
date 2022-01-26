package fr.theoszanto.mc.crateexpress.models.reward;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ClaimableReward {
	private final @NotNull String id;
	private final @NotNull CrateReward reward;

	public ClaimableReward(@NotNull String id, @NotNull CrateReward reward) {
		this.id = id;
		this.reward = reward;
	}

	public @NotNull String getId() {
		return this.id;
	}

	public @NotNull CrateReward getReward() {
		return this.reward;
	}

	@Override
	@Contract(value = "null -> false", pure = true)
	public boolean equals(@Nullable Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		ClaimableReward reward = (ClaimableReward) o;
		return id.equals(reward.id);
	}

	@Override
	public int hashCode() {
		return id.hashCode();
	}
}
