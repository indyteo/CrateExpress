package fr.theoszanto.mc.crateexpress.models.gui;

import fr.theoszanto.mc.crateexpress.CrateExpress;
import fr.theoszanto.mc.crateexpress.models.reward.ClaimableReward;
import fr.theoszanto.mc.crateexpress.utils.MathUtils;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class CrateClaimGUI extends CratePaginatedGUI<ClaimableReward> {
	private static final int[] contentSlots = MathUtils.numbers(0, 5 * 9);

	public CrateClaimGUI(@NotNull CrateExpress plugin, @NotNull List<@NotNull ClaimableReward> rewards) {
		super(plugin, rewards, 6, "menu.claim.title");
	}

	@Override
	protected void prepareGUI() {
		this.setButtons(slot(5, 0), slot(5, 8), slot(5, 3), slot(5, 5));
		this.setEmptyIndicator(slot(2, 4), "menu.claim.empty");
	}

	@Override
	protected int @NotNull[] contentSlots() {
		return contentSlots;
	}

	@Override
	protected @NotNull ItemStack icon(@NotNull Player player, @NotNull ClaimableReward element) {
		return element.getReward().getIcon();
	}

	@Override
	public boolean onClickOnElement(@NotNull Player player, @NotNull ClickType click, @NotNull InventoryAction action, @NotNull ClaimableReward element) {
		// Remove stored pending reward
		this.storage().deleteReward(player, element.getId());
		// Try to give it to player and remove it from current pending rewards if successful
		if (element.getReward().giveRewardTo(player))
			this.list.remove(element);
		// Update GUI
		this.refresh(player);
		return true;
	}
}
