package fr.theoszanto.mc.crateexpress.models.gui;

import fr.theoszanto.mc.crateexpress.CrateExpress;
import fr.theoszanto.mc.express.gui.ExpressGUI;
import fr.theoszanto.mc.express.utils.ItemBuilder;
import fr.theoszanto.mc.express.utils.ItemUtils;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryAction;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

public class CrateColorGUI extends ExpressGUI<CrateExpress> {
	private final @Nullable DyeColor color;
	private final @NotNull ExpressGUI<CrateExpress> returnTo;
	private final @NotNull Consumer<@Nullable DyeColor> onSelect;

	private static final int[] contentSlots = {
			/**//**//**//**//**//**//**//**//**/
			/**//**//**/ 12, 13, 14, 15,/**//**/
			/**/ 19, 20, 21, 22, 23, 24, 25,/**/
			/**//**/ 29, 30, 31, 32, 33 /**//**/
			/**//**//**//**//**//**//**//**//**/
	};

	public CrateColorGUI(@NotNull CrateExpress plugin, @Nullable DyeColor color, @NotNull ExpressGUI<CrateExpress> returnTo, @NotNull Consumer<@Nullable DyeColor> onSelect) {
		super(plugin, 5, "menu.color.title");
		this.color = color;
		this.returnTo = returnTo;
		this.onSelect = onSelect;
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

		// Header
		this.set(slot(0, 4), new ItemBuilder(this.color == null ? Material.WHITE_DYE : ItemUtils.colored("DYE", this.color, Material.WHITE_DYE), 1, this.i18n("menu.color.header")));

		// Go back & close buttons
		this.set(slot(4, 0), new ItemBuilder(Material.ARROW, 1, this.i18n("menu.back")), "back");
		this.setCloseButton(slot(4, 8));

		// Choices
		this.set(slot(1, 2), new ItemBuilder(Material.STRUCTURE_VOID, 1, this.i18n("menu.color.option", "color", this.i18n("menu.color.none")), this.i18nLines("menu.color.select"))
				.addLoreConditionally(this.color == null, this.i18n("menu.color.current"))
				.setGlint(this.color == null), "none");
		DyeColor[] colors = DyeColor.values();
		for (int i = 0; i < contentSlots.length; i++) {
			DyeColor color = colors[i];
			this.set(contentSlots[i], new ItemBuilder(ItemUtils.colored("DYE", color, Material.PAPER), 1, this.i18n("menu.color.option", "color", color.name()), this.i18nLines("menu.color.select"))
					.addLoreConditionally(this.color == color, this.i18n("menu.color.current"))
					.setGlint(this.color == color), "dye", color);
		}
	}

	@Override
	public boolean onClick(@NotNull Player player, @NotNull ClickType click, @NotNull InventoryAction action, @Nullable SlotData data) {
		if (data == null)
			return true;
		switch (data.getName()) {
			case "back" -> player.closeInventory();
			case "none" -> {
				this.onSelect.accept(null);
				player.closeInventory();
			}
			case "dye" -> {
				DyeColor color = data.getUserData();
				this.onSelect.accept(color);
				player.closeInventory();
			}
		}
		return true;
	}

	@Override
	public void onClose(@NotNull Player player) {
		this.run(() -> this.returnTo.showToPlayer(player));
	}
}
