package io.unthrottled.doki.icons.jetbrains.shared.listeners

import com.intellij.ide.AppLifecycleListener
import com.intellij.openapi.project.DumbAware
import io.unthrottled.doki.icons.jetbrains.shared.PluginMaster
import io.unthrottled.doki.icons.jetbrains.shared.integrations.PlatformHacker

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
