import io.izzel.taboolib.gradle.APPLICATION
import io.izzel.taboolib.gradle.CONFIGURATION
import io.izzel.taboolib.gradle.EXPANSION_COMMAND_HELPER

plugins {
    java
    id("io.izzel.taboolib") version "2.0.11"
    kotlin("jvm") version "1.9.21"
}

group = "cn.souts"
version = "1.3"

taboolib {
    env {
        install(APPLICATION)
        install(CONFIGURATION)
        install(EXPANSION_COMMAND_HELPER)
        // 依赖下载目录
        fileLibs = "libraries"
        // 资源下载目录
        fileAssets = "assets"
    }
    version {
        taboolib = "6.1.1-beta21"
        skipKotlinRelocate = true
        skipTabooLibRelocate = true
    }

    classifier = null
}


repositories {
    mavenCentral()
}

dependencies {
    taboo("org.fusesource.jansi:jansi:2.4.1")
    taboo("org.freemarker:freemarker:2.3.32")
    taboo("org.jetbrains.kotlin:kotlin-stdlib-jdk8:1.9.21")

//    implementation("cn.hutool:hutool-all:5.8.26")

    testImplementation("org.jetbrains.kotlin:kotlin-test")
}


tasks.jar {
    manifest {
        attributes("Main-Class" to "cn.souts.taboolibinitializr.Main")
        attributes("Description" to "TabooLib模板生成器")
    }
}


tasks.test {
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(8)
}

