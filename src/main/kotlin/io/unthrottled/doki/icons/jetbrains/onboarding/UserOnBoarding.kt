package io.unthrottled.doki.icons.jetbrains.onboarding

import com.intellij.ide.plugins.PluginManagerCore
import com.intellij.openapi.extensions.PluginId
import com.intellij.openapi.project.Project
import com.intellij.openapi.startup.StartupManager
import io.unthrottled.doki.icons.jetbrains.Constants.PLUGIN_ID
import io.unthrottled.doki.icons.jetbrains.config.Config
import io.unthrottled.doki.icons.jetbrains.tools.toOptional
import java.util.Optional
import java.util.UUID

object UserOnBoarding {

  fun attemptToPerformNewUpdateActions(project: Project) {
    getNewVersion().ifPresent { newVersion ->
      Config.instance.version = newVersion
      StartupManager.getInstance(project)
        .runWhenProjectIsInitialized {
          UpdateNotification.display(project, newVersion)
        }
    }

    val isNewUser = Config.instance.userId.isEmpty()
    if (isNewUser) {
      Config.instance.userId = UUID.randomUUID().toString()
    }
  }

  private fun getNewVersion() =
    getVersion()
      .filter { it != Config.instance.version }

  fun getVersion(): Optional<String> =
    PluginManagerCore.getPlugin(PluginId.getId(PLUGIN_ID))
      .toOptional()
      .map { it.version }
}
