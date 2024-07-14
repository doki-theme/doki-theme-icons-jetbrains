package io.unthrottled.doki.icons.jetbrains.integrations

import io.unthrottled.doki.icons.jetbrains.path.IconPathReplacementComponent
import io.unthrottled.doki.icons.jetbrains.tools.Logging
import io.unthrottled.doki.icons.jetbrains.tools.logger
import io.unthrottled.doki.icons.jetbrains.tools.runSafely
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
    fixEXPUIDefaultButton()
    fixEXPUIRunWidget()
    fixEXPUIStopButton()
  }

  private fun fixEXPUIStopButton() {
    runSafely({
      val cp = ClassPool(true)
      cp.insertClassPath(
        ClassClassPath(
          Class.forName("com.intellij.execution.ui.RunState"),
        ),
      )
      val ctClass = cp.get("com.intellij.execution.ui.StopWithDropDownAction")
      ctClass.getDeclaredMethods("update").forEach { doPaintText ->
        doPaintText.instrument(
          object : ExprEditor() {
            override fun edit(m: MethodCall?) {
              if (m?.methodName == "toStrokeIcon") {
                m.replace("{ \$_ = \$1; }")
              }
            }
          },
        )
      }
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
          Class.forName("com.intellij.execution.ui.RunState"),
        ),
      )
      val ctClass = cp.get("com.intellij.execution.ui.RunWidgetButtonLook")
      ctClass.getDeclaredMethods("paintIcon").forEach { doPaintText ->
        doPaintText.instrument(
          object : ExprEditor() {
            override fun edit(m: MethodCall?) {
              if (m?.methodName == "toStrokeIcon") {
                m.replace("{ \$_ = \$1; }")
              }
            }
          },
        )
      }
      ctClass.toClass()
    }) {
      logger().warn("Unable to hack 'fixEXPUIRunWidget' for raisins", it)
    }

    runSafely({
      val cp = ClassPool(true)
      cp.insertClassPath(
        ClassClassPath(
          Class.forName("com.intellij.execution.ui.RunState"),
        ),
      )
      val ctClass = cp.get("com.intellij.execution.ui.RedesignedRunConfigurationSelector")
      ctClass.getDeclaredMethods("update").forEach { doPaintText ->
        doPaintText.instrument(
          object : ExprEditor() {
            override fun edit(m: MethodCall?) {
              if (m?.methodName == "toStrokeIcon") {
                m.replace("{ \$_ = \$1; }")
              }
            }
          },
        )
      }
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
          Class.forName("com.intellij.openapi.wm.impl.SideStack"),
        ),
      )
      val ctClass = cp.get("com.intellij.openapi.wm.impl.SquareStripeButtonLook")
      ctClass.getDeclaredMethods("paintIcon").forEach { doPaintText ->
        doPaintText.instrument(
          object : ExprEditor() {
            override fun edit(m: MethodCall?) {
              if (m?.methodName == "toStrokeIcon") {
                m.replace("{ \$_ = \$1; }")
              }
            }
          },
        )
      }
      ctClass.toClass()
    }) {
      logger().warn("Unable to hack 'fixEXPUIButton' for raisins", it)
    }
  }
  private fun fixEXPUIDefaultButton() {
    runSafely({
      val cp = ClassPool(true)
      cp.insertClassPath(
        ClassClassPath(
          Class.forName("com.intellij.ide.ui.laf.darcula.ui.DarculaMenuSeparatorUI"),
        ),
      )
      val ctClass = cp.get("com.intellij.ide.ui.laf.darcula.ui.DarculaOptionButtonUI")
      ctClass.getDeclaredMethods("paintArrow").forEach { doPaintText ->
        doPaintText.instrument(
          object : ExprEditor() {
            override fun edit(m: MethodCall?) {
              if (m?.methodName == "toStrokeIcon") {
                m.replace("{ \$_ = \$1; }")
              }
            }
          },
        )
      }
      ctClass.toClass()
    }) {
      logger().warn("Unable to hack 'fixEXPUIDefaultButton' for raisins", it)
    }
  }
}
