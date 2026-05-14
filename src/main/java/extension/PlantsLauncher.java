package extension;

import extension.ui.PlantsUi;
import gearth.extensions.ExtensionInfo;
import gearth.extensions.ThemedExtensionFormCreator;
import javafx.stage.Stage;

import java.net.URL;

public class PlantsLauncher extends ThemedExtensionFormCreator {

    static {
        URL logConfig = PlantsLauncher.class.getClassLoader().getResource("log.xml");
        if (logConfig != null) {
            System.setProperty("logback.configurationFile", logConfig.toString());
        }
    }

    @Override
    protected String getTitle() {
        return "Plants - " + Plants.class.getAnnotation(ExtensionInfo.class).Version();
    }

    @Override
    protected URL getFormResource() {
        return PlantsUi.getFormResource();
    }

    @Override
    protected void initialize(Stage primaryStage) {
        PlantsUi.configureStage(primaryStage);
    }

    public static void main(String[] args) {
        runExtensionForm(args, PlantsLauncher.class);
    }
}
