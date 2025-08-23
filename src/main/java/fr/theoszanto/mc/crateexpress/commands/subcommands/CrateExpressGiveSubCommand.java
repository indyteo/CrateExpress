package fr.theoszanto.mc.crateexpress.commands.subcommands;

import fr.theoszanto.mc.crateexpress.CrateExpress;
import fr.theoszanto.mc.crateexpress.commands.CrateExpressCommand;
import fr.theoszanto.mc.crateexpress.events.CrateGiveAllEvent;
import fr.theoszanto.mc.crateexpress.models.Crate;
import fr.theoszanto.mc.crateexpress.models.CrateKey;
import fr.theoszanto.mc.crateexpress.utils.CratePermission;
import fr.theoszanto.mc.express.utils.ItemUtils;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

public class CrateExpressGiveSubCommand extends CrateExpressSubCommand {
	public CrateExpressGiveSubCommand(@NotNull CrateExpress plugin) {
		super(plugin, "give");
	}

	@Override
	public boolean canExecute(@NotNull CommandSender sender, @NotNull CrateExpressCommand command, @NotNull String alias, @NotNull String subAlias, @NotNull String @NotNull[] args) {
		return sender.hasPermission(CratePermission.Command.GIVE);
	}

	@Override
	public boolean onCommand(@NotNull CommandSender sender, @NotNull CrateExpressCommand command, @NotNull String alias, @NotNull String subAlias, @NotNull String @NotNull[] args) {
		if (args.length == 0)
			return false;
		int i = 0;
		String target = args[i++];
		Collection<? extends OfflinePlayer> targets;
		Collection<Player> allPlayers;
		boolean all;
		if (target.equals("to")) {
			if (args.length < 3)
				return false;
			String playerName = args[i++];
			OfflinePlayer player = Bukkit.getOfflinePlayer(playerName);
			if (!this.players().exists(player)) {
				this.i18nMessage(sender, "command.unknown-player", "player", playerName);
				return true;
			}
			all = false;
			targets = List.of(player);
			allPlayers = null;
		} else if (target.equals("all")) {
			if (args.length < 2)
				return false;
			all = true;
			targets = allPlayers = new ArrayList<>(Bukkit.getOnlinePlayers());
		} else
			return false;
		String crateId = args[i++];
		Crate crate;
		try {
			crate = this.crates().get(crateId);
		} catch (IllegalArgumentException e) {
			this.i18nMessage(sender, "command.unknown-crate", "crate", crateId);
			return true;
		}
		CrateKey key = crate.getKey();
		if (key == null) {
			this.i18nMessage(sender, "command.give.no-key", "crate", crateId);
			return true;
		}
		int amount;
		if (args.length <= i)
			amount = 1;
		else {
			String amountValue = args[i];
			try {
				amount = Integer.parseInt(amountValue);
			} catch (NumberFormatException e) {
				this.i18nMessage(sender, "command.invalid-int", "value", amountValue);
				return true;
			}
		}
		if (all) {
			CrateGiveAllEvent event = new CrateGiveAllEvent(sender, key, amount, allPlayers);
			if (!event.callEvent())
				return true;
		}
		for (OfflinePlayer player : targets)
			key.giveTo(player, amount, sender, all);
		this.i18nMessage(sender, "command.give.success",
				"amount", amount,
				"key", ItemUtils.name(key.getItem()),
				"players", targets.size(),
				"total", amount * targets.size());
		return true;
	}

	@Override
	public @Nullable List<@NotNull String> tabComplete(@NotNull CommandSender sender, @NotNull CrateExpressCommand command, @NotNull String alias, @NotNull String subAlias, @NotNull String @NotNull[] args) {
		if (args.length == 1)
			return Arrays.asList("all", "to");
		String target = args[0];
		int i = 2;
		if (target.equals("to")) {
			if (args.length == i++)
				return this.onlinePlayers();
		} else if (!target.equals("all"))
			return null;
		if (args.length == i++)
			return this.crates().list().stream().filter(crate -> crate.getKey() != null).map(Crate::getId).toList();
		if (args.length == i)
			return this.numbers(1, 64);
		return null;
	}
}
