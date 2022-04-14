package fr.theoszanto.mc.crateexpress.storage;

import fr.theoszanto.mc.crateexpress.CrateExpress;
import fr.theoszanto.mc.crateexpress.models.reward.CrateReward;
import fr.theoszanto.mc.crateexpress.utils.PluginObject;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class CrateRewardStorage<T extends CrateReward> extends PluginObject {
	private final @NotNull String type;

	public CrateRewardStorage(@NotNull CrateExpress plugin, @NotNull String type) {
		super(plugin);
		this.type = type;
	}

	public abstract void serializeReward(@NotNull CrateReward reward, @Nullable Object @NotNull... arguments);

	public abstract @NotNull T deserializeReward(@Nullable Object @NotNull... arguments) throws IllegalStateException;

	public final @NotNull String getType() {
		return this.type;
	}
}
