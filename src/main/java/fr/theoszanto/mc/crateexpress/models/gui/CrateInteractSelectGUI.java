package fr.theoszanto.mc.crateexpress.models.gui;

import fr.theoszanto.mc.crateexpress.CrateExpress;
import fr.theoszanto.mc.crateexpress.models.Crate;
import fr.theoszanto.mc.crateexpress.models.CrateKey;
import fr.theoszanto.mc.crateexpress.utils.CratePermission;
import fr.theoszanto.mc.express.gui.ExpressPaginatedGUI;
import fr.theoszanto.mc.express.utils.ItemBuilder;
import fr.theoszanto.mc.express.utils.ItemUtils;
import fr.theoszanto.mc.express.utils.MathUtils;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.function.Consumer;

public class CrateInteractSelectGUI extends ExpressPaginatedGUI<CrateExpress, Crate> {
	private final boolean isOpenInteraction;
	private final @NotNull Consumer<@NotNull Crate> onSelect;

	private static final int[] contentSlots = MathUtils.numbers(10, 17);

	public CrateInteractSelectGUI(@NotNull CrateExpress plugin, @NotNull Collection<@NotNull Crate> crates, boolean isOpenInteraction, @NotNull Consumer<@NotNull Crate> onSelect) {
		super(plugin, crates, 3, key("title", isOpenInteraction));
		this.isOpenInteraction = isOpenInteraction;
		this.onSelect = onSelect;
	}

	@Override
	protected void prepareGUI(@NotNull Player player) {
		for (int i = 0; i < 9; i++) {
			this.set(slot(0, i), BORDER);
			this.set(slot(2, i), BORDER);
		}
		this.set(slot(1, 0), BORDER);
		this.set(slot(1, 8), BORDER);
		this.setButtons(slot(2, 0), slot(2, 8), slot(2, 3), slot(2, 5));
		this.setEmptyIndicator(slot(1, 4), this.key("empty"));
		this.set(slot(0, 4), new ItemBuilder(this.isOpenInteraction ? Material.TRIPWIRE_HOOK : Material.ENDER_EYE, 1, this.i18n(this.key("header.name")), this.i18nLines(this.key("header.lore"))));
	}

	@Override
	protected int @NotNull[] contentSlots() {
		return contentSlots;
	}

	@Override
	protected @Nullable ItemStack icon(@NotNull Player player, @NotNull Crate crate) {
		CrateKey key = crate.getKey();
		ItemStack item;
		if (key == null)
			item = new ItemBuilder(Material.CHEST, 1, crate.getName(), this.i18nLines(this.key("no-key"))).build();
		else
			item = key.getItem().clone();
		ItemUtils.addLore(item, this.i18nLines(this.key("lore"), "crate", crate.getName()));
		ItemUtils.addLoreConditionally(player.hasPermission(CratePermission.Command.LIST), item, this.i18nLines(this.key("id"), "id", crate.getId()));
		return item;
	}

	@Override
	protected boolean onClickOnElement(@NotNull Player player, @NotNull ClickType click, @NotNull InventoryAction action, @NotNull Crate crate) {
		player.closeInventory();
		this.onSelect.accept(crate);
		return true;
	}

	private @NotNull String key(@NotNull String key) {
		return key(key, this.isOpenInteraction);
	}

	private static @NotNull String key(@NotNull String key, boolean isOpenInteraction) {
		return "menu.interact-select." + (isOpenInteraction ? "open" : "preview") + "." + key;
	}
}
