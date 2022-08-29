fun properties(key: String) = project.findProperty(key).toString()

plugins {
  kotlin("jvm")
}

dependencies {
  implementation(project(":shared"))
}

intellij {
  version.set(properties("riderVersion"))
  type.set("RD")
  downloadSources.set(false)
  instrumentCode.set(false)
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
