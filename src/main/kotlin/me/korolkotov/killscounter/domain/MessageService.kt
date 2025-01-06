package me.korolkotov.killscounter.domain

import me.korolkotov.killscounter.config.ConfigurationManager
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.MiniMessage
import org.bukkit.command.CommandSender
import java.io.File
import org.bukkit.configuration.file.YamlConfiguration

/**
 * Handles the retrieval and sending of localized messages to players and command senders.
 *
 * This class uses MiniMessage for advanced message formatting and supports multiple languages
 * by loading the appropriate language file from the plugin's `messages` directory.
 *
 * @property configurationManager Provides access to the plugin's configuration settings.
 * @property pluginFolder The folder where plugin-specific files (e.g., messages) are stored.
 */
class MessageService(
    configurationManager: ConfigurationManager,
    private val pluginFolder: File
) {
    /**
     * MiniMessage instance for advanced message formatting.
     */
    private val miniMessage = MiniMessage.miniMessage()

    /**
     * The current language for message localization, retrieved from the configuration.
     */
    private val language: String = configurationManager.config.getString("plugin.language", "en")!!

    /**
     * The loaded messages for the current language.
     */
    private var messages: YamlConfiguration

    init {
        // Load the language file during initialization
        messages = loadLanguageFile(language)
    }

    /**
     * Loads the language file for the specified language.
     *
     * If the file does not exist, it is created from the default resource file.
     *
     * @param language The language code (e.g., "en" or "ru") to load.
     * @return The loaded [YamlConfiguration] containing the messages.
     */
    private fun loadLanguageFile(language: String): YamlConfiguration {
        val langFile = File(pluginFolder, "messages/$language.yml")
        if (!langFile.exists()) {
            // Create directories and copy the default language file
            pluginFolder.resolve("messages").mkdirs()
            this::class.java.getResourceAsStream("/messages/$language.yml")?.use {
                langFile.outputStream().use { out -> it.copyTo(out) }
            }
        }
        return YamlConfiguration.loadConfiguration(langFile)
    }

    /**
     * Retrieves a localized message by its key and formats it with the provided replacements.
     *
     * @param key The key of the message to retrieve.
     * @param replacements A map of placeholders and their corresponding values for formatting the message.
     * @return A [Component] representing the formatted message.
     */
    private fun getMessage(key: String, replacements: Map<String, String> = emptyMap()): Component {
        val rawMessage = messages.getString("messages.$key")
            ?: return Component.text("Message not found: $key")

        val formattedMessage = replacements.entries.fold(rawMessage) { message, (placeholder, value) ->
            message.replace(placeholder, value)
        }

        return miniMessage.deserialize(formattedMessage)
    }

    /**
     * Sends a localized message to the specified [CommandSender].
     *
     * @param sender The recipient of the message (e.g., a player or console).
     * @param key The key of the message to send.
     * @param replacements A map of placeholders and their corresponding values for formatting the message.
     */
    fun sendMessage(sender: CommandSender, key: String, replacements: Map<String, String> = emptyMap()) {
        val message = getMessage(key, replacements)
        sender.sendMessage(message)
    }

    /**
     * Reloads the messages from the language file.
     *
     * This method should be called if the message file is updated while the plugin is running.
     */
    fun reloadMessages() {
        messages = loadLanguageFile(language)
    }
}