package io.unthrottled.doki.icons.jetbrains.themes

import com.google.gson.reflect.TypeToken
import com.intellij.ide.ui.LafManager
import com.intellij.ide.ui.LafManagerListener
import com.intellij.ide.ui.laf.LafManagerImpl
import com.intellij.ide.ui.laf.UIThemeBasedLookAndFeelInfo
import com.intellij.openapi.Disposable
import com.intellij.openapi.application.ApplicationManager
import com.intellij.util.messages.Topic
import io.unthrottled.doki.icons.jetbrains.DokiThemeIcons
import io.unthrottled.doki.icons.jetbrains.shared.DokiThemeInformation
import io.unthrottled.doki.icons.jetbrains.tools.AssetTools
import io.unthrottled.doki.icons.jetbrains.tools.doOrElse
import io.unthrottled.doki.util.toOptional
import java.util.EventListener
import java.util.Optional
import javax.swing.UIManager

class DokiTheme(
  private val dokiThemeInformation: DokiThemeInformation,
  val version: String,
) {
  val id: String
    get() = dokiThemeInformation.id

  val colors: Map<String, String>
    get() = dokiThemeInformation.colors
}

interface ThemeManagerListener : EventListener {

  fun onDokiThemeActivated(dokiTheme: DokiTheme)

  fun onDokiThemeRemoved()
}

class IconThemeManager : LafManagerListener, Disposable {
  companion object {
    val TOPIC = Topic(ThemeManagerListener::class.java)
    val instance: IconThemeManager
      get() = ApplicationManager.getApplication().getService(IconThemeManager::class.java)
  }

  private val connection = ApplicationManager.getApplication().messageBus.connect()

  private val themeMap: Map<String, DokiTheme>
  init {
    connection.subscribe(LafManagerListener.TOPIC, this)
    val currentVersion = DokiThemeIcons.getVersion().orElse("69")
    themeMap = AssetTools.readJsonFromResources<List<DokiThemeInformation>>(
      "/doki/generated",
      "doki-theme-definitions.json",
      object : TypeToken<List<DokiThemeInformation>>() {}.type
    ).map {
      themes ->
      themes
        .map { DokiTheme(it, currentVersion) }
        .associateBy { it.id }
    }.orElseGet {
      emptyMap()
    }
  }

  fun init() {
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
