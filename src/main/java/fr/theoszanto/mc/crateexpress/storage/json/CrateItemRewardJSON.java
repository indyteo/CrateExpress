package fr.theoszanto.mc.crateexpress.storage.json;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import fr.theoszanto.mc.crateexpress.CrateExpress;
import fr.theoszanto.mc.crateexpress.models.reward.CrateItemReward;
import fr.theoszanto.mc.express.utils.ItemUtils;
import org.jetbrains.annotations.NotNull;

public class CrateItemRewardJSON extends CrateRewardJSON<CrateItemReward> {
	public CrateItemRewardJSON(@NotNull CrateExpress plugin) {
		super(plugin, "item");
	}

	@Override
	public void serialize(@NotNull CrateItemReward reward, @NotNull JsonObject data) {
		data.addProperty("item", ItemUtils.toString(reward.getItem()));
		if (reward.getAmount() > 0)
			data.addProperty("amount", reward.getAmount());
	}

	@Override
	public @NotNull CrateItemReward deserialize(@NotNull JsonObject data, @NotNull String id, double weight) throws IllegalStateException {
		return new CrateItemReward(this.plugin, id, weight, requireItem(data, "item"), getOrDefault(data, "amount", JsonElement::getAsInt, -1));
	}
}
