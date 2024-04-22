package io.unthrottled.doki.icons.jetbrains.svg

import com.intellij.util.io.DigestUtil
import io.unthrottled.doki.icons.jetbrains.shared.toByteArray
import io.unthrottled.doki.icons.jetbrains.shared.toLongArray
import io.unthrottled.doki.icons.jetbrains.themes.DokiThemePayload
import io.unthrottled.doki.icons.jetbrains.tools.runSafelyWithResult

class ComposedSVGColorizer(
  private val patchers: List<Patcher>,
) : Patcher {
  override fun patchColors(attributes: MutableMap<String, String>) {
    patchers.forEach {
      it.patchColors(attributes)
    }
  }
}

@Suppress("UnstableApiUsage")
class ComposedSVGColorizerProvider(
  dokiThemePayload: DokiThemePayload,
  otherSvgPatcherProvider: PatcherProvider,
) : PatcherProvider {
  private val colorizer = SVGColorizerProvider(dokiThemePayload.dokiTheme)
  private val replacer = SVGColorPaletteReplacer(dokiThemePayload.dokiTheme)
  private val patcherProviders =
    listOf(
      otherSvgPatcherProvider,
      colorizer,
      replacer,
      dokiThemePayload.colorPatcher,
    )
      .distinct()

  private val digest: LongArray

  init {
    val shaDigest = DigestUtil.sha512()
    patcherProviders.forEach { patchers ->
      shaDigest.update(toByteArray(patchers.digest()))
    }
    digest = toLongArray(shaDigest.digest())
  }

  override fun digest(): LongArray = digest

  override fun attributeForPath(path: String): Patcher =
    ComposedSVGColorizer(
      patcherProviders
        .mapNotNull { patcherProvider ->
          runSafelyWithResult({
            patcherProvider.attributeForPath(path)
          }) {
            null
          }
        },
    )
}

object ComposedSVGColorizerProviderFactory {
  fun createForTheme(dokiThemePayload: DokiThemePayload): PatcherProvider {
    val otherSvgPatcherProvider = SvgLoaderHacker.collectOtherPatcher()
    return ComposedSVGColorizerProvider(dokiThemePayload, otherSvgPatcherProvider)
  }
}
