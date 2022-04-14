package fr.theoszanto.mc.crateexpress;

import fr.theoszanto.mc.crateexpress.managers.ExportManager;
import fr.theoszanto.mc.crateexpress.managers.I18nManager;
import fr.theoszanto.mc.crateexpress.managers.MoneyManager;
import fr.theoszanto.mc.crateexpress.managers.RewardsManager;
import fr.theoszanto.mc.crateexpress.managers.SpigotManager;
import fr.theoszanto.mc.crateexpress.managers.StorageManager;
import fr.theoszanto.mc.crateexpress.models.CrateConfig;
import fr.theoszanto.mc.crateexpress.models.CrateRegistry;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;

public final class CrateExpress extends JavaPlugin {
	private final @NotNull CrateConfig config = new CrateConfig(this);
	private final @NotNull I18nManager i18n = new I18nManager(this);
	private final @NotNull StorageManager storage = new StorageManager(this);
	private final @NotNull MoneyManager money = new MoneyManager(this);
	private final @NotNull CrateRegistry crates = new CrateRegistry(this);
	private final @NotNull RewardsManager rewards = new RewardsManager(this);
	private final @NotNull ExportManager export = new ExportManager(this);
	private final @NotNull SpigotManager spigot = new SpigotManager(this);

	@Override
	public void onEnable() {
		this.load();
	}

	private void load() {
		this.saveDefaultConfig();
		this.loadConfig();

		// Initializing i18n module
		String locale = this.config.getLocale();
		String messagesFile = "messages/" + locale + ".yml";
		this.saveResource(messagesFile, false);
		this.i18n.loadMessages(new File(this.getDataFolder(), messagesFile));
		for (File additionalMessageFile : this.config.getAdditionalMessageFiles())
			this.i18n.loadMessages(additionalMessageFile);

		// Initializing storage module
		this.storage.loadStorageSource(this.config.getStorageConfig());

		// Initializing money module
		this.money.load(this.config.getMoneyConfig());

		// Initializing crates module
		this.crates.load(this.config.getCratesConfig());

		// Initializing rewards module
		this.rewards.load(this.config.getRewardsConfig());

		// Initializing export module
		this.export.loadExporters(this.config.getExportConfig());

		// Initializing Spigot plugin stuff
		this.spigot.init();
	}

	@Override
	public void onDisable() {
		this.unload();
	}

	private void unload() {
		this.spigot.reset();
		this.export.reset();
		this.rewards.reset();
		this.crates.reset();
		this.money.reset();
		this.storage.resetStorageSource();
		this.i18n.reset();
	}

	public void reload() {
		this.unload();
		this.load();
	}

	private void loadConfig() {
		this.reloadConfig();
		this.config.setRawConfig(this.getConfig());
	}

	public @NotNull String i18n(@NotNull String key, @Nullable Object @NotNull... format) {
		return this.i18n.getMessage(key, format);
	}

	public @NotNull StorageManager storage() {
		return this.storage;
	}

	public @NotNull MoneyManager money() {
		return this.money;
	}

	public @NotNull CrateRegistry crates() {
		return this.crates;
	}

	public @NotNull RewardsManager rewards() {
		return this.rewards;
	}

	public @NotNull ExportManager export() {
		return this.export;
	}

	public @NotNull SpigotManager spigot() {
		return this.spigot;
	}
}
