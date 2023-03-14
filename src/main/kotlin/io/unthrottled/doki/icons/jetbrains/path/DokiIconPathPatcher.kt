package io.unthrottled.doki.icons.jetbrains.path

import com.google.gson.reflect.TypeToken
import com.intellij.openapi.util.IconPathPatcher
import io.unthrottled.doki.icons.jetbrains.Constants
import io.unthrottled.doki.icons.jetbrains.tools.AssetTools.readJsonFromResources
import io.unthrottled.doki.icons.jetbrains.tools.Logging
import io.unthrottled.doki.icons.jetbrains.tools.logger

data class PathMapping(
  val originalPath: String,
  val iconName: String,
  val isOddBall: Boolean?
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
          val dokiIconsBasePath =
            if (it.isOddBall == true) Constants.DOKI_ODD_BALLS_ICONS_BASE_PATH else Constants.DOKI_ICONS_BASE_PATH
          it.originalPath to "$dokiIconsBasePath/${it.iconName}"
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
