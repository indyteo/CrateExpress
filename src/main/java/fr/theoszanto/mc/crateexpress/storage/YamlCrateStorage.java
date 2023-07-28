package fr.theoszanto.mc.crateexpress.storage;

import fr.theoszanto.mc.crateexpress.CrateExpress;
import fr.theoszanto.mc.crateexpress.models.Crate;
import fr.theoszanto.mc.crateexpress.models.CrateKey;
import fr.theoszanto.mc.crateexpress.models.CrateRegistry;
import fr.theoszanto.mc.crateexpress.models.reward.ClaimableReward;
import fr.theoszanto.mc.crateexpress.models.reward.CrateReward;
import fr.theoszanto.mc.crateexpress.storage.yaml.CrateRewardYML;
import fr.theoszanto.mc.crateexpress.utils.PluginObject;
import fr.theoszanto.mc.express.utils.ItemUtils;
import fr.theoszanto.mc.express.utils.LocationUtils;
import fr.theoszanto.mc.express.utils.MathUtils;
import fr.theoszanto.mc.express.utils.UnloadableWorldLocation;
import org.bukkit.Sound;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Stream;

public class YamlCrateStorage extends PluginObject implements CrateStorage {
	private final @NotNull File cratesDir;
	private final @NotNull File rewardsDir;
	private final @NotNull List<@NotNull String> ignoreFiles;
	private final @NotNull Map<@NotNull UUID, @NotNull Integer> rewardsCountCache = new HashMap<>();

	public YamlCrateStorage(@NotNull CrateExpress plugin, @NotNull String cratesDir, @NotNull String rewardsDir) throws IllegalArgumentException {
		this(plugin, cratesDir, rewardsDir, new ArrayList<>());
	}

	public YamlCrateStorage(@NotNull CrateExpress plugin, @NotNull String cratesDir, @NotNull String rewardsDir, @NotNull ArrayList<@NotNull String> ignoreFiles) throws IllegalArgumentException {
		super(plugin);
		this.cratesDir = new File(plugin.getDataFolder(), cratesDir);
		if (!(this.cratesDir.exists() ? this.cratesDir.isDirectory() : this.cratesDir.mkdirs()))
			throw new IllegalArgumentException("YamlStorage crates directory is invalid: " + this.cratesDir);
		this.rewardsDir = new File(plugin.getDataFolder(), rewardsDir);
		if (!(this.rewardsDir.exists() ? this.rewardsDir.isDirectory() : this.rewardsDir.mkdirs()))
			throw new IllegalArgumentException("YamlStorage rewards directory is invalid: " + this.rewardsDir);
		this.ignoreFiles = ignoreFiles;
	}

	@Override
	public void loadCrates(@NotNull CrateRegistry registry) throws IllegalStateException {
		Path cratePath = this.cratesDir.toPath();
		try (Stream<Path> files = Files.find(cratePath, 10, this::crateFilesFilter)) {
			files.forEach(path -> {
				String id = crateIdFromFileName(cratePath.relativize(path).toString());
				try {
					YamlConfiguration data = new YamlConfiguration();
					data.load(path.toFile());
					int min = data.getInt("min", 1);
					int max = data.getInt("max", 1);
					String crateKey = data.getString("key", null);
					CrateKey key = crateKey == null ? null : new CrateKey(this.plugin, id, ItemUtils.fromString(crateKey));
					String name = data.getString("name", id);
					String message = data.getString("message", null);
					String crateLocation = data.getString("location", null);
					UnloadableWorldLocation location = crateLocation == null ? null : LocationUtils.fromString(crateLocation);
					double delay = data.getDouble("delay", 0);
					String crateSound = data.getString("sound", null);
					Sound sound;
					try {
						sound = crateSound == null ? null : Sound.valueOf(crateSound.toUpperCase());
					} catch (IllegalArgumentException e) {
						sound = null;
					}
					boolean disabled = data.getBoolean("disabled", false);
					boolean noPreview = data.getBoolean("no-preview", false);
					Crate crate = new Crate(this.plugin, id, min, max, key, name, message, location, delay, sound, disabled, noPreview);
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
							crate.addReward(slot, this.deserializeReward(reward));
						}
					}
					registry.register(id, crate);
				} catch (IOException | InvalidConfigurationException | IllegalArgumentException e) {
					this.error("Could not load crate: " + id, e);
				}
			});
		} catch (IOException e) {
			throw new IllegalStateException("Could not list files from YamlStorage crates directory: " + this.cratesDir);
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
			data.set("delay", crate.getDelay());
			if (crate.getSound() != null)
				data.set("sound", crate.getSound().name());
			data.set("disabled", crate.isDisabled());
			data.set("no-preview", crate.isNoPreview());
			ConfigurationSection items = data.createSection("items");
			crate.getRewardsWithSlot().forEach((slot, reward) -> {
				ConfigurationSection rewardData = items.createSection(slot.toString());
				this.serializeReward(rewardData, reward);
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
		File parent = file.getParentFile();
		while (!this.cratesDir.equals(parent)) {
			String[] list = parent.list();
			if ((list == null || list.length == 0) && !parent.delete())
				throw new IllegalStateException("Could not cleanup crate data parent dir: " + parent);
			parent = parent.getParentFile();
		}
	}

	@Override
	public void saveReward(@NotNull Player player, @NotNull CrateReward reward) throws IllegalStateException {
		UUID uuid = player.getUniqueId();
		try {
			File file = new File(this.rewardsDir, uuid + ".yml");
			YamlConfiguration data = new YamlConfiguration();
			try {
				data.load(file);
			} catch (FileNotFoundException ignored) {}
			ConfigurationSection rewardData = data.createSection(Integer.toString(MathUtils.nextAvailableInt(data.getKeys(false))));
			this.serializeReward(rewardData, reward);
			data.save(file);
			this.rewardsCountCache.compute(uuid, (u, count) -> count == null ? 1 : count + 1);
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
				rewards.add(new ClaimableReward(id, this.deserializeReward(reward)));
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
		UUID uuid = player.getUniqueId();
		return this.rewardsCountCache.computeIfAbsent(uuid, u -> {
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
		});
	}

	@Override
	public void deleteReward(@NotNull Player player, @NotNull String id) throws IllegalStateException {
		UUID uuid = player.getUniqueId();
		try {
			File file = new File(this.rewardsDir, uuid + ".yml");
			YamlConfiguration data = new YamlConfiguration();
			data.load(file);
			boolean exists = data.contains(id);
			if (exists) {
				Integer newCount = this.rewardsCountCache.computeIfPresent(uuid, (u, count) -> count > 0 ? count - 1 : 0);
				if (newCount != null && newCount == 0) {
					if (file.delete())
						return;
				}
			}
			data.set(id, null);
			data.save(file);
		} catch (FileNotFoundException ignored) {
		} catch (IOException | InvalidConfigurationException e) {
			throw new IllegalStateException("Could not delete reward for player: " + player.getName() + " (" + uuid + ")", e);
		}
	}

	@Override
	public void migrateRewards(@NotNull UUID from, @NotNull UUID to) throws IllegalStateException {
		try {
			Path src = new File(this.rewardsDir, from + ".yml").toPath();
			Path dst = new File(this.rewardsDir, to + ".yml").toPath();
			Files.move(src, dst, StandardCopyOption.REPLACE_EXISTING);
			this.rewardsCountCache.put(to, this.rewardsCountCache.remove(from));
		} catch (IOException e) {
			throw new IllegalStateException("Could not rename rewards file", e);
		}
	}

	private @NotNull CrateReward deserializeReward(@NotNull ConfigurationSection reward) throws InvalidConfigurationException {
		String type = reward.getString("type", "null");
		CrateRewardStorage<?> rewardStorage = this.storage().getRewardSource(type);
		if (rewardStorage == null)
			rewardStorage = this.storage().getRewardSource("unknown");
		if (!(rewardStorage instanceof CrateRewardYML<?>))
			throw new InvalidConfigurationException("Could not parse crate item reward type: " + type);
		return rewardStorage.deserializeReward(reward);
	}

	private void serializeReward(@NotNull ConfigurationSection data, @NotNull CrateReward reward) {
		CrateRewardStorage<?> rewardStorage = this.storage().getRewardSource(reward.getType());
		if (!(rewardStorage instanceof CrateRewardYML<?>))
			throw new IllegalArgumentException("Could not save crate item reward class: " + reward.getClass().getName());
		rewardStorage.serializeReward(reward, data);
	}

	private static @NotNull String crateIdFromFileName(@NotNull String fileName) {
		int lastDot = fileName.lastIndexOf('.');
		return lastDot == -1 ? fileName : fileName.substring(0, lastDot);
	}

	private static @NotNull String fileNameFromCrateId(@NotNull String crateId) {
		return crateId + ".yml";
	}

	private boolean crateFilesFilter(@NotNull Path path, @NotNull BasicFileAttributes attributes) {
		if (!attributes.isRegularFile())
			return false;
		String relativePath = this.cratesDir.toPath().relativize(path).toString();
		if (!relativePath.matches("^.*\\.ya?ml$"))
			return false;
		for (String ignore : this.ignoreFiles) {
			if (ignore.equalsIgnoreCase(relativePath))
				return false;
			if (ignore.endsWith("/") && relativePath.startsWith(ignore))
				return false;
		}
		return true;
	}
}
