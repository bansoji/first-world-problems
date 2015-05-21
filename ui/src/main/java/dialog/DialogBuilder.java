package dialog;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by gavintam on 18/05/15.
 */
public class DialogBuilder {
    public static EventHandler<ActionEvent> constructEventHandler(String title, List<Node> children) {
        return new EventHandler<ActionEvent>() {
            @Override public void handle(ActionEvent event) {
                final Stage dialog = initDialog(title);
                VBox dialogVbox = new VBox();
                dialogVbox.getStyleClass().add("dialog-content");

                for (Node child: children) {
                    dialogVbox.getChildren().add(child);
                }
                dialogVbox.getChildren().add(closeButton(dialog));
                Scene dialogScene = new Scene(dialogVbox);
                dialogScene.getStylesheets().addAll("general.css", "modal.css");
                dialog.setScene(dialogScene);
                dialog.show();
            }
        };
    }

    public static EventHandler<ActionEvent> constructEventHandler(String title, Node child) {
        List<Node> content = new ArrayList<>();
        content.add(child);
        return constructEventHandler(title,content);
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
