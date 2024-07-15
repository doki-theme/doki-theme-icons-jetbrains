package io.unthrottled.doki.icons.jetbrains.svg

import com.intellij.ui.svg.SvgAttributePatcher

typealias PatcherProvider = SvgElementColorPatcherProvider
typealias Patcher = SvgAttributePatcher

object NoOptPatcher : Patcher

val emptyLongArray = LongArray(0)

val noOptPatcherProvider =
  object : PatcherProvider {
    override fun attributeForPath(path: String): SvgAttributePatcher {
      return NoOptPatcher
    }

    override fun digest(): LongArray {
      return emptyLongArray
    }
  }
