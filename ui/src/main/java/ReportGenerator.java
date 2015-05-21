import date.DateUtils;
import core.History;
import core.Order;
import core.Portfolio;
import core.Returns;
import net.sf.dynamicreports.jasper.builder.JasperReportBuilder;
import net.sf.dynamicreports.jasper.builder.export.JasperXlsxExporterBuilder;
import net.sf.dynamicreports.jasper.constant.JasperProperty;
import net.sf.dynamicreports.report.builder.DynamicReports;
import net.sf.dynamicreports.report.builder.column.TextColumnBuilder;
import net.sf.dynamicreports.report.builder.component.MultiPageListBuilder;
import net.sf.dynamicreports.report.builder.component.TextFieldBuilder;
import net.sf.dynamicreports.report.builder.style.StyleBuilder;
import net.sf.dynamicreports.report.constant.HorizontalAlignment;
import net.sf.dynamicreports.report.constant.PageOrientation;
import net.sf.dynamicreports.report.constant.PageType;
import net.sf.dynamicreports.report.datasource.DRDataSource;
import net.sf.dynamicreports.report.exception.DRException;
import net.sf.jasperreports.engine.JRDataSource;
import net.sf.jasperreports.engine.JREmptyDataSource;

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
    private StyleBuilder subtitleStyle;
    private StyleBuilder titleStyle;
    private StyleBuilder tableHeader;
    private StyleBuilder tableBody;
    private StyleBuilder boldCentered;

    private static String pdfReportName = "report.pdf";
    private static String xlsReportName = "report.xlsx";
//    private static String xlsReportNameOD = "/Users/addo/Documents/OneDrive/dump/report.xlsx";

    public ReportGenerator(Portfolio p){
        this.portfolioData = p;
    }

    // Test Code for XLS Generation
    public void generateXLS(String name)
    {
        if (name == null) name = xlsReportName;
        tableHeader = DynamicReports.stl.style()
                .bold()
                .setHorizontalAlignment(HorizontalAlignment.LEFT)
                .setBorder(DynamicReports.stl.penThin())
                .setBackgroundColor(Color.LIGHT_GRAY);
        tableBody = DynamicReports.stl.style()
                .setHorizontalAlignment(HorizontalAlignment.LEFT)
                .setBorder(DynamicReports.stl.penThin());
        titleStyle = DynamicReports.stl.style()
                .bold();
        subtitleStyle = DynamicReports.stl.style()
                .bold();

        JasperReportBuilder totalSummary = buildTotalSummaryCmp();
        JasperReportBuilder assetReport = buildAssetCmp();
        JasperReportBuilder returnsReport = buildReturnsCmp();

        try {
            TextFieldBuilder<String> title = DynamicReports.cmp.text("BuyHard Report - Overview");
            JasperXlsxExporterBuilder xlsxExporter = DynamicReports.export.xlsxExporter(name)
                    .setDetectCellType(true)
                    .setIgnorePageMargins(true)
                    .setWhitePageBackground(false)
                    .setRemoveEmptySpaceBetweenColumns(true);
            DynamicReports.report()
                    .addProperty(JasperProperty.EXPORT_XLS_FREEZE_ROW, "2")
                    .detail(DynamicReports.cmp.horizontalFlowList(
                            DynamicReports.cmp.subreport(totalSummary),
                            DynamicReports.cmp.subreport(assetReport),
                            DynamicReports.cmp.subreport(returnsReport)
                            ))
                    .title(title.setStyle(titleStyle))
                    .ignorePageWidth()
                    .ignorePagination()
                    .setDataSource(new JREmptyDataSource())
                    .toXlsx(xlsxExporter);
        } catch (DRException e) {
            e.printStackTrace();
        }
    }

    public void generatePDF()
    {

        // build Styles

        tableHeader = DynamicReports.stl.style()
                .bold()
                .setHorizontalAlignment(HorizontalAlignment.LEFT)
                .setBorder(DynamicReports.stl.pen1Point())
                .setBackgroundColor(Color.LIGHT_GRAY)
                .setPadding(3);
        tableBody = DynamicReports.stl.style()
                .setHorizontalAlignment(HorizontalAlignment.LEFT)
                .setBorder(DynamicReports.stl.penThin())
                .setPadding(3);
        titleStyle = DynamicReports.stl.style()
                .setFontSize(30)
                .setBottomPadding(20)
                .bold();
        subtitleStyle = DynamicReports.stl.style()
                .setFontSize(20)
                .setBottomPadding(20)
                .setTopPadding(20)
                .bold();
        boldCentered = DynamicReports.stl.style()
                .bold()
                .setHorizontalAlignment(HorizontalAlignment.CENTER);


        JasperReportBuilder totalSummary = buildTotalSummaryCmp();
        JasperReportBuilder assetReport = buildAssetCmp();
        JasperReportBuilder returnsReport = buildReturnsCmp();
        MultiPageListBuilder companyReports = buildCompanySubReports();

        JasperReportBuilder report = DynamicReports.report();
        report.setPageMargin(DynamicReports.margin(20));
        report.setPageFormat(PageType.A4, PageOrientation.LANDSCAPE);
        report.setPageColumnsPerPage(2);
        report.setPageColumnSpace(15);
        TextFieldBuilder<String> title = DynamicReports.cmp.text("BuyHard Report - Overview");
        report.title(title.setStyle(titleStyle));
        report.detail(DynamicReports.cmp.multiPageList(
                        DynamicReports.cmp.subreport(totalSummary),
                        DynamicReports.cmp.subreport(assetReport),
                        DynamicReports.cmp.subreport(returnsReport)),
                companyReports
        );
        report.setDataSource(new JREmptyDataSource());

        // Sum columns.
        report.pageFooter(DynamicReports.cmp.pageXofY().setStyle(boldCentered)); //shows number of page at page footer



        try {
            report.toPdf(new FileOutputStream(new File(pdfReportName)));
            report.show();

        } catch (DRException | FileNotFoundException e) {
            e.printStackTrace();
        }
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
        // Asset value
        TextFieldBuilder<String> subtitle = DynamicReports.cmp.text("Equities");
        JRDataSource assetData = createDataSource();
        JasperReportBuilder assetReport = DynamicReports.report();
        assetReport.title(subtitle.setStyle(subtitleStyle));
        assetReport.columns(companyNameColumn, equityValueColumn);
        assetReport.setColumnHeaderStyle(tableHeader);
        assetReport.setColumnStyle(tableBody);
        assetReport.setDataSource(assetData);

        // Sum of Equities.
        assetReport.subtotalsAtSummary(DynamicReports.sbt.sum(equityValueColumn));

        return assetReport;
    }

    private JasperReportBuilder buildReturnsCmp()
    {
        TextColumnBuilder<String> companyNameColumn = DynamicReports.col.column("Company", "company", DynamicReports.type.stringType());
        TextColumnBuilder<Double> returnValueColumn = DynamicReports.col.column("Return", "returnValue", DynamicReports.type.doubleType());
        TextColumnBuilder<Double> returnPercentageColumn = DynamicReports.col.column("Return %", "returnPercentage", DynamicReports.type.doubleType()); //TODO THis should use percentType.
        // Return value
        TextFieldBuilder<String> subtitle = DynamicReports.cmp.text("Returns");
        JRDataSource resultsData = createDataSource();
        JasperReportBuilder returnsReport = DynamicReports.report();
        returnsReport.title(subtitle.setStyle(subtitleStyle));
        returnsReport.columns(companyNameColumn, returnValueColumn, returnPercentageColumn);
        returnsReport.setColumnHeaderStyle(tableHeader);
        returnsReport.setColumnStyle(tableBody);
        returnsReport.setDataSource(resultsData);

        // Sum of Returns.
        returnsReport.subtotalsAtSummary(DynamicReports.sbt.sum(returnValueColumn), DynamicReports.sbt.sum(returnPercentageColumn));

        return returnsReport;
    }

    private MultiPageListBuilder buildCompanySubReports()
    {
        TextColumnBuilder<String> companyNameColumn = DynamicReports.col.column("Company", "company", DynamicReports.type.stringType());
        TextColumnBuilder<Double> priceColumn = DynamicReports.col.column("Price", "price", DynamicReports.type.doubleType());
        TextColumnBuilder<Integer> volumeColumn = DynamicReports.col.column("Volume", "volume", DynamicReports.type.integerType());
        TextColumnBuilder<String> signalColumn = DynamicReports.col.column("Signal", "signal", DynamicReports.type.stringType());
        TextColumnBuilder<String> dateColumn = DynamicReports.col.column("Date", "date", DynamicReports.type.stringType());

        TextFieldBuilder<String> subtitle;

        // Create company subReports
        MultiPageListBuilder companyReports = new DynamicReports().cmp.multiPageList();
        History<Order> companyHistories = portfolioData.getOrderHistory();
        for (String c : companyHistories.getAllCompanies()) {
            JasperReportBuilder companyReport = DynamicReports.report();
            subtitle = DynamicReports.cmp.text(c);
            companyReport.title(subtitle.setStyle(subtitleStyle));
            companyReport.columns(companyNameColumn, dateColumn, priceColumn, volumeColumn, signalColumn);
            companyReport.setColumnHeaderStyle(tableHeader);
            companyReport.setColumnStyle(tableBody);
            companyReport.setDataSource(createCompanyDataSource(companyHistories.getCompanyHistory(c)));
            companyReports.add(DynamicReports.cmp.subreport(companyReport));
        }

        return companyReports;
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
                           returns.get(company).getReturns());
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


}