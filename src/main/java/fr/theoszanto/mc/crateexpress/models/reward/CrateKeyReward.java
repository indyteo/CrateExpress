package fr.theoszanto.mc.crateexpress.models.reward;

import fr.theoszanto.mc.crateexpress.CrateExpress;
import fr.theoszanto.mc.crateexpress.models.CrateKey;
import fr.theoszanto.mc.crateexpress.utils.ItemBuilder;
import fr.theoszanto.mc.crateexpress.utils.ItemUtils;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class CrateKeyReward extends CrateReward {
	private final @NotNull String key;
	private int amount;

	public CrateKeyReward(@NotNull CrateExpress plugin, int weight, @NotNull String key, int amount) {
		super(plugin, "key", ItemUtils.EMPTY, weight, true);
		this.key = key;
		this.amount = amount;
	}

	private @Nullable CrateKey fetchCrateKey() {
		try {
			return this.crates().get(this.key).getKey();
		} catch (IllegalArgumentException e) {
			return null;
		}
	}

	@Override
	public @NotNull ItemStack getIcon() {
		CrateKey key = this.fetchCrateKey();
		return ItemUtils.withAmount(key == null ? this.unknownKeyItem() : key.getItem(), this.amount);
	}

	@Override
	public void reward(@NotNull Player player) throws RewardGiveException {
		CrateKey key = this.fetchCrateKey();
		if (key == null)
			throw new RewardGiveException("Unknown key: " + this.key);
		player.getInventory().addItem(ItemUtils.withAmount(key.getItem(), this.amount));
	}

	@Override
	public @NotNull String describe() {
		return "x" + this.amount + " " + super.describe();
	}

	public @NotNull String getKey() {
		return this.key;
	}

	public int getAmount() {
		return this.amount;
	}

	public void setAmount(int amount) {
		this.amount = amount;
	}

	private @NotNull ItemStack unknownKeyItem() {
		return new ItemBuilder(Material.BARRIER, 1, this.i18n("crate.preview.unknown-key", "key", this.key)).buildUnmodifiable();
	}
}
