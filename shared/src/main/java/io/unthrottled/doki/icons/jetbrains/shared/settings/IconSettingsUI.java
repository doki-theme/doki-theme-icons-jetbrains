package io.unthrottled.doki.icons.jetbrains.shared.settings;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.options.SearchableConfigurable;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.util.NlsContexts;
import io.unthrottled.doki.icons.jetbrains.shared.config.Config;
import io.unthrottled.doki.icons.jetbrains.shared.config.IconConfigListener;
import io.unthrottled.doki.icons.jetbrains.shared.config.IconSettings;
import io.unthrottled.doki.icons.jetbrains.shared.config.IconSettingsModel;
import io.unthrottled.doki.icons.jetbrains.shared.integrations.PluginService;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
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
      initialIconSettingsModel.setUIIcons(UIIconsCheckBox.isSelected()));

    filesCheckBox.setSelected(initialIconSettingsModel.isNamedFileIcons());
    filesCheckBox.addActionListener(e ->
      initialIconSettingsModel.setNamedFileIcons(filesCheckBox.isSelected()));

    PSICheckBox.setSelected(initialIconSettingsModel.isGlyphIcons());
    PSICheckBox.addActionListener(e ->
      initialIconSettingsModel.setGlyphIcons(PSICheckBox.isSelected()));

    foldersCheckBox.setSelected(initialIconSettingsModel.isNamedFolderIcons());
    foldersCheckBox.addActionListener(e ->
      initialIconSettingsModel.setNamedFolderIcons(foldersCheckBox.isSelected()));
    foldersCheckBox.setVisible(false);

    syncWithDokiThemeCheckBox.setEnabled(PluginService.INSTANCE.isDokiThemeInstalled());
    syncWithDokiThemeCheckBox.setSelected(initialIconSettingsModel.getSyncWithDokiTheme());
    syncWithDokiThemeCheckBox.addActionListener(e ->
      initialIconSettingsModel.setSyncWithDokiTheme(syncWithDokiThemeCheckBox.isSelected()));
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
