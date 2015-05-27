package components;

import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;

/**
 * Created by gavintam on 20/05/15.
 */
public class LabelledSelector extends HBox {

    Node selector;

    public LabelledSelector(String name, Node selector) {
        Label label = new Label(name);
        getStyleClass().add("selector");
        getChildren().addAll(label,selector);
    }

    public Node getSelector() {
        return selector;
    }
}
