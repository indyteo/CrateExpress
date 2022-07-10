package fr.theoszanto.mc.crateexpress.models.gui.reward;

import fr.theoszanto.mc.crateexpress.CrateExpress;
import fr.theoszanto.mc.crateexpress.models.Crate;
import fr.theoszanto.mc.crateexpress.models.reward.CrateItemReward;
import fr.theoszanto.mc.express.utils.ItemBuilder;
import fr.theoszanto.mc.express.utils.ItemUtils;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class CrateItemRewardGUI extends CrateRewardGUI<CrateItemReward> {
	private @Nullable ItemStack item;

	public CrateItemRewardGUI(@NotNull CrateExpress plugin, @NotNull Crate crate) {
		super(plugin, crate);
	}

	public CrateItemRewardGUI(@NotNull CrateExpress plugin, @NotNull Crate crate, @Nullable CrateItemReward reward, @NotNull Integer slot) {
		super(plugin, crate, reward, slot);
	}

	@Override
	protected void setupButtons(@NotNull Player player) {
		this.set(slot(0, 4), new ItemBuilder(Material.ITEM_FRAME, 1, this.i18n("menu.reward.item.header.name"), this.i18nLines("menu.reward.item.header.lore")));
		if (this.reward == null)
			this.set(slot(1, 2), new ItemBuilder(Material.ITEM_FRAME, 1, this.i18n("menu.reward.item.item.name", "item", this.item == null ? this.i18n("menu.reward.item.item.none") : ItemUtils.name(this.item)), this.i18nLines("menu.reward.item.item.lore")), "item");
		else
			this.set(slot(1, 2), this.reward.getItem());
		this.setWeightButton(slot(1, 6));
	}

	@Override
	protected boolean canCreateReward() {
		return this.item != null;
	}

	@Override
	protected @NotNull CrateItemReward createReward() throws IllegalStateException {
		if (this.item == null)
			throw new IllegalStateException();
		return new CrateItemReward(this.plugin, this.getWeight(), this.item);
	}

	@Override
	protected boolean onButtonClick(@NotNull Player player, @NotNull ClickType click, @NotNull InventoryAction action, @NotNull SlotData data) {
		if (data.getName().equals("item") && this.reward == null) {
			if (action == InventoryAction.SWAP_WITH_CURSOR) {
				ItemStack item = player.getItemOnCursor();
				if (item.getType() != Material.AIR) {
					this.item = item.clone();
					this.refresh(player);
				}
			}
		}
		return true;
	}
}
