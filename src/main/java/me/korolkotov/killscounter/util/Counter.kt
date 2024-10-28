package me.korolkotov.killscounter.util

import me.korolkotov.killscounter.Main
import org.bukkit.scheduler.BukkitRunnable

class Counter(
    private val plugin: Main
) {
    private val playerKills = mutableMapOf<String, Int>()

    init {
        object : BukkitRunnable() {
            override fun run() = write()
        }.runTaskTimer(plugin, 100L, 100L)
    }

    fun add(playerName: String) {
        playerKills[playerName] = get(playerName) + 1
    }

    fun get(playerName: String): Int = playerKills.getOrDefault(playerName, 0)

    fun write() {
        if (playerKills.isNotEmpty()) {
            plugin.databaseManager.updatePlayerKills(playerKills)
            playerKills.clear()
        }
    }
}