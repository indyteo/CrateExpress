package fr.theoszanto.mc.crateexpress.models.gui;

import fr.theoszanto.mc.crateexpress.CrateExpress;
import fr.theoszanto.mc.express.gui.ExpressGUI;
import fr.theoszanto.mc.express.gui.ExpressPaginatedGUI;
import fr.theoszanto.mc.express.utils.ItemBuilder;
import fr.theoszanto.mc.express.utils.MathUtils;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Comparator;
import java.util.function.Consumer;

public class CrateParticleGUI extends ExpressPaginatedGUI<CrateExpress, Particle> {
	private final @NotNull ExpressGUI<CrateExpress> returnTo;
	private final @NotNull Consumer<@NotNull Particle> onSelect;

	private static final int[] contentSlots = MathUtils.numbers(9, 4 * 9);

	public CrateParticleGUI(@NotNull CrateExpress plugin, @NotNull ExpressGUI<CrateExpress> returnTo, @NotNull Consumer<@NotNull Particle> onSelect) {
		super(plugin, Arrays.stream(Particle.values()).sorted(Comparator.comparing(p -> p.getKey().getKey())).toList(), 5, "menu.particle.title");
		this.returnTo = returnTo;
		this.onSelect = onSelect;
	}

	@Override
	protected void prepareGUI(@NotNull Player player) {
		this.setButtons(slot(4, 0), slot(4, 8), slot(4, 3), slot(4, 5));
		this.setEmptyIndicator(slot(2, 4), "menu.particle.empty");
		this.set(slot(0, 4), new ItemBuilder(Material.BLAZE_POWDER, 1, this.i18n("menu.particle.header.name"), this.i18nLines("menu.particle.header.lore")));
	}

	@Override
	protected int @NotNull[] contentSlots() {
		return contentSlots;
	}

	@Override
	protected @Nullable ItemStack icon(@NotNull Player player, @NotNull Particle element) {
		ItemBuilder builder;
		if (element.getDataType() == Void.class)
			builder = new ItemBuilder(Material.PAPER, 1, this.i18n("menu.particle.value", "key", element.getKey().getKey()), this.i18nLines("menu.particle.lore"));
		else
			builder = new ItemBuilder(Material.RED_DYE, 1, this.i18n("menu.particle.value-unsupported", "key", element.getKey().getKey()), this.i18nLines("menu.particle.lore-unsupported"));
		return builder.addLore(this.i18n("menu.particle.value-key", "key", element.getKey().getKey())).build();
	}

	@Override
	protected boolean onClickOnElement(@NotNull Player player, @NotNull ClickType click, @NotNull InventoryAction action, @NotNull Particle element) {
		if (element.getDataType() == Void.class) {
			if (click.isLeftClick()) {
				this.onSelect.accept(element);
				player.closeInventory();
			} else if (click.isRightClick())
				player.spawnParticle(element, player.getLocation().add(0, 0.5, 0), 10, 0.1, 0.1, 0.1, 0.5);
		}
		return true;
	}

	@Override
	public void onClose(@NotNull Player player) {
		this.run(() -> this.returnTo.showToPlayer(player));
	}
}
