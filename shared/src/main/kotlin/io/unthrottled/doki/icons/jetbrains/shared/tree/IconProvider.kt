package io.unthrottled.doki.icons.jetbrains.shared.tree

import javax.swing.Icon

interface IconProvider {
  fun getIcon(virtualFileInfo: VirtualFileInfo): Icon?
}
