plugins {
  `kotlin-dsl`
}

repositories {
  mavenLocal()
  mavenCentral()
}

dependencies {
  implementation(group = "com.google.code.gson", name = "gson", version = "2.7")
  implementation("org.jsoup:jsoup:1.13.1")
  implementation("io.unthrottled.doki.build.jvm:doki-build-source-jvm:88.0.1")
}
