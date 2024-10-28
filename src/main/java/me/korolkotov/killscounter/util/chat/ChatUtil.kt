package me.korolkotov.killscounter.util.chat

import net.md_5.bungee.api.ChatColor
import org.bukkit.command.CommandSender
import java.util.regex.Pattern

object ChatUtil {
    fun format(text: String): String {
        var message = text

        val pattern = Pattern.compile("&#[a-fA-F0-9]{6}")
        var matcher = pattern.matcher(message)

        while (matcher.find()) {
            val color = message.substring(matcher.start() + 1, matcher.end())
            message = message.replace("&$color", ChatColor.of(color).toString() + "")
            matcher = pattern.matcher(message)
        }

        return ChatColor.translateAlternateColorCodes('&', message)
    }

    fun format(list: List<String>): List<String> {
        return list.map { format(it) }
    }

    fun sendMessage(sender: CommandSender, text: String) {
        sender.sendMessage(text)
    }

    fun sendMessage(sender: CommandSender, list: List<String>) {
        for (text in list) sendMessage(sender, text)
    }
}