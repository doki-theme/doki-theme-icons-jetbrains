package io.unthrottled.doki.icons.jetbrains.shared.laf

import com.intellij.openapi.actionSystem.impl.ActionToolbarImpl
import com.intellij.openapi.application.ApplicationManager
import com.intellij.util.ui.LafIconLookup
import icons.DokiThemeIcons
import io.unthrottled.doki.icons.jetbrains.shared.config.Config
import io.unthrottled.doki.icons.jetbrains.shared.config.IconConfigListener
import io.unthrottled.doki.icons.jetbrains.shared.config.IconSettingsModel
import io.unthrottled.doki.icons.jetbrains.shared.themes.DokiTheme
import io.unthrottled.doki.icons.jetbrains.shared.themes.IconThemeManager
import io.unthrottled.doki.icons.jetbrains.shared.themes.ThemeManagerListener
import javax.swing.Icon
import javax.swing.SwingUtilities
import javax.swing.UIManager

object LAFIconReplacementComponent : IconConfigListener, ThemeManagerListener {

  private val connection = ApplicationManager.getApplication().messageBus.connect()

  fun initialize() {
    this.connection.subscribe(IconConfigListener.TOPIC, this)
    this.connection.subscribe(IconThemeManager.TOPIC, this)

    installLAFIcons()
  }

  private fun installLAFIcons() {
    if (!Config.instance.isUIIcons) {
      return
    }
    setTreeIcons(
      collapsed = DokiThemeIcons.Tree.COLLAPSED,
      expanded = DokiThemeIcons.Tree.EXPANDED
    )
  }

  private fun setTreeIcons(collapsed: Icon, expanded: Icon) {
    val defaults = UIManager.getLookAndFeelDefaults()
    defaults[DokiThemeIcons.Tree.COLLAPSED_KEY] = collapsed
    defaults[DokiThemeIcons.Tree.SELECTED_COLLAPSED_KEY] = collapsed
    defaults[DokiThemeIcons.Tree.EXPANDED_KEY] = expanded
    defaults[DokiThemeIcons.Tree.SELECTED_EXPANDED_KEY] = expanded
    SwingUtilities.invokeLater { ActionToolbarImpl.updateAllToolbarsImmediately() }
  }

  fun dispose() {
    connection.dispose()
  }

  override fun iconConfigUpdated(previousState: IconSettingsModel, newState: IconSettingsModel) {
    if (newState.isUIIcons != previousState.isUIIcons) {
      if (newState.isUIIcons) {
        installLAFIcons()
      } else {
        removeLAFIcons()
      }
    }
  }

  private fun removeLAFIcons() {
    setTreeIcons(
      collapsed = LafIconLookup.getSelectedIcon("treeCollapsed"),
      expanded = LafIconLookup.getSelectedIcon("treeExpanded"),
    )
  }

  override fun onDokiThemeActivated(dokiTheme: DokiTheme) {
    installLAFIcons()
  }

  override fun onDokiThemeRemoved() {
  }
}
