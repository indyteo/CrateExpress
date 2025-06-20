package fr.theoszanto.mc.crateexpress.models.reward;

import fr.theoszanto.mc.crateexpress.CrateExpress;
import fr.theoszanto.mc.crateexpress.utils.PluginObject;
import fr.theoszanto.mc.express.utils.ItemBuilder;
import fr.theoszanto.mc.express.utils.ItemUtils;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public class CrateExpReward extends CrateReward {
	private int exp;
	private boolean levels;

	public CrateExpReward(@NotNull CrateExpress plugin, @NotNull String id, double weight, int exp, boolean levels) {
		super(plugin, id, "exp", icon(plugin.rewards(), exp, levels), weight, false);
		this.exp = exp;
		this.levels = levels;
	}

	@Override
	protected void reward(@NotNull Player player) throws RewardGiveException {
		if (this.levels)
			player.giveExpLevels(this.exp);
		else
			player.giveExp(this.exp, false);
	}

	public int getExp() {
		return this.exp;
	}

	public void setExp(int exp) {
		this.exp = exp;
	}

	public boolean isLevels() {
		return this.levels;
	}

	public void setLevels(boolean levels) {
		this.levels = levels;
	}

	private static @NotNull ItemStack icon(@NotNull PluginObject obj, int exp, boolean levels) {
		String levelsDisplay = obj.i18n("crate.preview.exp-" + (levels ? "levels" : "points"), "exp", exp);
		return new ItemBuilder(Material.EXPERIENCE_BOTTLE, ItemUtils.stackAmountFromValue(exp),
				obj.i18n("crate.preview.exp-name", "exp", exp, "levels", levelsDisplay),
				obj.i18nLines("crate.preview.exp-lore", "exp", exp, "levels", levelsDisplay)).buildUnmodifiable();
	}
}
