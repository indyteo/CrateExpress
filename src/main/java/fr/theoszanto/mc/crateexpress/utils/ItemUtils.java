package fr.theoszanto.mc.crateexpress.utils;

import org.bukkit.Material;
import org.bukkit.configuration.serialization.DelegateDeserialization;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.material.MaterialData;
import org.bukkit.util.io.BukkitObjectInputStream;
import org.bukkit.util.io.BukkitObjectOutputStream;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.yaml.snakeyaml.external.biz.base64Coder.Base64Coder;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class ItemUtils {
	public static final @NotNull ItemStack EMPTY = new ItemStack(Material.AIR);
	public static final @NotNull String @NotNull[] NO_LORE = new String[0];

	private ItemUtils() {
		throw new UnsupportedOperationException();
	}

	public static @NotNull ItemStack unmodifiableItemStack(@NotNull ItemStack item) {
		return new UnmodifiableItemStack(item);
	}

	@DelegateDeserialization(ItemStack.class)
	@SuppressWarnings({ "deprecation", "EqualsWhichDoesntCheckParameterClass", "MethodDoesntCallSuperMethod" })
	private static class UnmodifiableItemStack extends ItemStack {
		private final @NotNull ItemStack item;

		private UnmodifiableItemStack(@NotNull ItemStack item) {
			this.item = item;
		}

		@Override
		public @NotNull Material getType() {
			return this.item.getType();
		}

		@Override
		public int getAmount() {
			return this.item.getAmount();
		}

		@Override
		public @Nullable MaterialData getData() {
			return this.item.getData();
		}

		@Override
		public short getDurability() {
			return this.item.getDurability();
		}

		@Override
		public int getMaxStackSize() {
			return this.item.getMaxStackSize();
		}

		@Override
		public String toString() {
			return this.item.toString();
		}

		@Override
		public boolean equals(Object obj) {
			return this.item.equals(obj);
		}

		@Override
		public boolean isSimilar(@Nullable ItemStack stack) {
			return this.item.isSimilar(stack);
		}

		@Override
		public @NotNull ItemStack clone() {
			return this.item.clone();
		}

		@Override
		public int hashCode() {
			return this.item.hashCode();
		}

		@Override
		public boolean containsEnchantment(@NotNull Enchantment ench) {
			return this.item.containsEnchantment(ench);
		}

		@Override
		public int getEnchantmentLevel(@NotNull Enchantment ench) {
			return this.item.getEnchantmentLevel(ench);
		}

		@Override
		public @NotNull Map<@NotNull Enchantment, @NotNull Integer> getEnchantments() {
			return this.item.getEnchantments();
		}

		@Override
		public @Nullable ItemMeta getItemMeta() {
			return this.item.getItemMeta();
		}

		@Override
		public boolean hasItemMeta() {
			return this.item.hasItemMeta();
		}

		@Override
		public void setType(@NotNull Material type) {
			throw new UnsupportedOperationException();
		}

		@Override
		public void setAmount(int amount) {
			throw new UnsupportedOperationException();
		}

		@Override
		public void setData(@Nullable MaterialData data) {
			throw new UnsupportedOperationException();
		}

		@Override
		public void setDurability(short durability) {
			throw new UnsupportedOperationException();
		}

		@Override
		public void addEnchantments(@NotNull Map<Enchantment, Integer> enchantments) {
			throw new UnsupportedOperationException();
		}

		@Override
		public void addEnchantment(@NotNull Enchantment ench, int level) {
			throw new UnsupportedOperationException();
		}

		@Override
		public void addUnsafeEnchantments(@NotNull Map<Enchantment, Integer> enchantments) {
			throw new UnsupportedOperationException();
		}

		@Override
		public void addUnsafeEnchantment(@NotNull Enchantment ench, int level) {
			throw new UnsupportedOperationException();
		}

		@Override
		public int removeEnchantment(@NotNull Enchantment ench) {
			throw new UnsupportedOperationException();
		}

		@Override
		public boolean setItemMeta(@Nullable ItemMeta itemMeta) {
			throw new UnsupportedOperationException();
		}

		@Override
		public @NotNull Map<String, Object> serialize() {
			return this.item.serialize();
		}

		public static @NotNull ItemStack deserialize(@NotNull Map<String, Object> args) {
			return ItemStack.deserialize(args);
		}
	}

	public static @NotNull String name(@NotNull ItemStack item) {
		ItemMeta meta = item.getItemMeta();
		if (meta != null) {
			if (meta.hasDisplayName())
				return meta.getDisplayName();
			if (meta.hasLocalizedName())
				return meta.getLocalizedName();
		}
		char[] s = item.getType().name().replace('_', ' ').toLowerCase().toCharArray();
		boolean space = true;
		for (int i = 0; i < s.length; i++) {
			if (space)
				s[i] += 'A' - 'a';
			space = s[i] == ' ';
		}
		return new String(s);
	}

	public static void addLoreConditionally(boolean condition, @NotNull ItemStack item, @NotNull String @NotNull... lore) {
		if (condition)
			addLore(item, lore);
	}

	public static void addLore(@NotNull ItemStack item, @NotNull String @NotNull... lore) {
		ItemMeta meta = item.getItemMeta();
		if (meta == null)
			return;
		List<String> existingLore = meta.hasLore() ? meta.getLore() : new ArrayList<>();
		assert existingLore != null;
		existingLore.addAll(Arrays.asList(lore));
		meta.setLore(existingLore);
		item.setItemMeta(meta);
	}

	public static @NotNull ItemStack withAmount(@NotNull ItemStack item, int amount) {
		ItemStack clone = item.clone();
		clone.setAmount(amount);
		return clone;
	}

	public static int stackAmountFromValue(double value) {
		String valueStr = Double.toString(value);
		String firstTwoDigits = value < 1 ? "1" : valueStr.substring(0, value >= 10 ? 2 : 1);
		int amount = Integer.parseInt(firstTwoDigits);
		if (amount > 64)
			amount /= 10;
		return amount;
	}

	public static @NotNull String toString(@NotNull ItemStack item) {
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		try (BukkitObjectOutputStream ds = new BukkitObjectOutputStream(os)) {
			ds.writeObject(item);
			ds.flush();
			return new String(Base64Coder.encode(os.toByteArray()));
		} catch (IOException e) {
			throw new IllegalStateException("Unable to serialize item", e);
		}
	}

	public static @NotNull ItemStack fromString(@NotNull String item) {
		ByteArrayInputStream is = new ByteArrayInputStream(Base64Coder.decode(item));
		try (BukkitObjectInputStream ds = new BukkitObjectInputStream(is)) {
			return (ItemStack) ds.readObject();
		} catch (IOException | ClassNotFoundException e) {
			throw new IllegalStateException("Unable to deserialize item", e);
		}
	}
}
