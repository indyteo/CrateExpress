package fr.theoszanto.mc.crateexpress.commands.subcommands;

import fr.theoszanto.mc.crateexpress.CrateExpress;
import fr.theoszanto.mc.crateexpress.commands.CrateExpressCommand;
import fr.theoszanto.mc.crateexpress.models.CrateNamespace;
import fr.theoszanto.mc.crateexpress.models.gui.CrateListGUI;
import fr.theoszanto.mc.crateexpress.utils.CratePermission;
import fr.theoszanto.mc.express.gui.ExpressGUI;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class CrateExpressListSubCommand extends CrateExpressSubCommand {
	public CrateExpressListSubCommand(@NotNull CrateExpress plugin) {
		super(plugin, "list", "ls");
	}

	@Override
	public boolean canExecute(@NotNull CommandSender sender, @NotNull CrateExpressCommand command, @NotNull String alias, @NotNull String subAlias, @NotNull String @NotNull[] args) {
		return sender instanceof Player && sender.hasPermission(CratePermission.Command.LIST);
	}

	@Override
	public boolean onCommand(@NotNull CommandSender sender, @NotNull CrateExpressCommand command, @NotNull String alias, @NotNull String subAlias, @NotNull String @NotNull[] args) {
		ExpressGUI<CrateExpress> gui;
		if (args.length == 0)
			gui = new CrateListGUI(this.plugin);
		else if (args.length == 1) {
			String path = args[0];
			CrateNamespace namespace = new CrateNamespace(this.plugin, path);
			if (namespace.exists())
				gui = new CrateListGUI(this.plugin, namespace);
			else {
				this.i18nMessage(sender, "command.unknown-namespace", "path", path);
				return true;
			}
		} else
			return false;
		gui.showToPlayer((Player) sender);
		return true;
	}

	@Override
	public @Nullable List<@NotNull String> tabComplete(@NotNull CommandSender sender, @NotNull CrateExpressCommand command, @NotNull String alias, @NotNull String subAlias, @NotNull String @NotNull[] args) {
		return args.length == 1 ? this.existingNamespaces() : null;
	}
}
