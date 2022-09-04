package io.unthrottled.doki.icons.jetbrains.shared.svg

import com.intellij.util.SVGLoader
import org.w3c.dom.Element
import java.util.Optional

typealias PatcherProvider = SVGLoader.SvgElementColorPatcherProvider
typealias Patcher = SVGLoader.SvgElementColorPatcher

object NoOptPatcher : Patcher {
  override fun patchColors(svg: Element) {}
  val byteArray = ByteArray(0)
  override fun digest(): ByteArray {
    return byteArray
  }
}

val emptyByteArray = ByteArray(0)

val noOptPatcherProvider = object : PatcherProvider {

  override fun forPath(path: String?): SVGLoader.SvgElementColorPatcher {
    return NoOptPatcher
  }
}

object SvgLoaderHacker {

  fun collectOtherPatcher(): PatcherProvider =
    Optional.ofNullable(
      SVGLoader::class.java.declaredFields
        .firstOrNull { it.name == "ourColorPatcher" }
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
