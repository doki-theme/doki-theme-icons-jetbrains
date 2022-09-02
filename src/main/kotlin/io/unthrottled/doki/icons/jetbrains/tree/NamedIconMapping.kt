package io.unthrottled.doki.icons.jetbrains.tree

import com.google.gson.reflect.TypeToken
import io.unthrottled.doki.icons.jetbrains.tools.AssetTools
import io.unthrottled.doki.util.Logging
import io.unthrottled.doki.util.logger
import io.unthrottled.doki.util.toOptional
import java.util.Optional

enum class NamedIconMappings(val fileName: String) {
  FOLDER("folders.named.mappings.json"), FILE("files.named.mappings.json"),
}

data class SerializedNamedIconMapping(
  val name: String,
  val mappingPattern: String,
  val iconPath: String,
)
data class NamedIconMapping(
  val name: String,
  val mappingRegex: Regex,
  val iconPath: String,
)
class NamedIconMappingLocator(
  private val namedIconMappings: List<NamedIconMapping>
) {
  fun locateMapping(virtualFileInfo: VirtualFileInfo): Optional<NamedIconMapping> =
    virtualFileInfo.name.toOptional()
      .flatMap { fileName ->
        pickFileNameFromList(fileName)
      }

  private fun pickFileNameFromList(fileName: String): Optional<NamedIconMapping> {
    return namedIconMappings.firstOrNull {
      fileName.matches(it.mappingRegex)
    }.toOptional() // todo smart pick from list
  }
}

object NamedIconMappingLocatorFactory : Logging {

  fun create(namedIconMapping: NamedIconMappings): NamedIconMappingLocator =
    AssetTools.readJsonFromResources<List<SerializedNamedIconMapping>>(
      "/",
      namedIconMapping.fileName,
      object : TypeToken<List<SerializedNamedIconMapping>>() {}.type
    )
      .map { serializedNamedIconMappings ->
        NamedIconMappingLocator(
          serializedNamedIconMappings.map {
            NamedIconMapping(
              name = it.name,
              mappingRegex = Regex(it.mappingPattern),
              iconPath = it.iconPath
            )
          }
        )
      }
      .orElseGet {
        logger().warn("Unable to read named icon mappings for raisins")
        NamedIconMappingLocator(emptyList())
      }
}
