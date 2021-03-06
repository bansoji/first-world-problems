package image;

import javafx.scene.image.ImageView;

import java.net.URL;

/**
 * Created by gavintam on 21/05/15.
 */
public class ImageUtils {
    public static ImageView getImage(String path) {
        //class loader required as images are not in this package
        return new ImageView(ImageUtils.class.getClassLoader().getResource(path).toExternalForm());
    }

    public static URL getURL(String path) {
        return ImageUtils.class.getClassLoader().getResource(path);
    }

    public static ImageView getImage(URL url) {
        return new ImageView(url.toExternalForm());
    }
}
