package fr.theoszanto.mc.crateexpress.managers;

import fr.theoszanto.mc.crateexpress.CrateExpress;
import fr.theoszanto.mc.crateexpress.utils.Registry;
import org.bukkit.configuration.file.YamlConfiguration;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.regex.Matcher;

public class I18nManager extends Registry<String, String> {
	public I18nManager(@NotNull CrateExpress plugin) {
		super(plugin, "i18n");
	}

	public void loadMessages(@NotNull File file) {
		YamlConfiguration messages = YamlConfiguration.loadConfiguration(file);
		for (String key : messages.getKeys(true)) {
			Object message = messages.get(key, null);
			if (message instanceof String)
				this.register(key, message.toString());
		}
	}

	@Override
	public void register(@NotNull String key, @NotNull String message) {
		this.set(key, message);
	}

	public @NotNull String getMessage(@NotNull String key, @Nullable Object @NotNull... format) {
		String message;
		try {
			message = this.get(key);
		} catch (IllegalArgumentException e) {
			this.warn("Missing message with key \"" + key + "\".");
			return key;
		}
		try {
			return format(message, format);
		} catch (IllegalArgumentException e) {
			this.error("Could not format message with key \"" + key + "\".", e);
			return key;
		}
	}

	private static @NotNull String format(@NotNull String message, @Nullable Object @NotNull... format) {
		if (format.length % 2 != 0)
			throw new IllegalArgumentException("Illegal format parameters count (must be even): " + format.length);
		for (int i = 0; i < format.length; i += 2) {
			Object key = format[i];
			Object value = format[i + 1];
			if (!(key instanceof String))
				throw new IllegalArgumentException("Illegal format parameter key (must be String): " + key);
			message = message.replaceAll("#\\{" + key + "}", Matcher.quoteReplacement(String.valueOf(value)));
		}
		return message;
	}
}
