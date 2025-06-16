package fr.theoszanto.mc.crateexpress.storage.json;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import fr.theoszanto.mc.crateexpress.CrateExpress;
import fr.theoszanto.mc.crateexpress.models.reward.CrateCommandReward;
import fr.theoszanto.mc.express.utils.ItemUtils;
import org.jetbrains.annotations.NotNull;

public class CrateCommandRewardJSON extends CrateRewardJSON<CrateCommandReward> {
	public CrateCommandRewardJSON(@NotNull CrateExpress plugin) {
		super(plugin, "command");
	}

	@Override
	public void serialize(@NotNull CrateCommandReward reward, @NotNull JsonObject data) {
		data.addProperty("command", reward.getCommand());
		data.addProperty("icon", ItemUtils.toString(reward.getIcon()));
		data.addProperty("need-inventory-space", reward.isPhysicalReward());
	}

	@Override
	public @NotNull CrateCommandReward deserialize(@NotNull JsonObject data, @NotNull String id, double weight) throws IllegalStateException {
		return new CrateCommandReward(this.plugin,
				id,
				requireItem(data, "icon"),
				weight,
				require(data, "command").getAsString(),
				getOrDefault(data, "need-inventory-space", JsonElement::getAsBoolean, false));
	}
}
