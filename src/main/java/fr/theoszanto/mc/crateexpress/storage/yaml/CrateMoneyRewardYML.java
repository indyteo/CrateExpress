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
		data.set("amount", reward.getAmount());
	}

	@Override
	public @NotNull CrateMoneyReward deserialize(@NotNull ConfigurationSection data, double weight) throws IllegalStateException {
		double amount = data.getDouble("amount", Double.NaN);
		if (Double.isNaN(amount))
			throw new IllegalStateException("Missing amount value for crate money reward");
		return new CrateMoneyReward(this.plugin, weight, amount);
	}
}
