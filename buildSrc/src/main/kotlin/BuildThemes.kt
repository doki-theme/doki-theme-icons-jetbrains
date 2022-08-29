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
import io.unthrottled.doki.build.jvm.tools.PathTools.ensureDirectoryExists
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

// todo figure out how to share.
data class DokiTheme(
  val id: String,
  val name: String,
  val displayName: String,
  val group: String,
  val listName: String,
  val colors: Map<String, String>,
)

open class BuildThemes : DefaultTask() {

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
      ensureDirectoryExists(
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
      _,
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

  private fun extractResourcesPath(destination: Path): String {
    val fullResourcesPath = destination.toString()
    val separator = File.separator
    val editorPathResources =
      fullResourcesPath.substring(fullResourcesPath.indexOf("${separator}doki${separator}theme"))
    return editorPathResources.replace(separator.toString(), "/")
  }
}
