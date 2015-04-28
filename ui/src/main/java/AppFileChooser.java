import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

import java.awt.event.ActionListener;

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
