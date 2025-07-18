package fr.theoszanto.mc.crateexpress.models.gui;

import fr.theoszanto.mc.crateexpress.CrateExpress;
import fr.theoszanto.mc.express.gui.ExpressGUI;
import fr.theoszanto.mc.express.gui.ExpressPaginatedGUI;
import fr.theoszanto.mc.express.utils.ItemBuilder;
import fr.theoszanto.mc.express.utils.MathUtils;
import org.bukkit.Material;
import org.bukkit.Registry;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.function.Consumer;

public class CrateSoundGUI extends ExpressPaginatedGUI<CrateExpress, CrateSoundGUI.SoundElement> {
	private @NotNull SoundNamespace namespace;
	private final @NotNull ExpressGUI<CrateExpress> returnTo;
	private final @NotNull Consumer<@NotNull Sound> onSelect;

	private static final int[] contentSlots = MathUtils.numbers(9, 4 * 9);

	public CrateSoundGUI(@NotNull CrateExpress plugin, @NotNull SoundNamespace namespace, @NotNull ExpressGUI<CrateExpress> returnTo, @NotNull Consumer<@NotNull Sound> onSelect) {
		super(plugin, new ArrayList<>(namespace.listContent()), 5, "menu.sound.title");
		this.namespace = namespace;
		this.returnTo = returnTo;
		this.onSelect = onSelect;
	}

	@Override
	protected void prepareGUI(@NotNull Player player) {
		this.setButtons(slot(4, 0), slot(4, 8), slot(4, 3), slot(4, 5));
		this.setEmptyIndicator(slot(2, 4), "menu.sound.empty");
		this.set(slot(0, 4), new ItemBuilder(Material.BELL, 1, this.i18n("menu.sound.header.name", "namespace", this.namespace.isRoot() ? this.i18n("menu.sound.header.root-namespace") : this.namespace.getName()), this.i18nLines("menu.sound.header.lore")));
		if (!this.namespace.isRoot())
			this.set(slot(0, 0), new ItemBuilder(Material.SPECTRAL_ARROW, 1, this.i18n("menu.sound.parent-namespace"), this.i18nLines("menu.sound.enter-namespace")), "parent");
		this.set(slot(0, 8), new ItemBuilder(Material.RED_WOOL, 1, this.i18n("menu.sound.stop.name"), this.i18nLines("menu.sound.stop.lore")), "stop");
	}

	@Override
	protected int @NotNull[] contentSlots() {
		return contentSlots;
	}

	@Override
	protected @Nullable ItemStack icon(@NotNull Player player, @NotNull SoundElement element) {
		if (element instanceof SoundValue value)
			return new ItemBuilder(this.getIcon(value.getName().toUpperCase(), Material.PAPER), 1, this.i18n("menu.sound.value", "key", value.getKey()), this.i18nLines("menu.sound.lore"))
					.addLore(this.i18n("menu.sound.value-key", "key", value.getKey())).build();
		if (element instanceof SoundNamespace namespace)
			return new ItemBuilder(this.getIcon(namespace.getName().toUpperCase(), Material.BOOK), 1, this.i18n("menu.sound.namespace", "namespace", namespace.getName()), this.i18nLines("menu.sound.enter-namespace"))
					.addLore(this.i18n("menu.sound.namespace-prefix", "prefix", namespace.prefix())).build();
		return null;
	}

	private @NotNull Material getIcon(@NotNull String name, @NotNull Material def) {
		String parent = this.namespace.getName();
		Material icon = null;
		switch (parent) {
			case "block", "item" -> icon = Material.getMaterial(name);
			case "entity" -> {
				icon = Material.getMaterial(name + "_SPAWN_EGG");
				if (icon == null)
					icon = Material.getMaterial(name);
			}
			case "music_disc" -> {
				String disc = "MUSIC_DISC_" + name;
				icon = Material.getMaterial(disc);
			}
		}
		return icon == null || (!icon.isItem() && !icon.isRecord()) ? def : icon;
	}

	@Override
	protected boolean onClickOnElement(@NotNull Player player, @NotNull ClickType click, @NotNull InventoryAction action, @NotNull SoundElement element) {
		if (element instanceof SoundValue(Sound value)) {
			if (click.isLeftClick()) {
				this.onSelect.accept(value);
				player.closeInventory();
			} else if (click.isRightClick())
				player.playSound(player.getLocation(), value, SoundCategory.MASTER, 1, 1);
			else if (click == ClickType.MIDDLE)
				player.stopSound(value, SoundCategory.MASTER);
		} else if (element instanceof SoundNamespace namespace)
			this.openNamespace(player, namespace);
		return true;
	}

	@Override
	protected boolean onOtherClick(@NotNull Player player, @NotNull ClickType click, @NotNull InventoryAction action, @Nullable SlotData data) {
		if (data == null)
			return true;
		switch (data.getName()) {
			case "parent" -> {
				if (!this.namespace.isRoot())
					this.openNamespace(player, this.namespace.getParent());
			}
			case "stop" -> player.stopAllSounds();
		}
		return true;
	}

	@Override
	public void onClose(@NotNull Player player) {
		this.run(() -> this.returnTo.showToPlayer(player));
	}

	private void openNamespace(@NotNull Player player, @NotNull SoundNamespace namespace) {
		this.namespace = namespace;
		this.setElements(namespace.listContent());
		this.page = 0;
		this.refresh(player);
	}

	public interface SoundElement extends Comparable<SoundElement> {
		@Override
		default int compareTo(@NotNull SoundElement other) {
			if (this instanceof SoundNamespace(String thisPrefix)) {
				if (other instanceof SoundNamespace(String otherPrefix))
					return thisPrefix.compareToIgnoreCase(otherPrefix);
				return -1;
			}
			if (this instanceof SoundValue thisValue) {
				if (other instanceof SoundValue otherValue)
					return thisValue.getKey().compareToIgnoreCase(otherValue.getKey());
				return 1;
			}
			return 1;
		}
	}

	public record SoundValue(@NotNull Sound value) implements SoundElement {
		public @NotNull String getKey() {
			return Registry.SOUNDS.getKeyOrThrow(this.value).getKey();
		}

		public @NotNull String getName() {
			int sep = this.getKey().lastIndexOf(SoundNamespace.SEPARATOR);
			return sep == -1 ? this.getKey() : this.getKey().substring(sep + 1);
		}

		public @NotNull SoundNamespace getNamespace() {
			return SoundNamespace.ofValue(this);
		}
	}

	public record SoundNamespace(@NotNull String prefix) implements SoundElement {
		public static final @NotNull SoundNamespace ROOT = new SoundNamespace("");
		public static final char SEPARATOR = '.';

		public @NotNull String getName() {
			int sep = this.prefix.lastIndexOf(SEPARATOR);
			return sep == -1 ? this.prefix : this.prefix.substring(sep + 1);
		}

		public @NotNull SoundNamespace getParent() {
			if (this.isRoot())
				throw new IllegalStateException("Root namespace have no parent");
			return parent(this.prefix);
		}

		public boolean isRoot() {
			return this.prefix.isEmpty();
		}

		public @NotNull SortedSet<@NotNull SoundElement> listContent() {
			SortedSet<SoundElement> content = new TreeSet<>();
			boolean isRoot = this.isRoot();
			for (Sound sound : Registry.SOUNDS) {
				String key = Registry.SOUNDS.getKeyOrThrow(sound).getKey();
				if (isRoot || key.startsWith(this.prefix + SEPARATOR)) {
					int sep = key.indexOf(SEPARATOR, isRoot ? 0 : this.prefix.length() + 2);
					content.add(sep == -1 ? new SoundValue(sound) : new SoundNamespace(key.substring(0, sep)));
				}
			}
			return content;
		}

		public static @NotNull SoundNamespace ofValue(@NotNull SoundValue value) {
			return parent(value.getKey());
		}

		private static @NotNull SoundNamespace parent(@NotNull String prefix) {
			int sep = prefix.lastIndexOf(SEPARATOR);
			return new SoundNamespace(sep == -1 ? "" : prefix.substring(0, sep));
		}
	}
}
