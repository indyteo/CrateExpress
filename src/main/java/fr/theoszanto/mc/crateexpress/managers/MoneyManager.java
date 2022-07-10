package fr.theoszanto.mc.crateexpress.managers;

import fr.theoszanto.mc.crateexpress.CrateExpress;
import fr.theoszanto.mc.crateexpress.models.CrateConfig;
import fr.theoszanto.mc.crateexpress.utils.PluginObject;
import fr.theoszanto.mc.express.utils.ItemBuilder;
import fr.theoszanto.mc.express.utils.ItemUtils;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class MoneyManager extends PluginObject {
	private @Nullable String moneyGiveCommand = null;
	private @NotNull Material item = Material.AIR;
	private @NotNull String currencySymbol = "";
	private boolean placementBefore = true;
	private boolean physical = false;

	public MoneyManager(@NotNull CrateExpress plugin) {
		super(plugin);
	}

	public @NotNull String getMoneyGiveCommand(@NotNull Player player, double amount) throws IllegalStateException {
		if (this.moneyGiveCommand == null)
			throw new IllegalStateException("Money module not initialized");
		return this.moneyGiveCommand.replaceAll("<player>", player.getName())
				.replaceAll("<display>", player.getDisplayName())
				.replaceAll("<uuid>", player.getUniqueId().toString())
				.replaceAll("<amount>", Double.toString(amount));
	}

	public @NotNull ItemStack getItem(double amount) throws IllegalStateException {
		if (this.item == Material.AIR)
			throw new IllegalStateException("Money module not initialized");
		String money = this.formatMoney(amount);
		return new ItemBuilder(this.item, ItemUtils.stackAmountFromValue(amount),
				this.i18n("crate.preview.money-name", "amount", money),
				this.i18nLines("crate.preview.money-lore", "amount", money)).buildUnmodifiable();
	}

	public @NotNull String formatMoney(double amount) {
		return this.placementBefore ? this.currencySymbol + amount : amount + this.currencySymbol;
	}

	public boolean isPhysical() {
		return this.physical;
	}

	public void load(@NotNull CrateConfig.Money config) throws IllegalStateException {
		if (config.isEmpty())
			return;
		this.moneyGiveCommand = config.getGiveCommand();
		this.item = config.getItem();
		this.currencySymbol = config.getCurrencySymbol();
		this.placementBefore = config.isPlacementBefore();
		this.physical = config.isPhysical();
	}

	public void reset() {
		this.moneyGiveCommand = null;
		this.item = Material.AIR;
		this.currencySymbol = "";
		this.placementBefore = true;
		this.physical = false;
	}
}
