plugins {
    kotlin("jvm") version "1.9.10"
    id("net.minecraftforge.gradle") version "6.0.24"
}

version = "1.0.0"
group = "com.mryarg1ch.storylineofmineyarg"
base.archivesName.set("storylineofmineyarg")

val mcVersion = "1.20.1"
val forgeVersion = "47.2.0"

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
    minecraft("net.minecraftforge:forge:${mcVersion}-${forgeVersion}")
    implementation("thedarkcolour:kotlinforforge:4.4.0")
    
    // Дополнительные зависимости для GUI и конфигурации
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
}

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