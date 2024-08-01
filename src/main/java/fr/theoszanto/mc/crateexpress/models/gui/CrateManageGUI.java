package fr.theoszanto.mc.crateexpress.models.gui;

import fr.theoszanto.mc.crateexpress.CrateExpress;
import fr.theoszanto.mc.crateexpress.commands.subcommands.CrateExpressTeleportSubCommand;
import fr.theoszanto.mc.crateexpress.events.CrateGiveEvent;
import fr.theoszanto.mc.crateexpress.models.Crate;
import fr.theoszanto.mc.crateexpress.models.CrateKey;
import fr.theoszanto.mc.crateexpress.utils.CratePermission;
import fr.theoszanto.mc.crateexpress.utils.FormatUtils;
import fr.theoszanto.mc.express.gui.ExpressGUI;
import fr.theoszanto.mc.express.utils.ItemBuilder;
import fr.theoszanto.mc.express.utils.ItemUtils;
import fr.theoszanto.mc.express.utils.UnloadableWorldLocation;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class CrateManageGUI extends ExpressGUI<CrateExpress> {
	private final @NotNull Crate crate;

	public CrateManageGUI(@NotNull CrateExpress plugin, @NotNull Crate crate) {
		super(plugin, 5, "menu.manage.title", "crate", crate.getName());
		this.crate = crate;
	}

	@Override
	public void onOpen(@NotNull Player player, @Nullable ExpressGUI<CrateExpress> previous) {
		// Borders
		ItemBuilder border = new ItemBuilder(Material.GRAY_STAINED_GLASS_PANE, 1, "§r");
		for (int i = 0; i < 9; i++) {
			this.set(slot(0, i), border);
			this.set(slot(4, i), border);
		}
		this.set(slot(1, 0), border);
		this.set(slot(2, 0), border);
		this.set(slot(3, 0), border);
		this.set(slot(1, 8), border);
		this.set(slot(2, 8), border);
		this.set(slot(3, 8), border);

		// Crate options header
		CrateKey key = this.crate.getKey();
		String message = this.crate.getMessage();
		List<UnloadableWorldLocation> locations = this.crate.getLocations();
		Sound sound = this.crate.getSound();
		ItemBuilder header = new ItemBuilder(Material.CHEST, 1, this.i18n("menu.manage.header.name"), this.i18nLines("menu.manage.header.lore",
				"status", this.i18n("menu.manage.header." + (this.crate.isDisabled() ? "disabled" : "enabled")),
				"key", key == null ? this.i18n("menu.manage.header.no-key") : ItemUtils.name(key.getItem()),
				"location", locations == null || locations.isEmpty() ? this.i18n("menu.manage.header.no-location") : locations.size() == 1 ? this.i18n("menu.manage.header.location",
						"world", locations.get(0).getWorldName(),
						"x", locations.get(0).getBlockX(),
						"y", locations.get(0).getBlockY(),
						"z", locations.get(0).getBlockZ()
				) : this.i18nLines("menu.manage.header.multiple-locations", "count", locations.size()),
				"delay", FormatUtils.noTrailingZeroDecimal(this.crate.getDelay()),
				"preview", this.i18n("menu.manage.header." + (this.crate.isNoPreview() ? "disabled" : "enabled")),
				"name", this.crate.getName(),
				"message", message == null ? this.i18n("menu.manage.header.no-message") : message,
				"sound", sound == null ? this.i18n("menu.manage.header.no-sound") : sound.getKey(),
				"random", this.i18n("menu.manage.header.random." + (this.crate.isRandom() ? "enabled" : "disabled"))
		));
		if (this.crate.isRandom())
			header.addLore(this.i18nLines("menu.manage.header.random.settings",
					"duplicates", this.i18n(this.crate.doesAllowDuplicates() ? "misc.yes" : "misc.no"),
					"min", this.crate.getMin(),
					"max", this.crate.getMax()));
		this.set(slot(0, 4), header);

		// Go back & close buttons
		this.set(slot(4, 0), new ItemBuilder(Material.ARROW, 1, this.i18n("menu.back")), "back");
		this.setCloseButton(slot(4, 8));

		// Control buttons
		this.set(slot(1, 2), new ItemBuilder(this.crate.isDisabled() ? Material.GRAY_DYE : Material.LIME_DYE, 1, this.i18n("menu.manage.status.name", "status", this.i18n("menu.manage.status." + (this.crate.isDisabled() ? "disabled" : "enabled"))), this.i18nLines("menu.manage.status.lore")), "status");
		this.set(slot(1, 3), new ItemBuilder(Material.TRIPWIRE_HOOK, 1, this.i18n("menu.manage.key.name", "key", this.i18n(key == null ? "misc.no" : "misc.yes")), this.i18nLines("menu.manage.key.lore")), "key");
		this.set(slot(1, 4), new ItemBuilder(locations == null ? Material.RECOVERY_COMPASS : Material.COMPASS, 1, this.i18n("menu.manage.location.name", "location", this.i18n(locations == null ? "misc.no" : "misc.yes")), this.i18nLines("menu.manage.location.lore", "location", locations == null || locations.isEmpty() ? this.i18n("menu.manage.location.none") : locations.size() == 1 ? this.i18n("menu.manage.location.value",
				"world", locations.get(0).getWorldName(),
				"x", locations.get(0).getBlockX(),
				"y", locations.get(0).getBlockY(),
				"z", locations.get(0).getBlockZ()
		) : this.i18n("menu.manage.location.multiple", "count", locations.size()))).addLoreConditionally(locations != null && !locations.isEmpty(), this.i18n("menu.manage.location.teleport")), "location");
		this.set(slot(1, 5), new ItemBuilder(Material.CLOCK, 1, this.i18n("menu.manage.delay.name", "delay", FormatUtils.noTrailingZeroDecimal(this.crate.getDelay())), this.i18nLines("menu.manage.delay.lore")), "delay");
		this.set(slot(1, 6), new ItemBuilder(this.crate.isNoPreview() ? Material.ENDER_PEARL : Material.ENDER_EYE, 1, this.i18n("menu.manage.preview.name", "preview", this.i18n("menu.manage.preview." + (this.crate.isNoPreview() ? "disabled" : "enabled"))), this.i18nLines("menu.manage.preview.lore")), "preview");

		this.set(slot(2, 3), new ItemBuilder(Material.NAME_TAG, 1, this.i18n("menu.manage.name.name", "name", this.crate.getName()), this.i18nLines("menu.manage.name.lore")), "name");
		this.set(slot(2, 4), new ItemBuilder(Material.BIRCH_SIGN, 1, this.i18n("menu.manage.message.name", "message", this.i18n(message == null ? "misc.no" : "misc.yes")), this.i18nLines("menu.manage.message.lore", "message", message == null ? this.i18n("menu.manage.message.none") : message)).addLoreConditionally(message != null, this.i18n("menu.manage.message.show")), "message");
		this.set(slot(2, 5), new ItemBuilder(Material.BELL, 1, this.i18n("menu.manage.sound.name", "sound", this.i18n(sound == null ? "misc.no" : "misc.yes")), this.i18nLines("menu.manage.sound.lore", "sound", sound == null ? this.i18n("menu.manage.sound.none") : sound.getKey())).addLoreConditionally(sound != null, this.i18n("menu.manage.sound.play")), "sound");

		this.set(slot(3, this.crate.isRandom() ? 2 : 4), new ItemBuilder(this.crate.isRandom() ? Material.RABBIT_FOOT : Material.BARREL, 1, this.i18n("menu.manage.random.name", "random", this.i18n("menu.manage.random." + (this.crate.isRandom() ? "enabled" : "disabled"))), this.i18nLines("menu.manage.random.lore")), "random");
		if (this.crate.isRandom()) {
			this.set(slot(3, 3), new ItemBuilder(this.crate.doesAllowDuplicates() ? Material.NETHERITE_SCRAP : Material.NETHERITE_INGOT, 1, this.i18n("menu.manage.duplicates.name", "duplicates", this.i18n(this.crate.doesAllowDuplicates() ? "misc.yes" : "misc.no")), this.i18nLines("menu.manage.duplicates.lore")), "duplicates");
			this.set(slot(3, 5), new ItemBuilder(Material.MINECART, this.crate.getMin(), this.i18n("menu.manage.min.name", "min", this.crate.getMin()), this.i18nLines("menu.manage.min.lore"))
					.addLoreConditionally(this.crate.getMin() > 1, this.i18n("menu.manage.min.decrease"))
					.addLoreConditionally(this.crate.getMin() < this.crate.getMax(), this.i18n("menu.manage.min.increase")), "min");
			this.set(slot(3, 6), new ItemBuilder(Material.CHEST_MINECART, this.crate.getMax(), this.i18n("menu.manage.max.name", "max", this.crate.getMax()), this.i18nLines("menu.manage.max.lore"))
					.addLoreConditionally(this.crate.getMax() > this.crate.getMin(), this.i18n("menu.manage.max.decrease"))
					.addLoreConditionally(this.crate.getMax() < 10, this.i18n("menu.manage.max.increase")), "max");
		}

		// Delete button
		this.set(slot(4, 4), new ItemBuilder(Material.TNT, 1, this.i18n("menu.manage.delete.name"), this.i18nLines("menu.manage.delete.lore")), "delete");
	}

	@Override
	public boolean onClick(@NotNull Player player, @NotNull ClickType click, @NotNull InventoryAction action, @Nullable SlotData data) {
		if (data == null)
			return true;
		switch (data.getName()) {
		case "back":
			new CrateEditGUI(this.plugin, this.crate).showToPlayer(player);
			break;
		case "status":
			this.crate.setDisabled(!this.crate.isDisabled());
			this.refresh(player);
			break;
		case "key":
			if (click.isLeftClick()) {
				if (action == InventoryAction.SWAP_WITH_CURSOR) {
					ItemStack item = player.getItemOnCursor();
					if (item.getType() != Material.AIR) {
						this.crate.setKey(new CrateKey(this.plugin, this.crate.getId(), ItemUtils.withAmount(item, 1)));
						this.refresh(player);
					}
				}
			} else if (click.isRightClick()) {
				if (this.crate.getKey() != null) {
					this.crate.setKey(null);
					this.refresh(player);
				}
			} else if (click == ClickType.MIDDLE && player.hasPermission(CratePermission.Command.GIVE)) {
				CrateKey key = this.crate.getKey();
				if (key != null)
					key.giveTo(player, 1, CrateGiveEvent.AdminGUIGiveButton.MANAGE);
			}
			break;
		case "location":
			if (click.isLeftClick()) {
				if (this.crate.getLocations() == null)
					this.crate.setLocations(new ArrayList<>());
				new CrateLocationsGUI(this.plugin, this.crate, this).showToPlayer(player);
			} else if (click.isRightClick() && this.crate.getLocations() != null) {
				this.crate.setLocations(null);
				this.refresh(player);
			} else if (click == ClickType.MIDDLE && player.hasPermission(CratePermission.Command.TELEPORT)) {
				List<UnloadableWorldLocation> locations = this.crate.getLocations();
				if (locations != null && !locations.isEmpty() && CrateExpressTeleportSubCommand.teleportOrOpenSelectMenu(this, crate, player))
					player.closeInventory();
			}
			break;
		case "delay":
			this.i18nMessage(player, "menu.manage.delay.request");
			player.closeInventory();
			this.spigot().requestChatEdition(player, FormatUtils.noTrailingZeroDecimal(this.crate.getDelay()), 1, TimeUnit.MINUTES).whenComplete((delay, timeout) -> {
				if (timeout == null) {
					try {
						this.crate.setDelay(Double.parseDouble(delay));
					} catch (NumberFormatException e) {
						this.i18nMessage(player, "menu.manage.delay.invalid");
					}
				} else
					this.i18nMessage(player, "menu.manage.delay.timeout");
				this.run(() -> this.showToPlayer(player));
			});
			break;
		case "preview":
			this.crate.setNoPreview(!this.crate.isNoPreview());
			this.refresh(player);
			break;
		case "name":
			this.i18nMessage(player, "menu.manage.name.request");
			player.closeInventory();
			this.spigot().requestChatEdition(player, this.crate.getName().replace('§', '&'), 1, TimeUnit.MINUTES).whenComplete((name, timeout) -> {
				if (timeout == null)
					this.crate.setName(ItemUtils.translateAmpersandColorCodes(name));
				else
					this.i18nMessage(player, "menu.manage.name.timeout");
				this.run(() -> this.showToPlayer(player));
			});
			break;
		case "message":
			if (click.isLeftClick()) {
				this.i18nMessage(player, "menu.manage.message.request");
				player.closeInventory();
				this.spigot().requestChatEdition(player, this.crate.getMessage() == null ? null : this.crate.getMessage().replace('§', '&'), 1, TimeUnit.MINUTES).whenComplete((message, timeout) -> {
					if (timeout == null)
						this.crate.setMessage(ItemUtils.translateAmpersandColorCodes(message));
					else
						this.i18nMessage(player, "menu.manage.message.timeout");
					this.run(() -> this.showToPlayer(player));
				});
			} else if (click.isRightClick() && this.crate.getMessage() != null) {
				this.crate.setMessage(null);
				this.refresh(player);
			} else if (click == ClickType.MIDDLE) {
				String formattedMessage = this.crate.getFormattedMessage(player);
				if (formattedMessage != null)
					player.sendMessage(formattedMessage);
			}
			break;
		case "sound":
			Sound sound = this.crate.getSound();
			if (click.isLeftClick()) {
				new CrateSoundGUI(this.plugin, sound == null ? CrateSoundGUI.SoundNamespace.ROOT : new CrateSoundGUI.SoundValue(sound).getNamespace(), this, this.crate::setSound).showToPlayer(player);
			} else if (click.isRightClick() && sound != null) {
				this.crate.setSound(null);
				this.refresh(player);
			} else if (click == ClickType.MIDDLE && sound != null)
				player.playSound(player.getLocation(), sound, SoundCategory.MASTER, 1, 1);
			break;
		case "random":
			this.crate.setRandom(!this.crate.isRandom());
			this.refresh(player);
			break;
		case "duplicates":
			this.crate.setAllowDuplicates(!this.crate.doesAllowDuplicates());
			this.refresh(player);
			break;
		case "min":
			if (click.isLeftClick()) {
				if (this.crate.getMin() < this.crate.getMax()) {
					this.crate.setMin(this.crate.getMin() + 1);
					this.refresh(player);
				}
			} else if (click.isRightClick()) {
				if (this.crate.getMin() > 1) {
					this.crate.setMin(this.crate.getMin() - 1);
					this.refresh(player);
				}
			}
			break;
		case "max":
			if (click.isLeftClick()) {
				if (this.crate.getMax() < 10) {
					this.crate.setMax(this.crate.getMax() + 1);
					this.refresh(player);
				}
			} else if (click.isRightClick()) {
				if (this.crate.getMax() > this.crate.getMin()) {
					this.crate.setMax(this.crate.getMax() - 1);
					this.refresh(player);
				}
			}
			break;
		case "delete":
			new CrateDeleteGUI(this.plugin, this.crate, this).showToPlayer(player);
			break;
		}
		return true;
	}

	@Override
	public void onClose(@NotNull Player player) {
		this.plugin.storage().getSource().saveCrate(this.crate);
	}
}
