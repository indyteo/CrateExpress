package fr.theoszanto.mc.crateexpress.storage.json;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import fr.theoszanto.mc.crateexpress.CrateExpress;
import fr.theoszanto.mc.crateexpress.models.reward.CrateExpReward;
import org.jetbrains.annotations.NotNull;

public class CrateExpRewardJSON extends CrateRewardJSON<CrateExpReward> {
	public CrateExpRewardJSON(@NotNull CrateExpress plugin) {
		super(plugin, "exp");
	}

	@Override
	public void serialize(@NotNull CrateExpReward reward, @NotNull JsonObject data) {
		data.addProperty("exp", reward.getExp());
		data.addProperty("levels", reward.isLevels());
	}

	@Override
	public @NotNull CrateExpReward deserialize(@NotNull JsonObject data, @NotNull String id, double weight) throws IllegalStateException {
		return new CrateExpReward(this.plugin, id, weight, require(data, "exp").getAsInt(), getOrDefault(data, "levels", JsonElement::getAsBoolean, true));
	}
}
