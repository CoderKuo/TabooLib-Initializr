package cn.souts.taboolibinitializr

import cn.hutool.core.collection.CollUtil
import cn.hutool.core.io.FileUtil
import cn.hutool.core.lang.Dict
import cn.hutool.core.util.StrUtil.splitToInt
import cn.hutool.extra.template.TemplateConfig
import cn.hutool.extra.template.TemplateUtil
import cn.souts.taboolibinitializr.utils.print
import cn.souts.taboolibinitializr.utils.println
import freemarker.template.Configuration
import freemarker.template.Template
import java.io.File
import java.io.FileWriter
import java.io.Writer
import java.util.*


class InitializrService {

    val scanner = Scanner(System.`in`)


    fun start() {
        println("&f[&a✓&f] &a设置已完成,准备开始生成文件")

        val genSettingTemplate = genSettingTemplate()
        writeFile(genSettingTemplate)

        println("&f[&a✓&f] &a成功!!")

    }

    fun writeFile(settingTemplate: SettingTemplate) {
        val out = File("./out/${settingTemplate.name}").also {
            if (!it.exists()) {
                it.mkdirs()
            }
        }
        startWriteFromTemplate(settingTemplate, out)
        writeGradleWrapper(File(out, "gradle/wrapper/gradle-wrapper.jar"))
    }

    fun writeGradleWrapper(outFile: File) {
        if (!outFile.exists()) {
            FileUtil.copy(Initializr.wrapperFile, outFile, false)
        }
    }

    fun startWriteFromTemplate(settingTemplate: SettingTemplate, outFile: File) {
        val templateFile = File("./template")
        val configuration = Configuration()
        configuration.setDirectoryForTemplateLoading(templateFile)
        configuration.defaultEncoding = "utf-8"
        val engine = TemplateUtil.createEngine(TemplateConfig())
        Initializr.templates.forEach {
            val template = engine.getTemplate(it.path)
            val path = template.render(Dict.create().apply {
                put("name", settingTemplate.name)
                put("group", settingTemplate.group?.replace(".", "/"))
            })
            writeFTL(settingTemplate, it, File(outFile, path), configuration)
        }
    }

    fun writeFTL(
        settingTemplate: SettingTemplate,
        template: cn.souts.taboolibinitializr.Template,
        outFile: File,
        configuration: Configuration
    ) {
        val freemarkerTemplate: Template = configuration.getTemplate(template.name)
        val outFile = if (outFile.toString().last() != '/' && outFile.toString().last() != '.') {
            outFile.also {
                if (!it.parentFile.exists()) {
                    it.parentFile.mkdirs()
                }
            }
        } else {
            File(outFile.toString().removeSuffix("."), template.name!!.removeSuffix(".ftl")).apply {
                if (!parentFile.exists()) {
                    parentFile.mkdirs()
                }
            }
        }
        val out: Writer = FileWriter(outFile)
        freemarkerTemplate.process(settingTemplate, out)
        out.flush()
        out.close()
    }

    fun genSettingTemplate(): SettingTemplate {
        print("&f[&c?&f] &a是否要使用设置模板(&6config.yml&a) &f(&ayes&f/&cno&f) &a: ")
        val next = scanner.next()
        val settingTemplate = if (next.matches(Regex("^([Yy][Ee][Ss]|[Yy])\$"))) {
            println("&f[&a*&f] &a请选择你要使用的模板: ")
            selectSettingTemplate()
        } else if (next.matches(Regex("^([Nn][Oo]|[Nn])\$"))) {
            println("&f[&a*&f] &a选择了不使用设置模板.")
            SettingTemplate()
        } else {
            selectSettingTemplate()
        }

        insertOtherSet(settingTemplate)

        return settingTemplate
    }

    fun insertOtherSet(template: SettingTemplate): SettingTemplate {
        if (template.name.isNullOrEmpty()) {
            print("&f[&a*&f] &a请输入项目名: ")
            template.name = scanner.next()
        }
        if (template.group.isNullOrEmpty()) {
            print("&f[&a*&f] &a请输入组名: ")
            template.group = scanner.next()
        }
        if (template.description == null) {
            print("&f[&6!&f] &6项目描述未配置,是否需要配置? &f(&ayes&f/&cno&f) &a: ")
            val next = scanner.next()
            val description = if (next.matches(Regex("^([Yy][Ee][Ss]|[Yy])\$"))) {
                insertDesc()
            } else if (next.matches(Regex("^([Nn][Oo]|[Nn])\$"))) {
                Description()
            } else {
                Description()
            }
            template.description = description
        }
        if (template.env == null || template.env?.modules?.size == 0) {
            print("&f[&6!&f] &6项目环境(ENV)未配置,是否需要配置? &f(&ayes&f/&cno&f) &a: ")
            val next = scanner.next()
            val env = if (next.matches(Regex("^([Yy][Ee][Ss]|[Yy])\$"))) {
                insertEnv(template.env)
            } else if (next.matches(Regex("^([Nn][Oo]|[Nn])\$"))) {
                Env()
            } else {
                Env()
            }
            template.env = env
        }

        print("&f[&6?&f] &6请问是否需要继续添加模块: &f(&ayes&f/&cany&f) ")
        if (scanner.next().matches(Regex("^([Yy][Ee][Ss]|[Yy])\$"))) {
            template.env = insertEnv(template.env)
        }

        return template
    }

    fun insertEnv(env: Env?): Env {
        val env = env ?: Env()
        println("&f[&a!&f] 选择模块开始 ─┐")
        val list = mutableListOf<Module>()
        Initializr.moduleMapping.onEachIndexed { index, (key, module) ->
            list.add(index, module)
            println("&f[&a$index&f] 模块名:${module.name} 模块描述:${module.desc} 模块依赖:${module.dependency}")
        }
        print("&f[&a!&f] 请输入你要选择的模块前的索引数字(多个模块之间使用,分割): ")
        val next = scanner.next()
        val splitToInt = splitToInt(next, ',')
        val any = CollUtil.getAny(list, *splitToInt)
        println("&f[&a*&f] 以下是你选择的模块名: [${any.map { it.name }}]")
        if (env.modules == null) {
            env.modules = any
        } else {
            env.modules!!.addAll(any)
        }
        print("&f[&6?&f] &6请问是否需要继续添加: &f(&ayes&f/&cany&f) ")
        if (next.matches(Regex("^([Yy][Ee][Ss]|[Yy])\$"))) {
            insertEnv(env)
        }
        return env
    }

    fun insertDesc(): Description {
        val description = Description()
        print("&f[&a*&f] &a请输入描述名: ")
        description.name = scanner.next()
        println("&f[&a+&f] &a 项目描述配置完成.")
        return description
    }

    private fun selectSettingTemplate(): SettingTemplate {
        val settingTemplates = Initializr.config.get("setting-template") as Map<String, Any>
        val list = mutableListOf<SettingTemplate>()
        var index = 0
        settingTemplates.forEach { (key, u) ->
            val template = Dict(u as Map<String, Any>).toBean(SettingTemplate::class.java)
            if (u.keys.contains("env")) {
                (u.get("env") as Map<String, Any>).let {
                    it.get("modules") as? List<Map<String, Any>>
                }?.map {
                    Module(it.get("name") as? String, it.get("desc") as? String)
                }?.also {
                    if (template.env == null) {
                        template.env = Env(it.toMutableList())
                    } else {
                        template.env!!.modules = it.toMutableList()
                    }
                }
            } else {
                template.env = null
            }

            if (u.keys.contains("description")) {
                (u.get("description") as Map<String, Any>).also {
                    (it.get("contributors") as? List<Map<String, Any>>)?.map {
                        Contributor(it.get("name") as? String, it.get("description") as? String)
                    }?.also {
                        if (template.description == null) {
                            template.description = Description(it)
                        } else {
                            template.description!!.contributors = it
                        }
                    }
                    (it.get("links") as? List<Map<String, Any>>)?.map {
                        Link(it.get("name") as? String, it.get("url") as? String)
                    }?.also {
                        if (template.description == null) {
                            template.description = Description(links = it)
                        } else {
                            template.description!!.links = it
                        }
                    }
                }
            }

            list.add(template)
            println("&f[&c${index++}&f] &a模板名称: &b$key")
        }
        print("&f[&6?&f] &a请选择你要使用的模板(请输入前面的索引数字): ")
        return scanner.nextInt().let { list.get(it) }
    }

}