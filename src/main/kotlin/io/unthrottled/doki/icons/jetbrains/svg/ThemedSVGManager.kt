package io.unthrottled.doki.icons.jetbrains.svg

import com.intellij.openapi.Disposable
import com.intellij.openapi.application.ApplicationManager
import com.intellij.util.SVGLoader
import io.unthrottled.doki.icons.jetbrains.themes.DokiTheme
import io.unthrottled.doki.icons.jetbrains.themes.IconThemeManager
import io.unthrottled.doki.icons.jetbrains.themes.ThemeManagerListener

class ThemedSVGManager : ThemeManagerListener, Disposable {
  companion object {
    val instance: ThemedSVGManager =
      ApplicationManager.getApplication().getService(ThemedSVGManager::class.java)
  }

  private val connection = ApplicationManager.getApplication().messageBus.connect()

  fun initialize() {
    IconThemeManager.instance.currentTheme
      .ifPresent {
        activateTheme(it)
      }
  }

  private fun activateTheme(currentTheme: DokiTheme) {
    SVGLoader.setColorPatcherProvider(
      SVGColorizerProviderFactory.createForTheme(currentTheme)
    )
  }

  override fun dispose() {
    connection.dispose()
  }

  override fun onDokiThemeActivated(dokiTheme: DokiTheme) {
    activateTheme(dokiTheme)
  }

  override fun onDokiThemeRemoved() {}
}
