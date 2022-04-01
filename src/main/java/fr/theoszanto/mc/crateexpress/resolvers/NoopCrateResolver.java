package fr.theoszanto.mc.crateexpress.resolvers;

import fr.theoszanto.mc.crateexpress.CrateExpress;
import fr.theoszanto.mc.crateexpress.models.Crate;
import fr.theoszanto.mc.crateexpress.utils.PluginObject;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class NoopCrateResolver extends PluginObject implements CrateResolver {
	public NoopCrateResolver(@NotNull CrateExpress plugin) {
		super(plugin);
	}

	@Override
	public @Nullable Crate resolve(@NotNull String name) {
		return null;
	}
}
