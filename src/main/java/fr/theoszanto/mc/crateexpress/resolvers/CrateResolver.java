package fr.theoszanto.mc.crateexpress.resolvers;

import fr.theoszanto.mc.crateexpress.models.Crate;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface CrateResolver {
	@Nullable Crate resolve(@NotNull String name);
}
