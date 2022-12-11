package fr.theoszanto.mc.crateexpress.utils;

import fr.theoszanto.mc.crateexpress.CrateExpress;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.jetbrains.annotations.NotNull;

public class VaultEconomy extends PluginObject {
	private final @NotNull Economy economy;

	public VaultEconomy(@NotNull CrateExpress plugin) {
		super(plugin);
		RegisteredServiceProvider<Economy> economyProvider = plugin.getServer().getServicesManager().getRegistration(Economy.class);
		if (economyProvider == null)
			throw new IllegalStateException("Vault economy not found");
		this.economy = economyProvider.getProvider();
	}

	public void giveMoney(@NotNull Player player, double amount) {
		this.economy.depositPlayer(player, amount);
	}
}
