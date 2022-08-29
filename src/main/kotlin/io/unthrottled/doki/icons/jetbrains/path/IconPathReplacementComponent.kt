package io.unthrottled.doki.icons.jetbrains.path

import com.intellij.openapi.util.IconLoader

object IconPathReplacementComponent {
  val patcho = UIIconPathPatcher()
  fun initialize() {
    IconLoader.installPathPatcher(patcho)
  }

  fun dispose() {
    IconLoader.removePathPatcher(patcho)
  }
}
