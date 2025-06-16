package fr.theoszanto.mc.crateexpress.models;

import fr.theoszanto.mc.crateexpress.CrateExpress;
import fr.theoszanto.mc.crateexpress.events.CrateGiveEvent;
import fr.theoszanto.mc.crateexpress.models.reward.CrateKeyReward;
import fr.theoszanto.mc.crateexpress.models.reward.CrateReward;
import fr.theoszanto.mc.crateexpress.utils.PluginObject;
import fr.theoszanto.mc.express.utils.ItemUtils;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;

public class CrateKey extends PluginObject {
	private final @NotNull String crateId;
	private final @NotNull ItemStack item;

	public CrateKey(@NotNull CrateExpress plugin, @NotNull String crateId, @NotNull ItemStack item) {
		super(plugin);
		this.crateId = crateId;
		this.item = ItemUtils.unmodifiableItemStack(item);
	}

	public @NotNull String getCrateId() {
		return this.crateId;
	}

	@Unmodifiable
	public @NotNull ItemStack getItem() {
		return this.item;
	}

	public void giveTo(@NotNull OfflinePlayer player, int amount, @NotNull CommandSender commandSource, boolean all) {
		this.giveTo(player, amount, all ? CrateGiveEvent.Cause.GIVE_ALL_COMMAND : CrateGiveEvent.Cause.GIVE_TO_COMMAND, commandSource, null, null);
	}

	public void giveTo(@NotNull OfflinePlayer player, int amount, @NotNull CrateGiveEvent.AdminGUIGiveButton adminSource) {
		this.giveTo(player, amount, CrateGiveEvent.Cause.ADMIN_GIVE, null, adminSource, null);
	}

	public void giveTo(@NotNull OfflinePlayer player, int amount, @NotNull JavaPlugin pluginSource) {
		this.giveTo(player, amount, CrateGiveEvent.Cause.PLUGIN_GIVE, null, null, pluginSource);
	}

	private void giveTo(@NotNull OfflinePlayer player, int amount, @NotNull CrateGiveEvent.Cause cause, @Nullable CommandSender commandSource, @Nullable CrateGiveEvent.AdminGUIGiveButton adminSource, @Nullable JavaPlugin pluginSource) {
		boolean saving = !(player instanceof Player onlinePlayer) || onlinePlayer.getInventory().firstEmpty() == -1;
		CrateGiveEvent event = new CrateGiveEvent(player, saving, cause, this, amount, commandSource, adminSource, pluginSource);
		if (this.event(event)) {
			CrateKey key = event.getKey();
			if (saving) {
				this.async(() -> this.storage().getSource().saveReward(player, new CrateKeyReward(this.plugin, CrateReward.generateRandomId(), 0, key.getCrateId(), event.getAmount())));
				if (player instanceof Player onlinePlayer)
					this.i18nMessage(onlinePlayer, "action.key.stored", "key", ItemUtils.name(key.getItem()));
			} else {
				Player onlinePlayer = (Player) player;
				onlinePlayer.getInventory().addItem(ItemUtils.withAmount(key.getItem(), event.getAmount()));
				this.i18nMessage(onlinePlayer, "action.key.receive", "key", ItemUtils.name(key.getItem()));
			}
		}
	}
}
