package fr.theoszanto.mc.crateexpress.utils;

import org.jetbrains.annotations.NotNull;

public class FormatUtils {
	private FormatUtils() {
		throw new UnsupportedOperationException();
	}

	public static @NotNull String noTrailingZeroDecimal(double value) {
		String str = Double.toString(value);
		return str.endsWith(".0") ? str.substring(0, str.length() - 2) : str;
	}

	public static @NotNull String splitEveryThreeDigits(double value) {
		return splitEveryThreeDigits(value, false);
	}

	public static @NotNull String splitEveryThreeDigits(double value, boolean noTrailingZeroDecimal) {
		StringBuilder builder = new StringBuilder(noTrailingZeroDecimal ? noTrailingZeroDecimal(value) : Double.toString(value));
		int dot = builder.indexOf(".");
		int end = dot == -1 ? builder.length() : dot;
		for (int i = 3; i < end; i++)
			if (i % 3 == 0)
				builder.insert(end - i, ' ');
		return builder.toString();
	}
}
