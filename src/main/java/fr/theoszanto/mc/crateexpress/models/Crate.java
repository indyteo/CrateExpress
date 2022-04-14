package fr.theoszanto.mc.crateexpress.models;

import fr.theoszanto.mc.crateexpress.CrateExpress;
import fr.theoszanto.mc.crateexpress.events.CrateOpenEvent;
import fr.theoszanto.mc.crateexpress.models.gui.CratePreviewGUI;
import fr.theoszanto.mc.crateexpress.models.reward.CrateReward;
import fr.theoszanto.mc.crateexpress.utils.MathUtils;
import fr.theoszanto.mc.crateexpress.utils.PluginObject;
import fr.theoszanto.mc.crateexpress.utils.UnloadableWorldLocation;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnmodifiableView;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class Crate extends PluginObject implements Iterable<CrateReward>, CrateElement {
	private final @NotNull String id;
	private final @NotNull Map<@NotNull Integer, @NotNull CrateReward> rewards = new HashMap<>();
	private int min;
	private int max;
	private @Nullable CrateKey key;
	private @NotNull String name;
	private @Nullable String message;
	private @Nullable UnloadableWorldLocation location;

	@UnmodifiableView
	private final @NotNull Map<@NotNull Integer, @NotNull CrateReward> rewardsUnmodifiable = Collections.unmodifiableMap(this.rewards);
	@UnmodifiableView
	private final @NotNull Collection<@NotNull CrateReward> rewardsValuesUnmodifiable = Collections.unmodifiableCollection(this.rewards.values());

	public Crate(@NotNull CrateExpress plugin, @NotNull String id, int min, int max, @Nullable CrateKey key, @NotNull String name, @Nullable String message, @Nullable UnloadableWorldLocation location) {
		super(plugin);
		this.id = id;
		this.min = min;
		this.max = max;
		this.key = key;
		this.name = name;
		this.message = message;
		this.location = location;
	}

	public void open(@NotNull Player player) {
		List<CrateReward> rewards = new ArrayList<>();
		if (!this.isEmpty()) {
			int rewardCount = MathUtils.random(this.min, this.max + 1);
			for (int i = 0; i < rewardCount; i++)
				rewards.add(this.randomReward());
		}
		if (this.event(new CrateOpenEvent(this, player, rewards)))
			rewards.forEach(reward -> reward.giveRewardTo(player));
	}

	public void show(@NotNull Player player) {
		new CratePreviewGUI(this.plugin, this).showToPlayer(player);
	}

	public int totalWeight() {
		return MathUtils.totalWeight(this.getRewards());
	}

	public @NotNull CrateReward randomReward() {
		return MathUtils.weightedRandom(this.getRewards());
	}

	public @NotNull String getId() {
		return this.id;
	}

	public @NotNull CrateNamespace getNamespace() {
		return CrateNamespace.ofCrate(this);
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

	public void addReward(int slot, @NotNull CrateReward reward) {
		this.rewards.put(slot, reward);
	}

	public void removeReward(int slot) {
		this.rewards.remove(slot);
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

	public @Nullable CrateKey getKey() {
		return this.key;
	}

	public void setKey(@Nullable CrateKey key) {
		this.key = key;
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
		return this.message == null ? null : this.message
				.replaceAll("<player>", player.getName())
				.replaceAll("<display>", player.getDisplayName());
	}

	public @Nullable UnloadableWorldLocation getLocation() {
		return this.location;
	}

	public void setLocation(@Nullable UnloadableWorldLocation location) {
		this.location = location;
	}
}
