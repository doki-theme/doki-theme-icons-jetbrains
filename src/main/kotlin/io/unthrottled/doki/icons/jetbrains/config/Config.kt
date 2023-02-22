package io.unthrottled.doki.icons.jetbrains.config

import com.intellij.openapi.application.Application
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import com.intellij.util.xmlb.XmlSerializerUtil.copyBean
import com.intellij.util.xmlb.XmlSerializerUtil.createCopy
import io.unthrottled.doki.icons.jetbrains.themes.IconThemeManager

fun Application.getConfig(): Config = this.getService(Config::class.java)

@State(
  name = "doki-theme-icon-config",
  storages = [Storage("doki-theme-icons.xml")]
)
class Config : PersistentStateComponent<Config>, Cloneable {
  companion object {
    @JvmStatic
    val instance: Config
      get() = ApplicationManager.getApplication().getService(Config::class.java)
  }

  var userId: String = ""
  var version: String = ""
  var currentThemeId: String = IconThemeManager.DEFAULT_THEME_ID
  var isUIIcons: Boolean = true
  var isNamedFileIcons: Boolean = true
  var isGlyphIcon: Boolean = true
  var isNamedFolderIcons: Boolean = true
  var isMyIcons: Boolean = false
  var syncWithDokiTheme: Boolean = true

  override fun getState(): Config? =
    createCopy(this)

  override fun loadState(state: Config) {
    copyBean(state, this)
  }
}
