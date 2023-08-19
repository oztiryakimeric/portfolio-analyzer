package org.mericoztiryaki.app;

import lombok.extern.log4j.Log4j2;
import org.apache.commons.cli.*;
import org.mericoztiryaki.app.adapter.PriceApiAdapter;
import org.mericoztiryaki.app.reader.CsvReader;
import org.mericoztiryaki.app.reader.PortfolioReader;
import org.mericoztiryaki.app.util.CliParser;
import org.mericoztiryaki.domain.ReportManager;
import org.mericoztiryaki.domain.exception.PriceApiException;
import org.mericoztiryaki.domain.exception.ReportGenerationException;
import org.mericoztiryaki.domain.model.ReportRequest;
import org.mericoztiryaki.domain.model.constant.Currency;
import org.mericoztiryaki.domain.model.constant.Period;
import org.mericoztiryaki.domain.model.constant.PnlHistoryUnit;
import org.mericoztiryaki.domain.model.constant.ReportOutputType;
import org.mericoztiryaki.domain.port.PriceSource;
import org.mericoztiryaki.domain.service.impl.PriceService;
import org.mericoztiryaki.domain.service.impl.ReportService;
import org.mericoztiryaki.domain.service.impl.TransactionService;
import org.mericoztiryaki.domain.util.ExecutorManager;

import java.io.IOException;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Set;

@Log4j2
public class App {

    private static PriceSource priceSource;
    private static PriceService priceService;
    private static TransactionService transactionService;
    private static ReportService reportService;
    private static ReportManager reportManager;

    public static void main(String[] args) {
        try {
            initializeBeans();

            createReport(CliParser.buildReportRequest(args));
        } catch (ReportGenerationException e) {
            log.error(e.getMessage());
        } finally {
            ExecutorManager.shutdown();
        }
    }

    private static void initializeBeans() {
        priceSource = new PriceApiAdapter();
        priceService = new PriceService(priceSource);
        transactionService = new TransactionService(priceService);
        reportService = new ReportService(priceService, transactionService);
        reportManager = new ReportManager(reportService);
    }

    private static void createReport(ReportRequest reportRequest) throws ReportGenerationException {
        PortfolioReader csvReader = new CsvReader(reportRequest.getInputFileLocation());
        reportRequest.setTransactions(csvReader.read());

        reportManager.generateReport(reportRequest);
    }

}
