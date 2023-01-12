import org.jetbrains.changelog.markdownToHTML

fun properties(key: String) = project.findProperty(key).toString()

plugins {
  // Custom plugin for building all the themes
  id("doki-theme-plugin")
  // Java support
  id("java")
  // Kotlin support
  kotlin("jvm") version "1.8.0"
  // Gradle IntelliJ Plugin
  id("org.jetbrains.intellij") version "1.11.0"
  // Gradle Changelog Plugin
  id("org.jetbrains.changelog") version "2.0.0"
  // Gradle Qodana Plugin
  id("org.jetbrains.qodana") version "0.1.13"
  // detekt linter - read more: https://detekt.github.io/detekt/gradle.html
  id("io.gitlab.arturbosch.detekt") version "1.22.0"
  // ktlint linter - read more: https://github.com/JLLeitschuh/ktlint-gradle
  id("org.jlleitschuh.gradle.ktlint") version "11.0.0"
}

group = properties("pluginGroup")
version = properties("pluginVersion")

// Configure project's dependencies
repositories {
  mavenCentral()
}

dependencies {
  implementation(project(":shared"))
  runtimeOnly(project(":rider"))
}

allprojects {
  apply {
    plugin("java")
    plugin("org.jetbrains.kotlin.jvm")
    plugin("org.jetbrains.intellij")
    plugin("io.gitlab.arturbosch.detekt")
    plugin("org.jlleitschuh.gradle.ktlint")
  }

  dependencies {
    implementation("org.javassist:javassist:3.29.2-GA")
    testImplementation("org.assertj:assertj-core:3.24.1")
    testImplementation("io.mockk:mockk:1.13.3")
    testImplementation("org.junit.jupiter:junit-jupiter:5.9.2")
  }

  repositories {
    mavenCentral()
  }

  // Set the JVM language level used to compile sources and generate files - Java 11 is required since 2020.3
  kotlin {
    jvmToolchain {
      languageVersion.set(JavaLanguageVersion.of(17))
    }
  }

// Configure detekt plugin.
// Read more: https://detekt.github.io/detekt/kotlindsl.html
  detekt {
    config = files("./detekt-config.yml")
    buildUponDefaultConfig = true
    autoCorrect = true

    reports {
      html.enabled = false
      xml.enabled = false
      txt.enabled = false
    }
  }

  tasks {
    buildSearchableOptions {
      enabled = false
    }
  }
}

// Configure Gradle IntelliJ Plugin - read more: https://plugins.jetbrains.com/docs/intellij/tools-gradle-intellij-plugin.html
intellij {
  pluginName.set(properties("pluginName"))
  version.set(properties("platformVersion"))
  type.set(properties("platformType"))

  // Plugin Dependencies. Uses `platformPlugins` property from the gradle.properties file.
  val activePlugins: MutableList<Any> = properties("platformPlugins").split(',').map(String::trim).filter(String::isNotEmpty).toMutableList()

//  activePlugins.add(
//    project(":doki-theme")
//  )

  plugins.set(activePlugins)
}

// Configure Gradle Changelog Plugin - read more: https://github.com/JetBrains/gradle-changelog-plugin
changelog {
  version.set(properties("pluginVersion"))
  groups.set(emptyList())
}

// Configure Gradle Qodana Plugin - read more: https://github.com/JetBrains/gradle-qodana-plugin
qodana {
  cachePath.set(projectDir.resolve(".qodana").canonicalPath)
  reportPath.set(projectDir.resolve("build/reports/inspections").canonicalPath)
  saveReport.set(true)
  showReport.set(System.getenv("QODANA_SHOW_REPORT")?.toBoolean() ?: false)
}

tasks {
  wrapper {
    gradleVersion = properties("gradleVersion")
  }

  compileKotlin {
    dependsOn("buildThemes")
  }

  patchPluginXml {
    version.set(properties("pluginVersion"))
    sinceBuild.set(properties("pluginSinceBuild"))
    untilBuild.set(properties("pluginUntilBuild"))

    // Extract the <!-- Plugin description --> section from README.md and provide for the plugin's manifest
    pluginDescription.set(
      projectDir.resolve("README.md").readText().lines().run {
        val start = "<!-- Plugin description -->"
        val end = "<!-- Plugin description end -->"

        if (!containsAll(listOf(start, end))) {
          throw GradleException("Plugin description section not found in README.md:\n$start ... $end")
        }
        subList(indexOf(start) + 1, indexOf(end))
      }.joinToString("\n").run { markdownToHTML(this) }
    )

    changeNotes.set(
      projectDir.resolve("RELEASE-NOTES.md").readText().run { markdownToHTML(this) }
    )
  }

  runIde {
    maxHeapSize = "2g"
    systemProperty("idea.ui.icons.svg.disk.cache", "false")
    val idePath = properties("idePath")
    if (idePath.isNotEmpty()) {
      ideDir.set(file(idePath))
    }
  }

  // Configure UI tests plugin
  // Read more: https://github.com/JetBrains/intellij-ui-test-robot
  runIdeForUiTests {
    systemProperty("robot-server.port", "8082")
    systemProperty("ide.mac.message.dialogs.as.sheets", "false")
    systemProperty("jb.privacy.policy.text", "<!--999.999-->")
    systemProperty("jb.consents.confirmation.enabled", "false")
  }

  signPlugin {
    certificateChain.set(System.getenv("CERTIFICATE_CHAIN"))
    privateKey.set(System.getenv("PRIVATE_KEY"))
    password.set(System.getenv("PRIVATE_KEY_PASSWORD"))
  }

  publishPlugin {
    dependsOn("patchChangelog")
    token.set(System.getenv("PUBLISH_TOKEN"))
    // pluginVersion is based on the SemVer (https://semver.org) and supports pre-release labels, like 2.1.7-alpha.3
    // Specify pre-release label to publish the plugin in a custom Release Channel automatically. Read more:
    // https://plugins.jetbrains.com/docs/intellij/deployment.html#specifying-a-release-channel
    channels.set(listOf(properties("pluginVersion").split('-').getOrElse(1) { "default" }.split('.').first()))
  }
}
