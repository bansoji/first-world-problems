package image;

import javafx.scene.image.ImageView;

/**
 * Created by gavintam on 21/05/15.
 */
public class ImageUtils {
    public static ImageView getImage(String path) {
        //class loader required as images are not in this package
        return new ImageView(ImageUtils.class.getClassLoader().getResource(path).toExternalForm());
    }
}
