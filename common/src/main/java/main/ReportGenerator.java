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
 * This class is responsible for generating reports.
 */
public class ReportGenerator {

    private static Portfolio portfolioData;

    public ReportGenerator(Portfolio p){
        this.portfolioData = p;
        System.out.println(portfolioData.getReturns().get(0)); // seeing if the report has anything
    }

    public static void generateReport(){
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
                .setBottomPadding(20)
                .bold();

        //  Create Columns
        TextColumnBuilder<String> companyNameColumn = DynamicReports.col.column("Company", "company", DynamicReports.type.stringType());
        TextColumnBuilder<Double> equityValueColumn = DynamicReports.col.column("Equity", "equity", DynamicReports.type.doubleType());
        TextColumnBuilder<Double> returnValueColumn = DynamicReports.col.column("Return", "returnValue", DynamicReports.type.doubleType());
        TextColumnBuilder<Double> returnPercentageColumn = DynamicReports.col.column("Return %", "returnPercentage", DynamicReports.type.doubleType());



        // Asset value subReport
        JRDataSource db = createDataSource();
        JasperReportBuilder assetReport = DynamicReports.report();
        assetReport.columns(companyNameColumn, equityValueColumn);
        assetReport.setColumnHeaderStyle(tableHeader);
        assetReport.setColumnStyle(tableBody);
        assetReport.setDataSource(db);


        // Return value subReport
        JRDataSource db1 = createDataSource();
        JasperReportBuilder returnsReport = DynamicReports.report();
        returnsReport.columns(companyNameColumn, returnValueColumn, returnPercentageColumn);
        returnsReport.setColumnHeaderStyle(tableHeader);
        returnsReport.setColumnStyle(tableBody);
        returnsReport.setDataSource(db1);

        // Main Report
        JasperReportBuilder report = DynamicReports.report();
        TextFieldBuilder<String> title = DynamicReports.cmp.text("BuyHard Report - Overview");
        report.title(title.setStyle(titleStyle));
        report.title(DynamicReports.cmp.verticalList(DynamicReports.cmp.subreport(assetReport), DynamicReports.cmp.subreport(returnsReport)));
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
