package fr.theoszanto.mc.crateexpress.models;

import fr.theoszanto.mc.crateexpress.CrateExpress;
import fr.theoszanto.mc.crateexpress.events.CrateLoadEvent;
import fr.theoszanto.mc.crateexpress.resolvers.CrateResolver;
import fr.theoszanto.mc.crateexpress.resolvers.CrateResolversList;
import fr.theoszanto.mc.crateexpress.resolvers.NoopCrateResolver;
import fr.theoszanto.mc.crateexpress.resolvers.SimpleCrateResolver;
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
	private @NotNull CrateResolver resolver;

	public CrateRegistry(@NotNull CrateExpress plugin) {
		super(plugin, "crate");
		this.resolver = new NoopCrateResolver(plugin);
	}

	public void load(@NotNull ConfigurationSection config) {
		this.storage().loadCrates(this);
		for (Crate crate : this)
			this.event(new CrateLoadEvent(crate));
		this.maximumPlayerRewards = config.getInt("maximum-player-rewards", -1);
		ConfigurationSection resolvers = config.getConfigurationSection("resolvers");
		if (resolvers == null)
			this.resolver = new SimpleCrateResolver(this.plugin);
		else {
			CrateResolversList resolversList = new CrateResolversList(this.plugin);
			for (String resolverKey : resolvers.getKeys(false)) {
				ConfigurationSection resolverConfig = resolvers.getConfigurationSection(resolverKey);
				if (resolverConfig != null) {
					String resolverClassName = resolverConfig.getString("class", null);
					if (resolverClassName == null)
						throw new IllegalStateException("Missing resolver class name in config: " + resolverConfig.getCurrentPath());
					try {
						resolversList.addResolver((CrateResolver) this.instanciate(resolverClassName, resolverConfig.getList("options")));
					} catch (IllegalArgumentException | ClassCastException e) {
						throw new IllegalStateException("Invalid resolver class: " + resolverClassName, e);
					}
				}
			}
			this.resolver = resolversList;
		}
	}

	@Override
	public void reset() {
		super.reset();
		this.resolver = new NoopCrateResolver(this.plugin);
	}

	public boolean noLimitToPlayerRewards() {
		return this.maximumPlayerRewards < 0;
	}

	public int getMaximumPlayerRewards() {
		return this.maximumPlayerRewards;
	}

	public @Nullable Crate resolve(@NotNull String name) {
		return this.resolver.resolve(name);
	}

	@Override
	public @NotNull Crate get(@NotNull String name) throws IllegalArgumentException {
		Crate crate = this.resolve(name);
		if (crate == null)
			throw new IllegalArgumentException("Failed to resolve crate with name: " + name);
		return crate;
	}

	@Override
	public @Nullable Crate getRaw(@NotNull String key) {
		return super.getRaw(key);
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
