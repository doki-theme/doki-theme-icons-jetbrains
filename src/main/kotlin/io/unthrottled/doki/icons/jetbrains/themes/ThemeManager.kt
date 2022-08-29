package io.unthrottled.doki.icons.jetbrains.themes

import com.google.gson.reflect.TypeToken
import com.intellij.ide.ui.laf.LafManagerImpl
import com.intellij.ide.ui.laf.UIThemeBasedLookAndFeelInfo
import com.intellij.openapi.application.ApplicationManager
import io.unthrottled.doki.icons.jetbrains.shared.DokiTheme
import io.unthrottled.doki.icons.jetbrains.tools.AssetTools
import io.unthrottled.doki.util.toOptional
import java.util.Optional
import javax.swing.UIManager

class ThemeManager {

  companion object {
    val instance: ThemeManager
      get() = ApplicationManager.getApplication().getService(ThemeManager::class.java)
  }

  private val themeMap: Map<String, DokiTheme> =
    AssetTools.readJsonFromResources<List<DokiTheme>>(
      "/doki/generated",
      "doki-theme-definitions.json",
      object : TypeToken<List<DokiTheme>>() {}.type
    ).map {
      themes ->
      themes.associateBy { it.id }
    }.orElseGet {
      emptyMap()
    }

  val isCurrentThemeDoki: Boolean
    get() = currentTheme.isPresent

  val currentTheme: Optional<DokiTheme>
    get() = processLaf(LafManagerImpl.getInstance().currentLookAndFeel)

  val allThemes: List<DokiTheme>
    get() = themeMap.values.toList()

  fun processLaf(currentLaf: UIManager.LookAndFeelInfo?): Optional<DokiTheme> {
    return currentLaf.toOptional()
      .filter { it is UIThemeBasedLookAndFeelInfo }
      .map { it as UIThemeBasedLookAndFeelInfo }
      .map { themeMap[it.getId()] }
  }
}
