package fr.theoszanto.mc.crateexpress.models.gui;

import fr.theoszanto.mc.crateexpress.CrateExpress;
import fr.theoszanto.mc.crateexpress.models.CrateNamespace;
import fr.theoszanto.mc.express.gui.ExpressGUI;
import fr.theoszanto.mc.express.utils.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryAction;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class CrateNamespaceDeleteGUI extends ExpressGUI<CrateExpress> {
	private final @NotNull CrateNamespace namespace;
	private final @Nullable ExpressGUI<CrateExpress> returnTo;

	public CrateNamespaceDeleteGUI(@NotNull CrateExpress plugin, @NotNull CrateNamespace namespace, @Nullable ExpressGUI<CrateExpress> returnTo) {
		super(plugin, 2, "menu.delete-namespace.title", "namespace", namespace.getName());
		this.namespace = namespace;
		this.returnTo = returnTo;
	}

	@Override
	public void onOpen(@NotNull Player player, @Nullable ExpressGUI previous) {
		this.set(slot(0, 4), new ItemBuilder(Material.TNT, 1, this.i18n("menu.delete-namespace.warning.name"), this.i18nLines("menu.delete-namespace.warning.lore")));
		this.set(slot(1, 2), new ItemBuilder(Material.LIME_DYE, 1, this.i18n("menu.delete-namespace.confirm.name"), this.i18nLines("menu.delete-namespace.confirm.lore")), "confirm");
		this.set(slot(1, 6), new ItemBuilder(Material.BARRIER, 1, this.i18n("menu.delete-namespace.cancel.name"), this.i18nLines("menu.delete-namespace.cancel.lore")), "cancel");
	}

	@Override
	public boolean onClick(@NotNull Player player, @NotNull ClickType click, @NotNull InventoryAction action, @Nullable SlotData data) {
		if (data == null)
			return true;
		switch (data.getName()) {
			case "cancel" -> {
				if (this.returnTo == null)
					player.closeInventory();
				else
					this.returnTo.showToPlayer(player);
			}
			case "confirm" -> {
				this.plugin.crates().namespaces().delete(this.namespace);
				this.i18nMessage(player, "menu.delete-namespace.success", "namespace", this.namespace.getName());
				player.closeInventory();
			}
		}
		return true;
	}
}
