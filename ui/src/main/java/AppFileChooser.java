import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;

/**
 * Created by Gavin Tam on 19/03/15.
 */
public class AppFileChooser extends VBox {

    private Button button;
    private Label label;

    public AppFileChooser (String buttonName)
    {
        this(buttonName,null);
    }

    public AppFileChooser (String buttonName, ImageView icon)
    {
        //make each word a separate line and all letters to uppercase
        button = new Button(buttonName.toUpperCase().replaceAll(" ", "\n"), icon);
        button.setId(buttonName);
        button.getStyleClass().add("icon-button");
        label = new Label("No file selected");
        getChildren().addAll(button, label);
        setAlignment(Pos.CENTER);
    }

    public void setLabelText(String text)
    {
        label.setText(text.length() < 14 ? text : text.substring(0,12) + "...");
    }

    public String getButtonText()
    {
        return button.getText();
    }

    public String getButtonId() {
        return button.getId();
    }

    public void addListener(EventHandler<ActionEvent> listener)
    {
        button.setOnAction(listener);
    }
}
