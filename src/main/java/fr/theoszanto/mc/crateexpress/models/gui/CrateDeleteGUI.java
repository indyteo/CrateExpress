package fr.theoszanto.mc.crateexpress.models.gui;

import fr.theoszanto.mc.crateexpress.CrateExpress;
import fr.theoszanto.mc.crateexpress.models.Crate;
import fr.theoszanto.mc.crateexpress.utils.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryAction;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class CrateDeleteGUI extends CrateGUI {
	private final @NotNull Crate crate;
	private final @Nullable CrateGUI returnTo;

	public CrateDeleteGUI(@NotNull CrateExpress plugin, @NotNull Crate crate, @Nullable CrateGUI returnTo) {
		super(plugin, 2, "menu.delete.title", "crate", crate.getName());
		this.crate = crate;
		this.returnTo = returnTo;
	}

	@Override
	public void onOpen(@NotNull Player player, @Nullable CrateGUI previous) {
		this.set(slot(0, 4), new ItemBuilder(Material.TNT, 1, this.i18n("menu.delete.warning.name"), this.i18nLines("menu.delete.warning.lore")));
		this.set(slot(1, 2), new ItemBuilder(Material.LIME_DYE, 1, this.i18n("menu.delete.confirm.name"), this.i18nLines("menu.delete.confirm.lore")), "confirm");
		this.set(slot(1, 6), new ItemBuilder(Material.BARRIER, 1, this.i18n("menu.delete.cancel.name"), this.i18nLines("menu.delete.cancel.lore")), "cancel");
	}

	@Override
	public boolean onClick(@NotNull Player player, @NotNull ClickType click, @NotNull InventoryAction action, @Nullable SlotData data) {
		if (data == null)
			return true;
		switch (data.getName()) {
		case "cancel":
			if (this.returnTo == null)
				player.closeInventory();
			else
				this.returnTo.showToPlayer(player);
			break;
		case "confirm":
			this.crates().deleteCrate(this.crate.getId());
			this.i18nMessage(player, "menu.delete.success", "crate", this.crate.getName());
			player.closeInventory();
			break;
		}
		return true;
	}
}
