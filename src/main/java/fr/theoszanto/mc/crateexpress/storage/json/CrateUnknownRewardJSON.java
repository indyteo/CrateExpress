package fr.theoszanto.mc.crateexpress.storage.json;

import com.google.gson.JsonObject;
import fr.theoszanto.mc.crateexpress.CrateExpress;
import fr.theoszanto.mc.crateexpress.models.reward.CrateUnknownReward;
import org.jetbrains.annotations.NotNull;

public class CrateUnknownRewardJSON extends CrateRewardJSON<CrateUnknownReward> {
	public CrateUnknownRewardJSON(@NotNull CrateExpress plugin) {
		super(plugin, "unknown");
	}

	@Override
	public void serialize(@NotNull CrateUnknownReward reward, @NotNull JsonObject data) {
		data.addProperty("type", reward.getUnknownType()); // Overwrite "unknown" type with the actual unknown type
		if (reward.getData() instanceof JsonObject unknownData)
			data.asMap().putAll(unknownData.asMap());
	}

	@Override
	public @NotNull CrateUnknownReward deserialize(@NotNull JsonObject data, @NotNull String id, double weight) throws IllegalStateException {
		JsonObject unknownData = data.deepCopy();
		unknownData.remove("type");
		unknownData.remove("weight");
		return new CrateUnknownReward(this.plugin, id, weight, require(data, "type").getAsString(), unknownData);
	}
}
