import components.ImageViewPane;
import components.LabelledSelector;
import image.ImageUtils;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.PixelReader;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.*;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import stock.Stock;
import stock.YahooFinance;

import java.net.URI;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by gavintam on 28/05/15.
 */
public class NowBuilder {

    private static final Logger logger = Logger.getLogger("application_log");

    private ScrollPane companyNews;
    private ScrollPane topNews;
    private ImageView chartImage;
    private VBox companyNewsBoxes;
    private VBox topNewsBoxes;
    private BorderPane charts;
    private VBox indices;
    private GridPane summary;
    private VBox news;

    public void buildCurrentStats(BorderPane now) {
        if (companyNews == null) init(now);
        now.setPadding(new Insets(30));

        buildSummary();
        buildTopNews();
        buildCompanyNews("^AORD");
        now.setCenter(indices);
        now.setRight(news);
    }

    public void init(BorderPane now) {
        indices = new VBox();
        indices.setId("indices");
        Label indicesTitle = new Label("Indices");
        indicesTitle.getStyleClass().add("md-label");
        summary = new GridPane();
        summary.setId("index-summary");
        charts = new BorderPane();
        List<String> indicesList = Arrays.asList("All Ords (^AORD)", "S&P/ASX 200 (^AXJO)" , "S&P/ASX 100 (^ATOI)");
        ComboBox<String> indicesChooser = new ComboBox<>(FXCollections.observableArrayList(indicesList));
        LabelledSelector labelledSelector = new LabelledSelector("Index:", indicesChooser);
        labelledSelector.getStyleClass().add("now-selector");
        indicesChooser.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldIndex, String newIndex) {
                Matcher m = Pattern.compile("\\(([^)]+)\\)").matcher(newIndex);
                if (m.find()) {
                    buildIndexChart(m.group(1));
                }
            }
        });
        indicesChooser.getSelectionModel().selectFirst();
        charts.setTop(labelledSelector);
        indices.getChildren().addAll(indicesTitle, summary, charts);
        HBox.setHgrow(indices, Priority.ALWAYS);

        news = new VBox();
        news.getStyleClass().add("news");
        BorderPane company = new BorderPane();
        companyNews = new ScrollPane();
        companyNews.setPrefHeight(300);
        companyNews.setPrefWidth(450);
        companyNews.setFitToWidth(true);
        companyNewsBoxes = new VBox();
        companyNews.setContent(companyNewsBoxes);
        Pane companySearcher = buildCompanySearcher();
        companySearcher.getStyleClass().add("now-selector");
        company.setTop(companySearcher);
        company.setCenter(companyNews);

        BorderPane top = new BorderPane();
        topNews = new ScrollPane();
        topNews.setPrefHeight(300);
        topNews.setPrefWidth(450);
        topNews.setFitToWidth(true);
        topNewsBoxes = new VBox();
        topNews.setContent(topNewsBoxes);
        Label topNewsLabel = new Label("Latest news");
        topNewsLabel.getStyleClass().addAll("md-label", "heading");
        top.setTop(topNewsLabel);
        top.setCenter(topNews);

        news.getChildren().addAll(top, company);
        HBox.setHgrow(news,Priority.ALWAYS);
    }

    public void buildSummary() {
        summary.getChildren().clear();
        List<Stock> stocks = YahooFinance.get(new String[]{"^AORD","^AXJO","^ATOI"});
        int col = 0;
        for (Stock stock: stocks) {
            Label symbol = new Label(stock.getName());
            symbol.getStyleClass().add("sm-label");
            BorderPane indexPrice = new BorderPane();
            indexPrice.getStyleClass().add("index-price");
            Label price = new Label(stock.getPrice());
            price.getStyleClass().add("lg-label");
            Label percent = new Label("(" + stock.getPercent() + "%)");
            percent.getStyleClass().addAll("xsm-label", "percent-label");
            indexPrice.setCenter(price);
            indexPrice.setRight(percent);
            if (Double.parseDouble(stock.getPercent()) > 0) {
                price.setGraphic(ImageUtils.getImage("icons/up.png"));
                percent.getStyleClass().add("positive-percent");
            } else {
                price.setGraphic(ImageUtils.getImage("icons/down.png"));
                percent.getStyleClass().add("negative-percent");
            }
            GridPane.setConstraints(symbol, col, 0);
            GridPane.setConstraints(indexPrice, col, 1);
            summary.getChildren().addAll(symbol, indexPrice);
            col++;
        }
    }

    public Pane buildCompanySearcher() {
        BorderPane pane = new BorderPane();
        Label boxTitle = new Label("Company news");
        boxTitle.getStyleClass().addAll("md-label", "heading");
        pane.setTop(boxTitle);
        HBox search = new HBox();
        TextField companySearch = new TextField();
        Button searchButton = new Button("Search");
        search.getChildren().addAll(companySearch, searchButton);
        pane.setCenter(search);
        searchButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                String company = companySearch.getText();
                buildCompanyNews(company);
            }
        });
        return pane;
    }

    public void buildCompanyNews(String company) {
        companyNewsBoxes.getChildren().clear();
        final String url = "http://finance.yahoo.com/rss/headline?s=" + company;

        try {
            Document doc = Jsoup.connect(url).get();

            for (Element item : doc.select("item")) {
                final String title = item.select("title").first().text();
                final String description = item.select("description").first().text();
                final String pubDate = item.select("pubdate").first().text();
                final String link = item.select("link").first().nextSibling().toString();
                companyNewsBoxes.getChildren().addAll(buildNewsBox(title, pubDate, description, link), new Separator());
            }
        } catch (Exception e) {
            logger.severe("Failed to load company news: " + e);
        }
    }

    public void buildTopNews() {
        final String url = "http://finance.yahoo.com/rss/topfinstories";
        try {
            Document doc = Jsoup.connect(url).get();

            for (Element item : doc.select("item")) {
                final String title = item.select("title").first().text();
                final String description = item.select("description").first().text();
                final String pubDate = item.select("pubdate").first().text();
                final String link = item.select("link").first().nextSibling().toString();
                topNewsBoxes.getChildren().addAll(buildNewsBox(title, pubDate, description, link), new Separator());
            }
        } catch (Exception e) {
            logger.severe("Failed to load latest news: " + e);
        }
    }

    public VBox buildNewsBox(String title, String pubDate, String description, String link) {
        VBox newsBox = new VBox();
        newsBox.getStyleClass().add("news-box");
        Hyperlink titleLabel = new Hyperlink(title);
        titleLabel.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent e) {
                try {
                    URI u = new URI(link);
                    java.awt.Desktop.getDesktop().browse(u);
                } catch (Exception ex) {
                    logger.warning("News link " + link + " could not be opened");
                }
            }
        });
        titleLabel.getStyleClass().add("sm-label");
        Label dateLabel = new Label(pubDate);
        dateLabel.getStyleClass().addAll("xsm-label", "news-date");
        Label descriptionLabel = new Label(description);
        descriptionLabel.getStyleClass().add("xsm-label");
        newsBox.getChildren().addAll(titleLabel, dateLabel, descriptionLabel);
        return newsBox;
    }

    public void buildIndexChart(String index) {
        charts.getChildren().remove(chartImage);
        Image image = new Image("https://chart.finance.yahoo.com/z?s=" + index + "&t=5d&q=l&l=on&z=l&a=v&p=s&lang=en-AU&region=AU");
        PixelReader reader = image.getPixelReader();
        //crop white bit at the bottom of the chart
        WritableImage newImage = new WritableImage(reader, 0, 0, (int)image.getWidth(), 350);
        chartImage = new ImageView(newImage);
        charts.setCenter(new ImageViewPane(chartImage));
    }
}
