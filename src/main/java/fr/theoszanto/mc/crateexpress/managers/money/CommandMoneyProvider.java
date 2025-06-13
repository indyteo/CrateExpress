package fr.theoszanto.mc.crateexpress.managers.money;

import fr.theoszanto.mc.crateexpress.CrateExpress;
import fr.theoszanto.mc.crateexpress.models.reward.CrateReward;
import fr.theoszanto.mc.crateexpress.utils.PluginObject;
import fr.theoszanto.mc.express.utils.ItemUtils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class CommandMoneyProvider extends PluginObject implements MoneyProvider {
	private final @NotNull String moneyGiveCommand;

	public CommandMoneyProvider(@NotNull CrateExpress plugin, @NotNull String moneyGiveCommand) {
		super(plugin);
		this.moneyGiveCommand = moneyGiveCommand;
	}

	@Override
	public void giveMoney(@NotNull Player player, double amount, @NotNull CrateReward origin) throws IllegalStateException {
		long round = Math.round(amount);
		Bukkit.dispatchCommand(Bukkit.getConsoleSender(), this.moneyGiveCommand
				.replaceAll("<player>", player.getName())
				.replaceAll("<display>", ItemUtils.COMPONENT_SERIALIZER.serialize(player.displayName()))
				.replaceAll("<uuid>", player.getUniqueId().toString())
				.replaceAll("<amount>", amount == round ? Long.toString(round) : Double.toString(amount))
				.replaceAll("<origin>", origin.describe()));
	}
}
