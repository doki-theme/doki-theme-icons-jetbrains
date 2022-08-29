package io.unthrottled.doki.icons.jetbrains.tools

import com.intellij.ui.ColorUtil
import java.awt.Color

fun Color.toHexString() = "#${ColorUtil.toHex(this)}"

fun String.toColor() = ColorUtil.fromHex(this)
