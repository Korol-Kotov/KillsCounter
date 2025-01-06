package me.korolkotov.killscounter

import me.korolkotov.killscounter.config.ConfigurationManager
import me.korolkotov.killscounter.di.DependencyInjector
import me.korolkotov.killscounter.listeners.PlayerKillsListener
import org.bukkit.plugin.java.JavaPlugin

/**
 * Main class of the `KillsCounter` plugin.
 * This class extends [JavaPlugin] and manages the plugin's lifecycle,
 * including initialization, dependency injection, and event registration.
 */
class Main : JavaPlugin() {

    /**
     * Manages the configuration files for the plugin.
     * Initialized during [onEnable].
     */
    lateinit var configurationManager: ConfigurationManager
        private set

    /**
     * Called when the plugin is enabled.
     * This method is responsible for:
     * - Initializing the [ConfigurationManager] for configuration handling.
     * - Initializing the [DependencyInjector] for managing dependencies.
     * - Registering the [PlayerKillsListener] for handling player-related events.
     * - Setting up the `/kills` command executor.
     */
    override fun onEnable() {
        // Initialize the configuration manager with the plugin's data folder
        configurationManager = ConfigurationManager(dataFolder)

        // Initialize the dependency injector
        DependencyInjector.init(this)

        // Register the event listener
        server.pluginManager.registerEvents(PlayerKillsListener(), this)

        // Register the "/kills" command executor from the dependency injector
        getCommand("kills")?.setExecutor(DependencyInjector.killsCommand)

        // Log that the plugin has been successfully enabled
        logger.info("Plugin $name enabled!")
    }

    /**
     * Called when the plugin is disabled.
     * This method is responsible for:
     * - Shutting down the [DependencyInjector] to clean up resources.
     * - Logging that the plugin has been disabled.
     */
    override fun onDisable() {
        // Shut down the dependency injector
        DependencyInjector.shutdown()

        // Log that the plugin has been disabled
        logger.info("Plugin $name disabled!")
    }
}
