import date.DateUtils;
import utils.FinanceUtils;
import quickDate.Order;
import main.OrderType;
import quickDate.Price;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.logging.Logger;

/**
 * Live Free or Buy Hard.
 * Doesn't sound very wise, so placing no orders is much better here.
 * Use in adverserial market conditions. 
 */
public class NullStrategy implements TradingStrategy {

    private static final Logger logger = Logger.getLogger("log");

    public NullStrategy(List<Price> historicalPrices, InputStream config) {
       
        // Initialise the config according to the parameters.
		// For consistency with the other strategies, still take in a config file.
		// Here it does not matter what you pass in. 
        Properties prop = new Properties();
        try {
            prop.load(config);
        } catch (IOException e) {
            logger.severe("Invalid Parameters File.");
            e.printStackTrace();
        }

        configureStrategy(prop);

        String parameters = "Parameters Used:\n" +
                "N/A For this strategy.\n";

        logger.info(parameters);
    }

    /**
     * Configure the strategy given a Properties file.
     * @param prop a Properties object, containing the configuration parameters of
     *             the strategy module.
     */
    private void configureStrategy(Properties prop) {
		return; 
    }

    /**
     * Generate orders using this strategy.
     */
    @Override
    public void generateOrders() {
        return;
    }

    /**
     * Gets all the orders that have been generated using this strategy.
     * @return an ArrayList of Orders.
     */
    @Override
    public List<Order> getOrders() {
        return new ArrayList<Order>();
    }


}
