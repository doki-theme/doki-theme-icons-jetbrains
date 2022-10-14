package io.unthrottled.doki.icons.jetbrains.shared.path

import com.intellij.openapi.actionSystem.impl.ActionToolbarImpl
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.fileTypes.ex.FileTypeManagerEx
import com.intellij.openapi.util.IconLoader
import io.unthrottled.doki.icons.jetbrains.shared.config.Config
import io.unthrottled.doki.icons.jetbrains.shared.config.IconConfigListener
import io.unthrottled.doki.icons.jetbrains.shared.config.IconSettingsModel

data class IconReplacementPack(
  val iconPatcher: DokiIconPathPatcher,
  val iconSettingsExtractor: (IconSettingsModel) -> Boolean,
  val iconConfigExtractor: (Config) -> Boolean,
)

object IconPathReplacementComponent : IconConfigListener {
  private val iconInstallPacs =
    listOf(
      IconReplacementPack(
        DokiIconPathPatcher("ui-icons.path.mappings.json"),
        { it.isUIIcons },
        { it.isUIIcons }
      ),
      IconReplacementPack(
        DokiIconPathPatcher("node.path.mappings.json"),
        { it.isUIIcons },
        { it.isUIIcons }
      ),
      IconReplacementPack(
        DokiIconPathPatcher("glyph-icons.path.mappings.json"),
        { it.isGlyphIcons },
        { it.isGlyphIcon }
      )
    )

  private val connection = ApplicationManager.getApplication().messageBus.connect()

  fun initialize() {
    this.connection.subscribe(IconConfigListener.TOPIC, this)

    iconInstallPacs.forEach { pak ->
      if (pak.iconConfigExtractor(Config.instance)) {
        IconLoader.installPathPatcher(pak.iconPatcher)
      }
    }
  }

  fun dispose() {
    connection.dispose()
    iconInstallPacs.forEach { pak ->
      IconLoader.removePathPatcher(pak.iconPatcher)
    }
  }

  override fun iconConfigUpdated(previousState: IconSettingsModel, newState: IconSettingsModel) {
    iconInstallPacs.filter {
      it.iconSettingsExtractor(previousState) != it.iconSettingsExtractor(newState)
    }.forEach { pak ->
      val newIconPatchState = pak.iconSettingsExtractor(newState)
      if (newIconPatchState) {
        IconLoader.removePathPatcher(pak.iconPatcher)
      } else {
        IconLoader.installPathPatcher(pak.iconPatcher)
      }
    }

    refresh()
  }

  private fun refresh() {
    ApplicationManager.getApplication().invokeLater {
      val app = ApplicationManager.getApplication()
      app.runWriteAction { FileTypeManagerEx.getInstanceEx().fireFileTypesChanged() }
      app.runWriteAction { ActionToolbarImpl.updateAllToolbarsImmediately() }
    }
  }
}
