package fr.theoszanto.mc.crateexpress.models.reward;

import fr.theoszanto.mc.crateexpress.CrateExpress;
import fr.theoszanto.mc.crateexpress.models.Crate;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class CrateOtherReward extends CrateReward {
	private final @NotNull String other;

	public CrateOtherReward(@NotNull CrateExpress plugin, @NotNull String id, @NotNull ItemStack icon, double weight, @NotNull String other) {
		super(plugin, id, "other", icon, weight, true);
		this.other = other;
	}

	private @Nullable Crate fetchOtherCrate() {
		try {
			return this.crates().get(this.other);
		} catch (IllegalArgumentException e) {
			return null;
		}
	}

	@Override
	protected void reward(@NotNull Player player) throws RewardGiveException {
		Crate other = this.fetchOtherCrate();
		if (other == null)
			throw new RewardGiveException("Unknown crate: " + this.other);
		other.open(player);
	}

	@Override
	public void setIcon(@NotNull ItemStack icon) {
		super.setIcon(icon);
	}

	public @NotNull String getOther() {
		return this.other;
	}
}
