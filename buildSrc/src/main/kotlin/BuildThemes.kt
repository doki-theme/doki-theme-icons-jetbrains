import com.google.gson.GsonBuilder
import groovy.util.Node
import groovy.util.NodeList
import io.unthrottled.doki.build.jvm.tools.GroupToNameMapping.getLafNamePrefix
import io.unthrottled.doki.build.jvm.models.*
import io.unthrottled.doki.build.jvm.tools.DefinitionSupplier.createThemeDefinitions
import io.unthrottled.doki.build.jvm.tools.DefinitionSupplier.getAllDokiThemeDefinitions
import io.unthrottled.doki.build.jvm.tools.DokiProduct
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction
import java.io.File
import java.io.InputStreamReader
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Files.*
import java.nio.file.Path
import java.nio.file.Paths.get
import java.nio.file.StandardCopyOption
import java.nio.file.StandardOpenOption
import java.util.*
import java.util.regex.Pattern
import java.util.stream.Collectors
import java.util.stream.Stream

fun String.getStickerName(): String = this.substring(this.lastIndexOf("/") + 1)

open class BuildThemes : DefaultTask() {

  companion object {
    private const val LAF_TEMPLATE = "LAF"
    private const val COLOR_TEMPLATE = "COLOR"
  }

  private val gson = GsonBuilder().setPrettyPrinting().create()

  init {
    group = "doki"
    description = "Builds all the themes and places them in the proper places"
  }

  @TaskAction
  fun run() {
    val buildAssetDirectory = getBuildAssetDirectory()
    val masterThemeDirectory = get(project.rootDir.absolutePath, "masterThemes")
    val dokiThemeTemplates = createThemeDefinitions(
      buildAssetDirectory,
      masterThemeDirectory
    )

    cleanThemeDirectory()

    val jetbrainsDokiThemeDefinitionDirectory = getThemeDefinitionDirectory()

    // todo: create one big json file with all the theme definitions!
    getAllDokiThemeDefinitions(
      DokiProduct.ICONS,
      jetbrainsDokiThemeDefinitionDirectory,
      masterThemeDirectory,
      IconsAppDefinition::class.java
    )
      .forEach { pathMasterDefinitionAndJetbrainsDefinition ->
        val dokiThemeResourcePath = constructIntellijTheme(
          pathMasterDefinitionAndJetbrainsDefinition,
          dokiThemeTemplates,
        )
      }
  }

  private fun getThemeDefinitionDirectory() = get(getBuildAssetDirectory().toString(), "themes")

  private fun getBuildAssetDirectory() = get(project.rootDir.absolutePath, "buildSrc", "assets")

  private fun constructIntellijTheme(
    pathMasterAndJetbrainsDefinition: Triple<Path, MasterThemeDefinition, IconsAppDefinition>,
    dokiTemplates: Map<String, Map<String, ThemeTemplateDefinition>>,
  ): String {
    val (
      dokiThemeDefinitionPath,
      themeDefinition,
      jetbrainsDefinition
    ) = pathMasterAndJetbrainsDefinition
    val resourceDirectory = getResourceDirectory(themeDefinition)
    if (!exists(resourceDirectory)) {
      createDirectories(resourceDirectory)
    }

    val themeJson = get(resourceDirectory.toString(), "${themeDefinition.usableName}.theme.json")

    if (exists(themeJson)) {
      delete(themeJson)
    }


    val templateName = if (themeDefinition.dark) "dark" else "light"
    val dokiThemeTemplates =
      dokiTemplates[LAF_TEMPLATE] ?: throw IllegalStateException("Expected the $LAF_TEMPLATE template to be present")

    val dokiColorTemplates =
      dokiTemplates[COLOR_TEMPLATE]
        ?: throw IllegalStateException("Expected the $COLOR_TEMPLATE template to be present")
    val resolvedNamedColors = resolveAttributes(
      dokiColorTemplates[templateName] ?: throw IllegalStateException("Theme $templateName does not exist."),
      dokiColorTemplates,
      themeDefinition.colors
    ) {
      it.colors ?: throw IllegalArgumentException("Expected the $LAF_TEMPLATE to have a 'colors' attribute")
    } as MutableMap<String, String>
    val name = "${getLafNamePrefix(themeDefinition.group)}${themeDefinition.name}"

    return extractResourcesPath(themeJson)
  }

  private fun sanitizePath(dirtyPath: String): String =
    dirtyPath.replace(File.separator, "/")

  private fun cleanThemeDirectory() {
    val themeDirectory = get(
      getResourcesDirectory().toString(),
      "doki",
      "themes"
    )
    if (notExists(themeDirectory)) {
      createDirectories(themeDirectory)
    } else {
      walk(themeDirectory)
        .sorted(Comparator.reverseOrder())
        .forEach(Files::delete)
    }
  }

  private fun getResourceDirectory(masterThemeDefinition: MasterThemeDefinition): Path = get(
    getResourcesDirectory().toString(),
    "doki",
    "themes",
    masterThemeDefinition.usableGroup.toLowerCase()
  )

  private fun getResourcesDirectory(): Path = get(
    project.rootDir.absolutePath,
    "src",
    "main",
    "resources"
  )
  private fun resolveNamedColorsForMap(
    resolveAttributes: MutableMap<String, Any>,
    colors: Map<String, String>
  ): TreeMap<String, Any> = resolveAttributes.entries
    .stream()
    .map {
      it.key to when (val value = it.value) {
        is String -> resolveStringTemplate(value, colors)
        is Map<*, *> -> resolveNamedColorsForMap(value as MutableMap<String, Any>, colors)
        else -> value
      }
    }
    .collect(Collectors.toMap({ it.first }, { it.second }, { _, b -> b },
      { TreeMap(Comparator.comparing { item -> item.toLowerCase() }) })
    )

  private fun resolveStringTemplate(value: String, colors: Map<String, String>): String =
    if (value.contains('&')) {
      val (end, replacementColor) = getReplacementColor(value, '&') { templateColor ->
        colors[templateColor]
          ?: throw IllegalArgumentException("$templateColor is not in the color definition.")
      }
      '#' + buildReplacement(replacementColor, value, end)
    } else {
      value
    }

  private fun resolveAttributes(
    childTemplate: ThemeTemplateDefinition,
    dokiTemplateDefinitions: Map<String, ThemeTemplateDefinition>,
    definitionAttributes: Map<String, Any>,
    attributeExtractor: (ThemeTemplateDefinition) -> Map<String, Any>
  ): MutableMap<String, Any> =
    Stream.of(
      resolveTemplate(childTemplate, dokiTemplateDefinitions, attributeExtractor),
      definitionAttributes.entries.stream()
    )
      .flatMap { it }
      .collect(Collectors.toMap({ it.key }, { it.value }, { _, b -> b },
        { TreeMap(Comparator.comparing { item -> item.toLowerCase() }) })
      )

  private fun resolveTemplate(
    childTemplate: ThemeTemplateDefinition,
    allThemeTemplates: Map<String, ThemeTemplateDefinition>,
    entryExtractor: (ThemeTemplateDefinition) -> Map<String, Any>
  ): Stream<Map.Entry<String, Any>> =
    if (childTemplate.extends == null) {
      entryExtractor(childTemplate).entries.stream()
    } else {
      Stream.concat(
        resolveTemplate(
          allThemeTemplates[childTemplate.extends]
            ?: throw IllegalStateException("Theme template ${childTemplate.extends} is not a valid parent template"),
          allThemeTemplates,
          entryExtractor
        ), entryExtractor(childTemplate).entries.stream()
      )
    }

  private fun getComparable(left: Node): String =
    (left.attribute("name") as? String) ?: left.name().toString()

  private fun buildReplacement(replacementColor: String, value: String, end: Int) =
    "$replacementColor${value.substring(end + 1)}"

  private fun getReplacementColor(
    value: String,
    templateDelemiter: Char,
    replacementSupplier: (CharSequence) -> String
  ): Pair<Int, String> {
    val start = value.indexOf(templateDelemiter)
    val end = value.lastIndexOf(templateDelemiter)
    val templateColor = value.subSequence(start + 1, end)
    val replacementHexColor = replacementSupplier(templateColor)
    val replacementColor = replacementHexColor.substring(1)
    return Pair(end, replacementColor)
  }


  private fun extractResourcesPath(destination: Path): String {
    val fullResourcesPath = destination.toString()
    val separator = File.separator
    val editorPathResources =
      fullResourcesPath.substring(fullResourcesPath.indexOf("${separator}doki${separator}theme"))
    return editorPathResources.replace(separator.toString(), "/")
  }
}
