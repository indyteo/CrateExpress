package fr.theoszanto.mc.crateexpress.models.resolver;

import fr.theoszanto.mc.crateexpress.CrateExpress;
import fr.theoszanto.mc.crateexpress.models.Crate;
import fr.theoszanto.mc.crateexpress.utils.PluginObject;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class CrateResolver extends PluginObject {
	public CrateResolver(@NotNull CrateExpress plugin) {
		super(plugin);
	}

	public abstract @Nullable Crate resolve(@NotNull String name);
}
