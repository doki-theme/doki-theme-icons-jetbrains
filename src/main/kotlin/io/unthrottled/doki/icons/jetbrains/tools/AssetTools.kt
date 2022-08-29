package io.unthrottled.doki.icons.jetbrains.tools

import com.google.gson.Gson
import com.intellij.util.ResourceUtil
import java.io.InputStreamReader
import java.lang.reflect.Type
import java.nio.charset.StandardCharsets
import java.util.Optional

object AssetTools : Logging {
  private val gson = Gson()

  fun <T : Any> readJsonFromResources(filePath: String, type: Type): Optional<T> {
    return runSafelyWithResult({
      ResourceUtil.getResourceAsStream(
        AssetTools::class.java.classLoader,
        "/",
        filePath
      ).use { inputStream ->
        gson.fromJson<T>(
          InputStreamReader(inputStream, StandardCharsets.UTF_8),
          type
        ).toOptional()
      }
    }) {
      logger().error("Unable to read JSON from resources for file $filePath", it)
      Optional.empty()
    }
  }
}
