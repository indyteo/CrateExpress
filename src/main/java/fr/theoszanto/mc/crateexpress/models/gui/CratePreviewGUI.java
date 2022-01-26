package fr.theoszanto.mc.crateexpress.models.gui;

import fr.theoszanto.mc.crateexpress.CrateExpress;
import fr.theoszanto.mc.crateexpress.models.Crate;
import fr.theoszanto.mc.crateexpress.models.CrateKey;
import fr.theoszanto.mc.crateexpress.utils.CratePermission;
import fr.theoszanto.mc.crateexpress.utils.ItemBuilder;
import fr.theoszanto.mc.crateexpress.utils.ItemUtils;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Comparator;

public class CratePreviewGUI extends CrateGUI {
	private final @NotNull Crate crate;

	public CratePreviewGUI(@NotNull CrateExpress plugin, @NotNull Crate crate) {
		super(plugin, computeRows(crate), "menu.preview.title", "crate", crate.getName());
		this.crate = crate;
	}

	@Override
	public void onOpen(@NotNull Player player, @Nullable CrateGUI previous) {
		// Crate info & close button
		CrateKey key = this.crate.getKey();
		ItemStack keyItem;
		if (key == null)
			keyItem = new ItemBuilder(Material.STRUCTURE_VOID, 1, this.crate.getName(), this.i18nLines("menu.preview.no-key")).build();
		else {
			keyItem = key.getItem().clone();
			ItemUtils.addLoreConditionally(player.hasPermission(CratePermission.Command.GIVE), keyItem, this.i18nLines("menu.preview.give-key"));
		}
		this.set(slot(this.rows - 1, 2), keyItem, key == null ? "" : "key");
		this.setCloseButton(slot(this.rows - 1, 6));

		// Edition button
		if (player.hasPermission(CratePermission.Command.EDIT))
			this.set(slot(this.rows - 1, 4), new ItemBuilder(Material.CHEST, 1, this.i18n("menu.preview.edit")), "edit");

		// Content
		if (this.crate.isEmpty())
			this.set(slot(0, 4), new ItemBuilder(Material.STRUCTURE_VOID, 1, this.i18n("menu.preview.empty")));
		else {
			int crateWeight = this.crate.totalWeight();
			this.crate.getRewards().forEach((slot, reward) -> this.set(slot, reward.getIconWithChance(crateWeight)));
		}
	}

	@Override
	public boolean onClick(@NotNull Player player, @NotNull ClickType click, @NotNull InventoryAction action, @Nullable SlotData data) {
		if (data == null)
			return true;
		switch (data.getName()) {
		case "key":
			if (player.hasPermission(CratePermission.Command.GIVE)) {
				CrateKey key = this.crate.getKey();
				if (key != null)
					key.giveTo(player, 1);
			}
			break;
		case "edit":
			if (player.hasPermission(CratePermission.Command.EDIT))
				new CrateEditGUI(this.plugin, this.crate).showToPlayer(player);
			break;
		}
		return true;
	}

	private static int computeRows(@NotNull Crate crate) {
		return crate.getRewards().keySet().stream().max(Comparator.naturalOrder()).orElse(0) / 9 + 2;
	}
}
