package fr.theoszanto.mc.crateexpress.commands.subcommands;

import fr.theoszanto.mc.crateexpress.CrateExpress;
import fr.theoszanto.mc.crateexpress.commands.CrateExpressCommand;
import fr.theoszanto.mc.crateexpress.models.Crate;
import fr.theoszanto.mc.crateexpress.utils.CratePermission;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class CrateExpressStatsSubCommand extends CrateExpressSubCommand {
	public CrateExpressStatsSubCommand(@NotNull CrateExpress plugin) {
		super(plugin, "stats", "stat");
	}

	@Override
	public boolean canExecute(@NotNull CommandSender sender, @NotNull CrateExpressCommand command, @NotNull String alias, @NotNull String subAlias, @NotNull String @NotNull[] args) {
		return sender.hasPermission(CratePermission.Command.STATS);
	}

	@Override
	public boolean onCommand(@NotNull CommandSender sender, @NotNull CrateExpressCommand command, @NotNull String alias, @NotNull String subAlias, @NotNull String @NotNull[] args) {
		if (args.length != 1)
			return false;
		String crateId = args[0];
		try {
			Crate crate = this.crates().get(crateId);
			this.i18nMessage(sender, "command.stats", "crate", crate.getName(), "open", this.stats().getTimesOpened(crate));
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
