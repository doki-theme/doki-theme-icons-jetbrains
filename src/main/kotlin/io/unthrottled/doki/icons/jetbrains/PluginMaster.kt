package io.unthrottled.doki.icons.jetbrains

import com.intellij.openapi.Disposable
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.project.ProjectManager
import com.intellij.openapi.project.ProjectManagerListener
import io.unthrottled.doki.icons.jetbrains.laf.LAFIconReplacementComponent
import io.unthrottled.doki.icons.jetbrains.onboarding.UserOnBoarding
import io.unthrottled.doki.icons.jetbrains.path.ExperimentalUIFixer
import io.unthrottled.doki.icons.jetbrains.path.IconPathReplacementComponent
import io.unthrottled.doki.icons.jetbrains.svg.ThemedSVGManager
import io.unthrottled.doki.icons.jetbrains.themes.IconThemeManager
import io.unthrottled.doki.icons.jetbrains.tools.Logging
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentMap

class PluginMaster : ProjectManagerListener, Disposable, Logging {
  companion object {
    init {
      ExperimentalUIFixer.fixExperimentalUI()
    }

    val instance: PluginMaster
      get() = ApplicationManager.getApplication().getService(PluginMaster::class.java)
  }

  private val projectListeners: ConcurrentMap<String, ProjectListeners> = ConcurrentHashMap()

  init {
    IconThemeManager.instance.init()
    ThemedSVGManager.getInstance().initialize()
    IconPathReplacementComponent.initialize()
    LAFIconReplacementComponent.initialize()
  }

  fun handleProjectOpened(project: Project) {
    registerListenersForProject(project)
  }

  private fun registerListenersForProject(project: Project) {
    UserOnBoarding.attemptToPerformNewUpdateActions(project)
  }

  override fun projectClosed(project: Project) {
    projectListeners[project.locationHash]?.dispose()
    projectListeners.remove(project.locationHash)
  }

  override fun dispose() {
    IconThemeManager.instance.dispose()
    ThemedSVGManager.getInstance().dispose()
    IconPathReplacementComponent.dispose()
    LAFIconReplacementComponent.dispose()
    projectListeners.forEach { (_, listeners) -> listeners.dispose() }
  }

  fun initializePlugin() {
    ProjectManager.getInstance().openProjects
      .forEach { registerListenersForProject(it) }
  }
}

internal data class ProjectListeners(
  private val project: Project,
) : Disposable {
  override fun dispose() {
  }
}
