package io.unthrottled.doki.icons.jetbrains.path

import com.intellij.openapi.util.IconLoader
import com.intellij.openapi.util.IconPathPatcher

object IconPathReplacementComponent {
  val patcho = object: IconPathPatcher() {
    override fun getContextClassLoader(path: String, originalClassLoader: ClassLoader?): ClassLoader? =
      javaClass.classLoader

    override fun patchPath(path: String, classLoader: ClassLoader?): String? {
     return if(path == "/actions/compile.svg") {
       "/doki/icons/build.svg"
      } else {
        null
     }
    }
  }

  fun initialize() {
    IconLoader.installPathPatcher(patcho)

  }

  fun dispose() {
    IconLoader.removePathPatcher(patcho)
  }
}