import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

/**
 * Created by Gavin Tam on 19/03/15.
 */
public class AppFileChooser extends VBox {

    private Button button;
    private Label label;

    public AppFileChooser (String buttonName)
    {
        button = new Button(buttonName);
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

    public void addListener(EventHandler<ActionEvent> listener)
    {
        button.setOnAction(listener);
    }
}
