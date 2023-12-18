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
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class YamlCrateStorage extends PluginObject implements CrateStorage {
	private final @NotNull File cratesDir;
	private final @NotNull File rewardsDir;
	private final @NotNull File statsDir;
	private final @NotNull List<@NotNull String> ignoreFiles;
	private final @NotNull Map<@NotNull UUID, @NotNull Integer> rewardsCountCache = new HashMap<>();

	private static final @NotNull SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");
	private static final @NotNull SimpleDateFormat TIME_FORMAT = new SimpleDateFormat("HH:mm:ss");

	public YamlCrateStorage(@NotNull CrateExpress plugin, @NotNull String cratesDir, @NotNull String rewardsDir, @NotNull String statsDir) throws IllegalArgumentException {
		this(plugin, cratesDir, rewardsDir, statsDir, new ArrayList<>());
	}

	public YamlCrateStorage(@NotNull CrateExpress plugin, @NotNull String cratesDir, @NotNull String rewardsDir, @NotNull String statsDir, @NotNull ArrayList<@NotNull String> ignoreFiles) throws IllegalArgumentException {
		super(plugin);
		this.cratesDir = this.initDir(cratesDir, "crates");
		this.rewardsDir = this.initDir(rewardsDir, "rewards");
		this.statsDir = this.initDir(statsDir, "stats");
		this.ignoreFiles = ignoreFiles;
	}

	protected @NotNull File initDir(@NotNull String dir, @NotNull String name) {
		File file = new File(this.plugin.getDataFolder(), dir);
		if (!(file.exists() ? file.isDirectory() : file.mkdirs()))
			throw new IllegalArgumentException("YamlStorage " + name + " directory is invalid: " + file);
		return file;
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
					boolean disabled = data.getBoolean("disabled", false);
					String crateKey = data.getString("key", null);
					CrateKey key = crateKey == null ? null : new CrateKey(this.plugin, id, ItemUtils.fromString(crateKey));
					List<UnloadableWorldLocation> locations = data.contains("locations") ? data.getStringList("locations").stream().map(LocationUtils::fromString).collect(Collectors.toCollection(ArrayList::new)) : null;
					double delay = data.getDouble("delay", 0);
					boolean noPreview = data.getBoolean("no-preview", false);
					String name = data.getString("name", id);
					String message = data.getString("message", null);
					String crateSound = data.getString("sound", null);
					Sound sound;
					try {
						sound = crateSound == null ? null : Sound.valueOf(crateSound.toUpperCase());
					} catch (IllegalArgumentException e) {
						sound = null;
					}
					boolean random = data.getBoolean("random", true);
					boolean allowDuplicates = data.getBoolean("allow-duplicates", true);
					int min = data.getInt("min", 1);
					int max = data.getInt("max", 1);
					Crate crate = new Crate(this.plugin, id, disabled, key, locations, delay, noPreview, name, message, sound, random, allowDuplicates, min, max);
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
			data.set("disabled", crate.isDisabled());
			CrateKey key = crate.getKey();
			if (key != null)
				data.set("key", ItemUtils.toString(key.getItem()));
			if (crate.getLocations() != null)
				data.set("locations", crate.getLocations().stream().map(LocationUtils::toString).collect(Collectors.toList()));
			data.set("delay", crate.getDelay());
			data.set("no-preview", crate.isNoPreview());
			data.set("name", crate.getName());
			if (crate.getMessage() != null)
				data.set("message", crate.getMessage());
			if (crate.getSound() != null)
				data.set("sound", crate.getSound().name());
			data.set("random", crate.isRandom());
			data.set("allow-duplicates", crate.doesAllowDuplicates());
			data.set("min", crate.getMin());
			data.set("max", crate.getMax());
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

	@Override
	public void crateOpenStats(@NotNull Player player, @NotNull Crate crate, @NotNull List<@NotNull CrateReward> rewards) throws IllegalStateException {
		try {
			// Increase global stats
			this.saveCrateOpenStats(this.statsDir, crate, rewards);

			// Increase player stats
			File playerStatsDir = new File(this.statsDir, player.getUniqueId().toString());
			this.saveCrateOpenStats(playerStatsDir, crate, rewards);

			// Log rewards received
			Date now = new Date();
			File playerRewardsLogStatsFile = new File(playerStatsDir, DATE_FORMAT.format(now) + ".yml");
			YamlConfiguration playerDailyRewardsStats = new YamlConfiguration();
			try {
				playerDailyRewardsStats.load(playerRewardsLogStatsFile);
			} catch (FileNotFoundException ignored) {}
			int n = MathUtils.nextAvailableInt(playerDailyRewardsStats.getKeys(false));
			for (CrateReward reward : rewards) {
				ConfigurationSection playerRewardStats = playerDailyRewardsStats.createSection(Integer.toString(n++));
				playerRewardStats.set("time", TIME_FORMAT.format(now));
				playerRewardStats.set("reward", reward.getId());
				playerRewardStats.set("crate", crate.getId());
			}
			playerDailyRewardsStats.save(playerRewardsLogStatsFile);
		} catch (IOException | InvalidConfigurationException e) {
			throw new IllegalStateException("Could not save stats for crate opening", e);
		}
	}

	private void saveCrateOpenStats(@NotNull File dir, @NotNull Crate crate, @NotNull List<@NotNull CrateReward> rewards) throws IOException, InvalidConfigurationException {
		File cratesStatsFile = new File(dir, "crates.yml");
		YamlConfiguration cratesStats = new YamlConfiguration();
		try {
			cratesStats.load(cratesStatsFile);
		} catch (FileNotFoundException ignored) {}
		ConfigurationSection crateStats = getOrCreateSection(cratesStats, crate.getId());
		crateStats.set("times-opened", crateStats.getInt("times-opened", 0) + 1);
		ConfigurationSection rewardsStats = getOrCreateSection(crateStats, "rewards-given");
		for (CrateReward reward : rewards)
			rewardsStats.set(reward.getId(), rewardsStats.getInt(reward.getId(), 0) + 1);
		cratesStats.save(cratesStatsFile);
	}

	@Override
	public void migratePlayerStats(@NotNull UUID from, @NotNull UUID to) throws IllegalStateException {
		try {
			Path src = new File(this.statsDir, from.toString()).toPath();
			Path dst = new File(this.statsDir, to.toString()).toPath();
			Files.move(src, dst, StandardCopyOption.REPLACE_EXISTING);
		} catch (IOException e) {
			throw new IllegalStateException("Could not rename stats directory", e);
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

	protected static @NotNull String crateIdFromFileName(@NotNull String fileName) {
		int lastDot = fileName.lastIndexOf('.');
		return lastDot == -1 ? fileName : fileName.substring(0, lastDot);
	}

	protected static @NotNull String fileNameFromCrateId(@NotNull String crateId) {
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

	@NotNull
	private static ConfigurationSection getOrCreateSection(@NotNull ConfigurationSection cratesStats, @NotNull String key) {
		ConfigurationSection crateStats = cratesStats.getConfigurationSection(key);
		return crateStats == null ? cratesStats.createSection(key) : crateStats;
	}
}
