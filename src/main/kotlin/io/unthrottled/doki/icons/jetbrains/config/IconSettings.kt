package io.unthrottled.doki.icons.jetbrains.config

import com.intellij.openapi.ui.ComboBox
import io.unthrottled.doki.icons.jetbrains.themes.DokiTheme
import io.unthrottled.doki.icons.jetbrains.themes.IconThemeManager
import java.util.Vector
import javax.swing.DefaultComboBoxModel

class ThemeComboItem(private val dokiTheme: DokiTheme) {
  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (javaClass != other?.javaClass) return false

    other as ThemeComboItem

    if (dokiTheme.id != other.dokiTheme.id) return false

    return true
  }

  override fun hashCode(): Int {
    return dokiTheme.id.hashCode()
  }

  val id: String
    get() = dokiTheme.id

  override fun toString(): String {
    return dokiTheme.listName
  }
}

data class IconSettingsModel(
  var isUIIcons: Boolean,
  var isFileIcons: Boolean,
  var isPSIIcons: Boolean,
  var isFolderIcons: Boolean,
  var currentThemeId: String,
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
      currentThemeId = IconThemeManager.instance.currentTheme.map { it.id }.orElseGet { IconThemeManager.DEFAULT_THEME_ID }
    )

  fun createThemeComboBoxModel(settingsSupplier: () -> IconSettingsModel): ComboBox<ThemeComboItem> {
    val themeList = IconThemeManager.instance.allThemes
      .sortedBy { theme -> theme.listName }
      .map { ThemeComboItem(it) }
    val themeComboBox = ComboBox(
      DefaultComboBoxModel(
        Vector(
          themeList
        )
      )
    )

    themeComboBox.model.selectedItem = themeList.find {
      it.id == settingsSupplier().currentThemeId
    }

    themeComboBox.addActionListener {
      settingsSupplier().currentThemeId = (themeComboBox.model.selectedItem as ThemeComboItem).id
    }
    return themeComboBox
  }
}
