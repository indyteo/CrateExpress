package fr.theoszanto.mc.crateexpress.commands.subcommands;

import fr.theoszanto.mc.crateexpress.CrateExpress;
import fr.theoszanto.mc.crateexpress.commands.CrateExpressCommand;
import fr.theoszanto.mc.crateexpress.models.Crate;
import fr.theoszanto.mc.crateexpress.models.CrateKey;
import fr.theoszanto.mc.crateexpress.utils.CratePermission;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
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
		Collection<? extends Player> targets;
		if (target.equals("to")) {
			if (args.length < 3)
				return false;
			String playerName = args[i++];
			Player player = Bukkit.getPlayer(playerName);
			if (player == null) {
				this.i18nMessage(sender, "command.unknown-player", "player", playerName);
				return true;
			}
			targets = Collections.singletonList(player);
		} else if (target.equals("all")) {
			if (args.length < 2)
				return false;
			targets = Bukkit.getOnlinePlayers();
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
		for (Player player : targets)
			key.giveTo(player, amount);
		this.i18nMessage(sender, "command.give.success");
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
			return this.existingCrates();
		if (args.length == i)
			return this.numbers(1, 64);
		return null;
	}
}
