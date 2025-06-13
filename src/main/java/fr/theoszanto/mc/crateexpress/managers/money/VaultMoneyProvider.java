package fr.theoszanto.mc.crateexpress.managers.money;

import fr.theoszanto.mc.crateexpress.CrateExpress;
import fr.theoszanto.mc.crateexpress.models.reward.CrateReward;
import fr.theoszanto.mc.crateexpress.utils.PluginObject;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class VaultMoneyProvider extends PluginObject implements MoneyProvider {
	private final @Nullable Economy economy;

	public VaultMoneyProvider(@NotNull CrateExpress plugin) {
		super(plugin);
		RegisteredServiceProvider<Economy> economyProvider = plugin.getServer().getServicesManager().getRegistration(Economy.class);
		this.economy = economyProvider == null ? null : economyProvider.getProvider();
	}

	@Override
	public void giveMoney(@NotNull Player player, double amount, @NotNull CrateReward origin) throws IllegalStateException {
		if (this.economy == null)
			throw new IllegalStateException("Vault economy not found");
		this.economy.depositPlayer(player, amount);
	}
}
