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
import java.util.UUID;
import java.util.function.Consumer;

public class StorageManager extends PluginObject {
	private @NotNull CrateStorage source;
	private final @NotNull Map<@NotNull String, @NotNull Map<@NotNull String, @NotNull CrateRewardStorage<?>>> rewardStorages = new HashMap<>();

	public StorageManager(@NotNull CrateExpress plugin) {
		super(plugin);
		this.source = new NoopCrateStorage(plugin);
	}

	public @NotNull CrateStorage getSource() {
		return this.source;
	}

	public @Nullable CrateRewardStorage<?> getRewardSource(@NotNull String storageType, @NotNull String rewardType) {
		return this.rewardStorages.getOrDefault(storageType, Map.of()).get(rewardType);
	}

	public void runOnStorage(@NotNull Consumer<@NotNull CrateStorage> action) {
		if (this.source.isAsync())
			this.async(() -> action.accept(this.source));
		else
			action.accept(this.source);
	}

	public void migratePlayerData(@NotNull UUID from, @NotNull UUID to) throws IllegalStateException {
		this.source.migrateRewards(from, to);
		this.source.migratePlayerStats(from, to);
	}

	public void clearPlayerData(@NotNull UUID uuid) throws IllegalStateException {
		this.source.clearRewards(uuid);
		this.source.clearPlayerStats(uuid);
	}

	public void resetStorageSource() {
		this.source = new NoopCrateStorage(this.plugin);
		this.rewardStorages.clear();
	}

	public void loadStorageSource(@NotNull CrateConfig.Storage config) throws IllegalStateException {
		if (config.isEmpty())
			return;
		this.source = config.getSource().instanciate();
		config.getRewards().forEach((type, rewards) -> {
			Map<String, CrateRewardStorage<?>> rewardStorages = new HashMap<>();
			for (CrateConfig.SerializedPluginObject reward : rewards) {
				CrateRewardStorage<?> rewardStorage = reward.instanciate();
				rewardStorages.put(rewardStorage.getType(), rewardStorage);
			}
			this.rewardStorages.put(type, rewardStorages);
		});
		this.source.initialize();
	}
}
