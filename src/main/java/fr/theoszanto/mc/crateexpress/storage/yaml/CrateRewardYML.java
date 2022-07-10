package fr.theoszanto.mc.crateexpress.storage.yaml;

import fr.theoszanto.mc.crateexpress.CrateExpress;
import fr.theoszanto.mc.crateexpress.models.reward.CrateReward;
import fr.theoszanto.mc.crateexpress.storage.CrateRewardStorage;
import fr.theoszanto.mc.express.utils.ItemUtils;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class CrateRewardYML<T extends CrateReward> extends CrateRewardStorage<T> {
	public CrateRewardYML(@NotNull CrateExpress plugin, @NotNull String type) {
		super(plugin, type);
	}

	@SuppressWarnings("unchecked")
	@Override
	public final void serializeReward(@NotNull CrateReward reward, @Nullable Object @NotNull... arguments) {
		if (arguments.length != 1 || !(arguments[0] instanceof ConfigurationSection))
			throw new IllegalArgumentException();
		ConfigurationSection data = (ConfigurationSection) arguments[0];
		data.set("type", reward.getType());
		data.set("weight", reward.getWeight());
		this.serialize((T) reward, data);
	}

	@Override
	public final @NotNull T deserializeReward(@Nullable Object @NotNull... arguments) throws IllegalStateException {
		if (arguments.length != 1 || !(arguments[0] instanceof ConfigurationSection))
			throw new IllegalArgumentException();
		ConfigurationSection data = (ConfigurationSection) arguments[0];
		int weight = data.getInt("weight", 1);
		return this.deserialize(data, weight);
	}

	protected abstract void serialize(@NotNull T reward, @NotNull ConfigurationSection data);

	protected abstract @NotNull T deserialize(@NotNull ConfigurationSection data, int weight) throws IllegalStateException;

	protected static @NotNull String requireString(@NotNull ConfigurationSection data, @NotNull String key) throws IllegalStateException {
		return assertNotNull(data.getString(key, null), key, "string");
	}

	protected static @NotNull ItemStack requireItem(@NotNull ConfigurationSection data, @NotNull String key) throws IllegalStateException {
		return ItemUtils.fromString(assertNotNull(data.getString(key, null), key, "item"));
	}

	@Contract("null, _, _ -> fail")
	private static <T> @NotNull T assertNotNull(@Nullable T value, @NotNull String key, @NotNull String type) throws IllegalStateException {
		if (value == null)
			throw new IllegalStateException("Missing " + key + " " + type + " value in a crate reward");
		return value;
	}
}
