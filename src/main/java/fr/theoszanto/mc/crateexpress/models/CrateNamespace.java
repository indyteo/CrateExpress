package fr.theoszanto.mc.crateexpress.models;

import fr.theoszanto.mc.crateexpress.CrateExpress;
import fr.theoszanto.mc.crateexpress.utils.PluginObject;
import org.bukkit.DyeColor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Iterator;
import java.util.SortedSet;
import java.util.TreeSet;

public class CrateNamespace extends PluginObject implements CrateElement, Iterable<@NotNull CrateElement> {
	private final @NotNull String path;
	private final @NotNull SortedSet<@NotNull CrateElement> content = new TreeSet<>(); // Lazy initialization
	private boolean contentComputed = false;
	private @Nullable DyeColor color;
	private boolean autoCreated;

	public static final char SEPARATOR = '/';

	/* package-private */ CrateNamespace(@NotNull CrateExpress plugin, @NotNull String path) {
		super(plugin);
		this.path = path;
		this.color = null;
		this.autoCreated = true;
	}

	public CrateNamespace(@NotNull CrateExpress plugin, @NotNull String path, @Nullable DyeColor color) {
		super(plugin);
		this.path = path;
		this.color = color;
		this.autoCreated = false;
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
		return this.crates().namespaces().parent(this.path);
	}

	public boolean isRoot() {
		return this.path.isEmpty();
	}

	public @NotNull SortedSet<@NotNull CrateElement> listContent() {
		if (this.contentComputed)
			return this.content;
		this.contentComputed = true;
		boolean isRoot = this.isRoot();
		for (Crate crate : this.crates()) {
			String id = crate.getId();
			if (isRoot || id.startsWith(this.path + SEPARATOR)) {
				int sep = id.indexOf(SEPARATOR, isRoot ? 0 : this.path.length() + 2);
				this.content.add(sep == -1 ? crate : this.crates().namespaces().getOrAutoCreate(id.substring(0, sep)));
			}
		}
		return this.content;
	}

	public @Nullable DyeColor getColor() {
		return this.color;
	}

	public void setColor(@Nullable DyeColor color) {
		this.color = color;
		this.autoCreated = false;
	}

	/* package-private */ void elementAdded(@NotNull CrateElement element) {
		if (this.contentComputed)
			this.content.add(element);
	}

	/* package-private */ void elementRemoved(@NotNull CrateElement element) {
		if (this.contentComputed) {
			this.content.remove(element);
			if (this.autoCreated && this.content.isEmpty())
				this.crates().namespaces().softDelete(this);
		}
	}

	@Override
	public @NotNull Iterator<@NotNull CrateElement> iterator() {
		return this.listContent().iterator();
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
}
