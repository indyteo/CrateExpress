package fr.theoszanto.mc.crateexpress.commands;

import fr.theoszanto.mc.crateexpress.CrateExpress;
import fr.theoszanto.mc.crateexpress.commands.subcommands.CrateExpressSubCommand;
import fr.theoszanto.mc.express.commands.ExpressCommand;
import fr.theoszanto.mc.express.utils.JavaUtils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class CrateExpressCommand extends ExpressCommand<CrateExpress> {
	private final @NotNull List<? extends CrateExpressSubCommand> subCommands;

	public CrateExpressCommand(@NotNull CrateExpress plugin) {
		super(plugin, "crateexpress");
		this.subCommands = JavaUtils.instanciateSubClasses(CrateExpressSubCommand.class, "fr.theoszanto.mc.crateexpress.commands.subcommands", plugin);
	}

	@Override
	public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String @NotNull[] args) {
		String subCommandName = args.length == 0 ? "help" : args[0];
		CrateExpressSubCommand subCommand = this.subCommandByName(subCommandName);
		if (subCommand == null) {
			this.i18nMessage(sender, "command.unknown-subcommand", "command", alias, "subcommand", subCommandName);
			return true;
		}
		String[] subArgs = JavaUtils.subArray(args, 1);
		if (subCommand.canExecute(sender, this, alias, subCommandName, subArgs)) {
			if (!subCommand.onCommand(sender, this, alias, subCommandName, subArgs)) {
				this.i18nMessage(sender, "command.invalid-usage");
				this.i18nRawMessage(sender, "command.help." + subCommand.getName(), "alias", alias, "subcommand", subCommandName);
			}
		}
		else
			this.i18nMessage(sender, "command.not-access", "command", alias, "subcommand", subCommandName);
		return true;
	}

	@Override
	public @Nullable List<@NotNull String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String @NotNull[] args) {
		if (args.length == 0)
			return Collections.emptyList();
		String subCommandName = args[0];
		List<String> result = null;
		if (args.length == 1) {
			result = this.getAllowedSubCommandsNames(sender, alias);
		} else {
			CrateExpressSubCommand subCommand = this.subCommandByName(subCommandName);
			if (subCommand != null) {
				String[] subArgs = JavaUtils.subArray(args, 1);
				if (subCommand.canExecute(sender, this, alias, subCommandName, subArgs))
					result = subCommand.tabComplete(sender, this, alias, subCommandName, subArgs);
			}
		}
		if (result == null)
			return Collections.emptyList();
		String lastArg = args[args.length - 1].toLowerCase();
		return result.stream().filter(name -> name.toLowerCase().startsWith(lastArg)).collect(Collectors.toList());
	}

	public @Nullable CrateExpressSubCommand subCommandByName(@NotNull String name) {
		for (CrateExpressSubCommand subCommand : this.subCommands)
			if (subCommand.hasName(name))
				return subCommand;
		return null;
	}

	public @NotNull List<? extends CrateExpressSubCommand> getSubCommands() {
		return this.subCommands;
	}

	public @NotNull List<@NotNull String> getAllowedSubCommandsNames(@NotNull CommandSender sender, @NotNull String alias) {
		List<String> result = new ArrayList<>();
		for (CrateExpressSubCommand subCommand : this.subCommands) {
			if (subCommand.canExecute(sender, this, alias, subCommand.getName(), new String[0])) {
				result.add(subCommand.getName());
				result.addAll(subCommand.getAliases());
			}
		}
		return result;
	}
}
