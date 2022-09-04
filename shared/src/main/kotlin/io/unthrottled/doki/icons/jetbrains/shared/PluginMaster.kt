package io.unthrottled.doki.icons.jetbrains.shared

import com.intellij.openapi.Disposable
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.project.ProjectManager
import com.intellij.openapi.project.ProjectManagerListener
import io.unthrottled.doki.icons.jetbrains.shared.onboarding.UserOnBoarding
import io.unthrottled.doki.icons.jetbrains.shared.path.IconPathReplacementComponent
import io.unthrottled.doki.icons.jetbrains.shared.svg.ThemedSVGManager
import io.unthrottled.doki.icons.jetbrains.shared.themes.IconThemeManager
import io.unthrottled.doki.icons.jetbrains.shared.tools.Logging
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentMap

class PluginMaster : ProjectManagerListener, Disposable, Logging {

  companion object {
    val instance: PluginMaster
      get() = ApplicationManager.getApplication().getService(PluginMaster::class.java)
  }

  private val projectListeners: ConcurrentMap<String, ProjectListeners> = ConcurrentHashMap()

  init {
    IconThemeManager.instance.init()
    ThemedSVGManager.instance.initialize()
    IconPathReplacementComponent.initialize()
  }

  override fun projectOpened(project: Project) {
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
    ThemedSVGManager.instance.dispose()
    IconPathReplacementComponent.dispose()
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
