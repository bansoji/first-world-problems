import components.ImageViewPane;
import components.LabelledSelector;
import components.SearchBar;
import components.TitleBox;
import core.Order;
import core.Portfolio;
import image.ImageUtils;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.PixelReader;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import stock.Stock;
import stock.YahooFinance;
import table.ExportableTable;
import table.TableUtils;

import java.net.ConnectException;
import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

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
    private TableView ordersTable;

    //default company for news is CBA
    private String currCompany = "CBA.AX";

    public void buildCurrentStats(BorderPane now, Portfolio portfolio) {
        this.now = now;
        if (companyNews == null) init();
        now.setPadding(new Insets(30));

        try {
            buildSummary();
            buildTopNews();
            buildCompanyNews(currCompany);
            buildOrdersInfo(portfolio);
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
        HBox chartHeader = new HBox();
        chartHeader.setId("chart-header");
        SearchBar search = new SearchBar();
        search.setPromptText("e.g. ^AORD");
        LabelledSelector labelledSelector = new LabelledSelector("Symbol:", search);
        labelledSelector.setId("market-data-symbol-selector");
        ToggleGroup periods = new ToggleGroup();
        HBox chartDatePeriods = new HBox();
        chartDatePeriods.setId("date-periods");
        EventHandler<ActionEvent> updateChart = new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                String symbol = search.getText();
                if (symbol.equals("")) symbol = "^AORD";
                try {
                    buildIndexChart(symbol, ((ToggleButton)periods.getSelectedToggle()).getText());
                } catch (ConnectException e) {
                    logger.warning("Could not update index chart");
                }
            }
        };
        for (String period: new String[] {"1d","5d","1m","3m","6m","1y","2y","5y","max"}) {
            ToggleButton datePeriod = new ToggleButton(period);
            datePeriod.getStyleClass().add("toggle");
            datePeriod.setToggleGroup(periods);
            datePeriod.setOnAction(updateChart);
            chartDatePeriods.getChildren().add(datePeriod);
        }
        periods.getToggles().get(0).setSelected(true);
        search.setOnAction(updateChart);
        chartHeader.getChildren().addAll(labelledSelector, chartDatePeriods);
        try {
            buildIndexChart("^AORD", "1d");
        } catch (ConnectException e) {
            logger.warning("Could not update index chart");
        }
        charts.setTop(chartHeader);
        indices.getChildren().addAll(summary, new TitleBox("Market data", charts));
        this.indices = new TitleBox("Indices", indices);
        this.indices.setId("indices-box");
        HBox.setHgrow(indices, Priority.ALWAYS);

        BorderPane company = new BorderPane();
        companyNews = new ScrollPane();
        companyNews.setPrefHeight(300);
        companyNews.setPrefWidth(450);
        companyNews.setFitToWidth(true);
        companyNewsBoxes = new VBox();
        companyNews.setContent(companyNewsBoxes);
        HBox companySearcher = buildCompanySearcher();
        company.setTop(companySearcher);
        company.setCenter(companyNews);
        Tab companyNewsTab = new Tab("Company");
        companyNewsTab.getStyleClass().add("news-tab");
        companyNewsTab.setContent(company);
        companyNewsTab.setClosable(false);

        topNews = new ScrollPane();
        topNews.setPrefHeight(300);
        topNews.setPrefWidth(450);
        topNews.setFitToWidth(true);
        topNewsBoxes = new VBox();
        topNews.setContent(topNewsBoxes);
        Tab topNewsTab = new Tab("Latest");
        topNewsTab.getStyleClass().add("news-tab");
        topNewsTab.setContent(topNews);
        topNewsTab.setClosable(false);

        TabPane newsTabPane = new TabPane();
        newsTabPane.setId("news-tabpane");
        newsTabPane.getTabs().addAll(topNewsTab, companyNewsTab);
        news = new VBox();
        news.getStyleClass().add("news");
        TitleBox newsBox = new TitleBox("News",newsTabPane);
        HBox.setHgrow(news,Priority.ALWAYS);

        initOrdersTable();
        TitleBox ordersBox = new TitleBox("Suggested orders", ordersTable);
        news.getChildren().addAll(newsBox, ordersBox);
    }

    private void initOrdersTable() {
        ordersTable = new ExportableTable();
        ordersTable.setPlaceholder(new Label("No suggested orders"));
        TableColumn company = TableUtils.createMapColumn("Company", TableUtils.ColumnType.String);
        TableColumn price = TableUtils.createMapColumn("Price", TableUtils.ColumnType.Double);
        TableColumn type = TableUtils.createMapColumn("Type", TableUtils.ColumnType.String);
        type.setCellFactory(column -> {
           return new TableCell<Map,String>() {
                @Override
                protected void updateItem(String type, boolean empty) {
                    super.updateItem(type, empty);
                    setText(type);
                    if (type != null && !empty) {
                        if (type.equals("BUY")) {
                            setStyle("-fx-background-color: green");
                            //getStyleClass().add("buy-row");
                        } else if (type.equals("SELL")) {
                            setStyle("-fx-background-color: red");
                            //getStyleClass().add("sell-row");
                        }
                        setTextFill(Color.WHITE);
                    } else {
                        setStyle("");
                        //getStyleClass().add("normal-row");
                    }
                }
            };
        });
        ordersTable.getColumns().addAll(company, price, type);
        //ensures extra space to given to existing columns
        ordersTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
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
        companySearchBox.setId("company-news-selector");
        SearchBar search = new SearchBar();
        search.setPromptText("e.g. CBA.AX");
        HBox companyPrice = new HBox();
        companyPrice.setId("company-price");
        Stock stock = YahooFinance.get(currCompany);
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
        companySearchBox.getChildren().addAll(search,companyPrice);
        search.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                String company = search.getText();
                try {
                    currCompany = company;
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

    public void buildIndexChart(String index, String period) throws ConnectException {
        charts.getChildren().remove(chartImage);
        if (period.equals("max")) period = "my";
        Image image = new Image("https://chart.finance.yahoo.com/z?s=" + index + "&t=" + period + "&q=l&l=on&z=l&a=v&p=s&lang=en-AU&region=AU");
        if (!image.isError()) {
            //no volume chart for 1d and 5d
            if (period.equals("1d") || period.equals("5d")) {
                PixelReader reader = image.getPixelReader();
                //crop white bit at the bottom of the chart
                WritableImage newImage = new WritableImage(reader, 0, 0, (int) image.getWidth(), 350 + (period.equals("1d")?10:0));
                chartImage = new ImageView(newImage);
            } else {
                chartImage = new ImageView(image);
            }
            charts.setCenter(new ImageViewPane(chartImage));
        } else {
            throw new ConnectException();
        }
    }

    private void buildOrdersInfo(Portfolio portfolio) {
        ordersTable.getItems().clear();
        for (Order order: portfolio.getEndDateOrders()) {
            Map<String,Object> orders = new HashMap<>();
            orders.put("Company", order.getCompanyName());
            orders.put("Price", order.getPrice());
            orders.put("Type", order.getOrderType().toString());
            ordersTable.getItems().add(orders);
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
