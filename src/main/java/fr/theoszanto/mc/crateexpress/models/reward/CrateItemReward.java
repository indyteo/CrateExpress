package fr.theoszanto.mc.crateexpress.models.reward;

import fr.theoszanto.mc.crateexpress.CrateExpress;
import fr.theoszanto.mc.express.utils.ItemUtils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public class CrateItemReward extends CrateReward {
	private final @NotNull ItemStack item;
	private int amount;

	public CrateItemReward(@NotNull CrateExpress plugin, @NotNull String id, double weight, @NotNull ItemStack item, int amount) {
		super(plugin, id, "item", ItemUtils.EMPTY, weight, true);
		this.item = item;
		this.amount = amount;
	}

	@Override
	public @NotNull ItemStack getIcon() {
		if (this.amount <= 0)
			return this.item;
		if (this.amount < 100) {
			ItemStack icon = this.item.clone();
			icon.editMeta(meta -> meta.setMaxStackSize(this.amount));
			icon.setAmount(this.amount);
			return icon;
		}
		ItemStack icon = this.item.asOne();
		icon.editMeta(meta -> meta.customName(Component.text()
				.append(Component.text(this.amount + "x ", NamedTextColor.GRAY).decoration(TextDecoration.ITALIC, false))
				.append(icon.effectiveName())
				.build()));
		return icon;
	}

	@Override
	protected boolean cannotGiveTo(@NotNull Player player) {
		int space = 0;
		for (ItemStack content : player.getInventory().getStorageContents()) {
			if (content == null)
				space += this.item.getMaxStackSize();
			else if (content.isSimilar(this.item))
				space += content.getMaxStackSize() - content.getAmount();
		}
		return space < this.getEffectiveAmount();
	}

	@Override
	protected void reward(@NotNull Player player) throws RewardGiveException {
		player.getInventory().addItem(this.item.asQuantity(this.getEffectiveAmount()));
	}

	@Override
	public @NotNull String describe() {
		return (this.amount < 100 ? this.getEffectiveAmount() + "x " : "") + super.describe();
	}

	public @NotNull ItemStack getItem() {
		return this.item;
	}

	public int getEffectiveAmount() {
		return this.amount > 0 ? this.amount : this.item.getAmount();
	}

	public int getAmount() {
		return this.amount;
	}

	public void setAmount(int amount) {
		this.amount = amount;
	}
}
