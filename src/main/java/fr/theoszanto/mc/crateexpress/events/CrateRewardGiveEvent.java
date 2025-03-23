package fr.theoszanto.mc.crateexpress.events;

import fr.theoszanto.mc.crateexpress.models.reward.CrateReward;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class CrateRewardGiveEvent extends Event implements Cancellable {
	private final @NotNull Player player;
	private @NotNull CrateReward reward;
	private boolean savingReward;
	private boolean cancelled = false;

	private static final @NotNull HandlerList handlers = new HandlerList();

	public CrateRewardGiveEvent(@NotNull Player player, @NotNull CrateReward reward, boolean savingReward) {
		this.player = player;
		this.reward = reward;
		this.savingReward = savingReward;
	}

	public @NotNull Player getPlayer() {
		return this.player;
	}

	public @NotNull CrateReward getReward() {
		return this.reward;
	}

	public void setReward(@NotNull CrateReward reward) {
		this.reward = reward;
		this.savingReward = reward.cannotGiveTo(this.player);
	}

	public boolean isSavingReward() {
		return this.savingReward;
	}

	@Override
	public boolean isCancelled() {
		return this.cancelled;
	}

	@Override
	public void setCancelled(boolean cancel) {
		this.cancelled = cancel;
	}

	@Override
	public @NotNull HandlerList getHandlers() {
		return handlers;
	}

	public static @NotNull HandlerList getHandlerList() {
		return handlers;
	}
}
