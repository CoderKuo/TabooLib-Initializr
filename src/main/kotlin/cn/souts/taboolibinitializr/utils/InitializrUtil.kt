package cn.souts.taboolibinitializr.utils

import cn.souts.taboolibinitializr.Initializr
import cn.souts.taboolibinitializr.Module
import cn.souts.taboolibinitializr.SettingTemplate
import freemarker.template.Configuration
import freemarker.template.Template
import taboolib.module.configuration.ConfigSection
import taboolib.module.configuration.Configuration.Companion.getObject
import java.awt.Desktop
import java.io.*
import java.nio.file.Files
import java.nio.file.StandardCopyOption


object InitializrUtil {

    val moduleList = getModuleListFromMapping()

    val settingTemplate = getSettingTemplateFromConfig()

    fun writeFile(settingTemplate: SettingTemplate, outFile: File) {
        val out = File(outFile, "${settingTemplate.name}").also {
            if (!it.exists()) {
                it.mkdirs()
            }
        }
        startWriteFromTemplate(settingTemplate, out)
        writeGradleWrapper(File(out, "gradle/wrapper/gradle-wrapper.jar"))

        openDir(outFile)
    }

    private fun openDir(folder: File) {
        if (!Desktop.isDesktopSupported() || !Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
            return
        }

        val desktop = Desktop.getDesktop()
        try {
            desktop.open(folder)
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    private fun writeGradleWrapper(outFile: File) {
        if (!outFile.exists()) {
            if (!outFile.parentFile.exists()) {
                outFile.parentFile.mkdirs()
            }
            try {
                Files.copy(Initializr.wrapperFile.toPath(), outFile.toPath(), StandardCopyOption.REPLACE_EXISTING)
            } catch (e: IOException) {
                e.printStackTrace()
                println("复制文件时发生错误: ${e.message}")
            }
        }
    }

    private fun startWriteFromTemplate(settingTemplate: SettingTemplate, outFile: File) {
        val templateFile = File("./template")
        val configuration = Configuration()
        configuration.setDirectoryForTemplateLoading(templateFile)
        configuration.defaultEncoding = "utf-8"

        val stringTemplateLoaderConfiguration = Configuration()
        stringTemplateLoaderConfiguration.templateLoader = StringTemplateLoader()
        stringTemplateLoaderConfiguration.defaultEncoding = "utf-8"
        Initializr.templates.forEach {
            val template = stringTemplateLoaderConfiguration.getTemplate(it.path)
            val stw = StringWriter()
            template.process(
                mapOf(
                    "name" to settingTemplate.name, "group" to settingTemplate.group?.replace(".", "/")
                ), stw
            )
            stw.flush()
            stw.close()
            writeFTL(settingTemplate, it, File(outFile, stw.toString()), configuration)
        }
    }

    private fun writeFTL(
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

    private fun getModuleListFromMapping(): MutableList<Module> {
        val list = mutableListOf<Module>()
        Initializr.moduleMapping.onEachIndexed { index, (key, module) ->
            list.add(index, module)
        }
        return list
    }


    private fun getSettingTemplateFromConfig(): Map<String, SettingTemplate> {
        val settingTemplates = Initializr.config.get("setting-template") as ConfigSection
        return settingTemplates.getKeys(false).map { key ->
            key to settingTemplates.getObject<SettingTemplate>(key)
        }.toMap()
    }

}