import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
	kotlin("jvm") version "1.6.0"
	kotlin("plugin.serialization") version "1.6.0"
	id("fabric-loom") version "0.12-SNAPSHOT"
	`idea`
}

object Versions {
	const val Minecraft = "1.19.1"
	object Jvm {
		val Java = JavaVersion.VERSION_17
		const val Kotlin = "1.7.10"
		const val TargetKotlin = "17"
	}
	object Fabric {
		const val Yarn = "1.19.1+build.5"
		const val Loader = "0.14.8"
		const val Api = "0.58.5+1.19.1"
	}
	object Mod {
		const val Group = "io.ejekta"
		const val ID = "bountiful"
		const val Version = "3.0.0"
	}
	object Env {
		const val Kambrik = "4.0-1.19.1-SNAPSHOT+"
		const val FLK = "1.8.2+kotlin.1.7.10"
		const val ClothConfig = "8.0.75"
		const val ModMenu = "4.0.5"
	}
}


java {
	sourceCompatibility = Versions.Jvm.Java
	targetCompatibility = Versions.Jvm.Java
	withSourcesJar()
	withJavadocJar()
}

project.group = Versions.Mod.Group
version = Versions.Mod.Version

repositories {
	mavenLocal()
	mavenCentral()
	//maven(url = "https://kotlin.bintray.com/kotlinx")
	maven(url = "https://maven.shedaniel.me/")
	maven(url = "https://maven.terraformersmc.com/") {
		name = "Mod Menu"
	}
}

dependencies {
	//to change the versions see the gradle.properties file
	minecraft("com.mojang:minecraft:${Versions.Minecraft}")
	mappings("net.fabricmc:yarn:${Versions.Fabric.Yarn}:v2")
	modImplementation("net.fabricmc:fabric-loader:${Versions.Fabric.Loader}")

	// Kambrik API
	modImplementation("io.ejekta:kambrik:${Versions.Env.Kambrik}")

	modApi("me.shedaniel.cloth:cloth-config-fabric:${Versions.Env.ClothConfig}") {
		exclude(group = "net.fabricmc.fabric-api")
	}

	implementation("com.google.code.findbugs:jsr305:3.0.2")

	modApi("com.terraformersmc:modmenu:${Versions.Env.ModMenu}") {
		exclude(module = "fabric-api")
		exclude(module = "config-2")
	}

	modImplementation(group = "net.fabricmc", name = "fabric-language-kotlin", version = Versions.Env.FLK)

	// Fabric API. This is technically optional, but you probably want it anyway.
	modImplementation("net.fabricmc.fabric-api:fabric-api:${Versions.Fabric.Api}")
}

tasks.getByName<ProcessResources>("processResources") {
	filesMatching("fabric.mod.json") {
		expand(
			mutableMapOf<String, String>(
				"modid" to Versions.Mod.ID,
				"version" to Versions.Mod.Version,
				"kotlinVersion" to Versions.Jvm.Kotlin,
				"fabricApiVersion" to Versions.Fabric.Api
			)
		)
	}
}

tasks.withType<KotlinCompile> {
	kotlinOptions {
		jvmTarget = Versions.Jvm.TargetKotlin
	}
}
