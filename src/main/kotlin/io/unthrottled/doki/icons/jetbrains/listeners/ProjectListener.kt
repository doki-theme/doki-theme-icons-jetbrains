package io.unthrottled.doki.icons.jetbrains.listeners

import com.intellij.openapi.project.Project
import com.intellij.openapi.project.ProjectManagerListener
import com.intellij.openapi.startup.StartupActivity
import io.unthrottled.doki.icons.jetbrains.PluginMaster
import io.unthrottled.doki.icons.jetbrains.tools.Logging

internal class ProjectListener :
  ProjectManagerListener, Logging {

  override fun projectClosed(project: Project) {
    PluginMaster.instance.projectClosed(project)
  }
}

internal class PluginPostStartUpActivity : StartupActivity {
  override fun runActivity(project: Project) {
    PluginMaster.instance.handleProjectOpened(project)
  }
}
