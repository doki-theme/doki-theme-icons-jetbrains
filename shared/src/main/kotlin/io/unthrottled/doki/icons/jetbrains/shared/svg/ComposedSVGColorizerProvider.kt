package io.unthrottled.doki.icons.jetbrains.shared.svg

import com.intellij.util.SVGLoader
import com.intellij.util.io.DigestUtil
import io.unthrottled.doki.icons.jetbrains.shared.themes.DokiThemePayload
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
  dokiThemePayload: DokiThemePayload,
  otherSvgPatcherProvider: PatcherProvider
) : PatcherProvider {
  private val colorizer = SVGColorizerProvider(dokiThemePayload.dokiTheme)
  private val replacer = SVGColorPaletteReplacer(dokiThemePayload.dokiTheme)
  private val patcherProviders = listOf(
    otherSvgPatcherProvider,
    colorizer,
    replacer,
    dokiThemePayload.uiTheme.colorPatcher,
  )
    .distinct()

  override fun forPath(path: String?): SVGLoader.SvgElementColorPatcher? =
    ComposedSVGColorizer(
      patcherProviders
        .mapNotNull { patcherProvider ->
          runSafelyWithResult({
            patcherProvider.forPath(path)
          }) {
            null
          }
        }
    )
}

object ComposedSVGColorizerProviderFactory {

  fun createForTheme(dokiThemePayload: DokiThemePayload): PatcherProvider {
    val otherSvgPatcherProvider = SvgLoaderHacker.collectOtherPatcher()
    return ComposedSVGColorizerProvider(dokiThemePayload, otherSvgPatcherProvider)
  }
}
