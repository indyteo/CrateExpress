package fr.theoszanto.mc.crateexpress.utils;

import java.util.Objects;

public final class Pair<T, U> {
	private final T t;
	private final U u;

	public Pair(T t, U u) {
		this.t = t;
		this.u = u;
	}

	public T getT() {
		return this.t;
	}

	public U getU() {
		return this.u;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		Pair<?, ?> pair = (Pair<?, ?>) o;
		if (!Objects.equals(t, pair.t)) return false;
		return Objects.equals(u, pair.u);
	}

	@Override
	public int hashCode() {
		int result = t != null ? t.hashCode() : 0;
		result = 31 * result + (u != null ? u.hashCode() : 0);
		return result;
	}

	@Override
	public String toString() {
		return "[" + this.t + ", " + this.u + "]";
	}
}
