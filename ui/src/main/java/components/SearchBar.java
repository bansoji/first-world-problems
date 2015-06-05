package components;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;

/**
 * Created by gavintam on 31/05/15.
 */
public class SearchBar extends HBox {
    private TextField searchContent;
    private Button searchButton;

    public SearchBar() {
        searchContent = new TextField();
        searchButton = new Button("Search");
        getChildren().addAll(searchContent, searchButton);
    }

    public void setPromptText(String string) {
        searchContent.setPromptText(string);
    }

    public String getText() {
        return searchContent.getText();
    }

    public void setOnAction(EventHandler<ActionEvent> event) {
        searchButton.setOnAction(event);
    }
}
