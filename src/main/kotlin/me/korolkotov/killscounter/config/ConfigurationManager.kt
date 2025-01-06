package me.korolkotov.killscounter.config

import org.bukkit.configuration.file.YamlConfiguration
import java.io.File

/**
 * Manages the configuration files for the plugin.
 *
 * This class handles the loading, creation, and reloading of configuration and localization files.
 * By default, it loads `config.yml` and `messages.yml` from the plugin's data folder.
 *
 * @param dataFolder The folder where the plugin stores its data files.
 */
class ConfigurationManager(private val dataFolder: File) {

    /**
     * The main configuration file (`config.yml`).
     */
    val config: YamlConfiguration

    /**
     * The localization messages file (`messages.yml`).
     */
    private val messages: YamlConfiguration

    init {
        // Load or create the necessary configuration files
        config = loadOrCreate("config.yml")
        messages = loadOrCreate("messages.yml")
    }

    /**
     * Loads an existing YAML file or creates it by copying from the plugin's resources.
     *
     * @param fileName The name of the file to load or create.
     * @return The loaded [YamlConfiguration] object.
     */
    private fun loadOrCreate(fileName: String): YamlConfiguration {
        val file = File(dataFolder, fileName)
        if (!file.exists()) {
            // Create parent directories if necessary
            file.parentFile.mkdirs()
            // Copy the default file from resources if available
            this::class.java.getResourceAsStream("/$fileName")?.use {
                file.outputStream().use { out -> it.copyTo(out) }
            }
        }
        return YamlConfiguration.loadConfiguration(file)
    }

    /**
     * Reloads the configuration and messages files.
     *
     * This method should be called when configuration changes are made externally
     * (e.g., by editing the files while the server is running).
     */
    fun reload() {
        config.load(File(dataFolder, "config.yml"))
        messages.load(File(dataFolder, "messages.yml"))
    }
}
