package io.unthrottled.doki.icons.jetbrains.shared.integrations

import io.unthrottled.doki.icons.jetbrains.shared.path.IconPathReplacementComponent
import io.unthrottled.doki.icons.jetbrains.shared.tools.Logging
import io.unthrottled.doki.icons.jetbrains.shared.tools.logger
import io.unthrottled.doki.icons.jetbrains.shared.tools.runSafely
import javassist.ClassClassPath
import javassist.ClassPool
import javassist.expr.ExprEditor
import javassist.expr.MethodCall

object PlatformHacker : Logging {

  init {
    IconPathReplacementComponent.installComponents()
    hackPlatform()
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

    runSafely({
      val cp = ClassPool(true)
      cp.insertClassPath(
        ClassClassPath(
          Class.forName("com.intellij.execution.ui.RunState")
        )
      )
      val ctClass = cp.get("com.intellij.execution.ui.RedesignedRunConfigurationSelector")
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
      logger().warn("Unable to hack 'fixEXPUIRunWidget' try two for raisins", it)
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
