package cn.souts.taboolibinitializr.utils

import java.awt.Color

object BukkitColorAnsiBuilder {

    private const val MARKER = '&'
    private const val PREFIX = '{'
    private const val SUFFIX = '}'

    fun colored(string: String): String {
        val sb = StringBuilder()
        val charArray = string.toCharArray()
        var index = 0
        while (index < charArray.size) {
            val c = charArray[index]
            if (c == MARKER) {
                if (index + 1 < charArray.size) {
                    val code = charArray[index + 1]
                    if (code == PREFIX) {
                        sb.append(AnsiBukkitCode.parse(appendEndSuffix(string.substring(index + 2)).also {
                            index += it.length + 2
                        }))

                    } else {
                        sb.append(AnsiBukkitCode.parse(code))
                        index++
                    }
                }
            } else {
                sb.append(c)
            }
            index++
        }
        return sb.apply { append("\u001B[0m") }.toString()
    }

    fun addColor(key: String, color: Color, alias: Char? = null) {
        AnsiBukkitCode.customMap.put(key, object : AnsiBukkitCode {
            override val alias: Char? = alias

            override fun transform(): String {
                return "\u001B[38;2;${color.red};${color.green};${color.blue}m"
            }
        })
    }

    private fun appendEndSuffix(string: String): String {
        val sb = StringBuilder()
        val charArray = string.toCharArray()
        charArray.forEachIndexed { index, c ->
            if (c == SUFFIX) {
                return sb.toString()
            } else {
                sb.append(c)
            }
        }
        return sb.toString()
    }

}

interface AnsiBukkitCode {

    val alias: Char?

    fun transform(): String

    companion object {

        val customMap: MutableMap<String, AnsiBukkitCode> = mutableMapOf()

        @JvmStatic
        fun parse(code: String): String {
            if (code.split(Regex("[, ，]")).size == 3) {
                val split = code.split(Regex("[, ，]"))
                return "\u001B[38;2;${split[0].trim()};${split[1].trim()};${split[2].trim()}m"
            }
            AnsiBukkitColorCode.entries.find {
                it.name.lowercase() == code.lowercase() || it.alias.toString().lowercase() == code.lowercase()
            }?.also {
                return it.transform()
            }
            AnsiBukkitFormatCode.entries.find {
                it.name.lowercase() == code.lowercase() || it.alias.toString().lowercase() == code.lowercase()
            }?.also {
                return it.transform()
            }
            customMap[code]?.also { return it.transform() }
            customMap.values.find { it.alias.toString().lowercase() == code.lowercase() }
                ?.also { return it.transform() }
            return code
        }

        @JvmStatic
        fun parse(code: Char): String {
            return parse(code.toString())
        }

    }
}

enum class AnsiBukkitColorCode(val red: Int, val green: Int, val blue: Int, override val alias: Char? = null) :
    AnsiBukkitCode {
    GREEN(85, 255, 85, 'a'),
    RED(255, 85, 85, 'c'),
    BLUE(85, 255, 255, 'b'),
    LightPurple(255, 85, 255, 'd'),
    YELLOW(255, 255, 85, 'e'),
    WHITE(255, 255, 255, 'f'),
    DARKGRAY(85, 85, 85, '8'),
    GRAY(170, 170, 170, '7'),
    GOLD(255, 170, 0, '6'),
    DarkPurple(170, 0, 170, '5'),
    DarkRed(170, 0, 0, '4'),
    DarkAqua(0, 170, 170, '3'),
    DarkGreen(0, 170, 0, '2'),
    Black(0, 0, 0, '0')
    ;


    override fun transform(): String {
        return "\u001B[38;2;$red;$green;${blue}m"
    }


}

enum class AnsiBukkitFormatCode(val code: String, override val alias: Char?) : AnsiBukkitCode {
    // 重置样式
    RESET("0", 'r'),

    // 加粗
    BOLD("1", 'l'),

    // 斜体
    ITALIC("3", 'o'),

    // 删除线
    STRIKETHROUGH("9", 'm'),

    // 下划线
    UNDERLINE("4", 'n')
    ;

    override fun transform(): String {
        return "\u001B[${code}m"
    }


}

fun String.consoleColored(): String {
    return BukkitColorAnsiBuilder.colored(this)
}

fun println(message: Any) {
    System.out.println(message.toString().consoleColored())
}

fun print(message: Any) {
    System.out.print(message.toString().consoleColored())
}

fun println() {
    System.out.println()
}