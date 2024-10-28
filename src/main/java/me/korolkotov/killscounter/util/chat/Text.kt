package me.korolkotov.killscounter.util.chat

class Text private constructor() {
    companion object {
        fun empty(): Text = Text()
    }

    private val texts = mutableListOf<String>()

    constructor(text: String) : this() {
        texts.add(ChatUtil.format(text))
    }

    constructor(list: List<String>, inStart: String = "") : this() {
        for (text in list)
            texts.add(ChatUtil.format(inStart + text))
    }

    fun getString(): String = texts[0]
    fun getList(): List<String> = texts

    fun format(args: Map<String, String>) {
        for (textIndex in texts.indices) {
            var text = texts[textIndex]
            for (key in args.keys)
                text = text.replace(key, args[key]!!)
            texts[textIndex] = text
        }
    }

    fun isSingle(): Boolean = texts.size == 1
    fun isEmpty(): Boolean = texts.isEmpty()
    fun isMulti(): Boolean = texts.size > 1
}