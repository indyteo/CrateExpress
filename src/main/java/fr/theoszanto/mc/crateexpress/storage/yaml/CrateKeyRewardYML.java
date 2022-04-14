package fr.theoszanto.mc.crateexpress.storage.yaml;

import fr.theoszanto.mc.crateexpress.CrateExpress;
import fr.theoszanto.mc.crateexpress.models.reward.CrateKeyReward;
import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;

public class CrateKeyRewardYML extends CrateRewardYML<CrateKeyReward> {
	public CrateKeyRewardYML(@NotNull CrateExpress plugin) {
		super(plugin, "key");
	}

	@Override
	public void serialize(@NotNull CrateKeyReward reward, @NotNull ConfigurationSection data) {
		data.set("key", reward.getKey());
		data.set("amount", reward.getAmount());
	}

	@Override
	public @NotNull CrateKeyReward deserialize(@NotNull ConfigurationSection data, int weight) throws IllegalStateException {
		int amount = data.getInt("amount", -1);
		if (amount < 0 || amount > 64)
			throw new IllegalStateException("Missing (or invalid) key amount for crate key reward");
		return new CrateKeyReward(this.plugin, weight, CrateRewardYML.requireString(data, "key"), amount);
	}
}
