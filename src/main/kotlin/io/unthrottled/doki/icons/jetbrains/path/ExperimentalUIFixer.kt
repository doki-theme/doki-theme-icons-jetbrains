package io.unthrottled.doki.icons.jetbrains.path

import com.intellij.openapi.util.IconLoader
import com.intellij.openapi.util.IconPathPatcher
import com.intellij.ui.ExperimentalUI
import io.unthrottled.doki.icons.jetbrains.tools.runSafely

object ExperimentalUIFixer {

  init {
    fixExperimentalUI()
  }

  @Suppress("UnstableApiUsage")
  fun fixExperimentalUI() {
    if (!ExperimentalUI.isNewUI()) return

    runSafely({
      val expUI = Class.forName("com.intellij.ui.ExperimentalUI")
      expUI.declaredFields
        .filter { it.name == "iconPathPatcher" }
        .forEach {
          it.isAccessible = true
          val patcher = it.get(ExperimentalUI.getInstance())
          IconLoader.removePathPatcher(patcher as IconPathPatcher)
        }
    }) {}
  }
}
