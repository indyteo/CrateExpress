package fr.theoszanto.mc.crateexpress.listeners;

import fr.theoszanto.mc.crateexpress.CrateExpress;
import fr.theoszanto.mc.express.listeners.ExpressListener;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerJoinEvent;
import org.jetbrains.annotations.NotNull;

public class PlayerListener extends ExpressListener<CrateExpress> {
	public PlayerListener(@NotNull CrateExpress plugin) {
		super(plugin);
	}

	@EventHandler
	private void onPlayerJoin(@NotNull PlayerJoinEvent event) {
		Player player = event.getPlayer();
		this.plugin.store(storage -> storage.invalidateCache(player.getUniqueId()));
		if (this.plugin.crates().isClaimNoticeOnLogin())
			this.plugin.crates().noticePlayerIfCanClaim(player);
	}
}
