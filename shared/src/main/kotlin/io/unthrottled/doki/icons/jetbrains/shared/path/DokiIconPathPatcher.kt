package io.unthrottled.doki.icons.jetbrains.shared.path

import com.google.gson.reflect.TypeToken
import com.intellij.openapi.util.IconPathPatcher
import io.unthrottled.doki.icons.jetbrains.shared.Constants
import io.unthrottled.doki.icons.jetbrains.shared.tools.AssetTools.readJsonFromResources
import io.unthrottled.doki.icons.jetbrains.shared.tools.Logging
import io.unthrottled.doki.icons.jetbrains.shared.tools.logger

data class PathMapping(
  val originalPath: String,
  val iconName: String
)

class DokiIconPathPatcher(private val mappingFile: String) : IconPathPatcher(), Logging {

  private val pathMappings: Map<String, String> =
    readJsonFromResources<List<PathMapping>>(
      "/",
      mappingFile,
      object : TypeToken<List<PathMapping>>() {}.type
    )
      .map { def ->
        def.associate {
          it.originalPath to "${Constants.DOKI_ICONS_BASE_PATH}/${it.iconName}"
        }
      }.orElseGet {
        logger().warn("Unable to read path mappings")
        emptyMap()
      }

  override fun patchPath(
    path: String,
    classLoader: ClassLoader?
  ): String? = pathMappings[
    if (path.startsWith('/')) {
      path
    } else {
      "/$path"
    }
  ]

  override fun getContextClassLoader(
    path: String,
    originalClassLoader: ClassLoader?
  ): ClassLoader? =
    javaClass.classLoader

  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (javaClass != other?.javaClass) return false

    other as DokiIconPathPatcher

    if (mappingFile != other.mappingFile) return false

    return true
  }

  override fun hashCode(): Int {
    return mappingFile.hashCode()
  }
}
