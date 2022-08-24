package io.unthrottled.doki.icons.jetbrains.integrations

import com.intellij.openapi.project.Project
import com.intellij.openapi.project.ProjectManagerListener
import io.unthrottled.doki.icons.jetbrains.PluginMaster
import io.unthrottled.doki.icons.jetbrains.tools.Logging

internal class ProjectListener :
  ProjectManagerListener, Logging {

  override fun projectOpened(project: Project) {
    PluginMaster.instance.projectOpened(project)
  }

  override fun projectClosed(project: Project) {
    PluginMaster.instance.projectClosed(project)
  }
}
