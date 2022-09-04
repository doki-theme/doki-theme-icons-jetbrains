package io.unthrottled.doki.icons.jetbrains.shared.themes

import com.google.gson.reflect.TypeToken
import com.intellij.ide.ui.LafManager
import com.intellij.ide.ui.LafManagerListener
import com.intellij.ide.ui.laf.LafManagerImpl
import com.intellij.ide.ui.laf.UIThemeBasedLookAndFeelInfo
import com.intellij.openapi.Disposable
import com.intellij.openapi.application.ApplicationManager
import com.intellij.util.messages.Topic
import io.unthrottled.doki.icons.jetbrains.shared.DokiThemeIcons
import io.unthrottled.doki.icons.jetbrains.shared.DokiThemeInformation
import io.unthrottled.doki.icons.jetbrains.shared.config.Config
import io.unthrottled.doki.icons.jetbrains.shared.config.IconConfigListener
import io.unthrottled.doki.icons.jetbrains.shared.config.IconSettingsModel
import io.unthrottled.doki.icons.jetbrains.shared.tools.AssetTools
import io.unthrottled.doki.icons.jetbrains.shared.tools.doOrElse
import io.unthrottled.doki.icons.jetbrains.shared.tools.toOptional
import java.util.EventListener
import java.util.Optional
import javax.swing.UIManager

class DokiTheme(
  private val dokiThemeInformation: DokiThemeInformation,
  val version: String,
) {
  val id: String
    get() = dokiThemeInformation.id

  val listName: String
    get() = dokiThemeInformation.listName

  val colors: Map<String, String>
    get() = dokiThemeInformation.colors
}

interface ThemeManagerListener : EventListener {

  fun onDokiThemeActivated(dokiTheme: DokiTheme)

  fun onDokiThemeRemoved()
}

class IconThemeManager : LafManagerListener, Disposable, IconConfigListener {
  companion object {
    const val DEFAULT_THEME_ID = "13adffd9-acbe-47af-8101-fa71269a4c5c" // Zero Two Obsidian
    val TOPIC = Topic(ThemeManagerListener::class.java)
    val instance: IconThemeManager
      get() = ApplicationManager.getApplication().getService(IconThemeManager::class.java)
  }

  private val connection = ApplicationManager.getApplication().messageBus.connect()

  private val themeMap: Map<String, DokiTheme>

  init {
    connection.subscribe(LafManagerListener.TOPIC, this)
    connection.subscribe(IconConfigListener.TOPIC, this)
    val currentVersion = DokiThemeIcons.getVersion().orElse("69")
    themeMap = AssetTools.readJsonFromResources<List<DokiThemeInformation>>(
      "/doki/generated",
      "doki-theme-definitions.json",
      object : TypeToken<List<DokiThemeInformation>>() {}.type
    ).map { themes ->
      themes
        .map { DokiTheme(it, currentVersion) }
        .associateBy { it.id }
    }.orElseGet {
      emptyMap()
    }
  }

  fun init() {
  }

  val defaultTheme: DokiTheme
    get() = getThemeById(DEFAULT_THEME_ID).get()

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
    if (!Config.instance.syncWithDokiTheme) {
      return
    }

    val messageBus = ApplicationManager.getApplication().messageBus
    processLaf(source.currentLookAndFeel)
      .doOrElse({ dokiTheme ->
        messageBus.syncPublisher(TOPIC)
          .onDokiThemeActivated(dokiTheme)
      }) {
        messageBus.syncPublisher(TOPIC)
          .onDokiThemeRemoved()
      }
  }

  fun getThemeById(currentThemeId: String): Optional<DokiTheme> =
    themeMap[currentThemeId].toOptional()

  override fun iconConfigUpdated(
    previousState: IconSettingsModel,
    newState: IconSettingsModel
  ) {
    val currentThemeId = newState.currentThemeId
    if (previousState.currentThemeId != currentThemeId) {
      getThemeById(currentThemeId).ifPresent {
        ApplicationManager.getApplication().messageBus
          .syncPublisher(TOPIC)
          .onDokiThemeActivated(it)
      }
    }
  }
}
