package fr.theoszanto.mc.crateexpress.models;

import fr.theoszanto.mc.crateexpress.CrateExpress;
import fr.theoszanto.mc.crateexpress.models.reward.CrateKeyReward;
import fr.theoszanto.mc.crateexpress.utils.ItemUtils;
import fr.theoszanto.mc.crateexpress.utils.PluginObject;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.UnmodifiableView;

public class CrateKey extends PluginObject {
	private final @NotNull String crateId;
	private final @NotNull ItemStack item;

	@UnmodifiableView
	private final @NotNull ItemStack itemUnmodifiable;

	public CrateKey(@NotNull CrateExpress plugin, @NotNull String crateId, @NotNull ItemStack item) {
		super(plugin);
		this.crateId = crateId;
		this.item = item;
		this.itemUnmodifiable = ItemUtils.unmodifiableItemStack(item);
	}

	public @NotNull String getCrateId() {
		return this.crateId;
	}

	@UnmodifiableView
	public @NotNull ItemStack getItem() {
		return this.itemUnmodifiable;
	}

	public void giveTo(@NotNull Player player, int amount) {
		Inventory inventory = player.getInventory();
		if (inventory.firstEmpty() == -1)
			this.storage().saveReward(player, new CrateKeyReward(this.plugin, 0, this.crateId, amount));
		else
			player.getInventory().addItem(ItemUtils.withAmount(this.item, amount));
		this.i18nMessage(player, "action.key.receive", "key", ItemUtils.name(this.item));
	}
}
