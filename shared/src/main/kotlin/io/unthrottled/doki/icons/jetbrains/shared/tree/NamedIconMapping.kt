package io.unthrottled.doki.icons.jetbrains.shared.tree

import com.google.gson.reflect.TypeToken
import io.unthrottled.doki.icons.jetbrains.shared.tools.AssetTools
import io.unthrottled.doki.icons.jetbrains.shared.tools.Logging
import io.unthrottled.doki.icons.jetbrains.shared.tools.logger
import io.unthrottled.doki.icons.jetbrains.shared.tools.toOptional
import java.util.Optional

enum class NamedIconMappings(
  val fileName: String,
) {
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

object NamedIconMappingLocator {
  fun locateMapping(virtualFileInfo: VirtualFileInfo): Optional<NamedIconMapping> =
    virtualFileInfo.name.toOptional()
      .flatMap { fileName ->
        virtualFileInfo.psiElement.project.nameProvider()
          .findMapping(fileName)
      }
}

object NamedIconMappingFactory : Logging {

  fun create(namedIconMapping: NamedIconMappings): List<NamedIconMapping> =
    AssetTools.readJsonFromResources<List<SerializedNamedIconMapping>>(
      "/",
      namedIconMapping.fileName,
      object : TypeToken<List<SerializedNamedIconMapping>>() {}.type
    )
      .map { serializedNamedIconMappings ->
        serializedNamedIconMappings.map {
          NamedIconMapping(
            name = it.name,
            mappingRegex = Regex(it.mappingPattern),
            iconName = it.iconName
          )
        }
      }
      .orElseGet {
        logger().warn("Unable to read named icon mappings for raisins")
        emptyList()
      }
}

object NamedMappingStore {
  val FILES: List<NamedIconMapping> =
    NamedIconMappingFactory.create(NamedIconMappings.FILE)
}
