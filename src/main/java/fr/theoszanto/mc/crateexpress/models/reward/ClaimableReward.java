package fr.theoszanto.mc.crateexpress.models.reward;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public record ClaimableReward(@NotNull String id, @NotNull CrateReward reward) {
	@Override
	@Contract(value = "null -> false", pure = true)
	public boolean equals(@Nullable Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		ClaimableReward reward = (ClaimableReward) o;
		return id.equals(reward.id);
	}
}
