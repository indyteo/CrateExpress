package fr.theoszanto.mc.crateexpress.models.gui.reward;

import fr.theoszanto.mc.crateexpress.CrateExpress;
import fr.theoszanto.mc.crateexpress.models.Crate;
import fr.theoszanto.mc.crateexpress.models.reward.CrateItemReward;
import fr.theoszanto.mc.crateexpress.models.reward.CrateReward;
import fr.theoszanto.mc.express.utils.ItemBuilder;
import fr.theoszanto.mc.express.utils.ItemUtils;
import fr.theoszanto.mc.express.utils.MathUtils;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.TimeUnit;

public class CrateItemRewardGUI extends CrateRewardGUI<CrateItemReward> {
	private @Nullable ItemStack item;
	private int amount = -1;

	public CrateItemRewardGUI(@NotNull CrateExpress plugin, @NotNull Crate crate) {
		super(plugin, crate);
	}

	public CrateItemRewardGUI(@NotNull CrateExpress plugin, @NotNull Crate crate, @Nullable CrateItemReward reward, @NotNull Integer slot) {
		super(plugin, crate, reward, slot);
	}

	private int getAmount() {
		return Math.max(0, this.reward == null ? this.amount : this.reward.getAmount());
	}

	private void setAmount(int amount) {
		if (this.reward == null)
			this.amount = amount;
		else
			this.reward.setAmount(amount);
	}

	private int getMaxAmount() {
		int maxStackSize;
		if (this.reward != null)
			maxStackSize = this.reward.getItem().getMaxStackSize();
		else if (this.item != null)
			maxStackSize = this.item.getMaxStackSize();
		else
			maxStackSize = 64;
		return maxStackSize * 36;
	}

	@Override
	protected void setupButtons(@NotNull Player player) {
		this.set(slot(0, 4), new ItemBuilder(Material.ITEM_FRAME, 1, this.i18n("menu.reward.item.header.name"), this.i18nLines("menu.reward.item.header.lore")));
		if (this.reward == null)
			this.set(slot(1, 2), new ItemBuilder(Material.ITEM_FRAME, 1, this.i18n("menu.reward.item.item.name", "item", this.item == null ? this.i18n("menu.reward.item.item.none") : ItemUtils.name(this.item)), this.i18nLines("menu.reward.item.item.lore")), "item");
		else
			this.set(slot(1, 2), this.reward.getItem());
		int amount = this.getAmount();
		this.set(slot(1, 4), new ItemBuilder(Material.NETHER_STAR, MathUtils.minMax(1, amount, 99), this.i18n("menu.reward.item.amount.name", "amount", amount > 0 ? amount : this.i18n("menu.reward.item.amount.none")), this.i18nLines("menu.reward.item.amount.lore"))
				.addLoreConditionally(amount > 1, this.i18n("menu.reward.item.amount.decrease"))
				.addLoreConditionally(amount < this.getMaxAmount(), this.i18n("menu.reward.item.amount.increase"))
				.addLoreConditionally(amount > 0, this.i18n("menu.reward.item.amount.reset")), "amount");
		this.setWeightButton(slot(1, 6));
	}

	@Override
	protected boolean canCreateReward() {
		return this.item != null;
	}

	@Override
	protected @NotNull CrateItemReward createReward() throws IllegalStateException {
		if (this.item == null)
			throw new IllegalStateException();
		return new CrateItemReward(this.plugin, CrateReward.generateRandomId(), this.getWeight(), this.item, this.amount);
	}

	@Override
	protected boolean onButtonClick(@NotNull Player player, @NotNull ClickType click, @NotNull InventoryAction action, @NotNull SlotData data) {
		switch (data.getName()) {
			case "item" -> {
				if (this.reward == null && action == InventoryAction.SWAP_WITH_CURSOR) {
					ItemStack item = player.getItemOnCursor();
					if (item.getType() != Material.AIR) {
						this.item = item.clone();
						this.setAmount(Math.min(this.getAmount(), this.getMaxAmount()));
						this.refresh(player);
					}
				}
			}
			case "amount" -> {
				int n = click.isShiftClick() ? 10 : 1;
				if (click.isLeftClick()) {
					if (this.getAmount() < this.getMaxAmount()) {
						this.setAmount(Math.min(this.getAmount() + n, this.getMaxAmount()));
						this.refresh(player);
					}
				} else if (click.isRightClick()) {
					if (this.getAmount() > 1) {
						this.setAmount(Math.max(1, this.getAmount() - n));
						this.refresh(player);
					}
				} else if (click == ClickType.DROP || click == ClickType.CONTROL_DROP) {
					this.setAmount(0);
					this.refresh(player);
				} else if (click == ClickType.MIDDLE) {
					this.i18nMessage(player, "menu.reward.item.amount.request");
					player.closeInventory();
					this.spigot().requestChatMessage(player, 1, TimeUnit.MINUTES).whenComplete((amount, timeout) -> {
						if (timeout == null) {
							try {
								int value = Integer.parseInt(amount);
								if (value < 0 || value > this.getMaxAmount())
									throw new NumberFormatException();
								this.setAmount(value);
							} catch (NumberFormatException e) {
								this.i18nMessage(player, "menu.reward.item.amount.invalid", "max", this.getMaxAmount());
							}
						} else
							this.i18nMessage(player, "menu.reward.item.amount.timeout");
						this.run(() -> this.showToPlayer(player));
					});
				}
			}
		}
		return true;
	}
}
