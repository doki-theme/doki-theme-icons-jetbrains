import com.google.gson.GsonBuilder
import groovy.util.Node
import io.unthrottled.doki.build.jvm.models.IconsAppDefinition
import io.unthrottled.doki.build.jvm.models.MasterThemeDefinition
import io.unthrottled.doki.build.jvm.models.ThemeTemplateDefinition
import io.unthrottled.doki.build.jvm.tools.DefinitionSupplier.createThemeDefinitions
import io.unthrottled.doki.build.jvm.tools.DefinitionSupplier.getAllDokiThemeDefinitions
import io.unthrottled.doki.build.jvm.tools.DokiProduct
import io.unthrottled.doki.build.jvm.tools.GroupToNameMapping.getLafNamePrefix
import io.unthrottled.doki.build.jvm.tools.PathTools.cleanDirectory
import io.unthrottled.doki.build.jvm.tools.PathTools.ensureExists
import java.io.File
import java.nio.file.Files
import java.nio.file.Files.createDirectories
import java.nio.file.Files.exists
import java.nio.file.Files.notExists
import java.nio.file.Files.walk
import java.nio.file.Path
import java.nio.file.Paths.get
import java.nio.file.StandardOpenOption
import java.util.TreeMap
import java.util.stream.Collectors
import java.util.stream.Stream
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

fun String.getStickerName(): String = this.substring(this.lastIndexOf("/") + 1)

data class DokiTheme(
  val id: String,
  val name: String,
  val displayName: String,
  val group: String,
  val listName: String,
  val colors: Map<String, String>,
)

open class BuildThemes : DefaultTask() {

  companion object {
    private const val LAF_TEMPLATE = "LAF"
    private const val COLOR_TEMPLATE = "COLOR"
  }

  private val gson = GsonBuilder().create()

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

    cleanDirectory(getGenerateResourcesDirectory())

    val jetbrainsDokiThemeDefinitionDirectory = getThemeDefinitionDirectory()

    val allDokiThemeDefinitions = getAllDokiThemeDefinitions(
      DokiProduct.ICONS,
      jetbrainsDokiThemeDefinitionDirectory,
      masterThemeDirectory,
      IconsAppDefinition::class.java
    ).collect(Collectors.toList())

    val dokiThemes = allDokiThemeDefinitions
      .map {
        constructDokiTheme(it, dokiThemeTemplates)
      }

    writeThemesAsJson(dokiThemes)
  }

  private fun writeThemesAsJson(dokiThemes: List<DokiTheme>) {
    val directoryToPutStuffIn =
      ensureExists(
        getGenerateResourcesDirectory()
      )

    val dokiThemesPath = get(directoryToPutStuffIn.toString(), "doki-theme-definitions.json");

    Files.newBufferedWriter(dokiThemesPath, StandardOpenOption.CREATE_NEW)
      .use { writer ->
        gson.toJson(dokiThemes, writer)
      }
  }

  private fun constructDokiTheme(
    it: Triple<Path, MasterThemeDefinition, IconsAppDefinition>,
    dokiThemeTemplates: Map<String, Map<String, ThemeTemplateDefinition>>
  ): DokiTheme {
    val (
    path,
      masterThemeDefinition,
    appDefinition
    ) = it
    return DokiTheme(
      id = masterThemeDefinition.id,
      name = masterThemeDefinition.name,
      displayName = masterThemeDefinition.displayName,
      group = masterThemeDefinition.group,
      listName = "${getLafNamePrefix(masterThemeDefinition.group)}${masterThemeDefinition.name}",
      colors = resolveColors(masterThemeDefinition, dokiThemeTemplates),
    )
  }

  private fun getThemeDefinitionDirectory() = get(getBuildAssetDirectory().toString(), "themes")

  private fun getBuildAssetDirectory() = get(project.rootDir.absolutePath, "buildSrc", "assets")

  // todo: common
  private fun resolveColors(
      themeDefinition: MasterThemeDefinition,
      dokiTemplates: Map<String, Map<String, ThemeTemplateDefinition>>
  ): MutableMap<String, String> {
    val templateName = if (themeDefinition.dark) "dark" else "light"
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
    return resolvedNamedColors
  }

  private fun sanitizePath(dirtyPath: String): String =
    dirtyPath.replace(File.separator, "/")

  private fun getGenerateResourcesDirectory(): Path = get(
    getResourcesDirectory().toString(),
    "doki",
    "generated"
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
