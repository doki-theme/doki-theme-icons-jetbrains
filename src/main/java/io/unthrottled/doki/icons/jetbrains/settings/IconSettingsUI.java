package io.unthrottled.doki.icons.jetbrains.settings;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.options.SearchableConfigurable;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.util.NlsContexts;
import io.unthrottled.doki.icons.jetbrains.config.Config;
import io.unthrottled.doki.icons.jetbrains.config.IconConfigListener;
import io.unthrottled.doki.icons.jetbrains.config.IconSettings;
import io.unthrottled.doki.icons.jetbrains.config.IconSettingsModel;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.JComponent;
import javax.swing.JPanel;

public class IconSettingsUI implements SearchableConfigurable, Configurable.NoScroll, DumbAware {

  private final IconSettingsModel iconSettingsModel = IconSettings.createSettingsModule();
  private IconSettingsModel initialIconSettingsModel = IconSettings.createSettingsModule();


  private JPanel rootPane;

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

  }

  @Override
  public boolean isModified() {
    return !initialIconSettingsModel.equals(iconSettingsModel);
  }

  @Override
  public void apply() throws ConfigurationException {
    Config config = Config.getInstance();
    ApplicationManager.getApplication()
      .getMessageBus()
      .syncPublisher(IconConfigListener.getICON_CONFIG_TOPIC())
      .iconConfigUpdated(config);
    initialIconSettingsModel = iconSettingsModel;
  }
}
