package fr.theoszanto.mc.crateexpress.models.gui.reward;

import fr.theoszanto.mc.crateexpress.CrateExpress;
import fr.theoszanto.mc.crateexpress.models.Crate;
import fr.theoszanto.mc.crateexpress.models.gui.CrateSelectGUI;
import fr.theoszanto.mc.crateexpress.models.reward.CrateOtherReward;
import fr.theoszanto.mc.crateexpress.models.reward.CrateReward;
import fr.theoszanto.mc.express.utils.ItemBuilder;
import fr.theoszanto.mc.express.utils.ItemUtils;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class CrateOtherRewardGUI extends CrateRewardGUI<CrateOtherReward> {
	private @NotNull ItemStack icon;
	private @Nullable String crate;

	public CrateOtherRewardGUI(@NotNull CrateExpress plugin, @NotNull Crate crate) {
		super(plugin, crate);
		this.icon = this.defaultIcon();
	}

	public CrateOtherRewardGUI(@NotNull CrateExpress plugin, @NotNull Crate crate, @Nullable CrateOtherReward reward, @NotNull Integer slot) {
		super(plugin, crate, reward, slot);
		this.icon = this.defaultIcon();
	}

	private @NotNull ItemStack defaultIcon() {
		return new ItemBuilder(Material.CHEST_MINECART, 1, this.i18n("misc.default-other-icon-name")).build();
	}

	private void setIcon(@NotNull ItemStack icon) {
		if (this.reward == null)
			this.icon = icon;
		else
			this.reward.setIcon(icon);
	}

	@Override
	protected void setupButtons(@NotNull Player player) {
		this.set(slot(0, 4), new ItemBuilder(Material.CHEST, 1, this.i18n("menu.reward.other.header.name"), this.i18nLines("menu.reward.other.header.lore")));
		this.set(slot(1, 2), new ItemBuilder(Material.PAINTING, 1, this.i18n("menu.reward.other.icon.name", "icon", ItemUtils.name(this.icon)), this.i18nLines("menu.reward.other.icon.lore")), "icon");
		if (this.reward == null)
			this.set(slot(1, 4), new ItemBuilder(Material.CHEST, 1, this.i18n("menu.reward.other.crate.name", "crate", this.crate == null ? this.i18n("menu.reward.other.crate.none") : this.crate), this.i18nLines("menu.reward.other.crate.lore")), "crate");
		else
			this.set(slot(1, 4), this.reward.getIcon());
		this.setWeightButton(slot(1, 6));
	}

	@Override
	protected boolean canCreateReward() {
		return this.crate != null;
	}

	@Override
	protected @NotNull CrateOtherReward createReward() throws IllegalStateException {
		if (this.crate == null)
			throw new IllegalStateException();
		return new CrateOtherReward(this.plugin, CrateReward.generateRandomId(), this.icon, this.getWeight(), this.crate);
	}

	@Override
	protected boolean onButtonClick(@NotNull Player player, @NotNull ClickType click, @NotNull InventoryAction action, @NotNull SlotData data) {
		switch (data.getName()) {
			case "icon" -> {
				if (action == InventoryAction.SWAP_WITH_CURSOR) {
					ItemStack item = player.getItemOnCursor();
					if (item.getType() != Material.AIR) {
						this.setIcon(item.clone());
						this.refresh(player);
					}
				}
			}
			case "crate" -> {
				if (this.reward == null)
					new CrateSelectGUI(this.plugin, false, this, crate -> this.crate = crate).showToPlayer(player);
			}
		}
		return true;
	}
}
