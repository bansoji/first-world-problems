package components;

import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

/**
 * Created by gavintam on 30/05/15.
 */
public class TitleBox extends VBox {
    public TitleBox(String title, Node node) {
        Label titleLabel = new Label(title);
        titleLabel.getStyleClass().add("title-box-label");
        HBox box = new HBox();
        box.getStyleClass().add("title-box");
        box.getChildren().add(titleLabel);
        getChildren().addAll(box, node);
    }
}
