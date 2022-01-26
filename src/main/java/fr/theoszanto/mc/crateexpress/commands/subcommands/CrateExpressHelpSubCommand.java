package fr.theoszanto.mc.crateexpress.commands.subcommands;

import fr.theoszanto.mc.crateexpress.CrateExpress;
import fr.theoszanto.mc.crateexpress.commands.CrateExpressCommand;
import fr.theoszanto.mc.crateexpress.utils.CratePermission;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class CrateExpressHelpSubCommand extends CrateExpressSubCommand {
	public CrateExpressHelpSubCommand(@NotNull CrateExpress plugin) {
		super(plugin, "help", "h", "?");
	}

	@Override
	public boolean canExecute(@NotNull CommandSender sender, @NotNull CrateExpressCommand command, @NotNull String alias, @NotNull String subAlias, @NotNull String @NotNull[] args) {
		return sender.hasPermission(CratePermission.Command.ACCESS);
	}

	@Override
	public boolean onCommand(@NotNull CommandSender sender, @NotNull CrateExpressCommand command, @NotNull String alias, @NotNull String subAlias, @NotNull String @NotNull[] args) {
		if (args.length == 0) {
			this.i18nMessage(sender, "command.help.all-commands");
			for (CrateExpressSubCommand subCommand : command.getSubCommands())
				this.i18nRawMessage(sender, "command.help." + subCommand.getName(), "alias", alias, "subcommand", subCommand.getName());
		} else if (args.length == 1) {
			String subCommandName = args[0];
			CrateExpressSubCommand subCommand = command.subCommandByName(subCommandName);
			if (subCommand == null)
				this.i18nMessage(sender, "command.unknown-subcommand", "command", alias, "subcommand", subCommandName);
			else if (subCommand.canExecute(sender, command, alias, subCommandName, new String[0]))
				this.i18nMessage(sender, "command.help." + subCommand.getName(), "alias", alias, "subcommand", subCommandName);
			else
				this.i18nMessage(sender, "command.not-access", "command", alias, "subcommand", subCommandName);
		} else
			return false;
		return true;
	}

	@Override
	public @Nullable List<@NotNull String> tabComplete(@NotNull CommandSender sender, @NotNull CrateExpressCommand command, @NotNull String alias, @NotNull String subAlias, @NotNull String @NotNull[] args) {
		return args.length == 1 ? command.getAllowedSubCommandsNames(sender, alias) : null;
	}
}
