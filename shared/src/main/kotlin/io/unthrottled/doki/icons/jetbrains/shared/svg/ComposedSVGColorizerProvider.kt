package io.unthrottled.doki.icons.jetbrains.shared.svg

import com.intellij.util.SVGLoader
import com.intellij.util.io.DigestUtil
import io.unthrottled.doki.icons.jetbrains.shared.themes.DokiTheme
import io.unthrottled.doki.icons.jetbrains.shared.tools.runSafelyWithResult
import org.w3c.dom.Element

class ComposedSVGColorizer(
  private val patchers: List<Patcher>
) : Patcher {
  override fun patchColors(svg: Element) {
    patchers.forEach {
      it.patchColors(svg)
    }
  }

  override fun digest(): ByteArray? {
    val shaDigest = DigestUtil.sha512()
    patchers.forEach { patchers ->
      shaDigest.update(patchers.digest() ?: emptyByteArray)
    }
    return shaDigest.digest()
  }
}

@Suppress("UnstableApiUsage")
class ComposedSVGColorizerProvider(
  private val dokiTheme: DokiTheme,
  private val otherSvgPatcherProvider: PatcherProvider
) : PatcherProvider {

  override fun forPath(path: String?): SVGLoader.SvgElementColorPatcher? {
    return ComposedSVGColorizer(
      listOf(
        otherSvgPatcherProvider,
        SVGColorizerProvider(dokiTheme),
        SVGColorPaletteReplacer(dokiTheme),
      )
        .distinct()
        .mapNotNull { patcherProvider ->
          runSafelyWithResult({
            patcherProvider.forPath(path)
          }) {
            null
          }
        }
    )
  }
}

object ComposedSVGColorizerProviderFactory {

  fun createForTheme(dokiTheme: DokiTheme): PatcherProvider {
    val otherSvgPatcherProvider = SvgLoaderHacker.collectOtherPatcher()
    return ComposedSVGColorizerProvider(dokiTheme, otherSvgPatcherProvider)
  }
}
