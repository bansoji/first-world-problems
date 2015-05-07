package alert;

import javafx.scene.control.Alert;

/**
 * Created by gavintam on 6/05/15.
 */
public class AlertManager {
    public static void warning(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        setAndShow(alert,title,content);
    }

    public static void error(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        setAndShow(alert,title,content);
    }

    public static void info(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        setAndShow(alert,title,content);
    }

    private static void setAndShow(Alert alert, String title, String content){
        alert.setTitle(title);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
