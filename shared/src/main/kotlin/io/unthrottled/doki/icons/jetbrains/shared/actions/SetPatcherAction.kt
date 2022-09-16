package io.unthrottled.doki.icons.jetbrains.shared.actions

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.project.DumbAware
import io.unthrottled.doki.icons.jetbrains.shared.svg.ThemedSVGManager

class SetPatcherAction: AnAction(), DumbAware {
  override fun actionPerformed(e: AnActionEvent) {
    ThemedSVGManager.instance.initialize()
  }
}