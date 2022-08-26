import com.google.gson.GsonBuilder
import groovy.util.Node
import io.unthrottled.doki.build.jvm.models.AssetTemplateDefinition
import io.unthrottled.doki.build.jvm.models.IconsAppDefinition
import io.unthrottled.doki.build.jvm.models.MasterThemeDefinition
import io.unthrottled.doki.build.jvm.tools.BuildTools
import io.unthrottled.doki.build.jvm.tools.BuildTools.combineMaps
import io.unthrottled.doki.build.jvm.tools.ConstructableAssetSupplier
import io.unthrottled.doki.build.jvm.tools.ConstructableTypes
import io.unthrottled.doki.build.jvm.tools.DefinitionSupplier
import io.unthrottled.doki.build.jvm.tools.DefinitionSupplier.getAllDokiThemeDefinitions
import io.unthrottled.doki.build.jvm.tools.DokiProduct
import io.unthrottled.doki.build.jvm.tools.GroupToNameMapping.getLafNamePrefix
import io.unthrottled.doki.build.jvm.tools.PathTools.cleanDirectory
import io.unthrottled.doki.build.jvm.tools.PathTools.ensureExists
import java.io.File
import java.nio.file.Files
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
    val buildSourceAssetDirectory = getBuildSourceAssetDirectory()
    val masterThemesDirectory = get(project.rootDir.absolutePath, "masterThemes")
    val constructableAssetSupplier =
      DefinitionSupplier.createCommonAssetsTemplate(
        buildSourceAssetDirectory,
        masterThemesDirectory
      )

    cleanDirectory(getGenerateResourcesDirectory())

    val jetbrainsIconsThemeDirectory = getThemeDefinitionDirectory()

    val allDokiThemeDefinitions = getAllDokiThemeDefinitions(
      DokiProduct.ICONS,
      jetbrainsIconsThemeDirectory,
      masterThemesDirectory,
      IconsAppDefinition::class.java
    ).collect(Collectors.toList())

    val dokiThemes = allDokiThemeDefinitions
      .map {
        constructDokiTheme(it, constructableAssetSupplier)
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
    constructableAssetSupplier: ConstructableAssetSupplier
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
      colors = resolveColors(
        masterThemeDefinition,
        appDefinition,
        constructableAssetSupplier
      ),
    )
  }

  private fun getThemeDefinitionDirectory() = get(getBuildSourceAssetDirectory().toString(), "themes")

  private fun getBuildSourceAssetDirectory() = get(project.rootDir.absolutePath, "buildSrc", "assets")

  private fun resolveColors(
    masterThemeDefinition: MasterThemeDefinition,
    iconsAppDefinition: IconsAppDefinition,
    constructableAssetSupplier: ConstructableAssetSupplier
  ): MutableMap<String, String> {
    val templateName = if (masterThemeDefinition.dark) "dark" else "light"
    return constructableAssetSupplier.getConstructableAsset(
      ConstructableTypes.Color
    ).map { colorAsset ->
      BuildTools.resolveTemplateWithCombini(
        AssetTemplateDefinition(
          colors = combineMaps(
            masterThemeDefinition.colors,
            iconsAppDefinition.colors,
          ),
          name = "app color template",
          extends = templateName,
        ),
        colorAsset.definitions,
        { it.colors!! },
        { it.extends },
        { parent, child ->
          combineMaps(parent, child)
        }
      )
    }.map {
      it.toMutableMap()
    }
      .orElseGet {
        masterThemeDefinition.colors.toMutableMap()
      }
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
