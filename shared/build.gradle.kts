fun properties(key: String) = project.findProperty(key).toString()

plugins {
  kotlin("jvm")
}

dependencies {
  implementation("commons-io:commons-io:2.11.0")
  implementation("io.sentry:sentry:6.11.0")
}

configurations {
  implementation.configure {
    // sentry brings in a slf4j that breaks when
    // with the platform slf4j
    exclude("org.slf4j")
  }
}

intellij {
  version.set(properties("platformVersion"))
  type.set(properties("platformType"))
}

detekt {
  config = files("../detekt-config.yml")
}

tasks {
  verifyPlugin {
    enabled = false
  }

  publishPlugin {
    enabled = false
  }

  runIde {
    enabled = false
  }
}
