import java.io.File;
import java.io.IOException;
import java.util.logging.Logger;

/**
 * Created by Gavin Tam on 5/05/15.
 */
public class StrategyRunner {

    private static final Logger logger = Logger.getLogger("log");
    private Process p;

    private boolean shouldRun = true;

    //TODO Remove values - only for TESTING
    private String strategyFile = "out/artifacts/trading_jar/trading.jar";
    private String dataFile = "common/src/main/resources/sampleDataSmall";
    private String paramFile = "trading/resources/config.properties";

    public void setStrategyFile(String strategyFile) {
        this.strategyFile = strategyFile;
    }

    public void setDataFile(String dataFile) {
        this.dataFile = dataFile;
    }

    public void setParamFile(String paramFile) {
        this.paramFile = paramFile;
    }

    public String getStrategyFile() {
        return strategyFile;
    }

    public String getDataFile() {
        return dataFile;
    }

    public String getParamFile() {
        return paramFile;
    }

    public boolean validFiles() {
        return (strategyFile != null && dataFile != null && paramFile != null);
    }

    public void run(boolean waitFor) {
        if (strategyFile != null && dataFile != null && paramFile != null) {
            try {
                ProcessBuilder pb = new ProcessBuilder("java", "-jar", strategyFile, dataFile, paramFile);
                pb.redirectErrorStream(true);
                pb.redirectOutput(new File("/dev/null"));
                p = pb.start();
                if (waitFor) {
                    try {
                        p.waitFor();
                    } catch (InterruptedException ex) {
                        logger.severe("Failed to run module without errors.");
                    }
                }
            } catch (IOException e) {
                logger.severe("Failed to run module without errors.");
            }
        }
    }

    public void stop() {
        p.destroyForcibly();
        logger.info("Process was terminated voluntarily.");
        shouldRun = false;
    }

    public boolean shouldRun() {
        boolean b = shouldRun;
        shouldRun = true;
        return b;
    }
}
