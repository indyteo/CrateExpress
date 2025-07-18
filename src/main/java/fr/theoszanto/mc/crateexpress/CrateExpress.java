package fr.theoszanto.mc.crateexpress;

import fr.theoszanto.mc.crateexpress.events.CrateExpressLoadEvent;
import fr.theoszanto.mc.crateexpress.managers.ExportManager;
import fr.theoszanto.mc.crateexpress.managers.MoneyManager;
import fr.theoszanto.mc.crateexpress.managers.RewardsManager;
import fr.theoszanto.mc.crateexpress.managers.StatsManager;
import fr.theoszanto.mc.crateexpress.managers.StorageManager;
import fr.theoszanto.mc.crateexpress.models.CrateConfig;
import fr.theoszanto.mc.crateexpress.models.CrateRegistry;
import fr.theoszanto.mc.crateexpress.storage.CrateStorage;
import fr.theoszanto.mc.express.ExpressPlugin;
import org.bukkit.configuration.file.FileConfiguration;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.List;
import java.util.function.Consumer;

public final class CrateExpress extends ExpressPlugin<CrateExpress> {
	private final @NotNull CrateConfig config = new CrateConfig(this);
	private final @NotNull StorageManager storage = new StorageManager(this);
	private final @NotNull MoneyManager money = new MoneyManager(this);
	private final @NotNull CrateRegistry crates = new CrateRegistry(this);
	private final @NotNull RewardsManager rewards = new RewardsManager(this);
	private final @NotNull StatsManager stats = new StatsManager(this);
	private final @NotNull ExportManager export = new ExportManager(this);
	private boolean loadedOnce = false;

	public CrateExpress() {
		super("fr.theoszanto.mc.crateexpress");
	}

	@Override
	public void loadConfig(@NotNull FileConfiguration config) {
		this.config.setRawConfig(config);
	}

	@Override
	public @NotNull String getLocale() {
		return this.config.getLocale();
	}

	@Override
	protected @NotNull List<@NotNull File> getAdditionalMessageFiles() {
		return this.config.getAdditionalMessageFiles();
	}

	@Override
	protected void init() {
		// Initializing storage module
		this.storage.loadStorageSource(this.config.getStorageConfig());

		// Initializing money module
		this.money.load(this.config.getMoneyConfig());

		// Initializing crates module
		this.crates.load(this.config.getCratesConfig());

		// Initializing rewards module
		this.rewards.load(this.config.getRewardsConfig());

		// Initializing stats module
		this.stats.load();

		// Initializing export module
		this.export.loadExporters(this.config.getExportConfig());

		this.spigot().event(new CrateExpressLoadEvent(this.loadedOnce));
		this.loadedOnce = true;
	}

	@Override
	protected void reset() {
		this.export.reset();
		this.stats.reset();
		this.rewards.reset();
		this.crates.reset();
		this.money.reset();
		this.storage.resetStorageSource();
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

	public @NotNull StatsManager stats() {
		return this.stats;
	}

	public @NotNull ExportManager export() {
		return this.export;
	}

	public void store(@NotNull Consumer<@NotNull CrateStorage> action) {
		this.storage.runOnStorage(action);
	}
}
