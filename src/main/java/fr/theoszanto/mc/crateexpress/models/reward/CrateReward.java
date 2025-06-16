package fr.theoszanto.mc.crateexpress.models.reward;

import fr.theoszanto.mc.crateexpress.CrateExpress;
import fr.theoszanto.mc.crateexpress.events.CrateRewardGiveEvent;
import fr.theoszanto.mc.crateexpress.utils.FormatUtils;
import fr.theoszanto.mc.crateexpress.utils.PluginObject;
import fr.theoszanto.mc.express.utils.ItemUtils;
import fr.theoszanto.mc.express.utils.MathUtils;
import fr.theoszanto.mc.express.utils.Weighted;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;
import java.util.logging.Level;

public abstract class CrateReward extends PluginObject implements Weighted {
	private final @NotNull String id;
	private final @NotNull String type;
	private @NotNull ItemStack icon;
	private double weight;
	private boolean physicalReward;

	public CrateReward(@NotNull CrateExpress plugin, @NotNull String id, @NotNull String type, @NotNull ItemStack icon, double weight, boolean physicalReward) {
		super(plugin);
		this.id = id;
		this.type = type;
		this.icon = icon;
		this.weight = weight;
		this.physicalReward = physicalReward;
	}

	public boolean cannotGiveTo(@NotNull Player player) {
		return this.physicalReward && player.getInventory().firstEmpty() == -1;
	}

	public boolean giveRewardTo(@NotNull Player player, boolean saveRewardIfUnableToGive) {
		CrateRewardGiveEvent event = new CrateRewardGiveEvent(player, this, this.cannotGiveTo(player));
		event.callEvent();
		CrateReward reward = event.getReward();
		if (event.isSavingReward()) {
			if (saveRewardIfUnableToGive) {
				reward.save(player);
				this.i18nMessage(player, "crate.reward.save", "reward", reward.describe());
			}
			return false;
		} else {
			try {
				reward.reward(player);
				return true;
			} catch (RewardGiveException e) {
				if (saveRewardIfUnableToGive) {
					this.getLogger().log(Level.WARNING, "Unable to reward player " + player.getName() + " (" + player.getUniqueId() + "): " + e.getMessage() + "! Saving reward instead", e);
					reward.save(player);
					this.i18nMessage(player, "crate.reward.error", "reward", reward.describe());
				}
				return false;
			}
		}
	}

	protected abstract void reward(@NotNull Player player) throws RewardGiveException;

	public @NotNull String describe() {
		return ItemUtils.name(this.getIcon());
	}

	protected void save(@NotNull Player player) {
		this.async(() -> this.storage().getSource().saveReward(player, this));
	}

	public @NotNull ItemStack getIconWithChance(double crateWeight) {
		ItemStack icon = this.getIcon().clone();
		if (crateWeight <= 0)
			return icon;
		double chance = MathUtils.round(100 * this.weight / crateWeight, 2);
		ItemUtils.addLore(icon, this.i18nLines("crate.preview.reward-chance", "chance", FormatUtils.noTrailingZeroDecimal(chance)));
		return icon;
	}

	public @NotNull String getId() {
		return this.id;
	}

	public @NotNull String getType() {
		return this.type;
	}

	public @NotNull ItemStack getIcon() {
		return this.icon;
	}

	protected void setIcon(@NotNull ItemStack icon) {
		this.icon = icon;
	}

	@Override
	public double getWeight() {
		return this.weight;
	}

	public void setWeight(double weight) {
		this.weight = weight;
	}

	public boolean isPhysicalReward() {
		return this.physicalReward;
	}

	protected void setPhysicalReward(boolean physicalReward) {
		this.physicalReward = physicalReward;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		CrateReward that = (CrateReward) o;
		return this.id.equals(that.id);
	}

	@Override
	public int hashCode() {
		return this.id.hashCode();
	}

	public static @NotNull String generateRandomId() {
		return UUID.randomUUID().toString().substring(0, 8);
	}

	protected static class RewardGiveException extends Exception {
		public RewardGiveException(@NotNull String message) {
			super(message);
		}
	}
}
