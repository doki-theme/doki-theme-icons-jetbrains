package io.unthrottled.doki.icons.jetbrains.listeners

import com.intellij.openapi.project.Project
import com.intellij.openapi.project.ProjectManagerListener
import com.intellij.openapi.startup.ProjectActivity
import io.unthrottled.doki.icons.jetbrains.PluginMaster
import io.unthrottled.doki.icons.jetbrains.tools.Logging

internal class ProjectListener :
  ProjectManagerListener, Logging {
  override fun projectClosed(project: Project) {
    PluginMaster.instance.projectClosed(project)
  }
}

internal class PluginPostStartUpActivity : ProjectActivity {
  override suspend fun execute(project: Project) {
    PluginMaster.instance.handleProjectOpened(project)
  }
}
