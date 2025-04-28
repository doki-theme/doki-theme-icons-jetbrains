package io.unthrottled.doki.icons.jetbrains.tools

import com.intellij.openapi.diagnostic.Logger

val logger = Logger.getInstance("io.unthrottled.doki.icons.jetbrains.tools.PlatformFunctions")

fun updateToolbars() {
  val forName = runSafelyWithResult({
    Class.forName("com.intellij.openapi.actionSystem.impl.ActionToolbarImpl")

  }) {
    logger.warn("Error getting toolbar ", it)
    null
  }
  forName?.methods?.firstOrNull { it.name == "updateAllToolbarsImmediately" }?.run {
    this.invoke(null)
  }
}

