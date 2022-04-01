package fr.theoszanto.mc.crateexpress;

import fr.theoszanto.mc.crateexpress.managers.ExportManager;
import fr.theoszanto.mc.crateexpress.managers.I18nManager;
import fr.theoszanto.mc.crateexpress.managers.MoneyManager;
import fr.theoszanto.mc.crateexpress.managers.SpigotManager;
import fr.theoszanto.mc.crateexpress.managers.StorageManager;
import fr.theoszanto.mc.crateexpress.models.CrateRegistry;
import fr.theoszanto.mc.crateexpress.storage.CrateStorage;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;

public final class CrateExpress extends JavaPlugin {
	private final @NotNull I18nManager i18n = new I18nManager(this);
	private final @NotNull StorageManager storage = new StorageManager(this);
	private final @NotNull MoneyManager money = new MoneyManager(this);
	private final @NotNull CrateRegistry crates = new CrateRegistry(this);
	private final @NotNull ExportManager export = new ExportManager(this);
	private final @NotNull SpigotManager spigot = new SpigotManager(this);

	@Override
	public void onEnable() {
		this.load();
	}

	private void load() {
		this.saveDefaultConfig();
		FileConfiguration config = this.getConfig();

		// Initializing i18n module
		String locale = config.getString("locale", "en");
		String messagesFile = "messages/" + locale + ".yml";
		this.saveResource(messagesFile, false);
		this.i18n.loadMessages(new File(this.getDataFolder(), messagesFile));

		// Initializing storage module
		ConfigurationSection storageConfig = config.getConfigurationSection("storage");
		if (storageConfig != null)
			this.storage.loadStorageSource(storageConfig);

		// Initializing money module
		ConfigurationSection moneyConfig = config.getConfigurationSection("money");
		if (moneyConfig != null)
			this.money.load(moneyConfig);

		// Initializing crates module
		ConfigurationSection cratesConfig = config.getConfigurationSection("crates");
		if (cratesConfig == null)
			cratesConfig = config.createSection("crates");
		this.crates.load(cratesConfig);

		// Initializing export module
		ConfigurationSection exportConfig = config.getConfigurationSection("export");
		if (exportConfig != null)
			this.export.loadExporters(exportConfig);

		// Initializing Spigot plugin stuff
		this.spigot.init();
	}

	@Override
	public void onDisable() {
		this.unload();
	}

	private void unload() {
		this.i18n.reset();
		this.storage.resetStorageSource();
		this.money.reset();
		this.crates.reset();
		this.export.reset();
		this.spigot.reset();
	}

	public void reload() {
		this.unload();
		this.reloadConfig();
		this.load();
	}

	public @NotNull String i18n(@NotNull String key, @Nullable Object @NotNull... format) {
		return this.i18n.getMessage(key, format);
	}

	public @NotNull CrateStorage storage() {
		return this.storage.getStorage();
	}

	public @NotNull MoneyManager money() {
		return this.money;
	}

	public @NotNull CrateRegistry crates() {
		return this.crates;
	}

	public @NotNull ExportManager export() {
		return this.export;
	}

	public @NotNull SpigotManager spigot() {
		return this.spigot;
	}
}
