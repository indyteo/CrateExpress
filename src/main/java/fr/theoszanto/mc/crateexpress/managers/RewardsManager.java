package fr.theoszanto.mc.crateexpress.managers;

import fr.theoszanto.mc.crateexpress.CrateExpress;
import fr.theoszanto.mc.crateexpress.models.CrateConfig;
import fr.theoszanto.mc.crateexpress.models.reward.CrateRewardType;
import fr.theoszanto.mc.crateexpress.utils.PluginObject;
import org.bukkit.Material;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

public class RewardsManager extends PluginObject {
	private final @NotNull SortedMap<@NotNull String, @NotNull CrateRewardType> rewardsType = new TreeMap<>();

	public RewardsManager(@NotNull CrateExpress plugin) {
		super(plugin);
	}

	public void load(@NotNull CrateConfig.Rewards config) {
		if (config.isEmpty())
			return;
		config.getRewardsGUI().forEach((type, gui) -> {
			try {
				this.rewardsType.put(type, new CrateRewardType(this.plugin, type, Material.valueOf(gui.getGuiIcon().toUpperCase()), gui.getGuiClass()));
			} catch (IllegalArgumentException e) {
				throw new IllegalStateException("Invalid reward type: " + type);
			}
		});
	}

	public void reset() {
		this.rewardsType.clear();
	}

	public @NotNull CrateRewardType getRewardType(@NotNull String type) {
		CrateRewardType rewardType = this.rewardsType.get(type);
		if (rewardType == null)
			throw new IllegalArgumentException("Unknown reward type: " + type);
		return rewardType;
	}

	public @NotNull List<@NotNull CrateRewardType> getRewardsType() {
		return new ArrayList<>(this.rewardsType.values());
	}
}
