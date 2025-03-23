package fr.theoszanto.mc.crateexpress.storage.yaml;

import fr.theoszanto.mc.crateexpress.CrateExpress;
import fr.theoszanto.mc.crateexpress.models.reward.CrateMoneyReward;
import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;

public class CrateMoneyRewardYML extends CrateRewardYML<CrateMoneyReward> {
	public CrateMoneyRewardYML(@NotNull CrateExpress plugin) {
		super(plugin, "money");
	}

	@Override
	public void serialize(@NotNull CrateMoneyReward reward, @NotNull ConfigurationSection data) {
		data.set("min", reward.getMin());
		data.set("max", reward.getMax());
	}

	@Override
	public @NotNull CrateMoneyReward deserialize(@NotNull ConfigurationSection data, @NotNull String id, double weight) throws IllegalStateException {
		// Legacy money reward format
		double amount = data.getDouble("amount", Double.NaN);
		if (!Double.isNaN(amount))
			return new CrateMoneyReward(this.plugin, id, weight, amount);

		double min = data.getDouble("min", Double.NaN);
		if (Double.isNaN(min))
			throw new IllegalStateException("Missing min amount value for crate money reward");
		double max = data.getDouble("max", Double.NaN);
		if (Double.isNaN(max))
			throw new IllegalStateException("Missing max amount value for crate money reward");
		return new CrateMoneyReward(this.plugin, id, weight, min, max);
	}
}
