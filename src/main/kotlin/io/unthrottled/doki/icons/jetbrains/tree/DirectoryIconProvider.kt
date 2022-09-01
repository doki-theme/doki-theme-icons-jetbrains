package io.unthrottled.doki.icons.jetbrains.tree

import com.intellij.openapi.util.IconLoader
import io.unthrottled.doki.icons.jetbrains.Constants
import javax.swing.Icon

object DirectoryIconProvider : IconProvider {
  override fun getNamedIcon(virtualFileInfo: VirtualFileInfo): Icon? {
    return IconLoader.getIcon(
      "${Constants.DOKI_ICONS_BASE_PATH}/folder.svg",
      DirectoryIconProvider::class.java
    )
  }
}
