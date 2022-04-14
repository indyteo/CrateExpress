package fr.theoszanto.mc.crateexpress.models.gui.reward;

import fr.theoszanto.mc.crateexpress.CrateExpress;
import fr.theoszanto.mc.crateexpress.models.Crate;
import fr.theoszanto.mc.crateexpress.models.reward.CrateMoneyReward;
import fr.theoszanto.mc.crateexpress.utils.ItemBuilder;
import fr.theoszanto.mc.crateexpress.utils.ItemUtils;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryAction;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.TimeUnit;

public class CrateMoneyRewardGUI extends CrateRewardGUI<CrateMoneyReward> {
	private double amount;

	public CrateMoneyRewardGUI(@NotNull CrateExpress plugin, @NotNull Crate crate) {
		super(plugin, crate);
	}

	public CrateMoneyRewardGUI(@NotNull CrateExpress plugin, @NotNull Crate crate, @Nullable CrateMoneyReward reward, @NotNull Integer slot) {
		super(plugin, crate, reward, slot);
	}

	@Override
	protected void setupButtons(@NotNull Player player) {
		this.set(slot(0, 4), new ItemBuilder(Material.EMERALD, 1, this.i18n("menu.reward.money.header.name"), this.i18nLines("menu.reward.money.header.lore")));
		if (this.reward == null)
			this.set(slot(1, 2), new ItemBuilder(Material.EMERALD, ItemUtils.stackAmountFromValue(this.amount), this.i18n("menu.reward.money.amount.name", "amount", this.money().formatMoney(this.amount)), this.i18nLines("menu.reward.money.amount.lore")), "amount");
		else
			this.set(slot(1, 2), this.reward.getIcon());
		this.setWeightButton(slot(1, 6));
	}

	@Override
	protected boolean canCreateReward() {
		return this.amount > 0;
	}

	@Override
	protected @NotNull CrateMoneyReward createReward() throws IllegalStateException {
		if (this.amount <= 0)
			throw new IllegalStateException();
		return new CrateMoneyReward(this.plugin, this.getWeight(), this.amount);
	}

	@Override
	protected boolean onButtonClick(@NotNull Player player, @NotNull ClickType click, @NotNull InventoryAction action, @NotNull SlotData data) {
		if (data.getName().equals("amount") && this.reward == null) {
			this.i18nMessage(player, "menu.reward.money.request");
			player.closeInventory();
			this.spigot().requestChatMessage(player, 1, TimeUnit.MINUTES).whenComplete((amount, timeout) -> {
				if (timeout == null) {
					try {
						this.amount = Double.parseDouble(amount);
					} catch (NumberFormatException e) {
						this.i18nMessage(player, "menu.reward.money.invalid");
					}
				} else
					this.i18nMessage(player, "menu.reward.money.timeout");
				this.run(() -> this.showToPlayer(player));
			});
		}
		return true;
	}
}
