package extension.ui;

import javafx.application.Platform;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.net.URL;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class PlantsUi {

    public static final double MIN_WIDTH = 300.0;
    public static final double MIN_CONTENT_HEIGHT = 430.0;
    public static final double MIN_HEIGHT = 455.0;
    private static final double RESIZE_BORDER = 7.0;

    public static URL getFormResource() {
        return PlantsUi.class.getResource("plants.fxml");
    }

    public static void configureStage(Stage primaryStage) {
        primaryStage.setResizable(true);
        primaryStage.setMinWidth(MIN_WIDTH);
        primaryStage.setMinHeight(MIN_CONTENT_HEIGHT);
        Platform.runLater(() -> {
            primaryStage.setResizable(true);
            primaryStage.setMinWidth(MIN_WIDTH);
            primaryStage.setMinHeight(MIN_HEIGHT);
            configureResizableContent(primaryStage);
            installResizeHandling(primaryStage);
        });
    }

    private static void configureResizableContent(Stage stage) {
        Scene scene = stage.getScene();
        if (scene == null) {
            return;
        }

        Parent sceneRoot = scene.getRoot();
        if (sceneRoot instanceof Region) {
            ((Region) sceneRoot).setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        }
        if (sceneRoot instanceof VBox) {
            VBox titleBarContainer = (VBox) sceneRoot;
            titleBarContainer.setFillWidth(true);
            for (Node child : titleBarContainer.getChildren()) {
                if (child instanceof Region) {
                    ((Region) child).setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
                }
            }
            if (titleBarContainer.getChildren().size() > 1) {
                VBox.setVgrow(titleBarContainer.getChildren().get(1), Priority.ALWAYS);
            }
        }
    }

    private static void installResizeHandling(Stage stage) {
        Scene scene = stage.getScene();
        if (scene == null || scene.getRoot() == null) {
            return;
        }

        ResizeState state = new ResizeState();

        scene.addEventFilter(MouseEvent.MOUSE_MOVED, event -> {
            if (!state.resizing) {
                scene.setCursor(getResizeCursor(stage, event));
            }
        });
        scene.addEventFilter(MouseEvent.MOUSE_EXITED, event -> {
            if (state.resizing && !event.isPrimaryButtonDown()) {
                state.reset();
                scene.setCursor(Cursor.DEFAULT);
            } else if (!state.resizing) {
                scene.setCursor(Cursor.DEFAULT);
            }
        });
        scene.addEventFilter(MouseEvent.MOUSE_PRESSED, event -> {
            Cursor cursor = getResizeCursor(stage, event);
            if (cursor == Cursor.DEFAULT) {
                return;
            }
            state.resizing = true;
            state.cursor = cursor;
            state.startX = event.getScreenX();
            state.startY = event.getScreenY();
            state.stageX = stage.getX();
            state.stageY = stage.getY();
            state.stageWidth = stage.getWidth();
            state.stageHeight = stage.getHeight();
            event.consume();
        });
        scene.addEventFilter(MouseEvent.MOUSE_DRAGGED, event -> {
            if (!state.resizing) {
                return;
            }
            resizeStage(stage, state, event);
            event.consume();
        });
        scene.addEventFilter(MouseEvent.MOUSE_RELEASED, event -> {
            if (state.resizing) {
                state.reset();
                scene.setCursor(getResizeCursor(stage, event));
                event.consume();
            }
        });
    }

    private static Cursor getResizeCursor(Stage stage, MouseEvent event) {
        double x = event.getScreenX() - stage.getX();
        double y = event.getScreenY() - stage.getY();
        boolean left = x <= RESIZE_BORDER;
        boolean right = x >= stage.getWidth() - RESIZE_BORDER;
        boolean top = y <= RESIZE_BORDER;
        boolean bottom = y >= stage.getHeight() - RESIZE_BORDER;

        if (left && top) {
            return Cursor.NW_RESIZE;
        }
        if (right && top) {
            return Cursor.NE_RESIZE;
        }
        if (left && bottom) {
            return Cursor.SW_RESIZE;
        }
        if (right && bottom) {
            return Cursor.SE_RESIZE;
        }
        if (left) {
            return Cursor.W_RESIZE;
        }
        if (right) {
            return Cursor.E_RESIZE;
        }
        if (top) {
            return Cursor.N_RESIZE;
        }
        if (bottom) {
            return Cursor.S_RESIZE;
        }
        return Cursor.DEFAULT;
    }

    private static void resizeStage(Stage stage, ResizeState state, MouseEvent event) {
        double deltaX = event.getScreenX() - state.startX;
        double deltaY = event.getScreenY() - state.startY;
        boolean left = state.cursor == Cursor.W_RESIZE || state.cursor == Cursor.NW_RESIZE || state.cursor == Cursor.SW_RESIZE;
        boolean right = state.cursor == Cursor.E_RESIZE || state.cursor == Cursor.NE_RESIZE || state.cursor == Cursor.SE_RESIZE;
        boolean top = state.cursor == Cursor.N_RESIZE || state.cursor == Cursor.NW_RESIZE || state.cursor == Cursor.NE_RESIZE;
        boolean bottom = state.cursor == Cursor.S_RESIZE || state.cursor == Cursor.SW_RESIZE || state.cursor == Cursor.SE_RESIZE;

        if (right) {
            stage.setWidth(Math.max(stage.getMinWidth(), state.stageWidth + deltaX));
        }
        if (bottom) {
            stage.setHeight(Math.max(stage.getMinHeight(), state.stageHeight + deltaY));
        }
        if (left) {
            double width = Math.max(stage.getMinWidth(), state.stageWidth - deltaX);
            stage.setX(state.stageX + state.stageWidth - width);
            stage.setWidth(width);
        }
        if (top) {
            double height = Math.max(stage.getMinHeight(), state.stageHeight - deltaY);
            stage.setY(state.stageY + state.stageHeight - height);
            stage.setHeight(height);
        }
    }

    private static final class ResizeState {
        private boolean resizing;
        private Cursor cursor = Cursor.DEFAULT;
        private double startX;
        private double startY;
        private double stageX;
        private double stageY;
        private double stageWidth;
        private double stageHeight;

        private void reset() {
            resizing = false;
            cursor = Cursor.DEFAULT;
        }
    }
}
