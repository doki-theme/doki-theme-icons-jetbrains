package io.unthrottled.doki.icons.jetbrains.settings;

import com.intellij.ide.BrowserUtil;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.options.SearchableConfigurable;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.util.NlsContexts;
import com.intellij.ui.components.ActionLink;
import icons.DokiThemeIconz;
import io.unthrottled.doki.icons.jetbrains.Constants;
import io.unthrottled.doki.icons.jetbrains.config.Config;
import io.unthrottled.doki.icons.jetbrains.config.IconConfigListener;
import io.unthrottled.doki.icons.jetbrains.config.IconSettings;
import io.unthrottled.doki.icons.jetbrains.config.IconSettingsModel;
import io.unthrottled.doki.icons.jetbrains.integrations.PluginService;
import io.unthrottled.doki.icons.jetbrains.tools.PluginMessageBundle;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;

public class IconSettingsUI implements SearchableConfigurable, Configurable.NoScroll, DumbAware {

  private final IconSettingsModel iconSettingsModel = IconSettings.createSettingsModule();
  private IconSettingsModel initialIconSettingsModel = IconSettings.createSettingsModule();


  private JPanel rootPane;
  private JComboBox currentThemeWomboComboBox;
  private JCheckBox syncWithDokiThemeCheckBox;
  private JCheckBox UIIconsCheckBox;
  private JCheckBox foldersCheckBox;
  private JCheckBox filesCheckBox;
  private JCheckBox PSICheckBox;
  private JLabel uiIcon;
  private JLabel namedFoldersIcon;
  private JLabel namedFilesIcon;
  private JLabel fileGlyphs;
  private ActionLink iconRequest;
  private ActionLink reportIssue;
  private JCheckBox myIconsCheckBox;
  private JLabel myIconsIcon;

  @Override
  public @NotNull @NonNls String getId() {
    return IconSettings.SETTINGS_ID;
  }

  @Override
  public @NlsContexts.ConfigurableName String getDisplayName() {
    return IconSettings.ICON_SETTINGS_DISPLAY_NAME;
  }

  @Override
  public @Nullable JComponent createComponent() {
    initializeAutoCreatedComponents();
    return rootPane;
  }

  private void initializeAutoCreatedComponents() {
    UIIconsCheckBox.setSelected(initialIconSettingsModel.isUIIcons());
    UIIconsCheckBox.addActionListener(e ->
      iconSettingsModel.setUIIcons(UIIconsCheckBox.isSelected()));
    uiIcon.setIcon(DokiThemeIconz.PROJECT_GLYPH);

    filesCheckBox.setSelected(initialIconSettingsModel.isNamedFileIcons());
    filesCheckBox.addActionListener(e ->
      iconSettingsModel.setNamedFileIcons(filesCheckBox.isSelected()));
    namedFilesIcon.setIcon(DokiThemeIconz.CURLY_BRACES);
    namedFilesIcon.setVisible(false);
    filesCheckBox.setVisible(false);

    PSICheckBox.setSelected(initialIconSettingsModel.isGlyphIcons());
    PSICheckBox.addActionListener(e ->
      iconSettingsModel.setGlyphIcons(PSICheckBox.isSelected()));
    fileGlyphs.setIcon(DokiThemeIconz.SOLID_DOKI_GLYPH);

    foldersCheckBox.setSelected(initialIconSettingsModel.isNamedFolderIcons());
    foldersCheckBox.addActionListener(e ->
      iconSettingsModel.setNamedFolderIcons(foldersCheckBox.isSelected()));
    foldersCheckBox.setVisible(false);
    namedFoldersIcon.setVisible(false);

    myIconsCheckBox.setSelected(initialIconSettingsModel.isMyIcons());
    myIconsCheckBox.addActionListener(e ->
      iconSettingsModel.setMyIcons(myIconsCheckBox.isSelected()));
    myIconsIcon.setIcon(DokiThemeIconz.MAMSNRHBR_CHEHFDE);

    boolean dokiThemeInstalled = PluginService.INSTANCE.isDokiThemeInstalled();
    syncWithDokiThemeCheckBox.setEnabled(dokiThemeInstalled);
    syncWithDokiThemeCheckBox.setSelected(initialIconSettingsModel.getSyncWithDokiTheme());
    syncWithDokiThemeCheckBox.addActionListener(e -> {
      iconSettingsModel.setSyncWithDokiTheme(syncWithDokiThemeCheckBox.isSelected());
      currentThemeWomboComboBox.setEnabled(!syncWithDokiThemeCheckBox.isSelected());
    });

    currentThemeWomboComboBox.setEnabled(
      !(dokiThemeInstalled &&
        initialIconSettingsModel.getSyncWithDokiTheme())
    );

    iconRequest.setIcon(DokiThemeIconz.WATCH);
    iconRequest.setText(PluginMessageBundle.message("settings.icon.request"));
    iconRequest.addActionListener(e -> {
      BrowserUtil.browse(Constants.REPO_URL + "/issues/new?assignees=Unthrottled&labels=enhancement&template=ICON_REQUEST.yml&title=%5BICON%5D%3A+");
    });

    reportIssue.setIcon(DokiThemeIconz.SOLID_ERROR);
    reportIssue.setText(PluginMessageBundle.message("settings.report.bug"));
    reportIssue.addActionListener(e -> {
      BrowserUtil.browse(Constants.REPO_URL + "/issues/new?assignees=Unthrottled&labels=bug%2Ctriage&template=BUG_REPORT.yml&title=[Bug]%3A+");
    });
  }

  @Override
  public boolean isModified() {
    return !initialIconSettingsModel.equals(iconSettingsModel);
  }

  @Override
  public void apply() throws ConfigurationException {
    Config config = Config.getInstance();
    config.setUIIcons(UIIconsCheckBox.isSelected());
    config.setNamedFileIcons(filesCheckBox.isSelected());
    config.setGlyphIcon(PSICheckBox.isSelected());
    config.setNamedFolderIcons(foldersCheckBox.isSelected());
    config.setCurrentThemeId(iconSettingsModel.getCurrentThemeId());
    config.setSyncWithDokiTheme(syncWithDokiThemeCheckBox.isSelected());
    config.setMyIcons(myIconsCheckBox.isSelected());

    ApplicationManager.getApplication()
      .getMessageBus()
      .syncPublisher(IconConfigListener.getTOPIC())
      .iconConfigUpdated(initialIconSettingsModel, iconSettingsModel);
    initialIconSettingsModel = iconSettingsModel;
  }

  private void createUIComponents() {
    currentThemeWomboComboBox = IconSettings.INSTANCE.createThemeComboBoxModel(
      () -> this.iconSettingsModel == null ?
        IconSettings.createSettingsModule() :
        iconSettingsModel
    );
  }
}
