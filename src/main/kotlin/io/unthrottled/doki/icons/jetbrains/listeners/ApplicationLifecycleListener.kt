package io.unthrottled.doki.icons.jetbrains.listeners

import com.intellij.ide.AppLifecycleListener
import com.intellij.openapi.project.DumbAware
import io.unthrottled.doki.icons.jetbrains.PluginMaster
import io.unthrottled.doki.icons.jetbrains.integrations.PlatformHacker

class ApplicationLifecycleListener : AppLifecycleListener, DumbAware {

  companion object {
    init {
      PlatformHacker.toString()
    }
  }

  override fun appFrameCreated(commandLineArgs: MutableList<String>) {
    PluginMaster.instance.initializePlugin()
  }

  override fun appClosing() {
    PluginMaster.instance.dispose()
  }
}
