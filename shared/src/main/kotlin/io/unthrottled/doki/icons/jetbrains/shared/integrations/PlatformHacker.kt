package io.unthrottled.doki.icons.jetbrains.shared.integrations

import com.intellij.ide.ApplicationInitializedListener
import io.unthrottled.doki.icons.jetbrains.shared.path.IconPathReplacementComponent
import io.unthrottled.doki.icons.jetbrains.shared.tools.Logging
import io.unthrottled.doki.icons.jetbrains.shared.tools.logger
import io.unthrottled.doki.icons.jetbrains.shared.tools.runSafely
import javassist.ClassClassPath
import javassist.ClassPool
import javassist.expr.ExprEditor
import javassist.expr.MethodCall
import kotlinx.coroutines.CoroutineScope

object PlatformHacker : ApplicationInitializedListener, Logging {

  init {
    IconPathReplacementComponent.initialize()
    hackPlatform()
  }

  @Deprecated("Use {@link #execute()}", replaceWith = ReplaceWith("execute()"))
  override fun componentsInitialized() {
  }

  override suspend fun execute(asyncScope: CoroutineScope) {
  }

  fun hackPlatform() {
    hackEXPUI()
  }

  private fun hackEXPUI() {
    fixEXPUIButton()
    fixEXPUIRunWidget()
    fixEXPUIStopButton()
  }

  private fun fixEXPUIStopButton() {
    runSafely({
      val cp = ClassPool(true)
      cp.insertClassPath(
        ClassClassPath(
          Class.forName("com.intellij.execution.ui.RunState")
        )
      )
      val ctClass = cp.get("com.intellij.execution.ui.StopWithDropDownAction")
      val doPaintText = ctClass.getDeclaredMethods("update")[0]
      doPaintText.instrument(
        object : ExprEditor() {
          override fun edit(m: MethodCall?) {
            if (m?.methodName == "toStrokeIcon") {
              m.replace("{ \$_ = \$1; }")
            }
          }
        }
      )
      ctClass.toClass()
    }) {
      logger().warn("Unable to hack 'fixEXPUIStopButton' for raisins", it)
    }
  }

  private fun fixEXPUIRunWidget() {
    runSafely({
      val cp = ClassPool(true)
      cp.insertClassPath(
        ClassClassPath(
          Class.forName("com.intellij.execution.ui.RunState")
        )
      )
      val ctClass = cp.get("com.intellij.execution.ui.RunWidgetButtonLook")
      val doPaintText = ctClass.getDeclaredMethods("paintIcon")[0]
      doPaintText.instrument(
        object : ExprEditor() {
          override fun edit(m: MethodCall?) {
            if (m?.methodName == "toStrokeIcon") {
              m.replace("{ \$_ = \$1; }")
            }
          }
        }
      )
      ctClass.toClass()
    }) {
      logger().warn("Unable to hack 'fixEXPUIRunWidget' for raisins", it)
    }
  }

  private fun fixEXPUIButton() {
    runSafely({
      val cp = ClassPool(true)
      cp.insertClassPath(
        ClassClassPath(
          Class.forName("com.intellij.openapi.wm.impl.SideStack")
        )
      )
      val ctClass = cp.get("com.intellij.openapi.wm.impl.SquareStripeButtonLook")
      val doPaintText = ctClass.getDeclaredMethods("paintIcon")[0]
      doPaintText.instrument(
        object : ExprEditor() {
          override fun edit(m: MethodCall?) {
            if (m?.methodName == "toStrokeIcon") {
              m.replace("{ \$_ = \$1; }")
            }
          }
        }
      )
      ctClass.toClass()
    }) {
      logger().warn("Unable to hack 'fixEXPUIButton' for raisins", it)
    }
  }
}
