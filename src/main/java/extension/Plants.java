package extension;

import extension.features.PlantManagerFeature;
import extension.ui.PlantsView;
import gearth.extensions.ExtensionForm;
import gearth.extensions.ExtensionInfo;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Button;
import javafx.scene.control.Tooltip;
import javafx.scene.control.TextArea;
import javafx.animation.PauseTransition;
import javafx.util.Duration;
import javafx.geometry.Point2D;
import javafx.scene.layout.Region;

import lombok.extern.slf4j.Slf4j;
import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import org.slf4j.LoggerFactory;

@Slf4j
@ExtensionInfo(
        Title = "Plants",
        Description = "Automate treating and composting monster plants",
        Version = "1.1.0",
        Author = "Lanflyy"
)
public class Plants extends ExtensionForm {

    @FXML
    private TextArea commandsArea;
    @FXML
    private TextArea logArea;
    @FXML
    private ComboBox<String> logLevelCombo;
    @FXML
    private CheckBox chkRequestPetInfo;
    @FXML
    private Button btnPetInfoHelp;
    @FXML
    private Tooltip ttPetInfoHelp;

    private PlantsView view;
    private PlantManagerFeature plantManagerFeature;

    public void initialize() {
        getView();
        log.debug("[Plants] UI initialized");
        initLogLevelControl();
        initSettingsControl();
        initHelpIcon();
    }

    private void initSettingsControl() {
        try {
            if (chkRequestPetInfo == null) return;
            // initialize from current setting
            chkRequestPetInfo.setSelected(extension.util.PlantSettings.isRequestPetInfoBeforeTreat());
            chkRequestPetInfo.setOnAction(e -> extension.util.PlantSettings.setRequestPetInfoBeforeTreat(chkRequestPetInfo.isSelected()));
        } catch (Exception e) {
            log.error("Failed to initialize settings control", e);
        }
    }

    @FXML
    private void helpPetInfoClick() {
        try {
            if (btnPetInfoHelp == null || ttPetInfoHelp == null) return;
            if (!ttPetInfoHelp.isShowing()) {
                Point2D p = btnPetInfoHelp.localToScreen(0, btnPetInfoHelp.getHeight());
                double offsetX = 8.0; // small horizontal gap from the icon
                double offsetY = 6.0; // small vertical gap from the icon
                ttPetInfoHelp.show(btnPetInfoHelp, p.getX() + offsetX, p.getY() + offsetY);
                PauseTransition pt = new PauseTransition(Duration.seconds(10));
                pt.setOnFinished(e -> ttPetInfoHelp.hide());
                pt.play();
            } else {
                ttPetInfoHelp.hide();
            }
        } catch (Exception ex) {
            log.debug("Failed to show help tooltip", ex);
        }
    }

    private void initHelpIcon() {
        try {
            if (btnPetInfoHelp == null) return;
            // Use simple text icon instead of SVG
            btnPetInfoHelp.setGraphic(null);
            btnPetInfoHelp.setText("(?)");
            // Ensure bold blue styling and transparent background, remove borders and focus rings
            try {
                btnPetInfoHelp.setStyle("-fx-font-weight: bold; -fx-text-fill: #2b7df6; -fx-background-color: transparent; -fx-border-color: transparent; -fx-border-width: 0; -fx-background-insets: 0; -fx-border-insets: 0; -fx-padding: 0; -fx-focus-color: transparent; -fx-faint-focus-color: transparent;");
            } catch (Exception ignored) {}

            // Size the button to its content (text) exactly
            try {
                btnPetInfoHelp.setMinSize(Region.USE_COMPUTED_SIZE, Region.USE_COMPUTED_SIZE);
                btnPetInfoHelp.setPrefSize(Region.USE_COMPUTED_SIZE, Region.USE_COMPUTED_SIZE);
                btnPetInfoHelp.setMaxSize(Region.USE_COMPUTED_SIZE, Region.USE_COMPUTED_SIZE);
            } catch (Exception ignored) {}

            // Prepare a programmatic tooltip and attach it to the button (hover shows it)
            try {
                String tip = (ttPetInfoHelp != null && ttPetInfoHelp.getText() != null)
                        ? ttPetInfoHelp.getText()
                        : "When enabled, the extension will request plant (pet) info before treating to verify wellbeing and avoid accidental treats.";
                ttPetInfoHelp = new Tooltip(tip);
                ttPetInfoHelp.setShowDelay(Duration.millis(200));
                ttPetInfoHelp.setShowDuration(Duration.seconds(10));
            } catch (Exception ignored) {}
            try {
                btnPetInfoHelp.setTooltip(ttPetInfoHelp);
            } catch (Exception ignored) {}
        } catch (Exception e) {
            log.debug("Failed to initialize help button", e);
        }
    }

    private void initLogLevelControl() {
        try {
            if (logLevelCombo == null) return;
            ObservableList<String> items = FXCollections.observableArrayList("ERROR", "WARN", "INFO", "DEBUG", "TRACE", "OFF");
            logLevelCombo.setItems(items);
            // default to INFO
            logLevelCombo.getSelectionModel().select("INFO");

            // Use default cell rendering (no additional highlight)

            logLevelCombo.setOnAction(e -> {
                String lvl = logLevelCombo.getValue();
                if (lvl != null) setLogLevel(lvl);
            });

            // apply initial level
            setLogLevel(logLevelCombo.getValue());
        } catch (Exception e) {
            log.error("Failed to initialize log level control", e);
        }
    }

    private void setLogLevel(String levelName) {
        try {
            Level level = Level.toLevel(levelName, Level.INFO);
            Logger logger = (Logger) LoggerFactory.getLogger("extension");
            logger.setLevel(level);
            log.info("[Plants] Set log level to {}", level);
        } catch (Exception e) {
            log.error("Failed to set log level", e);
        }
    }

    @Override
    protected void initExtension() {
        getView();
        plantManagerFeature = new PlantManagerFeature(this);
        plantManagerFeature.install();
    }

    @FXML
    public void clearLogs() {
        getView().clearLogs();
    }

    @Override
    protected void onStartConnection() {
        log.debug("[Connection] Started");
    }

    @Override
    protected void onEndConnection() {
        if (plantManagerFeature != null) {
            plantManagerFeature.reset();
        }
        log.debug("[Connection] Ended");
    }

    private PlantsView getView() {
        if (view == null) {
            view = new PlantsView(commandsArea, logArea);
            view.initialize();
        }
        return view;
    }
}
