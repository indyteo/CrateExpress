package fr.theoszanto.mc.crateexpress.commands.subcommands;

import fr.theoszanto.mc.crateexpress.CrateExpress;
import fr.theoszanto.mc.crateexpress.commands.CrateExpressCommand;
import fr.theoszanto.mc.crateexpress.models.reward.ClaimableReward;
import fr.theoszanto.mc.crateexpress.models.gui.CrateClaimGUI;
import fr.theoszanto.mc.crateexpress.utils.CratePermission;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class CrateExpressClaimSubCommand extends CrateExpressSubCommand {
	public CrateExpressClaimSubCommand(@NotNull CrateExpress plugin) {
		super(plugin, "claim", "key");
	}

	@Override
	public boolean canExecute(@NotNull CommandSender sender, @NotNull CrateExpressCommand command, @NotNull String alias, @NotNull String subAlias, @NotNull String @NotNull[] args) {
		return sender instanceof Player && sender.hasPermission(CratePermission.Command.CLAIM);
	}

	@Override
	public boolean onCommand(@NotNull CommandSender sender, @NotNull CrateExpressCommand command, @NotNull String alias, @NotNull String subAlias, @NotNull String @NotNull[] args) {
		if (args.length != 0)
			return false;
		Player player = (Player) sender;
		this.async(() -> {
			List<ClaimableReward> playerRewards = this.storage().getSource().listRewards(player);
			this.run(() -> new CrateClaimGUI(this.plugin, playerRewards).showToPlayer(player));
		});
		return true;
	}

	@Override
	public @Nullable List<@NotNull String> tabComplete(@NotNull CommandSender sender, @NotNull CrateExpressCommand command, @NotNull String alias, @NotNull String subAlias, @NotNull String @NotNull[] args) {
		return null;
	}
}
