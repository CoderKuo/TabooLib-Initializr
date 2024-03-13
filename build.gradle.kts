plugins {
    java
    id("com.github.johnrengelman.shadow") version "8.1.1"
    kotlin("jvm") version "1.9.21"
}

group = "cn.souts"
version = "1.0"


repositories {
    mavenCentral()
}

dependencies {
    implementation("org.yaml:snakeyaml:2.2")
    implementation("org.fusesource.jansi:jansi:2.4.1")
    implementation("org.freemarker:freemarker:2.3.32")
    implementation("cn.hutool:hutool-all:5.8.26")

    implementation("com.google.code.gson:gson:2.9.0")

    testImplementation("org.jetbrains.kotlin:kotlin-test")
}

tasks.shadowJar {
    destinationDirectory.set(File("./out"))
    archiveBaseName = "TabooLibInitializr"
    archiveClassifier = ""
    archiveVersion = "$version"
    manifest {
        attributes("Main-Class" to "cn.souts.taboolibinitializr.Initializr")
        attributes("Description" to "TabooLib模板生成器")
    }

}


tasks.test {
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(8)
}

