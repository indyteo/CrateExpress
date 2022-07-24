package fr.theoszanto.mc.crateexpress.models.gui;

import fr.theoszanto.mc.crateexpress.CrateExpress;
import fr.theoszanto.mc.crateexpress.events.CrateGiveEvent;
import fr.theoszanto.mc.crateexpress.models.Crate;
import fr.theoszanto.mc.crateexpress.models.CrateKey;
import fr.theoszanto.mc.crateexpress.utils.CratePermission;
import fr.theoszanto.mc.express.gui.ExpressGUI;
import fr.theoszanto.mc.express.utils.ItemBuilder;
import fr.theoszanto.mc.express.utils.ItemUtils;
import fr.theoszanto.mc.express.utils.UnloadableWorldLocation;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.TimeUnit;

public class CrateManageGUI extends ExpressGUI<CrateExpress> {
	private final @NotNull Crate crate;

	public CrateManageGUI(@NotNull CrateExpress plugin, @NotNull Crate crate) {
		super(plugin, 4, "menu.manage.title", "crate", crate.getName());
		this.crate = crate;
	}

	@Override
	public void onOpen(@NotNull Player player, @Nullable ExpressGUI<CrateExpress> previous) {
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

		// Crate options header
		CrateKey key = this.crate.getKey();
		String message = this.crate.getMessage();
		UnloadableWorldLocation location = this.crate.getLocation();
		Sound sound = this.crate.getSound();
		this.set(slot(0, 4), new ItemBuilder(Material.CHEST, 1, this.i18n("menu.manage.header.name"), this.i18nLines("menu.manage.header.lore",
				"min", this.crate.getMin(),
				"max", this.crate.getMax(),
				"key", key == null ? this.i18n("menu.manage.header.no-key") : ItemUtils.name(key.getItem()),
				"location", location == null ? this.i18n("menu.manage.header.no-location") : this.i18n("menu.manage.header.location",
						"world", location.getWorldName(),
						"x", location.getBlockX(),
						"y", location.getBlockY(),
						"z", location.getBlockZ()
				),
				"name", this.crate.getName(),
				"message", message == null ? this.i18n("menu.manage.header.no-message") : message,
				"delay", this.crate.getDelay(),
				"sound", sound == null ? this.i18n("menu.manage.header.no-sound") : sound.getKey()
		)));

		// Go back & close buttons
		this.set(slot(3, 0), new ItemBuilder(Material.ARROW, 1, this.i18n("menu.back")), "back");
		this.setCloseButton(slot(3, 8));

		// Control buttons
		this.set(slot(1, 1), new ItemBuilder(Material.MINECART, this.crate.getMin(), this.i18n("menu.manage.min.name", "min", this.crate.getMin()), this.i18nLines("menu.manage.min.lore"))
				.addLoreConditionally(this.crate.getMin() > 1, this.i18n("menu.manage.min.decrease"))
				.addLoreConditionally(this.crate.getMin() < this.crate.getMax(), this.i18n("menu.manage.min.increase")), "min");
		this.set(slot(1, 2), new ItemBuilder(Material.CHEST_MINECART, this.crate.getMax(), this.i18n("menu.manage.max.name", "max", this.crate.getMax()), this.i18nLines("menu.manage.max.lore"))
				.addLoreConditionally(this.crate.getMax() > this.crate.getMin(), this.i18n("menu.manage.max.decrease"))
				.addLoreConditionally(this.crate.getMax() < 10, this.i18n("menu.manage.max.increase")), "max");
		this.set(slot(1, 4), new ItemBuilder(Material.TRIPWIRE_HOOK, 1, this.i18n("menu.manage.key.name", "key", this.i18n(key == null ? "misc.no" : "misc.yes")), this.i18nLines("menu.manage.key.lore")), "key");
		this.set(slot(1, 5), new ItemBuilder(Material.COMPASS, 1, this.i18n("menu.manage.location.name", "location", this.i18n(location == null ? "misc.no" : "misc.yes")), this.i18nLines("menu.manage.location.lore", "location", location == null ? this.i18n("menu.manage.location.none") : this.i18n("menu.manage.location.value",
				"world", location.getWorldName(),
				"x", location.getBlockX(),
				"y", location.getBlockY(),
				"z", location.getBlockZ()
		))).addLoreConditionally(location != null, this.i18n("menu.manage.location.teleport")), "location");
		this.set(slot(1, 7), new ItemBuilder(Material.TNT, 1, this.i18n("menu.manage.delete.name"), this.i18nLines("menu.manage.delete.lore")), "delete");
		this.set(slot(2, 2), new ItemBuilder(Material.NAME_TAG, 1, this.i18n("menu.manage.name.name", "name", this.crate.getName()), this.i18nLines("menu.manage.name.lore")), "name");
		this.set(slot(2, 3), new ItemBuilder(Material.BIRCH_SIGN, 1, this.i18n("menu.manage.message.name", "message", this.i18n(message == null ? "misc.no" : "misc.yes")), this.i18nLines("menu.manage.message.lore", "message", message == null ? this.i18n("menu.manage.message.none") : message)).addLoreConditionally(message != null, this.i18n("menu.manage.message.show")), "message");
		this.set(slot(2, 5), new ItemBuilder(Material.CLOCK, 1, this.i18n("menu.manage.delay.name", "delay", this.crate.getDelay()), this.i18nLines("menu.manage.delay.lore")), "delay");
		this.set(slot(2, 6), new ItemBuilder(Material.BELL, 1, this.i18n("menu.manage.sound.name", "sound", this.i18n(sound == null ? "misc.no" : "misc.yes")), this.i18nLines("menu.manage.sound.lore", "sound", sound == null ? this.i18n("menu.manage.sound.none") : sound.getKey())).addLoreConditionally(sound != null, this.i18n("menu.manage.sound.play")), "sound");
	}

	@Override
	public boolean onClick(@NotNull Player player, @NotNull ClickType click, @NotNull InventoryAction action, @Nullable SlotData data) {
		if (data == null)
			return true;
		switch (data.getName()) {
		case "back":
			new CrateEditGUI(this.plugin, this.crate).showToPlayer(player);
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
				Block block = player.getTargetBlockExact(15);
				Location location = block == null ? player.getLocation() : block.getLocation();
				this.crate.setLocation(new UnloadableWorldLocation(player.getWorld(), location.getBlockX(), location.getBlockY(), location.getBlockZ()));
				this.refresh(player);
			} else if (click.isRightClick() && this.crate.getLocation() != null) {
				this.crate.setLocation(null);
				this.refresh(player);
			} else if (click == ClickType.MIDDLE && player.hasPermission(CratePermission.Command.TELEPORT)) {
				UnloadableWorldLocation location = this.crate.getLocation();
				if (location != null) {
					if (location.isWorldLoaded()) {
						player.closeInventory();
						player.teleport(location.clone().add(0.5, 0, 0.5));
						this.i18nMessage(player, "command.teleport.success", "crate", this.crate.getName());
					} else
						this.i18nMessage(player, "command.teleport.unloaded-world", "world", location.getWorldName());
				}
			}
			break;
		case "delete":
			new CrateDeleteGUI(this.plugin, this.crate, this).showToPlayer(player);
			break;
		case "name":
			this.i18nMessage(player, "menu.manage.name.request");
			player.closeInventory();
			this.spigot().requestChatMessage(player, 1, TimeUnit.MINUTES).whenComplete((name, timeout) -> {
				if (timeout == null)
					this.crate.setName(ChatColor.translateAlternateColorCodes('&', name));
				else
					this.i18nMessage(player, "menu.manage.name.timeout");
				this.run(() -> this.showToPlayer(player));
			});
			break;
		case "message":
			if (click.isLeftClick()) {
				this.i18nMessage(player, "menu.manage.message.request");
				player.closeInventory();
				this.spigot().requestChatMessage(player, 1, TimeUnit.MINUTES).whenComplete((message, timeout) -> {
					if (timeout == null)
						this.crate.setMessage(ChatColor.translateAlternateColorCodes('&', message));
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
		case "delay":
			this.i18nMessage(player, "menu.manage.delay.request");
			player.closeInventory();
			this.spigot().requestChatMessage(player, 1, TimeUnit.MINUTES).whenComplete((delay, timeout) -> {
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
		}
		return true;
	}

	@Override
	public void onClose(@NotNull Player player) {
		this.plugin.storage().getSource().saveCrate(this.crate);
	}
}
