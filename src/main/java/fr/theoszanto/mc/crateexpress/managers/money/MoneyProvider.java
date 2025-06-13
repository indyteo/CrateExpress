package fr.theoszanto.mc.crateexpress.managers.money;

import fr.theoszanto.mc.crateexpress.models.reward.CrateReward;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public interface MoneyProvider {
	void giveMoney(@NotNull Player player, double amount, @NotNull CrateReward origin) throws IllegalStateException;
}
