package fr.theoszanto.mc.crateexpress.utils;

import org.jetbrains.annotations.NotNull;

public final class CratePermission {
	public static final @NotNull String BASE = "crateexpress";
	public static final @NotNull String ALL = sub("*");

	public static final @NotNull String UNLIMITED_CLAIM = sub("unlimited-claim");

	public static final class Command {
		public static final @NotNull String BASE = CratePermission.sub("command");
		public static final @NotNull String ALL = sub("*");

		public static final @NotNull String ACCESS = sub("access");
		public static final @NotNull String CLAIM = sub("claim");
		public static final @NotNull String OPEN = sub("open");
		public static final @NotNull String PREVIEW = sub("preview");
		public static final @NotNull String GIVE = sub("give");
		public static final @NotNull String RELOAD = sub("reload");
		public static final @NotNull String LIST = sub("list");
		public static final @NotNull String CREATE = sub("create");
		public static final @NotNull String EDIT = sub("edit");
		public static final @NotNull String EXPORT = sub("export");
		public static final @NotNull String DELETE = sub("delete");
		public static final @NotNull String TELEPORT = sub("teleport");
		public static final @NotNull String VERSION = sub("version");

		private static @NotNull String sub(@NotNull String perm) {
			return BASE + "." + perm;
		}
	}

	private static @NotNull String sub(@NotNull String perm) {
		return BASE + "." + perm;
	}
}
