package fr.theoszanto.mc.crateexpress.utils;

import fr.theoszanto.mc.crateexpress.CrateExpress;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.UnmodifiableView;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class Registry<K, V> extends PluginObject implements Iterable<V> {
	private final @NotNull Map<@NotNull K, @NotNull V> values = new HashMap<>();
	@UnmodifiableView
	private final @NotNull Collection<@NotNull V> unmodifiableValues = Collections.unmodifiableCollection(this.values.values());
	private final @NotNull String name;

	public Registry(@NotNull CrateExpress plugin, @NotNull String name) {
		super(plugin);
		this.name = name;
	}

	public void reset() {
		this.values.clear();
	}

	@UnmodifiableView
	public @NotNull Collection<@NotNull V> list() {
		return this.unmodifiableValues;
	}

	@Override
	public @NotNull Iterator<@NotNull V> iterator() {
		return this.list().iterator();
	}

	protected final void set(@NotNull K key, @NotNull V value) {
		V old = this.values.put(key, value);
		if (old != null)
			this.warn("Overwriting old value in registry \"" + this.name + "\" at key \"" + key + "\".");
	}

	public void register(@NotNull K key, @NotNull V value) throws IllegalArgumentException {
		if (this.values.containsKey(key))
			throw new IllegalArgumentException("A value already has been register for this key");
		this.set(key, value);
	}

	public @NotNull V get(@NotNull K key) throws IllegalArgumentException {
		V value = this.values.get(key);
		if (value == null)
			throw new IllegalArgumentException("No value was registered for this key");
		return value;
	}

	public void delete(@NotNull K key) throws IllegalArgumentException {
		V old = this.values.remove(key);
		if (old == null)
			throw new IllegalArgumentException("No value was registered for this key");
	}
}