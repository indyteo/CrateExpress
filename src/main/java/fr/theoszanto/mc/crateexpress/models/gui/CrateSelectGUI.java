package fr.theoszanto.mc.crateexpress.models.gui;

import fr.theoszanto.mc.crateexpress.CrateExpress;
import fr.theoszanto.mc.crateexpress.models.Crate;
import fr.theoszanto.mc.crateexpress.models.CrateKey;
import fr.theoszanto.mc.express.gui.ExpressGUI;
import fr.theoszanto.mc.express.utils.ItemBuilder;
import fr.theoszanto.mc.express.utils.ItemUtils;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

public class CrateSelectGUI extends CrateListGUI {
	private final boolean withKeyOnly;
	private final @NotNull ExpressGUI<CrateExpress> returnTo;
	private final @NotNull Consumer<@NotNull String> onSelect;

	public CrateSelectGUI(@NotNull CrateExpress plugin, boolean withKeyOnly, @NotNull ExpressGUI<CrateExpress> returnTo, @NotNull Consumer<@NotNull String> onSelect) {
		super(plugin, "menu.select.title");
		this.withKeyOnly = withKeyOnly;
		this.returnTo = returnTo;
		this.onSelect = onSelect;
	}

	@Override
	protected void prepareGUI() {
		super.prepareGUI();
		this.setEmptyIndicator(slot(1, 4), "menu.select.empty");
	}

	@Override
	protected @Nullable ItemStack crateIcon(@NotNull Player player, @NotNull Crate crate) {
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
	protected @Nullable ItemStack crateSimpleIcon(@NotNull Player player, @NotNull Crate crate) {
		CrateKey key = crate.getKey();
		return key == null ? (this.withKeyOnly ? null : new ItemStack(Material.CHEST)) : key.getItem();
	}

	@Override
	protected boolean onClickOnCrate(@NotNull Player player, @NotNull ClickType click, @NotNull InventoryAction action, @NotNull Crate crate) {
		this.onSelect.accept(crate.getId());
		player.closeInventory();
		return true;
	}

	@Override
	public void onClose(@NotNull Player player) {
		this.run(() -> this.returnTo.showToPlayer(player));
	}
}
