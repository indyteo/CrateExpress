package fr.theoszanto.mc.crateexpress.models;

import fr.theoszanto.mc.crateexpress.CrateExpress;
import fr.theoszanto.mc.crateexpress.utils.PluginObject;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

public class CrateNamespaceRegistry extends PluginObject implements Iterable<@NotNull CrateNamespace> {
	private final @NotNull Map<@NotNull String, @NotNull CrateNamespace> namespaces = new HashMap<>();

	public CrateNamespaceRegistry(@NotNull CrateExpress plugin) {
		super(plugin);
	}

	public void load() {
		for (CrateNamespace namespace : this.plugin.storage().getSource().loadNamespaces())
			this.namespaces.put(namespace.getPath(), namespace);
	}

	public void reset() {
		this.namespaces.clear();
	}

	/* package-private */ @NotNull CrateNamespace getOrAutoCreate(@NotNull String path) {
		AtomicBoolean created = new AtomicBoolean(false);
		CrateNamespace namespace = this.namespaces.computeIfAbsent(path, p -> {
			created.set(true);
			return new CrateNamespace(this.plugin, p);
		});
		if (created.get() && !namespace.isRoot())
			namespace.getParent().elementAdded(namespace);
		return namespace;
	}

	public @NotNull CrateNamespace create(@NotNull String path) {
		if (this.namespaces.containsKey(path))
			throw new IllegalArgumentException("The namespace " + path + " already exists");
		CrateNamespace namespace = new CrateNamespace(this.plugin, path, null);
		this.namespaces.put(path, namespace);
		this.async(() -> this.storage().getSource().saveNamespace(namespace));
		if (!namespace.isRoot())
			namespace.getParent().elementAdded(namespace);
		return namespace;
	}

	public @Nullable CrateNamespace get(@NotNull String path) {
		return path.isEmpty() ? this.root() : this.namespaces.get(path);
	}

	public @NotNull CrateNamespace root() {
		return this.getOrAutoCreate("");
	}

	public @NotNull CrateNamespace ofCrate(@NotNull Crate crate) {
		return this.parent(crate.getId());
	}

	/* package-private */ @NotNull CrateNamespace parent(@NotNull String path) {
		int sep = path.lastIndexOf(CrateNamespace.SEPARATOR);
		return this.getOrAutoCreate(sep == -1 ? "" : path.substring(0, sep));
	}

	public void delete(@NotNull CrateNamespace namespace) {
		this.softDelete(namespace);
		this.async(() -> this.storage().getSource().deleteNamespace(namespace.getPath()));
	}

	/* package-private */ void softDelete(@NotNull CrateNamespace namespace) {
		this.namespaces.remove(namespace.getPath());
		if (!namespace.isRoot())
			namespace.getParent().elementRemoved(namespace);
	}

	@Override
	public @NotNull Iterator<@NotNull CrateNamespace> iterator() {
		return this.namespaces.values().iterator();
	}
}
