package io.unthrottled.doki.icons.jetbrains.shared.tree

import com.intellij.openapi.project.Project
import io.unthrottled.doki.icons.jetbrains.shared.tools.toOptional
import java.util.Optional

fun Project.nameProvider(): OptimisticNameProvider =
  this.getService(OptimisticNameProvider::class.java)

class OptimisticNameProvider(private val project: Project) {

  fun findMapping(fileName: String): Optional<NamedIconMapping> {
    return NamedMappingStore.FILES.firstOrNull {
      fileName.matches(it.mappingRegex)
    }.toOptional()
  }
}
