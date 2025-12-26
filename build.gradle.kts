plugins {
	id("fabric-loom") version "1.14-SNAPSHOT"
	id("maven-publish")
	id("me.modmuss50.mod-publish-plugin") version "1.1.0"
}

version = "${project.property("mod_version")}+${stonecutter.current.version}"
group = project.property("maven_group")!!

base {
	archivesName.set(project.property("archives_base_name") as String)
}

loom {
	splitEnvironmentSourceSets()

	mods {
		create("redstonetools", Action {
			sourceSet(sourceSets.main.get())
			sourceSet("client")
		})
	}
}

stonecutter {
	swaps["mouse_clicked_params"] = if (current.parsed < "1.21.10") "double mouseX, double mouseY, int button" else "Click click, boolean doubleClick" // mouseClicked
	swaps["mouse_clicked_args"] = if (current.parsed < "1.21.10") "mouseX, mouseY, button" else "click, doubleClick"
	swaps["on_mouse_clicked_args"] = if (current.parsed < "1.21.10") "(int) mouseX, (int) mouseY, button" else "click, doubleClick" // onMouseClicked
	swaps["dragged_released_params"] = if (current.parsed < "1.21.10") "double mouseX, double mouseY, int button" else "Click click" // mouseDragged, mouseReleased
	swaps["dragged_released_args"] = if (current.parsed < "1.21.10") "mouseX, mouseY, button" else "click"
	swaps["on_released_args"] = if (current.parsed < "1.21.10") "(int) mouseX, (int) mouseY, button" else "click" // onMouseReleased
	swaps["keyinput_params"] = if (current.parsed < "1.21.10") "int keyCode, int scanCode, int modifiers" else "KeyInput input" // keyPressed, keyReleased
	swaps["keyinput_args"] = if (current.parsed < "1.21.10") "keyCode, scanCode, modifiers" else "input"
	swaps["charinput_params"] = if (current.parsed < "1.21.10") "char chr, int modifiers" else "CharInput input" // charTyped
	swaps["charinput_args"] = if (current.parsed < "1.21.10") "chr, modifiers" else "input"
	swaps["click_and_inputs_imports"] = if (current.parsed < "1.21.10") "" else
		"//\nimport net.minecraft.client.gui.Click;\nimport net.minecraft.client.input.KeyInput;\nimport net.minecraft.client.input.CharInput;"

	swaps["get_x"] = if (current.parsed < "1.21.10") "x" else "getX()"
	swaps["get_y"] = if (current.parsed < "1.21.10") "y" else "getY()"
	swaps["get_width"] = if (current.parsed < "1.21.10") "entryWidth" else "getWidth()"
	swaps["permission_level_2"] =
		if (current.parsed < "1.21.11")
			"source -> source.hasPermissionLevel(2)"
		else
			"CommandManager.requirePermissionLevel(new PermissionCheck.Require(DefaultPermissions.GAMEMASTERS))"
}

repositories {
	exclusiveContent {
		forRepository {
			maven {
				name = "Modrinth"
				url = uri("https://api.modrinth.com/maven")
			}
		}
		filter {
			includeGroup("maven.modrinth")
		}
	}
	maven {
		name = "WorldEdit Maven"
		url = uri("https://maven.enginehub.org/repo/")
	}
}

dependencies {
	minecraft("com.mojang:minecraft:${project.property("minecraft_version")}")
	mappings("net.fabricmc:yarn:${project.property("yarn_mappings")}:v2")
	modImplementation("net.fabricmc:fabric-loader:${project.property("loader_version")}")
	modImplementation("net.fabricmc.fabric-api:fabric-api:${project.property("fabric_version")}")
	modImplementation("com.sk89q.worldedit:worldedit-fabric-mc${project.property("worldedit_version")}")
	modApi("maven.modrinth:malilib:${project.property("malilib_version")}")
}

loom {
	decompilerOptions.named("vineflower") {
		options.put("mark-corresponding-synthetics", "1")
	}

	runConfigs.configureEach {
		ideConfigGenerated(true)
		runDir = "../../run"
	}
}

tasks.processResources {
	val properties = mapOf(
		"version" to project.version,
		"minecraft_version" to project.property("minecraft_version"),
		"minecraft_version_out" to project.property("minecraft_version_out"),
		"malilib_version" to project.property("minecraft_version_out"),
		"loader_version" to project.property("loader_version")
	)
	properties.forEach { inputs.property(it.key, it.value) }
	filteringCharset = "UTF-8"

	filesMatching("fabric.mod.json") {
		expand(properties)
	}
}

tasks.register<DefaultTask>("collectFile") {
	group = "build"
	mustRunAfter("build")

	doLast {
		copy {
			from(
				file(
					"build/libs/${project.property("archives_base_name")}-${project.property("mod_version")}+${project.property("minecraft_version")}.jar"
				)
			)
			into(rootProject.file("build/libs"))
		}
	}
}

tasks.register<DefaultTask>("buildAndCollect") {
	group = "build"
	dependsOn(tasks.named("build"), tasks.named("collectFile"))
}

tasks.withType<JavaCompile>().configureEach {
	options.encoding = "UTF-8"
	options.release = 21
}

java {
	toolchain.languageVersion = JavaLanguageVersion.of(21)
	withSourcesJar()
}

tasks.jar {
	from("LICENSE") {
		rename { "${it}_${project.property("archives_base_name")}" }
	}
}

publishing {
	publications {
		create<MavenPublication>("mavenJava") {
			artifactId = project.property("archives_base_name") as String
			from(components["java"])
		}
	}
	repositories {
	}
}

publishMods {
	file.set(tasks.remapJar.get().archiveFile)
	type.set(STABLE)
	modLoaders.add("fabric")

	changelog.set("")
	displayName.set(project.property("mod_version") as String)

	if (providers.environmentVariable("RELEASE_MODRINTH").orNull?.toBoolean() ?: false) {
		modrinth {
			accessToken.set(providers.environmentVariable("MODRINTH_TOKEN"))
			projectId.set("9ySQVrz2")
			minecraftVersions.add(project.property("minecraft_version") as String)

			requires("fabric-api")
			requires("malilib")
			optional("worldedit")
		}
	}

	if (providers.environmentVariable("RELEASE_GITHUB").orNull?.toBoolean() ?: false) {
		github {
			accessToken.set(providers.environmentVariable("GITHUB_TOKEN"))
			parent(rootProject.tasks.named("publishGithub"))
		}
	}
}
