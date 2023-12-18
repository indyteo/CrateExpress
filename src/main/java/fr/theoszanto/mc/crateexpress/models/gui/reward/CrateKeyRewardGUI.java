package fr.theoszanto.mc.crateexpress.models.gui.reward;

import fr.theoszanto.mc.crateexpress.CrateExpress;
import fr.theoszanto.mc.crateexpress.models.Crate;
import fr.theoszanto.mc.crateexpress.models.CrateKey;
import fr.theoszanto.mc.crateexpress.models.gui.CrateSelectGUI;
import fr.theoszanto.mc.crateexpress.models.reward.CrateKeyReward;
import fr.theoszanto.mc.crateexpress.models.reward.CrateReward;
import fr.theoszanto.mc.express.utils.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryAction;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class CrateKeyRewardGUI extends CrateRewardGUI<CrateKeyReward> {
	private @Nullable String key;
	private int amount = 1;

	public CrateKeyRewardGUI(@NotNull CrateExpress plugin, @NotNull Crate crate) {
		super(plugin, crate);
	}

	public CrateKeyRewardGUI(@NotNull CrateExpress plugin, @NotNull Crate crate, @Nullable CrateKeyReward reward, @NotNull Integer slot) {
		super(plugin, crate, reward, slot);
	}

	public int getAmount() {
		return this.reward == null ? this.amount : this.reward.getAmount();
	}

	public void setAmount(int amount) {
		if (this.reward == null)
			this.amount = amount;
		else
			this.reward.setAmount(amount);
	}

	private void selectCrate(@NotNull String key) {
		this.key = key;
		this.amount = Math.min(this.amount, this.getMaxAmount());
	}

	private int getMaxAmount() {
		if (this.key != null) {
			try {
				CrateKey key = this.plugin.crates().get(this.key).getKey();
				if (key != null) {
					int max = key.getItem().getMaxStackSize();
					if (max != -1)
						return max;
				}
			} catch (IllegalArgumentException ignored) {}
		}
		return 64;
	}

	@Override
	protected void setupButtons(@NotNull Player player) {
		this.set(slot(0, 4), new ItemBuilder(Material.TRIPWIRE_HOOK, 1, this.i18n("menu.reward.key.header.name"), this.i18nLines("menu.reward.key.header.lore")));
		if (this.reward == null)
			this.set(slot(1, 2), new ItemBuilder(Material.TRIPWIRE_HOOK, 1, this.i18n("menu.reward.key.key.name", "key", this.key == null ? this.i18n("menu.reward.key.key.none") : this.key), this.i18nLines("menu.reward.key.key.lore")), "key");
		else
			this.set(slot(1, 2), this.reward.getIcon());
		this.set(slot(1, 4), new ItemBuilder(Material.NETHER_STAR, this.getAmount(), this.i18n("menu.reward.key.amount.name", "amount", this.getAmount()), this.i18nLines("menu.reward.key.amount.lore"))
				.addLoreConditionally(this.getAmount() > 1, this.i18n("menu.reward.key.amount.decrease"))
				.addLoreConditionally(this.getAmount() < this.getMaxAmount(), this.i18n("menu.reward.key.amount.increase")), "amount");
		this.setWeightButton(slot(1, 6));
	}

	@Override
	protected boolean canCreateReward() {
		return this.key != null;
	}

	@Override
	protected @NotNull CrateKeyReward createReward() throws IllegalStateException {
		if (this.key == null)
			throw new IllegalStateException();
		return new CrateKeyReward(this.plugin, CrateReward.generateRandomId(), this.getWeight(), this.key, this.amount);
	}

	@Override
	protected boolean onButtonClick(@NotNull Player player, @NotNull ClickType click, @NotNull InventoryAction action, @NotNull SlotData data) {
		switch (data.getName()) {
		case "key":
			if (this.reward == null)
				new CrateSelectGUI(this.plugin, true, this, this::selectCrate).showToPlayer(player);
			break;
		case "amount":
			if (click.isLeftClick()) {
				if (this.getAmount() < this.getMaxAmount()) {
					this.setAmount(this.getAmount() + 1);
					this.refresh(player);
				}
			} else if (click.isRightClick()) {
				if (this.getAmount() > 1) {
					this.setAmount(this.getAmount() - 1);
					this.refresh(player);
				}
			}
			break;
		}
		return true;
	}
}
