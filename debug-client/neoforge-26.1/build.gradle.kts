plugins {
  alias(libs.plugins.neoforge.moddev)
}

evaluationDependsOn(":packages:neoforge-26.1")

val targetMinecraftVersion = "26.1"
val neoForgeVersion = "26.1.0.19-beta"
val jcefGithubVersion = libs.versions.jcefgithub.get()
val grapheneProject = project(":packages:neoforge-26.1")
val grapheneMainSourceSet = grapheneProject.extensions.getByType<SourceSetContainer>().named("main")
val resourceProperties =
    mapOf(
        "version" to project.version.toString(),
        "minecraftVersion" to targetMinecraftVersion,
        "neoForgeVersion" to neoForgeVersion,
    )

base {
  archivesName = "graphene-debug-$targetMinecraftVersion"
}

neoForge {
  version = neoForgeVersion

  runs {
    register("debugClient") {
      client()
      ideName.set("Graphene Debug Client $targetMinecraftVersion")
      gameDirectory.set(layout.projectDirectory.dir("run/client"))
    }
  }

  mods {
    register("grapheneui") {
      sourceSet(grapheneMainSourceSet.get())
    }
    register("grapheneui-debug") {
      sourceSet(sourceSets.main.get())
    }
  }
}

dependencies {
  implementation(project(":packages:neoforge-26.1"))
  implementation(project(":packages:common"))
  implementation(libs.gson)
  runtimeOnly("io.github.trethore:jcefgithub:${jcefGithubVersion}:all-relocated") {
    isTransitive = false
  }
}

tasks.processResources {
  from(rootProject.file("debug-client/shared/resources"))
  inputs.properties(resourceProperties)
  filesMatching("META-INF/neoforge.mods.toml") {
    expand(resourceProperties)
  }
}

tasks.withType<JavaCompile>().configureEach {
  options.release = 25
}

java {
  toolchain {
    languageVersion = JavaLanguageVersion.of(25)
  }
  sourceCompatibility = JavaVersion.VERSION_25
  targetCompatibility = JavaVersion.VERSION_25
}
