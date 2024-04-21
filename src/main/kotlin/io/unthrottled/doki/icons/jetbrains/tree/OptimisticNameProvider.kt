package io.unthrottled.doki.icons.jetbrains.tree

import com.intellij.openapi.project.Project
import io.unthrottled.doki.icons.jetbrains.tools.toOptional
import java.util.Optional

fun Project.nameProvider(): OptimisticNameProvider = this.getService(OptimisticNameProvider::class.java)

class OptimisticNameProvider() {
  private val priorityList = SchwiftyList<NamedIconMapping>()

  fun findMapping(fileName: String): Optional<NamedIconMapping> {
    return priorityList.first {
      fileName.matches(it.mappingRegex)
    }.or {
      NamedMappingStore.FILES.firstOrNull {
        fileName.matches(it.mappingRegex)
      }.toOptional()
        .map {
          priorityList.enqueue(it)
        }
    }
  }
}
