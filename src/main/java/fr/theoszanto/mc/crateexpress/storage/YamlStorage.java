package fr.theoszanto.mc.crateexpress.storage;

import fr.theoszanto.mc.crateexpress.CrateExpress;
import fr.theoszanto.mc.crateexpress.models.Crate;
import fr.theoszanto.mc.crateexpress.models.CrateKey;
import fr.theoszanto.mc.crateexpress.models.CrateRegistry;
import fr.theoszanto.mc.crateexpress.models.reward.ClaimableReward;
import fr.theoszanto.mc.crateexpress.models.reward.CrateCommandReward;
import fr.theoszanto.mc.crateexpress.models.reward.CrateItemReward;
import fr.theoszanto.mc.crateexpress.models.reward.CrateKeyReward;
import fr.theoszanto.mc.crateexpress.models.reward.CrateMoneyReward;
import fr.theoszanto.mc.crateexpress.models.reward.CrateOtherReward;
import fr.theoszanto.mc.crateexpress.models.reward.CrateReward;
import fr.theoszanto.mc.crateexpress.utils.ItemUtils;
import fr.theoszanto.mc.crateexpress.utils.LocationUtils;
import fr.theoszanto.mc.crateexpress.utils.MathUtils;
import fr.theoszanto.mc.crateexpress.utils.PluginObject;
import fr.theoszanto.mc.crateexpress.utils.UnloadableWorldLocation;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class YamlStorage extends PluginObject implements Storage {
	private final @NotNull File cratesDir;
	private final @NotNull File rewardsDir;

	public YamlStorage(@NotNull CrateExpress plugin, @NotNull String cratesDir, @NotNull String rewardsDir) throws IllegalArgumentException {
		super(plugin);
		this.cratesDir = new File(plugin.getDataFolder(), cratesDir);
		if (!(this.cratesDir.exists() ? this.cratesDir.isDirectory() : this.cratesDir.mkdirs()))
			throw new IllegalArgumentException("YamlStorage crates directory is invalid: " + this.cratesDir);
		this.rewardsDir = new File(plugin.getDataFolder(), rewardsDir);
		if (!(this.rewardsDir.exists() ? this.rewardsDir.isDirectory() : this.rewardsDir.mkdirs()))
			throw new IllegalArgumentException("YamlStorage rewards directory is invalid: " + this.rewardsDir);
	}

	@Override
	public void loadCrates(@NotNull CrateRegistry registry) throws IllegalStateException {
		File[] files = this.cratesDir.listFiles((dir, name) -> name.matches("^.*\\.ya?ml$"));
		if (files == null)
			throw new IllegalStateException("Could not list files from YamlStorage crates directory: " + this.cratesDir);
		for (File file : files) {
			String id = crateIdFromFileName(file.getName());
			try {
				YamlConfiguration data = new YamlConfiguration();
				data.load(file);
				int min = data.getInt("min", 1);
				int max = data.getInt("max", 1);
				String crateKey = data.getString("key", null);
				CrateKey key = crateKey == null ? null : new CrateKey(this.plugin, id, ItemUtils.fromString(crateKey));
				String name = data.getString("name", id);
				String message = data.getString("message", null);
				String crateLocation = data.getString("location", null);
				UnloadableWorldLocation location = crateLocation == null ? null : LocationUtils.fromString(crateLocation);
				Crate crate = new Crate(this.plugin, id, min, max, key, name, message, location);
				ConfigurationSection items = data.getConfigurationSection("items");
				if (items != null) {
					for (String item : items.getKeys(false)) {
						int slot;
						try {
							slot = Integer.parseInt(item);
						} catch (NumberFormatException e) {
							throw new InvalidConfigurationException("Could not parse crate item slot number: " + item, e);
						}
						ConfigurationSection reward = items.getConfigurationSection(item);
						assert reward != null;
						crate.addReward(slot, deserializeReward(reward, this.plugin));
					}
				}
				registry.register(id, crate);
			} catch (IOException | InvalidConfigurationException | IllegalArgumentException e) {
				this.error("Could not load crate: " + id, e);
			}
		}
	}

	@Override
	public void saveCrate(@NotNull Crate crate) throws IllegalStateException {
		String id = crate.getId();
		try {
			File file = new File(this.cratesDir, fileNameFromCrateId(id));
			YamlConfiguration data = new YamlConfiguration();
			data.set("min", crate.getMin());
			data.set("max", crate.getMax());
			CrateKey key = crate.getKey();
			if (key != null)
				data.set("key", ItemUtils.toString(key.getItem()));
			data.set("name", crate.getName());
			if (crate.getMessage() != null)
				data.set("message", crate.getMessage());
			if (crate.getLocation() != null)
				data.set("location", LocationUtils.toString(crate.getLocation()));
			ConfigurationSection items = data.createSection("items");
			crate.getRewards().forEach((slot, reward) -> {
				ConfigurationSection rewardData = items.createSection(slot.toString());
				serializeReward(rewardData, reward);
			});
			data.save(file);
		} catch (IOException e) {
			throw new IllegalStateException("Could not save crate: " + id, e);
		}
	}

	@Override
	public void deleteCrate(@NotNull String id) throws IllegalStateException {
		File file = new File(this.cratesDir, fileNameFromCrateId(id));
		if (file.exists() && !file.delete())
			throw new IllegalStateException("Could not delete crate data file: " + file);
	}

	@Override
	public void saveReward(@NotNull Player player, @NotNull CrateReward reward) throws IllegalStateException {
		String uuid = player.getUniqueId().toString();
		try {
			File file = new File(this.rewardsDir, uuid + ".yml");
			YamlConfiguration data = new YamlConfiguration();
			try {
				data.load(file);
			} catch (FileNotFoundException ignored) {}
			ConfigurationSection rewardData = data.createSection(Integer.toString(MathUtils.nextAvailableInt(data.getKeys(false))));
			serializeReward(rewardData, reward);
			data.save(file);
		} catch (IOException | InvalidConfigurationException e) {
			throw new IllegalStateException("Could not save reward for player: " + player.getName() + " (" + uuid + ")", e);
		}
	}

	@Override
	public @NotNull List<@NotNull ClaimableReward> listRewards(@NotNull Player player) throws IllegalStateException {
		String uuid = player.getUniqueId().toString();
		try {
			File file = new File(this.rewardsDir, uuid + ".yml");
			YamlConfiguration data = new YamlConfiguration();
			data.load(file);
			List<ClaimableReward> rewards = new ArrayList<>();
			for (String id : data.getKeys(false)) {
				ConfigurationSection reward = data.getConfigurationSection(id);
				assert reward != null;
				rewards.add(new ClaimableReward(id, deserializeReward(reward, this.plugin)));
			}
			return rewards;
		} catch (FileNotFoundException e) {
			return new ArrayList<>();
		} catch (IOException | InvalidConfigurationException e) {
			throw new IllegalStateException("Could not list rewards for player: " + player.getName() + " (" + uuid + ")", e);
		}
	}

	@Override
	public int countRewards(@NotNull Player player) throws IllegalStateException {
		String uuid = player.getUniqueId().toString();
		try {
			File file = new File(this.rewardsDir, uuid + ".yml");
			YamlConfiguration data = new YamlConfiguration();
			data.load(file);
			return data.getKeys(false).size();
		} catch (FileNotFoundException e) {
			return 0;
		} catch (IOException | InvalidConfigurationException e) {
			throw new IllegalStateException("Could not count rewards for player: " + player.getName() + " (" + uuid + ")", e);
		}
	}

	@Override
	public void deleteReward(@NotNull Player player, @NotNull String id) throws IllegalStateException {
		String uuid = player.getUniqueId().toString();
		try {
			File file = new File(this.rewardsDir, uuid + ".yml");
			YamlConfiguration data = new YamlConfiguration();
			data.load(file);
			data.set(id, null);
			data.save(file);
		} catch (FileNotFoundException ignored) {
		} catch (IOException | InvalidConfigurationException e) {
			throw new IllegalStateException("Could not delete reward for player: " + player.getName() + " (" + uuid + ")", e);
		}
	}

	private static @NotNull CrateReward deserializeReward(@NotNull ConfigurationSection reward, @NotNull CrateExpress plugin) throws InvalidConfigurationException {
		String type = reward.getString("type", "null");
		int weight = reward.getInt("weight", 1);
		switch (type) {
		case "item":
			String item = reward.getString("item", null);
			if (item == null)
				throw new InvalidConfigurationException("Missing item value for crate item reward");
			return new CrateItemReward(plugin, weight, ItemUtils.fromString(item));
		case "command":
			String command = reward.getString("command", null);
			if (command == null)
				throw new InvalidConfigurationException("Missing command value for crate command reward");
			String icon1 = reward.getString("icon", null);
			if (icon1 == null)
				throw new InvalidConfigurationException("Missing icon value for crate command reward");
			boolean needInventorySpace = reward.getBoolean("need-inventory-space", false);
			return new CrateCommandReward(plugin, ItemUtils.fromString(icon1), weight, command, needInventorySpace);
		case "key":
			String key = reward.getString("key", null);
			if (key == null)
				throw new InvalidConfigurationException("Missing key value for crate key reward");
			int amount = reward.getInt("amount", -1);
			if (amount < 0 || amount > 64)
				throw new InvalidConfigurationException("Missing (or invalid) key amount for crate key reward");
			return new CrateKeyReward(plugin, weight, key, amount);
		case "money":
			double moneyAmount = reward.getDouble("amount", Double.NaN);
			if (Double.isNaN(moneyAmount))
				throw new InvalidConfigurationException("Missing amount value for crate money reward");
			return new CrateMoneyReward(plugin, weight, moneyAmount);
		case "other":
			String crate = reward.getString("crate", null);
			if (crate == null)
				throw new InvalidConfigurationException("Missing crate value for crate other reward");
			String icon2 = reward.getString("icon", null);
			if (icon2 == null)
				throw new InvalidConfigurationException("Missing icon value for crate other reward");
			boolean random = reward.getBoolean("random", false);
			return new CrateOtherReward(plugin, ItemUtils.fromString(icon2), weight, crate, random);
		default:
			throw new InvalidConfigurationException("Could not parse crate item reward type: " + type);
		}
	}

	private static void serializeReward(@NotNull ConfigurationSection data, @NotNull CrateReward reward) {
		if (reward instanceof CrateItemReward) {
			data.set("type", "item");
			data.set("item", ItemUtils.toString(((CrateItemReward) reward).getItem()));
		} else if (reward instanceof CrateCommandReward) {
			data.set("type", "command");
			data.set("command", ((CrateCommandReward) reward).getCommand());
			data.set("icon", ItemUtils.toString(reward.getIcon()));
			data.set("need-inventory-space", reward.isPhysicalReward());
		} else if (reward instanceof CrateKeyReward) {
			CrateKeyReward keyReward = (CrateKeyReward) reward;
			data.set("type", "key");
			data.set("key", keyReward.getKey());
			data.set("amount", keyReward.getAmount());
		} else if (reward instanceof CrateMoneyReward) {
			data.set("type", "money");
			data.set("amount", ((CrateMoneyReward) reward).getAmount());
		} else if (reward instanceof CrateOtherReward) {
			CrateOtherReward otherReward = (CrateOtherReward) reward;
			data.set("type", "other");
			data.set("crate", otherReward.getOther());
			data.set("icon", ItemUtils.toString(reward.getIcon()));
			data.set("random", otherReward.isRandom());
		} else
			throw new IllegalArgumentException("Could not save crate item reward class: " + reward.getClass().getName());
		data.set("weight", reward.getWeight());
	}

	private static @NotNull String crateIdFromFileName(@NotNull String fileName) {
		int lastDot = fileName.lastIndexOf('.');
		return lastDot == -1 ? fileName : fileName.substring(0, lastDot);
	}

	private static @NotNull String fileNameFromCrateId(@NotNull String crateId) {
		return crateId + ".yml";
	}
}
