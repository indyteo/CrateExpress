package fr.theoszanto.mc.crateexpress.models.reward;

import fr.theoszanto.mc.crateexpress.CrateExpress;
import fr.theoszanto.mc.crateexpress.models.Crate;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class CrateOtherReward extends CrateReward {
	private final @NotNull String other;
	private boolean random;

	public CrateOtherReward(@NotNull CrateExpress plugin, @NotNull ItemStack icon, double weight, @NotNull String other, boolean random) {
		super(plugin, "other", icon, weight, true);
		this.other = other;
		this.random = random;
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
		if (this.random)
			other.open(player);
		else
			for (CrateReward reward : other.getRewards())
				reward.giveRewardTo(player);
	}

	@Override
	public void setIcon(@NotNull ItemStack icon) {
		super.setIcon(icon);
	}

	public @NotNull String getOther() {
		return this.other;
	}

	public boolean isRandom() {
		return this.random;
	}

	public void setRandom(boolean random) {
		this.random = random;
	}
}
