package website;

import javafx.scene.Node;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;

/**
 * Created by gavintam on 25/05/15.
 */
public class ReutersLoader {
    public static Node buildWebsiteDialog(String company) {
        final WebView browser = new WebView();
        final WebEngine webEngine = browser.getEngine();
        webEngine.load("http://www.reuters.com/finance/stocks/overview?symbol=" + company);
        return browser;
    }
}
