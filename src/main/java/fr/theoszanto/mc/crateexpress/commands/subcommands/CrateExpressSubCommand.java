package fr.theoszanto.mc.crateexpress.commands.subcommands;

import fr.theoszanto.mc.crateexpress.CrateExpress;
import fr.theoszanto.mc.crateexpress.commands.CrateExpressCommand;
import fr.theoszanto.mc.crateexpress.utils.PluginObject;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.HumanEntity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public abstract class CrateExpressSubCommand extends PluginObject {
	private final @NotNull String name;
	private final @NotNull List<@NotNull String> aliases;

	public CrateExpressSubCommand(@NotNull CrateExpress plugin, @NotNull String name, @NotNull String @NotNull... aliases) {
		super(plugin);
		this.name = name;
		this.aliases = Arrays.asList(aliases);
	}

	public abstract boolean canExecute(@NotNull CommandSender sender, @NotNull CrateExpressCommand command, @NotNull String alias, @NotNull String subAlias, @NotNull String @NotNull[] args);

	public abstract boolean onCommand(@NotNull CommandSender sender, @NotNull CrateExpressCommand command, @NotNull String alias, @NotNull String subAlias, @NotNull String @NotNull[] args);

	public abstract @Nullable List<@NotNull String> tabComplete(@NotNull CommandSender sender, @NotNull CrateExpressCommand command, @NotNull String alias, @NotNull String subAlias, @NotNull String @NotNull[] args);

	public final @NotNull String getName() {
		return this.name;
	}

	public final @NotNull List<@NotNull String> getAliases() {
		return this.aliases;
	}

	public final boolean hasName(@NotNull String name) {
		if (this.name.equalsIgnoreCase(name))
			return true;
		for (String alias : this.aliases)
			if (alias.equalsIgnoreCase(name))
				return true;
		return false;
	}

	protected final @NotNull List<@NotNull String> onlinePlayers() {
		return Bukkit.getOnlinePlayers().stream().map(HumanEntity::getName).collect(Collectors.toList());
	}

	protected final @NotNull List<@NotNull String> existingCrates() {
		return new ArrayList<>(this.crates().keys());
	}

	protected final @NotNull List<@NotNull String> numbers(int min, int max) {
		List<String> ints = new ArrayList<>();
		for (int n = min; n <= max; n++)
			ints.add(Integer.toString(n));
		return ints;
	}
}
