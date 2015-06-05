package website;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;

/**
 * Created by gavintam on 25/05/15.
 */
public class ReutersLoader {
    public static WebView buildWebView() {
        final WebView browser = new WebView();
        return browser;
    }

    public static void loadWebsite(WebView webView, String company) {
        webView.getEngine().load("http://www.reuters.com/finance/stocks/overview?symbol=" + company);
    }
}
