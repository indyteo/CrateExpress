package fr.theoszanto.mc.crateexpress.storage.yaml;

import fr.theoszanto.mc.crateexpress.CrateExpress;
import fr.theoszanto.mc.crateexpress.models.reward.CrateCommandReward;
import fr.theoszanto.mc.crateexpress.utils.ItemUtils;
import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;

public class CrateCommandRewardYML extends CrateRewardYML<CrateCommandReward> {
	public CrateCommandRewardYML(@NotNull CrateExpress plugin) {
		super(plugin, "command");
	}

	@Override
	public void serialize(@NotNull CrateCommandReward reward, @NotNull ConfigurationSection data) {
		data.set("command", reward.getCommand());
		data.set("icon", ItemUtils.toString(reward.getIcon()));
		data.set("need-inventory-space", reward.isPhysicalReward());
	}

	@Override
	public @NotNull CrateCommandReward deserialize(@NotNull ConfigurationSection data, int weight) throws IllegalStateException {
		return new CrateCommandReward(this.plugin,
				CrateRewardYML.requireItem(data, "icon"),
				weight,
				CrateRewardYML.requireString(data, "command"),
				data.getBoolean("need-inventory-space", false));
	}
}
