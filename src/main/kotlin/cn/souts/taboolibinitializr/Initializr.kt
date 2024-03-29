package cn.souts.taboolibinitializr

import cn.souts.taboolibinitializr.utils.println
import org.fusesource.jansi.Ansi
import org.fusesource.jansi.AnsiConsole
import taboolib.common.LifeCycle
import taboolib.common.platform.Awake
import taboolib.module.configuration.Config
import taboolib.module.configuration.Configuration
import java.io.*
import java.net.URL
import java.nio.file.Files
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException
import java.util.*


object Initializr {

    @Config
    lateinit var config: Configuration
    val moduleMapping: LinkedHashMap<String, Module> = LinkedHashMap()
    val templates = mutableListOf<Template>()
    val wrapperFile = File("./template/gradle-wrapper.jar")


    fun start() {
        val startTime = System.currentTimeMillis()
        AnsiConsole.systemInstall()
        printBanner()

        loadConfig()
        loadMapping()
        loadTemplate()
        if (!wrapperFile.exists()) {
            downloadFile(URL(config.getString("wrapperDownloadFile")), wrapperFile)
        }

        InitializrCommand.register()



        println("&f[&a✓&f] &a所有设置已完成,启动共耗时: &6${System.currentTimeMillis() - startTime} &ams.")
    }

    @Awake(LifeCycle.DISABLE)
    fun disable() {
        AnsiConsole.systemUninstall()
    }

    private fun loadTemplate() {
        println("&f[&a*&f] &6加载模板文件中...")
        val remoteURL = config.getString("templateDownloadURL")
        val file = File("./template").also {
            if (!it.exists()) {
                it.mkdirs()
            }
        }
        val templates = (config.get("templates") as List<Map<String, Any>>)
        val total = templates.size
        var index = 1
        templates.map {
            Template(it.get("name").toString(), it.get("path").toString())
        }.map {
            val templateFile = File(file, it.name)
            val thisFileURL = URL(remoteURL.plus("/${it.name}"))
            if (templateFile.exists()) {
                if (config.getBoolean("verify") && verify(templateFile, URL(thisFileURL.toString().plus(".sha1")))) {
                    println("&f[&6!&f] &a检测到 ${it.name} 文件有更新,正在更新...")
                    downloadFile(thisFileURL, templateFile)
                }
            } else {
                println("&f[&6!&f] &a检测到 ${it.name} 文件未下载,正在下载...")
                downloadFile(thisFileURL, templateFile)
            }
            println("&f[&a+&f] &b${it.name} &a模板加载成功. &f(&a${index++}&f/&a$total&f)")
            it
        }.forEach {
            this.templates.add(it)
        }
        println("&f[&a*&f] &a共加载了 ${templates.size} 个模板文件.")
    }

    private fun verify(file: File, url: URL): Boolean {
        val openStream = url.openConnection().getInputStream()
        val readFully = readFully(openStream).toString(Charsets.UTF_8).substring(0, 40)
        openStream.close()
        val hash = getHash(file)
        return readFully != hash
    }

    private fun downloadFile(url: URL, out: File) {
        out.parentFile.mkdirs()
        val ins = url.openStream()
        val outs: OutputStream = Files.newOutputStream(out.toPath())
        val buffer = ByteArray(4096)
        var len: Int
        while ((ins.read(buffer).also { len = it }) > 0) {
            outs.write(buffer, 0, len)
        }
        outs.close()
        ins.close()
    }

    private fun readFully(inputStream: InputStream): ByteArray {
        val stream = ByteArrayOutputStream()
        val buf = ByteArray(1024)
        var len: Int
        while ((inputStream.read(buf).also { len = it }) > 0) {
            stream.write(buf, 0, len)
        }
        return stream.toByteArray()
    }

    private fun getHash(file: File): String {
        try {
            val digest = MessageDigest.getInstance("sha-1")
            Files.newInputStream(file.toPath()).use { inputStream ->
                val buffer = ByteArray(1024)
                var total: Int
                while ((inputStream.read(buffer).also { total = it }) != -1) {
                    digest.update(buffer, 0, total)
                }
            }
            val result = StringBuilder()
            for (b in digest.digest()) {
                result.append(String.format("%02x", b))
            }
            return result.toString()
        } catch (ex: IOException) {
            ex.printStackTrace()
        } catch (ex: NoSuchAlgorithmException) {
            ex.printStackTrace()
        }
        return ("null (" + UUID.randomUUID()).toString() + ")"
    }

    private fun loadMapping() {
        println("&f[&a*&f] &6正在加载模块文件...")

        val file = File("./module-mapping.yml")
        if (!file.exists()) {
            println("&f[&c!&f] &c没有找到模块文件,开始自动创建模块文件...")
            file.createNewFile()
            this::class.java.classLoader.getResource("module-mapping.yml").readBytes().also {
                file.writeBytes(it)
            }
            println("&f[&a+&f] &a模块文件创建成功!")
        }
        val configuration = Configuration.loadFromFile(file)
        val modules = configuration.getMapList("modules")
        modules.forEach {
            it.let {
                Module(it.get("name") as? String, it.get("desc") as? String, it.get("dependency") as? List<String>)
            }.also {
                moduleMapping[it.name!!] = it
            }
        }


        println("&f[&a+&f] ✓ &a模块文件加载成功 共加载了 &6${moduleMapping.size} &a个模块.")
    }

    private fun loadConfig() {
        println("&f[&a*&f] &6正在加载配置文件...")
        val file = File("./config.yml")
        if (!file.exists()) {
            println("&f[&c!&f] &c没有找到配置文件,开始自动创建配置文件...")
            file.createNewFile()
            this::class.java.classLoader.getResource("config.yml").readBytes().also {
                file.writeBytes(it)
            }
            println("&f[&a+&f] &a配置文件创建成功!")
        }
        config = Configuration.loadFromFile(file)
        println("&f[&a+&f] &a配置文件加载成功 ✓")
    }


    private fun printBanner() {
        println(Ansi.ansi().eraseScreen())
        println("&a欢迎使用TabooLib-Initializr")
        println("")
        println("")
    }

}