import file.FileUtils;
import core.Order;
import core.OrderReader;
import core.Reader;

/**
 * Created by gavintam on 22/05/15.
 */
public class IntegrationUtils {
    public static Reader<Order> selectReader(String jarFilename) {
        //integration with other JARs
        if (FileUtils.matches(jarFilename, "aurora.jar")) {
            return new OrderReaderKoK(FileUtils.OUTPUT_FILE_PATH);
        } else {
            return new OrderReader(FileUtils.OUTPUT_FILE_PATH);
        }
    }
}