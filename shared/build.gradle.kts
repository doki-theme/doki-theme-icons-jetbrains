fun properties(key: String) = project.findProperty(key).toString()

plugins {
  kotlin("jvm")
}

intellij {
  version.set(properties("platformVersion"))
  type.set(properties("platformType"))
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
