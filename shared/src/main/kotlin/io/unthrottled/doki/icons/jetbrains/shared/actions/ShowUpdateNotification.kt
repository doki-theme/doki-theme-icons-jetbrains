package io.unthrottled.doki.icons.jetbrains.shared.actions

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.project.DumbAware
import io.unthrottled.doki.icons.jetbrains.shared.DokiThemeIcons.getVersion
import io.unthrottled.doki.icons.jetbrains.shared.onboarding.UpdateNotification

class ShowUpdateNotification : AnAction(), DumbAware {

  override fun actionPerformed(e: AnActionEvent) {
    getVersion()
      .ifPresent {
        UpdateNotification.display(
          e.project!!,
          it,
        )
      }
  }
}

