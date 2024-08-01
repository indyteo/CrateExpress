package fr.theoszanto.mc.crateexpress.commands.subcommands;

import fr.theoszanto.mc.crateexpress.CrateExpress;
import fr.theoszanto.mc.crateexpress.commands.CrateExpressCommand;
import fr.theoszanto.mc.crateexpress.models.gui.CrateHistoryGUI;
import fr.theoszanto.mc.crateexpress.utils.CratePermission;
import fr.theoszanto.mc.crateexpress.utils.TimeUtils;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class CrateExpressHistorySubCommand extends CrateExpressSubCommand {
	private static final @NotNull SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("dd/MM/yyyy");

	public CrateExpressHistorySubCommand(@NotNull CrateExpress plugin) {
		super(plugin, "history");
	}

	@Override
	public boolean canExecute(@NotNull CommandSender sender, @NotNull CrateExpressCommand command, @NotNull String alias, @NotNull String subAlias, @NotNull String @NotNull[] args) {
		if (!(sender instanceof Player))
			return false;
		if (args.length == 1)
			return sender.hasPermission(CratePermission.Command.History.OTHER);
		return sender.hasPermission(CratePermission.Command.History.SELF);
	}

	@Override
	@SuppressWarnings("ConstantValue")
	public boolean onCommand(@NotNull CommandSender sender, @NotNull CrateExpressCommand command, @NotNull String alias, @NotNull String subAlias, @NotNull String @NotNull[] args) {
		if (args.length > 2)
			return false;
		Player viewer = (Player) sender;
		OfflinePlayer player;
		boolean unlimited;
		if (args.length == 0) {
			player = viewer;
			unlimited = false;
		} else {
			String playerName = args[0];
			player = Bukkit.getOfflinePlayer(playerName);
			if (player == null || !player.hasPlayedBefore()) {
				this.i18nMessage(sender, "command.unknown-player", "player", playerName);
				return true;
			}
			unlimited = true;
		}
		Date date;
		if (args.length < 2)
			date = TimeUtils.today();
		else {
			String rawDate = args[1];
			try {
				date = DATE_FORMAT.parse(rawDate);
			} catch (ParseException e) {
				this.i18nMessage(sender, "command.history.invalid-date", "date", rawDate, "format", DATE_FORMAT.toPattern());
				return true;
			}
		}
		new CrateHistoryGUI(this.plugin, player, date, unlimited).showToPlayer(viewer);
		return true;
	}

	@Override
	public @Nullable List<@NotNull String> tabComplete(@NotNull CommandSender sender, @NotNull CrateExpressCommand command, @NotNull String alias, @NotNull String subAlias, @NotNull String @NotNull[] args) {
		return args.length == 1 ? this.onlinePlayers() : null;
	}
}
