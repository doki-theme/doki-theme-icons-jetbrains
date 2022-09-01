package io.unthrottled.doki.icons.jetbrains.tree

import com.intellij.ide.IconProvider
import com.intellij.openapi.project.DumbAware
import com.intellij.psi.PsiDirectory
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.util.PsiUtilCore
import io.unthrottled.doki.icons.jetbrains.config.Config
import io.unthrottled.doki.icons.jetbrains.tools.toOptional
import javax.swing.Icon

class TreeIconProvider : IconProvider(), DumbAware {

  override fun getIcon(element: PsiElement, flags: Int): Icon? =
    when (element) {
      is PsiDirectory -> getDirectoryIcon(element)
      is PsiFile -> getFileIcon(element)
      else -> null
    }

  private fun getDirectoryIcon(element: PsiDirectory): Icon? =
    provideIcon(Config.instance.isFolderIcons, element) { DirectoryIconProvider.getIcon(it) }

  private fun getFileIcon(element: PsiFile): Icon? =
    provideIcon(Config.instance.isFileIcons, element) { FileIconProvider.getIcon(it) }

  // todo: make sure refreshes
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
