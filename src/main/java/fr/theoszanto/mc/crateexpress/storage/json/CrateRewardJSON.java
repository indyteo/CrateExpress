package fr.theoszanto.mc.crateexpress.storage.json;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import fr.theoszanto.mc.crateexpress.CrateExpress;
import fr.theoszanto.mc.crateexpress.models.reward.CrateReward;
import fr.theoszanto.mc.crateexpress.storage.CrateRewardStorage;
import fr.theoszanto.mc.express.utils.ItemUtils;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.function.Function;

public abstract class CrateRewardJSON<T extends CrateReward> extends CrateRewardStorage<T> {
	public CrateRewardJSON(@NotNull CrateExpress plugin, @NotNull String type) {
		super(plugin, type);
	}

	@SuppressWarnings("unchecked")
	@Override
	public void serializeReward(@NotNull CrateReward reward, @Nullable Object @NotNull... arguments) {
		if (arguments.length != 1 || !(arguments[0] instanceof JsonObject data))
			throw new IllegalArgumentException();
		data.addProperty("id", reward.getId());
		data.addProperty("type", reward.getType());
		data.addProperty("weight", reward.getWeight());
		this.serialize((T) reward, data);
	}

	@Override
	public @NotNull T deserializeReward(@Nullable Object @NotNull... arguments) throws IllegalStateException {
		if (arguments.length != 1 || !(arguments[0] instanceof JsonObject data))
			throw new IllegalArgumentException();
		String id = data.has("id") ? data.get("id").getAsString() : null;
		double weight = data.has("weight") ? data.get("weight").getAsDouble() : 1;
		return this.deserialize(data, id == null ? CrateReward.generateRandomId() : id, weight);
	}

	protected abstract void serialize(@NotNull T reward, @NotNull JsonObject data);

	protected abstract @NotNull T deserialize(@NotNull JsonObject data, @NotNull String id, double weight) throws IllegalStateException;

	protected static @NotNull Optional<@NotNull JsonElement> optional(@NotNull JsonObject data, @NotNull String key) throws IllegalStateException {
		return Optional.ofNullable(data.get(key));
	}

	protected static <T> @NotNull T getOrDefault(@NotNull JsonObject data, @NotNull String key, @NotNull Function<? super @NotNull JsonElement, @NotNull T> mapper, @NotNull T def) throws IllegalStateException {
		return optional(data, key).map(mapper).orElse(def);
	}

	protected static @NotNull JsonElement require(@NotNull JsonObject data, @NotNull String key) throws IllegalStateException {
		return assertNotNull(data.get(key), key);
	}

	protected static @NotNull ItemStack requireItem(@NotNull JsonObject data, @NotNull String key) throws IllegalStateException {
		return ItemUtils.fromString(require(data, key).getAsString());
	}

	@Contract("null, _ -> fail")
	private static <T> @NotNull T assertNotNull(@Nullable T value, @NotNull String key) throws IllegalStateException {
		if (value == null)
			throw new IllegalStateException("Missing " + key + " value in a crate reward");
		return value;
	}
}
