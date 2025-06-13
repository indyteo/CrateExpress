package fr.theoszanto.mc.crateexpress.managers.money;

import fr.theoszanto.mc.crateexpress.models.reward.CrateReward;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class NoopMoneyProvider implements MoneyProvider {
	@Override
	public void giveMoney(@NotNull Player player, double amount, @NotNull CrateReward origin) throws IllegalStateException {
		throw new IllegalStateException("Money module not configured");
	}
}
