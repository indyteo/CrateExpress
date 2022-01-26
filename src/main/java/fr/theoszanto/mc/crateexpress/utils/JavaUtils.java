package fr.theoszanto.mc.crateexpress.utils;

import com.google.common.reflect.ClassPath;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.lang.reflect.Array;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

public class JavaUtils {
	private JavaUtils() {
		throw new UnsupportedOperationException();
	}

	public static @NotNull String caller() {
		// Get call stack and skip "getStackTrace", "caller" & calling method calls
		StackTraceElement[] stack = Thread.currentThread().getStackTrace();
		return stack.length < 4 ? "Unknown caller" : stack[3].toString();
	}

	public static @NotNull Class<?> @NotNull[] extractTypes(@Nullable Object @NotNull[] objects) {
		Class<?>[] types = new Class<?>[objects.length];
		for (int i = 0; i < objects.length; i++) {
			Object object = objects[i];
			if (object instanceof Null) {
				Null<?> nullObject = (Null<?>) object;
				types[i] = nullObject.getType();
				objects[i] = nullObject.get();
			} else
				types[i] = object == null ? Object.class : object.getClass();
		}
		return types;
	}

	@SuppressWarnings({ "unchecked", "UnstableApiUsage" })
	public static <T> @NotNull List<? extends T> instanciateSubClasses(@NotNull Class<T> parentClass, @NotNull String packageName, @NotNull Object @NotNull... initArgs) {
		ClassLoader loader = parentClass.getClassLoader();
		ClassPath path;
		try {
			path = ClassPath.from(loader);
		} catch (IOException e) {
			throw new IllegalArgumentException("Could not load class path from parent class", e);
		}
		Class<?>[] types = extractTypes(initArgs);
		List<T> instances = new ArrayList<>();
		for (ClassPath.ClassInfo info : path.getTopLevelClassesRecursive(packageName)) {
			String name = info.getName();
			try {
				Class<?> type = Class.forName(name, true, loader);
				if (type != parentClass && parentClass.isAssignableFrom(type)
						&& !parentClass.isInterface() && Modifier.isAbstract(parentClass.getModifiers()))
					instances.add((T) type.getConstructor(types).newInstance(initArgs));
			} catch (ReflectiveOperationException e) {
				throw new IllegalArgumentException("Failed to instanciate sub-class: " + name, e);
			}
		}
		return instances;
	}

	public static class Null<T> {
		private final @NotNull Class<T> type;

		public Null(@NotNull Class<T> type) {
			this.type = type;
		}

		public @NotNull Class<T> getType() {
			return this.type;
		}

		public @Nullable T get() {
			return null;
		}
	}

	public static <T> @NotNull T @NotNull[] subArray(@NotNull T @NotNull[] array, int start) {
		return subArray(array, start, array.length);
	}

	@SuppressWarnings("unchecked")
	public static <T> @NotNull T @NotNull[] subArray(@NotNull T @NotNull[] array, int start, int end) {
		int length = end - start;
		if (length < 0 || start >= array.length)
			length = 0;
		T[] sub = (T[]) Array.newInstance(array.getClass().getComponentType(), length);
		if (length == 0)
			return sub;
		System.arraycopy(array, start, sub, 0, length);
		return sub;
	}
}
