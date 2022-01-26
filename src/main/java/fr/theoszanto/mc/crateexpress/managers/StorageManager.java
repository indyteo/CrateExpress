package fr.theoszanto.mc.crateexpress.managers;

import fr.theoszanto.mc.crateexpress.CrateExpress;
import fr.theoszanto.mc.crateexpress.storage.NoopStorage;
import fr.theoszanto.mc.crateexpress.storage.Storage;
import fr.theoszanto.mc.crateexpress.utils.PluginObject;
import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;

public class StorageManager extends PluginObject {
	private @NotNull Storage storage;

	public StorageManager(@NotNull CrateExpress plugin) {
		super(plugin);
		this.storage = new NoopStorage(plugin);
	}

	public @NotNull Storage getStorage() {
		return this.storage;
	}

	public void resetStorageSource() {
		this.storage = new NoopStorage(this.plugin);
	}

	@SuppressWarnings("unchecked")
	public void loadStorageSource(@NotNull ConfigurationSection config) throws IllegalStateException {
		String className = config.getString("class", null);
		if (className == null)
			throw new IllegalStateException("Missing class name in config");
		Class<? extends Storage> clazz;
		try {
			clazz = (Class<? extends Storage>) Class.forName(className);
		} catch (ClassNotFoundException e) {
			throw new IllegalStateException("Unknown storage class: " + className, e);
		} catch (ClassCastException e) {
			throw new IllegalStateException("Invalid storage class: " + className, e);
		}
		List<?> options = config.getList("options");
		if (options == null)
			options = Collections.emptyList();
		Object[] params = new Object[options.size() + 1];
		Class<?>[] paramsTypes = new Class<?>[params.length];
		params[0] = this.plugin;
		paramsTypes[0] = this.plugin.getClass();
		int i = 0;
		while (i < options.size()) {
			Object option = options.get(i++);
			params[i] = option;
			paramsTypes[i] = option.getClass();
		}
		try {
			this.storage = clazz.getDeclaredConstructor(paramsTypes).newInstance(params);
		} catch (ReflectiveOperationException e) {
			throw new IllegalStateException("Invalid storage class: " + className, e);
		}
	}
}
