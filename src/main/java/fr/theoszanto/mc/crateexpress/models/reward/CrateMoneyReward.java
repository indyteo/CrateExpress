package fr.theoszanto.mc.crateexpress.models.reward;

import fr.theoszanto.mc.crateexpress.CrateExpress;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class CrateMoneyReward extends CrateReward {
	private final double amount;

	public CrateMoneyReward(@NotNull CrateExpress plugin, int weight, double amount) {
		super(plugin, plugin.money().getItem(amount), weight, plugin.money().isPhysical());
		this.amount = amount;
	}

	@Override
	public void reward(@NotNull Player player) throws RewardGiveException {
		try {
			Bukkit.dispatchCommand(Bukkit.getConsoleSender(), this.money().getMoneyGiveCommand(player, this.amount));
		} catch (IllegalStateException e) {
			throw new RewardGiveException(e.getMessage());
		}
	}

	public double getAmount() {
		return this.amount;
	}
}
