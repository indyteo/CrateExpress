package fr.theoszanto.mc.crateexpress.models.gui.reward;

import fr.theoszanto.mc.crateexpress.CrateExpress;
import fr.theoszanto.mc.crateexpress.models.Crate;
import fr.theoszanto.mc.crateexpress.models.gui.CrateEditGUI;
import fr.theoszanto.mc.crateexpress.models.gui.CrateGUI;
import fr.theoszanto.mc.crateexpress.utils.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryAction;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class CrateAddRewardGUI extends CrateGUI {
	private final @NotNull Crate crate;

	public CrateAddRewardGUI(@NotNull CrateExpress plugin, @NotNull Crate crate) {
		super(plugin, 4, "menu.reward.new.title", "crate", crate.getName());
		this.crate = crate;
	}

	@Override
	public void onOpen(@NotNull Player player, @Nullable CrateGUI previous) {
		// Borders
		ItemBuilder border = new ItemBuilder(Material.GRAY_STAINED_GLASS_PANE, 1, "§r");
		for (int i = 0; i < 9; i++) {
			this.set(slot(0, i), border);
			this.set(slot(3, i), border);
		}
		this.set(slot(1, 0), border);
		this.set(slot(2, 0), border);
		this.set(slot(1, 8), border);
		this.set(slot(2, 8), border);

		// Add reward header
		this.set(slot(0, 4), new ItemBuilder(Material.NETHER_STAR, 1, this.i18n("menu.reward.new.header.name"), this.i18nLines("menu.reward.new.header.lore")));

		// Go back & close buttons
		this.set(slot(3, 0), new ItemBuilder(Material.ARROW, 1, this.i18n("menu.back")), "back");
		this.setCloseButton(slot(3, 8));

		// Control buttons
		this.set(slot(1, 2), new ItemBuilder(Material.ITEM_FRAME, 1, this.i18n("menu.reward.new.item.name"), this.i18nLines("menu.reward.new.item.lore")), "item");
		this.set(slot(1, 4), new ItemBuilder(Material.TRIPWIRE_HOOK, 1, this.i18n("menu.reward.new.key.name"), this.i18nLines("menu.reward.new.key.lore")), "key");
		this.set(slot(1, 6), new ItemBuilder(Material.EMERALD, 1, this.i18n("menu.reward.new.money.name"), this.i18nLines("menu.reward.new.money.lore")), "money");
		this.set(slot(2, 3), new ItemBuilder(Material.COMMAND_BLOCK, 1, this.i18n("menu.reward.new.command.name"), this.i18nLines("menu.reward.new.command.lore")), "command");
		this.set(slot(2, 5), new ItemBuilder(Material.CHEST, 1, this.i18n("menu.reward.new.other.name"), this.i18nLines("menu.reward.new.other.lore")), "other");
	}

	@Override
	public boolean onClick(@NotNull Player player, @NotNull ClickType click, @NotNull InventoryAction action, @Nullable SlotData data) {
		if (data == null)
			return true;
		switch (data.getName()) {
		case "back":
			new CrateEditGUI(this.plugin, this.crate).showToPlayer(player);
			break;
		case "item":
			new CrateItemRewardGUI(this.plugin, this.crate, null, -1).showToPlayer(player);
			break;
		case "key":
			new CrateKeyRewardGUI(this.plugin, this.crate, null, -1).showToPlayer(player);
			break;
		case "money":
			new CrateMoneyRewardGUI(this.plugin, this.crate, null, -1).showToPlayer(player);
			break;
		case "command":
			new CrateCommandRewardGUI(this.plugin, this.crate, null, -1).showToPlayer(player);
			break;
		case "other":
			new CrateOtherRewardGUI(this.plugin, this.crate, null, -1).showToPlayer(player);
			break;
		}
		return true;
	}
}
