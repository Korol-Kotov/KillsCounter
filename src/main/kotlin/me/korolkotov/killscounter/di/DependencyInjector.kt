package me.korolkotov.killscounter.di

import me.korolkotov.killscounter.Main
import me.korolkotov.killscounter.data.DatabaseManager
import me.korolkotov.killscounter.domain.Counter
import me.korolkotov.killscounter.domain.MessageService
import me.korolkotov.killscounter.presentation.commands.KillsCommand

/**
 * A singleton object responsible for dependency injection in the `KillsCounter` plugin.
 *
 * This object initializes and manages the lifecycle of core components, such as the database manager,
 * message service, and command handlers.
 */
object DependencyInjector {
    /**
     * The manager responsible for database operations.
     */
    private lateinit var databaseManager: DatabaseManager

    /**
     * The counter for tracking player kills.
     */
    lateinit var counter: Counter
        private set

    /**
     * The service for handling localized messages.
     */
    lateinit var messageService: MessageService
        private set

    /**
     * The command executor for the `/kills` command.
     */
    lateinit var killsCommand: KillsCommand
        private set

    /**
     * Initializes the dependencies required for the plugin.
     *
     * This method must be called during the plugin's [Main.onEnable] lifecycle event.
     *
     * @param plugin The main plugin instance, used to access configurations and the plugin folder.
     */
    fun init(plugin: Main) {
        // Initialize the database manager
        databaseManager = DatabaseManager(plugin.configurationManager.config, plugin.dataFolder)

        // Initialize the message service
        messageService = MessageService(plugin.configurationManager, plugin.dataFolder)

        // Initialize the counter for player kills
        counter = Counter(databaseManager)

        // Initialize the command executor for the `/kills` command
        killsCommand = KillsCommand(counter, messageService)
    }

    /**
     * Cleans up resources and saves data during the plugin shutdown.
     *
     * This method must be called during the plugin's [Main.onDisable] lifecycle event.
     */
    fun shutdown() {
        // Save kill data to the database
        counter.save()

        // Close the database connection
        databaseManager.close()
    }
}
