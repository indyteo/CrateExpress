package fr.theoszanto.mc.crateexpress.models.resolver;

import fr.theoszanto.mc.crateexpress.CrateExpress;
import fr.theoszanto.mc.crateexpress.models.Crate;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class CaseInsensitiveCrateResolver extends CrateResolver {
	public CaseInsensitiveCrateResolver(@NotNull CrateExpress plugin) {
		super(plugin);
	}

	@Override
	public @Nullable Crate resolve(@NotNull String name) {
		for (Crate crate : this.crates())
			if (crate.getId().equalsIgnoreCase(name))
				return crate;
		return null;
	}
}
