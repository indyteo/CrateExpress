package fr.theoszanto.mc.crateexpress.models.gui.reward;

import fr.theoszanto.mc.crateexpress.CrateExpress;
import fr.theoszanto.mc.crateexpress.models.Crate;
import fr.theoszanto.mc.crateexpress.models.reward.CrateExpReward;
import fr.theoszanto.mc.crateexpress.models.reward.CrateReward;
import fr.theoszanto.mc.express.utils.ItemBuilder;
import fr.theoszanto.mc.express.utils.ItemUtils;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryAction;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.TimeUnit;

public class CrateExpRewardGUI extends CrateRewardGUI<CrateExpReward> {
	private int exp;
	private boolean levels = true;

	public CrateExpRewardGUI(@NotNull CrateExpress plugin, @NotNull Crate crate) {
		super(plugin, crate);
	}

	public CrateExpRewardGUI(@NotNull CrateExpress plugin, @NotNull Crate crate, @Nullable CrateExpReward reward, @NotNull Integer slot) {
		super(plugin, crate, reward, slot);
	}

	private int getExp() {
		return this.reward == null ? this.exp : this.reward.getExp();
	}

	private void setExp(int exp) {
		if (this.reward == null)
			this.exp = exp;
		else
			this.reward.setExp(exp);
	}

	private boolean isLevels() {
		return this.reward == null ? this.levels : this.reward.isLevels();
	}

	private void setLevels(boolean levels) {
		if (this.reward == null)
			this.levels = levels;
		else
			this.reward.setLevels(levels);
	}

	@Override
	protected void setupButtons(@NotNull Player player) {
		this.set(slot(0, 4), new ItemBuilder(Material.EXPERIENCE_BOTTLE, 1, this.i18n("menu.reward.exp.header.name"), this.i18nLines("menu.reward.exp.header.lore")));
		this.set(slot(1, 2), new ItemBuilder(this.isLevels() ? Material.ENCHANTED_BOOK : Material.BOOK, 1, this.i18n("menu.reward.exp.levels.name", "levels", this.i18nBoolean(this.isLevels())), this.i18nLines("menu.reward.exp.levels.lore")), "levels");
		this.set(slot(1, 4), new ItemBuilder(Material.EXPERIENCE_BOTTLE, ItemUtils.stackAmountFromValue(this.getExp()), this.i18n("menu.reward.exp.exp.name", "exp", this.getExp()), this.i18nLines("menu.reward.exp.exp.lore")), "exp");
		this.setWeightButton(slot(1, 6));
	}

	@Override
	protected boolean canCreateReward() {
		return this.exp > 0;
	}

	@Override
	protected @NotNull CrateExpReward createReward() throws IllegalStateException {
		if (!this.canCreateReward())
			throw new IllegalStateException();
		return new CrateExpReward(this.plugin, CrateReward.generateRandomId(), this.getWeight(), this.exp, this.levels);
	}

	@Override
	protected boolean onButtonClick(@NotNull Player player, @NotNull ClickType click, @NotNull InventoryAction action, @NotNull SlotData data) {
		switch (data.getName()) {
			case "levels" -> {
				this.setLevels(!this.isLevels());
				this.refresh(player);
			}
			case "exp" -> {
				this.i18nMessage(player, "menu.reward.exp.request");
				player.closeInventory();
				this.spigot().requestChatMessage(player, 1, TimeUnit.MINUTES).whenComplete((exp, timeout) -> {
					if (timeout == null) {
						try {
							this.setExp(Integer.parseInt(exp));
						} catch (NumberFormatException e) {
							this.i18nMessage(player, "menu.reward.exp.invalid");
						}
					} else
						this.i18nMessage(player, "menu.reward.exp.timeout");
					this.run(() -> this.showToPlayer(player));
				});
			}
		}
		return true;
	}
}
