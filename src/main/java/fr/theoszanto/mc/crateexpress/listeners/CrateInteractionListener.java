package fr.theoszanto.mc.crateexpress.listeners;

import fr.theoszanto.mc.crateexpress.CrateExpress;
import fr.theoszanto.mc.crateexpress.events.interaction.CrateOpenInteractEvent;
import fr.theoszanto.mc.crateexpress.events.interaction.CratePreviewInteractEvent;
import fr.theoszanto.mc.crateexpress.models.Crate;
import fr.theoszanto.mc.crateexpress.models.gui.CratePreviewGUI;
import fr.theoszanto.mc.crateexpress.utils.CratePermission;
import fr.theoszanto.mc.crateexpress.utils.Pair;
import fr.theoszanto.mc.crateexpress.utils.TimeUtils;
import fr.theoszanto.mc.express.listeners.ExpressListener;
import fr.theoszanto.mc.express.utils.LocationUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;

import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class CrateInteractionListener extends ExpressListener<CrateExpress> {
	private final @NotNull Map<@NotNull Player, @NotNull BukkitRunnable> droppers = new HashMap<>();
	private final @NotNull Map<@NotNull Pair<@NotNull Player, @NotNull Crate>, @NotNull Instant> lastUsages = new HashMap<>();

	public CrateInteractionListener(@NotNull CrateExpress plugin) {
		super(plugin);
	}

	// https://hub.spigotmc.org/jira/browse/SPIGOT-5974
	@EventHandler
	private void onPlayerDropItem(@NotNull PlayerDropItemEvent event) {
		Player player = event.getPlayer();
		BukkitRunnable runnable = new BukkitRunnable() {
			@Override
			public void run() {
				CrateInteractionListener.this.droppers.remove(player);
			}
		};
		BukkitRunnable previous = this.droppers.put(player, runnable);
		if (previous != null)
			previous.cancel();
		runnable.runTaskLater(this.plugin, 1);
	}

	@EventHandler
	private void onPlayerInteract(@NotNull PlayerInteractEvent event) {
		if (event.getHand() == EquipmentSlot.OFF_HAND)
			return;
		Player player = event.getPlayer();
		if (this.droppers.containsKey(player))
			return;
		ItemStack item = event.getItem();
		Block block = event.getClickedBlock();
		Action action = event.getAction();
		Optional<Crate> clickedCrate = this.plugin.crates().byLocation(block == null ? null : block.getLocation());
		Optional<Crate> usedKeyCrate = this.plugin.crates().byItem(item);
		if (action == Action.RIGHT_CLICK_AIR || action == Action.RIGHT_CLICK_BLOCK) {
			if (usedKeyCrate.isPresent()) {
				assert item != null;
				Crate crate = usedKeyCrate.get();
				Location crateLocation = crate.getLocation();
				if (crateLocation == null || (block != null && LocationUtils.blockEquals(block.getLocation(), crateLocation))) {
					Pair<Player, Crate> pair = new Pair<>(player, crate);
					Instant lastUsage = this.lastUsages.get(pair);
					Instant now = Instant.now();
					Duration delay = lastUsage == null ? Duration.ZERO : Duration.between(lastUsage, now).minusMillis(crate.getDelayMillis());
					if (!delay.isNegative()) {
						if (this.plugin.crates().noLimitToPlayerRewards() || player.hasPermission(CratePermission.UNLIMITED_CLAIM)
								|| this.plugin.storage().getSource().countRewards(player) <= this.plugin.crates().getMaximumPlayerRewards()) {
							CrateOpenInteractEvent e = new CrateOpenInteractEvent(crate, player, item, player.isSneaking() ? item.getAmount() : 1);
							if (this.event(e)) {
								int amount = e.getAmount();
								if (e.doesConsumingKey())
									item.setAmount(item.getAmount() - amount);
								for (int i = 0; i < amount; i++)
									crate.open(player);
								this.i18nMessage(player, "action.crate.open", "crate", crate.getName());
								if (e.doesBroadcastMessage()) {
									String message = crate.getFormattedMessage(player);
									if (message != null)
										for (Player p : Bukkit.getOnlinePlayers())
											p.sendMessage(message);
								}
								if (e.doesPlaySound())
									crate.playSoundAtLocation();
								this.lastUsages.put(pair, now);
							}
						} else
							this.i18nMessage(player, "action.crate.too-much-rewards", "crate", crate.getName());
					} else
						this.i18nMessage(player, "action.crate.must-wait", "crate", crate.getName(), "delay", TimeUtils.formatDuration(delay));
				} else
					this.i18nMessage(player, "action.key.use", "crate", crate.getName());
				event.setCancelled(true);
			} else if (clickedCrate.isPresent()) {
				this.i18nMessage(player, "action.crate.need-key", "crate", clickedCrate.get().getName());
				event.setCancelled(true);
			}
		} else if (action == Action.LEFT_CLICK_AIR || action == Action.LEFT_CLICK_BLOCK) {
			if (clickedCrate.isPresent()) {
				Crate crate = clickedCrate.get();
				if (this.event(new CratePreviewInteractEvent(crate, player, true)))
					new CratePreviewGUI(this.plugin, crate).showToPlayer(player);
				event.setCancelled(true);
			} else if (usedKeyCrate.isPresent()) {
				Crate crate = usedKeyCrate.get();
				if (this.event(new CratePreviewInteractEvent(crate, player, false)))
					new CratePreviewGUI(this.plugin, crate).showToPlayer(player);
				event.setCancelled(true);
			}
		}
	}
}
