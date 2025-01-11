package fr.theoszanto.mc.crateexpress.listeners;

import fr.theoszanto.mc.crateexpress.CrateExpress;
import fr.theoszanto.mc.crateexpress.events.interaction.CrateOpenInteractEvent;
import fr.theoszanto.mc.crateexpress.events.interaction.CratePreviewInteractEvent;
import fr.theoszanto.mc.crateexpress.models.Crate;
import fr.theoszanto.mc.crateexpress.models.gui.CrateInteractSelectGUI;
import fr.theoszanto.mc.crateexpress.models.gui.CratePreviewGUI;
import fr.theoszanto.mc.crateexpress.utils.CratePermission;
import fr.theoszanto.mc.crateexpress.utils.Pair;
import fr.theoszanto.mc.crateexpress.utils.TimeUtils;
import fr.theoszanto.mc.express.listeners.ExpressListener;
import fr.theoszanto.mc.express.utils.ItemUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
	private void onPlayerInteractEntity(@NotNull PlayerInteractEntityEvent event) {
		if (event.getHand() == EquipmentSlot.OFF_HAND)
			return;
		Entity entity = event.getRightClicked();
		if (entity.getType() != EntityType.INTERACTION)
			return;
		Player player = event.getPlayer();
		this.crateInteraction(player, player.getInventory().getItemInMainHand(), entity.getLocation(), true, event);
	}

	@EventHandler
	private void onEntityDamageByEntity(@NotNull EntityDamageByEntityEvent event) {
		Entity entity = event.getEntity();
		if (entity.getType() != EntityType.INTERACTION)
			return;
		Entity damager = event.getDamager();
		if (damager.getType() != EntityType.PLAYER)
			return;
		Player player = (Player) damager;
		this.crateInteraction(player, player.getInventory().getItemInMainHand(), entity.getLocation(), false, event);
	}

	@EventHandler
	private void onPlayerInteract(@NotNull PlayerInteractEvent event) {
		if (event.getHand() == EquipmentSlot.OFF_HAND)
			return;
		Player player = event.getPlayer();
		if (this.droppers.containsKey(player))
			return;
		Action action = event.getAction();
		if (action == Action.PHYSICAL)
			return;
		Block block = event.getClickedBlock();
		this.crateInteraction(player, event.getItem(), block == null ? null : block.getLocation(), action == Action.RIGHT_CLICK_AIR || action == Action.RIGHT_CLICK_BLOCK, event);
	}

	private void crateInteraction(@NotNull Player player, @Nullable ItemStack item, @Nullable Location location, boolean isRightClick, @NotNull Cancellable event) {
		List<Crate> clickedCrates = this.plugin.crates().byLocation(location);
		List<Crate> usedKeyCrates = this.plugin.crates().byItem(item);
		// If the interaction is a right click => Open crate
		if (isRightClick) {
			// If the player didn't use a key
			if (usedKeyCrates.isEmpty()) {
				// Maybe the player clicked on a crate
				if (!clickedCrates.isEmpty())
					event.setCancelled(true);
				Stream<Crate> clickedCratesStream = clickedCrates.stream();
				if (!player.hasPermission(CratePermission.BYPASS_DISABLED))
					clickedCratesStream = clickedCratesStream.filter(((Predicate<Crate>) Crate::isDisabled).negate());
				String enabledClickedCratesNames = clickedCratesStream.map(Crate::getName)
						.collect(Collectors.joining(this.i18n("action.crate.need-key.delimiter")));
				if (!enabledClickedCratesNames.isEmpty())
					this.i18nMessage(player, "action.crate.need-key.message", "crates", enabledClickedCratesNames);
			} else { // Otherwise, the player did use a key so the item cannot be null
				assert item != null;
				event.setCancelled(true);
				// Callback to perform when we know which crate the player wants to open
				Consumer<Crate> openCrate = crate -> {
					// Check last crate usage by this player (time cooldown)
					Pair<Player, Crate> pair = new Pair<>(player, crate);
					Instant lastUsage = this.lastUsages.get(pair);
					Instant now = Instant.now();
					Duration delay = lastUsage == null ? Duration.ZERO : Duration.between(lastUsage, now).minusMillis(crate.getDelayMillis());
					if (!delay.isNegative()) {
						// Check if player has enough space in /crate claim just in case
						if (this.plugin.crates().noLimitToPlayerRewards() || player.hasPermission(CratePermission.UNLIMITED_CLAIM)
								|| this.plugin.storage().getSource().countRewards(player) <= this.plugin.crates().getMaximumPlayerRewards()) {
							// Trigger CrateOpenInteractEvent and check if it wasn't cancelled
							CrateOpenInteractEvent e = new CrateOpenInteractEvent(crate, player, item, player.isSneaking() ? item.getAmount() : 1);
							if (this.event(e)) {
								int amount = e.getAmount();
								// Consume key if necessary
								if (e.doesConsumingKey())
									item.setAmount(item.getAmount() - amount);
								// Open crate
								for (int i = 0; i < amount; i++)
									crate.open(player, true);
								this.i18nMessage(player, "action.crate.open", "crate", crate.getName());
								// Broadcast message if necessary
								if (e.doesBroadcastMessage()) {
									String message = crate.getFormattedMessage(player);
									if (message != null)
										for (Player p : Bukkit.getOnlinePlayers())
											p.sendMessage(message);
								}
								// Play sound if necessary
								if (e.doesPlaySound())
									crate.playSoundAtLocation(crate.isOpenableAnywhere() || location == null ? player.getLocation() : location);
								// Record crate usage by this player
								this.lastUsages.put(pair, now);
							}
						} else
							this.i18nMessage(player, "action.crate.too-much-rewards", "crate", crate.getName());
					} else
						this.i18nMessage(player, "action.crate.must-wait", "crate", crate.getName(), "delay", TimeUtils.formatDuration(delay));
				};

				// The key the player used is only assigned to 1 crate
				if (usedKeyCrates.size() == 1) {
					Crate crate = usedKeyCrates.getFirst();
					// Check if player can use the crate
					if (!crate.isDisabled() || player.hasPermission(CratePermission.BYPASS_DISABLED)) {
						// Check if crate can be opened from here
						if (location == null ? crate.isOpenableAnywhere() : crate.isOpenableAtLocation(location))
							openCrate.accept(crate);
						else
							this.i18nMessage(player, "action.key.use", "crate", crate.getName());
					} else
						this.i18nMessage(player, "action.crate.disabled", "crate", crate.getName());
				} else { // Otherwise, there are multiple crates
					// Filter accessible crates
					Stream<Crate> usedKeyCratesStream = usedKeyCrates.stream();
					if (!player.hasPermission(CratePermission.BYPASS_DISABLED))
						usedKeyCratesStream = usedKeyCratesStream.filter(Predicate.not(Crate::isDisabled));

					List<Crate> accessibleCrates = usedKeyCratesStream.toList();
					if (accessibleCrates.isEmpty()) {
						this.i18nMessage(player, "action.key.cannot-use", "key", ItemUtils.name(item));
						return;
					}

					// Filter crate openable here
					usedKeyCratesStream = accessibleCrates.stream();
					if (location == null)
						usedKeyCratesStream = usedKeyCratesStream.filter(Crate::isOpenableAnywhere);
					else
						usedKeyCratesStream = usedKeyCratesStream.filter(crate -> crate.isOpenableAtLocation(location));

					List<Crate> crates = usedKeyCratesStream.toList();
					if (crates.isEmpty()) {
						this.i18nMessage(player, "action.key.cannot-use-here", "key", ItemUtils.name(item));
						return;
					}

					// Ask player to select a crate if needed then open it
					if (crates.size() == 1)
						openCrate.accept(crates.getFirst());
					else
						new CrateInteractSelectGUI(this.plugin, crates, true, openCrate).showToPlayer(player);
				}
			}
		} else { // Left click => Preview
			// Check if there is a crate to preview
			if (clickedCrates.isEmpty() && usedKeyCrates.isEmpty())
				return;
			event.setCancelled(true);

			// Filter crate previewable by the player
			Stream<Crate> cratesStream = Stream.concat(clickedCrates.stream(), usedKeyCrates.stream()).distinct();
			if (!player.hasPermission(CratePermission.BYPASS_NO_PREVIEW))
				cratesStream = cratesStream.filter(((Predicate<Crate>) Crate::isNoPreview).negate());
			List<Crate> crates = cratesStream.toList();
			if (crates.isEmpty()) {
				this.i18nMessage(player, "action.crate.no-preview-here");
				return;
			}

			// Callback to perform to preview a crate
			Consumer<Crate> previewCrate = crate -> {
				if (this.event(new CratePreviewInteractEvent(crate, player, clickedCrates.contains(crate))))
					new CratePreviewGUI(this.plugin, crate).showToPlayer(player);
			};

			// Ask player to select a crate if needed then preview it
			if (crates.size() == 1)
				previewCrate.accept(crates.getFirst());
			else
				new CrateInteractSelectGUI(this.plugin, crates, false, previewCrate).showToPlayer(player);
		}
	}
}
