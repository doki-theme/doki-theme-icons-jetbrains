package io.unthrottled.doki.icons.jetbrains

import com.intellij.ide.plugins.PluginManagerCore
import com.intellij.openapi.extensions.PluginId
import io.unthrottled.doki.icons.jetbrains.tools.toOptional
import java.util.Optional

object DokiThemeIcons {
  fun getVersion(): Optional<String> =
    PluginManagerCore.getPlugin(PluginId.getId(Constants.PLUGIN_ID))
      .toOptional()
      .map { it.version }
}
