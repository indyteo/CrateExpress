package fr.theoszanto.mc.crateexpress.commands.subcommands;

import fr.theoszanto.mc.crateexpress.CrateExpress;
import fr.theoszanto.mc.crateexpress.commands.CrateExpressCommand;
import fr.theoszanto.mc.crateexpress.models.Crate;
import fr.theoszanto.mc.crateexpress.utils.CratePermission;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class CrateExpressOpenSubCommand extends CrateExpressSubCommand {
	public CrateExpressOpenSubCommand(@NotNull CrateExpress plugin) {
		super(plugin, "open");
	}

	@Override
	public boolean canExecute(@NotNull CommandSender sender, @NotNull CrateExpressCommand command, @NotNull String alias, @NotNull String subAlias, @NotNull String @NotNull[] args) {
		return sender.hasPermission(CratePermission.Command.OPEN);
	}

	@Override
	public boolean onCommand(@NotNull CommandSender sender, @NotNull CrateExpressCommand command, @NotNull String alias, @NotNull String subAlias, @NotNull String @NotNull[] args) {
		if (args.length < 2 || args.length > 3)
			return false;
		String playerName = args[0];
		Player player = Bukkit.getPlayer(playerName);
		if (player == null) {
			this.i18nMessage(sender, "command.unknown-player", "player", playerName);
			return true;
		}
		String crateId = args[1];
		Crate crate;
		try {
			crate = this.crates().get(crateId);
		} catch (IllegalArgumentException e) {
			this.i18nMessage(sender, "command.unknown-crate", "crate", crateId);
			return true;
		}
		boolean triggerEffects = args.length > 2 && args[2].equalsIgnoreCase("true");
		crate.open(player, true);
		if (triggerEffects) {
			crate.playSoundAtLocation(player.getLocation());
			crate.showParticleAtLocation(player.getLocation());
		}
		this.i18nMessage(sender, "command.open", "crate", crate.getName(), "player", player.getName());
		return true;
	}

	@Override
	public @Nullable List<@NotNull String> tabComplete(@NotNull CommandSender sender, @NotNull CrateExpressCommand command, @NotNull String alias, @NotNull String subAlias, @NotNull String @NotNull[] args) {
		if (args.length == 1)
			return this.onlinePlayers();
		if (args.length == 2)
			return this.existingCrates();
		if (args.length == 3)
			return List.of("true", "false");
		return null;
	}
}
