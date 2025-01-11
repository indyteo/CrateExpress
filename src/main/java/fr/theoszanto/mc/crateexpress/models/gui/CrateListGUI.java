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
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BundleMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Objects;
import java.util.SortedSet;

public class CrateListGUI extends ExpressPaginatedGUI<CrateExpress, CrateElement> {
	private @NotNull CrateNamespace namespace;

	private static final int[] contentSlots = MathUtils.numbers(9, 4 * 9);

	public CrateListGUI(@NotNull CrateExpress plugin) {
		this(plugin, plugin.crates().namespaces().root());
	}

	public CrateListGUI(@NotNull CrateExpress plugin, @NotNull CrateNamespace namespace) {
		this(plugin, "menu.list.title", namespace);
	}

	CrateListGUI(@NotNull CrateExpress plugin, @NotNull String key) {
		this(plugin, key, plugin.crates().namespaces().root());
	}

	CrateListGUI(@NotNull CrateExpress plugin, @NotNull String key, @NotNull CrateNamespace namespace) {
		super(plugin, namespace.listContent(), 5, key);
		this.namespace = namespace;
	}

	protected boolean isAllowedToManageNamespace(@NotNull Player player) {
		return player.hasPermission(CratePermission.Command.EDIT);
	}

	@Override
	protected void prepareGUI(@NotNull Player player) {
		for (int i = 0; i < 9; i++) {
			this.set(slot(0, i), BORDER);
			this.set(slot(4, i), BORDER);
		}
		this.setButtons(slot(4, 0), slot(4, 8), slot(4, 3), slot(4, 5));
		this.setEmptyIndicator(slot(2, 4), "menu.list.empty");
		this.set(slot(0, 4), new ItemBuilder(Material.OAK_SIGN, 1, this.i18n("menu.list.header.name", "namespace", this.namespace.isRoot() ? this.i18n("menu.list.header.root-namespace") : this.namespace.getName()), this.i18nLines("menu.list.header.lore")));
		if (!this.namespace.isRoot()) {
			DyeColor color = this.namespace.getColor();
			ItemStack arrow = new ItemBuilder(color == null ? Material.SPECTRAL_ARROW : Material.TIPPED_ARROW, 1, this.i18n("menu.list.parent-namespace"), this.i18nLines("menu.list.enter-namespace")).addFlag(ItemFlag.HIDE_ADDITIONAL_TOOLTIP).build();
			if (color != null)
				arrow.editMeta(PotionMeta.class, meta -> meta.setColor(color.getColor()));
			this.set(slot(0, 0), arrow, "parent");
			if (this.isAllowedToManageNamespace(player))
				this.set(slot(0, 8), new ItemBuilder(Material.ENDER_CHEST, 1, this.i18n("menu.list.manage-namespace.name"), this.i18nLines("menu.list.manage-namespace.lore")), "manage");
		}
	}

	@Override
	protected int @NotNull[] contentSlots() {
		return contentSlots;
	}

	@Override
	@SuppressWarnings("UnstableApiUsage") // BundleMeta
	protected @Nullable ItemStack icon(@NotNull Player player, @NotNull CrateElement crateElement) {
		if (crateElement instanceof Crate crate)
			return this.crateIcon(player, crate);
		if (crateElement instanceof CrateNamespace namespace) {
			SortedSet<CrateElement> content = namespace.listContent();
			ItemStack item = new ItemBuilder(ItemUtils.colored(Material.BUNDLE, namespace.getColor()), content.size(), this.i18n("menu.list.namespace", "namespace", namespace.getName()), this.i18nLines("menu.list.enter-namespace")).build();
			BundleMeta meta = (BundleMeta) item.getItemMeta();
			if (meta != null) {
				meta.setItems(content.stream().map(child -> {
					if (child instanceof Crate childCrate)
						return this.crateSimpleIcon(player, childCrate);
					if (child instanceof CrateNamespace childNamespace)
						return new ItemBuilder(ItemUtils.colored(Material.BUNDLE, childNamespace.getColor()), 1, this.i18n("menu.list.namespace", "namespace", childNamespace.getName())).build();
					return null;
				}).filter(Objects::nonNull).toList());
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
				"world", locations.getFirst().getWorldName(),
				"x", locations.getFirst().getBlockX(),
				"y", locations.getFirst().getBlockY(),
				"z", locations.getFirst().getBlockZ()
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
		return key == null ? new ItemBuilder(Material.CHEST, 1, crate.getName()).build() : key.getItem().clone();
	}

	@Override
	protected boolean onClickOnElement(@NotNull Player player, @NotNull ClickType click, @NotNull InventoryAction action, @NotNull CrateElement element) {
		if (element instanceof Crate crate)
			return this.onClickOnCrate(player, click, action, crate);
		if (element instanceof CrateNamespace namespace)
			this.openNamespace(player, namespace);
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
		if (data != null && !this.namespace.isRoot()) {
			switch (data.getName()) {
			case "parent":
				this.openNamespace(player, this.namespace.getParent());
				break;
			case "manage":
				if (this.isAllowedToManageNamespace(player))
					new CrateNamespaceManageGUI(this.plugin, this.namespace).showToPlayer(player);
				break;
			}
		}
		return true;
	}

	private void openNamespace(@NotNull Player player, @NotNull CrateNamespace namespace) {
		this.namespace = namespace;
		this.setElements(namespace.listContent());
		this.page = 0;
		this.refresh(player);
	}
}
