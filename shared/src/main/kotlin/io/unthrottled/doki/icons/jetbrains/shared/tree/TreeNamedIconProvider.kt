package io.unthrottled.doki.icons.jetbrains.shared.tree

import com.intellij.ide.IconProvider
import com.intellij.openapi.project.DumbAware
import com.intellij.psi.PsiDirectory
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.util.PsiUtilCore
import io.unthrottled.doki.icons.jetbrains.shared.config.Config
import io.unthrottled.doki.icons.jetbrains.shared.tools.toOptional
import javax.swing.Icon

class TreeNamedIconProvider : IconProvider(), DumbAware {

  override fun getIcon(element: PsiElement, flags: Int): Icon? =
    when (element) {
      is PsiDirectory -> getDirectoryIcon(element)
      is PsiFile -> getFileIcon(element)
      else -> null
    }

  private fun getDirectoryIcon(element: PsiDirectory): Icon? =
    provideIcon(Config.instance.isUIIcons, element) { RainbowDirectoryIconProvider.getNamedIcon(it) }

  private fun getFileIcon(element: PsiFile): Icon? =
    provideIcon(Config.instance.isNamedFileIcons, element) { NamedFileIconProvider.getNamedIcon(it) }

  private fun provideIcon(
    configOption: Boolean,
    element: PsiElement,
    function: (t: VirtualFileInfo) -> Icon?
  ): Icon? = configOption.toOptional()
    .filter { it }
    .flatMap { PsiUtilCore.getVirtualFile(element).toOptional() }
    .map { VirtualFileInfo(element, it) }
    .map(function)
    .orElseGet { null }
}
