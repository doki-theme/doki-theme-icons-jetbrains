package io.unthrottled.doki.icons.jetbrains.themes

import com.google.gson.reflect.TypeToken
import com.intellij.ide.ui.LafManager
import com.intellij.ide.ui.LafManagerListener
import com.intellij.ide.ui.laf.UIThemeLookAndFeelInfo
import com.intellij.ide.ui.laf.UIThemeLookAndFeelInfoImpl
import com.intellij.openapi.Disposable
import com.intellij.openapi.application.ApplicationManager
import com.intellij.util.SVGLoader
import com.intellij.util.messages.Topic
import io.unthrottled.doki.icons.jetbrains.DokiThemeIcons
import io.unthrottled.doki.icons.jetbrains.DokiThemeInformation
import io.unthrottled.doki.icons.jetbrains.config.Config
import io.unthrottled.doki.icons.jetbrains.config.IconConfigListener
import io.unthrottled.doki.icons.jetbrains.config.IconSettingsModel
import io.unthrottled.doki.icons.jetbrains.svg.noOptPatcherProvider
import io.unthrottled.doki.icons.jetbrains.tools.AssetTools
import io.unthrottled.doki.icons.jetbrains.tools.Logging
import io.unthrottled.doki.icons.jetbrains.tools.doOrElse
import io.unthrottled.doki.icons.jetbrains.tools.logger
import io.unthrottled.doki.icons.jetbrains.tools.toOptional
import java.util.EventListener
import java.util.Optional

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

data class DokiThemePayload(
  val dokiTheme: DokiTheme,
  val colorPatcher: SVGLoader.SvgElementColorPatcherProvider,
)

interface ThemeManagerListener : EventListener {
  fun onDokiThemeActivated(dokiThemePayload: DokiThemePayload)

  fun onDokiThemeRemoved()
}

class IconThemeManager : LafManagerListener, Disposable, IconConfigListener, Logging {
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
    themeMap =
      AssetTools.readJsonFromResources<List<DokiThemeInformation>>(
        "/doki/generated",
        "doki-theme-definitions.json",
        object : TypeToken<List<DokiThemeInformation>>() {}.type,
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

  val currentTheme: Optional<DokiThemePayload> =
    if (Config.instance.syncWithDokiTheme) {
      mapLAFToDokiTheme(LafManager.getInstance().currentUIThemeLookAndFeel)
    } else {
      userSetTheme
    }.or {
      userSetTheme
    }
  private val userSetTheme: Optional<DokiThemePayload>
    get() =
      LafManager.getInstance().installedLookAndFeels
        .filterIsInstance<UIThemeLookAndFeelInfoImpl>()
        .firstOrNull {
          it.getId() == Config.instance.currentThemeId
        }.toOptional()
        .map {
          val themeId = it.getId()
          DokiThemePayload(
            themeMap[themeId] ?: error("Expecting theme with ID $themeId to be present"),
            it.theme.colorPatcher ?: noOptPatcherProvider,
          )
        }.or {
          val themeId = Config.instance.currentThemeId
          DokiThemePayload(
            themeMap[themeId] ?: error("Expecting theme with ID $themeId to be present"),
            LafManager.getInstance().currentUIThemeLookAndFeel
              .toOptional()
              .filter { it is UIThemeLookAndFeelInfoImpl }
              .map {
                val uiTheme = it as UIThemeLookAndFeelInfoImpl
                uiTheme.theme.colorPatcher ?: noOptPatcherProvider
              }.orElse(
                noOptPatcherProvider,
              ),
          ).toOptional()
        }
  val allThemes: List<DokiTheme>
    get() = themeMap.values.toList()

  private fun mapLAFToDokiTheme(currentLaf: UIThemeLookAndFeelInfo?): Optional<DokiThemePayload> {
    return currentLaf.toOptional()
      .filter { it is UIThemeLookAndFeelInfoImpl }
      .map { it as UIThemeLookAndFeelInfoImpl }
      .filter { themeMap.containsKey(it.id) }
      .map {
        DokiThemePayload(
          themeMap[it.id]!!,
          it.theme.colorPatcher ?: noOptPatcherProvider,
        )
      }
  }

  override fun dispose() {
    connection.dispose()
  }

  override fun lookAndFeelChanged(source: LafManager) {
    val messageBus = ApplicationManager.getApplication().messageBus
    if (Config.instance.syncWithDokiTheme) {
      mapLAFToDokiTheme(source.currentUIThemeLookAndFeel)
        .map {
          Config.instance.currentThemeId = it.dokiTheme.id
          it
        }.or {
          userSetTheme
        }
    } else {
      userSetTheme
    }
      .doOrElse({ dokiThemePayload ->
        messageBus.syncPublisher(TOPIC)
          .onDokiThemeActivated(dokiThemePayload)
      }) {
        this.logger().warn("Unable to set Doki Theme for icons for current theme")
      }
  }

  fun getThemeById(currentThemeId: String): Optional<DokiTheme> = themeMap[currentThemeId].toOptional()

  override fun iconConfigUpdated(
    previousState: IconSettingsModel,
    newState: IconSettingsModel,
  ) {
    val currentThemeId = newState.currentThemeId
    if (previousState.currentThemeId != currentThemeId) {
      LafManager.getInstance().installedLookAndFeels
        .filterIsInstance<UIThemeLookAndFeelInfoImpl>()
        .firstOrNull {
          it.getId() == currentThemeId
        }.toOptional()
        .flatMap { uiTheme ->
          getThemeById(currentThemeId)
            .map { dokiTheme ->
              DokiThemePayload(dokiTheme, uiTheme.theme.colorPatcher ?: noOptPatcherProvider)
            }
        }
        .ifPresent {
          ApplicationManager.getApplication().messageBus
            .syncPublisher(TOPIC)
            .onDokiThemeActivated(it)
        }
    }
  }
}
