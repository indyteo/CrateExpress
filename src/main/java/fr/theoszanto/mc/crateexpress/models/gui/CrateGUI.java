package fr.theoszanto.mc.crateexpress.models.gui;

import fr.theoszanto.mc.crateexpress.CrateExpress;
import fr.theoszanto.mc.crateexpress.utils.ItemBuilder;
import fr.theoszanto.mc.crateexpress.utils.MathUtils;
import fr.theoszanto.mc.crateexpress.utils.PluginObject;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public abstract class CrateGUI extends PluginObject {
	private final @NotNull Inventory inventory;
	private final @NotNull Map<@NotNull Integer, @NotNull SlotData> slotData = new HashMap<>();
	protected final int rows;

	public static final @NotNull String CLOSE = "close";

	public CrateGUI(@NotNull CrateExpress plugin, int rows, @NotNull String key, @Nullable Object @NotNull... format) {
		this(plugin, Bukkit.createInventory(null, MathUtils.minMax(1, rows, 6) * 9,
				plugin.i18n("menu.title-style", "title", plugin.i18n(key, format))), MathUtils.minMax(1, rows, 6));
	}

	private CrateGUI(@NotNull CrateExpress plugin, @NotNull Inventory inventory, int rows) {
		super(plugin);
		this.inventory = inventory;
		this.rows = rows;
	}

	protected final void clear() {
		this.inventory.clear();
		this.slotData.clear();
	}

	protected final void clear(int slot) {
		this.inventory.clear(slot);
		this.slotData.remove(slot);
	}

	protected final void set(int slot, @NotNull ItemBuilder item) {
		this.set(slot, item, "");
	}

	protected final void set(int slot, @NotNull ItemBuilder item, @NotNull String name) {
		this.set(slot, item, name, null);
	}

	protected final void set(int slot, @NotNull ItemBuilder item, @NotNull String name, @Nullable Object data) {
		this.set(slot, item.build(), name, data);
	}

	protected final void set(int slot, @NotNull ItemStack item) {
		this.set(slot, item, "");
	}

	protected final void set(int slot, @NotNull ItemStack item, @NotNull String name) {
		this.set(slot, item, name, null);
	}

	protected final void set(int slot, @NotNull ItemStack item, @NotNull String name, @Nullable Object data) {
		this.inventory.setItem(slot, item);
		this.setData(slot, name, data);
	}

	protected final void setData(int slot, @NotNull String name, @Nullable Object data) {
		this.slotData.put(slot, new SlotData(slot, name, data));
	}

	protected final void setCloseButton(int slot) {
		this.set(slot, new ItemBuilder(Material.BARRIER, 1, this.i18n("menu.close")), CLOSE);
	}

	public final void showToPlayer(@NotNull Player player) {
		this.spigot().showGUI(player, this);
	}

	public final @NotNull Inventory getInventory() {
		return this.inventory;
	}

	public final @Nullable SlotData getSlotData(int slot) {
		return this.slotData.get(slot);
	}

	protected final void refresh(@NotNull Player player) {
		this.clear();
		this.onOpen(player, this);
	}

	public void onOpen(@NotNull Player player, @Nullable CrateGUI previous) {}

	public boolean onClick(@NotNull Player player, @NotNull ClickType click, @NotNull InventoryAction action, @Nullable SlotData data) {
		return true;
	}

	public boolean onClickOutside(@NotNull Player player, @NotNull ClickType click, @NotNull InventoryAction action) {
		return false;
	}

	public boolean onClickInOtherInventory(@NotNull Player player, @NotNull Inventory inventory, @NotNull ClickType click, @NotNull InventoryAction action, int slot) {
		return impactOtherInventory(action);
	}

	public boolean preventDragInside() {
		return true;
	}

	public boolean preventDragOutside() {
		return false;
	}

	public void onClose(@NotNull Player player) {}

	protected static boolean impactOtherInventory(@NotNull InventoryAction action) {
		return action == InventoryAction.MOVE_TO_OTHER_INVENTORY || action == InventoryAction.COLLECT_TO_CURSOR
				|| action == InventoryAction.HOTBAR_MOVE_AND_READD || action == InventoryAction.UNKNOWN;
	}

	protected static int slot(int row, int column) {
		return row * 9 + column;
	}

	public static final class SlotData {
		private final int slot;
		private final @NotNull String name;
		private final @Nullable Object userData;

		public SlotData(int slot, @NotNull String name, @Nullable Object userData) {
			this.slot = slot;
			this.name = name;
			this.userData = userData;
		}

		public int getSlot() {
			return this.slot;
		}

		public @NotNull String getName() {
			return this.name;
		}

		@SuppressWarnings("unchecked")
		public <T> @NotNull T getUserData() throws IllegalStateException {
			try {
				if (this.userData == null)
					throw new IllegalStateException("User data is null");
				return (T) this.userData;
			} catch (ClassCastException e) {
				throw new IllegalStateException("User data could not be cast to requested type", e);
			}
		}

		@SuppressWarnings("unchecked")
		public <T> @NotNull Optional<@NotNull T> getOptionalUserData() {
			return (Optional<T>) Optional.ofNullable(this.userData);
		}

		public @Nullable Object getRawUserData() {
			return this.userData;
		}
	}
}
