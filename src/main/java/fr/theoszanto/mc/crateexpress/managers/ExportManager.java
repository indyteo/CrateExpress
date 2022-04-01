package fr.theoszanto.mc.crateexpress.managers;

import fr.theoszanto.mc.crateexpress.CrateExpress;
import fr.theoszanto.mc.crateexpress.exporters.CrateExporter;
import fr.theoszanto.mc.crateexpress.models.Crate;
import fr.theoszanto.mc.crateexpress.utils.Registry;
import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;

public class ExportManager extends Registry<String, CrateExporter> {
	private @NotNull File exportDir;

	public ExportManager(@NotNull CrateExpress plugin) {
		super(plugin, "exporters");
		this.exportDir = this.defaultDir();
	}

	@Override
	public void reset() {
		super.reset();
		this.exportDir = this.defaultDir();
	}

	public void loadExporters(@NotNull ConfigurationSection config) throws IllegalStateException {
		this.exportDir = new File(this.plugin.getDataFolder(), config.getString("directory", "exports"));
		ConfigurationSection exporters = config.getConfigurationSection("exporters");
		if (exporters != null) {
			for (String exporterKey : exporters.getKeys(false)) {
				ConfigurationSection exporterConfig = exporters.getConfigurationSection(exporterKey);
				if (exporterConfig != null) {
					String exporterClassName = exporterConfig.getString("class", null);
					if (exporterClassName == null)
						throw new IllegalStateException("Missing exporter class name in config: " + exporterConfig.getCurrentPath());
					try {
						CrateExporter exporter = (CrateExporter) this.instanciate(exporterClassName, exporterConfig.getList("options"));
						this.register(exporter.getFormat(), exporter);
					} catch (IllegalArgumentException | ClassCastException e) {
						throw new IllegalStateException("Invalid exporter class: " + exporterClassName, e);
					}
				}
			}
		}
	}

	public @Nullable File toFormat(@NotNull Crate crate, @NotNull String format) {
		File file = new File(this.exportDir, crate.getId() + "." + format);
		return this.export(crate, format, file);
	}

	public @Nullable File toFile(@NotNull Crate crate, @NotNull String filename) {
		int dot = filename.lastIndexOf('.');
		if (dot == -1)
			return null;
		String format = filename.substring(dot + 1);
		File file = new File(this.exportDir, filename);
		return this.export(crate, format, file);
	}

	private @Nullable File export(@NotNull Crate crate, @NotNull String format, @NotNull File file) {
		try {
			CrateExporter exporter = this.get(format);
			File dir = file.getParentFile();
			if (!dir.exists() && !dir.mkdirs())
				return null;
			if (!file.exists() && !file.createNewFile())
				return null;
			try (Writer writer = new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8)) {
				exporter.export(crate, writer);
			}
			return file;
		} catch (IllegalArgumentException | IOException e) {
			return null;
		}
	}

	private @NotNull File defaultDir() {
		return new File(this.plugin.getDataFolder(), "exports");
	}
}
