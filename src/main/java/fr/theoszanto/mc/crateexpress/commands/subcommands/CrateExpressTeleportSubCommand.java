package fr.theoszanto.mc.crateexpress.commands.subcommands;

import fr.theoszanto.mc.crateexpress.CrateExpress;
import fr.theoszanto.mc.crateexpress.commands.CrateExpressCommand;
import fr.theoszanto.mc.crateexpress.models.Crate;
import fr.theoszanto.mc.crateexpress.utils.CratePermission;
import fr.theoszanto.mc.crateexpress.utils.UnloadableWorldLocation;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.stream.Collectors;

public class CrateExpressTeleportSubCommand extends CrateExpressSubCommand {
	public CrateExpressTeleportSubCommand(@NotNull CrateExpress plugin) {
		super(plugin, "teleport", "tp");
	}

	@Override
	public boolean canExecute(@NotNull CommandSender sender, @NotNull CrateExpressCommand command, @NotNull String alias, @NotNull String subAlias, @NotNull String @NotNull[] args) {
		return sender instanceof Player && sender.hasPermission(CratePermission.Command.TELEPORT);
	}

	@Override
	public boolean onCommand(@NotNull CommandSender sender, @NotNull CrateExpressCommand command, @NotNull String alias, @NotNull String subAlias, @NotNull String @NotNull[] args) {
		if (args.length != 1)
			return false;
		String crateId = args[0];
		try {
			Crate crate = this.crates().get(crateId);
			UnloadableWorldLocation location = crate.getLocation();
			if (location == null)
				this.i18nMessage(sender, "command.teleport.no-location");
			else {
				if (location.isWorldLoaded()) {
					((Player) sender).teleport(location);
					this.i18nMessage(sender, "command.teleport.success", "crate", crate.getName());
				} else
					this.i18nMessage(sender, "command.teleport.unloaded-world", "world", location.getWorldName());
			}
		} catch (IllegalArgumentException e) {
			this.i18nMessage(sender, "command.unknown-crate", "crate", crateId);
		}
		return true;
	}

	@Override
	public @Nullable List<@NotNull String> tabComplete(@NotNull CommandSender sender, @NotNull CrateExpressCommand command, @NotNull String alias, @NotNull String subAlias, @NotNull String @NotNull[] args) {
		return args.length == 1 ? this.crates().list().stream().map(Crate::getId).collect(Collectors.toList()) : null;
	}
}
