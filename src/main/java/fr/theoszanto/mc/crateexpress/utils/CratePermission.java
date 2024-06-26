package fr.theoszanto.mc.crateexpress.utils;

import org.jetbrains.annotations.NotNull;

public final class CratePermission {
	public static final @NotNull String BASE = "crateexpress";
	public static final @NotNull String ALL = sub("*");

	public static final @NotNull String UNLIMITED_CLAIM = sub("unlimited-claim");
	public static final @NotNull String BYPASS_DISABLED = sub("bypass-disabled");
	public static final @NotNull String BYPASS_NO_PREVIEW = sub("bypass-no-preview");

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
		public static final @NotNull String STATS = sub("stats");
		public static final @NotNull String CREATE = sub("create");
		public static final @NotNull String EDIT = sub("edit");
		public static final @NotNull String EXPORT = sub("export");
		public static final @NotNull String DELETE = sub("delete");
		public static final @NotNull String TELEPORT = sub("teleport");
		public static final @NotNull String VERSION = sub("version");

		public static final class History {
			public static final @NotNull String BASE = Command.sub("history");
			public static final @NotNull String ALL = sub("*");

			public static final @NotNull String SELF = sub("self");
			public static final @NotNull String OTHER = sub("other");

			private static @NotNull String sub(@NotNull String perm) {
				return BASE + "." + perm;
			}
		}

		private static @NotNull String sub(@NotNull String perm) {
			return BASE + "." + perm;
		}
	}

	private static @NotNull String sub(@NotNull String perm) {
		return BASE + "." + perm;
	}
}
