package fr.theoszanto.mc.crateexpress.models;

import fr.theoszanto.mc.crateexpress.CrateExpress;
import fr.theoszanto.mc.crateexpress.utils.PluginObject;
import org.jetbrains.annotations.NotNull;

import java.util.SortedSet;
import java.util.TreeSet;

public class CrateNamespace extends PluginObject implements CrateElement {
	private final @NotNull String path;

	public static final char SEPARATOR = '/';

	public CrateNamespace(@NotNull CrateExpress plugin, @NotNull String path) {
		super(plugin);
		this.path = path;
	}

	public @NotNull String getPath() {
		return this.path;
	}

	public @NotNull String getName() {
		int sep = this.path.lastIndexOf(SEPARATOR);
		return sep == -1 ? this.path : this.path.substring(sep + 1);
	}

	public @NotNull CrateNamespace getParent() {
		if (this.isRoot())
			throw new IllegalStateException("Root namespace have no parent");
		int sep = this.path.lastIndexOf(SEPARATOR);
		return new CrateNamespace(this.plugin, sep == -1 ? "" : this.path.substring(0, sep));
	}

	public boolean isRoot() {
		return this.path.isEmpty();
	}

	public @NotNull SortedSet<@NotNull CrateElement> listContent() {
		SortedSet<CrateElement> content = new TreeSet<>();
		boolean isRoot = this.isRoot();
		for (Crate crate : this.crates()) {
			String id = crate.getId();
			if (isRoot || id.startsWith(this.path + SEPARATOR)) {
				int sep = id.indexOf(SEPARATOR, isRoot ? 0 : this.path.length() + 2);
				content.add(sep == -1 ? crate : new CrateNamespace(this.plugin, id.substring(0, sep)));
			}
		}
		return content;
	}

	public static @NotNull CrateNamespace root(@NotNull CrateExpress plugin) {
		return new CrateNamespace(plugin, "");
	}
}
