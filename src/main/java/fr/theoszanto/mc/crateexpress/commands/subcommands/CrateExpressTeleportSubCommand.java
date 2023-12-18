package fr.theoszanto.mc.crateexpress.commands.subcommands;

import fr.theoszanto.mc.crateexpress.CrateExpress;
import fr.theoszanto.mc.crateexpress.commands.CrateExpressCommand;
import fr.theoszanto.mc.crateexpress.models.Crate;
import fr.theoszanto.mc.crateexpress.models.gui.CrateTeleportSelectGUI;
import fr.theoszanto.mc.crateexpress.utils.CratePermission;
import fr.theoszanto.mc.express.ExpressObject;
import fr.theoszanto.mc.express.utils.UnloadableWorldLocation;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

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
		Player player = (Player) sender;
		String crateId = args[0];
		try {
			Crate crate = this.crates().get(crateId);
			List<UnloadableWorldLocation> locations = crate.getLocations();
			if (locations == null || locations.isEmpty())
				this.i18nMessage(sender, "command.teleport.no-location");
			else
				teleportOrOpenSelectMenu(this, crate, player);
		} catch (IllegalArgumentException e) {
			this.i18nMessage(sender, "command.unknown-crate", "crate", crateId);
		}
		return true;
	}

	@Override
	public @Nullable List<@NotNull String> tabComplete(@NotNull CommandSender sender, @NotNull CrateExpressCommand command, @NotNull String alias, @NotNull String subAlias, @NotNull String @NotNull[] args) {
		return args.length == 1 ? this.existingCrates() : null;
	}

	public static boolean teleportOrOpenSelectMenu(@NotNull ExpressObject<CrateExpress> object, @NotNull Crate crate, @NotNull Player player) {
		List<UnloadableWorldLocation> locations = crate.getLocations();
		if (locations == null || locations.isEmpty())
			return false;
		if (locations.size() == 1) {
			UnloadableWorldLocation location = locations.get(0);
			if (location.isWorldLoaded()) {
				player.teleport(location.clone().add(0.5, 0, 0.5));
				object.i18nMessage(player, "command.teleport.success", "crate", crate.getName());
				return true;
			} else {
				object.i18nMessage(player, "command.teleport.unloaded-world", "world", location.getWorldName());
				return false;
			}
		} else
			new CrateTeleportSelectGUI(object.getPlugin(), crate, null).showToPlayer(player);
		return false;
	}
}
