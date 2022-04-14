package fr.theoszanto.mc.crateexpress.models.reward;

import fr.theoszanto.mc.crateexpress.CrateExpress;
import fr.theoszanto.mc.crateexpress.models.Crate;
import fr.theoszanto.mc.crateexpress.models.gui.reward.CrateRewardGUI;
import fr.theoszanto.mc.crateexpress.utils.PluginObject;
import org.bukkit.Material;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Collections;

public class CrateRewardType extends PluginObject {
	private final @NotNull String type;
	private final @NotNull Material icon;
	private final @NotNull String guiClass;

	public CrateRewardType(@NotNull CrateExpress plugin, @NotNull String type, @NotNull Material icon, @NotNull String guiClass) {
		super(plugin);
		this.type = type;
		this.icon = icon;
		this.guiClass = guiClass;
	}

	public @NotNull String getType() {
		return this.type;
	}

	public @NotNull Material getIcon() {
		return this.icon;
	}

	public @NotNull CrateRewardGUI<?> createNewGUI(@NotNull Crate crate) {
		return (CrateRewardGUI<?>) this.instanciate(this.guiClass, Collections.singletonList(crate));
	}

	public @NotNull CrateRewardGUI<?> createNewGUI(@NotNull Crate crate, CrateReward reward, int slot) {
		return (CrateRewardGUI<?>) this.instanciate(this.guiClass, Arrays.asList(crate, reward, slot));
	}
}
