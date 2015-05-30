import components.ImageViewPane;
import components.LabelledSelector;
import components.TitleBox;
import image.ImageUtils;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
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

import java.net.ConnectException;
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

    private BorderPane now;
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
        this.now = now;
        if (companyNews == null) init();
        now.setPadding(new Insets(30));

        try {
            buildSummary();
            buildTopNews();
            buildCompanyNews("^AORD");
            now.setCenter(indices);
            now.setRight(news);
        } catch (ConnectException e) {
            showDefaultImage();
        }
    }

    public void init() {
        VBox indices = new VBox();
        indices.setId("indices");
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
                    try {
                        buildIndexChart(m.group(1));
                    } catch (ConnectException e) {
                        logger.warning("Could not update index chart");
                    }
                }
            }
        });
        indicesChooser.getSelectionModel().selectFirst();
        charts.setTop(labelledSelector);
        indices.getChildren().addAll(summary, charts);
        this.indices = new TitleBox("Indices", indices);
        this.indices.setId("indices-box");
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
        HBox companySearcher = buildCompanySearcher();
        companySearcher.getStyleClass().add("now-selector");
        company.setTop(companySearcher);
        company.setCenter(companyNews);
        TitleBox companyNewsBox = new TitleBox("Company news", company);

        topNews = new ScrollPane();
        topNews.setPrefHeight(300);
        topNews.setPrefWidth(450);
        topNews.setFitToWidth(true);
        topNewsBoxes = new VBox();
        topNews.setContent(topNewsBoxes);
        TitleBox topNewsBox = new TitleBox("Latest news", topNews);

        news.getChildren().addAll(topNewsBox, companyNewsBox);
        HBox.setHgrow(news,Priority.ALWAYS);
    }

    public void buildSummary() throws ConnectException{
        summary.getChildren().clear();
        List<Stock> stocks = YahooFinance.get(new String[]{"^AORD","^AXJO","^ATOI"});
        if (stocks == null) {
            now.setVisible(false);
            throw new ConnectException();
        }
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

    public HBox buildCompanySearcher() {
        HBox companySearchBox = new HBox();
        companySearchBox.setId("company-search-box");
        HBox search = new HBox();
        TextField companySearch = new TextField();
        Button searchButton = new Button("Search");
        HBox companyPrice = new HBox();
        companyPrice.setId("company-price");
        Stock stock = YahooFinance.get("^AORD");
        Label price;
        if (stock != null) {
            price = new Label(stock.getPrice() + " (" + stock.getPercent() + "%)");
            price.getStyleClass().add("xsm-label");
            if (Double.parseDouble(stock.getPercent()) > 0) {
                price.setGraphic(ImageUtils.getImage("icons/up.png"));
            } else {
                price.setGraphic(ImageUtils.getImage("icons/down.png"));
            }
        } else {
            price = new Label();
            price.getStyleClass().add("xsm-label");
        }
        companyPrice.getChildren().add(price);
        search.getChildren().addAll(companySearch, searchButton);
        companySearchBox.getChildren().addAll(search,companyPrice);
        searchButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                String company = companySearch.getText();
                try {
                    buildCompanyNews(company);
                    Stock stock = YahooFinance.get(company);
                    price.setText(stock.getPrice() + " (" + stock.getPercent() + "%)");
                    if (Double.parseDouble(stock.getPercent()) > 0) {
                        price.setGraphic(ImageUtils.getImage("icons/up.png"));
                    } else {
                        price.setGraphic(ImageUtils.getImage("icons/down.png"));
                    }
                } catch (ConnectException e) {
                    logger.warning("Could not update company news");
                }
            }
        });
        return companySearchBox;
    }

    public void buildCompanyNews(String company) throws ConnectException {
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
            throw new ConnectException();
        }
    }

    public void buildTopNews() throws ConnectException {
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
            throw new ConnectException();
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

    public void buildIndexChart(String index) throws ConnectException {
        charts.getChildren().remove(chartImage);
        Image image = new Image("https://chart.finance.yahoo.com/z?s=" + index + "&t=5d&q=l&l=on&z=l&a=v&p=s&lang=en-AU&region=AU");
        if (!image.isError()) {
            PixelReader reader = image.getPixelReader();
            //crop white bit at the bottom of the chart
            WritableImage newImage = new WritableImage(reader, 0, 0, (int)image.getWidth(), 350);
            chartImage = new ImageView(newImage);
            charts.setCenter(new ImageViewPane(chartImage));
        } else {
            throw new ConnectException();
        }
    }

    private void showDefaultImage() {
        ImageView imageView = ImageUtils.getImage("buyhard-logov2-indent.png");
        imageView.getStyleClass().add("default");
        Label label = new Label("Please check your internet connection.");
        label.getStyleClass().add("default");
        label.setId("default-label");
        BorderPane pane = new BorderPane();
        pane.setCenter(imageView);
        pane.setBottom(label);
        pane.setId("default-pane");
        pane.setAlignment(label, Pos.CENTER);
        now.setCenter(pane);
    }
}
