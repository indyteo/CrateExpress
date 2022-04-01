package fr.theoszanto.mc.crateexpress.exporters;

import fr.theoszanto.mc.crateexpress.CrateExpress;
import fr.theoszanto.mc.crateexpress.models.Crate;
import fr.theoszanto.mc.crateexpress.utils.PluginObject;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.Writer;

public abstract class CrateExporter extends PluginObject {
	private final @NotNull String format;

	public CrateExporter(@NotNull CrateExpress plugin, @NotNull String format) {
		super(plugin);
		this.format = format;
	}

	public abstract void export(@NotNull Crate crate, @NotNull Writer writer) throws IOException;

	public @NotNull String getFormat() {
		return this.format;
	}
}
