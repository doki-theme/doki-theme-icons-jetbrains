package io.unthrottled.doki.icons.jetbrains.path

import com.google.gson.reflect.TypeToken
import com.intellij.openapi.util.IconPathPatcher
import io.unthrottled.doki.icons.jetbrains.tools.AssetTools.readJsonFromResources
import io.unthrottled.doki.icons.jetbrains.tools.Logging
import io.unthrottled.doki.icons.jetbrains.tools.logger

data class PathMapping(
  val originalPath: String,
  val newPath: String,
)

class UIIconPathPatcher : IconPathPatcher(), Logging {

  private val pathMappings: Map<String, String> =
    readJsonFromResources<List<PathMapping>>(
      "ui-icons.path.mappings.json",
      object : TypeToken<List<PathMapping>>() {}.type
    )
      .map { def ->
        def.associate {
          it.originalPath to it.newPath
        }
      }.orElseGet {
        logger().warn("Unable to read path mappings")
        emptyMap()
      }

  override fun patchPath(
    path: String,
    classLoader: ClassLoader?
  ): String? = pathMappings[path]

  override fun getContextClassLoader(
    path: String,
    originalClassLoader: ClassLoader?
  ): ClassLoader? =
    javaClass.classLoader
}
