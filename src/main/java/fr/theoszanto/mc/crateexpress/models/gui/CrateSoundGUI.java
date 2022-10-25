package fr.theoszanto.mc.crateexpress.models.gui;

import fr.theoszanto.mc.crateexpress.CrateExpress;
import fr.theoszanto.mc.express.gui.ExpressGUI;
import fr.theoszanto.mc.express.gui.ExpressPaginatedGUI;
import fr.theoszanto.mc.express.utils.ItemBuilder;
import fr.theoszanto.mc.express.utils.MathUtils;
import org.bukkit.Material;
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
	private static final @NotNull Sound @NotNull[] SOUNDS = Sound.values();

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
		if (element.isValue()) {
			SoundValue value = (SoundValue) element;
			return new ItemBuilder(this.getIcon(value.getName().toUpperCase(), Material.PAPER), 1, this.i18n("menu.sound.value", "key", value.getKey()), this.i18nLines("menu.sound.lore"))
					.addLore(this.i18n("menu.sound.value-key", "key", value.getKey())).build();
		}
		if (element.isNamespace()) {
			SoundNamespace namespace = (SoundNamespace) element;
			return new ItemBuilder(this.getIcon(namespace.getName().toUpperCase(), Material.BOOK), 1, this.i18n("menu.sound.namespace", "namespace", namespace.getName()), this.i18nLines("menu.sound.enter-namespace"))
					.addLore(this.i18n("menu.sound.namespace-prefix", "prefix", namespace.getPrefix())).build();
		}
		return null;
	}

	private @NotNull Material getIcon(@NotNull String name, @NotNull Material def) {
		String parent = this.namespace.getName();
		Material icon = null;
		switch (parent) {
		case "block":
		case "item":
			icon = Material.getMaterial(name);
			break;
		case "entity":
			icon = Material.getMaterial(name + "_SPAWN_EGG");
			if (icon == null)
				icon = Material.getMaterial(name);
			break;
		case "music_disc":
			String disc = "MUSIC_DISC_" + name;
			icon = Material.getMaterial(disc);
			break;
		}
		return icon == null || (!icon.isItem() && !icon.isRecord()) ? def : icon;
	}

	@Override
	protected boolean onClickOnElement(@NotNull Player player, @NotNull ClickType click, @NotNull InventoryAction action, @NotNull SoundElement element) {
		if (element.isValue()) {
			SoundValue value = (SoundValue) element;
			if (click.isLeftClick()) {
				this.onSelect.accept(value.getValue());
				player.closeInventory();
			} else if (click.isRightClick())
				player.playSound(player.getLocation(), value.getValue(), SoundCategory.MASTER, 1, 1);
			else if (click == ClickType.MIDDLE)
				player.stopSound(value.getValue(), SoundCategory.MASTER);
		} else if (element.isNamespace())
			this.openNamespace(player, (SoundNamespace) element);
		return true;
	}

	@Override
	protected boolean onOtherClick(@NotNull Player player, @NotNull ClickType click, @NotNull InventoryAction action, @Nullable SlotData data) {
		if (data == null)
			return true;
		switch (data.getName()) {
		case "parent":
			if (!this.namespace.isRoot())
				this.openNamespace(player, this.namespace.getParent());
			break;
		case "stop":
			player.stopAllSounds();
			break;
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
		default boolean isValue() {
			return this instanceof SoundValue;
		}

		default boolean isNamespace() {
			return this instanceof SoundNamespace;
		}

		@Override
		default int compareTo(@NotNull SoundElement other) {
			if (this.isNamespace()) {
				if (other.isNamespace())
					return ((SoundNamespace) this).getPrefix().compareToIgnoreCase(((SoundNamespace) other).getPrefix());
				return -1;
			}
			if (this.isValue()) {
				if (other.isValue())
					return ((SoundValue) this).getKey().compareToIgnoreCase(((SoundValue) other).getKey());
				return 1;
			}
			return 1;
		}
	}

	public static class SoundValue implements SoundElement {
		private final @NotNull Sound value;

		public SoundValue(@NotNull Sound value) {
			this.value = value;
		}

		public @NotNull Sound getValue() {
			return this.value;
		}

		public @NotNull String getKey() {
			return this.value.getKey().getKey();
		}

		public @NotNull String getName() {
			int sep = this.getKey().lastIndexOf(SoundNamespace.SEPARATOR);
			return sep == -1 ? this.getKey() : this.getKey().substring(sep + 1);
		}

		public @NotNull SoundNamespace getNamespace() {
			return SoundNamespace.ofValue(this);
		}
	}

	public static class SoundNamespace implements SoundElement {
		private final @NotNull String prefix;

		public static final @NotNull SoundNamespace ROOT = new SoundNamespace("");
		public static final char SEPARATOR = '.';

		public SoundNamespace(@NotNull String prefix) {
			this.prefix = prefix;
		}

		public @NotNull String getPrefix() {
			return this.prefix;
		}

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
			for (Sound sound : SOUNDS) {
				String key = sound.getKey().getKey();
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
