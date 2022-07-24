package fr.theoszanto.mc.crateexpress.utils;

import org.jetbrains.annotations.NotNull;

import java.time.Duration;

public class TimeUtils {
	private TimeUtils() {
		throw new UnsupportedOperationException();
	}

	public static @NotNull String formatDuration(@NotNull Duration duration) {
		if (duration.isNegative())
			duration = duration.negated();
		duration = ceilToSecond(duration);
		StringBuilder str = new StringBuilder();
		long days = duration.toDays();
		if (days > 0)
			str.append(days).append("j ");
		long hours = duration.toHours() % 24;
		if (hours > 0)
			str.append(hours).append("h ");
		long minutes = duration.toMinutes() % 60;
		if (minutes > 0)
			str.append(minutes).append("m ");
		long seconds = duration.getSeconds() % 60;
		if (seconds > 0)
			str.append(seconds).append("s ");
		return str.length() == 0 ? "" : str.deleteCharAt(str.length() - 1).toString();
	}

	public static @NotNull Duration ceilToSecond(@NotNull Duration duration) {
		return duration.getNano() == 0 ? duration : duration.withNanos(0).withSeconds(duration.getSeconds() + 1);
	}
}
