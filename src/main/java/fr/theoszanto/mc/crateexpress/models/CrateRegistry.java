package fr.theoszanto.mc.crateexpress.models;

import fr.theoszanto.mc.crateexpress.CrateExpress;
import fr.theoszanto.mc.crateexpress.events.CrateLoadEvent;
import fr.theoszanto.mc.crateexpress.utils.LocationUtils;
import fr.theoszanto.mc.crateexpress.utils.Registry;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public class CrateRegistry extends Registry<String, Crate> {
	private int maximumPlayerRewards = -1;

	public CrateRegistry(@NotNull CrateExpress plugin) {
		super(plugin, "crate");
	}

	public void load(@NotNull ConfigurationSection config) {
		this.storage().loadCrates(this);
		for (Crate crate : this)
			this.event(new CrateLoadEvent(crate));
		this.maximumPlayerRewards = config.getInt("maximum-player-rewards", -1);
	}

	public boolean noLimitToPlayerRewards() {
		return this.maximumPlayerRewards < 0;
	}

	public int getMaximumPlayerRewards() {
		return this.maximumPlayerRewards;
	}

	public void addCrate(@NotNull Crate crate) {
		this.storage().saveCrate(crate);
		this.register(crate.getId(), crate);
	}

	public void deleteCrate(@NotNull String id) {
		this.storage().deleteCrate(id);
		this.delete(id);
	}

	public @NotNull Optional<@NotNull Crate> byLocation(@Nullable Location location) {
		return location == null ? Optional.empty() : this.list().stream().filter(crate -> {
			Location loc = crate.getLocation();
			return loc != null && LocationUtils.blockEquals(loc, location);
		}).findAny();
	}

	public @NotNull Optional<@NotNull Crate> byItem(@Nullable ItemStack item) {
		return item == null ? Optional.empty() : this.list().stream().filter(crate -> {
			CrateKey key = crate.getKey();
			return key != null && key.getItem().isSimilar(item);
		}).findAny();
	}
}
