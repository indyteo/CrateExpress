package fr.theoszanto.mc.crateexpress.events.interaction;

import fr.theoszanto.mc.crateexpress.events.CrateInteractEvent;
import fr.theoszanto.mc.crateexpress.models.Crate;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public class CrateOpenInteractEvent extends CrateInteractEvent {
	private final @NotNull ItemStack item;
	private int amount;
	private boolean consumeKey = true;
	private boolean broadcastMessage = true;
	private boolean playSound = true;

	private static final @NotNull HandlerList handlers = new HandlerList();

	public CrateOpenInteractEvent(@NotNull Crate crate, @NotNull Player player, @NotNull ItemStack item, int amount) {
		super(crate, player);
		this.item = item;
		this.amount = amount;
	}

	public @NotNull ItemStack getItem() {
		return this.item;
	}

	public int getAmount() {
		return this.amount;
	}

	public void setAmount(int amount) {
		this.amount = amount;
	}

	public int getMaxPossibleAmount() {
		return this.item.getAmount();
	}

	public void setAmountToMax() {
		this.amount = this.getMaxPossibleAmount();
	}

	public boolean doesConsumingKey() {
		return this.consumeKey;
	}

	public void setConsumeKey(boolean consume) {
		this.consumeKey = consume;
	}

	public boolean doesBroadcastMessage() {
		return this.broadcastMessage;
	}

	public void setBroadcastMessage(boolean broadcast) {
		this.broadcastMessage = broadcast;
	}

	public boolean doesPlaySound() {
		return this.playSound;
	}

	public void setPlaySound(boolean play) {
		this.playSound = play;
	}

	@Override
	public @NotNull HandlerList getHandlers() {
		return handlers;
	}

	public static @NotNull HandlerList getHandlerList() {
		return handlers;
	}
}
