package me.korolkotov.killscounter.presentation.commands

import me.korolkotov.killscounter.domain.Counter
import me.korolkotov.killscounter.domain.MessageService
import org.bukkit.Bukkit
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender

/**
 * Handles the `/kills` command.
 * This command allows players or administrators to view the number of kills for a specified player.
 *
 * @property counter Provides access to the kill count data for players.
 * @property messageService Sends localized messages to the command sender.
 */
class KillsCommand(
    private val counter: Counter,
    private val messageService: MessageService
) : CommandExecutor {

    /**
     * Executes the `/kills` command.
     *
     * @param sender The entity that issued the command (e.g., player or console).
     * @param command The command being executed.
     * @param label The alias used to execute the command.
     * @param args Arguments passed with the command, where the first argument is expected to be a player's name.
     * @return `true` if the command was executed successfully, otherwise `false`.
     */
    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        // Check if the player name argument is provided
        if (args.isEmpty()) {
            messageService.sendMessage(sender, "specify-player")
            return true
        }

        // Extract the player name from the arguments
        val playerName = args[0]
        val onlinePlayer = Bukkit.getPlayerExact(playerName)

        // Check if the specified player is online
        if (onlinePlayer == null || !onlinePlayer.isOnline) {
            messageService.sendMessage(sender, "player-not-found", mapOf("%player%" to playerName))
            return true
        }

        // Retrieve the player's UUID and their kill count
        val playerUuid = onlinePlayer.uniqueId
        val kills = counter.getKills(playerUuid)

        // Send an appropriate message based on the kill count
        if (kills <= 0) {
            messageService.sendMessage(sender, "player-has-no-kills", mapOf("%player%" to playerName))
        } else {
            messageService.sendMessage(
                sender,
                "player-kills",
                mapOf("%player%" to playerName, "%kills%" to kills.toString())
            )
        }

        // Log the request in the server console
        Bukkit.getLogger().info("${sender.name} requested the kill count for player $playerName. Kills: $kills")

        return true
    }
}
