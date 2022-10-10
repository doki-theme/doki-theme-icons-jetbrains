package io.unthrottled.doki.icons.jetbrains.shared.svg

import com.intellij.openapi.Disposable
import com.intellij.openapi.actionSystem.impl.ActionToolbarImpl
import com.intellij.openapi.application.ApplicationManager
import com.intellij.util.SVGLoader
import io.unthrottled.doki.icons.jetbrains.shared.themes.DokiThemePayload
import io.unthrottled.doki.icons.jetbrains.shared.themes.IconThemeManager
import io.unthrottled.doki.icons.jetbrains.shared.themes.ThemeManagerListener
import javax.swing.SwingUtilities

class ThemedSVGManager : ThemeManagerListener, Disposable {
  companion object {
    val instance: ThemedSVGManager =
      ApplicationManager.getApplication().getService(ThemedSVGManager::class.java)
  }

  private val connection = ApplicationManager.getApplication().messageBus.connect()

  init {
    connection.subscribe(IconThemeManager.TOPIC, this)
  }

  fun initialize() {
    IconThemeManager.instance.userSetTheme
      .ifPresent {
        activateTheme(it)
      }
  }

  private fun activateTheme(currentTheme: DokiThemePayload) {
    SVGLoader.colorPatcherProvider =
      ComposedSVGColorizerProviderFactory.createForTheme(currentTheme)
    SwingUtilities.invokeLater { ActionToolbarImpl.updateAllToolbarsImmediately() }
  }

  override fun dispose() {
    connection.dispose()
  }

  override fun onDokiThemeActivated(dokiThemePayload: DokiThemePayload) {
    activateTheme(dokiThemePayload)
  }

  override fun onDokiThemeRemoved() {}
}
