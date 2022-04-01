package fr.theoszanto.mc.crateexpress.commands.subcommands;

import fr.theoszanto.mc.crateexpress.CrateExpress;
import fr.theoszanto.mc.crateexpress.commands.CrateExpressCommand;
import fr.theoszanto.mc.crateexpress.models.Crate;
import fr.theoszanto.mc.crateexpress.utils.CratePermission;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class CrateExpressExportSubCommand extends CrateExpressSubCommand {
	public CrateExpressExportSubCommand(@NotNull CrateExpress plugin) {
		super(plugin, "export");
	}

	@Override
	public boolean canExecute(@NotNull CommandSender sender, @NotNull CrateExpressCommand command, @NotNull String alias, @NotNull String subAlias, @NotNull String @NotNull[] args) {
		return sender.hasPermission(CratePermission.Command.EXPORT);
	}

	@Override
	public boolean onCommand(@NotNull CommandSender sender, @NotNull CrateExpressCommand command, @NotNull String alias, @NotNull String subAlias, @NotNull String @NotNull[] args) {
		if (args.length != 2)
			return false;
		String formatOrFilename = args[0];
		String crateId = args[1];
		Crate crate;
		try {
			crate = this.crates().get(crateId);
		} catch (IllegalArgumentException e) {
			this.i18nMessage(sender, "command.unknown-crate", "crate", crateId);
			return true;
		}
		File exportedFile;
		if (formatOrFilename.indexOf('.') == -1) {
			exportedFile = this.export().toFormat(crate, formatOrFilename);
			if (exportedFile == null) {
				this.i18nMessage(sender, "command.export.fail.format", "format", formatOrFilename);
				return true;
			}
		} else {
			exportedFile = this.export().toFile(crate, formatOrFilename);
			if (exportedFile == null) {
				this.i18nMessage(sender, "command.export.fail.file", "file", formatOrFilename);
				return true;
			}
		}
		String path = this.plugin.getDataFolder().toPath().relativize(exportedFile.toPath()).normalize().toString();
		this.i18nMessage(sender, "command.export.success", "crate", crate.getName(), "file", path);
		return true;
	}

	@Override
	public @Nullable List<@NotNull String> tabComplete(@NotNull CommandSender sender, @NotNull CrateExpressCommand command, @NotNull String alias, @NotNull String subAlias, @NotNull String @NotNull[] args) {
		if (args.length == 1) {
			String formatOrFilename = args[0];
			boolean nonEmptyArg = !formatOrFilename.isEmpty();
			int dot = formatOrFilename.lastIndexOf('.') + 1;
			List<String> values = new ArrayList<>();
			String filename = formatOrFilename.substring(0, dot);
			String format = formatOrFilename.substring(dot);
			String sep = formatOrFilename.isEmpty() || formatOrFilename.charAt(formatOrFilename.length() - 1) != '.' ? "." : "";
			for (String key : this.export().keys()) {
				if (key.startsWith(format))
					values.add(filename + key);
				if (nonEmptyArg)
					values.add(formatOrFilename + sep + key);
			}
			return values;
		}
		if (args.length == 2)
			return this.existingCrates();
		return null;
	}
}
