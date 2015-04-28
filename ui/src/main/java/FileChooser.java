import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;

/**
 * Created by calvin on 19/03/15.
 */
public class FileChooser extends AppPanel {

    private JButton button;
    private JLabel label;

    public FileChooser (String buttonName)
    {
        button = new JButton(buttonName);
        button.setAlignmentX(Component.CENTER_ALIGNMENT);
        label = new JLabel("No file selected");
        label.setAlignmentX(Component.CENTER_ALIGNMENT);
        setLayout(new BoxLayout(this,BoxLayout.Y_AXIS));
        setOpaque(false);
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
