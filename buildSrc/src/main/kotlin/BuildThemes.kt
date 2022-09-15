import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import io.unthrottled.doki.build.jvm.models.AssetTemplateDefinition
import io.unthrottled.doki.build.jvm.models.IconsAppDefinition
import io.unthrottled.doki.build.jvm.models.MasterThemeDefinition
import io.unthrottled.doki.build.jvm.tools.BuildFunctions
import io.unthrottled.doki.build.jvm.tools.BuildFunctions.combineMaps
import io.unthrottled.doki.build.jvm.tools.CommonConstructionFunctions.getAllDokiThemeDefinitions
import io.unthrottled.doki.build.jvm.tools.ConstructableAssetSupplier
import io.unthrottled.doki.build.jvm.tools.ConstructableAssetSupplierFactory
import io.unthrottled.doki.build.jvm.tools.ConstructableTypes
import io.unthrottled.doki.build.jvm.tools.DokiProduct
import io.unthrottled.doki.build.jvm.tools.GroupToNameMapping.getLafNamePrefix
import io.unthrottled.doki.build.jvm.tools.PathTools.cleanDirectory
import io.unthrottled.doki.build.jvm.tools.PathTools.ensureDirectoryExists
import io.unthrottled.doki.build.jvm.tools.PathTools.readJSONFromFile
import java.io.File
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.nio.file.Paths.get
import java.nio.file.StandardOpenOption
import java.util.stream.Collectors
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

data class IconPathMapping(
  val iconName: String,
)

open class BuildThemes : DefaultTask() {

  private val gson = GsonBuilder()
    .create()

  init {
    group = "doki"
    description = "Builds all the themes and places them in the proper places"
  }

  @TaskAction
  fun run() {
    val buildSourceAssetDirectory = getBuildSourceAssetDirectory()
    val masterThemesDirectory = get(project.rootDir.absolutePath, "masterThemes")
    val constructableAssetSupplier =
      ConstructableAssetSupplierFactory.createCommonAssetsTemplate(
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

    copyUsedIconsFromIconSource()

    copyIconPaletteFromIconSource()
  }

  private fun copyIconPaletteFromIconSource() {
    Files.copy(
      Paths.get(
        iconSourceDirectory().toAbsolutePath().toString(),
        "buildSrc",
        "assets",
        "templates",
        "icon.palette.template.json"
      ),
      Paths.get(
        getGenerateResourcesDirectory().toAbsolutePath().toString(),
        "icon.palette.template.json"
      )
    )
  }

  private fun copyUsedIconsFromIconSource() {
    ensureDirectoryExists(getIconsDirectory())
    cleanDirectory(getIconsDirectory())

    val allUsedIcons = arrayListOf(
      "files.named.mappings.json",
      "glyph-icons.path.mappings.json",
      "ui-icons.path.mappings.json",
    )
      .flatMap { mappingPak ->
        readJSONFromFile(
          getFileFromResources(mappingPak),
          object : TypeToken<List<IconPathMapping>>() {}
        )
      }
      .map { it.iconName }
      .toMutableSet()

    allUsedIcons.addAll(
      readJSONFromFile(
        get(
          getBuildSourceAssetDirectory().toAbsolutePath().toString(),
          "templates",
          "specialUsedIcons.json"
        ),
        object : TypeToken<List<String>>() {}
      )
    )

    Files.walk(svgIconSourceDirectory())
      .filter {
        allUsedIcons.contains(it.fileName.toString())
      }
      .forEach { dokiIconPath ->
        Files.copy(
          dokiIconPath,
          Paths.get(
            getIconsDirectory().toAbsolutePath().toString(),
            dokiIconPath.fileName.toString()
          ),
        )
      }
  }

  private fun getFileFromResources(mappingFile: String): Path = get(
    getResourcesDirectory().toAbsolutePath().toString(),
    mappingFile,
  )

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
      BuildFunctions.resolveTemplateWithCombini(
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

  private fun getIconsDirectory(): Path = get(
    getResourcesDirectory().toString(),
    "doki",
    "icons"
  )

  private fun getResourcesDirectory(): Path = get(
    project.rootDir.absolutePath,
    "shared",
    "src",
    "main",
    "resources"
  )

  private fun iconSourceDirectory(): Path = get(
    project.rootDir.absolutePath,
    "iconSource",
  )
  private fun svgIconSourceDirectory(): Path = get(
    iconSourceDirectory().toAbsolutePath().toString(),
    "icons",
  )

  private fun extractResourcesPath(destination: Path): String {
    val fullResourcesPath = destination.toString()
    val separator = File.separator
    val editorPathResources =
      fullResourcesPath.substring(fullResourcesPath.indexOf("${separator}doki${separator}theme"))
    return editorPathResources.replace(separator.toString(), "/")
  }
}
