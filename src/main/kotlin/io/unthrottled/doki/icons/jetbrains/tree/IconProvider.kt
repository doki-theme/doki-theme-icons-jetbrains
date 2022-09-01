package io.unthrottled.doki.icons.jetbrains.tree

import javax.swing.Icon

interface IconProvider {
  fun getNamedIcon(virtualFileInfo: VirtualFileInfo): Icon?
}
