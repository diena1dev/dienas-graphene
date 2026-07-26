pluginManagement {
  includeBuild("build-logic")

  repositories {
    maven {
      name = "Fabric"
      url = uri("https://maven.fabricmc.net/")
    }
    mavenCentral()
    gradlePluginPortal()
  }
}

// Gradle project name
rootProject.name = "graphene"

include("packages:common")

// fabric versions
include("packages:fabric-26.2")
include("packages:fabric-1.21.11")

include("tools:debug-client-fabric-26.2")
include("tools:debug-client-fabric-1.21.11")

// neoforge versions
include("packages:neoforge-26.2")
include("packages:neoforge-26.1")

include("tools:debug-client-neoforge-26.2")
include("tools:debug-client-neoforge-26.1")

// tools
project(":tools").projectDir = file("debug-client")

project(":tools:debug-client-fabric-26.2").projectDir = file("debug-client/fabric-26.2")
project(":tools:debug-client-fabric-1.21.11").projectDir = file("debug-client/fabric-1.21.11")

project(":tools:debug-client-neoforge-26.2").projectDir = file("debug-client/neoforge-26.2")
project(":tools:debug-client-neoforge-26.1").projectDir = file("debug-client/neoforge-26.1")
