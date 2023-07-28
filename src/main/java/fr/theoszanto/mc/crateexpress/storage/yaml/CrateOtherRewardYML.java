package fr.theoszanto.mc.crateexpress.storage.yaml;

import fr.theoszanto.mc.crateexpress.CrateExpress;
import fr.theoszanto.mc.crateexpress.models.reward.CrateOtherReward;
import fr.theoszanto.mc.express.utils.ItemUtils;
import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;

public class CrateOtherRewardYML extends CrateRewardYML<CrateOtherReward> {
	public CrateOtherRewardYML(@NotNull CrateExpress plugin) {
		super(plugin, "other");
	}

	@Override
	public void serialize(@NotNull CrateOtherReward reward, @NotNull ConfigurationSection data) {
		data.set("crate", reward.getOther());
		data.set("icon", ItemUtils.toString(reward.getIcon()));
		data.set("random", reward.isRandom());
	}

	@Override
	public @NotNull CrateOtherReward deserialize(@NotNull ConfigurationSection data, double weight) throws IllegalStateException {
		return new CrateOtherReward(this.plugin,
				CrateRewardYML.requireItem(data, "icon"),
				weight,
				CrateRewardYML.requireString(data, "crate"),
				data.getBoolean("random", false));
	}
}
