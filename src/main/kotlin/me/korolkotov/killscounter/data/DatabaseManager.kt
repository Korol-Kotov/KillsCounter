package me.korolkotov.killscounter.data

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import org.bukkit.configuration.file.YamlConfiguration
import java.io.File
import java.sql.Connection
import java.sql.DriverManager

/**
 * Manages the database connection and initializes the required schema for the plugin.
 *
 * This class supports two types of databases:
 * - MySQL (recommended for large-scale environments)
 * - SQLite (recommended for smaller setups or local testing)
 *
 * @param config The YAML configuration file for database settings.
 * @param dataFolder The plugin's data folder for storing SQLite files and other resources.
 */
class DatabaseManager(config: YamlConfiguration, private val dataFolder: File) {

    /**
     * The type of database being used (`mysql` or `sqlite`).
     */
    private val databaseType = config.getString("database.type", "sqlite")!!

    /**
     * The HikariCP connection pool for MySQL connections.
     * This will be null if SQLite is used.
     */
    private var dataSource: HikariDataSource? = null

    /**
     * The direct connection for SQLite.
     * This will be null if MySQL is used.
     */
    private var sqliteConnection: Connection? = null

    init {
        // Initialize the database connection based on the configured type
        when (databaseType.lowercase()) {
            "mysql" -> initializeMySQL(config)
            "sqlite" -> initializeSQLite(config)
            else -> throw IllegalArgumentException("Unsupported database type: $databaseType")
        }
        // Initialize the database schema
        initializeSchema()
    }

    /**
     * Initializes a MySQL connection using HikariCP.
     *
     * @param config The configuration containing MySQL settings.
     */
    private fun initializeMySQL(config: YamlConfiguration) {
        val hikariConfig = HikariConfig().apply {
            jdbcUrl = "jdbc:mysql://${config.getString("database.mysql.host")}:${config.getString("database.mysql.port")}/${config.getString("database.mysql.database")}?useSSL=false"
            username = config.getString("database.mysql.user")
            password = config.getString("database.mysql.password")
            maximumPoolSize = config.getInt("database.mysql.pool-size", 10)
            minimumIdle = config.getInt("database.mysql.minimum-idle", 5)
            idleTimeout = config.getLong("database.mysql.idle-timeout", 600000)
            connectionTimeout = config.getLong("database.mysql.connection-timeout", 30000)
            maxLifetime = config.getLong("database.mysql.max-lifetime", 1800000)
            addDataSourceProperty("cachePrepStmts", "true")
            addDataSourceProperty("prepStmtCacheSize", "250")
            addDataSourceProperty("prepStmtCacheSqlLimit", "2048")
        }
        dataSource = HikariDataSource(hikariConfig)
    }

    /**
     * Initializes an SQLite connection and creates the database file if it does not exist.
     *
     * @param config The configuration containing SQLite settings.
     */
    private fun initializeSQLite(config: YamlConfiguration) {
        val databaseFile = File(dataFolder, config.getString("database.sqlite.file", "database.sqlite")!!)
        if (!databaseFile.exists()) {
            databaseFile.parentFile.mkdirs()
            databaseFile.createNewFile()
        }
        sqliteConnection = DriverManager.getConnection("jdbc:sqlite:${databaseFile.absolutePath}")
    }

    /**
     * Provides a database connection based on the configured database type.
     *
     * @return A [Connection] object for interacting with the database.
     * @throws IllegalStateException If an unsupported database type is configured.
     */
    fun getConnection(): Connection {
        return when (databaseType.lowercase()) {
            "mysql" -> dataSource!!.connection
            "sqlite" -> sqliteConnection!!
            else -> throw IllegalStateException("Unsupported database type: $databaseType")
        }
    }

    /**
     * Closes the database connection or the connection pool.
     *
     * This method should be called when the plugin is disabled to release all resources.
     */
    fun close() {
        dataSource?.close()
        sqliteConnection?.close()
    }

    /**
     * Initializes the database schema.
     *
     * Creates the required tables for the plugin, including `players` and `player_kills`.
     * Ensures that the schema is consistent across MySQL and SQLite.
     */
    private fun initializeSchema() {
        getConnection().use { conn ->
            val schemaStatements = listOf(
                """
                CREATE TABLE IF NOT EXISTS players (
                    uuid CHAR(36) PRIMARY KEY,
                    name VARCHAR(255) NOT NULL UNIQUE,
                    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                    INDEX idx_name (name)
                );
                """,
                """
                CREATE TABLE IF NOT EXISTS player_kills (
                    killer_uuid CHAR(36) NOT NULL,
                    victim_uuid CHAR(36) NOT NULL,
                    kills INT DEFAULT 1,
                    timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                    PRIMARY KEY (killer_uuid, victim_uuid),
                    FOREIGN KEY (killer_uuid) REFERENCES players (uuid) ON DELETE CASCADE,
                    FOREIGN KEY (victim_uuid) REFERENCES players (uuid) ON DELETE CASCADE
                );
                """
            )
            conn.createStatement().use { stmt ->
                schemaStatements.forEach { stmt.addBatch(it) }
                stmt.executeBatch()
            }
        }
    }
}
