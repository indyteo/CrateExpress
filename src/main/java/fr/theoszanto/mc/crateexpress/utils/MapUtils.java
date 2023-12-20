package fr.theoszanto.mc.crateexpress.utils;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.BiFunction;

public class MapUtils {
	private MapUtils() {
		throw new UnsupportedOperationException();
	}

	public static final @NotNull BiFunction<@Nullable Object, @Nullable Integer, @NotNull Integer> INCREASE = (ignored, value) -> value == null ? 1 : value + 1;
}
