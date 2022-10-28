package icons

import com.intellij.openapi.util.IconLoader
import com.intellij.ui.IconManager.getInstance
import javax.swing.Icon

object DokiThemeIcons {
  @JvmField
  val PLUGIN_ICON = IconLoader.getIcon("/doki/icons/doki_icon_logo.svg", javaClass)
  @JvmField
  val PROJECT_GLYPH = IconLoader.getIcon("/doki/icons/project_glyph.svg", javaClass)
  @JvmField
  val CURLY_BRACES = IconLoader.getIcon("/doki/icons/curly_braces.svg", javaClass)
  @JvmField
  val SOLID_DOKI_GLYPH = IconLoader.getIcon("/doki/icons/solid_doki_glyph.svg", javaClass)
  @JvmField
  val WATCH = IconLoader.getIcon("/doki/icons/watch.svg", javaClass)
  @JvmField
  val SOLID_ERROR = IconLoader.getIcon("/doki/icons/warning_circle_filled.svg", javaClass)

  object Tree {
    const val EXPANDED_KEY = "Tree.expandedIcon"
    const val SELECTED_EXPANDED_KEY = "Tree.expandedSelectedIcon"
    const val COLLAPSED_KEY = "Tree.collapsedIcon"
    const val SELECTED_COLLAPSED_KEY = "Tree.collapsedSelectedIcon"
    val COLLAPSED = load("/doki/icons/chevron_right_13x13.svg")
    val EXPANDED = load("/doki/icons/chevron_down_13x13.svg")
  }

  private fun load(path: String): Icon =
    getInstance().getIcon(path, DokiThemeIcons::class.java)
}
