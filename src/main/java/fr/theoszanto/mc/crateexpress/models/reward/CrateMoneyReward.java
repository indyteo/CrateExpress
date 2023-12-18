package fr.theoszanto.mc.crateexpress.models.reward;

import fr.theoszanto.mc.crateexpress.CrateExpress;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class CrateMoneyReward extends CrateReward {
	private final double amount;

	public CrateMoneyReward(@NotNull CrateExpress plugin, @NotNull String id, double weight, double amount) {
		super(plugin, id, "money", plugin.money().getItem(amount), weight, plugin.money().isPhysical());
		this.amount = amount;
	}

	@Override
	protected void reward(@NotNull Player player) throws RewardGiveException {
		try {
			this.money().giveMoney(player, this.amount);
		} catch (IllegalStateException e) {
			throw new RewardGiveException(e.getMessage());
		}
	}

	public double getAmount() {
		return this.amount;
	}
}
