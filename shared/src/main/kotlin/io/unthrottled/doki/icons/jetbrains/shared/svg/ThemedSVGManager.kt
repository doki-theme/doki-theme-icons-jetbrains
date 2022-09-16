package io.unthrottled.doki.icons.jetbrains.shared.svg

import com.intellij.openapi.Disposable
import com.intellij.openapi.application.ApplicationManager
import com.intellij.util.SVGLoader
import io.unthrottled.doki.icons.jetbrains.shared.themes.DokiTheme
import io.unthrottled.doki.icons.jetbrains.shared.themes.IconThemeManager
import io.unthrottled.doki.icons.jetbrains.shared.themes.ThemeManagerListener

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
    SVGLoader.colorPatcherProvider =
      ComposedSVGColorizerProviderFactory.createForTheme(currentTheme)
  }

  override fun dispose() {
    connection.dispose()
  }

  override fun onDokiThemeActivated(dokiTheme: DokiTheme) {
    activateTheme(dokiTheme)
  }

  override fun onDokiThemeRemoved() {}
}
