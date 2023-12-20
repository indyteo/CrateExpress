package fr.theoszanto.mc.crateexpress.managers;

import fr.theoszanto.mc.crateexpress.CrateExpress;
import fr.theoszanto.mc.crateexpress.models.Crate;
import fr.theoszanto.mc.crateexpress.models.StatsRecord;
import fr.theoszanto.mc.crateexpress.utils.PluginObject;
import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

public class StatsManager extends PluginObject {
	private final @NotNull Queue<@NotNull StatsRecord> pendingStats = new LinkedList<>();
	private @Nullable BukkitTask saveTask;

	public StatsManager(@NotNull CrateExpress plugin) {
		super(plugin);
	}

	public void load() {
		this.saveTask = Bukkit.getScheduler().runTaskTimerAsynchronously(this.plugin, this::savePendingStats, 1200, 1200); // Runs every minute
	}

	public void reset() {
		this.savePendingStats();
		this.pendingStats.clear();
		if (this.saveTask != null) {
			this.saveTask.cancel();
			this.saveTask = null;
		}
	}

	public int getTimesOpened(@NotNull Crate crate) {
		this.savePendingStats();
		return this.storage().getSource().getOpenStats(crate);
	}

	public void recordStats(@NotNull StatsRecord stats) {
		this.pendingStats.offer(stats);
	}

	public void savePendingStats() {
		if (this.pendingStats.isEmpty())
			return;
		List<StatsRecord> stats = new ArrayList<>(this.pendingStats.size() + 16);
		while (!this.pendingStats.isEmpty())
			stats.add(this.pendingStats.poll());
		this.storage().getSource().updateStats(stats);
	}
}
