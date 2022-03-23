package fr.theoszanto.mc.crateexpress.models.resolver;

import fr.theoszanto.mc.crateexpress.CrateExpress;
import fr.theoszanto.mc.crateexpress.models.Crate;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class UpperNamespaceCrateResolver extends CrateResolver {
	public UpperNamespaceCrateResolver(@NotNull CrateExpress plugin) {
		super(plugin);
	}

	@Override
	public @Nullable Crate resolve(@NotNull String name) {
		int slash = name.indexOf('/');
		if (slash == -1)
			return null;
		return this.crates().resolve(name.substring(slash + 1));
	}
}
