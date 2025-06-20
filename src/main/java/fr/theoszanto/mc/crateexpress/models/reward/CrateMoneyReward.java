package fr.theoszanto.mc.crateexpress.models.reward;

import fr.theoszanto.mc.crateexpress.CrateExpress;
import fr.theoszanto.mc.express.utils.MathUtils;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class CrateMoneyReward extends CrateReward implements CrateRandomReward {
	private double min;
	private double max;

	public CrateMoneyReward(@NotNull CrateExpress plugin, @NotNull String id, double weight, double amount) {
		this(plugin, id, weight, amount, amount);
	}

	public CrateMoneyReward(@NotNull CrateExpress plugin, @NotNull String id, double weight, double min, double max) {
		super(plugin, id, "money", plugin.money().getIcon(min, max), weight, plugin.money().isPhysical());
		this.min = min;
		this.max = max;
	}

	@Override
	protected void reward(@NotNull Player player) throws RewardGiveException {
		try {
			this.money().giveMoney(player, this.getAmount(), this);
		} catch (IllegalStateException e) {
			throw new RewardGiveException(e.getMessage());
		}
	}

	@Override
	public @NotNull String describe() {
		return this.isRandom() ? super.describe() : this.money().formatMoney(this.min);
	}

	public double getMin() {
		return this.min;
	}

	public void setMin(double min) {
		this.min = min;
		this.setIcon(this.money().getIcon(min, this.max));
	}

	public double getMax() {
		return this.max;
	}

	public void setMax(double max) {
		this.max = max;
		this.setIcon(this.money().getIcon(this.min, max));
	}

	public boolean isRandom() {
		return this.min != this.max;
	}

	public double getAmount() {
		return this.isRandom() ? MathUtils.random(this.min, this.max) : this.min;
	}

	public @NotNull CrateMoneyReward fixed() {
		if (this.isRandom())
			return new CrateMoneyReward(this.plugin, this.getId(), this.getWeight(), this.getAmount());
		throw new IllegalStateException("Money reward is not random");
	}
}
