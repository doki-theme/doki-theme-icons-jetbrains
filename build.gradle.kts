import org.jetbrains.intellij.platform.gradle.TestFrameworkType

System.setProperty("HACK", "false")

plugins {
  // Custom plugin for building all the themes
  id("doki-theme-plugin")
  id("java") // Java support
  alias(libs.plugins.kotlin) // Kotlin support
  alias(libs.plugins.intelliJPlatform) // IntelliJ Platform Gradle Plugin
}

group = providers.gradleProperty("pluginGroup").get()
version = providers.gradleProperty("pluginVersion").get()

// Set the JVM language level used to build the project.
kotlin {
  jvmToolchain(21)
}

// Configure project's dependencies
repositories {
  mavenCentral()

  // IntelliJ Platform Gradle Plugin Repositories Extension - read more: https://plugins.jetbrains.com/docs/intellij/tools-intellij-platform-gradle-plugin-repositories-extension.html
  intellijPlatform {
    defaultRepositories()
  }
}

// Dependencies are managed with Gradle version catalog - read more: https://docs.gradle.org/current/userguide/platforms.html#sub:version-catalog
dependencies {
  implementation("commons-io:commons-io:2.15.1")
  implementation("org.javassist:javassist:3.29.2-GA")
  implementation("io.sentry:sentry:6.28.0")
  testImplementation("org.assertj:assertj-core:3.25.3")
  testImplementation("io.mockk:mockk:1.13.8")
  testImplementation(libs.junit)
  testImplementation(libs.opentest4j)

  // IntelliJ Platform Gradle Plugin Dependencies Extension - read more: https://plugins.jetbrains.com/docs/intellij/tools-intellij-platform-gradle-plugin-dependencies-extension.html
  intellijPlatform {
    create(providers.gradleProperty("platformType"), providers.gradleProperty("platformVersion"))

    // Plugin Dependencies. Uses `platformBundledPlugins` property from the gradle.properties file for bundled IntelliJ Platform plugins.
    bundledPlugins(providers.gradleProperty("platformBundledPlugins").map { it.split(',') })

    // Plugin Dependencies. Uses `platformPlugins` property from the gradle.properties file for plugin from JetBrains Marketplace.
    plugins(providers.gradleProperty("platformPlugins").map { it.split(',') })

    testFramework(TestFrameworkType.Platform)
  }
}

configurations {
  implementation.configure {
    // sentry brings in a slf4j that breaks when
    // with the platform slf4j
    exclude("org.slf4j")
  }
}

// Configure IntelliJ Platform Gradle Plugin - read more: https://plugins.jetbrains.com/docs/intellij/tools-intellij-platform-gradle-plugin-extension.html
intellijPlatform {
  pluginConfiguration {
    name = providers.gradleProperty("pluginName")
    version = providers.gradleProperty("pluginVersion")

    // Extract the <!-- Plugin description --> section from README.md and provide for the plugin's manifest
//    description = providers.fileContents(layout.projectDirectory.file("README.md")).asText.map {
//      val start = "<!-- Plugin description -->"
//      val end = "<!-- Plugin description end -->"
//
//      with(it.lines()) {
//        if (!containsAll(listOf(start, end))) {
//          throw GradleException("Plugin description section not found in README.md:\n$start ... $end")
//        }
//        subList(indexOf(start) + 1, indexOf(end)).joinToString("\n").let(::markdownToHTML)
//      }
//    }

//    val changelog = project.changelog // local variable for configuration cache compatibility
//    // Get the latest available change notes from the changelog file
//    changeNotes = providers.gradleProperty("pluginVersion").map { pluginVersion ->
//      with(changelog) {
//        renderItem(
//          (getOrNull(pluginVersion) ?: getUnreleased())
//            .withHeader(false)
//            .withEmptySections(false),
//          Changelog.OutputType.HTML,
//        )
//      }
//    }

    ideaVersion {
      sinceBuild = providers.gradleProperty("pluginSinceBuild")
      untilBuild = providers.gradleProperty("pluginUntilBuild")
    }
  }

  signing {
    certificateChain = providers.environmentVariable("CERTIFICATE_CHAIN")
    privateKey = providers.environmentVariable("PRIVATE_KEY")
    password = providers.environmentVariable("PRIVATE_KEY_PASSWORD")
  }

  publishing {
    token = providers.environmentVariable("PUBLISH_TOKEN")
    // The pluginVersion is based on the SemVer (https://semver.org) and supports pre-release labels, like 2.1.7-alpha.3
    // Specify pre-release label to publish the plugin in a custom Release Channel automatically. Read more:
    // https://plugins.jetbrains.com/docs/intellij/deployment.html#specifying-a-release-channel
    channels = providers.gradleProperty("pluginVersion").map { listOf(it.substringAfter('-', "").substringBefore('.').ifEmpty { "default" }) }
  }

  pluginVerification {
    ides {
      recommended()
    }
  }
}

tasks {
  wrapper {
    gradleVersion = providers.gradleProperty("gradleVersion").get()
  }

  buildSearchableOptions {
    environment["DOKI_HACK"] = "false"
  }

  patchPluginXml {
    dependsOn("buildThemes")
  }
//  publishPlugin {
//    dependsOn(patchChangelog)
//  }
}

intellijPlatformTesting {
  runIde {
//    maxHeapSize = "2g"
//    systemProperty("idea.ui.icons.svg.disk.cache", "false")
    register("runIdeForUiTests") {
      task {
        jvmArgumentProviders += CommandLineArgumentProvider {
          listOf(
            "-Drobot-server.port=8082",
            "-Dide.mac.message.dialogs.as.sheets=false",
            "-Djb.privacy.policy.text=<!--999.999-->",
            "-Djb.consents.confirmation.enabled=false",
          )
        }
      }

      plugins {
        robotServerPlugin()
      }
    }
  }
}