package report;

import date.DateUtils;
import image.ImageUtils;
import javafx.collections.ObservableList;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import core.History;
import core.Order;
import core.Portfolio;
import core.Returns;
import net.sf.dynamicreports.jasper.builder.JasperReportBuilder;
import net.sf.dynamicreports.jasper.builder.export.JasperXlsExporterBuilder;
import net.sf.dynamicreports.jasper.builder.export.JasperXlsxExporterBuilder;
import net.sf.dynamicreports.jasper.constant.JasperProperty;
import net.sf.dynamicreports.report.base.DRSubtotal;
import net.sf.dynamicreports.report.builder.DynamicReports;
import net.sf.dynamicreports.report.builder.column.TextColumnBuilder;
import net.sf.dynamicreports.report.builder.component.ComponentBuilder;
import net.sf.dynamicreports.report.builder.component.MultiPageListBuilder;
import net.sf.dynamicreports.report.builder.component.TextFieldBuilder;
import net.sf.dynamicreports.report.builder.style.ConditionalStyleBuilder;
import net.sf.dynamicreports.report.builder.style.StyleBuilder;
import net.sf.dynamicreports.report.constant.HorizontalAlignment;
import net.sf.dynamicreports.report.constant.PageOrientation;
import net.sf.dynamicreports.report.constant.PageType;
import net.sf.dynamicreports.report.datasource.DRDataSource;
import net.sf.dynamicreports.report.exception.DRException;
import net.sf.jasperreports.engine.JRDataSource;
import net.sf.jasperreports.engine.JREmptyDataSource;

import javax.swing.text.Style;
import java.awt.*;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.*;
import java.util.List;

/**
 * Created by Addo Wondo and Banson Tong on 17/5/2015.
 * This class is responsible for generating reports.
 *
 * READ THIS ADDO:
 *
 * NOTE: At the moment the trigger to this code is temporarily inserted
 * into StatsBuilder so it shows the pdf after u select the Portfolio tab.
 *
 */
public class ReportGenerator {

    private Portfolio portfolioData;
    private TableView table;

    private StyleBuilder subtitleStyle;
    private StyleBuilder titleStyle;
    private StyleBuilder tableHeader;
    private StyleBuilder tableBody;
    private StyleBuilder tableBodyBolded;
    private StyleBuilder positiveVal;
    private StyleBuilder negativeVal;
    private StyleBuilder boldCentered;

    private static String pdfReportName = "report.pdf";
    private static String xlsReportName = "report.xls";
//    private static String xlsReportNameOD = "/Users/addo/Documents/OneDrive/dump/report.xlsx";



    public ReportGenerator(Portfolio p){
        this.portfolioData = p;
    }
    public ReportGenerator(TableView t){
        this.table = t;
    }

    // Test Code for XLS Generation
    public void generateXLS(String path)
    {
        if (path == null) path = xlsReportName;
        tableHeader = DynamicReports.stl.style()
                .bold()
                .setHorizontalAlignment(HorizontalAlignment.LEFT)
                .setBorder(DynamicReports.stl.penThin())
                .setBackgroundColor(Color.LIGHT_GRAY);
        tableBody = DynamicReports.stl.style()
                .setHorizontalAlignment(HorizontalAlignment.LEFT)
                .setBorder(DynamicReports.stl.penThin());
        tableBodyBolded = DynamicReports.stl.style(tableBody).bold();
        titleStyle = DynamicReports.stl.style()
                .bold();
        subtitleStyle = DynamicReports.stl.style()
                .bold();

        JasperReportBuilder totalSummary = buildTotalSummaryCmp();
        JasperReportBuilder assetReport = buildAssetCmp();
        JasperReportBuilder returnsReport = buildReturnsCmp();

        try {
            JasperReportBuilder report = DynamicReports.report();
            // Main Report Header
            TextFieldBuilder<String> title = DynamicReports.cmp.text("Report - Overview");
            report.title(DynamicReports.cmp.verticalList().add(
                    DynamicReports.cmp.image(ImageUtils.getURL("logosizes/BuyHard2Logo_Small.png")).setFixedDimension(313,35)

                            .setHorizontalAlignment(HorizontalAlignment.LEFT),
                    title.setStyle(titleStyle)
                            .setHorizontalAlignment(HorizontalAlignment.RIGHT),
                    DynamicReports.cmp.filler().setStyle(
                            DynamicReports.stl.style().setTopBorder(DynamicReports.stl.penThin())
                    ).setFixedHeight(10)
            ));

            JasperXlsExporterBuilder xlsxExporter = DynamicReports.export.xlsExporter(path)
                    .setDetectCellType(true)
                    .setIgnorePageMargins(true)
                    .setWhitePageBackground(false);
//                    .setRemoveEmptySpaceBetweenColumns(true);
            report.addProperty( "net.sf.jasperreports.export.xls.create.custom.palette", "true");
            report
                    .addProperty(JasperProperty.EXPORT_XLS_FREEZE_ROW, "2")
                    .detail(DynamicReports.cmp.horizontalFlowList(
                            DynamicReports.cmp.subreport(totalSummary),
                            DynamicReports.cmp.subreport(assetReport),
                            DynamicReports.cmp.subreport(returnsReport)
                    ))
                    .ignorePageWidth()
                    .ignorePagination()
                    .setDataSource(new JREmptyDataSource())
                    .toXls(xlsxExporter);
        } catch (DRException e) {
            e.printStackTrace();
        }
    }

    public void generatePDF(String path)
    {
        if (path == null) path = pdfReportName;
        initPDFStyle();
        JasperReportBuilder totalSummary = buildTotalSummaryCmp();
        JasperReportBuilder assetReport = buildAssetCmp();
        JasperReportBuilder returnsReport = buildReturnsCmp();
//        MultiPageListBuilder companyReports = buildCompanySubReports();

        JasperReportBuilder report = init("Report - Overview");
        report.detail(DynamicReports.cmp.multiPageList(
                        DynamicReports.cmp.subreport(totalSummary),
                        DynamicReports.cmp.subreport(assetReport),
                        DynamicReports.cmp.subreport(returnsReport))
//                companyReports
//                DynamicReports.cmp.subreport(buildTableViewReport(table))
        );

        report.setDataSource(new JREmptyDataSource());

        // Main Report Footer
        report.pageFooter(createFooter());

        try {
            report.toPdf(new FileOutputStream(new File(path)));
        } catch (DRException | FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    private ComponentBuilder createFooter() {
        return DynamicReports.cmp.verticalList().add(DynamicReports.cmp.filler().setStyle(
                        DynamicReports.stl.style().setTopBorder(DynamicReports.stl.penThin())
                ).setFixedHeight(5),
                DynamicReports.cmp.pageXofY().setStyle(boldCentered));
    }

    private void initPDFStyle() {
        // build PDF Styles
        tableHeader = DynamicReports.stl.style()
                .bold()
                .setHorizontalAlignment(HorizontalAlignment.LEFT)
                .setBorder(DynamicReports.stl.pen1Point())
                .setBackgroundColor(new Color(222, 222, 222))
                .setPadding(3);
        tableBody = DynamicReports.stl.style()
                .setHorizontalAlignment(HorizontalAlignment.LEFT)
                .setBorder(DynamicReports.stl.penThin().setLineColor(Color.WHITE))
                .setPadding(3);
        tableBodyBolded = DynamicReports.stl.style(tableBody).bold();
        titleStyle = DynamicReports.stl.style()
                .setFontSize(20)
                .setBottomPadding(20)
                .bold();
        subtitleStyle = DynamicReports.stl.style()
                .setFontSize(15)
                .setBottomPadding(15)
                .setTopPadding(20)
                .bold();
        boldCentered = DynamicReports.stl.style()
                .bold()
                .setHorizontalAlignment(HorizontalAlignment.CENTER);
        positiveVal = DynamicReports.stl.style()
                .setBackgroundColor(new Color(210,255,210));
        negativeVal = DynamicReports.stl.style()
                .setBackgroundColor(new Color(255,210,210));
    }

    private JasperReportBuilder init(String reportTitle) {
        JasperReportBuilder report = DynamicReports.report();
        report.setPageMargin(DynamicReports.margin(20));
        report.setPageFormat(PageType.A4, PageOrientation.LANDSCAPE);
        report.setPageColumnsPerPage(2);
        report.setPageColumnSpace(15);

        // Main Report Header
        TextFieldBuilder<String> title = DynamicReports.cmp.text(reportTitle);
        report.title(DynamicReports.cmp.horizontalList().add(
                DynamicReports.cmp.image(ImageUtils.getURL("logosizes/BuyHard2Logo_Small.png"))
                        .setFixedHeight(40)
                        .setHorizontalAlignment(HorizontalAlignment.LEFT),
                title.setStyle(titleStyle)
                        .setHorizontalAlignment(HorizontalAlignment.RIGHT)
        ).newRow().add(DynamicReports.cmp.filler().setStyle(
                        DynamicReports.stl.style().setTopBorder(DynamicReports.stl.penThin())
                ).setFixedHeight(10)
        ));
        return report;
    }

    public void generateTableReport(String pdfReportName) {
        if (pdfReportName == null) pdfReportName = this.pdfReportName;
        initPDFStyle();
        JasperReportBuilder tableInfo = buildTableViewReport();

        JasperReportBuilder report = init("Report - Table");
        report.detail(DynamicReports.cmp.subreport(tableInfo));

        report.setDataSource(new JREmptyDataSource());

        // Main Report Footer
        report.pageFooter(createFooter());

        try {
            report.toPdf(new FileOutputStream(new File(pdfReportName)));
        } catch (DRException | FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    private JasperReportBuilder buildTableViewReport()
    {
        if (table == null) return null;

        JasperReportBuilder tableReport = DynamicReports.report();
        ObservableList<TableColumn> cols = table.getColumns();
        List<TextColumnBuilder>  columns = new ArrayList<>();
        for (TableColumn col : cols){
            columns.add(new DynamicReports().col.column(col.getText(),col.getText().toLowerCase(),DynamicReports.type.stringType()));
        }
        tableReport.columns(columns.toArray(new TextColumnBuilder[columns.size()]));
        tableReport.setColumnHeaderStyle(tableHeader);
        tableReport.setColumnStyle(tableBody);
        tableReport.setDataSource(createTableViewDataSource(table));

        return tableReport;

    }

    private JasperReportBuilder buildTotalSummaryCmp()
    {
        double totalPortfolioValue = 0;
        for (String company : portfolioData.getPortfolioValue().keySet()) {
            totalPortfolioValue += portfolioData.getPortfolioValue().get(company);
        }
        double totalEquityValue = totalPortfolioValue-portfolioData.getTotalReturnValue();

        JasperReportBuilder totalSummary = DynamicReports.report();
        TextFieldBuilder<String> subtitle = DynamicReports.cmp.text("Summary");
        totalSummary.title(subtitle.setStyle(subtitleStyle));
        totalSummary.addTitle(DynamicReports.cmp.text("Total return: " + format.FormatUtils.formatPrice(portfolioData.getTotalReturnValue())
                + "\nTotal return %: " + format.FormatUtils.round2dp(portfolioData.getTotalReturnValue() / (portfolioData.getTotalBuyValue() - totalEquityValue) * 100)));

        return totalSummary;
    }

    private JasperReportBuilder buildAssetCmp()
    {
        TextColumnBuilder<String> companyNameColumn = DynamicReports.col.column("Company", "company", DynamicReports.type.stringType());
        TextColumnBuilder<Double> equityValueColumn = DynamicReports.col.column("Equity", "equity", DynamicReports.type.doubleType());

        // Conditional Styling
        ConditionalStyleBuilder positive = DynamicReports.stl.conditionalStyle(DynamicReports.cnd.greater(equityValueColumn,0))
                .setBackgroundColor(new Color(210,255,210));
        ConditionalStyleBuilder negative = DynamicReports.stl.conditionalStyle(DynamicReports.cnd.smaller(equityValueColumn, 0))
                .setBackgroundColor(new Color(255,210,210));

        // Asset value
        TextFieldBuilder<String> subtitle = DynamicReports.cmp.text("Equities");
        JRDataSource assetData = createDataSource();
        JasperReportBuilder assetReport = DynamicReports.report();
        assetReport.title(subtitle.setStyle(subtitleStyle));
        assetReport.columns(companyNameColumn, equityValueColumn);
        assetReport.setColumnHeaderStyle(tableHeader);
        assetReport.setColumnStyle(tableBody);
        assetReport.detailRowHighlighters(positive, negative);
        assetReport.setDataSource(assetData);

        // Sum of Equities.
        assetReport.subtotalsAtSummary(DynamicReports.sbt.sum(equityValueColumn)
                .setStyle(tableBodyBolded));



        return assetReport;
    }

    private JasperReportBuilder buildReturnsCmp()
    {
        TextColumnBuilder<String> companyNameColumn = DynamicReports.col.column("Company", "company", DynamicReports.type.stringType());
        TextColumnBuilder<Double> returnValueColumn = DynamicReports.col.column("Return", "returnValue", DynamicReports.type.doubleType());
        TextColumnBuilder<Double> returnPercentageColumn = DynamicReports.col.column("Return %", "returnPercentage", DynamicReports.type.doubleType()); //TODO THis should use percentType.

        // Conditional Styling
        ConditionalStyleBuilder positive = DynamicReports.stl.conditionalStyle(DynamicReports.cnd.greater(returnValueColumn,0))
                .setBackgroundColor(new Color(210, 255, 210));
        ConditionalStyleBuilder negative = DynamicReports.stl.conditionalStyle(DynamicReports.cnd.smaller(returnValueColumn, 0))
                .setBackgroundColor(new Color(255, 210, 210));

        // Return value
        TextFieldBuilder<String> subtitle = DynamicReports.cmp.text("Returns");
        JRDataSource resultsData = createDataSource();
        JasperReportBuilder returnsReport = DynamicReports.report();
        returnsReport.title(subtitle.setStyle(subtitleStyle));
        returnsReport.columns(companyNameColumn, returnValueColumn, returnPercentageColumn);
        returnsReport.setColumnHeaderStyle(tableHeader);
        returnsReport.setColumnStyle(tableBody);
        returnsReport.detailRowHighlighters(positive,negative);
        returnsReport.setDataSource(resultsData);

        // Sum of Returns.
        returnsReport.subtotalsAtSummary(
                DynamicReports.sbt.sum(returnValueColumn).setStyle(tableBodyBolded),
                DynamicReports.sbt.sum(returnPercentageColumn).setStyle(tableBodyBolded)
        );

        return returnsReport;
    }

    public void generateCompanyReports(String location)
    {
        initPDFStyle();
        TextColumnBuilder<String> companyNameColumn = DynamicReports.col.column("Company", "company", DynamicReports.type.stringType());
        TextColumnBuilder<Double> priceColumn = DynamicReports.col.column("Price", "price", DynamicReports.type.doubleType());
        TextColumnBuilder<Integer> volumeColumn = DynamicReports.col.column("Volume", "volume", DynamicReports.type.integerType());
        TextColumnBuilder<String> signalColumn = DynamicReports.col.column("Signal", "signal", DynamicReports.type.stringType());
        TextColumnBuilder<String> dateColumn = DynamicReports.col.column("Date", "date", DynamicReports.type.stringType());

        TextFieldBuilder<String> subtitle;

        // Create reports folder
        File reportsFolder = new File(location + "/reports");
        reportsFolder.mkdir();

        // Create company subReports
        History<Order> companyHistories = portfolioData.getOrderHistory();
        for (String c : companyHistories.getAllCompanies()) {
            JasperReportBuilder companyReport = init("Report - " + c);
            subtitle = DynamicReports.cmp.text(c);
            companyReport.title(subtitle.setStyle(subtitleStyle));
            companyReport.columns(companyNameColumn, dateColumn, priceColumn, volumeColumn, signalColumn);
            companyReport.setColumnHeaderStyle(tableHeader);
            companyReport.setColumnStyle(tableBody);
            companyReport.setDataSource(createCompanyDataSource(companyHistories.getCompanyHistory(c)));
            // Main Report Footer
            companyReport.pageFooter(createFooter());
            try {
                companyReport.toPdf(new FileOutputStream(new File(location + "/reports/" + c + ".pdf")));

            } catch (DRException | FileNotFoundException e) {
                e.printStackTrace();
            }
        }
    }


    /**
     * Constructs Data Source for Overview report
     * @return JRDataSource datasource
     */
    private JRDataSource createDataSource()
    {
        DRDataSource dataSource = new DRDataSource("company","equity","returnValue","returnPercentage");

        // Reconstruct data set
        Map<String, Returns> returns = portfolioData.getReturns();
        Map<String, Double> assetValue = portfolioData.getAssetValue();
        for (String company : assetValue.keySet()) {
            dataSource.add(company, assetValue.get(company),returns.get(company).getReturns(),
                    format.FormatUtils.round2dp(returns.get(company).getReturns()/returns.get(company).getBought()*100));
        }

        return dataSource;
    }

    /**
     * Constructs Data Source for each company
     * @param companyOrders
     * @return JRDataSource dataSource
     */
    private JRDataSource createCompanyDataSource(List<Order> companyOrders)
    {
        DRDataSource dataSource = new DRDataSource("company", "date", "price", "volume", "signal");
        for (Order o : companyOrders) {
            dataSource.add(o.getCompanyName(), DateUtils.format(o.getOrderDate()),o.getPrice(),o.getVolume(),
                    o.getOrderType().getSignal(o.getOrderType()));
        }
        return dataSource;
    }

    private JRDataSource createTableViewDataSource(TableView table)
    {
        ObservableList<TableColumn> cols = table.getColumns();
        List<String> columnNames = new ArrayList();
        List<String> columnHeaders = new ArrayList<>();
        for (TableColumn col : cols){
            columnNames.add(col.getText().toLowerCase());
            columnHeaders.add(col.getText());
        }
        DRDataSource dataSource = new DRDataSource(columnNames.toArray(new String[columnNames.size()]));
        ObservableList<Map> rows = table.getItems();
        for (Map row : rows){
            List<String> rowItems = new ArrayList();
            for (String column: columnHeaders) {
                rowItems.add(row.get(column).toString());
            }

            dataSource.add(rowItems.toArray(new String[rowItems.size()]));
        }

        return dataSource;
    }
}