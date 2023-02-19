package io.unthrottled.doki.icons.jetbrains.shared

data class DokiThemeInformation(
  val id: String,
  val name: String,
  val displayName: String,
  val group: String,
  val listName: String,
  val colors: Map<String, String>
)
