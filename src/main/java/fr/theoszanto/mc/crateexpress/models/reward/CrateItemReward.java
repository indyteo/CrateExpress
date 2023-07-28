package fr.theoszanto.mc.crateexpress.models.reward;

import fr.theoszanto.mc.crateexpress.CrateExpress;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public class CrateItemReward extends CrateReward {
	private final @NotNull ItemStack item;

	public CrateItemReward(@NotNull CrateExpress plugin, double weight, @NotNull ItemStack item) {
		super(plugin, "item", item, weight, true);
		this.item = item;
	}

	@Override
	protected void reward(@NotNull Player player) throws RewardGiveException {
		player.getInventory().addItem(this.item.clone());
	}

	@Override
	public @NotNull String describe() {
		return "x" + this.item.getAmount() + " " + super.describe();
	}

	public @NotNull ItemStack getItem() {
		return this.item;
	}
}
