package main;

import net.sf.dynamicreports.jasper.builder.JasperReportBuilder;
import net.sf.dynamicreports.report.builder.DynamicReports;
import net.sf.dynamicreports.report.builder.column.TextColumnBuilder;
import net.sf.dynamicreports.report.builder.component.TextFieldBuilder;
import net.sf.dynamicreports.report.exception.DRException;

import java.awt.*;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;

/**
 * This class is responsible for generating reports.
 */
public class ReportGenerator {

    private Portfolio portfolioData;

    public ReportGenerator(Portfolio p){
        this.portfolioData = p;
    }

    public static void generateReport(){
        //  Create Columns
        TextColumnBuilder<String> companyName = DynamicReports.col.column("Item", "item", DynamicReports.type.stringType());

        JasperReportBuilder report = DynamicReports.report();

        // Components.
        TextFieldBuilder<String> title = DynamicReports.cmp.text("BuyHard Report");
        report.title(title);


        try {
            report.toPdf(new FileOutputStream(new File("report.pdf")));
            report.show();
        } catch (DRException | FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        generateReport();

    }

}
