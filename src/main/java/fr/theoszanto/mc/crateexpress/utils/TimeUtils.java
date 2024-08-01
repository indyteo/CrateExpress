package fr.theoszanto.mc.crateexpress.utils;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.time.Duration;
import java.util.Comparator;
import java.util.Date;
import java.util.concurrent.TimeUnit;

public class TimeUtils {
	@SuppressWarnings("deprecation")
	public static final @NotNull Comparator<@NotNull Date> DATE_WITHOUT_TIME_COMPARATOR = Comparator.comparingInt(Date::getYear)
			.thenComparingInt(Date::getMonth)
			.thenComparingInt(Date::getDate);

	private TimeUtils() {
		throw new UnsupportedOperationException();
	}

	@Contract(value = " -> new")
	public static @NotNull Date today() {
		return removeTime(new Date());
	}

	@Contract(value = "_ -> new")
	public static @NotNull Date cloneWithoutTime(@NotNull Date date) {
		return removeTime((Date) date.clone());
	}

	@Contract(value = "_, _ -> new")
	public static @NotNull Date cloneWithTime(@NotNull Date date, @NotNull Date time) {
		return setTime((Date) date.clone(), time);
	}

	@SuppressWarnings("deprecation")
	@Contract(value = "_ -> param1", mutates = "param1")
	public static @NotNull Date removeTime(@NotNull Date date) {
		date.setHours(0);
		date.setMinutes(0);
		date.setSeconds(0);
		return date;
	}

	@SuppressWarnings("deprecation")
	@Contract(value = "_, _ -> param1", mutates = "param1")
	public static @NotNull Date setTime(@NotNull Date date, @NotNull Date time) {
		date.setHours(time.getHours());
		date.setMinutes(time.getMinutes());
		date.setSeconds(time.getSeconds());
		return date;
	}

	@Contract(value = "_, _, _ -> new")
	public static @NotNull Date add(@NotNull Date date, long time, @NotNull TimeUnit unit) {
		return new Date(date.getTime() + unit.toMillis(time));
	}

	public static int compareIgnoringTime(@NotNull Date a, @NotNull Date b) {
		return DATE_WITHOUT_TIME_COMPARATOR.compare(a, b);
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
		return str.isEmpty() ? "" : str.deleteCharAt(str.length() - 1).toString();
	}

	public static @NotNull Duration ceilToSecond(@NotNull Duration duration) {
		return duration.getNano() == 0 ? duration : duration.withNanos(0).withSeconds(duration.getSeconds() + 1);
	}
}
