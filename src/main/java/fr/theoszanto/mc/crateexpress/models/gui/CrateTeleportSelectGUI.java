package fr.theoszanto.mc.crateexpress.models.gui;

import fr.theoszanto.mc.crateexpress.CrateExpress;
import fr.theoszanto.mc.crateexpress.models.Crate;
import fr.theoszanto.mc.express.gui.ExpressGUI;
import fr.theoszanto.mc.express.gui.ExpressPaginatedGUI;
import fr.theoszanto.mc.express.utils.ItemBuilder;
import fr.theoszanto.mc.express.utils.ItemUtils;
import fr.theoszanto.mc.express.utils.MathUtils;
import fr.theoszanto.mc.express.utils.UnloadableWorldLocation;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public class CrateTeleportSelectGUI extends ExpressPaginatedGUI<CrateExpress, UnloadableWorldLocation> {
	private final @NotNull Crate crate;
	private @Nullable ExpressGUI<CrateExpress> returnTo;

	private static final int[] contentSlots = MathUtils.numbers(10, 17);

	public CrateTeleportSelectGUI(@NotNull CrateExpress plugin, @NotNull Crate crate, @Nullable ExpressGUI<CrateExpress> returnTo) {
		super(plugin, Objects.requireNonNull(crate.getLocations()), 3, "menu.teleport-select.title");
		this.crate = crate;
		this.returnTo = returnTo;
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
		this.setEmptyIndicator(slot(1, 4), "menu.teleport-select.empty");
		this.set(slot(0, 4), new ItemBuilder(Material.ENDER_PEARL, 1, this.i18n("menu.teleport-select.header.name"), this.i18nLines("menu.teleport-select.header.lore", "crate", this.crate.getName())));
	}

	@Override
	protected int @NotNull[] contentSlots() {
		return contentSlots;
	}

	@Override
	protected @Nullable ItemStack icon(@NotNull Player player, @NotNull UnloadableWorldLocation location) {
		ItemStack item = new ItemBuilder(
				location.isWorldLoaded() ? Material.COMPASS : Material.RECOVERY_COMPASS,
				1,
				this.i18n("menu.teleport-select.location.name", "world", location.getWorldName()),
				this.i18nLines("menu.teleport-select.location.lore",
						"x", location.getBlockX(),
						"y", location.getBlockY(),
						"z", location.getBlockZ())
		).build();
		ItemUtils.addLore(item, this.i18nLines(location.isWorldLoaded() ? "menu.teleport-select.location.select" : "menu.teleport-select.location.unloaded"));
		return item;
	}

	@Override
	protected boolean onClickOnElement(@NotNull Player player, @NotNull ClickType click, @NotNull InventoryAction action, @NotNull UnloadableWorldLocation location) {
		if (location.isWorldLoaded()) {
			player.teleport(location.clone().add(0.5, 0, 0.5));
			this.i18nMessage(player, "command.teleport.success", "crate", this.crate.getName());
		}
		this.returnTo = null;
		player.closeInventory();
		return true;
	}

	@Override
	public void onClose(@NotNull Player player) {
		if (this.returnTo != null)
			this.run(() -> this.returnTo.showToPlayer(player));
	}
}
