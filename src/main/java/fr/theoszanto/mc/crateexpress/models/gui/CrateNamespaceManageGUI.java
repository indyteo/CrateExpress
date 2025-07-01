package fr.theoszanto.mc.crateexpress.models.gui;

import fr.theoszanto.mc.crateexpress.CrateExpress;
import fr.theoszanto.mc.crateexpress.models.CrateNamespace;
import fr.theoszanto.mc.express.gui.ExpressGUI;
import fr.theoszanto.mc.express.utils.ItemBuilder;
import fr.theoszanto.mc.express.utils.ItemUtils;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.inventory.ItemFlag;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class CrateNamespaceManageGUI extends ExpressGUI<CrateExpress> {
	private final @NotNull CrateNamespace namespace;

	public CrateNamespaceManageGUI(@NotNull CrateExpress plugin, @NotNull CrateNamespace namespace) {
		super(plugin, 5, "menu.manage-namespace.title", "namespace", namespace.getName());
		this.namespace = namespace;
	}

	@Override
	public void onOpen(@NotNull Player player, @Nullable ExpressGUI<CrateExpress> previous) {
		// Borders
		for (int i = 0; i < 9; i++) {
			this.set(slot(0, i), BORDER);
			this.set(slot(4, i), BORDER);
		}
		for (int i = 1; i < 4; i++) {
			this.set(slot(i, 0), BORDER);
			this.set(slot(i, 8), BORDER);
		}

		// Namespace options header
		DyeColor color = this.namespace.getColor();
		String colorStr = color == null ? this.i18n("menu.manage-namespace.color.none") : color.name();
		this.set(slot(0, 4), new ItemBuilder(Material.ENDER_CHEST, 1, this.i18n("menu.manage-namespace.header.name"), this.i18nLines("menu.manage-namespace.header.lore", "color", colorStr)));

		// Go back & close buttons
		this.set(slot(4, 0), new ItemBuilder(Material.ARROW, 1, this.i18n("menu.back")), "back");
		this.setCloseButton(slot(4, 8));

		// Control buttons
		this.set(slot(2, 4), new ItemBuilder(ItemUtils.colored(Material.BUNDLE, color), 1, this.i18n("menu.manage-namespace.color.name", "color", colorStr), this.i18nLines("menu.manage-namespace.color.lore")).addFlag(ItemFlag.HIDE_ADDITIONAL_TOOLTIP), "color");

		// Delete button
		this.set(slot(4, 4), new ItemBuilder(Material.TNT, 1, this.i18n("menu.manage-namespace.delete.name"), this.i18nLines("menu.manage-namespace.delete.lore")), "delete");
	}

	@Override
	public boolean onClick(@NotNull Player player, @NotNull ClickType click, @NotNull InventoryAction action, @Nullable SlotData data) {
		if (data == null)
			return true;
		switch (data.getName()) {
			case "back" -> new CrateListGUI(this.plugin, this.namespace).showToPlayer(player);
			case "color" -> new CrateColorGUI(this.plugin, this.namespace.getColor(), this, this.namespace::setColor).showToPlayer(player);
			case "delete" -> new CrateNamespaceDeleteGUI(this.plugin, this.namespace, this).showToPlayer(player);
		}
		return true;
	}

	@Override
	public void onClose(@NotNull Player player) {
		this.plugin.store(storage -> storage.saveNamespace(this.namespace));
	}
}
