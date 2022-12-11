package fr.theoszanto.mc.crateexpress.models;

import fr.theoszanto.mc.crateexpress.CrateExpress;
import fr.theoszanto.mc.crateexpress.events.CrateConfigLoadEvent;
import fr.theoszanto.mc.crateexpress.managers.MoneyManager;
import fr.theoszanto.mc.crateexpress.models.gui.reward.CrateRewardGUI;
import fr.theoszanto.mc.crateexpress.utils.PluginObject;
import org.bukkit.Material;
import org.bukkit.configuration.Configuration;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class CrateConfig extends PluginObject {
	private @NotNull Configuration config = new YamlConfiguration();

	public CrateConfig(@NotNull CrateExpress plugin) {
		super(plugin);
	}

	public @NotNull Configuration getRawConfig() {
		return this.config;
	}

	public void setRawConfig(@NotNull Configuration rawConfig) {
		this.config = rawConfig;
		this.event(new CrateConfigLoadEvent(this));
	}

	public void expandRawConfig(@NotNull ConfigurationSection extraConfig) {
		for (String key : extraConfig.getKeys(true)) {
			Object value = extraConfig.get(key);
			if (!(value instanceof ConfigurationSection))
				this.config.set(key, value);
		}
	}

	public @NotNull String getLocale() {
		return this.config.getString("locale", "en");
	}

	public void setLocale(@NotNull String locale) {
		this.config.set("locale", locale);
	}

	public @NotNull List<@NotNull File> getAdditionalMessageFiles() {
		return this.config.getStringList("additional-message-files").stream().map(File::new).collect(Collectors.toList());
	}

	public void setAdditionalMessageFiles(@NotNull List<@NotNull File> additionalMessageFiles) {
		this.config.set("additional-message-files", additionalMessageFiles.stream().map(File::getPath).collect(Collectors.toList()));
	}

	public @NotNull Storage getStorageConfig() {
		return new Storage(this);
	}

	public @NotNull Money getMoneyConfig() {
		return new Money(this);
	}

	public @NotNull Crates getCratesConfig() {
		return new Crates(this);
	}

	public @NotNull Rewards getRewardsConfig() {
		return new Rewards(this);
	}

	public @NotNull Export getExportConfig() {
		return new Export(this);
	}

	private @NotNull ConfigurationSection getSection(@NotNull String path) {
		ConfigurationSection section = this.config.getConfigurationSection(path);
		if (section == null)
			return this.config.createSection(path);
		return section;
	}

	private static abstract class Section extends PluginObject {
		protected final @NotNull ConfigurationSection section;

		public Section(@NotNull CrateConfig config, @NotNull String name) {
			super(config.getPlugin());
			this.section = config.getSection(name);
		}

		public boolean isEmpty() {
			return this.section.getKeys(false).isEmpty();
		}

		@Contract(value = "_ -> new", pure = true)
		protected final @NotNull SerializedPluginObject getSerializedPluginObject(@NotNull String path) {
			return this.getSerializedPluginObject(this.section, path);
		}

		@Contract(value = "_, _ -> new", pure = true)
		protected final @NotNull SerializedPluginObject getSerializedPluginObject(@NotNull ConfigurationSection config, @NotNull String path) {
			ConfigurationSection serializedPluginObjectConfig = config.getConfigurationSection(path);
			if (serializedPluginObjectConfig == null)
				throw new IllegalStateException();
			String className = serializedPluginObjectConfig.getString("class", null);
			if (className == null)
				throw new IllegalStateException("Missing class name in config: " + serializedPluginObjectConfig.getCurrentPath());
			return new SerializedPluginObject(this.plugin, className, serializedPluginObjectConfig.getList("options"));
		}

		@Contract(value = "_ -> new", pure = true)
		protected final @NotNull List<@NotNull SerializedPluginObject> getSerializedPluginObjects(@NotNull String path) {
			return this.getSerializedPluginObjects(this.section, path);
		}

		@Contract(value = "_, _ -> new", pure = true)
		protected final @NotNull List<@NotNull SerializedPluginObject> getSerializedPluginObjects(@NotNull ConfigurationSection config, @NotNull String path) {
			List<SerializedPluginObject> serializedPluginObjects = new ArrayList<>();
			ConfigurationSection serializedPluginObjectsConfig = config.getConfigurationSection(path);
			if (serializedPluginObjectsConfig != null)
				for (String key : serializedPluginObjectsConfig.getKeys(false))
					serializedPluginObjects.add(this.getSerializedPluginObject(serializedPluginObjectsConfig, key));
			return serializedPluginObjects;
		}

		protected final void setSerializedPluginObject(@NotNull String path, @NotNull SerializedPluginObject serializedPluginObject) {
			this.setSerializedPluginObject(this.section, path, serializedPluginObject);
		}

		protected final void setSerializedPluginObject(@NotNull ConfigurationSection config, @NotNull String path, @NotNull SerializedPluginObject serializedPluginObject) {
			ConfigurationSection serializedPluginObjectConfig = config.createSection(path);
			serializedPluginObjectConfig.set("class", serializedPluginObject.getClassName());
			serializedPluginObjectConfig.set("options", serializedPluginObject.getOptions());
		}

		protected final void setSerializedPluginObjects(@NotNull String path, @NotNull List<@NotNull SerializedPluginObject> serializedPluginObjects) {
			this.setSerializedPluginObjects(this.section, path, serializedPluginObjects);
		}

		protected final void setSerializedPluginObjects(@NotNull ConfigurationSection config, @NotNull String path, @NotNull List<@NotNull SerializedPluginObject> serializedPluginObjects) {
			ConfigurationSection serializedPluginObjectsConfig = config.createSection(path);
			for (int i = 0; i < serializedPluginObjects.size(); i++)
				this.setSerializedPluginObject(serializedPluginObjectsConfig, Integer.toString(i), serializedPluginObjects.get(i));
		}
	}

	public static class Storage extends Section {
		public Storage(@NotNull CrateConfig config) {
			super(config, "storage");
		}

		public @NotNull SerializedPluginObject getSource() {
			return this.getSerializedPluginObject("source");
		}

		public void setSource(@NotNull SerializedPluginObject source) {
			this.setSerializedPluginObject("source", source);
		}

		public @NotNull List<@NotNull SerializedPluginObject> getRewards() {
			return this.getSerializedPluginObjects("rewards");
		}

		public void setRewards(@NotNull List<@NotNull SerializedPluginObject> rewards) {
			this.setSerializedPluginObjects("rewards", rewards);
		}
	}

	public static class Money extends Section {
		public Money(@NotNull CrateConfig config) {
			super(config, "money");
		}

		public @NotNull MoneyManager.GiveType getGiveType() {
			String giveType = this.section.getString("give-type", null);
			if (giveType == null)
				throw new IllegalStateException("Missing money give type in config");
			try {
				return MoneyManager.GiveType.valueOf(giveType.toUpperCase());
			} catch (IllegalArgumentException e) {
				return MoneyManager.GiveType.NONE;
			}
		}

		public void setGiveType(@NotNull MoneyManager.GiveType giveType) {
			this.section.set("give-type", giveType.name());
		}

		public @NotNull String getGiveCommand() {
			String giveCommand = this.section.getString("give-command", null);
			if (giveCommand == null)
				throw new IllegalStateException("Missing money give command in config");
			return giveCommand;
		}

		public void setGiveCommand(@NotNull String giveCommand) {
			this.section.set("give-command", giveCommand);
		}

		public @NotNull Material getItem() {
			String item = this.section.getString("item", null);
			if (item == null)
				throw new IllegalStateException("Missing money icon item in config");
			try {
				return Material.valueOf(item.toUpperCase());
			} catch (IllegalArgumentException e) {
				throw new IllegalStateException("Invalid money icon item in config: " + item);
			}
		}

		public void setItem(@NotNull Material item) {
			this.section.set("item", item.name());
		}

		public @NotNull String getCurrencySymbol() {
			return this.section.getString("currency-symbol", "");
		}

		public void setCurrencySymbol(@NotNull String currencySymbol) {
			this.section.set("currency-symbol", currencySymbol);
		}

		public boolean isPlacementBefore() {
			String placement = this.section.getString("placement", "before");
			if (!placement.equalsIgnoreCase("before") && !placement.equalsIgnoreCase("after"))
				this.warn("Unrecognized currency symbol placement value for money manager: " + placement + ". Should be either \"before\" or \"after\"");
			return placement.equals("before");
		}

		public void setPlacement(@NotNull String placement) {
			this.section.set("placement", placement);
		}

		public boolean isPhysical() {
			return this.section.getBoolean("physical", false);
		}

		public void setPhysical(boolean physical) {
			this.section.set("physical", physical);
		}
	}

	public static class Crates extends Section {
		public static final int UNLIMITED_PLAYER_REWARDS = -1;
		public static final int NO_CLAIM_NOTICE = -1;

		public Crates(@NotNull CrateConfig config) {
			super(config, "crates");
		}

		public int getMaximumPlayerRewards() {
			return this.section.getInt("maximum-player-rewards", UNLIMITED_PLAYER_REWARDS);
		}

		public void setMaximumPlayerRewards(int maximumPlayerRewards) {
			this.section.set("maximum-player-rewards", maximumPlayerRewards);
		}

		public int getClaimNoticeInterval() {
			return this.section.getInt("claim-notice-interval", NO_CLAIM_NOTICE);
		}

		public void setClaimNoticeInterval(int claimNoticeInterval) {
			this.section.set("claim-notice-interval", claimNoticeInterval);
		}

		public boolean isClaimNoticeOnLogin() {
			return this.section.getBoolean("claim-notice-on-login", false);
		}

		public void setClaimNoticeOnLogin(boolean claimNoticeOnLogin) {
			this.section.set("claim-notice-on-login", claimNoticeOnLogin);
		}

		@Contract(value = " -> new", pure = true)
		public @NotNull List<@NotNull SerializedPluginObject> getResolvers() {
			return this.getSerializedPluginObjects("resolvers");
		}

		public void setResolvers(@NotNull List<@NotNull SerializedPluginObject> resolvers) {
			this.setSerializedPluginObjects("resolvers", resolvers);
		}
	}

	public static class Rewards extends Section {
		public Rewards(@NotNull CrateConfig config) {
			super(config, "rewards");
		}

		@Contract(value = " -> new", pure = true)
		public @NotNull Map<@NotNull String, @NotNull RewardGUI> getRewardsGUI() {
			Map<String, RewardGUI> rewardsGUI = new HashMap<>();
			for (String type : this.section.getKeys(false)) {
				ConfigurationSection guiConfig = this.section.getConfigurationSection(type);
				if (guiConfig == null)
					throw new IllegalStateException();
				String guiClass = guiConfig.getString("gui-class");
				if (guiClass == null)
					throw new IllegalStateException("Missing GUI class in reward type: " + type);
				String guiIcon = guiConfig.getString("gui-icon");
				if (guiIcon == null)
					throw new IllegalStateException("Missing GUI icon in reward type: " + type);
				rewardsGUI.put(type, new RewardGUI(guiClass, guiIcon));
			}
			return rewardsGUI;
		}

		public void setRewardsGUI(@NotNull Map<@NotNull String, @NotNull RewardGUI> rewards) {
			rewards.forEach((type, gui) -> {
				ConfigurationSection guiConfig = this.section.createSection(type);
				guiConfig.set("gui-class", gui.getGuiClass());
				guiConfig.set("gui-icon", gui.getGuiIcon());
			});
		}

		public static class RewardGUI {
			private final @NotNull String guiClass;
			private final @NotNull String guiIcon;

			public RewardGUI(@NotNull String guiClass, @NotNull String guiIcon) {
				this.guiClass = guiClass;
				this.guiIcon = guiIcon;
			}

			public RewardGUI(@NotNull Class<? extends CrateRewardGUI<?>> guiClass, @NotNull Material guiIcon) {
				this(guiClass.getName(), guiIcon.name());
			}

			public @NotNull String getGuiClass() {
				return this.guiClass;
			}

			public @NotNull String getGuiIcon() {
				return this.guiIcon;
			}
		}
	}

	public static class Export extends Section {
		public Export(@NotNull CrateConfig config) {
			super(config, "export");
		}

		public @NotNull String getDirectory() {
			return this.section.getString("directory", "exports");
		}

		public void setDirectory(@NotNull String directory) {
			this.section.set("directory", directory);
		}

		@Contract(value = " -> new", pure = true)
		public @NotNull List<@NotNull SerializedPluginObject> getExporters() {
			return this.getSerializedPluginObjects("exporters");
		}

		public void setExporters(@NotNull List<@NotNull SerializedPluginObject> exporters) {
			this.setSerializedPluginObjects("exporters", exporters);
		}
	}

	public static class SerializedPluginObject extends PluginObject {
		private final @NotNull String className;
		private final @Nullable List<?> options;

		public SerializedPluginObject(@NotNull CrateExpress plugin, @NotNull String className) {
			this(plugin, className, null);
		}

		public SerializedPluginObject(@NotNull CrateExpress plugin, @NotNull String className, @Nullable List<?> options) {
			super(plugin);
			this.className = className;
			this.options = options;
		}

		public SerializedPluginObject(@NotNull CrateExpress plugin, @NotNull Class<? extends PluginObject> clazz) {
			this(plugin, clazz, null);
		}

		public SerializedPluginObject(@NotNull CrateExpress plugin, @NotNull Class<? extends PluginObject> clazz, @Nullable List<?> options) {
			this(plugin, clazz.getName(), options);
		}

		public @NotNull String getClassName() {
			return this.className;
		}

		public @Nullable List<?> getOptions() {
			return this.options;
		}

		@SuppressWarnings("unchecked")
		public <T extends PluginObject> @NotNull T instanciate() throws IllegalStateException {
			try {
				return (T) this.instanciate(this.className, this.options);
			} catch (IllegalArgumentException | ClassCastException e) {
				throw new IllegalStateException("Invalid plugin object class: " + this.className, e);
			}
		}
	}
}
