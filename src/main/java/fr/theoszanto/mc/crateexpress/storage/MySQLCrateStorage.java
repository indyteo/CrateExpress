package fr.theoszanto.mc.crateexpress.storage;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import fr.theoszanto.mc.crateexpress.CrateExpress;
import fr.theoszanto.mc.crateexpress.models.Crate;
import fr.theoszanto.mc.crateexpress.models.CrateKey;
import fr.theoszanto.mc.crateexpress.models.CrateNamespace;
import fr.theoszanto.mc.crateexpress.models.CrateRegistry;
import fr.theoszanto.mc.crateexpress.models.StatsRecord;
import fr.theoszanto.mc.crateexpress.models.reward.ClaimableReward;
import fr.theoszanto.mc.crateexpress.models.reward.CrateReward;
import fr.theoszanto.mc.crateexpress.models.reward.HistoricalReward;
import fr.theoszanto.mc.crateexpress.storage.json.CrateRewardJSON;
import fr.theoszanto.mc.crateexpress.utils.MapUtils;
import fr.theoszanto.mc.crateexpress.utils.PluginObject;
import fr.theoszanto.mc.express.utils.ItemUtils;
import fr.theoszanto.mc.express.utils.LocationUtils;
import fr.theoszanto.mc.express.utils.UnloadableWorldLocation;
import net.kyori.adventure.key.InvalidKeyException;
import net.kyori.adventure.key.Key;
import org.bukkit.DyeColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.Particle;
import org.bukkit.Registry;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.intellij.lang.annotations.Language;
import org.intellij.lang.annotations.Subst;
import org.jetbrains.annotations.NotNull;

import java.io.Reader;
import java.lang.reflect.Type;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

public abstract class MySQLCrateStorage extends PluginObject implements CrateStorage {
	private final @NotNull String tablePrefix;
	private final @NotNull Map<@NotNull UUID, @NotNull Integer> rewardsCountCache = new HashMap<>();

	private static final @NotNull Gson GSON = new Gson();
	private static final @NotNull Type JSON_OBJECTS = new TypeToken<Map<Integer, JsonObject>>() {}.getType();

	public MySQLCrateStorage(@NotNull CrateExpress plugin, @NotNull String tablePrefix) {
		super(plugin);
		this.tablePrefix = tablePrefix;
	}

	protected abstract @NotNull Connection getConnection();

	protected @NotNull String formatWithTablePrefix(@NotNull @Language("SQL") String sql) {
		return sql.replace("prefix_", this.tablePrefix);
	}

	protected @NotNull PreparedStatement prepareSQL(@NotNull @Language("SQL") String sql) throws SQLException {
		return this.getConnection().prepareStatement(this.formatWithTablePrefix(sql));
	}

	@Override
	public void initialize() throws IllegalStateException {
		try {
			try (PreparedStatement createNamespacesTable = this.prepareSQL("""
					CREATE TABLE IF NOT EXISTS `prefix_crate_namespaces` (
						`path` VARCHAR(512) PRIMARY KEY NOT NULL,
						`color` VARCHAR(64) DEFAULT NULL
					)""")) {
				createNamespacesTable.execute();
			}
			try (PreparedStatement createCratesTable = this.prepareSQL("""
					CREATE TABLE IF NOT EXISTS `prefix_crates` (
						`id` VARCHAR(512) PRIMARY KEY NOT NULL,
						`disabled` BOOLEAN NOT NULL DEFAULT FALSE,
						`key` BLOB,
						`locations` TEXT,
						`delay` DOUBLE UNSIGNED NOT NULL DEFAULT 0,
						`no_preview` BOOLEAN NOT NULL DEFAULT FALSE,
						`name` TEXT NOT NULL,
						`message` TEXT,
						`sound` VARCHAR(256) DEFAULT NULL,
						`particle` VARCHAR(256) DEFAULT NULL,
						`particle_count` INT UNSIGNED NOT NULL DEFAULT 0,
						`random` BOOLEAN NOT NULL DEFAULT TRUE,
						`allow_duplicates` BOOLEAN NOT NULL DEFAULT TRUE,
						`min` INT UNSIGNED NOT NULL DEFAULT 1,
						`max` INT UNSIGNED NOT NULL DEFAULT 1,
						`items` JSON
					)""")) {
				createCratesTable.execute();
			}
			try (PreparedStatement createRewardsTable = this.prepareSQL("""
					CREATE TABLE IF NOT EXISTS `prefix_crate_claimed_rewards` (
						`id` INT UNSIGNED PRIMARY KEY AUTO_INCREMENT NOT NULL,
						`player` BINARY(16) NOT NULL,
						`reward` JSON,
						INDEX (`player`)
					)""")) {
				createRewardsTable.execute();
			}
			try (PreparedStatement createHistoryTable = this.prepareSQL("""
					CREATE TABLE IF NOT EXISTS `prefix_crate_history` (
						`id` INT UNSIGNED PRIMARY KEY AUTO_INCREMENT NOT NULL,
						`open_id` INT UNSIGNED NOT NULL,
						`player` BINARY(16),
						`date` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
						`crate` VARCHAR(512) NOT NULL,
						`reward` VARCHAR(64) NOT NULL,
						INDEX (`open_id`),
						INDEX (`player`),
						INDEX (`crate`)
					)""")) {
				createHistoryTable.execute();
			}
		} catch (SQLException e) {
			throw new IllegalStateException("Unable to initialize SQL storage", e);
		}
	}

	@Override
	public @NotNull List<@NotNull CrateNamespace> loadNamespaces() throws IllegalStateException {
		try (PreparedStatement listNamespaces = this.prepareSQL("SELECT `path`, `color` FROM `prefix_crate_namespaces`")) {
			ResultSet result = listNamespaces.executeQuery();
			List<CrateNamespace> namespaces = new ArrayList<>();
			while (result.next()) {
				String path = result.getString("path");
				try {
					String colorStr = result.getString("color");
					DyeColor color;
					if (colorStr == null)
						color = null;
					else {
						try {
							color = DyeColor.valueOf(colorStr);
						} catch (IllegalArgumentException e) {
							throw new IllegalStateException("Could not parse namespace color: " + colorStr, e);
						}
					}
					namespaces.add(new CrateNamespace(this.plugin, path, color));
				} catch (Throwable e) {
					this.error("Could not load namespace: " + path, e);
				}
			}
			return namespaces;
		} catch (SQLException e) {
			throw new IllegalStateException("Unable to list namespaces", e);
		}
	}

	@Override
	public void saveNamespace(@NotNull CrateNamespace namespace) throws IllegalStateException {
		try (PreparedStatement setNamespace = this.prepareSQL("INSERT INTO `prefix_crate_namespaces` (`path`, `color`) VALUES (?, ?) AS new(`new_path`, `new_color`) ON DUPLICATE KEY UPDATE `color` = `new_color`")) {
			setNamespace.setString(1, namespace.getPath());
			if (namespace.getColor() == null)
				setNamespace.setNull(2, Types.VARCHAR);
			else
				setNamespace.setString(2, namespace.getColor().name());
			setNamespace.executeUpdate();
		} catch (SQLException e) {
			throw new IllegalStateException("Unable to save namespace: " + namespace.getPath(), e);
		}
	}

	@Override
	public void deleteNamespace(@NotNull String path) throws IllegalStateException {
		try (PreparedStatement removeNamespace = this.prepareSQL("DELETE FROM `prefix_crate_namespaces` WHERE `path` = ?")) {
			removeNamespace.setString(1, path);
			removeNamespace.executeUpdate();
		} catch (SQLException e) {
			throw new IllegalStateException("Unable to delete namespace: " + path, e);
		}
	}

	@Override
	public void loadCrates(@NotNull CrateRegistry registry) throws IllegalStateException {
		try (PreparedStatement listCrates = this.prepareSQL("SELECT `id`, `disabled`, `key`, `locations`, `delay`, `no_preview`, `name`, `message`, `sound`, `particle`, `particle_count`, `random`, `allow_duplicates`, `min`, `max`, `items` FROM `prefix_crates`")) {
			ResultSet result = listCrates.executeQuery();
			while (result.next()) {
				String id = result.getString("id");
				try {
					boolean disabled = result.getBoolean("disabled");
					byte[] crateKey = result.getBytes("key");
					CrateKey key = crateKey == null ? null : new CrateKey(this.plugin, id, ItemUtils.fromBytes(crateKey));
					String crateLocations = result.getString("locations");
					List<UnloadableWorldLocation> locations = crateLocations == null ? null : crateLocations.lines().map(LocationUtils::fromString).collect(Collectors.toCollection(ArrayList::new));
					double delay = result.getDouble("delay");
					boolean noPreview = result.getBoolean("no_preview");
					String name = result.getString("name");
					String message = result.getString("message");
					@Subst("minecraft:block.chest.open")
					String crateSound = result.getString("sound");
					Sound sound = null;
					if (crateSound != null) {
						try {
							sound = Registry.SOUNDS.get(Key.key(crateSound));
						} catch (InvalidKeyException ignored) {}
						if (sound == null) {
							try {
								// Old format - Try to update value
								sound = Sound.valueOf(crateSound.toUpperCase());
								try (PreparedStatement migrateSound = this.prepareSQL("UPDATE `prefix_crates` SET `sound` = ? WHERE `id` = ?")) {
									migrateSound.setString(1, Registry.SOUNDS.getKeyOrThrow(sound).getKey());
									migrateSound.setString(2, id);
									migrateSound.executeUpdate();
								}
							} catch (SQLException | IllegalArgumentException ignored) {}
						}
						if (sound == null)
							this.warn("Unable to parse crate sound: " + crateSound + " (#" + id + ")");
					}
					String crateParticle = result.getString("particle");
					Particle particle = null;
					if (crateParticle != null) {
						try {
							particle = Particle.valueOf(crateParticle);
						} catch (IllegalArgumentException ignored) {}
						if (particle == null)
							this.warn("Unable to parse crate particle: " + crateParticle + " (#" + id + ")");
					}
					int particleCount = result.getInt("particle_count");
					boolean random = result.getBoolean("random");
					boolean allowDuplicates = result.getBoolean("allow_duplicates");
					int min = result.getInt("min");
					int max = result.getInt("max");
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
					Reader crateItems = result.getCharacterStream("items");
					if (crateItems != null) {
						Map<Integer, JsonObject> items = GSON.fromJson(crateItems, JSON_OBJECTS);
						items.forEach((slot, reward) -> crate.addReward(slot, this.deserializeReward(reward)));
					}
					registry.register(id, crate);
				} catch (Throwable e) {
					this.error("Could not load crate: " + id, e);
				}
			}
		} catch (SQLException e) {
			throw new IllegalStateException("Unable to list crates", e);
		}
	}

	@Override
	public void saveCrate(@NotNull Crate crate) throws IllegalStateException {
		try (PreparedStatement setCrate = this.prepareSQL("""
				INSERT INTO `prefix_crates` (
					`id`,
					`disabled`,
					`key`,
					`locations`,
					`delay`,
					`no_preview`,
					`name`,
					`message`,
					`sound`,
					`particle`,
					`particle_count`,
					`random`,
					`allow_duplicates`,
					`min`,
					`max`,
					`items`
				) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?) AS new(
					`new_id`,
					`new_disabled`,
					`new_key`,
					`new_locations`,
					`new_delay`,
					`new_no_preview`,
					`new_name`,
					`new_message`,
					`new_sound`,
					`new_particle`,
					`new_particle_count`,
					`new_random`,
					`new_allow_duplicates`,
					`new_min`,
					`new_max`,
					`new_items`
				) ON DUPLICATE KEY UPDATE
					`disabled` = `new_disabled`,
					`key` = `new_key`,
					`locations` = `new_locations`,
					`delay` = `new_delay`,
					`no_preview` = `new_no_preview`,
					`name` = `new_name`,
					`message` = `new_message`,
					`sound` = `new_sound`,
					`particle` = `new_particle`,
					`particle_count` = `new_particle_count`,
					`random` = `new_random`,
					`allow_duplicates` = `new_allow_duplicates`,
					`min` = `new_min`,
					`max` = `new_max`,
					`items` = `new_items`""")) {
			setCrate.setString(1, crate.getId());
			setCrate.setBoolean(2, crate.isDisabled());
			if (crate.getKey() == null)
				setCrate.setNull(3, Types.BLOB);
			else
				setCrate.setBytes(3, ItemUtils.toBytes(crate.getKey().getItem()));
			if (crate.getLocations() == null)
				setCrate.setNull(4, Types.LONGVARCHAR);
			else
				setCrate.setString(4, crate.getLocations().stream().map(LocationUtils::toString).collect(Collectors.joining("\n")));
			setCrate.setDouble(5, crate.getDelay());
			setCrate.setBoolean(6, crate.isNoPreview());
			setCrate.setString(7, crate.getName());
			if (crate.getMessage() == null)
				setCrate.setNull(8, Types.LONGVARCHAR);
			else
				setCrate.setString(8, crate.getMessage());
			if (crate.getSound() == null)
				setCrate.setNull(9, Types.VARCHAR);
			else
				setCrate.setString(9, Registry.SOUNDS.getKeyOrThrow(crate.getSound()).getKey());
			if (crate.getParticle() == null)
				setCrate.setNull(10, Types.VARCHAR);
			else
				setCrate.setString(10, crate.getParticle().getKey().getKey());
			setCrate.setInt(11, crate.getParticleCount());
			setCrate.setBoolean(12, crate.isRandom());
			setCrate.setBoolean(13, crate.doesAllowDuplicates());
			setCrate.setInt(14, crate.getMin());
			setCrate.setInt(15, crate.getMax());
			Map<Integer, JsonObject> items = new HashMap<>();
			crate.getRewardsWithSlot().forEach((slot, reward) -> {
				JsonObject rewardData = new JsonObject();
				this.serializeReward(rewardData, reward);
				items.put(slot, rewardData);
			});
			setCrate.setString(16, GSON.toJson(items));
			setCrate.executeUpdate();
		} catch (SQLException e) {
			throw new IllegalStateException("Unable to save crate: " + crate.getId(), e);
		}
	}

	@Override
	public void deleteCrate(@NotNull String id) throws IllegalStateException {
		try (PreparedStatement removeCrate = this.prepareSQL("DELETE FROM `prefix_crates` WHERE `id` = ?")) {
			removeCrate.setString(1, id);
			removeCrate.executeUpdate();
		} catch (SQLException e) {
			throw new IllegalStateException("Unable to delete crate: " + id, e);
		}
	}

	@Override
	public void saveReward(@NotNull OfflinePlayer player, @NotNull CrateReward reward) throws IllegalStateException {
		UUID uuid = player.getUniqueId();
		try (PreparedStatement insertReward = this.prepareSQL("INSERT INTO `prefix_crate_claimed_rewards` (`player`, `reward`) VALUES (UUID_TO_BIN(?), ?)")) {
			insertReward.setString(1, uuid.toString());
			JsonObject rewardData = new JsonObject();
			this.serializeReward(rewardData, reward);
			insertReward.setString(2, GSON.toJson(rewardData));
			insertReward.executeUpdate();
			this.rewardsCountCache.compute(uuid, MapUtils.INCREASE);
		} catch (SQLException e) {
			throw new IllegalStateException("Unable to save player reward: " + uuid, e);
		}
	}

	@Override
	public @NotNull List<@NotNull ClaimableReward> listRewards(@NotNull Player player) throws IllegalStateException {
		try (PreparedStatement listRewards = this.prepareSQL("SELECT `id`, `reward` FROM `prefix_crate_claimed_rewards` WHERE `player` = UUID_TO_BIN(?)")) {
			ResultSet result = listRewards.executeQuery();
			List<ClaimableReward> rewards = new ArrayList<>();
			while (result.next()) {
				int id = result.getInt("id");
				Reader reward = result.getCharacterStream("reward");
				JsonObject rewardData = GSON.fromJson(reward, JsonObject.class);
				rewards.add(new ClaimableReward(Integer.toString(id), this.deserializeReward(rewardData)));
			}
			this.rewardsCountCache.put(player.getUniqueId(), rewards.size());
			return rewards;
		} catch (SQLException e) {
			throw new IllegalStateException("Unable to list namespaces", e);
		}
	}

	@Override
	public int countRewards(@NotNull Player player) throws IllegalStateException {
		return this.rewardsCountCache.computeIfAbsent(player.getUniqueId(), uuid -> {
			try (PreparedStatement countRewards = this.prepareSQL("SELECT COUNT(`id`) AS `total` FROM `prefix_crate_claimed_rewards` WHERE `player` = UUID_TO_BIN(?)")) {
				countRewards.setString(1, uuid.toString());
				ResultSet result = countRewards.executeQuery();
				return result.next() ? result.getInt("total") : 0;
			} catch (SQLException e) {
				throw new IllegalStateException("Unable to count player rewards: " + uuid, e);
			}
		});

	}

	@Override
	public void deleteReward(@NotNull Player player, @NotNull String id) throws IllegalStateException {
		try (PreparedStatement removeReward = this.prepareSQL("DELETE FROM `prefix_crate_claimed_rewards` WHERE `id` = ? AND `player` = UUID_TO_BIN(?)")) {
			removeReward.setInt(1, Integer.parseInt(id));
			removeReward.setString(2, player.getUniqueId().toString());
			removeReward.executeUpdate();
			this.rewardsCountCache.computeIfPresent(player.getUniqueId(), MapUtils.DECREASE_NON_NEGATIVE);
		} catch (SQLException e) {
			throw new IllegalStateException("Unable to delete player reward: " + id, e);
		}
	}

	@Override
	public void clearRewards(@NotNull UUID uuid) throws IllegalStateException {
		try (PreparedStatement removeAllRewards = this.prepareSQL("DELETE FROM `prefix_crate_claimed_rewards` WHERE `player` = UUID_TO_BIN(?)")) {
			removeAllRewards.setString(1, uuid.toString());
			removeAllRewards.executeUpdate();
			this.rewardsCountCache.remove(uuid);
		} catch (SQLException e) {
			throw new IllegalStateException("Unable to delete all player rewards: " + uuid, e);
		}
	}

	@Override
	public void migrateRewards(@NotNull UUID from, @NotNull UUID to) throws IllegalStateException {
		try (PreparedStatement migrateUUID = this.prepareSQL("UPDATE `prefix_crate_claimed_rewards` SET `player` = UUID_TO_BIN(?) WHERE `player` = UUID_TO_BIN(?)")) {
			migrateUUID.setString(1, to.toString());
			migrateUUID.setString(2, from.toString());
			migrateUUID.executeUpdate();
			this.rewardsCountCache.put(to, this.rewardsCountCache.remove(from));
		} catch (SQLException e) {
			throw new IllegalStateException("Unable to migrate player rewards from " + from + " to " + to, e);
		}
	}

	@Override
	public int getOpenStats(@NotNull Crate crate) throws IllegalStateException {
		try (PreparedStatement countOpens = this.prepareSQL("SELECT COUNT(DISTINCT `open_id`) AS `total` FROM `prefix_crate_history` WHERE `crate` = ?")) {
			countOpens.setString(1, crate.getId());
			ResultSet result = countOpens.executeQuery();
			return result.next() ? result.getInt("total") : 0;
		} catch (SQLException e) {
			throw new IllegalStateException("Unable to count crate opens: " + crate.getId(), e);
		}
	}

	@Override
	public void updateStats(@NotNull List<@NotNull StatsRecord> stats) throws IllegalStateException {
		int openId = 0;
		try (PreparedStatement getLatestOpenId = this.prepareSQL("SELECT `open_id` FROM `prefix_crate_history` ORDER BY `open_id` DESC LIMIT 1")) {
			ResultSet result = getLatestOpenId.executeQuery();
			if (result.next())
				openId = result.getInt("open_id");
		} catch (SQLException e) {
			throw new IllegalStateException("Unable to get latest open ID from crates history", e);
		}
		try (PreparedStatement insertHistory = this.prepareSQL("INSERT INTO `prefix_crate_history` (`open_id`, `player`, `date`, `crate`, `reward`) VALUES (?, UUID_TO_BIN(?), ?, ?, ?)")) {
			for (StatsRecord stat : stats) {
				openId++;
				String player = stat.player().getUniqueId().toString();
				Timestamp date = Timestamp.from(stat.date().toInstant());
				String crate = stat.crate().getId();
				for (CrateReward reward : stat.rewards()) {
					insertHistory.setInt(1, openId);
					insertHistory.setString(2, player);
					insertHistory.setTimestamp(3, date);
					insertHistory.setString(4, crate);
					insertHistory.setString(5, reward.getId());
					insertHistory.addBatch();
				}
			}
			insertHistory.executeBatch();
		} catch (SQLException e) {
			throw new IllegalStateException("Unable to save crates history", e);
		}
	}

	@Override
	public @NotNull List<@NotNull HistoricalReward> listHistory(@NotNull OfflinePlayer player, @NotNull Date date) throws IllegalStateException {
		try (PreparedStatement listHistory = this.prepareSQL("SELECT `date`, `crate`, `reward` FROM `prefix_crate_history` WHERE `player` = UUID_TO_BIN(?) AND DATE(`date`) = ?")) {
			String uuid = player.getUniqueId().toString();
			listHistory.setString(1, uuid);
			listHistory.setDate(2, new java.sql.Date(date.getTime()));
			ResultSet result = listHistory.executeQuery();
			List<HistoricalReward> history = new ArrayList<>();
			while (result.next()) {
				try {
					Date datetime = Date.from(result.getTimestamp("date").toInstant());
					String crateId = result.getString("crate");
					String rewardId = result.getString("reward");
					Crate crate = this.crates().resolve(crateId);
					if (crate == null)
						throw new IllegalStateException("Unknown crate (" + crateId + ")");
					CrateReward reward = crate.getReward(rewardId);
					if (reward == null)
						throw new IllegalStateException("Unknown reward (" + rewardId + ", in crate " + crate.getId() + ")");
					history.add(new HistoricalReward(datetime, crate, reward));
				} catch (Throwable e) {
					this.warn("Could not parse history element (player: " + player.getName() + ", " + uuid + "): " + e.getMessage());
				}
			}
			return history;
		} catch (SQLException e) {
			throw new IllegalStateException("Unable to list player crates history", e);
		}
	}

	@Override
	public void migratePlayerStats(@NotNull UUID from, @NotNull UUID to) throws IllegalStateException {
		try (PreparedStatement migrateUUID = this.prepareSQL("UPDATE `prefix_crate_history` SET `player` = UUID_TO_BIN(?) WHERE `player` = UUID_TO_BIN(?)")) {
			migrateUUID.setString(1, to.toString());
			migrateUUID.setString(2, from.toString());
			migrateUUID.executeUpdate();
		} catch (SQLException e) {
			throw new IllegalStateException("Unable to migrate player stats from " + from + " to " + to, e);
		}
	}

	@Override
	public void clearPlayerStats(@NotNull UUID uuid) throws IllegalStateException {
		try (PreparedStatement removeUUID = this.prepareSQL("UPDATE `prefix_crate_history` SET `player` = NULL WHERE `player` = UUID_TO_BIN(?)")) {
			removeUUID.setString(1, uuid.toString());
			removeUUID.executeUpdate();
		} catch (SQLException e) {
			throw new IllegalStateException("Unable to delete all player stats: " + uuid, e);
		}
	}

	private @NotNull CrateReward deserializeReward(@NotNull JsonObject reward) throws JsonParseException {
		String type = reward.has("type") ? reward.get("type").getAsString() : "null";
		CrateRewardStorage<?> rewardStorage = this.storage().getRewardSource(type);
		if (rewardStorage == null)
			rewardStorage = this.storage().getRewardSource("unknown");
		if (!(rewardStorage instanceof CrateRewardJSON<?>))
			throw new JsonParseException("Could not parse crate item reward type: " + type);
		return rewardStorage.deserializeReward(reward);
	}

	private void serializeReward(@NotNull JsonObject data, @NotNull CrateReward reward) {
		CrateRewardStorage<?> rewardStorage = this.storage().getRewardSource(reward.getType());
		if (!(rewardStorage instanceof CrateRewardJSON<?>))
			throw new IllegalArgumentException("Could not save crate item reward class: " + reward.getClass().getName());
		rewardStorage.serializeReward(reward, data);
	}
}
