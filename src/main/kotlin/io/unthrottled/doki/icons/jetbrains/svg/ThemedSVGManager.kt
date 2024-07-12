package io.unthrottled.doki.icons.jetbrains.svg

import com.intellij.openapi.Disposable
import com.intellij.openapi.actionSystem.impl.ActionToolbarImpl
import com.intellij.openapi.application.ApplicationManager
import io.unthrottled.doki.icons.jetbrains.themes.DokiThemePayload
import io.unthrottled.doki.icons.jetbrains.themes.IconThemeManager
import io.unthrottled.doki.icons.jetbrains.themes.ThemeManagerListener
import io.unthrottled.doki.icons.jetbrains.tools.Logging
import io.unthrottled.doki.icons.jetbrains.tools.logger
import io.unthrottled.doki.icons.jetbrains.tools.runSafely
import io.unthrottled.doki.icons.jetbrains.tools.runSafelyWithResult
import java.lang.reflect.InvocationHandler
import java.lang.reflect.Method
import java.lang.reflect.Proxy
import javax.swing.SwingUtilities

class ThemedSVGManager : ThemeManagerListener, Disposable, Logging {
  companion object {
    val instance: ThemedSVGManager =
      ApplicationManager.getApplication().getService(ThemedSVGManager::class.java)
  }

  private val connection = ApplicationManager.getApplication().messageBus.connect()

  init {
    connection.subscribe(IconThemeManager.TOPIC, this)
  }

  fun initialize() {
    IconThemeManager.instance.currentTheme
      .ifPresent {
        activateTheme(it)
      }
  }

  private fun activateTheme(currentTheme: DokiThemePayload) {
    val svgPatcherProvider = ComposedSVGColorizerProviderFactory.createForTheme(currentTheme)
    runSafely({
      setHackedPatcher(svgPatcherProvider)
    }) {
      logger().warn("Unable to set hacked patcher", it)
    }
    SwingUtilities.invokeLater { ActionToolbarImpl.updateAllToolbarsImmediately() }
  }

  private fun setHackedPatcher(svgPatcherProvider: PatcherProvider) {
    val patcherProxyHandler =
      object : InvocationHandler, Logging {
        val associatedMethods = svgPatcherProvider.javaClass.methods.associateBy { it.name }

        override fun invoke(
          proxy: Any?,
          method: Method?,
          arguments: Array<out Any>?,
        ): Any? {
          if (method == null) return null
          return runSafelyWithResult({
            val methodToInvoke = associatedMethods[method.name]
            val usableArguments = arguments ?: emptyArray()
            methodToInvoke?.invoke(
              svgPatcherProvider,
              *usableArguments,
            )
          }) {
            logger().warn("unable to invoke proxy handler method", it)
            null
          }
        }
      }
    val patcherProviderClass = Class.forName("com.intellij.util.SVGLoader\$SvgElementColorPatcherProvider")
    val proxiedSVGElementColorProvider =
      Proxy.newProxyInstance(
        patcherProviderClass.classLoader,
        arrayOf(patcherProviderClass),
        patcherProxyHandler,
      )
    val svgLoaderClass = Class.forName("com.intellij.util.SVGLoader")
    val setPatcher = svgLoaderClass.declaredMethods.firstOrNull { it.name == "setColorPatcherProvider" }
    setPatcher?.invoke(null, proxiedSVGElementColorProvider)

    val clazz = Class.forName("com.intellij.ui.svg.SvgKt")
    val setPatcherProvider = clazz.declaredMethods.firstOrNull { it.name == "setSelectionColorPatcherProvider" }
    setPatcherProvider?.invoke(null, proxiedSVGElementColorProvider)
  }

  override fun dispose() {
    connection.dispose()
  }

  override fun onDokiThemeActivated(dokiThemePayload: DokiThemePayload) {
    activateTheme(dokiThemePayload)
  }

  override fun onDokiThemeRemoved() {}
}
