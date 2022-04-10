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
		return parent(this.plugin, this.path);
	}

	public boolean isRoot() {
		return this.path.isEmpty();
	}

	public boolean exists() {
		return this.isRoot() || !this.listContent().isEmpty();
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

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		CrateNamespace namespace = (CrateNamespace) o;
		return this.path.equals(namespace.path);
	}

	@Override
	public int hashCode() {
		return this.path.hashCode();
	}

	public static @NotNull CrateNamespace root(@NotNull CrateExpress plugin) {
		return new CrateNamespace(plugin, "");
	}

	public static @NotNull CrateNamespace ofCrate(@NotNull Crate crate) {
		return parent(crate.getPlugin(), crate.getId());
	}

	private static @NotNull CrateNamespace parent(@NotNull CrateExpress plugin, @NotNull String path) {
		int sep = path.lastIndexOf(SEPARATOR);
		return new CrateNamespace(plugin, sep == -1 ? "" : path.substring(0, sep));
	}
}
