package me.korolkotov.killscounter

import me.korolkotov.killscounter.commands.KillsCMD
import me.korolkotov.killscounter.database.DatabaseManager
import me.korolkotov.killscounter.listeners.PlayerKillsListener
import me.korolkotov.killscounter.util.Counter
import me.korolkotov.killscounter.util.chat.MessageUtil
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.plugin.java.JavaPlugin
import java.io.File

class Main : JavaPlugin() {

    lateinit var messageUtil: MessageUtil private set
    lateinit var databaseManager: DatabaseManager private set
    lateinit var counter: Counter private set

    lateinit var config: YamlConfiguration private set
    lateinit var messages: YamlConfiguration private set

    override fun onEnable() {
        loadFiles()

        this.messageUtil = MessageUtil(this)
        this.databaseManager = DatabaseManager(this)
        this.counter = Counter(this)

        server.pluginManager.registerEvents(PlayerKillsListener(this), this)

        getCommand("kills")?.setExecutor(KillsCMD(this))

        logger.info("Plugin $name enabled!")
    }

    override fun onDisable() {
        this.counter.write()
        logger.info("Plugin $name disabled!")
    }

    private fun loadFiles() {
        val configFile = File(dataFolder, "config.yml")
        if (!configFile.exists()) saveResource("config.yml", false)
        config = YamlConfiguration.loadConfiguration(configFile)

        val messagesFile = File(dataFolder, "messages.yml")
        if (!messagesFile.exists()) saveResource("messages.yml", false)
        messages = YamlConfiguration.loadConfiguration(messagesFile)
    }
}