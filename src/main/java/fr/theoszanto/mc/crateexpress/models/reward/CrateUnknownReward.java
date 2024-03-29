package fr.theoszanto.mc.crateexpress.models.reward;

import fr.theoszanto.mc.crateexpress.CrateExpress;
import fr.theoszanto.mc.express.utils.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class CrateUnknownReward extends CrateReward {
	private final @NotNull String type;
	private final @NotNull Object data;

	public CrateUnknownReward(@NotNull CrateExpress plugin, @NotNull String id, double weight, @NotNull String type, @NotNull Object data) {
		super(plugin, id, "unknown", new ItemBuilder(Material.BARRIER, 1, plugin.i18n("crate.reward.unknown", "type", type)).build(), weight, false);
		this.type = type;
		this.data = data;
	}

	@Override
	protected void reward(@NotNull Player player) throws RewardGiveException {
		throw new RewardGiveException("Unknown reward type: " + this.type);
	}

	@Override
	public @NotNull String describe() {
		return this.i18n("crate.reward.unknown", "type", type);
	}

	public @NotNull String getUnknownType() {
		return this.type;
	}

	public @NotNull Object getData() {
		return this.data;
	}
}
