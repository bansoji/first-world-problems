package file;

import alert.AlertManager;
import javafx.application.Platform;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.logging.Logger;

/**
 * Created by Gavin Tam on 5/05/15.
 */
public class StrategyRunner {

    private static final Logger logger = Logger.getLogger("application_log");
    private Process p;

    private boolean shouldRun = true;

    //TODO Remove values - only for TESTING
    private String strategyFile = "out/artifacts/momentum_jar/momentum.jar";
    private String dataFile;// = "common/src/main/resources/sampleDataSmall";
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
                //pb.redirectErrorStream(true);
                pb.redirectOutput(new File("/dev/null"));
                p = pb.start();
                new Thread() {
                    public void run() {
                        BufferedReader reader = new BufferedReader(new InputStreamReader(p.getErrorStream()));
                        try {
                            boolean error = false;
                            String s;
                            while ((s = reader.readLine()) != null) {
                                System.out.println(s);
                                error = true;
                            }
                            if (error) {
                                Platform.runLater(new Runnable() {
                                    @Override
                                    public void run() {
                                        AlertManager.error("Could not run strategy",
                                                "BuyHard could not run the given strategy.\nPlease check that a compatible module has been used" +
                                                        " and that an appropriate parameters file and a valid TRTH file have been selected. " +
                                                        "Please contact us if errors persist.");
                                        logger.severe("Incompatible module used.");
                                    }
                                });
                            }
                        } catch (IOException e) {
                            Platform.runLater(new Runnable() {
                                @Override
                                public void run() {
                                    AlertManager.error("Unexpected error",
                                            "An IO error was encountered when running the module." +
                                                    " Please contact us if errors persist.");
                                }
                            });
                            logger.severe("Failed to run module without errors.");
                        }
                    }
                }.start();
                if (waitFor) {
                    try {
                        p.waitFor();
                    } catch (InterruptedException ex) {
                        logger.severe("Failed to run module without errors.");
                    }
                }
            } catch (IOException e) {
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        AlertManager.error("Could not run strategy",
                                "BuyHard could not run the given strategy.\nPlease check that a compatible module has been used" +
                                        " and that an appropriate parameters file and a valid TRTH file have been selected. " +
                                        "Please contact us if errors persist.");
                    }
                });
                logger.severe("IO error: failed to run module without errors.");
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
