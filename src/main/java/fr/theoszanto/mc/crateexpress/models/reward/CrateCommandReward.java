package fr.theoszanto.mc.crateexpress.models.reward;

import fr.theoszanto.mc.crateexpress.CrateExpress;
import fr.theoszanto.mc.express.utils.ItemUtils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public class CrateCommandReward extends CrateReward {
	private final @NotNull String command;

	public CrateCommandReward(@NotNull CrateExpress plugin, @NotNull String id, @NotNull ItemStack icon, double weight, @NotNull String command, boolean needInventorySpace) {
		super(plugin, id, "command", icon, weight, needInventorySpace);
		this.command = command;
	}

	@Override
	public void setIcon(@NotNull ItemStack icon) {
		super.setIcon(icon);
	}

	@Override
	public void setPhysicalReward(boolean physicalReward) {
		super.setPhysicalReward(physicalReward);
	}

	@Override
	protected void reward(@NotNull Player player) throws RewardGiveException {
		Bukkit.dispatchCommand(Bukkit.getConsoleSender(), this.formattedCommand(player));
	}

	public @NotNull String getCommand() {
		return this.command;
	}

	private @NotNull String formattedCommand(@NotNull Player player) {
		return this.command.replaceAll("<player>", player.getName())
				.replaceAll("<display>", ItemUtils.COMPONENT_SERIALIZER.serialize(player.displayName()))
				.replaceAll("<uuid>", player.getUniqueId().toString());
	}
}
