package fr.theoszanto.mc.crateexpress.events;

import fr.theoszanto.mc.crateexpress.models.Crate;
import fr.theoszanto.mc.crateexpress.models.reward.CrateReward;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class CrateOpenEvent extends CancellableCrateEvent {
	private final @NotNull Player player;
	private final @NotNull List<@NotNull CrateReward> rewards;

	private static final @NotNull HandlerList handlers = new HandlerList();

	public CrateOpenEvent(@NotNull Crate crate, @NotNull Player player, @NotNull List<@NotNull CrateReward> rewards) {
		super(crate);
		this.player = player;
		this.rewards = rewards;
	}

	public @NotNull Player getPlayer() {
		return this.player;
	}

	public @NotNull List<@NotNull CrateReward> getRewards() {
		return this.rewards;
	}

	@Override
	public @NotNull HandlerList getHandlers() {
		return handlers;
	}

	public static @NotNull HandlerList getHandlerList() {
		return handlers;
	}
}
