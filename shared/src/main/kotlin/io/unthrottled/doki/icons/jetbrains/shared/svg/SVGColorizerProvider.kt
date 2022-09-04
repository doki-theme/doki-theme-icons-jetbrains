package io.unthrottled.doki.icons.jetbrains.shared.svg

import com.intellij.ui.ColorUtil
import com.intellij.ui.JBColor
import com.intellij.util.SVGLoader
import io.unthrottled.doki.icons.jetbrains.shared.themes.DokiTheme
import io.unthrottled.doki.icons.jetbrains.shared.tools.toColor
import io.unthrottled.doki.icons.jetbrains.shared.tools.toHexString
import org.w3c.dom.Element
import java.awt.Color

class SVGColorizerProvider(private val dokiTheme: DokiTheme) : PatcherProvider {
  override fun forPath(path: String?): SVGLoader.SvgElementColorPatcher? = SVGColorizer(dokiTheme)
}

class SVGColorizer(private val dokiTheme: DokiTheme) : Patcher {
  override fun patchColors(svg: Element) {
    patchChildren(
      svg,
    )
  }

  private fun patchChildren(
    svg: Element,
  ) {
    patchAccent(svg.getAttribute("accentTintTesto"), svg) {
      it.toHexString()
    }
    patchAccent(svg.getAttribute("accentTintDarker"), svg) {
      ColorUtil.darker(it, 1).toHexString()
    }
    patchAccent(svg.getAttribute("accentContrastTint"), svg) {
      getIconAccentContrastColor().toHexString()
    }
    patchAccent(svg.getAttribute("stopTint"), svg) {
      getThemedStopColor()
    }

    val themedStartAttr = svg.getAttribute("themedStart")
    val themedStopAttr = svg.getAttribute("themedStop")
    val themedFillAttr = svg.getAttribute("themedFill")
    when {
      "true" == themedStartAttr -> {
        val themedStart = getThemedStartColor()
        svg.setAttribute("stop-color", themedStart)
        svg.setAttribute("fill", themedStart)
      }
      "true" == themedStopAttr -> {
        val themedStop = getThemedStopColor()
        svg.setAttribute("stop-color", themedStop)
        svg.setAttribute("fill", themedStop)
      }
      "true" == themedFillAttr -> {
        val themedStart = getThemedStartColor()
        svg.setAttribute("fill", themedStart)
        svg.setAttribute("stroke", themedStart)
      }
    }

    val nodes = svg.childNodes
    val length = nodes.length
    for (i in 0 until length) {
      val item = nodes.item(i)
      if (item is Element) {
        patchColors(item)
      }
    }
  }

  private fun patchAccent(attribute: String?, svg: Element, colorDecorator: (Color) -> String) {
    when (attribute) {
      "fill" -> svg.setAttribute("fill", colorDecorator(getAccentColor()))
      "stroke" -> svg.setAttribute("stroke", colorDecorator(getAccentColor()))
      "both", "partialFill" -> {
        val accentColor = colorDecorator(getAccentColor())
        svg.setAttribute("stroke", accentColor)
        svg.setAttribute("stroke-opacity", if (attribute == "both") "1" else "0.25")
        svg.setAttribute("fill", accentColor)
      }
    }
  }

  private fun getAccentColor() =
    dokiTheme.colors["accentColor"]!!.toColor()

  private fun getIconAccentContrastColor() =
    JBColor.namedColor("Doki.Icon.Accent.Contrast.color", Color.WHITE)

  private fun getThemedStartColor() =
    JBColor.namedColor("Doki.startColor", Color.CYAN).toHexString()

  private fun getThemedStopColor() =
    JBColor.namedColor("Doki.stopColor", Color.CYAN).toHexString()

  override fun digest(): ByteArray = (dokiTheme.id + dokiTheme.version).toByteArray(Charsets.UTF_8)
}
