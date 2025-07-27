package fr.theoszanto.mc.crateexpress.models;

import org.jetbrains.annotations.NotNull;

import java.util.Date;
import java.util.List;
import java.util.UUID;

public record RawStatsRecord(@NotNull Date date, @NotNull UUID player, @NotNull String crate, @NotNull List<@NotNull String> rewards) {}
