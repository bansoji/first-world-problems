package dialog;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.util.List;

/**
 * Created by gavintam on 18/05/15.
 */
public class DialogBuilder {
    public static EventHandler<ActionEvent> constructEventHandler(String title, List<Node> children) {
        return new EventHandler<ActionEvent>() {
            @Override public void handle(ActionEvent event) {
                final Stage dialog = new Stage();
                dialog.setTitle(title);
                dialog.initModality(Modality.APPLICATION_MODAL);
                dialog.initOwner(null);
                VBox dialogVbox = new VBox();
                dialogVbox.setPadding(new Insets(20));
                dialogVbox.setSpacing(20);
                dialogVbox.setAlignment(Pos.CENTER);
                Button close = new Button("Close");
                close.setOnAction(new EventHandler<ActionEvent>() {
                    @Override
                    public void handle(ActionEvent event) {
                        dialog.close();
                    }
                });
                for (Node child: children) {
                    dialogVbox.getChildren().add(child);
                }
                dialogVbox.getChildren().add(close);
                Scene dialogScene = new Scene(dialogVbox);
                dialog.setScene(dialogScene);
                dialog.show();
            }
        };
    }

    public static EventHandler<ActionEvent> constructHelpModal(Node node) {
        return new EventHandler<ActionEvent>() {
            @Override public void handle(ActionEvent event) {
                final Stage dialog = new Stage();
                dialog.setTitle("Tutorial");
                dialog.initModality(Modality.APPLICATION_MODAL);
                dialog.initOwner(null);
                VBox dialogVbox = new VBox();
                dialogVbox.getStyleClass().add("tutorial-modal");
                dialogVbox.setAlignment(Pos.CENTER_RIGHT);
                Button close = new Button("Close");
                close.setOnAction(new EventHandler<ActionEvent>() {
                    @Override
                    public void handle(ActionEvent event) {
                        dialog.close();
                    }
                });
                dialogVbox.getChildren().add(node);
                Scene dialogScene = new Scene(dialogVbox);
                dialog.setScene(dialogScene);
                dialog.show();
            }
        };
    }
}
