package io.unthrottled.doki.icons.jetbrains.config

data class IconSettingsModel(
  var isUIIcons: Boolean,
  var isFileIcons: Boolean,
  var isPSIIcons: Boolean,
  var isFolderIcons: Boolean,
)

object IconSettings {
  const val SETTINGS_ID = "io.unthrottled.doki.icons.jetbrains.settings.ThemeSettings"
  const val ICON_SETTINGS_DISPLAY_NAME = "Doki Theme Icons Settings"

  @JvmStatic
  fun createSettingsModule(): IconSettingsModel =
    IconSettingsModel(
      isUIIcons = false,
      isFileIcons = false,
      isPSIIcons = false,
      isFolderIcons = false,
    )
}
