package io.unthrottled.doki.icons.jetbrains.integrations

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import com.intellij.ide.plugins.PluginManagerCore
import com.intellij.openapi.application.ApplicationInfo
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.application.ex.ApplicationInfoEx
import com.intellij.openapi.extensions.PluginId
import com.intellij.util.Urls
import com.intellij.util.io.HttpRequests
import io.unthrottled.doki.icons.jetbrains.tools.Logging
import io.unthrottled.doki.icons.jetbrains.tools.logger
import io.unthrottled.doki.icons.jetbrains.tools.runSafelyWithResult
import io.unthrottled.doki.icons.jetbrains.tools.toOptional
import java.util.Collections
import java.util.concurrent.Callable

const val MOTIVATOR_PLUGIN_ID = "zd.zero.waifu-motivator-plugin"
const val AMII_PLUGIN_ID = "io.unthrottled.amii"
const val RANDOMIZER_PLUGIN_ID = "io.unthrottled.theme.randomizer"

const val DOKI_THEME_COMMUNITY_PLUGIN_ID = "io.acari.DDLCTheme"
private const val DOKI_THEME_ULTIMATE_PLUGIN_ID = "io.unthrottled.DokiTheme"

object PluginService : Logging {
  private val objectMapper by lazy { ObjectMapper() }

  fun isMotivatorInstalled(): Boolean =
    PluginManagerCore.isPluginInstalled(
      PluginId.getId(MOTIVATOR_PLUGIN_ID),
    )

  fun isDokiThemeInstalled(): Boolean =
    PluginManagerCore.isPluginInstalled(
      PluginId.getId(DOKI_THEME_COMMUNITY_PLUGIN_ID),
    ) ||
      PluginManagerCore.isPluginInstalled(
        PluginId.getId(DOKI_THEME_ULTIMATE_PLUGIN_ID),
      )

  fun isAmiiInstalled(): Boolean =
    PluginManagerCore.isPluginInstalled(
      PluginId.getId(AMII_PLUGIN_ID),
    )

  fun isRandomizerInstalled(): Boolean =
    PluginManagerCore.isPluginInstalled(
      PluginId.getId(RANDOMIZER_PLUGIN_ID),
    )

  private val PLUGIN_MANAGER_URL by lazy {
    ApplicationInfo.getInstance()
      .toOptional()
      .filter { it is ApplicationInfoEx }
      .map { it as ApplicationInfoEx }
      .map { it.pluginManagerUrl }
      .orElseGet { "https://plugins.jetbrains.com" }
      .trimEnd('/')
  }
  private val COMPATIBLE_UPDATE_URL by lazy { "$PLUGIN_MANAGER_URL/api/search/compatibleUpdates" }

  private data class CompatibleUpdateRequest(
    val build: String,
    val pluginXMLIds: List<String>,
  )

  /**
   * Object from Search Service for getting compatible updates for IDE.
   * [externalUpdateId] update ID from Plugin Repository database.
   * [externalPluginId] plugin ID from Plugin Repository database.
   */
  @JsonIgnoreProperties(ignoreUnknown = true)
  data class IdeCompatibleUpdate(
    @get:JsonProperty("id")
    val externalUpdateId: String = "",
    @get:JsonProperty("pluginId")
    val externalPluginId: String = "",
    @get:JsonProperty("pluginXmlId")
    val pluginId: String = "",
    val version: String = "",
  )

  private fun getLastCompatiblePluginUpdate(ids: Set<PluginId>): List<IdeCompatibleUpdate> {
    return try {
      if (ids.isEmpty()) {
        return emptyList()
      }

      val data =
        objectMapper.writeValueAsString(
          CompatibleUpdateRequest(
            ApplicationInfo.getInstance()
              .build.asString(),
            ids.map { it.idString },
          ),
        )
      HttpRequests
        .post(Urls.newFromEncoded(COMPATIBLE_UPDATE_URL).toExternalForm(), HttpRequests.JSON_CONTENT_TYPE)
        .productNameAsUserAgent()
        .throwStatusCodeException(false)
        .connect {
          it.write(data)
          objectMapper.readValue(it.inputStream, object : TypeReference<List<IdeCompatibleUpdate>>() {})
        }
    } catch (e: Exception) {
      logger().warn("Unable to check to see if plugin $ids is compatible for reasons", e)
      emptyList()
    }
  }

  fun canAmiiBeInstalled(): Boolean {
    return ApplicationManager.getApplication().executeOnPooledThread(
      Callable {
        runSafelyWithResult({
          val pluginId = PluginId.getId(AMII_PLUGIN_ID)
          val lastCompatiblePluginUpdate =
            getLastCompatiblePluginUpdate(
              Collections.singleton(pluginId),
            )
          lastCompatiblePluginUpdate.firstOrNull { pluginNode ->
            pluginNode.pluginId == AMII_PLUGIN_ID
          } != null
        }) {
          logger().warn("Unable to check to see if AMII can be installed", it)
          false
        }
      },
    ).get()
  }
}
