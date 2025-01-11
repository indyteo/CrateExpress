package fr.theoszanto.mc.crateexpress.models;

import org.jetbrains.annotations.NotNull;

public interface CrateElement extends Comparable<CrateElement> {
	@Override
	default int compareTo(@NotNull CrateElement other) {
		if (this instanceof CrateNamespace thisNamespace) {
			if (other instanceof CrateNamespace otherNamespace)
				return thisNamespace.getPath().compareToIgnoreCase(otherNamespace.getPath());
			return -1;
		}
		if (this instanceof Crate thisCrate) {
			if (other instanceof Crate otherCrate)
				return thisCrate.getId().compareToIgnoreCase(otherCrate.getId());
			return 1;
		}
		return 1;
	}
}
