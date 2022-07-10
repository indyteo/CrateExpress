package fr.theoszanto.mc.crateexpress.utils;

import fr.theoszanto.mc.crateexpress.CrateExpress;
import fr.theoszanto.mc.crateexpress.managers.ExportManager;
import fr.theoszanto.mc.crateexpress.managers.MoneyManager;
import fr.theoszanto.mc.crateexpress.managers.RewardsManager;
import fr.theoszanto.mc.crateexpress.managers.StorageManager;
import fr.theoszanto.mc.crateexpress.models.CrateRegistry;
import fr.theoszanto.mc.express.ExpressObject;
import fr.theoszanto.mc.express.utils.Logged;
import org.jetbrains.annotations.NotNull;

public abstract class PluginObject extends ExpressObject<CrateExpress> implements Logged {
	public PluginObject(@NotNull CrateExpress plugin) {
		super(plugin);
	}

	public final @NotNull StorageManager storage() {
		return this.plugin.storage();
	}

	public final @NotNull CrateRegistry crates() {
		return this.plugin.crates();
	}

	public final @NotNull RewardsManager rewards() {
		return this.plugin.rewards();
	}

	public final @NotNull MoneyManager money() {
		return this.plugin.money();
	}

	public final @NotNull ExportManager export() {
		return this.plugin.export();
	}
}
