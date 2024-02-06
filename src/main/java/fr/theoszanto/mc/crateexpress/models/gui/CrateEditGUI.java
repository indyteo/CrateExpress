package fr.theoszanto.mc.crateexpress.models.gui;

import fr.theoszanto.mc.crateexpress.CrateExpress;
import fr.theoszanto.mc.crateexpress.models.Crate;
import fr.theoszanto.mc.crateexpress.models.gui.reward.CrateAddRewardGUI;
import fr.theoszanto.mc.crateexpress.models.gui.reward.CrateDeleteRewardGUI;
import fr.theoszanto.mc.crateexpress.models.reward.CrateReward;
import fr.theoszanto.mc.crateexpress.utils.FormatUtils;
import fr.theoszanto.mc.express.gui.ExpressGUI;
import fr.theoszanto.mc.express.utils.ItemBuilder;
import fr.theoszanto.mc.express.utils.ItemUtils;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public class CrateEditGUI extends ExpressGUI<CrateExpress> {
	private final @NotNull Crate crate;
	private int cursorSlot;

	public CrateEditGUI(@NotNull CrateExpress plugin, @NotNull Crate crate) {
		super(plugin, 6, "menu.edit.title", "crate", crate.getName());
		this.crate = crate;
		this.cursorSlot = -1;
	}

	@Override
	public void onOpen(@NotNull Player player, @Nullable ExpressGUI<CrateExpress> previous) {
		// Borders
		ItemBuilder border = new ItemBuilder(Material.GRAY_STAINED_GLASS_PANE, 1, "§r");
		for (int i = 0; i < 9; i++)
			this.set(slot(5, i), border);

		// Back button
		this.set(slot(5, 0), new ItemBuilder(Material.ARROW, 1, this.i18n("menu.back")), "back");

		// Add reward button
		if (this.crate.getRewards().size() < 5 * 9)
			this.set(slot(5, 3), new ItemBuilder(Material.NETHER_STAR, 1, this.i18n("menu.edit.add.name"), this.i18nLines("menu.edit.add.lore")), "add");

		// Manage crate
		this.set(slot(5, 5), new ItemBuilder(Material.CHEST, 1, this.i18n("menu.edit.manage.name"), this.i18nLines("menu.edit.manage.lore")), "manage");

		// Close button
		this.setCloseButton(slot(5, 8));

		// Content
		if (this.crate.isEmpty())
			this.set(slot(2, 4), new ItemBuilder(Material.STRUCTURE_VOID, 1, this.i18n("menu.edit.empty")));
		else {
			for (int i = 0; i < 5; i++)
				for (int j = 0; j < 9; j++)
					this.set(slot(i, j), ItemUtils.EMPTY, "reward");
			double crateWeight = this.crate.totalWeight();
			this.crate.getRewardsWithSlot().forEach((slot, reward) -> {
				ItemStack item = reward.getIconWithChance(crateWeight);
				ItemUtils.addLore(item, this.i18nLines("menu.edit.reward.info",
						"weight", FormatUtils.noTrailingZeroDecimal(reward.getWeight()),
						"total", FormatUtils.noTrailingZeroDecimal(crateWeight),
						"type", this.i18n("crate.reward.type." + reward.getClass().getSimpleName())));
				this.set(slot, item, "reward", reward);
			});
		}
	}

	@Override
	public boolean onClick(@NotNull Player player, @NotNull ClickType click, @NotNull InventoryAction action, @Nullable SlotData data) {
		if (data == null)
			return true;
		int slot = data.getSlot();
		if (slot < 5 * 9) {
			Optional<CrateReward> optionalReward = data.getOptionalUserData();
			if (action == InventoryAction.DROP_ONE_SLOT || action == InventoryAction.DROP_ALL_SLOT) {
				if (optionalReward.isPresent())
					this.run(() -> new CrateDeleteRewardGUI(this.plugin, this.crate, slot, this).showToPlayer(player));
				return true;
			}
			if (click == ClickType.LEFT) {
				if (this.crate.isEmpty())
					return true;
				if (action == InventoryAction.PICKUP_ALL) {
					this.cursorSlot = slot;
					return false;
				}
				if (action == InventoryAction.PLACE_ALL) {
					if (slot != this.cursorSlot) {
						CrateReward reward = this.crate.getReward(this.cursorSlot);
						if (reward != null) {
							this.crate.removeReward(this.cursorSlot);
							this.clear(this.cursorSlot);
							this.crate.addReward(slot, reward);
							this.setData(slot, "reward", reward);
						}
					}
					this.cursorSlot = -1;
					return false;
				}
				if (action == InventoryAction.SWAP_WITH_CURSOR) {
					CrateReward reward = this.crate.getReward(this.cursorSlot);
					CrateReward swapped = this.crate.getReward(slot);
					if (reward != null && swapped != null) {
						this.crate.removeReward(this.cursorSlot);
						this.crate.removeReward(slot);
						this.crate.addReward(slot, reward);
						this.crate.addReward(this.cursorSlot, swapped);
						this.setData(slot, "reward", reward);
						this.setData(this.cursorSlot, "reward", swapped);
					}
					return false;
				}
				return true;
			}
			if (optionalReward.isPresent() && this.cursorSlot == -1) {
				CrateReward reward = optionalReward.get();
				if (click == ClickType.RIGHT) {
					try {
						this.plugin.rewards().getRewardType(reward.getType()).createNewGUI(this.crate, reward, data.getSlot()).showToPlayer(player);
					} catch (IllegalArgumentException e) {
						this.i18nMessage(player, "menu.edit.reward.unknown");
					}
				} else if (click == ClickType.MIDDLE)
					reward.giveRewardTo(player, true);
			}
			return true;
		}
		switch (data.getName()) {
		case "back":
			new CrateListGUI(this.plugin, this.crate.getNamespace()).showToPlayer(player);
			break;
		case "add":
			new CrateAddRewardGUI(this.plugin, this.crate).showToPlayer(player);
			break;
		case "manage":
			new CrateManageGUI(this.plugin, this.crate).showToPlayer(player);
			break;
		}
		return true;
	}

	@Override
	public boolean onClickInOtherInventory(@NotNull Player player, @NotNull Inventory inventory, @NotNull ClickType click, @NotNull InventoryAction action, int slot) {
		return true;
	}

	@Override
	public boolean onClickOutside(@NotNull Player player, @NotNull ClickType click, @NotNull InventoryAction action) {
		return true;
	}

	@Override
	public boolean preventDragOutside() {
		return true;
	}

	@Override
	public void onClose(@NotNull Player player) {
		this.plugin.storage().getSource().saveCrate(this.crate);
	}
}
