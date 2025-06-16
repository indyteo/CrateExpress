package fr.theoszanto.mc.crateexpress.storage.json;

import com.google.gson.JsonObject;
import fr.theoszanto.mc.crateexpress.CrateExpress;
import fr.theoszanto.mc.crateexpress.models.reward.CrateOtherReward;
import fr.theoszanto.mc.express.utils.ItemUtils;
import org.jetbrains.annotations.NotNull;

public class CrateOtherRewardJSON extends CrateRewardJSON<CrateOtherReward> {
	public CrateOtherRewardJSON(@NotNull CrateExpress plugin) {
		super(plugin, "other");
	}

	@Override
	public void serialize(@NotNull CrateOtherReward reward, @NotNull JsonObject data) {
		data.addProperty("crate", reward.getOther());
		data.addProperty("icon", ItemUtils.toString(reward.getIcon()));
	}

	@Override
	public @NotNull CrateOtherReward deserialize(@NotNull JsonObject data, @NotNull String id, double weight) throws IllegalStateException {
		return new CrateOtherReward(this.plugin,
				id,
				requireItem(data, "icon"),
				weight,
				require(data, "crate").getAsString());
	}
}
