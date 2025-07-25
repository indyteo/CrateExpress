package fr.theoszanto.mc.crateexpress.commands.subcommands;

import fr.theoszanto.mc.crateexpress.CrateExpress;
import fr.theoszanto.mc.crateexpress.commands.CrateExpressCommand;
import fr.theoszanto.mc.crateexpress.utils.CratePermission;
import io.papermc.paper.plugin.configuration.PluginMeta;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class CrateExpressInfoSubCommand extends CrateExpressSubCommand {
	public CrateExpressInfoSubCommand(@NotNull CrateExpress plugin) {
		super(plugin, "info", "i");
	}

	@Override
	public boolean canExecute(@NotNull CommandSender sender, @NotNull CrateExpressCommand command, @NotNull String alias, @NotNull String subAlias, @NotNull String @NotNull[] args) {
		return sender.hasPermission(CratePermission.Command.ACCESS);
	}

	@Override
	public boolean onCommand(@NotNull CommandSender sender, @NotNull CrateExpressCommand command, @NotNull String alias, @NotNull String subAlias, @NotNull String @NotNull[] args) {
		if (args.length != 0)
			return false;
		PluginMeta desc = this.plugin.getPluginMeta();
		for (String line : this.i18nLines("command.info",
				"plugin", desc.getName(),
				"authors", String.join(", ", desc.getAuthors()),
				"authors-count", desc.getAuthors().size(),
				"website", desc.getWebsite()))
			sender.sendMessage(this.prefix() + line);
		return true;
	}

	@Override
	public @Nullable List<@NotNull String> tabComplete(@NotNull CommandSender sender, @NotNull CrateExpressCommand command, @NotNull String alias, @NotNull String subAlias, @NotNull String @NotNull[] args) {
		return null;
	}
}
