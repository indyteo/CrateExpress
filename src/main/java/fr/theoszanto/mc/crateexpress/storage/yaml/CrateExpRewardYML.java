package fr.theoszanto.mc.crateexpress.storage.yaml;

import fr.theoszanto.mc.crateexpress.CrateExpress;
import fr.theoszanto.mc.crateexpress.models.reward.CrateExpReward;
import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;

public class CrateExpRewardYML extends CrateRewardYML<CrateExpReward> {
	public CrateExpRewardYML(@NotNull CrateExpress plugin) {
		super(plugin, "exp");
	}

	@Override
	public void serialize(@NotNull CrateExpReward reward, @NotNull ConfigurationSection data) {
		data.set("exp", reward.getExp());
		data.set("levels", reward.isLevels());
	}

	@Override
	public @NotNull CrateExpReward deserialize(@NotNull ConfigurationSection data, @NotNull String id, double weight) throws IllegalStateException {
		int exp = data.getInt("exp", 0);
		if (exp <= 0)
			throw new IllegalStateException("Missing or invalid exp value for crate exp reward");
		return new CrateExpReward(this.plugin, id, weight, exp, data.getBoolean("levels", true));
	}
}
