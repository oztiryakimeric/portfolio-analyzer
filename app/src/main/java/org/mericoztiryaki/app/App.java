package org.mericoztiryaki.app;

import org.apache.commons.cli.*;
import org.mericoztiryaki.app.adapter.PriceApiAdapter;
import org.mericoztiryaki.app.reader.CsvReader;
import org.mericoztiryaki.app.reader.PortfolioReader;
import org.mericoztiryaki.domain.ReportManager;
import org.mericoztiryaki.domain.exception.PriceApiException;
import org.mericoztiryaki.domain.model.ReportRequest;
import org.mericoztiryaki.domain.model.constant.Currency;
import org.mericoztiryaki.domain.model.constant.Period;
import org.mericoztiryaki.domain.model.constant.PnlHistoryUnit;
import org.mericoztiryaki.domain.model.constant.ReportOutputType;
import org.mericoztiryaki.domain.port.PriceSource;
import org.mericoztiryaki.domain.service.impl.PriceService;
import org.mericoztiryaki.domain.service.impl.ReportService;
import org.mericoztiryaki.domain.service.impl.TransactionService;

import java.io.IOException;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Set;

public class App {

    private static PriceSource priceSource;
    private static PriceService priceService;
    private static TransactionService transactionService;
    private static ReportService reportService;
    private static ReportManager reportManager;

    public static void main(String[] args) throws IOException, ParseException {
        initializeBeans();

        createReport(readReportRequest(args));

        System.out.println("EDOM");
    }

    private static void initializeBeans() {
        priceSource = new PriceApiAdapter();
        priceService = new PriceService(priceSource);
        transactionService = new TransactionService(priceService);
        reportService = new ReportService(priceService, transactionService);
        reportManager = new ReportManager(reportService);
    }

    private static ReportRequest readReportRequest(String[] args) throws ParseException {
        CommandLineParser parser = new DefaultParser();

        Options options = new Options();
        options.addOption(Option.builder()
                .option("d")
                .longOpt("date")
                .hasArg()
                .desc("Date the report will be generated")
                .build());

        options.addOption(Option.builder()
                .option("i")
                .longOpt("input-file")
                .hasArg()
                .required()
                .desc("Csv file path which transactions defined in")
                .build());

        ReportRequest reportRequest = ReportRequest.getDefaultReportRequest();
        CommandLine line = parser.parse(options, args);

        if (line.hasOption("date")) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("DD-MM-YYYY");
            LocalDate date = LocalDate.parse(line.getOptionValue("date"), formatter);

            reportRequest.setReportDate(date);
        }

        if (line.hasOption("input-file")) {
            reportRequest.setInputFileLocation(line.getOptionValue("input-file"));
        }

        return reportRequest;
    }

    private static void createReport(ReportRequest reportRequest) throws IOException {
        PortfolioReader csvReader = new CsvReader(reportRequest.getInputFileLocation());
        reportRequest.setTransactions(csvReader.read());

        reportManager.generateReport(reportRequest);
    }

}
