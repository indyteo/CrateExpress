package fr.theoszanto.mc.crateexpress.managers;

import fr.theoszanto.mc.crateexpress.CrateExpress;
import fr.theoszanto.mc.crateexpress.models.CrateConfig;
import fr.theoszanto.mc.crateexpress.storage.CrateRewardStorage;
import fr.theoszanto.mc.crateexpress.storage.CrateStorage;
import fr.theoszanto.mc.crateexpress.storage.NoopCrateStorage;
import fr.theoszanto.mc.crateexpress.utils.PluginObject;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

public class StorageManager extends PluginObject {
	private @NotNull CrateStorage source;
	private final @NotNull Map<@NotNull String, @NotNull CrateRewardStorage<?>> rewardStorages = new HashMap<>();

	public StorageManager(@NotNull CrateExpress plugin) {
		super(plugin);
		this.source = new NoopCrateStorage(plugin);
	}

	public @NotNull CrateStorage getSource() {
		return this.source;
	}

	public @Nullable CrateRewardStorage<?> getRewardSource(@NotNull String type) {
		return this.rewardStorages.get(type);
	}

	public void resetStorageSource() {
		this.source = new NoopCrateStorage(this.plugin);
		this.rewardStorages.clear();
	}

	public void loadStorageSource(@NotNull CrateConfig.Storage config) throws IllegalStateException {
		if (config.isEmpty())
			return;
		this.source = config.getSource().instanciate();
		for (CrateConfig.SerializedPluginObject reward : config.getRewards()) {
			CrateRewardStorage<?> rewardStorage = reward.instanciate();
			this.rewardStorages.put(rewardStorage.getType(), rewardStorage);
		}
	}
}
