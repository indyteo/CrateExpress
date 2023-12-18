package fr.theoszanto.mc.crateexpress.models.gui;

import fr.theoszanto.mc.crateexpress.CrateExpress;
import fr.theoszanto.mc.crateexpress.models.Crate;
import fr.theoszanto.mc.express.gui.ExpressGUI;
import fr.theoszanto.mc.express.gui.ExpressPaginatedGUI;
import fr.theoszanto.mc.express.utils.ItemBuilder;
import fr.theoszanto.mc.express.utils.MathUtils;
import fr.theoszanto.mc.express.utils.UnloadableWorldLocation;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Objects;

public class CrateLocationsGUI extends ExpressPaginatedGUI<CrateExpress, UnloadableWorldLocation> {
	private final @NotNull Crate crate;
	private final @NotNull List<@NotNull UnloadableWorldLocation> locations;
	private final @NotNull ExpressGUI<CrateExpress> returnTo;

	private static final int[] contentSlots = MathUtils.numbers(10, 17);

	public CrateLocationsGUI(@NotNull CrateExpress plugin, @NotNull Crate crate, @NotNull ExpressGUI<CrateExpress> returnTo) {
		super(plugin, Objects.requireNonNull(crate.getLocations()), 3, "menu.locations.title");
		this.crate = crate;
		this.locations = crate.getLocations();
		this.returnTo = returnTo;
	}

	@Override
	protected void prepareGUI(@NotNull Player player) {
		ItemBuilder border = new ItemBuilder(Material.GRAY_STAINED_GLASS_PANE, 1, "§r");
		for (int i = 0; i < 9; i++) {
			this.set(slot(0, i), border);
			this.set(slot(2, i), border);
		}
		this.set(slot(1, 0), border);
		this.set(slot(1, 8), border);
		this.setButtons(slot(2, 0), slot(2, 8), slot(2, 3), slot(2, 5));
		this.setEmptyIndicator(slot(1, 4), "menu.locations.empty");
		this.set(slot(0, 4), new ItemBuilder(Material.ENDER_PEARL, 1, this.i18n("menu.locations.header.name"), this.i18nLines("menu.locations.header.lore", "crate", this.crate.getName())));
		this.set(slot(0, 8), new ItemBuilder(Material.NETHER_STAR, 1, this.i18n("menu.locations.add.name"), this.i18nLines("menu.locations.add.lore")), "add");
	}

	@Override
	protected int @NotNull[] contentSlots() {
		return contentSlots;
	}

	@Override
	protected @Nullable ItemStack icon(@NotNull Player player, @NotNull UnloadableWorldLocation location) {
		return new ItemBuilder(
				location.isWorldLoaded() ? Material.COMPASS : Material.RECOVERY_COMPASS,
				1,
				this.i18n("menu.locations.location.name", "world", location.getWorldName()),
				this.i18nLines("menu.locations.location.lore",
						"x", location.getBlockX(),
						"y", location.getBlockY(),
						"z", location.getBlockZ(),
						"loaded", this.i18n(location.isWorldLoaded() ? "misc.yes" : "misc.no"))
		).build();
	}

	@Override
	protected boolean onClickOnElement(@NotNull Player player, @NotNull ClickType click, @NotNull InventoryAction action, @NotNull UnloadableWorldLocation location) {
		this.locations.remove(location);
		this.list.remove(location);
		this.refresh(player);
		return true;
	}

	@Override
	protected boolean onOtherClick(@NotNull Player player, @NotNull ClickType click, @NotNull InventoryAction action, @Nullable SlotData data) {
		if (data != null && data.getName().equalsIgnoreCase("add")) {
			Block block = player.getTargetBlockExact(15);
			Location loc = block == null ? player.getLocation() : block.getLocation();
			UnloadableWorldLocation location = new UnloadableWorldLocation(player.getWorld(), loc.getBlockX(), loc.getBlockY(), loc.getBlockZ());
			this.locations.add(location);
			this.list.add(location);
			this.refresh(player);
		}
		return true;
	}

	@Override
	public void onClose(@NotNull Player player) {
		this.run(() -> this.returnTo.showToPlayer(player));
	}
}
