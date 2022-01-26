package fr.theoszanto.mc.crateexpress.utils;

import org.jetbrains.annotations.NotNull;

import java.util.Collection;

public class MathUtils {
	private MathUtils() {
		throw new UnsupportedOperationException();
	}

	public static int minMax(int min, int value, int max) {
		return Math.max(min, Math.min(value, max));
	}

	public static double minMax(double min, double value, double max) {
		return Math.max(min, Math.min(value, max));
	}

	public static double round(double value, int precision) {
		double pow = Math.pow(10, precision);
		return Math.round(value * pow) / pow;
	}

	public static int nextAvailableInt(@NotNull Collection<@NotNull String> values) {
		int max = -1;
		for (String value : values) {
			try {
				int i = Integer.parseInt(value);
				if (i > max)
					max = i;
			} catch (NumberFormatException ignored) {}
		}
		return max + 1;
	}

	public static int random(int min, int max) {
		return min + (int) Math.floor(Math.random() * (max - min));
	}

	public static double random(double min, double max) {
		return min + Math.random() * (max - min);
	}

	public static int totalWeight(@NotNull Collection<? extends Weighted> elements) {
		int totalWeight = 0;
		for (Weighted reward : elements)
			totalWeight += reward.getWeight();
		return totalWeight;
	}

	public static <T extends Weighted> @NotNull T weightedRandom(@NotNull Collection<@NotNull T> elements) throws IllegalArgumentException {
		int random = random(0, totalWeight(elements));
		int weight = 0;
		for (T reward : elements) {
			weight += reward.getWeight();
			if (random < weight)
				return reward;
		}
		throw new IllegalArgumentException("Could not find random element in collection. Is it empty?");
	}

	public static int @NotNull[] numbers(int min, int max) {
		int[] numbers = new int[max - min];
		for (int i = 0, n = min; i < numbers.length; i++, n++) {
			numbers[i] = n;
		}
		return numbers;
	}
}
