package io.unthrottled.doki.icons.jetbrains.shared.path

import com.intellij.openapi.util.IconLoader
import com.intellij.openapi.util.IconPathPatcher
import com.intellij.ui.ExperimentalUI
import io.unthrottled.doki.icons.jetbrains.shared.tools.runSafely

object ExperimentalUIBastardizer {

  init {
      bastardizeExperimentalUI()
  }

  @Suppress("UnstableApiUsage")
  fun bastardizeExperimentalUI() {
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