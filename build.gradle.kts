plugins {
    id("java-library")
    id("com.gradleup.shadow") version "9.3.0"
    id("xyz.jpenilla.run-paper") version "3.0.2"
}

repositories {
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/")
    maven("https://jitpack.io")
    maven("https://repo.skriptlang.org/releases")
}

dependencies {
    implementation("org.bstats:bstats-bukkit:3.2.1")
    compileOnly("io.papermc.paper:paper-api:1.21-R0.1-SNAPSHOT")
    implementation("org.eclipse.jgit:org.eclipse.jgit:6.8.0.202311291450-r")
    compileOnly("com.github.SkriptLang:Skript:2.15.0")
    implementation("com.github.ShaneBeee:SkriptRegistration:1.5.0")
}

java {
    toolchain.languageVersion = JavaLanguageVersion.of(25)
}

tasks {
    runServer {
        minecraftVersion("1.21.11")
        runDirectory(file("run/1.21.11"))
        jvmArgs("-Xms2G", "-Xmx2G")
    }

    register<xyz.jpenilla.runpaper.task.RunServer>("runServer26_3") {
        minecraftVersion("26.3")
        runDirectory(file("run/26.3"))
        jvmArgs("-Xms2G", "-Xmx2G")
        pluginJars.from(shadowJar.flatMap { it.archiveFile })
    }

    processResources {
        val props = mapOf(
            "version" to project.version.toString(),
            "description" to project.description.toString()
        )
        inputs.properties(props)
        filteringCharset = "UTF-8"
        filesMatching("plugin.yml") {
            expand(props)
        }
    }

    shadowJar {
        relocate("com.github.shanebeee.skr", "io.github.ryanpeng69.skgit.libs.skr")
        relocate("org.eclipse.jgit", "io.github.ryanpeng69.skgit.libs.jgit")
        relocate("org.bstats", "io.github.ryanpeng69.skgit.libs.bstats")
    }
}