package fr.theoszanto.mc.crateexpress.managers;

import fr.theoszanto.mc.crateexpress.CrateExpress;
import fr.theoszanto.mc.crateexpress.storage.CrateStorage;
import fr.theoszanto.mc.crateexpress.storage.NoopCrateStorage;
import fr.theoszanto.mc.crateexpress.utils.PluginObject;
import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;

public class StorageManager extends PluginObject {
	private @NotNull CrateStorage storage;

	public StorageManager(@NotNull CrateExpress plugin) {
		super(plugin);
		this.storage = new NoopCrateStorage(plugin);
	}

	public @NotNull CrateStorage getStorage() {
		return this.storage;
	}

	public void resetStorageSource() {
		this.storage = new NoopCrateStorage(this.plugin);
	}

	public void loadStorageSource(@NotNull ConfigurationSection config) throws IllegalStateException {
		String className = config.getString("class", null);
		if (className == null)
			throw new IllegalStateException("Missing storage class name in config");
		try {
			this.storage = (CrateStorage) this.instanciate(className, config.getList("options"));
		} catch (IllegalArgumentException | ClassCastException e) {
			throw new IllegalStateException("Invalid storage class: " + className, e);
		}
	}
}
