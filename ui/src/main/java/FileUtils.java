import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Created by gavintam on 11/05/15.
 */
public class FileUtils {
    public static boolean matches(String absolutePath, String filename) {
        Path p = Paths.get(absolutePath);
        String file = p.getFileName().toString();
        return filename.equals(file);
    }
}
