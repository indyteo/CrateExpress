package fr.theoszanto.mc.crateexpress.models;

import fr.theoszanto.mc.crateexpress.CrateExpress;
import fr.theoszanto.mc.crateexpress.events.CrateLoadEvent;
import fr.theoszanto.mc.crateexpress.resolvers.CrateResolver;
import fr.theoszanto.mc.crateexpress.resolvers.CrateResolversList;
import fr.theoszanto.mc.crateexpress.resolvers.NoopCrateResolver;
import fr.theoszanto.mc.crateexpress.resolvers.SimpleCrateResolver;
import fr.theoszanto.mc.express.utils.ItemUtils;
import fr.theoszanto.mc.express.utils.LocationUtils;
import fr.theoszanto.mc.express.utils.Registry;
import org.bukkit.Location;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;

public class CrateRegistry extends Registry<CrateExpress, String, Crate> {
	private int maximumPlayerRewards = -1;
	private @NotNull CrateResolver resolver;

	public CrateRegistry(@NotNull CrateExpress plugin) {
		super(plugin, "crate");
		this.resolver = new NoopCrateResolver(plugin);
	}

	public void load(@NotNull CrateConfig.Crates config) {
		this.plugin.storage().getSource().loadCrates(this);
		for (Crate crate : this)
			this.event(new CrateLoadEvent(crate));
		this.maximumPlayerRewards = config.getMaximumPlayerRewards();
		List<CrateConfig.SerializedPluginObject> resolversConfig = config.getResolvers();
		if (resolversConfig.isEmpty())
			this.resolver = new SimpleCrateResolver(this.plugin);
		else {
			CrateResolversList resolversList = new CrateResolversList(this.plugin);
			for (CrateConfig.SerializedPluginObject resolverConfig : resolversConfig)
				resolversList.addResolver(resolverConfig.instanciate());
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
		this.plugin.storage().getSource().saveCrate(crate);
		this.register(crate.getId(), crate);
	}

	public void deleteCrate(@NotNull String id) {
		this.plugin.storage().getSource().deleteCrate(id);
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
			return key != null && ItemUtils.basicItemEquals(key.getItem(), item);
		}).findAny();
	}
}
