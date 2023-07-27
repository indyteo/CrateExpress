package fr.theoszanto.mc.crateexpress.models.gui.reward;

import fr.theoszanto.mc.crateexpress.CrateExpress;
import fr.theoszanto.mc.crateexpress.models.Crate;
import fr.theoszanto.mc.crateexpress.models.gui.CrateEditGUI;
import fr.theoszanto.mc.crateexpress.models.reward.CrateReward;
import fr.theoszanto.mc.express.gui.ExpressGUI;
import fr.theoszanto.mc.express.utils.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryAction;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class CrateRewardGUI<T extends CrateReward> extends ExpressGUI<CrateExpress> {
	private final @NotNull Crate crate;
	protected final @Nullable T reward;
	private final int slot;
	private int weight;

	public CrateRewardGUI(@NotNull CrateExpress plugin, @NotNull Crate crate) {
		this(plugin, crate, null, -1);
	}

	public CrateRewardGUI(@NotNull CrateExpress plugin, @NotNull Crate crate, @Nullable T reward, @NotNull Integer slot) {
		super(plugin, 3, "menu.reward.title", "crate", crate.getName());
		this.crate = crate;
		this.reward = reward;
		this.slot = slot;
		this.weight = 1;
	}

	public int getWeight() {
		return this.reward == null ? this.weight : (int) this.reward.getWeight();
	}

	public void setWeight(int weight) {
		if (this.reward == null)
			this.weight = weight;
		else
			this.reward.setWeight(weight);
	}

	protected abstract void setupButtons(@NotNull Player player);

	protected final void setWeightButton(int slot) {
		int weight = this.getWeight();
		this.set(slot, new ItemBuilder(Material.ANVIL, weight, this.i18n("menu.reward.weight.name", "weight", weight), this.i18nLines("menu.reward.weight.lore", "total", this.crate.totalWeight()))
				.addLoreConditionally(weight > 1, this.i18n("menu.reward.weight.decrease"))
				.addLore(this.i18n("menu.reward.weight.increase")), "weight");
	}

	@Override
	public void onOpen(@NotNull Player player, @Nullable ExpressGUI<CrateExpress> previous) {
		// Borders
		ItemBuilder border = new ItemBuilder(Material.GRAY_STAINED_GLASS_PANE, 1, "§r");
		for (int i = 0; i < 9; i++) {
			this.set(slot(0, i), border);
			this.set(slot(2, i), border);
		}
		this.set(slot(1, 0), border);
		this.set(slot(1, 8), border);

		// Go back & close buttons
		this.set(slot(2, 0), new ItemBuilder(Material.ARROW, 1, this.i18n("menu.back")), "back");
		this.setCloseButton(slot(2, 8));

		// Add & delete buttons
		if (this.reward == null) {
			if (this.canCreateReward())
				this.set(slot(2, 4), new ItemBuilder(Material.LIME_DYE, 1, this.i18n("menu.reward.confirm")), "confirm");
			else
				this.set(slot(2, 4), new ItemBuilder(Material.GRAY_DYE, 1, this.i18n("menu.reward.no-confirm")));
		} else
			this.set(slot(0, 0), new ItemBuilder(Material.TNT, 1, this.i18n("menu.reward.delete-button")), "delete");

		// Other buttons
		this.setupButtons(player);
	}

	protected abstract boolean canCreateReward();

	protected abstract @NotNull T createReward() throws IllegalStateException;

	protected abstract boolean onButtonClick(@NotNull Player player, @NotNull ClickType click, @NotNull InventoryAction action, @NotNull SlotData data);

	@Override
	public boolean onClick(@NotNull Player player, @NotNull ClickType click, @NotNull InventoryAction action, @Nullable SlotData data) {
		if (data == null)
			return true;
		switch (data.getName()) {
		case "confirm":
			CrateReward reward;
			try {
				reward = this.createReward();
			} catch (IllegalStateException e) {
				player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1, 1);
				return true;
			}
			int slot = 0;
			while (slot < 5 * 9 && this.crate.getReward(slot) != null)
				slot++;
			this.crate.addReward(slot, reward);
		case "back":
			new CrateEditGUI(this.plugin, this.crate).showToPlayer(player);
			break;
		case "weight":
			if (click.isLeftClick()) {
				this.setWeight(this.getWeight() + 1);
				this.refresh(player);
			} else if (click.isRightClick() && this.getWeight() > 1) {
				this.setWeight(this.getWeight() - 1);
				this.refresh(player);
			}
			break;
		case "delete":
			new CrateDeleteRewardGUI(this.plugin, this.crate, this.slot, this).showToPlayer(player);
			break;
		default:
			return this.onButtonClick(player, click, action, data);
		}
		return true;
	}

	@Override
	public void onClose(@NotNull Player player) {
		this.plugin.storage().getSource().saveCrate(this.crate);
	}
}
