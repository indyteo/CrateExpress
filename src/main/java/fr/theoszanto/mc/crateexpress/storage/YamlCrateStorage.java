package fr.theoszanto.mc.crateexpress.storage;

import fr.theoszanto.mc.crateexpress.CrateExpress;
import fr.theoszanto.mc.crateexpress.models.Crate;
import fr.theoszanto.mc.crateexpress.models.CrateKey;
import fr.theoszanto.mc.crateexpress.models.CrateNamespace;
import fr.theoszanto.mc.crateexpress.models.CrateRegistry;
import fr.theoszanto.mc.crateexpress.models.StatsRecord;
import fr.theoszanto.mc.crateexpress.models.reward.ClaimableReward;
import fr.theoszanto.mc.crateexpress.models.reward.CrateReward;
import fr.theoszanto.mc.crateexpress.models.reward.HistoricalReward;
import fr.theoszanto.mc.crateexpress.storage.yaml.CrateRewardYML;
import fr.theoszanto.mc.crateexpress.utils.MapUtils;
import fr.theoszanto.mc.crateexpress.utils.PluginObject;
import fr.theoszanto.mc.crateexpress.utils.TimeUtils;
import fr.theoszanto.mc.express.utils.ItemUtils;
import fr.theoszanto.mc.express.utils.LocationUtils;
import fr.theoszanto.mc.express.utils.MathUtils;
import fr.theoszanto.mc.express.utils.UnloadableWorldLocation;
import net.kyori.adventure.key.InvalidKeyException;
import net.kyori.adventure.key.Key;
import org.bukkit.DyeColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.Particle;
import org.bukkit.Registry;
import org.bukkit.Sound;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.intellij.lang.annotations.Subst;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.text.ParseException;
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
	public @NotNull List<@NotNull CrateNamespace> loadNamespaces() throws IllegalStateException {
		List<CrateNamespace> namespaces = new ArrayList<>();
		Path cratePath = this.cratesDir.toPath();
		try (Stream<Path> files = Files.find(cratePath, 10, this::namespaceFilesFilter)) {
			files.forEach(path -> {
				Path relativeParent = cratePath.relativize(path).getParent();
				String pathStr = relativeParent == null ? "" : relativeParent.toString();
				try {
					YamlConfiguration data = new YamlConfiguration();
					data.load(path.toFile());
					String colorStr = data.getString("color", null);
					DyeColor color;
					if (colorStr == null)
						color = null;
					else {
						try {
							color = DyeColor.valueOf(colorStr);
						} catch (IllegalArgumentException e) {
							throw new InvalidConfigurationException("Could not parse namespace color: " + colorStr, e);
						}
					}
					namespaces.add(new CrateNamespace(this.plugin, pathStr, color));
				} catch (IOException | InvalidConfigurationException | IllegalArgumentException e) {
					this.error("Could not load namespace: " + pathStr, e);
				}
			});
		} catch (IOException e) {
			throw new IllegalStateException("Could not list files from YamlStorage crates directory: " + this.cratesDir);
		}
		return namespaces;
	}

	@Override
	public void saveNamespace(@NotNull CrateNamespace namespace) throws IllegalStateException {
		String path = namespace.getPath();
		try {
			File file = new File(new File(this.cratesDir, path), ".namespace.yml");
			YamlConfiguration data = new YamlConfiguration();
			if (namespace.getColor() != null)
				data.set("color", namespace.getColor().name());
			data.save(file);
		} catch (IOException e) {
			throw new IllegalStateException("Could not save namespace: " + path, e);
		}
	}

	@Override
	public void deleteNamespace(@NotNull String path) throws IllegalStateException {
		File file = new File(this.cratesDir, path);
		if (!file.exists())
			return;
		try {
			delete(file);
		} catch (IOException e) {
			throw new IllegalStateException("Could not delete crate data file: " + file, e);
		}
		this.purgeEmptyParentDirs(file);
	}

	@Override
	public void loadCrates(@NotNull CrateRegistry registry) throws IllegalStateException {
		Path cratePath = this.cratesDir.toPath();
		try (Stream<Path> files = Files.find(cratePath, 10, this::crateFilesFilter)) {
			files.forEach(path -> {
				String id = crateIdFromFileName(cratePath.relativize(path).toString());
				try {
					File file = path.toFile();
					YamlConfiguration data = new YamlConfiguration();
					data.load(file);
					boolean disabled = data.getBoolean("disabled", false);
					String crateKey = data.getString("key", null);
					CrateKey key = crateKey == null ? null : new CrateKey(this.plugin, id, ItemUtils.fromString(crateKey));
					List<UnloadableWorldLocation> locations = data.contains("locations") ? data.getStringList("locations").stream().map(LocationUtils::fromString).collect(Collectors.toCollection(ArrayList::new)) : null;
					double delay = data.getDouble("delay", 0);
					boolean noPreview = data.getBoolean("no-preview", false);
					String name = data.getString("name", id);
					String message = data.getString("message", null);
					@Subst("minecraft:block.chest.open")
					String crateSound = data.getString("sound", null);
					Sound sound = null;
					if (crateSound != null) {
						try {
							sound = Registry.SOUNDS.get(Key.key(crateSound));
						} catch (InvalidKeyException ignored) {}
						if (sound == null) {
							try {
								// Old format - Try to update value
								sound = Sound.valueOf(crateSound.toUpperCase());
								data.set("sound", Registry.SOUNDS.getKeyOrThrow(sound).getKey());
								data.save(file);
							} catch (IllegalArgumentException ignored) {}
						}
						if (sound == null)
							this.warn("Unable to parse crate sound: " + crateSound + " (#" + id + ")");
					}
					String crateParticle = data.getString("particle", null);
					Particle particle = null;
					if (crateParticle != null) {
						try {
							particle = Particle.valueOf(crateParticle);
						} catch (IllegalArgumentException ignored) {}
						if (particle == null)
							this.warn("Unable to parse crate particle: " + crateParticle + " (#" + id + ")");
					}
					int particleCount = data.getInt("particle-count", 0);
					boolean random = data.getBoolean("random", true);
					boolean allowDuplicates = data.getBoolean("allow-duplicates", true);
					int min = data.getInt("min", 1);
					int max = data.getInt("max", 1);
					Crate crate = new Crate(
							this.plugin,
							id,
							disabled,
							key,
							locations,
							delay,
							noPreview,
							name,
							message,
							sound,
							particle,
							particleCount,
							random,
							allowDuplicates,
							min,
							max
					);
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
				data.set("locations", crate.getLocations().stream().map(LocationUtils::toString).toList());
			data.set("delay", crate.getDelay());
			data.set("no-preview", crate.isNoPreview());
			data.set("name", crate.getName());
			if (crate.getMessage() != null)
				data.set("message", crate.getMessage());
			if (crate.getSound() != null)
				data.set("sound", Registry.SOUNDS.getKeyOrThrow(crate.getSound()).getKey());
			if (crate.getParticle() != null)
				data.set("particle", crate.getParticle().getKey().getKey());
			data.set("particle-count", crate.getParticleCount());
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
		this.purgeEmptyParentDirs(file);
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
			this.rewardsCountCache.compute(uuid, MapUtils.INCREASE);
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
				Integer newCount = this.rewardsCountCache.computeIfPresent(uuid, MapUtils.DECREASE_NON_NEGATIVE);
				if (newCount != null && newCount == 0 && file.delete())
					return;
			}
			data.set(id, null);
			data.save(file);
		} catch (FileNotFoundException ignored) {
		} catch (IOException | InvalidConfigurationException e) {
			throw new IllegalStateException("Could not delete reward for player: " + player.getName() + " (" + uuid + ")", e);
		}
	}

	@Override
	public void clearRewards(@NotNull UUID uuid) throws IllegalStateException {
		File file = new File(this.rewardsDir, uuid + ".yml");
		if (!file.delete())
			throw new IllegalStateException("Could not clear rewards for player with UUID: " + uuid);
		this.rewardsCountCache.remove(uuid);
	}

	@Override
	public void migrateRewards(@NotNull UUID from, @NotNull UUID to) throws IllegalStateException {
		try {
			Path src = new File(this.rewardsDir, from + ".yml").toPath();
			Path dst = new File(this.rewardsDir, to + ".yml").toPath();
			Files.move(src, dst, StandardCopyOption.REPLACE_EXISTING);
			this.rewardsCountCache.put(to, this.rewardsCountCache.remove(from));
		} catch (NoSuchFileException ignored) {
		} catch (IOException e) {
			throw new IllegalStateException("Could not rename rewards file", e);
		}
	}

	@Override
	public int getOpenStats(@NotNull Crate crate) throws IllegalStateException {
		try {
			File cratesStatsFile = new File(this.statsDir, "crates.yml");
			YamlConfiguration cratesStats = new YamlConfiguration();
			cratesStats.load(cratesStatsFile);
			return cratesStats.getInt(crate.getId() + ".times-opened", 0);
		} catch (IOException | InvalidConfigurationException e) {
			throw new IllegalStateException("Could not get stats of crate opening", e);
		}
	}

	@Override
	public void updateStats(@NotNull List<@NotNull StatsRecord> stats) throws IllegalStateException {
		// Aggregate stats to improve file write perfs
		Map<String, CrateStatsUpdate> crateStatsUpdates = new HashMap<>();
		Map<Player, PlayerStatsUpdate> playerStatsUpdates = new HashMap<>();
		for (StatsRecord stat : stats) {
			Player player = stat.player();
			Crate crate = stat.crate();
			PlayerStatsUpdate playerStatsUpdate = playerStatsUpdates.computeIfAbsent(player, PlayerStatsUpdate::new);
			playerStatsUpdate.logs.add(stat);
			CrateStatsUpdate crateStatsUpdate = crateStatsUpdates.computeIfAbsent(crate.getId(), CrateStatsUpdate::new);
			CrateStatsUpdate playerCrateStatsUpdate = playerStatsUpdate.crateStatsUpdates.computeIfAbsent(crate.getId(), CrateStatsUpdate::new);
			crateStatsUpdate.timesOpened++;
			playerCrateStatsUpdate.timesOpened++;
			for (CrateReward reward : stat.rewards()) {
				crateStatsUpdate.rewardsGiven.compute(reward.getId(), MapUtils.INCREASE);
				playerCrateStatsUpdate.rewardsGiven.compute(reward.getId(), MapUtils.INCREASE);
			}
		}

		try {
			// Increase global stats
			this.updateCratesStats(this.statsDir, crateStatsUpdates);

			// Increase player stats
			for (Map.Entry<Player, PlayerStatsUpdate> entry : playerStatsUpdates.entrySet()) {
				File playerStatsDir = new File(this.statsDir, entry.getKey().getUniqueId().toString());
				PlayerStatsUpdate playerStatsUpdate = entry.getValue();
				this.updateCratesStats(playerStatsDir, playerStatsUpdate.crateStatsUpdates);

				// Log rewards received
				if (playerStatsUpdate.logs.isEmpty())
					continue;
				Date date = null;
				File playerRewardsLogStatsFile = null;
				YamlConfiguration playerDailyRewardsStats = null;
				for (StatsRecord log : playerStatsUpdate.logs) {
					// Ensure all the logs are on the same day (except initialization)
					if (date == null || TimeUtils.compareIgnoringTime(date, log.date()) != 0) {
						// If not, change current file (this should very rarely happen)
						if (date != null)
							playerDailyRewardsStats.save(playerRewardsLogStatsFile);
						date = log.date();
						playerRewardsLogStatsFile = new File(playerStatsDir, DATE_FORMAT.format(date) + ".yml");
						playerDailyRewardsStats = new YamlConfiguration();
						try {
							playerDailyRewardsStats.load(playerRewardsLogStatsFile);
						} catch (FileNotFoundException ignored) {}
					}
					// Save log
					String time = TIME_FORMAT.format(log.date());
					int n = MathUtils.nextAvailableInt(playerDailyRewardsStats.getKeys(false));
					for (CrateReward reward : log.rewards()) {
						ConfigurationSection playerRewardStats = playerDailyRewardsStats.createSection(Integer.toString(n++));
						playerRewardStats.set("time", time);
						playerRewardStats.set("reward", reward.getId());
						playerRewardStats.set("crate", log.crate().getId());
					}
				}
				playerDailyRewardsStats.save(playerRewardsLogStatsFile);
			}
		} catch (IOException | InvalidConfigurationException e) {
			throw new IllegalStateException("Could not save stats for crate opening", e);
		}
	}

	private static class CrateStatsUpdate {
		private int timesOpened = 0;
		private final @NotNull Map<@NotNull String, @NotNull Integer> rewardsGiven = new HashMap<>();

		private CrateStatsUpdate(@Nullable Object ignored) {}
	}

	private static class PlayerStatsUpdate {
		private final @NotNull Map<@NotNull String, @NotNull CrateStatsUpdate> crateStatsUpdates = new HashMap<>();
		private final @NotNull List<@NotNull StatsRecord> logs = new ArrayList<>();

		private PlayerStatsUpdate(@Nullable Object ignored) {}
	}

	private void updateCratesStats(@NotNull File dir, @NotNull Map<@NotNull String, @NotNull CrateStatsUpdate> crateStatsUpdates) throws IOException, InvalidConfigurationException {
		File cratesStatsFile = new File(dir, "crates.yml");
		YamlConfiguration cratesStats = new YamlConfiguration();
		try {
			cratesStats.load(cratesStatsFile);
		} catch (FileNotFoundException ignored) {}
		for (Map.Entry<String, CrateStatsUpdate> entry : crateStatsUpdates.entrySet()) {
			ConfigurationSection crateStats = getOrCreateSection(cratesStats, entry.getKey());
			CrateStatsUpdate crateStatsUpdate = entry.getValue();
			crateStats.set("times-opened", crateStats.getInt("times-opened", 0) + crateStatsUpdate.timesOpened);
			ConfigurationSection rewardsStats = getOrCreateSection(crateStats, "rewards-given");
			for (Map.Entry<String, Integer> reward : crateStatsUpdate.rewardsGiven.entrySet())
				rewardsStats.set(reward.getKey(), rewardsStats.getInt(reward.getKey(), 0) + reward.getValue());
			cratesStats.save(cratesStatsFile);
		}
	}

	@Override
	public @NotNull List<@NotNull HistoricalReward> listHistory(@NotNull OfflinePlayer player, @NotNull Date date) throws IllegalStateException {
		String uuid = player.getUniqueId().toString();
		try {
			File playerStatsDir = new File(this.statsDir, uuid);
			File playerRewardsLogFile = new File(playerStatsDir, DATE_FORMAT.format(date) + ".yml");
			YamlConfiguration playerDailyRewards = new YamlConfiguration();
			playerDailyRewards.load(playerRewardsLogFile);
			List<HistoricalReward> history = new ArrayList<>();
			for (String key : playerDailyRewards.getKeys(false)) {
				try {
					ConfigurationSection playerReward = playerDailyRewards.getConfigurationSection(key);
					assert playerReward != null;
					String timeStr = playerReward.getString("time");
					String crateStr = playerReward.getString("crate");
					String rewardStr = playerReward.getString("reward");
					if (timeStr == null || crateStr == null || rewardStr == null)
						throw new IllegalStateException("Missing reward data");
					Date datetime = TimeUtils.cloneWithTime(date, TIME_FORMAT.parse(timeStr));
					Crate crate = this.crates().resolve(crateStr);
					if (crate == null)
						throw new IllegalStateException("Unknown crate (" + crateStr + ")");
					CrateReward reward = crate.getReward(rewardStr);
					if (reward == null)
						throw new IllegalStateException("Unknown reward (" + rewardStr + ", in crate " + crate.getId() + ")");
					history.add(new HistoricalReward(datetime, crate, reward));
				} catch (ParseException | IllegalStateException e) {
					this.warn("Could not parse history element (player: " + player.getName() + ", " + uuid + "): " + e.getMessage());
				}
			}
			return history;
		} catch (FileNotFoundException e) {
			return new ArrayList<>();
		} catch (IOException | InvalidConfigurationException e) {
			throw new IllegalStateException("Could not list history for player: " + player.getName() + " (" + uuid + ")", e);
		}
	}

	@Override
	public void migratePlayerStats(@NotNull UUID from, @NotNull UUID to) throws IllegalStateException {
		try {
			Path src = new File(this.statsDir, from.toString()).toPath();
			Path dst = new File(this.statsDir, to.toString()).toPath();
			Files.move(src, dst, StandardCopyOption.REPLACE_EXISTING);
		} catch (IOException e) {
			throw new IllegalStateException("Could not rename player stats directory", e);
		}
	}

	@Override
	public void clearPlayerStats(@NotNull UUID uuid) throws IllegalStateException {
		File file = new File(this.statsDir, uuid.toString());
		if (!file.delete())
			throw new IllegalStateException("Could not clear player stats directory for UUID: " + uuid);
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

	private boolean namespaceFilesFilter(@NotNull Path path, @NotNull BasicFileAttributes attributes) {
		if (!attributes.isRegularFile())
			return false;
		return path.toFile().getName().matches("^\\.namespace\\.ya?ml$");
	}

	private boolean crateFilesFilter(@NotNull Path path, @NotNull BasicFileAttributes attributes) {
		if (!attributes.isRegularFile())
			return false;
		String relativePath = this.cratesDir.toPath().relativize(path).toString();
		if (!relativePath.matches("^.*\\.ya?ml$") || path.toFile().getName().matches("^\\.namespace\\.ya?ml$"))
			return false;
		for (String ignore : this.ignoreFiles) {
			if (ignore.equalsIgnoreCase(relativePath))
				return false;
			if (ignore.endsWith("/") && relativePath.startsWith(ignore))
				return false;
		}
		return true;
	}

	private void purgeEmptyParentDirs(@NotNull File file) {
		File parent = file.getParentFile();
		while (!this.cratesDir.equals(parent)) {
			String[] list = parent.list();
			if ((list == null || list.length == 0) && !parent.delete())
				throw new IllegalStateException("Could not cleanup crate data parent dir: " + parent);
			parent = parent.getParentFile();
		}
	}

	private static void delete(@NotNull File file) throws IOException {
		if (file.isDirectory()) {
			File[] files = file.listFiles();
			if (files == null)
				throw new IOException("Could not list files in directory: " + file);
			else
				for (File f : files)
					delete(f);
		}
		if (!file.delete())
			throw new IOException("Could not delete file: " + file);
	}

	@NotNull
	private static ConfigurationSection getOrCreateSection(@NotNull ConfigurationSection cratesStats, @NotNull String key) {
		ConfigurationSection crateStats = cratesStats.getConfigurationSection(key);
		return crateStats == null ? cratesStats.createSection(key) : crateStats;
	}
}
