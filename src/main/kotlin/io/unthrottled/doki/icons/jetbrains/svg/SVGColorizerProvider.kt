package io.unthrottled.doki.icons.jetbrains.svg

import com.intellij.util.SVGLoader
import com.intellij.util.SVGLoader.SvgElementColorPatcherProvider
import com.intellij.util.io.DigestUtil
import io.unthrottled.doki.icons.jetbrains.themes.DokiTheme
import io.unthrottled.doki.util.runSafelyWithResult
import org.w3c.dom.Element

object SVGColorizerProviderFactory {

  fun createForTheme(dokiTheme: DokiTheme): SVGColorizerProvider {
    val otherSvgPatcherProvider = SvgLoaderHacker.collectOtherPatcher()
    return SVGColorizerProvider(dokiTheme, otherSvgPatcherProvider)
  }
}

@Suppress("UnstableApiUsage")
class SVGColorizerProvider(
  private val dokiTheme: DokiTheme,
  private val otherSvgPatcherProvider: PatcherProvider
) : SvgElementColorPatcherProvider {

  override fun forPath(path: String?): SVGLoader.SvgElementColorPatcher? {
    return buildComposedColorizer(
      listOf(
        otherSvgPatcherProvider,
        object : PatcherProvider {
          override fun forPath(path: String?): SVGLoader.SvgElementColorPatcher? = SVGColorizer(dokiTheme)
        }
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

  private fun buildComposedColorizer(
    patchers: List<Patcher>,
  ): Patcher {
    val patcher = object : Patcher {
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

    return patcher
  }
}
