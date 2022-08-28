package io.unthrottled.doki.icons.jetbrains.listeners

import com.intellij.ide.AppLifecycleListener
import com.intellij.openapi.project.DumbAware
import io.unthrottled.doki.icons.jetbrains.PluginMaster

class ApplicationLifecycleListener: AppLifecycleListener, DumbAware {

  @Suppress("UnstableApiUsage")
  override fun appStarted() {
    PluginMaster.instance.initializePlugin()
  }

  override fun appClosing() {
    PluginMaster.instance.dispose()
  }
}