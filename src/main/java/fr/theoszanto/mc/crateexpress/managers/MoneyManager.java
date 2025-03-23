package fr.theoszanto.mc.crateexpress.managers;

import fr.theoszanto.mc.crateexpress.CrateExpress;
import fr.theoszanto.mc.crateexpress.models.CrateConfig;
import fr.theoszanto.mc.crateexpress.utils.FormatUtils;
import fr.theoszanto.mc.crateexpress.utils.PluginObject;
import fr.theoszanto.mc.crateexpress.utils.VaultEconomy;
import fr.theoszanto.mc.express.utils.ItemBuilder;
import fr.theoszanto.mc.express.utils.ItemUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class MoneyManager extends PluginObject {
	private @NotNull GiveType giveType = GiveType.NONE;
	private @Nullable String moneyGiveCommand = null;
	private @Nullable VaultEconomy vaultEconomy = null;
	private @NotNull Material item = Material.AIR;
	private @NotNull String currencySymbol = "";
	private boolean placementBefore = true;
	private boolean physical = false;
	private boolean round = false;

	public MoneyManager(@NotNull CrateExpress plugin) {
		super(plugin);
	}

	public void giveMoney(@NotNull Player player, double amount) throws IllegalStateException {
		switch (this.giveType) {
		case COMMAND:
			Bukkit.dispatchCommand(Bukkit.getConsoleSender(), this.getMoneyGiveCommand(player, amount));
			break;
		case VAULT:
			if (this.vaultEconomy == null)
				this.vaultEconomy = new VaultEconomy(this.plugin);
			this.vaultEconomy.giveMoney(player, this.round ? Math.round(amount) : amount);
			break;
		case NONE:
		default:
			throw new IllegalStateException("Money module not configured");
		}
	}

	public @NotNull String getMoneyGiveCommand(@NotNull Player player, double amount) throws IllegalStateException {
		if (this.moneyGiveCommand == null)
			throw new IllegalStateException("Money module not initialized");
		return this.moneyGiveCommand.replaceAll("<player>", player.getName())
				.replaceAll("<display>", ItemUtils.COMPONENT_SERIALIZER.serialize(player.displayName()))
				.replaceAll("<uuid>", player.getUniqueId().toString())
				.replaceAll("<amount>", this.round ? Long.toString(Math.round(amount)) : Double.toString(amount));
	}

	public @NotNull ItemStack getIcon(double min, double max) throws IllegalStateException {
		if (this.item == Material.AIR)
			throw new IllegalStateException("Money module not initialized");
		String moneyMin = this.formatMoney(this.round ? Math.round(min) : min);
		if (min == max)
			return new ItemBuilder(this.item, ItemUtils.stackAmountFromValue(min),
					this.i18n("crate.preview.money-name", "amount", moneyMin),
					this.i18nLines("crate.preview.money-lore", "amount", moneyMin)).buildUnmodifiable();
		String moneyMax = this.formatMoney(this.round ? Math.round(max) : max);
		return new ItemBuilder(this.item, ItemUtils.stackAmountFromValue((min + max) / 2),
				this.i18n("crate.preview.money-random-name", "min", moneyMin, "max", moneyMax),
				this.i18nLines("crate.preview.money-random-lore", "min", moneyMin, "max", moneyMax)).buildUnmodifiable();
	}

	public @NotNull String formatMoney(double amount) {
		String format = FormatUtils.splitEveryThreeDigits(amount, true);
		return this.placementBefore ? this.currencySymbol + format : format + this.currencySymbol;
	}

	public boolean isPhysical() {
		return this.physical;
	}

	public void load(@NotNull CrateConfig.Money config) throws IllegalStateException {
		if (config.isEmpty())
			return;
		this.giveType = config.getGiveType();
		this.moneyGiveCommand = config.getGiveCommand();
		this.item = config.getItem();
		this.currencySymbol = config.getCurrencySymbol();
		this.placementBefore = config.isPlacementBefore();
		this.physical = config.isPhysical();
		this.round = config.isRound();
	}

	public void reset() {
		this.giveType = GiveType.NONE;
		this.moneyGiveCommand = null;
		this.vaultEconomy = null;
		this.item = Material.AIR;
		this.currencySymbol = "";
		this.placementBefore = true;
		this.physical = false;
	}

	public enum GiveType {
		NONE, COMMAND, VAULT
	}
}
