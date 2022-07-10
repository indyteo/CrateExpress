package fr.theoszanto.mc.crateexpress.models.gui.reward;

import fr.theoszanto.mc.crateexpress.CrateExpress;
import fr.theoszanto.mc.crateexpress.models.Crate;
import fr.theoszanto.mc.crateexpress.models.gui.CrateSelectGUI;
import fr.theoszanto.mc.crateexpress.models.reward.CrateOtherReward;
import fr.theoszanto.mc.express.utils.ItemBuilder;
import fr.theoszanto.mc.express.utils.ItemUtils;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class CrateOtherRewardGUI extends CrateRewardGUI<CrateOtherReward> {
	private @NotNull ItemStack icon;
	private @Nullable String crate;
	private boolean random = true;

	public CrateOtherRewardGUI(@NotNull CrateExpress plugin, @NotNull Crate crate) {
		super(plugin, crate);
		this.icon = this.defaultIcon();
	}

	public CrateOtherRewardGUI(@NotNull CrateExpress plugin, @NotNull Crate crate, @Nullable CrateOtherReward reward, @NotNull Integer slot) {
		super(plugin, crate, reward, slot);
		this.icon = this.defaultIcon();
	}

	private @NotNull ItemStack defaultIcon() {
		return new ItemBuilder(Material.CHEST_MINECART, 1, this.i18n("misc.default-other-icon-name")).build();
	}

	private void setIcon(@NotNull ItemStack icon) {
		if (this.reward == null)
			this.icon = icon;
		else
			this.reward.setIcon(icon);
	}

	private boolean isRandom() {
		return this.reward == null ? this.random : this.reward.isRandom();
	}

	private void setRandom(boolean random) {
		if (this.reward == null)
			this.random = random;
		else
			this.reward.setRandom(random);
	}

	@Override
	protected void setupButtons(@NotNull Player player) {
		this.set(slot(0, 4), new ItemBuilder(Material.CHEST, 1, this.i18n("menu.reward.other.header.name"), this.i18nLines("menu.reward.other.header.lore")));
		this.set(slot(1, 1), new ItemBuilder(Material.PAINTING, 1, this.i18n("menu.reward.other.icon.name", "icon", ItemUtils.name(this.icon)), this.i18nLines("menu.reward.other.icon.lore")), "icon");
		if (this.reward == null)
			this.set(slot(1, 3), new ItemBuilder(Material.CHEST, 1, this.i18n("menu.reward.other.crate.name", "crate", this.crate == null ? this.i18n("menu.reward.other.crate.none") : this.crate), this.i18nLines("menu.reward.other.crate.lore")), "crate");
		else
			this.set(slot(1, 3), this.reward.getIcon());
		this.setWeightButton(slot(1, 5));
		this.set(slot(1, 7), new ItemBuilder(this.isRandom() ? Material.HOPPER : Material.BARREL, 1, this.i18n("menu.reward.other.random.name", "random", this.i18n(this.isRandom() ? "misc.yes" : "misc.no")), this.i18nLines("menu.reward.other.random.lore")), "random");
	}

	@Override
	protected boolean canCreateReward() {
		return this.crate != null;
	}

	@Override
	protected @NotNull CrateOtherReward createReward() throws IllegalStateException {
		if (this.crate == null)
			throw new IllegalStateException();
		return new CrateOtherReward(this.plugin, this.icon, this.getWeight(), this.crate, this.random);
	}

	@Override
	protected boolean onButtonClick(@NotNull Player player, @NotNull ClickType click, @NotNull InventoryAction action, @NotNull SlotData data) {
		switch (data.getName()) {
		case "icon":
			if (action == InventoryAction.SWAP_WITH_CURSOR) {
				ItemStack item = player.getItemOnCursor();
				if (item.getType() != Material.AIR) {
					this.setIcon(item.clone());
					this.refresh(player);
				}
			}
			break;
		case "crate":
			if (this.reward == null)
				new CrateSelectGUI(this.plugin, false, this, crate -> this.crate = crate).showToPlayer(player);
			break;
		case "random":
			this.setRandom(!this.isRandom());
			this.refresh(player);
			break;
		}
		return true;
	}
}
