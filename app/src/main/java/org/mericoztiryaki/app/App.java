package org.mericoztiryaki.app;

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
import java.util.Set;

public class App {

    public static void main(String[] args) throws IOException, PriceApiException {
        PriceSource priceSource = new PriceApiAdapter();
        PriceService priceService = new PriceService(priceSource);
        TransactionService transactionService = new TransactionService(priceService);
        ReportService reportService = new ReportService(priceService, transactionService);

        ReportManager reportManager = new ReportManager(reportService);

        PortfolioReader csvReader = new CsvReader("/Users/meric/dev/portfolio-vis/.dev-space/dev-portfolio-3.csv");

        ReportRequest reportRequest = ReportRequest.builder()
                .transactions(csvReader.read())
                .reportDate(LocalDate.now())
                .periods(Set.of(Period.D1, Period.W1, Period.M1, Period.ALL))
                .pnlHistoryUnits(Set.of(PnlHistoryUnit.DAY, PnlHistoryUnit.WEEK, PnlHistoryUnit.MONTH, PnlHistoryUnit.YEAR))
                .currencies(Set.of(Currency.TRY, Currency.USD))
                .outputType(ReportOutputType.EXCEL)
                .outputFileLocation(Paths.get("").toAbsolutePath().toString() + "/out.xlsx")
                .build();

        reportManager.generateReport(reportRequest);

        System.out.println("EDOM");
    }

}
