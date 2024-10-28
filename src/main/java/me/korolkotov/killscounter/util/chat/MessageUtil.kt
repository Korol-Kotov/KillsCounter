package me.korolkotov.killscounter.util.chat

import me.korolkotov.killscounter.Main

class MessageUtil(
    private val plugin: Main
) {
    private fun getTag(): String? = plugin.messages.getString("tag")?.replace("%plugin%", plugin.name)

    fun getText(path: String?, args: Map<String, String> = mapOf(), needTag: Boolean = true): Text {
        if (path == null) return Text.empty()

        val tag = if (needTag) getTag() ?: "" else ""

        if (plugin.messages.isList(path)) {
            val text = Text(plugin.messages.getStringList(path), tag)
            text.format(args)
            return text
        } else if (plugin.messages.isString(path)) {
            val text = Text(tag + plugin.messages.getString(path))
            text.format(args)
            return text
        } else return Text.empty()
    }
}