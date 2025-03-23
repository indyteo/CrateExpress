package fr.theoszanto.mc.crateexpress.models.gui.reward;

import fr.theoszanto.mc.crateexpress.CrateExpress;
import fr.theoszanto.mc.crateexpress.models.Crate;
import fr.theoszanto.mc.crateexpress.models.reward.CrateMoneyReward;
import fr.theoszanto.mc.crateexpress.models.reward.CrateReward;
import fr.theoszanto.mc.express.utils.ItemBuilder;
import fr.theoszanto.mc.express.utils.ItemUtils;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryAction;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.TimeUnit;
import java.util.function.DoubleConsumer;

public class CrateMoneyRewardGUI extends CrateRewardGUI<CrateMoneyReward> {
	private boolean random = false;
	private double amount;
	private double min;
	private double max;

	public CrateMoneyRewardGUI(@NotNull CrateExpress plugin, @NotNull Crate crate) {
		super(plugin, crate);
	}

	public CrateMoneyRewardGUI(@NotNull CrateExpress plugin, @NotNull Crate crate, @Nullable CrateMoneyReward reward, @NotNull Integer slot) {
		super(plugin, crate, reward, slot);
	}

	private boolean isRandom() {
		return this.reward == null ? this.random : this.reward.isRandom();
	}

	private double getAmount() {
		if (this.reward == null) {
			if (this.random)
				throw new IllegalStateException("No amount on random reward");
			return this.amount;
		}
		if (this.reward.isRandom())
			throw new IllegalStateException("No amount on random reward");
		return this.reward.getAmount();
	}

	private void setAmount(double amount) {
		if (this.reward == null)
			this.amount = amount;
		else {
			this.reward.setMin(amount);
			this.reward.setMax(amount);
		}
	}

	private double getMin() {
		if (this.reward == null) {
			if (!this.random)
				throw new IllegalStateException("No min amount on fixed reward");
			return this.min;
		}
		if (!this.reward.isRandom())
			throw new IllegalStateException("No min amount on fixed reward");
		return this.reward.getMin();
	}

	private void setMin(double min) {
		if (this.reward == null)
			this.min = min;
		else
			this.reward.setMin(min);
	}

	private double getMax() {
		if (this.reward == null) {
			if (!this.random)
				throw new IllegalStateException("No max amount on fixed reward");
			return this.max;
		}
		if (!this.reward.isRandom())
			throw new IllegalStateException("No max amount on fixed reward");
		return this.reward.getMax();
	}

	private void setMax(double max) {
		if (this.reward == null)
			this.max = max;
		else
			this.reward.setMax(max);
	}

	@Override
	protected void setupButtons(@NotNull Player player) {
		this.set(slot(0, 4), new ItemBuilder(Material.EMERALD, 1, this.i18n("menu.reward.money.header.name"), this.i18nLines("menu.reward.money.header.lore")));
		this.set(slot(1, 2), new ItemBuilder(Material.EMERALD, 1, this.i18n("menu.reward.money.random.name", "random", this.i18n(this.isRandom() ? "misc.yes" : "misc.no")), this.i18nLines("menu.reward.money.random.lore")).addLoreConditionally(this.reward == null, this.i18n("menu.reward.money.random.toggle")), this.reward == null ? "random" : "");
		if (this.isRandom()) {
			double min = this.getMin();
			double max = this.getMax();
			this.set(slot(1, 3), new ItemBuilder(Material.GOLD_NUGGET, ItemUtils.stackAmountFromValue(min), this.i18n("menu.reward.money.min.name", "min", this.plugin.money().formatMoney(min)), this.i18nLines("menu.reward.money.min.lore")), "min");
			this.set(slot(1, 4), new ItemBuilder(Material.RAW_GOLD, ItemUtils.stackAmountFromValue(max), this.i18n("menu.reward.money.max.name", "max", this.plugin.money().formatMoney(max)), this.i18nLines("menu.reward.money.max.lore")), "max");
		} else {
			double amount = this.getAmount();
			this.set(slot(1, 4), new ItemBuilder(Material.GOLD_INGOT, ItemUtils.stackAmountFromValue(amount), this.i18n("menu.reward.money.amount.name", "amount", this.plugin.money().formatMoney(amount)), this.i18nLines("menu.reward.money.amount.lore")), "amount");
		}
		this.setWeightButton(slot(1, 6));
	}

	@Override
	protected boolean canCreateReward() {
		return this.random ? this.min > 0 && this.max > this.min : this.amount > 0;
	}

	@Override
	protected @NotNull CrateMoneyReward createReward() throws IllegalStateException {
		if (!this.canCreateReward())
			throw new IllegalStateException();
		return this.random
				? new CrateMoneyReward(this.plugin, CrateReward.generateRandomId(), this.getWeight(), this.min, this.max)
				: new CrateMoneyReward(this.plugin, CrateReward.generateRandomId(), this.getWeight(), this.amount);
	}

	@Override
	protected boolean onButtonClick(@NotNull Player player, @NotNull ClickType click, @NotNull InventoryAction action, @NotNull SlotData data) {
		switch (data.getName()) {
		case "random":
			if (this.reward == null) {
				this.random = !this.random;
				this.refresh(player);
			}
			break;
		case "amount":
			this.askForAmount(player, "amount", this::setAmount);
			break;
		case "min":
			this.askForAmount(player, "min", this::setMin);
			break;
		case "max":
			this.askForAmount(player, "max", this::setMax);
			break;
		}
		return true;
	}

	private void askForAmount(@NotNull Player player, @NotNull String type, @NotNull DoubleConsumer setter) {
		this.i18nMessage(player, "menu.reward.money.request", "type", this.i18n("menu.reward.money." + type + ".type"));
		player.closeInventory();
		this.spigot().requestChatMessage(player, 1, TimeUnit.MINUTES).whenComplete((amount, timeout) -> {
			if (timeout == null) {
				try {
					setter.accept(Double.parseDouble(amount));
				} catch (NumberFormatException e) {
					this.i18nMessage(player, "menu.reward.money.invalid");
				}
			} else
				this.i18nMessage(player, "menu.reward.money.timeout");
			this.run(() -> this.showToPlayer(player));
		});
	}
}
