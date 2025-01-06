package me.korolkotov.killscounter.listeners

import me.korolkotov.killscounter.di.DependencyInjector
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.PlayerDeathEvent

/**
 * Listener for handling player death events.
 * Tracks player kills and notifies players of relevant events.
 */
class PlayerKillsListener : Listener {

    /**
     * Handles the [PlayerDeathEvent].
     *
     * Updates the killer's kill count and notifies both the killer and the victim.
     *
     * @param event The event triggered when a player dies.
     */
    @EventHandler
    fun onPlayerDeath(event: PlayerDeathEvent) {
        val victim = event.entity
        val killer = victim.killer

        // If the killer is a player, update the kill count and send notifications
        if (killer is Player) {
            val killerUuid = killer.uniqueId
            val victimUuid = victim.uniqueId

            // Increment the killer's kill count with victim details
            DependencyInjector.counter.addKill(killerUuid, victimUuid)

            // Retrieve the total kills for the killer
            val totalKills = DependencyInjector.counter.getKills(killerUuid)

            // Log the kill event
            Bukkit.getLogger().info("${killer.name} убил ${victim.name}. Всего убийств: $totalKills")

            // Notify the killer about their kill and total kill count
            DependencyInjector.messageService.sendMessage(
                killer,
                "kill-notification",
                mapOf("%victim%" to victim.name, "%kills%" to totalKills.toString())
            )

            // Notify the victim about their killer
            DependencyInjector.messageService.sendMessage(
                victim,
                "death-notification",
                mapOf("%killer%" to killer.name)
            )
        } else {
            Bukkit.getLogger().info("${victim.name} погиб, но не от рук игрока.")
        }
    }
}
