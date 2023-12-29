package io.unthrottled.doki.icons.jetbrains.svg

import com.google.gson.reflect.TypeToken
import com.intellij.openapi.util.text.Strings
import com.intellij.ui.ColorUtil
import com.intellij.ui.JBColor
import com.intellij.ui.svg.SvgAttributePatcher
import io.unthrottled.doki.icons.jetbrains.shared.toLongArray
import io.unthrottled.doki.icons.jetbrains.themes.DokiTheme
import io.unthrottled.doki.icons.jetbrains.tools.AssetTools
import io.unthrottled.doki.icons.jetbrains.tools.Logging
import io.unthrottled.doki.icons.jetbrains.tools.logger
import io.unthrottled.doki.icons.jetbrains.tools.toColor
import io.unthrottled.doki.icons.jetbrains.tools.toHexString
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
              |"${dokiTheme.listName}:${dokiTheme.id}" color "$namedColor", pls fix
            """.trimMargin()
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
          SECONDARY_BLEND_DARKENING
        )
          .toHexString()
      }

  override fun attributeForPath(path: String): SvgAttributePatcher? =
    PalletPatcher(
      newPalette
    )

  private val digest = toLongArray(("pallet" + dokiTheme.id + dokiTheme.version).toByteArray(Charsets.UTF_8))
  override fun digest(): LongArray = digest
}

class PalletPatcher(
  private val newPalette: Map<String, String>
) : Patcher {

  companion object {
    private const val HEX_STRING_LENGTH = 7
  }

  override fun patchColors(attributes: MutableMap<String, String>) {
    patchColorAttribute(attributes, "fill")
    patchColorAttribute(attributes, "stop-color")
    patchColorAttribute(attributes, "stroke")
  }

  @Suppress("MagicNumber") // cuz is majik
  private fun patchColorAttribute(attributes: MutableMap<String, String>, attrName: String) {
    val color = attributes[attrName]
    val opacity = attributes["$attrName-opacity"]
    if (!Strings.isEmpty(color)) {
      var alpha = 255
      if (!opacity.isNullOrBlank()) {
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
        attributes[attrName] = newColor
      }
    }
  }

  private fun toCanonicalColor(color: String?): String? {
    if (color.isNullOrBlank())
      return color

    var s = color.lowercase()
    if (s.startsWith("#") && s.length < HEX_STRING_LENGTH) {
      s = "#" + ColorUtil.toHex(ColorUtil.fromHex(s))
    }
    return s
  }
}

class SVGColorizerProvider(private val dokiTheme: DokiTheme) : PatcherProvider {

  override fun attributeForPath(path: String): SvgAttributePatcher? = SVGColorizer(dokiTheme)

  private val digest =
    toLongArray(("colorize" + dokiTheme.id + dokiTheme.version).toByteArray(Charsets.UTF_8))

  override fun digest(): LongArray = digest
}

class SVGColorizer(private val dokiTheme: DokiTheme) : Patcher {

  override fun patchColors(attributes: MutableMap<String, String>) {
    patchChildren(
      attributes
    )
  }

  @Suppress("MagicNumber")
  private fun patchChildren(
    attributes: MutableMap<String, String>
  ) {
    patchAccent(attributes["accentTint"], attributes) {
      it.toHexString()
    }
    patchAccent(attributes["accentTintDarker"], attributes) {
      ColorUtil.darker(it, 1).toHexString()
    }
    patchAccent(attributes["accentTintDarkest"], attributes) {
      ColorUtil.darker(it, 3).toHexString()
    }
    patchAccent(attributes["accentContrastTint"], attributes) {
      getIconAccentContrastColor().toHexString()
    }
    patchAccent(attributes["stopTint"], attributes) {
      getThemedStopColor()
    }

    val themedStartAttr = attributes["themedStart"]
    val themedStopAttr = attributes["themedStop"]
    val themedFillAttr = attributes["themedFill"]
    when {
      "true" == themedStartAttr -> {
        val themedStart = getThemedStartColor()
        attributes["stop-color"] = themedStart
        attributes["fill"] = themedStart
      }

      "true" == themedStopAttr -> {
        val themedStop = getThemedStopColor()
        attributes["stop-color"] = themedStop
        attributes["fill"] = themedStop
      }

      "true" == themedFillAttr -> {
        val themedStart = getThemedStartColor()
        attributes["fill"] = themedStart
        attributes["stroke"] = themedStart
      }
    }
  }

  private fun patchAccent(
    attribute: String?,
    attributes: MutableMap<String, String>,
    colorDecorator: (Color) -> String
  ) {
    when (attribute) {
      "fill" -> attributes["fill"] = colorDecorator(getAccentColor())
      "stroke" -> attributes["stroke"] = colorDecorator(getAccentColor())
      "both", "partialFill" -> {
        val accentColor = colorDecorator(getAccentColor())
        attributes["stroke"] = accentColor
        attributes["stroke-opacity"] = if (attribute == "both") "1" else "0.25"
        attributes["fill"] = accentColor
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
}
