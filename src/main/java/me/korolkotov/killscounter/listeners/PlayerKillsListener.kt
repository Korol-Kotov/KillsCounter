package me.korolkotov.killscounter.listeners

import me.korolkotov.killscounter.Main
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.PlayerDeathEvent

class PlayerKillsListener(
    private val plugin: Main
) : Listener {
    @EventHandler
    fun onPlayerKill(event: PlayerDeathEvent) {
        val player = event.entity
        val killer = player.killer

        if (killer != null && player.uniqueId != killer.uniqueId)
            plugin.counter.add(killer.name)
    }
}