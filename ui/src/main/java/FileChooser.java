import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;

/**
 * Created by calvin on 19/03/15.
 */
public class FileChooser extends JPanel {

    private JButton button;
    private JLabel label;

    public FileChooser (String buttonName)
    {
        setBackground(Color.WHITE);
        button = new JButton(buttonName);
        label = new JLabel("No file selected");
        add(button);
        add(label);
    }

    public void setLabelText(String text)
    {
        label.setText(text.length() < 14 ? text : text.substring(0,12) + "...");
    }

    public String getButtonText()
    {
        return button.getText();
    }

    public void addActionListener(ActionListener listener)
    {
        button.addActionListener(listener);
    }
}
