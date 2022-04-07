package fr.theoszanto.mc.crateexpress.models;

import org.jetbrains.annotations.NotNull;

public interface CrateElement extends Comparable<CrateElement> {
	default boolean isCrate() {
		return this instanceof Crate;
	}

	default boolean isNamespace() {
		return this instanceof CrateNamespace;
	}

	@Override
	default int compareTo(@NotNull CrateElement other) {
		if (this.isNamespace()) {
			if (other.isNamespace())
				return ((CrateNamespace) this).getPath().compareToIgnoreCase(((CrateNamespace) other).getPath());
			return -1;
		}
		if (this.isCrate()) {
			if (other.isCrate())
				return ((Crate) this).getId().compareToIgnoreCase(((Crate) other).getId());
			return 1;
		}
		return 1;
	}
}
