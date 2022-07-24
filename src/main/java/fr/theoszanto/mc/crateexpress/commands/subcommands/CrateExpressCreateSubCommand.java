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
import java.util.stream.Collectors;

public class CrateExpressCreateSubCommand extends CrateExpressSubCommand {
	public CrateExpressCreateSubCommand(@NotNull CrateExpress plugin) {
		super(plugin, "create", "new", "add");
	}

	@Override
	public boolean canExecute(@NotNull CommandSender sender, @NotNull CrateExpressCommand command, @NotNull String alias, @NotNull String subAlias, @NotNull String @NotNull[] args) {
		return sender instanceof Player && sender.hasPermission(CratePermission.Command.CREATE);
	}

	@Override
	public boolean onCommand(@NotNull CommandSender sender, @NotNull CrateExpressCommand command, @NotNull String alias, @NotNull String subAlias, @NotNull String @NotNull[] args) {
		if (args.length != 1)
			return false;
		String crateId = args[0];
		try {
			this.crates().get(crateId);
			this.i18nMessage(sender, "command.create.already-exists", "crate", crateId);
		} catch (IllegalArgumentException e) {
			Crate crate = new Crate(this.plugin, crateId, 1, 1, null, crateId, null, null, 0, null);
			this.crates().addCrate(crate);
			this.i18nMessage(sender, "command.create.success", "crate", crateId);
			new CrateEditGUI(this.plugin, crate).showToPlayer((Player) sender);
		}
		return true;
	}

	@Override
	public @Nullable List<@NotNull String> tabComplete(@NotNull CommandSender sender, @NotNull CrateExpressCommand command, @NotNull String alias, @NotNull String subAlias, @NotNull String @NotNull[] args) {
		return args.length == 1 ? this.existingNamespaces().stream().map(namespace -> namespace + "/").collect(Collectors.toList()) : null;
	}
}
