package fr.theoszanto.mc.crateexpress.utils;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ItemBuilder {
	private @NotNull Material material;
	private int amount;
	private @Nullable String displayName;
	private @Nullable List<@NotNull String> lore;

	public ItemBuilder(@NotNull Material material, int amount, @Nullable String displayName, @NotNull String @Nullable... lore) {
		this(material, amount, displayName, lore == null || lore.length == 0 ? null : Arrays.asList(lore));
	}

	public ItemBuilder(@NotNull Material material, int amount, @Nullable String displayName, @Nullable List<@NotNull String> lore) {
		this.material = material;
		this.amount = amount;
		this.displayName = displayName;
		this.setLore(lore);
	}

	public @NotNull Material getMaterial() {
		return material;
	}

	@Contract(value = "_ -> this", mutates = "this")
	public @NotNull ItemBuilder setMaterial(@NotNull Material material) {
		this.material = material;
		return this;
	}

	public int getAmount() {
		return amount;
	}

	@Contract(value = "_ -> this", mutates = "this")
	public @NotNull ItemBuilder setAmount(int amount) {
		this.amount = amount;
		return this;
	}

	public @Nullable String getDisplayName() {
		return displayName;
	}

	@Contract(value = "_ -> this", mutates = "this")
	public @NotNull ItemBuilder setDisplayName(@Nullable String displayName) {
		this.displayName = displayName;
		return this;
	}

	public @Nullable List<@NotNull String> getLore() {
		return lore;
	}

	@Contract(value = "_ -> this", mutates = "this")
	public @NotNull ItemBuilder setLore(@Nullable List<@NotNull String> lore) {
		this.lore = lore == null ? null : new ArrayList<>(lore);
		return this;
	}

	@Contract(value = "_ -> this", mutates = "this")
	public @NotNull ItemBuilder addLore(@NotNull String @Nullable... lore) {
		return lore == null ? this : this.addLore(Arrays.asList(lore));
	}

	@Contract(value = "_ -> this", mutates = "this")
	public @NotNull ItemBuilder addLore(@Nullable List<@NotNull String> lore) {
		if (lore != null) {
			if (this.lore == null)
				this.lore = new ArrayList<>(lore);
			else
				this.lore.addAll(lore);
		}
		return this;
	}

	@Contract(value = "_, _ -> this", mutates = "this")
	public @NotNull ItemBuilder addLoreConditionally(boolean condition, @NotNull String loreIfTrue) {
		if (condition)
			this.addLore(loreIfTrue);
		return this;
	}

	@Contract(value = "_, _, _ -> this", mutates = "this")
	public @NotNull ItemBuilder addLoreConditionally(boolean condition, @NotNull String loreIfTrue, @NotNull String loreIfFalse) {
		if (condition)
			this.addLore(loreIfTrue);
		else
			this.addLore(loreIfFalse);
		return this;
	}

	public @NotNull ItemStack build() {
		ItemStack item = new ItemStack(this.material, this.amount);
		ItemMeta meta = item.getItemMeta();
		assert meta != null;
		meta.setDisplayName(this.displayName);
		meta.setLore(this.lore);
		item.setItemMeta(meta);
		return item;
	}

	public @NotNull ItemStack buildUnmodifiable() {
		return ItemUtils.unmodifiableItemStack(this.build());
	}

	public static @NotNull ItemBuilder of(@NotNull ItemStack item) {
		ItemBuilder builder = new ItemBuilder(item.getType(), item.getAmount(), null);
		ItemMeta meta = item.getItemMeta();
		if (meta == null)
			return builder;
		if (meta.hasDisplayName())
			builder.setDisplayName(meta.getDisplayName());
		if (meta.hasLore())
			builder.setLore(meta.getLore());
		return builder;
	}
}
