package fr.theoszanto.mc.crateexpress.storage.yaml;

import fr.theoszanto.mc.crateexpress.CrateExpress;
import fr.theoszanto.mc.crateexpress.models.reward.CrateUnknownReward;
import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

public class CrateUnknownRewardYML extends CrateRewardYML<CrateUnknownReward> {
	public CrateUnknownRewardYML(@NotNull CrateExpress plugin) {
		super(plugin, "unknown");
	}

	@Override
	public void serialize(@NotNull CrateUnknownReward reward, @NotNull ConfigurationSection data) {
		data.set("type", reward.getUnknownType()); // Overwrite "unknown" type with the actual unknown type
		Object unknownData = reward.getData();
		if (unknownData instanceof Map) {
			Map<?, ?> unknownDataMap = (Map<?, ?>) unknownData;
			unknownDataMap.forEach((key, value) -> {
				if (key instanceof String)
					data.set((String) key, value);
			});
		}
	}

	@Override
	public @NotNull CrateUnknownReward deserialize(@NotNull ConfigurationSection data, double weight) throws IllegalStateException {
		Map<String, Object> unknownData = new HashMap<>();
		for (String key : data.getKeys(true)) {
			if (key.equals("type") || key.equals("weight"))
				continue;
			Object value = data.get(key);
			if (!(value instanceof ConfigurationSection))
				unknownData.put(key, value);
		}
		return new CrateUnknownReward(this.plugin, weight, CrateRewardYML.requireString(data, "type"), unknownData);
	}
}
