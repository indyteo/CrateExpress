package fr.theoszanto.mc.crateexpress.storage;

import fr.theoszanto.mc.crateexpress.CrateExpress;
import fr.theoszanto.mc.crateexpress.models.Crate;
import fr.theoszanto.mc.crateexpress.models.CrateRegistry;
import fr.theoszanto.mc.express.utils.LocationUtils;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.stream.Collectors;

public class YamlCrateStorageWithExternalLocations extends YamlCrateStorage {
	private final @NotNull File locationsDir;

	public YamlCrateStorageWithExternalLocations(@NotNull CrateExpress plugin, @NotNull String cratesDir, @NotNull String rewardsDir, @NotNull String statsDir, @NotNull String locationsDir) throws IllegalArgumentException {
		this(plugin, cratesDir, rewardsDir, statsDir, locationsDir, new ArrayList<>());
	}

	public YamlCrateStorageWithExternalLocations(@NotNull CrateExpress plugin, @NotNull String cratesDir, @NotNull String rewardsDir, @NotNull String statsDir, @NotNull String locationsDir, @NotNull ArrayList<@NotNull String> ignoreFiles) throws IllegalArgumentException {
		super(plugin, cratesDir, rewardsDir, statsDir, ignoreFiles);
		this.locationsDir = this.initDir(locationsDir, "locations");
	}

	@Override
	public void loadCrates(@NotNull CrateRegistry registry) throws IllegalStateException {
		super.loadCrates(registry);
		for (Crate crate : this.crates()) {
			String id = crate.getId();
			try {
				File file = new File(this.locationsDir, fileNameFromCrateId(id));
				YamlConfiguration data = new YamlConfiguration();
				data.load(file);
				crate.setLocations(data.getStringList("locations").stream()
						.map(LocationUtils::fromString)
						.collect(Collectors.toCollection(ArrayList::new)));
			} catch (FileNotFoundException e) {
				if (crate.getLocations() != null)
					crate.getLocations().clear();
			} catch (IOException | InvalidConfigurationException | IllegalArgumentException e) {
				this.error("Could not load crate locations: " + id, e);
			}
		}
	}

	@Override
	public void saveCrate(@NotNull Crate crate) throws IllegalStateException {
		super.saveCrate(crate);
		String id = crate.getId();
		try {
			File file = new File(this.locationsDir, fileNameFromCrateId(id));
			if (crate.getLocations() == null)
				this.deleteLocationsFile(file);
			else {
				YamlConfiguration data = new YamlConfiguration();
				data.set("locations", crate.getLocations().stream()
						.map(LocationUtils::toString)
						.collect(Collectors.toList()));
				data.save(file);
			}
		} catch (IOException e) {
			throw new IllegalStateException("Could not save crate locations: " + id, e);
		}
	}

	@Override
	public void deleteCrate(@NotNull String id) throws IllegalStateException {
		super.deleteCrate(id);
		this.deleteLocationsFile(new File(this.locationsDir, fileNameFromCrateId(id)));
	}

	private void deleteLocationsFile(@NotNull File file) {
		if (file.exists() && !file.delete())
			throw new IllegalStateException("Could not delete crate locations data file: " + file);
		File parent = file.getParentFile();
		while (!this.locationsDir.equals(parent)) {
			if (parent.exists()) {
				String[] list = parent.list();
				if ((list == null || list.length == 0) && !parent.delete())
					throw new IllegalStateException("Could not cleanup crate locations data parent dir: " + parent);
			}
			parent = parent.getParentFile();
		}
	}
}
