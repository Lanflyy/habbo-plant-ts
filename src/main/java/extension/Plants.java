package extension;

import extension.features.PlantManagerFeature;
import extension.ui.PlantsView;
import gearth.extensions.ExtensionForm;
import gearth.extensions.ExtensionInfo;
import javafx.fxml.FXML;
import javafx.scene.control.TextArea;
import lombok.extern.slf4j.Slf4j;

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

    private PlantsView view;
    private PlantManagerFeature plantManagerFeature;

    public void initialize() {
        getView();
        log.debug("[Plants] UI initialized");
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
