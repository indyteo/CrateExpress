package fr.theoszanto.mc.crateexpress.utils;

import java.util.Objects;

public final class Pair<T, U> {
	private final T first;
	private final U second;

	public Pair(T first, U second) {
		this.first = first;
		this.second = second;
	}

	public T getFirst() {
		return this.first;
	}

	public U getSecond() {
		return this.second;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		Pair<?, ?> pair = (Pair<?, ?>) o;
		if (!Objects.equals(this.first, pair.first)) return false;
		return Objects.equals(this.second, pair.second);
	}

	@Override
	public int hashCode() {
		int result = this.first != null ? this.first.hashCode() : 0;
		result = 31 * result + (this.second != null ? this.second.hashCode() : 0);
		return result;
	}

	@Override
	public String toString() {
		return "[" + this.first + ", " + this.second + "]";
	}
}
