package fr.theoszanto.mc.crateexpress.models;

import fr.theoszanto.mc.crateexpress.models.reward.CrateReward;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Date;
import java.util.List;

public record StatsRecord(@NotNull Date date, @NotNull Player player, @NotNull Crate crate, @NotNull List<@NotNull CrateReward> rewards) {}
