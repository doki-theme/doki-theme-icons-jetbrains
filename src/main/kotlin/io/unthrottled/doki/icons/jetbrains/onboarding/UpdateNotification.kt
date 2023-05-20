package io.unthrottled.doki.icons.jetbrains.onboarding

import com.intellij.notification.Notification
import com.intellij.notification.NotificationGroupManager
import com.intellij.notification.NotificationListener
import com.intellij.notification.NotificationType
import com.intellij.notification.impl.NotificationsManagerImpl
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.popup.Balloon
import com.intellij.openapi.util.Disposer
import com.intellij.ui.BalloonLayoutData
import icons.DokiThemeIconz.PLUGIN_ICON
import io.unthrottled.doki.icons.jetbrains.Constants.PLUGIN_NAME
import io.unthrottled.doki.icons.jetbrains.tools.BalloonTools.fetchBalloonParameters
import org.intellij.lang.annotations.Language

@Suppress("MaxLineLength")
@Language("HTML")
private fun buildUpdateMessage(): String =
  """
      What's New?<br>
      <ul>
        <li>Added initial 2023.2 build support.</li>
      </ul>
      <br>See the <a href="https://github.com/doki-theme/doki-theme-icons-jetbrains#documentation">documentation</a> for features, usages, and configurations.
      <br>The <a href="https://github.com/doki-theme/doki-theme-icons-jetbrains/blob/master/CHANGELOG.md">changelog</a> is available for more details.
      <br><br>
      Thanks for downloading!
      </div>
  """.trimIndent()

object UpdateNotification {

  private const val UPDATE_CHANNEL_NAME = "$PLUGIN_NAME Updates"
  private val notificationGroup =
    NotificationGroupManager.getInstance()
      .getNotificationGroup(UPDATE_CHANNEL_NAME)

  fun display(
    project: Project,
    newVersion: String
  ) {
    val updateNotification = notificationGroup.createNotification(
      buildUpdateMessage(),
      NotificationType.INFORMATION
    )
      .setTitle("$PLUGIN_NAME updated to v$newVersion")
      .setIcon(PLUGIN_ICON)
      .setListener(NotificationListener.UrlOpeningListener(false))

    showNotification(project, updateNotification)
  }

  fun sendMessage(
    title: String,
    message: String,
    project: Project? = null
  ) {
    showRegularNotification(
      title,
      message,
      project = project,
      listener = defaultListener
    )
  }

  private val defaultListener = NotificationListener.UrlOpeningListener(false)

  private fun showRegularNotification(
    title: String = "",
    content: String,
    project: Project? = null,
    listener: NotificationListener? = defaultListener
  ) {
    notificationGroup.createNotification(
      content,
      NotificationType.INFORMATION
    ).setIcon(PLUGIN_ICON)
      .setTitle(title)
      .setListener(listener ?: defaultListener)
      .notify(project)
  }

  private fun showNotification(
    project: Project,
    updateNotification: Notification
  ) {
    try {
      val (ideFrame, notificationPosition) = fetchBalloonParameters(project)
      val balloon = NotificationsManagerImpl.createBalloon(
        ideFrame,
        updateNotification,
        true,
        false,
        BalloonLayoutData.fullContent(),
        Disposer.newDisposable()
      )
      balloon.show(notificationPosition, Balloon.Position.atLeft)
    } catch (e: Throwable) {
      updateNotification.notify(project)
    }
  }
}
