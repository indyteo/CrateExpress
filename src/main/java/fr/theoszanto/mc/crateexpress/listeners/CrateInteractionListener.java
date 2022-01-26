package fr.theoszanto.mc.crateexpress.listeners;

import fr.theoszanto.mc.crateexpress.CrateExpress;
import fr.theoszanto.mc.crateexpress.models.Crate;
import fr.theoszanto.mc.crateexpress.models.gui.CratePreviewGUI;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public class CrateInteractionListener extends CrateListener {
	public CrateInteractionListener(@NotNull CrateExpress plugin) {
		super(plugin);
	}

	// TODO Split into multiple interactions listeners to avoid wierd Spigot event call
	@EventHandler
	private void onPlayerInteract(@NotNull PlayerInteractEvent event) {
		Player player = event.getPlayer();
		ItemStack item = event.getItem();
		Block block = event.getClickedBlock();
		Action action = event.getAction();
		Optional<Crate> clickedCrate = this.crates().byLocation(block == null ? null : block.getLocation());
		Optional<Crate> usedKeyCrate = this.crates().byItem(item);
		if (action == Action.RIGHT_CLICK_AIR || action == Action.RIGHT_CLICK_BLOCK) {
			if (usedKeyCrate.isPresent()) {
				assert item != null;
				Crate crate = usedKeyCrate.get();
				Location crateLocation = crate.getLocation();
				if (crateLocation == null || (block != null && block.getLocation().equals(crateLocation))) {
					if (this.crates().noLimitToPlayerRewards() || this.storage().countRewards(player) <= this.crates().getMaximumPlayerRewards()) {
						item.setAmount(item.getAmount() - 1);
						crate.open(player);
						this.i18nMessage(player, "action.crate.open", "crate", crate.getName());
						String message = crate.getMessage();
						if (message != null) {
							String formatted = message.replaceAll("<player>", player.getName()).replaceAll("<display>", player.getDisplayName());
							for (Player p : Bukkit.getOnlinePlayers())
								p.sendMessage(this.prefix() + formatted);
						}
					} else
						this.i18nMessage(player, "action.crate.too-much-rewards", "crate", crate.getName());
				} else
					this.i18nMessage(player, "action.key.use", "crate", crate.getName());
				event.setCancelled(true);
			} else if (clickedCrate.isPresent()) {
				this.i18nMessage(player, "action.crate.need-key", "crate", clickedCrate.get().getName());
				event.setCancelled(true);
			}
		} else if (action == Action.LEFT_CLICK_AIR || action == Action.LEFT_CLICK_BLOCK) {
			if (clickedCrate.isPresent()) {
				new CratePreviewGUI(this.plugin, clickedCrate.get()).showToPlayer(player);
				event.setCancelled(true);
			} else if (usedKeyCrate.isPresent()) {
				new CratePreviewGUI(this.plugin, usedKeyCrate.get()).showToPlayer(player);
				event.setCancelled(true);
			}
		}
	}
}
