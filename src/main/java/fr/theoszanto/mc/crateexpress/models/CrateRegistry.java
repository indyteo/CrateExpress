package fr.theoszanto.mc.crateexpress.models;

import fr.theoszanto.mc.crateexpress.CrateExpress;
import fr.theoszanto.mc.crateexpress.events.CrateLoadEvent;
import fr.theoszanto.mc.crateexpress.resolvers.CrateResolver;
import fr.theoszanto.mc.crateexpress.resolvers.CrateResolversList;
import fr.theoszanto.mc.crateexpress.resolvers.NoopCrateResolver;
import fr.theoszanto.mc.crateexpress.resolvers.SimpleCrateResolver;
import fr.theoszanto.mc.express.utils.Registry;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;

public class CrateRegistry extends Registry<CrateExpress, String, Crate> {
	private final @NotNull CrateNamespaceRegistry namespaces;
	private int maximumPlayerRewards = -1;
	private int claimNoticeInterval = -1;
	private boolean claimNoticeOnLogin = false;
	private @NotNull CrateResolver resolver;

	public CrateRegistry(@NotNull CrateExpress plugin) {
		super(plugin, "crate");
		this.namespaces = new CrateNamespaceRegistry(plugin);
		this.resolver = new NoopCrateResolver(plugin);
	}

	public void load(@NotNull CrateConfig.Crates config) {
		this.namespaces.load();
		this.plugin.storage().getSource().loadCrates(this);
		for (Crate crate : this)
			this.event(new CrateLoadEvent(crate));
		this.maximumPlayerRewards = config.getMaximumPlayerRewards();
		this.claimNoticeInterval = config.getClaimNoticeInterval();
		if (this.claimNoticeInterval > 0) {
			long intervalInTicks = this.claimNoticeInterval * 1200L;
			this.plugin.getServer().getScheduler().runTaskTimer(this.plugin, () -> Bukkit.getOnlinePlayers().forEach(this::noticePlayerIfCanClaim), intervalInTicks, intervalInTicks);
		}
		this.claimNoticeOnLogin = config.isClaimNoticeOnLogin();
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
		this.namespaces.reset();
		this.maximumPlayerRewards = -1;
		this.claimNoticeInterval = -1;
		this.claimNoticeOnLogin = false;
		this.resolver = new NoopCrateResolver(this.plugin);
	}

	public @NotNull CrateNamespaceRegistry namespaces() {
		return this.namespaces;
	}

	public boolean noLimitToPlayerRewards() {
		return this.maximumPlayerRewards < 0;
	}

	public int getMaximumPlayerRewards() {
		return this.maximumPlayerRewards;
	}

	public boolean isClaimNoticeOnLogin() {
		return this.claimNoticeOnLogin;
	}

	public void noticePlayerIfCanClaim(@NotNull Player player) {
		int rewards = this.plugin.storage().getSource().countRewards(player);
		if (rewards > 0)
			this.i18nMessage(player, "crate.claim-notice", "count", rewards);
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
		this.async(() -> this.plugin.storage().getSource().saveCrate(crate));
		this.register(crate.getId(), crate);
		crate.getNamespace().elementAdded(crate);
	}

	public void deleteCrate(@NotNull Crate crate) {
		crate.getNamespace().elementRemoved(crate);
		this.async(() -> this.plugin.storage().getSource().deleteCrate(crate.getId()));
		this.delete(crate.getId());
	}

	public @NotNull List<@NotNull Crate> byLocation(@Nullable Location location) {
		return location == null ? Collections.emptyList() : this.list().stream()
				.filter(crate -> crate.isAtLocation(location))
				.toList();
	}

	public @NotNull List<@NotNull Crate> byItem(@Nullable ItemStack item) {
		return item == null ? Collections.emptyList() : this.list().stream()
				.filter(crate -> crate.hasKey(item))
				.toList();
	}
}
