package io.unthrottled.doki.icons.jetbrains.svg

import com.intellij.openapi.Disposable
import com.intellij.openapi.actionSystem.impl.ActionToolbarImpl
import com.intellij.openapi.application.ApplicationManager
import com.intellij.ui.svg.setSelectionColorPatcherProvider
import com.intellij.util.SVGLoader
import io.unthrottled.doki.icons.jetbrains.themes.DokiThemePayload
import io.unthrottled.doki.icons.jetbrains.themes.IconThemeManager
import io.unthrottled.doki.icons.jetbrains.themes.ThemeManagerListener
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
    IconThemeManager.instance.currentTheme
      .ifPresent {
        activateTheme(it)
      }
  }

  private fun activateTheme(currentTheme: DokiThemePayload) {
    val svgPatcherProvider = ComposedSVGColorizerProviderFactory.createForTheme(currentTheme)
    SVGLoader.colorPatcherProvider =
      svgPatcherProvider
    setSelectionColorPatcherProvider(svgPatcherProvider)
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
