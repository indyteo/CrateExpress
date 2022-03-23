package fr.theoszanto.mc.crateexpress.models.resolver;

import fr.theoszanto.mc.crateexpress.CrateExpress;
import fr.theoszanto.mc.crateexpress.models.Crate;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class CrateResolverList extends CrateResolver {
	private final @NotNull List<@NotNull CrateResolver> resolvers = new ArrayList<>();

	public CrateResolverList(@NotNull CrateExpress plugin) {
		super(plugin);
	}

	public void addResolver(@NotNull CrateResolver resolver) {
		this.resolvers.add(resolver);
	}

	@Override
	public @Nullable Crate resolve(@NotNull String name) {
		for (CrateResolver resolver : this.resolvers) {
			Crate crate = resolver.resolve(name);
			if (crate != null)
				return crate;
		}
		return null;
	}
}
