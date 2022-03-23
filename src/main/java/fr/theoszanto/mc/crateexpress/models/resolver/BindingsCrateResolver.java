package fr.theoszanto.mc.crateexpress.models.resolver;

import fr.theoszanto.mc.crateexpress.CrateExpress;
import fr.theoszanto.mc.crateexpress.models.Crate;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.Map;

public class BindingsCrateResolver extends CrateResolver {
	private final @NotNull Map<@NotNull String, @NotNull String> bindings;

	public BindingsCrateResolver(@NotNull CrateExpress plugin) {
		this(plugin, Collections.emptyMap());
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
