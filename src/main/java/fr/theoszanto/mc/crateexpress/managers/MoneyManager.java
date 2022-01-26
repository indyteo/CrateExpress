package fr.theoszanto.mc.crateexpress.managers;

import fr.theoszanto.mc.crateexpress.CrateExpress;
import fr.theoszanto.mc.crateexpress.utils.ItemBuilder;
import fr.theoszanto.mc.crateexpress.utils.ItemUtils;
import fr.theoszanto.mc.crateexpress.utils.PluginObject;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
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

	public void load(@NotNull ConfigurationSection config) throws IllegalStateException {
		this.moneyGiveCommand = config.getString("give-command", null);
		if (this.moneyGiveCommand == null)
			throw new IllegalStateException("Missing money give command in config");
		String item = config.getString("item", null);
		if (item == null)
			throw new IllegalStateException("Missing money icon item in config");
		try {
			this.item = Material.valueOf(item.toUpperCase());
		} catch (IllegalArgumentException e) {
			throw new IllegalStateException("Invalid money icon item in config: " + item);
		}
		this.currencySymbol = config.getString("currency-symbol", "");
		String placement = config.getString("placement", "before");
		if (!placement.equalsIgnoreCase("before") && !placement.equalsIgnoreCase("after"))
			this.warn("Unrecognized currency symbol placement value for money manager: " + placement + ". Should be either \"before\" or \"after\"");
		this.placementBefore = placement.equals("before");
		this.physical = config.getBoolean("physical", false);
	}

	public void reset() {
		this.moneyGiveCommand = null;
		this.item = Material.AIR;
		this.currencySymbol = "";
		this.placementBefore = true;
		this.physical = false;
	}
}
