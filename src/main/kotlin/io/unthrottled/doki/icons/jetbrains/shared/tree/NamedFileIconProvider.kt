package io.unthrottled.doki.icons.jetbrains.shared.tree

import com.intellij.openapi.util.IconLoader
import io.unthrottled.doki.icons.jetbrains.shared.Constants
import javax.swing.Icon

object NamedFileIconProvider : IconProvider {

  override fun getIcon(virtualFileInfo: VirtualFileInfo): Icon? {
    return NamedIconMappingLocator.locateMapping(virtualFileInfo)
      .map { IconLoader.getIcon("${Constants.DOKI_ICONS_BASE_PATH}/${it.iconName}", javaClass) }
      .orElse(null)
  }
}
