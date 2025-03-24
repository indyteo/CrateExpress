package fr.theoszanto.mc.crateexpress.models;

import fr.theoszanto.mc.crateexpress.CrateExpress;
import fr.theoszanto.mc.crateexpress.events.CrateOpenEvent;
import fr.theoszanto.mc.crateexpress.models.gui.CratePreviewGUI;
import fr.theoszanto.mc.crateexpress.models.reward.CrateReward;
import fr.theoszanto.mc.crateexpress.utils.PluginObject;
import fr.theoszanto.mc.express.utils.ItemUtils;
import fr.theoszanto.mc.express.utils.LocationUtils;
import fr.theoszanto.mc.express.utils.MathUtils;
import fr.theoszanto.mc.express.utils.UnloadableWorldLocation;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnmodifiableView;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class Crate extends PluginObject implements Iterable<CrateReward>, CrateElement {
	private final @NotNull String id;
	private final @NotNull Map<@NotNull Integer, @NotNull CrateReward> rewards = new HashMap<>();
	private boolean disabled;
	private @Nullable CrateKey key;
	private @Nullable List<@NotNull UnloadableWorldLocation> locations;
	private double delay;
	private boolean noPreview;
	private @NotNull String name;
	private @Nullable String message;
	private @Nullable Sound sound;
	private @Nullable Particle particle;
	private int particleCount;
	private boolean random;
	private boolean allowDuplicates;
	private int min;
	private int max;

	@UnmodifiableView
	private final @NotNull Map<@NotNull Integer, @NotNull CrateReward> rewardsUnmodifiable = Collections.unmodifiableMap(this.rewards);
	@UnmodifiableView
	private final @NotNull Collection<@NotNull CrateReward> rewardsValuesUnmodifiable = Collections.unmodifiableCollection(this.rewards.values());

	public Crate(
			@NotNull CrateExpress plugin,
			@NotNull String id,
			boolean disabled,
			@Nullable CrateKey key,
			@Nullable List<@NotNull UnloadableWorldLocation> locations,
			double delay,
			boolean noPreview,
			@NotNull String name,
			@Nullable String message,
			@Nullable Sound sound,
			@Nullable Particle particle,
			int particleCount,
			boolean random,
			boolean allowDuplicates,
			int min,
			int max
	) {
		super(plugin);
		this.id = id;
		this.disabled = disabled;
		this.key = key;
		this.locations = locations;
		this.delay = delay;
		this.noPreview = noPreview;
		this.name = name;
		this.message = message;
		this.sound = sound;
		this.particle = particle;
		this.particleCount = particleCount;
		this.random = random;
		this.allowDuplicates = allowDuplicates;
		this.min = min;
		this.max = max;
	}

	public void open(@NotNull Player player, boolean recordStats) {
		List<CrateReward> rewards = new ArrayList<>();
		if (this.random) {
			if (!this.isEmpty()) {
				int rewardCount = MathUtils.random(this.min, this.max + 1);
				Collection<CrateReward> pool = this.allowDuplicates ? this.getRewards() : new HashSet<>(this.getRewards());
				for (int i = 0; i < rewardCount; i++)
					rewards.add(MathUtils.weightedRandom(pool, !this.allowDuplicates));
			}
		} else
			rewards.addAll(this.getRewards());
		if (this.event(new CrateOpenEvent(this, player, rewards))) {
			rewards.forEach(reward -> reward.giveRewardTo(player, true));
			if (recordStats) {
				Date date = new Date();
				this.stats().recordStats(new StatsRecord(date, player, this, rewards));
			}
		}
	}

	public void show(@NotNull Player player) {
		new CratePreviewGUI(this.plugin, this).showToPlayer(player);
	}

	public double totalWeight() {
		return MathUtils.totalWeight(this.getRewards());
	}

	public @NotNull String getId() {
		return this.id;
	}

	public @NotNull CrateNamespace getNamespace() {
		return this.crates().namespaces().ofCrate(this);
	}

	public boolean isEmpty() {
		return this.rewards.isEmpty();
	}

	@UnmodifiableView
	public @NotNull Map<@NotNull Integer, @NotNull CrateReward> getRewardsWithSlot() {
		return this.rewardsUnmodifiable;
	}

	@UnmodifiableView
	public @NotNull Collection<@NotNull CrateReward> getRewards() {
		return this.rewardsValuesUnmodifiable;
	}

	@Override
	public @NotNull Iterator<@NotNull CrateReward> iterator() {
		return this.getRewards().iterator();
	}

	public @Nullable CrateReward getReward(int slot) {
		return this.rewards.get(slot);
	}

	public @Nullable CrateReward getReward(@NotNull String id) {
		return this.getRewards().stream()
				.filter(reward -> reward.getId().equals(id))
				.findAny()
				.orElse(null);
	}

	public void addReward(int slot, @NotNull CrateReward reward) {
		this.rewards.put(slot, reward);
	}

	public void removeReward(int slot) {
		this.rewards.remove(slot);
	}

	public boolean isDisabled() {
		return this.disabled;
	}

	public void setDisabled(boolean disabled) {
		this.disabled = disabled;
	}

	public @Nullable CrateKey getKey() {
		return this.key;
	}

	public void setKey(@Nullable CrateKey key) {
		this.key = key;
	}

	public boolean hasKey(@NotNull ItemStack item) {
		return this.key != null && ItemUtils.basicItemEquals(this.key.getItem(), item);
	}

	public @Nullable List<@NotNull UnloadableWorldLocation> getLocations() {
		return this.locations;
	}

	public void setLocations(@Nullable List<@NotNull UnloadableWorldLocation> locations) {
		this.locations = locations;
	}

	public boolean isOpenableAnywhere() {
		return this.locations == null;
	}

	public boolean isAtLocation(@NotNull Location location) {
		return this.locations != null && this.locations.stream().anyMatch(loc -> LocationUtils.blockEquals(loc, location));
	}

	public boolean isOpenableAtLocation(@NotNull Location location) {
		return this.isOpenableAnywhere() || this.isAtLocation(location);
	}

	public double getDelay() {
		return this.delay;
	}

	public void setDelay(double delay) {
		this.delay = delay;
	}

	public long getDelayMillis() {
		return (long) (this.delay * 1000);
	}

	public boolean isNoPreview() {
		return this.noPreview;
	}

	public void setNoPreview(boolean noPreview) {
		this.noPreview = noPreview;
	}

	public @NotNull String getName() {
		return this.name;
	}

	public void setName(@NotNull String name) {
		this.name = name;
	}

	public @Nullable String getMessage() {
		return this.message;
	}

	public void setMessage(@Nullable String message) {
		this.message = message;
	}

	public @Nullable String getFormattedMessage(@NotNull Player player) {
		return this.message == null ? null : this.prefix() + this.message
				.replaceAll("<player>", player.getName())
				.replaceAll("<display>", ItemUtils.COMPONENT_SERIALIZER.serialize(player.displayName()))
				.replaceAll("<crate>", this.name)
				.replaceAll("<key>", this.key == null ? this.name : ItemUtils.name(this.key.getItem()));
	}

	public @Nullable Sound getSound() {
		return this.sound;
	}

	public void setSound(@Nullable Sound sound) {
		this.sound = sound;
	}

	public void playSoundAtLocation(@NotNull Location location) {
		if (this.sound != null) {
			World world = location.getWorld();
			if (world != null)
				world.playSound(location, this.sound, SoundCategory.BLOCKS, 1, 1);
		}
	}

	public @Nullable Particle getParticle() {
		return this.particle;
	}

	public void setParticle(@Nullable Particle particle) {
		this.particle = particle;
	}

	public int getParticleCount() {
		return this.particleCount;
	}

	public void setParticleCount(int particleCount) {
		this.particleCount = particleCount;
	}

	public void showParticleAtLocation(@NotNull Location location) {
		if (this.particle != null && this.particleCount > 0) {
			World world = location.getWorld();
			if (world != null)
				world.spawnParticle(this.particle, location.getX(), location.getY() + 0.5, location.getZ(), this.particleCount, 0.1, 0.1, 0.1, 0.5);
		}
	}

	public boolean isRandom() {
		return this.random;
	}

	public void setRandom(boolean random) {
		this.random = random;
	}

	public boolean doesAllowDuplicates() {
		return this.allowDuplicates;
	}

	public void setAllowDuplicates(boolean allowDuplicates) {
		this.allowDuplicates = allowDuplicates;
	}

	public int getMin() {
		return this.min;
	}

	public void setMin(int min) {
		this.min = min;
	}

	public int getMax() {
		return this.max;
	}

	public void setMax(int max) {
		this.max = max;
	}
}
