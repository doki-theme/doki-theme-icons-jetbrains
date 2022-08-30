package io.unthrottled.doki.icons.jetbrains.tree

import com.intellij.ide.IconProvider
import com.intellij.openapi.project.DumbAware
import com.intellij.psi.PsiDirectory
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.util.PsiUtilCore
import io.unthrottled.doki.icons.jetbrains.themes.IconThemeManager
import io.unthrottled.doki.icons.jetbrains.tools.toOptional
import javax.swing.Icon

class TreeIconProvider : IconProvider(), DumbAware {

  override fun getIcon(element: PsiElement, flags: Int): Icon? =
    when (element) {
      is PsiDirectory -> getDirectoryIcon(element)
      is PsiFile -> getFileIcon(element)
      else -> null
    }

  private fun getDirectoryIcon(element: PsiDirectory): Icon? {
    return IconThemeManager.instance.currentTheme // todo: not this.
      .flatMap { PsiUtilCore.getVirtualFile(element).toOptional() }
      .map { VirtualFileInfo(element, it) }
      .map { DirectoryIconProvider.getIcon(it) }
      .orElseGet { null }
  }

  private fun getFileIcon(element: PsiFile): Icon? {
    return IconThemeManager.instance.currentTheme
      .flatMap { PsiUtilCore.getVirtualFile(element).toOptional() }
      .map { VirtualFileInfo(element, it) }
      .map { FileIconProvider.getIcon(it) }
      .orElseGet { null }
  }
}
