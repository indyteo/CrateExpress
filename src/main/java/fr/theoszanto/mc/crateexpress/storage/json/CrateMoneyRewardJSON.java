package fr.theoszanto.mc.crateexpress.storage.json;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import fr.theoszanto.mc.crateexpress.CrateExpress;
import fr.theoszanto.mc.crateexpress.models.reward.CrateMoneyReward;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public class CrateMoneyRewardJSON extends CrateRewardJSON<CrateMoneyReward> {
	public CrateMoneyRewardJSON(@NotNull CrateExpress plugin) {
		super(plugin, "money");
	}

	@Override
	public void serialize(@NotNull CrateMoneyReward reward, @NotNull JsonObject data) {
		data.addProperty("min", reward.getMin());
		data.addProperty("max", reward.getMax());
	}

	@Override
	public @NotNull CrateMoneyReward deserialize(@NotNull JsonObject data, @NotNull String id, double weight) throws IllegalStateException {
		// Legacy money reward format
		Optional<Double> amount = optional(data, "amount").map(JsonElement::getAsDouble);
		if (amount.isPresent())
			return new CrateMoneyReward(this.plugin, id, weight, amount.get());

		double min = require(data, "min").getAsDouble();
		double max = require(data, "max").getAsDouble();
		return new CrateMoneyReward(this.plugin, id, weight, min, max);
	}
}
