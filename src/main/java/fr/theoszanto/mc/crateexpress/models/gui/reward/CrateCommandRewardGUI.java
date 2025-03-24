package fr.theoszanto.mc.crateexpress.models.gui.reward;

import fr.theoszanto.mc.crateexpress.CrateExpress;
import fr.theoszanto.mc.crateexpress.models.Crate;
import fr.theoszanto.mc.crateexpress.models.reward.CrateCommandReward;
import fr.theoszanto.mc.crateexpress.models.reward.CrateReward;
import fr.theoszanto.mc.express.utils.ItemBuilder;
import fr.theoszanto.mc.express.utils.ItemUtils;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.BlockState;
import org.bukkit.block.CommandBlock;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BlockStateMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class CrateCommandRewardGUI extends CrateRewardGUI<CrateCommandReward> {
	private @NotNull ItemStack icon;
	private @Nullable String command;
	private boolean physical = false;

	public CrateCommandRewardGUI(@NotNull CrateExpress plugin, @NotNull Crate crate) {
		super(plugin, crate);
		this.icon = this.defaultIcon();
	}

	public CrateCommandRewardGUI(@NotNull CrateExpress plugin, @NotNull Crate crate, @Nullable CrateCommandReward reward, @NotNull Integer slot) {
		super(plugin, crate, reward, slot);
		this.icon = this.defaultIcon();
	}

	private @NotNull ItemStack defaultIcon() {
		return new ItemBuilder(Material.COMMAND_BLOCK_MINECART, 1, this.i18n("misc.default-cmd-icon-name")).build();
	}

	private void setIcon(@NotNull ItemStack icon) {
		if (this.reward == null)
			this.icon = icon;
		else
			this.reward.setIcon(icon);
	}

	private boolean isPhysical() {
		return this.reward == null ? this.physical : this.reward.isPhysicalReward();
	}

	private void setPhysical(boolean physical) {
		if (this.reward == null)
			this.physical = physical;
		else
			this.reward.setPhysicalReward(physical);
	}

	@Override
	protected void setupButtons(@NotNull Player player) {
		this.set(slot(0, 4), new ItemBuilder(Material.COMMAND_BLOCK, 1, this.i18n("menu.reward.command.header.name"), this.i18nLines("menu.reward.command.header.lore")));
		this.set(slot(1, 1), new ItemBuilder(Material.PAINTING, 1, this.i18n("menu.reward.command.icon.name", "icon", ItemUtils.name(this.icon)), this.i18nLines("menu.reward.command.icon.lore")), "icon");
		this.set(slot(1, 3), new ItemBuilder(Material.COMMAND_BLOCK, 1, this.i18n("menu.reward.command.command.name", "command", this.i18nBoolean(this.reward != null || this.command != null)), this.i18nLines("menu.reward.command.command.lore")).addLoreConditionally(this.reward == null, this.i18n("menu.reward.command.command.set")), this.reward == null ? "command" : "");
		this.setWeightButton(slot(1, 5));
		this.set(slot(1, 7), new ItemBuilder(Material.CHEST, 1, this.i18n("menu.reward.command.physical.name", "physical", this.i18nBoolean(this.isPhysical())), this.i18nLines("menu.reward.command.physical.lore")), "physical");
	}

	@Override
	protected boolean canCreateReward() {
		return this.command != null && !this.command.isEmpty();
	}

	@Override
	protected @NotNull CrateCommandReward createReward() throws IllegalStateException {
		if (this.command == null || this.command.isEmpty() || this.command.equals("/"))
			throw new IllegalStateException();
		if (this.command.charAt(0) == '/')
			this.command = this.command.substring(1);
		return new CrateCommandReward(this.plugin, CrateReward.generateRandomId(), this.icon, this.getWeight(), this.command, this.physical);
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
		case "command":
			if (this.reward == null && action == InventoryAction.SWAP_WITH_CURSOR) {
				ItemStack item = player.getItemOnCursor();
				ItemMeta meta = item.getItemMeta();
				if (meta instanceof BlockStateMeta) {
					BlockState state = ((BlockStateMeta) meta).getBlockState();
					if (state instanceof CommandBlock) {
						String command = ((CommandBlock) state).getCommand();
						if (!command.isEmpty()) {
							this.command = command;
							this.refresh(player);
							break;
						}
					}
				}
				player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1, 1);
			}
			break;
		case "physical":
			this.setPhysical(!this.isPhysical());
			this.refresh(player);
			break;
		}
		return true;
	}
}
