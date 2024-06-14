package fr.theoszanto.mc.crateexpress.models.gui;

import fr.theoszanto.mc.crateexpress.CrateExpress;
import fr.theoszanto.mc.crateexpress.commands.subcommands.CrateExpressTeleportSubCommand;
import fr.theoszanto.mc.crateexpress.events.CrateGiveEvent;
import fr.theoszanto.mc.crateexpress.models.Crate;
import fr.theoszanto.mc.crateexpress.models.CrateElement;
import fr.theoszanto.mc.crateexpress.models.CrateKey;
import fr.theoszanto.mc.crateexpress.models.CrateNamespace;
import fr.theoszanto.mc.crateexpress.utils.CratePermission;
import fr.theoszanto.mc.express.gui.ExpressPaginatedGUI;
import fr.theoszanto.mc.express.utils.ItemBuilder;
import fr.theoszanto.mc.express.utils.ItemUtils;
import fr.theoszanto.mc.express.utils.MathUtils;
import fr.theoszanto.mc.express.utils.UnloadableWorldLocation;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BundleMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Objects;
import java.util.SortedSet;
import java.util.stream.Collectors;

public class CrateListGUI extends ExpressPaginatedGUI<CrateExpress, CrateElement> {
	private @NotNull CrateNamespace namespace;

	private static final int[] contentSlots = MathUtils.numbers(9, 4 * 9);

	public CrateListGUI(@NotNull CrateExpress plugin) {
		this(plugin, CrateNamespace.root(plugin));
	}

	public CrateListGUI(@NotNull CrateExpress plugin, @NotNull CrateNamespace namespace) {
		this(plugin, "menu.list.title", namespace);
	}

	CrateListGUI(@NotNull CrateExpress plugin, @NotNull String key) {
		this(plugin, key, CrateNamespace.root(plugin));
	}

	CrateListGUI(@NotNull CrateExpress plugin, @NotNull String key, @NotNull CrateNamespace namespace) {
		super(plugin, namespace.listContent(), 5, key);
		this.namespace = namespace;
	}

	@Override
	protected void prepareGUI(@NotNull Player player) {
		this.setButtons(slot(4, 0), slot(4, 8), slot(4, 3), slot(4, 5));
		this.setEmptyIndicator(slot(2, 4), "menu.list.empty");
		this.set(slot(0, 4), new ItemBuilder(Material.OAK_SIGN, 1, this.i18n("menu.list.header.name", "namespace", this.namespace.isRoot() ? this.i18n("menu.list.header.root-namespace") : this.namespace.getName()), this.i18nLines("menu.list.header.lore")));
		if (!this.namespace.isRoot())
			this.set(slot(0, 0), new ItemBuilder(Material.SPECTRAL_ARROW, 1, this.i18n("menu.list.parent-namespace"), this.i18nLines("menu.list.enter-namespace")), "parent");
	}

	@Override
	protected int @NotNull[] contentSlots() {
		return contentSlots;
	}

	@Override
	@SuppressWarnings("UnstableApiUsage")
	protected @Nullable ItemStack icon(@NotNull Player player, @NotNull CrateElement crateElement) {
		if (crateElement.isCrate())
			return this.crateIcon(player, (Crate) crateElement);
		if (crateElement.isNamespace()) {
			CrateNamespace namespace = (CrateNamespace) crateElement;
			SortedSet<CrateElement> content = namespace.listContent();
			ItemStack item = new ItemBuilder(Material.BUNDLE, content.size(), this.i18n("menu.list.namespace", "namespace", namespace.getName()), this.i18nLines("menu.list.enter-namespace")).build();
			BundleMeta meta = (BundleMeta) item.getItemMeta();
			if (meta != null) {
				meta.setItems(content.stream().map(child -> {
					if (child.isCrate())
						return this.crateSimpleIcon(player, (Crate) child);
					if (child.isNamespace())
						return new ItemStack(Material.BUNDLE);
					return null;
				}).filter(Objects::nonNull).collect(Collectors.toList()));
				meta.addItemFlags(ItemFlag.values());
				item.setItemMeta(meta);
			}
			ItemUtils.addLore(item, this.i18n("menu.list.namespace-path", "path", namespace.getPath()));
			return item;
		}
		return null;
	}

	protected @Nullable ItemStack crateIcon(@NotNull Player player, @NotNull Crate crate) {
		CrateKey key = crate.getKey();
		ItemStack item;
		if (key == null)
			item = new ItemBuilder(Material.CHEST, 1, crate.getName(), this.i18nLines("menu.list.no-key")).build();
		else
			item = key.getItem().clone();
		List<UnloadableWorldLocation> locations = crate.getLocations();
		ItemUtils.addLore(item, locations == null || locations.isEmpty() ? this.i18nLines("menu.list.no-location") : locations.size() == 1 ? this.i18nLines("menu.list.location",
				"world", locations.get(0).getWorldName(),
				"x", locations.get(0).getBlockX(),
				"y", locations.get(0).getBlockY(),
				"z", locations.get(0).getBlockZ()
		) : this.i18nLines("menu.list.multiple-locations", "count", locations.size()));
		ItemUtils.addLoreConditionally(crate.isDisabled(), item, this.i18n("menu.list.disabled"));
		ItemUtils.addLoreConditionally(crate.isNoPreview(), item, this.i18n("menu.list.no-preview"));
		ItemUtils.addLoreConditionally(player.hasPermission(CratePermission.Command.EDIT), item, this.i18n("menu.list.edit"));
		ItemUtils.addLoreConditionally(key != null && player.hasPermission(CratePermission.Command.GIVE), item, this.i18n("menu.list.give"));
		ItemUtils.addLoreConditionally(locations != null && !locations.isEmpty() && player.hasPermission(CratePermission.Command.TELEPORT), item, this.i18n("menu.list.teleport"));
		ItemUtils.addLore(item, this.i18n("menu.list.crate-id", "id", crate.getId()));
		return item;
	}

	protected @Nullable ItemStack crateSimpleIcon(@NotNull Player player, @NotNull Crate crate) {
		CrateKey key = crate.getKey();
		return key == null ? new ItemStack(Material.CHEST) : key.getItem();
	}

	@Override
	protected boolean onClickOnElement(@NotNull Player player, @NotNull ClickType click, @NotNull InventoryAction action, @NotNull CrateElement crateElement) {
		if (crateElement.isCrate())
			return this.onClickOnCrate(player, click, action, (Crate) crateElement);
		if (crateElement.isNamespace())
			this.openNamespace(player, (CrateNamespace) crateElement);
		return true;
	}

	protected boolean onClickOnCrate(@NotNull Player player, @NotNull ClickType click, @NotNull InventoryAction action, @NotNull Crate crate) {
		if (click.isLeftClick()) {
			if (player.hasPermission(CratePermission.Command.EDIT))
				new CrateEditGUI(this.plugin, crate).showToPlayer(player);
		} else if (click == ClickType.MIDDLE) {
			if (player.hasPermission(CratePermission.Command.GIVE)) {
				CrateKey key = crate.getKey();
				if (key != null)
					key.giveTo(player, 1, CrateGiveEvent.AdminGUIGiveButton.LIST);
			}
		} else if (click.isRightClick()) {
			if (player.hasPermission(CratePermission.Command.TELEPORT)) {
				List<UnloadableWorldLocation> locations = crate.getLocations();
				if (locations != null && !locations.isEmpty() && CrateExpressTeleportSubCommand.teleportOrOpenSelectMenu(this, crate, player))
					player.closeInventory();
			}
		}
		return true;
	}

	@Override
	protected boolean onOtherClick(@NotNull Player player, @NotNull ClickType click, @NotNull InventoryAction action, @Nullable SlotData data) {
		if (data != null && data.getName().equalsIgnoreCase("parent") && !this.namespace.isRoot())
			this.openNamespace(player, this.namespace.getParent());
		return true;
	}

	private void openNamespace(@NotNull Player player, @NotNull CrateNamespace namespace) {
		this.namespace = namespace;
		this.setElements(namespace.listContent());
		this.page = 0;
		this.refresh(player);
	}
}
