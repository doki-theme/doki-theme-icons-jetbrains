package io.unthrottled.doki.icons.jetbrains.shared.svg

import com.google.gson.reflect.TypeToken
import com.intellij.openapi.util.text.Strings
import com.intellij.ui.ColorUtil
import com.intellij.ui.JBColor
import com.intellij.util.SVGLoader
import io.unthrottled.doki.icons.jetbrains.shared.themes.DokiTheme
import io.unthrottled.doki.icons.jetbrains.shared.tools.AssetTools
import io.unthrottled.doki.icons.jetbrains.shared.tools.Logging
import io.unthrottled.doki.icons.jetbrains.shared.tools.logger
import io.unthrottled.doki.icons.jetbrains.shared.tools.toColor
import io.unthrottled.doki.icons.jetbrains.shared.tools.toHexString
import org.w3c.dom.Element
import java.awt.Color
import kotlin.math.ceil

class SVGColorPaletteReplacer(private val dokiTheme: DokiTheme) : PatcherProvider, Logging {

  companion object {
    private val iconTemplate: Map<String, String> =
      AssetTools.readJsonFromResources<Map<String, String>>(
        "/doki/generated",
        "icon.palette.template.json",
        object : TypeToken<Map<String, String>>() {}.type
      ).orElseGet {
        emptyMap()
      }

    private const val SECONDARY_BLEND_DARKENING = 3
  }

  private val newPalette =
    iconTemplate.entries
      .associateBy({
        it.key
      }) {
        val namedColor = it.value
        val newColor = dokiTheme.colors[namedColor] ?: ""
        if (newColor.isEmpty()) {
          logger().error(
            """Hey silly maintainer, you forgot to give theme 
              |"${dokiTheme.listName}:${dokiTheme.id}" color "$namedColor", pls fix""".trimMargin()
          )
        }
        newColor
      }.toMutableMap().apply {
        // todo: document deez
        this["#000000"] = JBColor.namedColor(
          "Panel.background",
          ColorUtil.fromHex(dokiTheme.colors["baseBackground"]!!)
        )
          .toHexString()
        this["#776bc4"] = ColorUtil.darker(
          ColorUtil.fromHex(dokiTheme.colors["iconSecondaryBlend"]!!),
          SECONDARY_BLEND_DARKENING,
        )
          .toHexString()
      }

  override fun forPath(path: String?): SVGLoader.SvgElementColorPatcher? =
    PalletPatcher(
      (dokiTheme.id + dokiTheme.version).toByteArray(Charsets.UTF_8),
      newPalette,
    )
}

class PalletPatcher(
  private val digest: ByteArray,
  private val newPalette: Map<String, String>,
) : SVGLoader.SvgElementColorPatcher {

  companion object {
    private const val HEX_STRING_LENGTH = 7
  }
  override fun digest(): ByteArray? {
    return digest
  }

  override fun patchColors(svg: Element) {
    patchColorAttribute(svg, "fill")
    patchColorAttribute(svg, "stop-color")
    patchColorAttribute(svg, "stroke")
    val nodes = svg.childNodes
    val length = nodes.length
    for (i in 0 until length) {
      val item = nodes.item(i)
      if (item is Element) {
        patchColors(item)
      }
    }
  }

  @Suppress("MagicNumber") // cuz is majik
  private fun patchColorAttribute(svg: Element, attrName: String) {
    val color = svg.getAttribute(attrName)
    val opacity = svg.getAttribute("$attrName-opacity")
    if (!Strings.isEmpty(color)) {
      var alpha = 255
      if (!Strings.isEmpty(opacity)) {
        try {
          alpha = ceil((255f * opacity.toFloat()).toDouble()).toInt()
        } catch (ignore: Exception) {
        }
      }
      var newColor: String? = null
      val key = toCanonicalColor(color)
      if (alpha != 255) {
        newColor = newPalette[key + Integer.toHexString(alpha)]
      }
      if (newColor == null) {
        newColor = newPalette[key]
      }
      if (newColor != null) {
        svg.setAttribute(attrName, newColor)
      }
    }
  }

  private fun toCanonicalColor(color: String): String {
    var s = color.lowercase()
    if (s.startsWith("#") && s.length < HEX_STRING_LENGTH) {
      s = "#" + ColorUtil.toHex(ColorUtil.fromHex(s))
    }
    return s
  }
}

class SVGColorizerProvider(private val dokiTheme: DokiTheme) : PatcherProvider {
  override fun forPath(path: String?): SVGLoader.SvgElementColorPatcher? = SVGColorizer(dokiTheme)
}

class SVGColorizer(private val dokiTheme: DokiTheme) : Patcher {
  override fun patchColors(svg: Element) {
    patchChildren(
      svg,
    )
  }

  @Suppress("MagicNumber")
  private fun patchChildren(
    svg: Element,
  ) {
    patchAccent(svg.getAttribute("accentTintTesto"), svg) {
      it.toHexString()
    }
    patchAccent(svg.getAttribute("accentTintDarker"), svg) {
      ColorUtil.darker(it, 1).toHexString()
    }
    patchAccent(svg.getAttribute("accentTintDarkest"), svg) {
      ColorUtil.darker(it, 3).toHexString()
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
