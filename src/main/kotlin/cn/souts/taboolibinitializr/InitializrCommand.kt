package cn.souts.taboolibinitializr

import cn.souts.taboolibinitializr.utils.InitializrUtil
import cn.souts.taboolibinitializr.utils.InitializrUtil.moduleList
import cn.souts.taboolibinitializr.utils.consoleColored
import taboolib.common.platform.ProxyCommandSender
import taboolib.common.platform.command.command
import taboolib.common.platform.command.component.CommandComponent
import taboolib.common.platform.command.component.CommandComponentDynamic
import taboolib.common.platform.command.component.CommandComponentLiteral
import taboolib.common.platform.command.suggest
import taboolib.common5.cint
import java.io.File

object InitializrCommand {

    private val map = mutableMapOf<String, SettingTemplate>()

    fun register() {
        command("taboolib", listOf("taboo", "tb"), description = "taboolib模板生成器命令") {
            createHelperColored()
            this.literal("help", "?", "h") {
                execute<ProxyCommandSender> { sender, context, argument ->
                    createHelperColored()
                }
            }
            this.literal("create") {
                dynamic("name") {
                    execute<ProxyCommandSender> { sender, context, argument ->
                        if (map.containsKey(argument)) {
                            sender.sendMessageColored("&f[&c!&f] &c当前预设项目已存在,如需覆盖请在结尾加入 &6force &c参数.")
                        } else {
                            createNewSetTemplate(context["name"], sender)
                        }
                    }
                    literal("force") {
                        execute<ProxyCommandSender> { sender, context, argument ->
                            createNewSetTemplate(context["name"], sender)
                        }
                    }
                    dynamic("模板") {
                        execute<ProxyCommandSender> { sender, context, argument ->
                            val settingTemplate = InitializrUtil.settingTemplate[argument]
                            if (settingTemplate == null) {
                                sender.sendMessageColored("&f[&a!&f] $argument 模板不存在,请重新选择")
                                return@execute
                            }
                            map[context["name"]] = settingTemplate
                            sender.sendMessageColored("&f[&a+&f] &a [${context["name"]}] 预设项目创建完成,输入 &btb edit ${context["name"]} &a命令进行下一步编辑")
                        }
                    }
                }
            }
            literal("edit") {
                dynamic("name") {
                    suggest {
                        map.keys.map { it }
                    }
                    literal("group") {
                        dynamic("group") {
                            execute<ProxyCommandSender> { sender, context, argument ->
                                map[context["name"]]?.group = argument
                                sender.sendMessageColored("group已改为 $argument")
                            }
                        }
                    }
                    literal("env") {
                        execute<ProxyCommandSender> { sender, context, argument ->
                            map[context["name"]]!!.env.also { env ->
                                if (env == null) {
                                    sender.sendMessageColored("env为空")
                                } else {
                                    sender.sendMessageColored(env.toString())
                                }
                            }
                        }
                        literal("list") {
                            execute<ProxyCommandSender> { sender, context, argument ->
                                val moduleList = moduleList
                                sender.sendMessageColored("&f[&a!&f] 模块列表 ─┐")
                                moduleList.forEachIndexed { index, module ->
                                    sender.sendMessageColored("&f[&a$index&f] 模块名:${module.name} 模块描述:${module.desc} 模块依赖:${module.dependency}")
                                }
                            }
                        }
                        literal("add") {
                            dynamic("modules") {
                                execute<ProxyCommandSender> { sender, context, argument ->
                                    val split = argument.split(" ")
                                    val add = mutableListOf<Module>()
                                    split.forEach { indexStr ->
                                        moduleList.getOrNull(indexStr.cint).also {
                                            if (it == null) {
                                                sender.sendMessageColored("&f[&c!&] 没有找到索引为 $indexStr 的模块")
                                            }
                                            add.add(it!!)
                                        }
                                    }
                                    map[context["name"]]!!.also {
                                        if (it.env == null) {
                                            it.env = Env(add)
                                        } else {
                                            if (it.env!!.modules == null) {
                                                it.env!!.modules = add
                                            } else {
                                                it.env!!.modules!!.addAll(add)
                                            }
                                        }
                                    }
                                    sender.sendMessageColored("&f[&a+&f] &a成功向预设项目 ${context["name"]} 添加了 ${add.map { it.name }} 模块")
                                    sender.sendMessageColored("&f[&a*&f] &a当前 ${context["name"]} 项目中所有模块为: ${map[context["name"]]?.env?.modules?.map { it.name }}")
                                }
                            }

                        }
                    }


                }
            }
            literal("remove") {
                dynamic("name") {
                    execute<ProxyCommandSender> { sender, context, argument ->
                        map.remove(argument)
                        sender.sendMessageColored("&f[&c-&f] &a$argument 已删除.")
                    }
                }
            }
            literal("write") {
                dynamic("name") {
                    dynamic("输出路径") {
                        execute<ProxyCommandSender> { sender, context, argument ->
                            map[context["name"]].let {
                                if (it == null) {
                                    sender.sendMessageColored("预设项目名称输入错误")
                                    return@execute
                                }
                                InitializrUtil.writeFile(it, File(argument))
                            }
                            sender.sendMessageColored("&a写出成功!")
                            map.remove(context["name"])
                        }
                    }
                    execute<ProxyCommandSender> { sender, context, argument ->
                        map[context["name"]].let {
                            if (it == null) {
                                sender.sendMessageColored("预设项目名称输入错误")
                                return@execute
                            }
                            InitializrUtil.writeFile(it, File("./out"))
                        }
                        sender.sendMessageColored("&a写出成功!")
                        map.remove(context["name"])
                    }
                }

            }
        }
    }


    private fun createNewSetTemplate(name: String, sender: ProxyCommandSender) {
        map[name] = SettingTemplate(name = name)
        sender.sendMessageColored("&f[&+&f] &a [$name] 预设项目创建完成,输入 &btb edit $name &a命令进行下一步编辑")
    }

    fun ProxyCommandSender.sendMessageColored(msg: String) {
        sendMessage(msg.consoleColored())
    }

    fun CommandComponent.createHelperColored(checkPermissions: Boolean = true) {
        execute<ProxyCommandSender> { sender, context, _ ->
            val command = context.command
            val builder = StringBuilder("§cUsage: /${command.name}")
            var newline = false

            fun check(children: List<CommandComponent>): List<CommandComponent> {
                // 检查权限
                val filterChildren = if (checkPermissions) {
                    children.filter { sender.hasPermission(it.permission) }
                } else {
                    children
                }
                // 过滤隐藏
                return filterChildren.filter { it !is CommandComponentLiteral || !it.hidden }
            }

            fun space(space: Int): String {
                return (1..space).joinToString("") { " " }
            }

            fun print(
                compound: CommandComponent,
                index: Int,
                size: Int,
                offset: Int = 8,
                level: Int = 0,
                end: Boolean = false,
                optional: Boolean = false
            ) {
                var option = optional
                var comment = 0
                when (compound) {
                    is CommandComponentLiteral -> {
                        if (size == 1) {
                            builder.append(" ").append("§c${compound.aliases[0]}")
                        } else {
                            newline = true
                            builder.appendLine()
                            builder.append(space(offset))
                            if (level > 1) {
                                builder.append(if (end) " " else "§7│")
                            }
                            builder.append(space(level))
                            if (index + 1 < size) {
                                builder.append("§7├── ")
                            } else {
                                builder.append("§7└── ")
                            }
                            builder.append("§c${compound.aliases[0]}")
                        }
                        option = false
                        comment = compound.aliases[0].length
                    }

                    is CommandComponentDynamic -> {
                        val value = compound.comment
                        comment = if (compound.optional || option) {
                            option = true
                            builder.append(" ").append("§8[<$value>]")
                            compound.comment.length + 4
                        } else {
                            builder.append(" ").append("§7<$value>")
                            compound.comment.length + 2
                        }
                    }
                }
                if (level > 0) {
                    comment += 1
                }
                val checkedChildren = check(compound.children)
                checkedChildren.forEachIndexed { i, children ->
                    // 因 literal 产生新的行
                    if (newline) {
                        print(children, i, checkedChildren.size, offset, level + comment, end, option)
                    } else {
                        val length = if (offset == 8) command.name.length + 1 else comment + 1
                        print(children, i, checkedChildren.size, offset + length, level, end, option)
                    }
                }
            }

            val checkedChildren = check(context.commandCompound.children)
            val size = checkedChildren.size
            checkedChildren.forEachIndexed { index, children ->
                print(children, index, size, end = index + 1 == size)
            }
            builder.lines().forEach {
                sender.sendMessageColored(it.replace("§", "&"))
            }
        }

    }
}