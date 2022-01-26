package fr.theoszanto.mc.crateexpress.models.gui;

import fr.theoszanto.mc.crateexpress.CrateExpress;
import fr.theoszanto.mc.crateexpress.models.Crate;
import fr.theoszanto.mc.crateexpress.models.CrateKey;
import fr.theoszanto.mc.crateexpress.utils.CratePermission;
import fr.theoszanto.mc.crateexpress.utils.ItemBuilder;
import fr.theoszanto.mc.crateexpress.utils.ItemUtils;
import fr.theoszanto.mc.crateexpress.utils.MathUtils;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class CrateListGUI extends CratePaginatedGUI<Crate> {
	private static final int[] contentSlots = MathUtils.numbers(0, 3 * 9);

	public CrateListGUI(@NotNull CrateExpress plugin, @NotNull List<@NotNull Crate> list) {
		super(plugin, list, 4, "menu.list.title");
	}

	@Override
	protected void prepareGUI() {
		this.setButtons(slot(3, 0), slot(3, 8), slot(3, 3), slot(3, 5));
		this.setEmptyIndicator(slot(1, 4), "menu.list.empty");
	}

	@Override
	protected int @NotNull[] contentSlots() {
		return contentSlots;
	}

	@Override
	protected @NotNull ItemStack icon(@NotNull Player player, @NotNull Crate crate) {
		CrateKey key = crate.getKey();
		ItemStack item;
		if (key == null)
			item = new ItemBuilder(Material.CHEST, 1, crate.getName(), this.i18nLines("menu.list.no-key")).build();
		else
			item = key.getItem().clone();
		Location location = crate.getLocation();
		if (location == null)
			ItemUtils.addLore(item, this.i18nLines("menu.list.no-location"));
		else {
			World world;
			if (location.isWorldLoaded())
				world = location.getWorld();
			else
				world = null;
			ItemUtils.addLore(item, this.i18nLines("menu.list.location", "world", world == null ? this.i18n("misc.unloaded-world") : world.getName(), "x", location.getBlockX(), "y", location.getBlockY(), "z", location.getBlockZ()));
		}
		ItemUtils.addLoreConditionally(player.hasPermission(CratePermission.Command.EDIT), item, this.i18n("menu.list.edit"));
		ItemUtils.addLoreConditionally(crate.getKey() != null && player.hasPermission(CratePermission.Command.GIVE), item, this.i18n("menu.list.give"));
		ItemUtils.addLoreConditionally(crate.getLocation() != null && player.hasPermission(CratePermission.Command.TELEPORT), item, this.i18n("menu.list.teleport"));
		return item;
	}

	@Override
	protected boolean onClickOnElement(@NotNull Player player, @NotNull ClickType click, @NotNull InventoryAction action, @NotNull Crate crate) {
		if (click.isLeftClick()) {
			if (player.hasPermission(CratePermission.Command.EDIT))
				new CrateEditGUI(this.plugin, crate).showToPlayer(player);
		} else if (click == ClickType.MIDDLE) {
			if (player.hasPermission(CratePermission.Command.GIVE)) {
				CrateKey key = crate.getKey();
				if (key != null)
					key.giveTo(player, 1);
			}
		} else if (click.isRightClick()) {
			if (player.hasPermission(CratePermission.Command.TELEPORT)) {
				Location location = crate.getLocation();
				if (location != null) {
					player.closeInventory();
					player.teleport(location);
				}
			}
		}
		return true;
	}
}
