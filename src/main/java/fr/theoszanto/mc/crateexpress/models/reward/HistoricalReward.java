package fr.theoszanto.mc.crateexpress.models.reward;

import fr.theoszanto.mc.crateexpress.models.Crate;
import org.jetbrains.annotations.NotNull;

import java.util.Date;

public record HistoricalReward(@NotNull Date date, @NotNull Crate crate, @NotNull CrateReward reward) implements Comparable<HistoricalReward> {
	@Override
	public int compareTo(@NotNull HistoricalReward o) {
		// Reverse natural date order (from most recent to most ancient)
		return o.date.compareTo(this.date);
	}
}
