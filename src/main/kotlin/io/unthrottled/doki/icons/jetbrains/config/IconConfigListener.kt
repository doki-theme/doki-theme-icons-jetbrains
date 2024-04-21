package io.unthrottled.doki.icons.jetbrains.config

import com.intellij.util.messages.Topic
import java.util.EventListener

interface IconConfigListener : EventListener {
  companion object {
    @JvmStatic
    val TOPIC: Topic<IconConfigListener> =
      Topic(IconConfigListener::class.java)
  }

  fun iconConfigUpdated(
    previousState: IconSettingsModel,
    newState: IconSettingsModel,
  )
}
