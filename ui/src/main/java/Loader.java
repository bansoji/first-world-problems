import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.HBox;

/**
 * Created by gavintam on 11/05/15.
 */
public class Loader extends HBox {
    private ProgressBar loading;
    private Label loadingInfo;

    public Loader() {
        loading = new ProgressBar(0);
        loadingInfo = new Label();
        getChildren().addAll(loading,loadingInfo);
        getStyleClass().add("loader");
        setAlignment(Pos.CENTER_LEFT);
    }

    public void setProgress(double value) {
        loading.setProgress(value);
    }

    public void setText(String text) {
        loadingInfo.setText(text);
    }
}