package fr.theoszanto.mc.crateexpress.models.gui;

import fr.theoszanto.mc.crateexpress.CrateExpress;
import fr.theoszanto.mc.crateexpress.models.Crate;
import fr.theoszanto.mc.crateexpress.models.CrateKey;
import fr.theoszanto.mc.crateexpress.utils.ItemBuilder;
import fr.theoszanto.mc.crateexpress.utils.ItemUtils;
import fr.theoszanto.mc.crateexpress.utils.MathUtils;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.function.Consumer;

public class CrateSelectGUI extends CratePaginatedGUI<Crate> {
	private final boolean withKeyOnly;
	private final @NotNull CrateGUI returnTo;
	private final @NotNull Consumer<@NotNull String> onSelect;

	private static final int[] contentSlots = MathUtils.numbers(0, 3 * 9);

	public CrateSelectGUI(@NotNull CrateExpress plugin, boolean withKeyOnly, @NotNull CrateGUI returnTo, @NotNull Consumer<@NotNull String> onSelect) {
		super(plugin, new ArrayList<>(plugin.crates().list()), 4, "menu.select.title");
		this.withKeyOnly = withKeyOnly;
		this.returnTo = returnTo;
		this.onSelect = onSelect;
	}

	@Override
	protected void prepareGUI() {
		this.setButtons(slot(3, 0), slot(3, 8), slot(3, 3), slot(3, 5));
		this.setEmptyIndicator(slot(1, 4), "menu.select.empty");
	}

	@Override
	protected int @NotNull[] contentSlots() {
		return contentSlots;
	}

	@Override
	protected @Nullable ItemStack icon(@NotNull Player player, @NotNull Crate crate) {
		CrateKey key = crate.getKey();
		ItemStack item;
		if (key == null) {
			if (this.withKeyOnly)
				return null;
			item = new ItemBuilder(Material.CHEST, 1, crate.getName(), this.i18nLines("menu.select.no-key")).build();
		} else
			item = key.getItem().clone();
		ItemUtils.addLore(item, this.i18nLines("menu.select.lore"));
		ItemUtils.addLore(item, this.i18n("menu.select.crate-id", "id", crate.getId()));
		return item;
	}

	@Override
	protected boolean onClickOnElement(@NotNull Player player, @NotNull ClickType click, @NotNull InventoryAction action, @NotNull Crate crate) {
		this.onSelect.accept(crate.getId());
		player.closeInventory();
		return true;
	}

	@Override
	public void onClose(@NotNull Player player) {
		this.run(() -> this.returnTo.showToPlayer(player));
	}
}
