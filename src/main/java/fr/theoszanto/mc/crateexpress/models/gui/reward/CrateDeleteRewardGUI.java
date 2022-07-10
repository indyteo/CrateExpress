package fr.theoszanto.mc.crateexpress.models.gui.reward;

import fr.theoszanto.mc.crateexpress.CrateExpress;
import fr.theoszanto.mc.crateexpress.models.Crate;
import fr.theoszanto.mc.crateexpress.models.gui.CrateEditGUI;
import fr.theoszanto.mc.express.gui.ExpressGUI;
import fr.theoszanto.mc.express.utils.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryAction;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class CrateDeleteRewardGUI extends ExpressGUI<CrateExpress> {
	private final @NotNull Crate crate;
	private final int slot;
	private final @NotNull ExpressGUI<CrateExpress> returnTo;

	public CrateDeleteRewardGUI(@NotNull CrateExpress plugin, @NotNull Crate crate, int slot, @NotNull ExpressGUI<CrateExpress> returnTo) {
		super(plugin, 2, "menu.reward.delete.title", "crate", crate.getName());
		this.crate = crate;
		this.slot = slot;
		this.returnTo = returnTo;
	}

	@Override
	public void onOpen(@NotNull Player player, @Nullable ExpressGUI previous) {
		this.set(slot(0, 4), new ItemBuilder(Material.TNT, 1, this.i18n("menu.reward.delete.warning.name"), this.i18nLines("menu.reward.delete.warning.lore")));
		this.set(slot(1, 2), new ItemBuilder(Material.LIME_DYE, 1, this.i18n("menu.reward.delete.confirm.name"), this.i18nLines("menu.reward.delete.confirm.lore")), "confirm");
		this.set(slot(1, 6), new ItemBuilder(Material.BARRIER, 1, this.i18n("menu.reward.delete.cancel.name"), this.i18nLines("menu.reward.delete.cancel.lore")), "cancel");
	}

	@Override
	public boolean onClick(@NotNull Player player, @NotNull ClickType click, @NotNull InventoryAction action, @Nullable SlotData data) {
		if (data == null)
			return true;
		switch (data.getName()) {
		case "cancel":
			this.returnTo.showToPlayer(player);
			break;
		case "confirm":
			this.crate.removeReward(this.slot);
			new CrateEditGUI(this.plugin, this.crate).showToPlayer(player);
			break;
		}
		return true;
	}
}
