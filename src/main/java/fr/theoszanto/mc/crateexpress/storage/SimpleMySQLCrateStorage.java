package fr.theoszanto.mc.crateexpress.storage;

import fr.theoszanto.mc.crateexpress.CrateExpress;
import org.jetbrains.annotations.NotNull;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class SimpleMySQLCrateStorage extends MySQLCrateStorage {
	private final @NotNull Connection connection;

	public SimpleMySQLCrateStorage(@NotNull CrateExpress plugin, @NotNull String hostname, @NotNull String database, @NotNull String username, @NotNull String password, @NotNull String tablePrefix) {
		this(plugin, hostname, database, username, password, tablePrefix, "");
	}

	public SimpleMySQLCrateStorage(@NotNull CrateExpress plugin, @NotNull String hostname, @NotNull String database, @NotNull String username, @NotNull String password, @NotNull String tablePrefix, @NotNull String additionalOptions) {
		super(plugin, tablePrefix);
		try {
			this.connection = DriverManager.getConnection("jdbc:mysql://" + hostname + "/" + database + "?autoreconnect=true&useUnicode=true&characterEncoding=utf-8" + additionalOptions, username, password);
		} catch (SQLException e) {
			throw new IllegalArgumentException("Unable to open SQL connection", e);
		}
	}

	@Override
	protected @NotNull Connection getConnection() {
		return this.connection;
	}
}
