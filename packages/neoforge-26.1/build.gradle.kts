import org.gradle.api.publish.tasks.GenerateModuleMetadata
import org.gradle.api.tasks.Sync
import org.gradle.api.tasks.bundling.AbstractArchiveTask

plugins {
  alias(libs.plugins.neoforge.moddev)
  id("io.github.trethore.architecture-check")
  `maven-publish`
  signing
}

val minecraftVersion = "26.1"
val neoForgeVersion = "26.1.0.19-beta"
val jcefGithubVersion = libs.versions.jcefgithub.get()
val mavenCentralSigningKey = providers.environmentVariable("MAVEN_GPG_PRIVATE_KEY")
val mavenCentralSigningPassphrase = providers.environmentVariable("MAVEN_GPG_PASSPHRASE")
val isMavenCentralPublishRequested =
    gradle.startParameter.taskNames.any { taskName ->
      taskName.contains("MavenCentralBundle", ignoreCase = true)
    }

base {
  archivesName = rootProject.name
}

neoForge {
  version = neoForgeVersion
  addModdingDependenciesTo(sourceSets.test.get())

  runs {
    register("client") {
      client()
      ideName.set("Minecraft Client 26.1")
      gameDirectory.set(layout.projectDirectory.dir("run/client"))
    }

    register("server") {
      server()
      ideName.set("Minecraft Server 26.1")
      gameDirectory.set(layout.projectDirectory.dir("run/server"))
    }
  }

  mods {
    register("grapheneui") {
      sourceSet(sourceSets.main.get())
    }
  }
}

dependencies {
  implementation(project(":packages:common"))
  jarJar(project(":packages:common"))

  jarJar("io.github.trethore:jcefgithub:${jcefGithubVersion}:all-relocated") {
    isTransitive = false
  }
}

architectureChecks {
  register("jcefIsolation") {
    sources.from(fileTree("src") { include("**/*.java") })
    forbiddenImports.addAll(
        "org.cef.",
        "io.github.trethore.jcefgithub.",
    )
    failureMessage.set("NeoForge code must not access JCEF directly; use the common API instead.")
  }
}

tasks.processResources {
  val version = project.version.toString()
  val properties =
      mapOf(
          "version" to version,
          "minecraftVersion" to minecraftVersion,
          "neoForgeVersion" to neoForgeVersion,
      )
  inputs.properties(properties)

  filesMatching("META-INF/neoforge.mods.toml") {
    expand(properties)
  }
}

tasks.withType<JavaCompile>().configureEach {
  options.release = 25
}

java {
  toolchain {
    languageVersion = JavaLanguageVersion.of(25)
  }
  withSourcesJar()
  withJavadocJar()

  sourceCompatibility = JavaVersion.VERSION_25
  targetCompatibility = JavaVersion.VERSION_25
}

tasks.named<Jar>("sourcesJar") {
  from(project(":packages:common").file("src/main/java"))
  from(project(":packages:common").file("src/main/resources"))
}

tasks.jar {
  val projectName = rootProject.name
  inputs.property("projectName", projectName)
  archiveFileName.set("${rootProject.name}-${project.version}-neoforge-${minecraftVersion}.jar")

  from(rootProject.file("LICENSE")) {
    rename { "${it}_$projectName" }
  }
}

tasks.register<Sync>("stageGithubRelease") {
  group = "distribution"
  description = "Stages the runtime JAR for a GitHub release."

  val jar = tasks.named<AbstractArchiveTask>("jar")

  dependsOn(jar)
  from(jar.flatMap { it.archiveFile })
  into(layout.buildDirectory.dir("github-release"))
}

publishing {
  publications {
    register<MavenPublication>("mavenJava") {
      artifactId = "graphene-ui-neoforge-$minecraftVersion"
      from(components["java"])

      pom {
        name.set("Graphene UI")
        description.set("Client-side Chromium-based UI library for Minecraft NeoForge mods.")
        url.set("https://github.com/trethore/graphene")

        licenses {
          license {
            name.set("MIT License")
            url.set("https://github.com/trethore/graphene/blob/main/LICENSE")
          }
        }

        developers {
          developer {
            id.set("trethore")
            name.set("Titouan Rethore")
            email.set("titou.rethore@gmail.com")
          }
          developer {
            id.set("diena1dev")
            name.set("diena1dev")
            email.set("diena@diena.dev")
          }
        }

        scm {
          connection.set("scm:git:git://github.com/trethore/graphene.git")
          developerConnection.set("scm:git:ssh://git@github.com/trethore/graphene.git")
          url.set("https://github.com/trethore/graphene")
        }

        withXml {
          val dependencies = asNode().get("dependencies") as groovy.util.NodeList
          dependencies
              .flatMap { (it as groovy.util.Node).children() }
              .filterIsInstance<groovy.util.Node>()
              .filter { dependency ->
                val groupId = dependency.get("groupId") as groovy.util.NodeList
                val artifactId = dependency.get("artifactId") as groovy.util.NodeList
                groupId.text() == project.group.toString() && artifactId.text() == "common"
              }
              .forEach { dependency -> dependency.parent().remove(dependency) }
        }
      }
    }
  }

  repositories {
    maven {
      name = "MavenCentralBundle"
      url = rootProject.layout.buildDirectory.dir("central-portal/staging").get().asFile.toURI()
    }
    maven {
      name = "GitHubPackages"
      url = uri("https://maven.pkg.github.com/trethore/graphene")
      credentials {
        username = providers.environmentVariable("GITHUB_ACTOR").orNull
        password = providers.environmentVariable("GITHUB_TOKEN").orNull
      }
    }
  }
}

tasks.withType<GenerateModuleMetadata>().configureEach {
  enabled = false
}

if (isMavenCentralPublishRequested) {
  check(!project.version.toString().endsWith("-SNAPSHOT")) {
    "Maven Central publishing requires a non-SNAPSHOT release version"
  }
  check(mavenCentralSigningKey.isPresent) {
    "Maven Central publishing requires MAVEN_GPG_PRIVATE_KEY"
  }
  check(mavenCentralSigningPassphrase.isPresent) {
    "Maven Central publishing requires MAVEN_GPG_PASSPHRASE"
  }
}

signing {
  setRequired { isMavenCentralPublishRequested }
  if (mavenCentralSigningKey.isPresent && mavenCentralSigningPassphrase.isPresent) {
    useInMemoryPgpKeys(mavenCentralSigningKey.get(), mavenCentralSigningPassphrase.get())
    sign(publishing.publications["mavenJava"])
  }
}
