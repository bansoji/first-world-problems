import file.ParameterManager;
import quickDate.Order;
import quickDate.Price;

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

    public NullStrategy(List<Price> historicalPrices, ParameterManager<Number> config, String configFileName) {
       
        // Initialise the config according to the parameters.
		// For consistency with the other strategies, still take in a config file.
		// Here it does not matter what you pass in.
        configureStrategy(config.getProperties(configFileName));

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
