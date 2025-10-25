plugins {
    kotlin("jvm") version "1.9.10"
    id("net.minecraftforge.gradle") version "6.0.24"
}

version = "1.1.1"
group = "com.mryarg1ch.storylineofmineyarg"
base.archivesName.set("storylineofmineyarg")

val mcVersion = "1.20.1"
val forgeVersion = "47.4.0"

java.toolchain.languageVersion.set(JavaLanguageVersion.of(17))

println("Java: ${System.getProperty("java.version")}, JVM: ${System.getProperty("java.vm.version")} (${System.getProperty("java.vendor")}), Arch: ${System.getProperty("os.arch")}")

minecraft {
    mappings("official", mcVersion)
    
    runs {
        create("client") {
            workingDirectory(project.file("run"))
            property("forge.logging.markers", "REGISTRIES")
            property("forge.logging.console.level", "debug")
            property("forge.enabledGameTestNamespaces", "storylineofmineyarg")
            mods {
                create("storylineofmineyarg") {
                    source(sourceSets.main.get())
                }
            }
        }

        create("server") {
            workingDirectory(project.file("run"))
            property("forge.logging.markers", "REGISTRIES")
            property("forge.logging.console.level", "debug")
            property("forge.enabledGameTestNamespaces", "storylineofmineyarg")
            mods {
                create("storylineofmineyarg") {
                    source(sourceSets.main.get())
                }
            }
        }

        create("gameTestServer") {
            workingDirectory(project.file("run"))
            property("forge.logging.markers", "REGISTRIES")
            property("forge.logging.console.level", "debug")
            property("forge.enabledGameTestNamespaces", "storylineofmineyarg")
            mods {
                create("storylineofmineyarg") {
                    source(sourceSets.main.get())
                }
            }
        }

        create("data") {
            workingDirectory(project.file("run"))
            property("forge.logging.markers", "REGISTRIES")
            property("forge.logging.console.level", "debug")
            args("--mod", "storylineofmineyarg", "--all", "--output", file("src/generated/resources/"), "--existing", file("src/main/resources/"))
            mods {
                create("storylineofmineyarg") {
                    source(sourceSets.main.get())
                }
            }
        }
    }
}

sourceSets.main.configure { resources.srcDirs("src/generated/resources/") }

repositories {
    maven {
        name = "Kotlin for Forge"
        setUrl("https://thedarkcolour.github.io/KotlinForForge/")
    }
}

dependencies {
    minecraft("net.minecraftforge:forge:${mcVersion}-${forgeVersion}") {
        exclude(group = "org.apache.logging.log4j", module = "log4j-api")
        exclude(group = "org.apache.logging.log4j", module = "log4j-core")
        exclude(group = "org.apache.logging.log4j", module = "log4j-slf4j-impl")
        exclude(group = "org.apache.logging.log4j", module = "log4j-slf4j18-impl")
        exclude(group = "org.apache.logging.log4j", module = "log4j-jul")
        exclude(group = "org.apache.logging.log4j", module = "log4j-jcl")
        exclude(group = "org.apache.logging.log4j", module = "log4j-1.2-api")
    }
    implementation("thedarkcolour:kotlinforforge:4.4.0") {
        exclude(group = "org.apache.logging.log4j", module = "log4j-api")
        exclude(group = "org.apache.logging.log4j", module = "log4j-core")
        exclude(group = "org.apache.logging.log4j", module = "log4j-slf4j-impl")
        exclude(group = "org.apache.logging.log4j", module = "log4j-slf4j18-impl")
    }
    
    // Дополнительные зависимости для GUI и конфигурации
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
}

// Using Minecraft's built-in logging system

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    kotlinOptions {
        jvmTarget = "17"
        freeCompilerArgs = listOf("-Xjvm-default=all")
    }
}

tasks.jar {
    manifest {
        attributes(mapOf(   
            "Specification-Title" to "Storyline Of Mineyarg",
            "Specification-Vendor" to "MrYArg1cH",
            "Specification-Version" to "1",
            "Implementation-Title" to project.name,
            "Implementation-Version" to project.version,
            "Implementation-Vendor" to "MrYArg1cH"
        ))
    }
}