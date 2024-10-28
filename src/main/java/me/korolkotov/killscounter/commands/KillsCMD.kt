package me.korolkotov.killscounter.commands

import me.korolkotov.killscounter.Main
import me.korolkotov.killscounter.util.chat.ChatUtil
import org.bukkit.Bukkit
import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.command.TabExecutor

class KillsCMD(
    private val plugin: Main
) : TabExecutor {
    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        if (args.isEmpty()) {
            ChatUtil.sendMessage(sender, plugin.messageUtil.getText("specify-player").getList())
            return true
        }

        val player = args[0]
        if (!plugin.databaseManager.playerExists(player)) {
            ChatUtil.sendMessage(sender, plugin.messageUtil.getText("has-no-kills").getList())
            return true
        }

        val kills = plugin.databaseManager.getPlayerKills(player)

        if (kills <= 0) {
            ChatUtil.sendMessage(sender, plugin.messageUtil.getText("has-no-kills").getList())
            return true
        }

        ChatUtil.sendMessage(sender, plugin.messageUtil.getText("kills", mapOf(Pair("%player%", player), Pair("%kills%", "$kills"))).getList())
        return true
    }

    override fun onTabComplete(sender: CommandSender, command: Command, alias: String, args: Array<out String>): MutableList<String> {
        if (args.size == 1) {
            val players = mutableListOf<String>()
            Bukkit.getOnlinePlayers().forEach { players.add(it.name) }
            return players
        }

        return mutableListOf()
    }
}