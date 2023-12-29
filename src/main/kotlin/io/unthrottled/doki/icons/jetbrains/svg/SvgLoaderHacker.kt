package io.unthrottled.doki.icons.jetbrains.svg

import com.intellij.ui.svg.SvgAttributePatcher
import com.intellij.util.SVGLoader
import java.util.Optional

typealias PatcherProvider = SVGLoader.SvgElementColorPatcherProvider
typealias Patcher = SvgAttributePatcher

object NoOptPatcher : Patcher {
}

val emptyLongArray = LongArray(0)

val noOptPatcherProvider = object : PatcherProvider {

  override fun attributeForPath(path: String): SvgAttributePatcher {
    return NoOptPatcher
  }

  override fun digest(): LongArray {
    return emptyLongArray
  }
}

object SvgLoaderHacker {

  fun collectOtherPatcher(): PatcherProvider =
    Optional.ofNullable(
      SVGLoader::class.java.declaredFields
        .firstOrNull { it.name == "colorPatcherProvider" }
    )
      .map { ourColorPatcherField ->
        ourColorPatcherField.isAccessible = true
        ourColorPatcherField.get(null)
      }
      .filter { it is PatcherProvider }
      .filter { it !is SVGColorizerProvider }
      .map {
        val otherPatcher = it as PatcherProvider
        otherPatcher
      }.orElseGet {
        noOptPatcherProvider
      }
}
