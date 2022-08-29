package io.unthrottled.doki.icons.jetbrains.themes

import com.google.gson.reflect.TypeToken
import com.intellij.ide.ui.LafManager
import com.intellij.ide.ui.LafManagerListener
import com.intellij.ide.ui.laf.LafManagerImpl
import com.intellij.ide.ui.laf.UIThemeBasedLookAndFeelInfo
import com.intellij.openapi.Disposable
import com.intellij.openapi.application.ApplicationManager
import com.intellij.util.messages.Topic
import io.unthrottled.doki.icons.jetbrains.shared.DokiTheme
import io.unthrottled.doki.icons.jetbrains.tools.AssetTools
import io.unthrottled.doki.icons.jetbrains.tools.doOrElse
import io.unthrottled.doki.util.toOptional
import java.util.EventListener
import java.util.Optional
import javax.swing.UIManager

interface ThemeManagerListener : EventListener {

  fun onDokiThemeActivated(dokiTheme: DokiTheme)

  fun onDokiThemeRemoved()
}

class ThemeManager : LafManagerListener, Disposable {
  companion object {
    val TOPIC = Topic(ThemeManagerListener::class.java)
    val instance: ThemeManager
      get() = ApplicationManager.getApplication().getService(ThemeManager::class.java)
  }

  private val connection = ApplicationManager.getApplication().messageBus.connect()

  init {
    connection.subscribe(LafManagerListener.TOPIC, this)
  }

  fun init() {
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

  private fun processLaf(currentLaf: UIManager.LookAndFeelInfo?): Optional<DokiTheme> {
    return currentLaf.toOptional()
      .filter { it is UIThemeBasedLookAndFeelInfo }
      .map { it as UIThemeBasedLookAndFeelInfo }
      .map { themeMap[it.getId()] }
  }

  override fun dispose() {
    connection.dispose()
  }

  override fun lookAndFeelChanged(source: LafManager) {
    val messageBus = ApplicationManager.getApplication().messageBus
    // todo: only update if integrated with theme plugin.
    processLaf(source.currentLookAndFeel)
      .doOrElse({ dokiTheme ->
        messageBus.syncPublisher(TOPIC)
          .onDokiThemeActivated(dokiTheme)
      }) {
        messageBus.syncPublisher(TOPIC)
          .onDokiThemeRemoved()
      }
  }
}
