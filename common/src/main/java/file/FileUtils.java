package file;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Created by gavintam on 11/05/15.
 */
public class FileUtils {
    public static final String OUTPUT_FILE_PATH = "orders.csv";

    public static boolean matches(String absolutePath, String filename) {
        String file = extractFilename(absolutePath);
        return filename.equals(file);
    }

    public static String extractFilename(String absolutePath) {
        Path p = Paths.get(absolutePath);
        return p.getFileName().toString();
    }
}
