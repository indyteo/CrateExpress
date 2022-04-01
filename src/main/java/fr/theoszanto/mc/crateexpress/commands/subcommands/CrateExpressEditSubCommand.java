package fr.theoszanto.mc.crateexpress.commands.subcommands;

import fr.theoszanto.mc.crateexpress.CrateExpress;
import fr.theoszanto.mc.crateexpress.commands.CrateExpressCommand;
import fr.theoszanto.mc.crateexpress.models.Crate;
import fr.theoszanto.mc.crateexpress.models.gui.CrateEditGUI;
import fr.theoszanto.mc.crateexpress.utils.CratePermission;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class CrateExpressEditSubCommand extends CrateExpressSubCommand {
	public CrateExpressEditSubCommand(@NotNull CrateExpress plugin) {
		super(plugin, "edit");
	}

	@Override
	public boolean canExecute(@NotNull CommandSender sender, @NotNull CrateExpressCommand command, @NotNull String alias, @NotNull String subAlias, @NotNull String @NotNull[] args) {
		return sender instanceof Player && sender.hasPermission(CratePermission.Command.EDIT);
	}

	@Override
	public boolean onCommand(@NotNull CommandSender sender, @NotNull CrateExpressCommand command, @NotNull String alias, @NotNull String subAlias, @NotNull String @NotNull[] args) {
		if (args.length != 1)
			return false;
		String crateId = args[0];
		try {
			Crate crate = this.crates().get(crateId);
			new CrateEditGUI(this.plugin, crate).showToPlayer((Player) sender);
		} catch (IllegalArgumentException e) {
			this.i18nMessage(sender, "command.unknown-crate", "crate", crateId);
		}
		return true;
	}

	@Override
	public @Nullable List<@NotNull String> tabComplete(@NotNull CommandSender sender, @NotNull CrateExpressCommand command, @NotNull String alias, @NotNull String subAlias, @NotNull String @NotNull[] args) {
		return args.length == 1 ? this.existingCrates() : null;
	}
}
