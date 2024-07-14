package io.unthrottled.doki.icons.jetbrains.path

import com.intellij.openapi.util.IconLoader
import com.intellij.openapi.util.IconPathPatcher
import com.intellij.ui.NewUI
import io.unthrottled.doki.icons.jetbrains.tools.Logging
import io.unthrottled.doki.icons.jetbrains.tools.logger
import io.unthrottled.doki.icons.jetbrains.tools.runSafely

object ExperimentalUIFixer : Logging {
  init {
    fixExperimentalUI()
  }

  fun fixExperimentalUI() {
    if (!NewUI.isEnabled()) return

    runSafely({
      val expUI = Class.forName("com.intellij.ui.ExperimentalUI")
      expUI.declaredFields
        .filter { it.name == "iconPathPatcher" }
        .forEach {
          it.isAccessible = true
          val experimentalUIClass = Class.forName("com.intellij.ui.ExperimentalUI")
          val expUIInstance =
            experimentalUIClass.methods.firstOrNull { method -> method.name == "getInstance" }
              ?.invoke(null)
          val patcher = it.get(expUIInstance)
          IconLoader.removePathPatcher(patcher as IconPathPatcher)
        }
    }) {
      logger().warn("Unable to fix experimental ui", it)
    }
  }
}
