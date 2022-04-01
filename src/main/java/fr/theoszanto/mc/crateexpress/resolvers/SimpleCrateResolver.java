package fr.theoszanto.mc.crateexpress.resolvers;

import fr.theoszanto.mc.crateexpress.CrateExpress;
import fr.theoszanto.mc.crateexpress.models.Crate;
import fr.theoszanto.mc.crateexpress.utils.PluginObject;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class SimpleCrateResolver extends PluginObject implements CrateResolver {
	public SimpleCrateResolver(@NotNull CrateExpress plugin) {
		super(plugin);
	}

	@Override
	public @Nullable Crate resolve(@NotNull String name) {
		return this.crates().getRaw(name);
	}
}
