package fr.theoszanto.mc.crateexpress.resolvers;

import fr.theoszanto.mc.crateexpress.CrateExpress;
import fr.theoszanto.mc.crateexpress.models.Crate;
import fr.theoszanto.mc.crateexpress.utils.PluginObject;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

public class BindingsCrateResolver extends PluginObject implements CrateResolver {
	private final @NotNull Map<@NotNull String, @NotNull String> bindings;

	public BindingsCrateResolver(@NotNull CrateExpress plugin) {
		this(plugin, Map.of());
	}

	public BindingsCrateResolver(@NotNull CrateExpress plugin, @NotNull Map<@NotNull String, @NotNull String> bindings) {
		super(plugin);
		this.bindings = bindings;
	}

	@Override
	public @Nullable Crate resolve(@NotNull String name) {
		if (this.bindings.containsKey(name))
			return this.crates().resolve(this.bindings.get(name));
		return null;
	}
}
