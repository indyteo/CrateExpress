package fr.theoszanto.mc.crateexpress.managers;

import fr.theoszanto.mc.crateexpress.CrateExpress;
import fr.theoszanto.mc.crateexpress.models.Crate;
import fr.theoszanto.mc.crateexpress.models.StatsRecord;
import fr.theoszanto.mc.crateexpress.models.reward.HistoricalReward;
import fr.theoszanto.mc.crateexpress.utils.Pair;
import fr.theoszanto.mc.crateexpress.utils.PluginObject;
import fr.theoszanto.mc.crateexpress.utils.TimeUtils;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.TreeMap;
import java.util.UUID;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

public class StatsManager extends PluginObject {
	private final @NotNull Queue<@NotNull StatsRecord> pendingStats = new LinkedList<>();
	private @Nullable BukkitTask saveTask;
	private final @NotNull Map<@NotNull Pair<@NotNull UUID, @NotNull Date>, @NotNull Map<@NotNull Crate, @NotNull List<@NotNull HistoricalReward>>> historyCache = new HashMap<>();

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
		this.historyCache.clear();
	}

	public int getTimesOpened(@NotNull Crate crate) {
		this.savePendingStats();
		return this.storage().getSource().getOpenStats(crate);
	}

	public void recordStats(@NotNull StatsRecord stats) {
		this.pendingStats.offer(stats);
		this.historyCache.remove(new Pair<>(stats.getPlayer().getUniqueId(), TimeUtils.cloneWithoutTime(stats.getDate())));
	}

	public void savePendingStats() {
		if (this.pendingStats.isEmpty())
			return;
		List<StatsRecord> stats = new ArrayList<>(this.pendingStats.size() + 16);
		while (!this.pendingStats.isEmpty())
			stats.add(this.pendingStats.poll());
		this.storage().getSource().updateStats(stats);
	}

	public @NotNull CompletableFuture<@NotNull Map<@NotNull Crate, @NotNull List<@NotNull HistoricalReward>>> loadHistory(@NotNull OfflinePlayer player, @NotNull Date date) {
		TimeUtils.removeTime(date);
		Pair<UUID, Date> cacheKey = new Pair<>(player.getUniqueId(), date);
		if (this.historyCache.containsKey(cacheKey))
			return CompletableFuture.completedFuture(this.historyCache.get(cacheKey));
		else {
			CompletableFuture<Map<Crate, List<HistoricalReward>>> future = new CompletableFuture<>();
			AtomicBoolean runComplete = new AtomicBoolean(false);
			BukkitTask task = Bukkit.getScheduler().runTaskAsynchronously(this.plugin, () -> {
				try {
					Map<Crate, List<HistoricalReward>> history = new HashMap<>();
					for (HistoricalReward reward : this.storage().getSource().listHistory(player, date))
						history.computeIfAbsent(reward.getCrate(), k -> new ArrayList<>()).add(reward);
					for (StatsRecord pending : this.pendingStats)
						if (pending.getPlayer().getUniqueId().equals(player.getUniqueId()))
							history.computeIfAbsent(pending.getCrate(), k -> new ArrayList<>()).addAll(pending.getRewards().stream()
									.map(reward -> new HistoricalReward(pending.getDate(), pending.getCrate(), reward))
									.collect(Collectors.toList()));
					history.values().forEach(Collections::sort);
					Map<Crate, Date> lastOpened = history.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, entry -> entry.getValue().get(0).getDate()));
					Map<Crate, List<HistoricalReward>> sortedHistory = new TreeMap<>(Comparator.comparing(lastOpened::get).reversed());
					sortedHistory.putAll(history);
					this.historyCache.put(cacheKey, sortedHistory);
					Bukkit.getScheduler().runTask(this.plugin, () -> future.complete(sortedHistory));
				} catch (Throwable e) {
					this.error("Error loading player history: " + player.getName(), e);
					future.completeExceptionally(e);
				} finally {
					runComplete.set(true);
				}
			});
			return future.whenComplete((history, exception) -> {
				if (exception instanceof CancellationException && !runComplete.get() && !task.isCancelled())
					task.cancel();
			});
		}
	}
}
