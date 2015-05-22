package dialog;

import image.ImageUtils;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Orientation;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;

import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by gavintam on 18/05/15.
 */
public class DialogBuilder {
    public static EventHandler<ActionEvent> constructExportableDialog(String title, List<Node> children) {
        return new EventHandler<ActionEvent>() {
            @Override public void handle(ActionEvent event) {
                final Stage dialog = initDialog(title);
                BorderPane dialogBox = new BorderPane();
                dialogBox.getStyleClass().add("dialog-content");
                VBox content = new VBox();

                ToolBar toolBar = new ToolBar();
                toolBar.setOrientation(Orientation.VERTICAL);
                toolBar.getStyleClass().add("export-toolbar");
                MenuButton exportButton = new MenuButton("",ImageUtils.getImage("icons/export-icon.png"));
                exportButton.getStyleClass().add("transparent-button");

                MenuItem screenshot = new MenuItem("Screenshot", ImageUtils.getImage("icons/screenshot.png"));
                toolBar.getItems().add(exportButton);
                exportButton.getItems().add(screenshot);
                screenshot.setOnAction(new EventHandler<ActionEvent>() {
                    @Override
                    public void handle(ActionEvent event) {
                        FileChooser fileChooser = new FileChooser();
                        fileChooser.setTitle("Save Image");
                        File file = fileChooser.showSaveDialog(dialog);
                        if (file != null) {
                            try {
                                ImageIO.write(SwingFXUtils.fromFXImage(dialog.getScene().snapshot(null),
                                        null), "png", file);
                            } catch (IOException ex) {
                                System.out.println(ex.getMessage());
                            }
                        }
                    }
                });
                dialogBox.setRight(toolBar);

                for (Node child: children) {
                    content.getChildren().add(child);
                }
                dialogBox.setBottom(closeButton(dialog));
                dialogBox.setCenter(content);
                Scene dialogScene = new Scene(dialogBox);
                dialogScene.getStylesheets().addAll("general.css", "modal.css");
                dialog.setScene(dialogScene);
                dialog.show();
            }
        };
    }

    public static EventHandler<ActionEvent> constructExportableDialog(String title, Node child) {
        List<Node> content = new ArrayList<>();
        content.add(child);
        return constructExportableDialog(title, content);
    }

    public static EventHandler<ActionEvent> constructHelpModal(Node node) {
        return new EventHandler<ActionEvent>() {
            @Override public void handle(ActionEvent event) {
                final Stage dialog = initDialog("Tutorial");
                VBox dialogVbox = new VBox();
                dialogVbox.getStyleClass().add("tutorial-modal");
                dialogVbox.getChildren().addAll(node, closeButton(dialog));
                Scene dialogScene = new Scene(dialogVbox);
                dialogScene.getStylesheets().addAll("general.css", "modal.css");
                dialog.setScene(dialogScene);
                dialog.show();
            }
        };
    }

    public static EventHandler<ActionEvent> constructSelectionModal(String title, List<Node> children) {
        return new EventHandler<ActionEvent>() {
            @Override public void handle(ActionEvent event) {
                final Stage dialog = initDialog(title);
                VBox dialogVbox = new VBox();
                dialogVbox.getStyleClass().add("dialog-content");

                HBox footer = new HBox();
                footer.getChildren().addAll(applyButton(dialog),closeButton(dialog));
                for (Node child: children) {
                    dialogVbox.getChildren().add(child);
                }
                dialogVbox.getChildren().add(footer);
                Scene dialogScene = new Scene(dialogVbox);
                dialogScene.getStylesheets().addAll("general.css", "modal.css");
                dialog.setScene(dialogScene);
                dialog.show();
            }
        };
    }

    public static EventHandler<ActionEvent> constructSelectionModal(String title, Node child) {
        List<Node> content = new ArrayList<>();
        content.add(child);
        return constructSelectionModal(title,content);
    }

    private static Button closeButton(Stage dialog) {
        Button close = new Button("Close");
        close.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                dialog.close();
            }
        });
        return close;
    }

    private static Button applyButton(Stage dialog) {
        Button apply = new Button("Apply");
        apply.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                dialog.close();
            }
        });
        return apply;
    }

    private static Stage initDialog(String title) {
        final Stage dialog = new Stage();
        dialog.setTitle(title);
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.initOwner(null);
        return dialog;
    }
}
