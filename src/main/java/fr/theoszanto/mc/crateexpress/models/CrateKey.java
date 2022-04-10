package fr.theoszanto.mc.crateexpress.models;

import fr.theoszanto.mc.crateexpress.CrateExpress;
import fr.theoszanto.mc.crateexpress.events.CrateGiveEvent;
import fr.theoszanto.mc.crateexpress.models.reward.CrateKeyReward;
import fr.theoszanto.mc.crateexpress.utils.ItemUtils;
import fr.theoszanto.mc.crateexpress.utils.PluginObject;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
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

	public void giveTo(@NotNull Player player, int amount, @NotNull CommandSender commandSource) {
		this.giveTo(player, amount, CrateGiveEvent.Cause.GIVE_COMMAND, commandSource, null);
	}

	public void giveTo(@NotNull Player player, int amount, @NotNull CrateGiveEvent.AdminGUIGiveButton adminSource) {
		this.giveTo(player, amount, CrateGiveEvent.Cause.ADMIN_GIVE, null, adminSource);
	}

	private void giveTo(@NotNull Player player, int amount, @NotNull CrateGiveEvent.Cause cause, @Nullable CommandSender commandSource, @Nullable CrateGiveEvent.AdminGUIGiveButton adminSource) {
		Inventory inventory = player.getInventory();
		boolean saving = inventory.firstEmpty() == -1;
		CrateGiveEvent event = new CrateGiveEvent(player, saving, cause, this, amount, commandSource, adminSource);
		if (this.event(event)) {
			CrateKey key = event.getKey();
			if (saving)
				this.storage().saveReward(player, new CrateKeyReward(this.plugin, 0, key.getCrateId(), event.getAmount()));
			else
				player.getInventory().addItem(ItemUtils.withAmount(key.getItem(), event.getAmount()));
			this.i18nMessage(player, "action.key.receive", "key", ItemUtils.name(key.getItem()));
		}
	}
}
