package io.unthrottled.doki.icons.jetbrains.shared.tree

import com.google.gson.reflect.TypeToken
import io.unthrottled.doki.icons.jetbrains.shared.tools.AssetTools
import io.unthrottled.doki.icons.jetbrains.shared.tools.Logging
import io.unthrottled.doki.icons.jetbrains.shared.tools.logger
import io.unthrottled.doki.icons.jetbrains.shared.tools.toOptional
import java.util.Optional

enum class NamedIconMappings(val fileName: String) {
  FILE("files.named.mappings.json"),
}

data class SerializedNamedIconMapping(
  val name: String,
  val mappingPattern: String,
  val iconName: String,
)
data class NamedIconMapping(
  val name: String,
  val mappingRegex: Regex,
  val iconName: String,
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
              iconName = it.iconName
            )
          }
        )
      }
      .orElseGet {
        logger().warn("Unable to read named icon mappings for raisins")
        NamedIconMappingLocator(emptyList())
      }
}
