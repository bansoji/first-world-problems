package main;

import net.sf.dynamicreports.jasper.builder.JasperReportBuilder;
import net.sf.dynamicreports.report.builder.DynamicReports;
import net.sf.dynamicreports.report.builder.column.TextColumnBuilder;
import net.sf.dynamicreports.report.builder.component.TextFieldBuilder;
import net.sf.dynamicreports.report.builder.style.StyleBuilder;
import net.sf.dynamicreports.report.constant.HorizontalAlignment;
import net.sf.dynamicreports.report.constant.PageOrientation;
import net.sf.dynamicreports.report.constant.PageType;
import net.sf.dynamicreports.report.datasource.DRDataSource;
import net.sf.dynamicreports.report.exception.DRException;
import net.sf.jasperreports.engine.JRDataSource;

import javax.activation.DataSource;
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

    private static Portfolio portfolioData;

    public ReportGenerator(Portfolio p){
        this.portfolioData = p;
    }

    public static void generateReport(){ // was made static for testing purposes eg. was using main before
        //  Create styles
        StyleBuilder tableHeader = DynamicReports.stl.style()
                .bold()
                .setHorizontalAlignment(HorizontalAlignment.LEFT)
                .setBorder(DynamicReports.stl.pen1Point())
                .setBackgroundColor(Color.LIGHT_GRAY)
                .setPadding(3);
        StyleBuilder tableBody = DynamicReports.stl.style()
                .setHorizontalAlignment(HorizontalAlignment.LEFT)
                .setBorder(DynamicReports.stl.penThin())
                .setPadding(3);
        StyleBuilder titleStyle = DynamicReports.stl.style()
                .setFontSize(30)
                .bold();
        StyleBuilder subtitleStyle = DynamicReports.stl.style()
                .setFontSize(20)
                .setBottomPadding(20)
                .setTopPadding(20)
                .bold();


        //  Create Columns
        TextColumnBuilder<String> companyNameColumn = DynamicReports.col.column("Company", "company", DynamicReports.type.stringType());
        TextColumnBuilder<Double> equityValueColumn = DynamicReports.col.column("Equity", "equity", DynamicReports.type.doubleType());
        TextColumnBuilder<Double> returnValueColumn = DynamicReports.col.column("Return", "returnValue", DynamicReports.type.doubleType());
        TextColumnBuilder<Double> returnPercentageColumn = DynamicReports.col.column("Return %", "returnPercentage", DynamicReports.type.doubleType());

        TextFieldBuilder<String> subtitle;

        // Total Summary subReport
        JasperReportBuilder totalSummary = DynamicReports.report();
        subtitle = DynamicReports.cmp.text("Summary");
        totalSummary.title(subtitle.setStyle(subtitleStyle));
        totalSummary.addTitle(DynamicReports.cmp.text("Total return: " + portfolioData.getTotalReturnValue() + "\nTotal return %: "));



        // Asset value subReport
        subtitle = DynamicReports.cmp.text("Equities");
        JRDataSource assetData = createDataSource();
        JasperReportBuilder assetReport = DynamicReports.report();
        assetReport.title(subtitle.setStyle(subtitleStyle));
        assetReport.columns(companyNameColumn, equityValueColumn);
        assetReport.setColumnHeaderStyle(tableHeader);
        assetReport.setColumnStyle(tableBody);
        assetReport.setDataSource(assetData);


        // Return value subReport
        subtitle = DynamicReports.cmp.text("Returns");
        JRDataSource resultsData = createDataSource();
        JasperReportBuilder returnsReport = DynamicReports.report();
        returnsReport.title(subtitle.setStyle(subtitleStyle));
        returnsReport.columns(companyNameColumn, returnValueColumn, returnPercentageColumn);
        returnsReport.setColumnHeaderStyle(tableHeader);
        returnsReport.setColumnStyle(tableBody);
        returnsReport.setDataSource(resultsData);

        // Main Report
        JasperReportBuilder report = DynamicReports.report();
        TextFieldBuilder<String> title = DynamicReports.cmp.text("BuyHard Report - Overview");
        report.title(title.setStyle(titleStyle));
        report.title(DynamicReports.cmp.multiPageList(
                DynamicReports.cmp.subreport(totalSummary),
                DynamicReports.cmp.subreport(assetReport),
                DynamicReports.cmp.subreport(returnsReport))
        );
        report.setPageMargin(DynamicReports.margin(20));

        // report.setPageFormat(PageType.A4, PageOrientation.LANDSCAPE);


        try {
            report.toPdf(new FileOutputStream(new File("report.pdf")));
            report.show();
        } catch (DRException | FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    private static JRDataSource createDataSource(){
        DRDataSource dataSource = new DRDataSource("company","equity","returnValue","returnPercentage");

        // Reconstruct data set
        Map<String, List<Double>> returns = portfolioData.getReturns();
        Map<String, Double> assetValue = portfolioData.getAssetValue();
        for (String company : assetValue.keySet()) {
            dataSource.add(company, assetValue.get(company),returns.get(company).get(0),returns.get(company).get(1));
        }

        return dataSource;
    }

//    public static void main(String[] args) {
//        generateReport();
//
//    }

}
