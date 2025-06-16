package fr.theoszanto.mc.crateexpress.storage.json;

import com.google.gson.JsonObject;
import fr.theoszanto.mc.crateexpress.CrateExpress;
import fr.theoszanto.mc.crateexpress.models.reward.CrateKeyReward;
import org.jetbrains.annotations.NotNull;

public class CrateKeyRewardJSON extends CrateRewardJSON<CrateKeyReward> {
	public CrateKeyRewardJSON(@NotNull CrateExpress plugin) {
		super(plugin, "key");
	}

	@Override
	public void serialize(@NotNull CrateKeyReward reward, @NotNull JsonObject data) {
		data.addProperty("key", reward.getKey());
		data.addProperty("amount", reward.getAmount());
	}

	@Override
	public @NotNull CrateKeyReward deserialize(@NotNull JsonObject data, @NotNull String id, double weight) throws IllegalStateException {
		int amount = require(data, "amount").getAsInt();
		if (amount < 0 || amount > 64)
			throw new IllegalStateException("Missing (or invalid) key amount for crate key reward");
		return new CrateKeyReward(this.plugin, id, weight, require(data, "key").getAsString(), amount);
	}
}
