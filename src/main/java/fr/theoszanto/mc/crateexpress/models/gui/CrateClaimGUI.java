package fr.theoszanto.mc.crateexpress.models.gui;

import fr.theoszanto.mc.crateexpress.CrateExpress;
import fr.theoszanto.mc.crateexpress.models.reward.ClaimableReward;
import fr.theoszanto.mc.express.gui.ExpressPaginatedGUI;
import fr.theoszanto.mc.express.utils.ItemBuilder;
import fr.theoszanto.mc.express.utils.ItemUtils;
import fr.theoszanto.mc.express.utils.MathUtils;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class CrateClaimGUI extends ExpressPaginatedGUI<CrateExpress, ClaimableReward> {
	private boolean processing = false;

	private static final int[] contentSlots = MathUtils.numbers(0, 5 * 9);

	public CrateClaimGUI(@NotNull CrateExpress plugin, @NotNull List<@NotNull ClaimableReward> rewards) {
		super(plugin, rewards, 6, "menu.claim.title");
	}

	@Override
	protected void prepareGUI(@NotNull Player player) {
		this.setButtons(slot(5, 0), slot(5, 8), slot(5, 2), slot(5, 6));
		this.setEmptyIndicator(slot(2, 4), "menu.claim.empty");
		if (!this.list.isEmpty())
			this.set(slot(5, 4), new ItemBuilder(Material.HOPPER, 1, this.i18n("menu.claim.all.name"), this.i18nLines("menu.claim.all.lore")), "all");
	}

	@Override
	protected int @NotNull[] contentSlots() {
		return contentSlots;
	}

	@Override
	protected @NotNull ItemStack icon(@NotNull Player player, @NotNull ClaimableReward element) {
		ItemStack icon = element.getReward().getIcon().clone();
		ItemUtils.addLore(icon, this.i18nLines("menu.claim.reward"));
		return icon;
	}

	@Override
	public boolean onClickOnElement(@NotNull Player player, @NotNull ClickType click, @NotNull InventoryAction action, @NotNull ClaimableReward element) {
		if (this.processing) {
			this.i18nMessage(player, "menu.claim.slow-down");
			return true;
		}
		this.processing = true;
		if (click == ClickType.DROP || click == ClickType.CONTROL_DROP) {
			this.list.remove(element);
			this.plugin.storage().getSource().deleteReward(player, element.getId());
			this.recomputePageMax();
			this.refresh(player);
		} else {
			PlayerInventory inventory = player.getInventory();
			if ((inventory.firstEmpty() != -1 || !element.getReward().isPhysicalReward()) && this.claimReward(player, element)) {
				this.recomputePageMax();
				this.refresh(player);
			}
		}
		this.processing = false;
		return true;
	}

	@Override
	protected boolean onOtherClick(@NotNull Player player, @NotNull ClickType click, @NotNull InventoryAction action, @Nullable SlotData data) {
		if (this.processing) {
			this.i18nMessage(player, "menu.claim.slow-down");
			return true;
		}
		this.processing = true;
		if (data != null && data.getName().equalsIgnoreCase("all")) {
			if (click == ClickType.DROP || click == ClickType.CONTROL_DROP) {
				this.list.clear();
				this.plugin.storage().getSource().clearRewards(player);
			} else {
				PlayerInventory inventory = player.getInventory();
				int index = 0;
				while (inventory.firstEmpty() != -1 && index < this.list.size()) {
					if (!this.claimReward(player, this.list.get(index)))
						index++;
				}
			}
			this.recomputePageMax();
			this.refresh(player);
		}
		this.processing = false;
		return true;
	}

	private boolean claimReward(@NotNull Player player, @NotNull ClaimableReward reward) {
		// Try to give it to player and remove it from current pending rewards if successful
		if (reward.getReward().giveRewardTo(player, false)) {
			this.list.remove(reward);
			// Remove stored pending reward
			this.plugin.storage().getSource().deleteReward(player, reward.getId());
			return true;
		}
		return false;
	}
}
