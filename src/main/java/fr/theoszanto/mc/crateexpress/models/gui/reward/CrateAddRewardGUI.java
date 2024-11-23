package fr.theoszanto.mc.crateexpress.models.gui.reward;

import fr.theoszanto.mc.crateexpress.CrateExpress;
import fr.theoszanto.mc.crateexpress.models.Crate;
import fr.theoszanto.mc.crateexpress.models.gui.CrateEditGUI;
import fr.theoszanto.mc.crateexpress.models.reward.CrateRewardType;
import fr.theoszanto.mc.express.gui.ExpressPaginatedGUI;
import fr.theoszanto.mc.express.utils.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class CrateAddRewardGUI extends ExpressPaginatedGUI<CrateExpress, CrateRewardType> {
	private final @NotNull Crate crate;

	private static final int[] contentSlots = { slot(1, 2), slot(1, 4), slot(2, 3), slot(1, 6), slot(2, 5) };

	public CrateAddRewardGUI(@NotNull CrateExpress plugin, @NotNull Crate crate) {
		super(plugin, plugin.rewards().getRewardsType(), 4, "menu.reward.new.title", "crate", crate.getName());
		this.crate = crate;
	}

	@Override
	protected void prepareGUI(@NotNull Player player) {
		// Borders
		for (int i = 0; i < 9; i++) {
			this.set(slot(0, i), BORDER);
			this.set(slot(3, i), BORDER);
		}
		this.set(slot(1, 0), BORDER);
		this.set(slot(2, 0), BORDER);
		this.set(slot(1, 8), BORDER);
		this.set(slot(2, 8), BORDER);

		// Add reward header
		this.set(slot(0, 4), new ItemBuilder(Material.NETHER_STAR, 1, this.i18n("menu.reward.new.header.name"), this.i18nLines("menu.reward.new.header.lore")));

		// Go back & pagination buttons
		this.set(slot(3, 0), new ItemBuilder(Material.ARROW, 1, this.i18n("menu.back")), "back");
		this.setButtons(slot(2, 1), slot(2, 7), slot(3, 4), slot(3, 8));
		this.setEmptyIndicator(slot(1, 4), "menu.reward.new.empty");
	}

	@Override
	protected int @NotNull[] contentSlots() {
		return contentSlots;
	}

	@Override
	protected @Nullable ItemStack icon(@NotNull Player player, @NotNull CrateRewardType element) {
		String type = element.getType();
		return new ItemBuilder(element.getIcon(), 1, this.i18n("menu.reward.new." + type + ".name"), this.i18nLines("menu.reward.new." + type + ".lore")).build();
	}

	@Override
	protected boolean onClickOnElement(@NotNull Player player, @NotNull ClickType click, @NotNull InventoryAction action, @NotNull CrateRewardType element) {
		element.createNewGUI(this.crate).showToPlayer(player);
		return true;
	}

	@Override
	protected boolean onOtherClick(@NotNull Player player, @NotNull ClickType click, @NotNull InventoryAction action, @Nullable SlotData data) {
		if (data != null && data.getName().equals("back"))
			new CrateEditGUI(this.plugin, this.crate).showToPlayer(player);
		return true;
	}
}
