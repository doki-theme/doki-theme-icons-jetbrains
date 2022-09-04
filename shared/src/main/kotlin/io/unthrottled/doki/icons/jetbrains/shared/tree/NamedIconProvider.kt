package io.unthrottled.doki.icons.jetbrains.shared.tree

import com.intellij.openapi.util.IconLoader
import io.unthrottled.doki.icons.jetbrains.shared.Constants
import javax.swing.Icon

open class NamedIconProvider(namedIconMappings: NamedIconMappings) : IconProvider {

  private val mappingLocator =
    NamedIconMappingLocatorFactory.create(namedIconMappings)

  override fun getNamedIcon(virtualFileInfo: VirtualFileInfo): Icon? {
    return mappingLocator.locateMapping(virtualFileInfo)
      .map { IconLoader.getIcon("${Constants.DOKI_ICONS_BASE_PATH}/${it.iconPath}", javaClass) }
      .orElse(null)
  }
}
