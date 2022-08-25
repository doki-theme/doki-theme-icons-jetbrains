plugins {
  `kotlin-dsl`
}

repositories {
  mavenLocal()
  mavenCentral()
}

dependencies {
  implementation("org.jsoup:jsoup:1.13.1")
  implementation("io.unthrottled.doki.build.jvm:doki-build-source-jvm:88.0.1")
}
