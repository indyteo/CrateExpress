package fr.theoszanto.mc.crateexpress.storage.yaml;

import fr.theoszanto.mc.crateexpress.CrateExpress;
import fr.theoszanto.mc.crateexpress.models.reward.CrateItemReward;
import fr.theoszanto.mc.express.utils.ItemUtils;
import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;

public class CrateItemRewardYML extends CrateRewardYML<CrateItemReward> {
	public CrateItemRewardYML(@NotNull CrateExpress plugin) {
		super(plugin, "item");
	}

	@Override
	public void serialize(@NotNull CrateItemReward reward, @NotNull ConfigurationSection data) {
		data.set("item", ItemUtils.toString(reward.getItem()));
	}

	@Override
	public @NotNull CrateItemReward deserialize(@NotNull ConfigurationSection data, int weight) throws IllegalStateException {
		return new CrateItemReward(this.plugin, weight, CrateRewardYML.requireItem(data, "item"));
	}
}
