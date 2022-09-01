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

data class NamedIconMapping(
  val name: String,
  val mappingPattern: Regex,
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
    return namedIconMappings.first {
      fileName.matches(it.mappingPattern)
    }.toOptional() // todo smart pick from list
  }
}

// todo: test this
object NamedIconMappingLocatorFactory : Logging {

  fun create(namedIconMapping: NamedIconMappings): NamedIconMappingLocator =
    AssetTools.readJsonFromResources<List<NamedIconMapping>>(
      "/",
      namedIconMapping.fileName,
      object : TypeToken<List<NamedIconMapping>>() {}.type
    )
      .map { NamedIconMappingLocator(it) }
      .orElseGet {
        logger().warn("Unable to read named icon mappings for raisins")
        NamedIconMappingLocator(emptyList())
      }
}
